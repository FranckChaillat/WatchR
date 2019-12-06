package services

import com.typesafe.config.{Config, ConfigFactory}
import dataaccess.{MongoBillingRepo, Repositories}
import org.json4s.{DefaultFormats, Formats}
import org.mongodb.scala.MongoClient
import org.openqa.selenium.chrome.ChromeDriver
import utils.DriverFactory
import utils.configuration.{ConfigObject, Configuration}
import org.json4s.jackson.Serialization.read
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

//TODO: Templatiser l'addresse serveur + nom de BDD
object Main {

  private def getMongoRepository(connectionString: String): MongoBillingRepo = {
    new MongoBillingRepo {
      override val dbDriver: MongoClient = MongoClient(connectionString)
    }
  }

  def main(args: Array[String]): Unit = {

    implicit val jsonFormats: Formats = DefaultFormats
    implicit val config: Config = ConfigFactory.load()
    val configuration = Configuration.getConfiguration(read[ConfigObject](args(0)))

    lazy val repositories = new Repositories {
      override def billingRepo: MongoBillingRepo = getMongoRepository(configuration.connectionString)

      override def crawlingRepo: ChromeDriver = DriverFactory.buildDriver()
    }

    val future = WatcherService.registerBilling(configuration.login, configuration.pwd).run(repositories)
          .map { _ =>
            println("Billing insertion have been done properly.")
            "OK"
          }.recover {
             case e: Throwable =>
              println(s"An error occurred while registering billing: ${e.getMessage}")
              e.getMessage
          }

    Await.result(future, 2.minutes)
  }
}
