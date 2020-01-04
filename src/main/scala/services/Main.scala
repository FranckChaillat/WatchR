package services

import java.util.Date

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}
import dataaccess.{MongoBillingRepo, Repositories}
import entities.BillingRow
import entities.commands.RegisterBilling
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}
import org.mongodb.scala.MongoClient
import org.openqa.selenium.chrome.ChromeDriver
import utils.DriverFactory
import utils.configuration.{ConfigObject, Configuration}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

//TODO: Templatiser l'addresse serveur + nom de BDD
object Test extends App {
  implicit val jsonFormats: Formats = DefaultFormats
  implicit val config: Config = ConfigFactory.load()
  val configuration = Configuration.getConfiguration(read[ConfigObject](args(0)))


  private def getMongoRepository(connectionString: String): MongoBillingRepo =
    new MongoBillingRepo { override val dbDriver: MongoClient = MongoClient(connectionString) }

  lazy val repositories: Repositories = new Repositories {
    override def billingRepo: MongoBillingRepo = getMongoRepository(configuration.connectionString)
    override def crawlingRepo: ChromeDriver = DriverFactory.buildDriver()
  }

  val res = repositories.billingRepo.insertBilling(Seq(BillingRow(new Date(System.currentTimeMillis), new Date(System.currentTimeMillis), "", 10)))
    .run(repositories.billingRepo.dbDriver)
  println(Await.result(res, 5 seconds))
  
}

object Main {

  def main(args: Array[String]): Unit = {

    implicit val jsonFormats: Formats = DefaultFormats
    implicit val config: Config = ConfigFactory.load()
    val system = ActorSystem("Billing")
    val configuration = Configuration.getConfiguration(read[ConfigObject](args(0)))

    val billingActor = system.actorOf(Props(new BillingActor(configuration)))
    system.scheduler.schedule(0 seconds, 10 minute)(billingActor ! RegisterBilling(new Date(System.currentTimeMillis)))
  }
}
