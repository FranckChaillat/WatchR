package services

import java.util.Date

import akka.actor.Actor
import dataaccess.{MongoBillingRepo, Repositories}
import entities.commands.RegisterBilling
import org.mongodb.scala.MongoClient
import org.openqa.selenium.chrome.ChromeDriver
import utils.DriverFactory
import utils.configuration.Configuration

import scala.concurrent.ExecutionContext

class BillingActor(config: Configuration)(implicit ec: ExecutionContext) extends Actor{

  private def getMongoRepository(connectionString: String): MongoBillingRepo =
    new MongoBillingRepo { override val dbDriver: MongoClient = MongoClient(connectionString) }

  lazy val repositories: Repositories = new Repositories {
    override def billingRepo: MongoBillingRepo = getMongoRepository(config.connectionString)
    override def crawlingRepo: ChromeDriver = DriverFactory.buildDriver()
  }

  override def receive: Receive = {
    case RegisterBilling(date: Date) =>
    WatcherService.registerBilling(config.login, config.pwd, date)
      .run(repositories)
      .map { _ =>
        println("Billing insertion have been done properly.")
        "OK"
      }.recover {
         case e: Throwable =>
          println(s"An error occurred while registering billing: ${e.getMessage}")
          e.getMessage
      }
  }
}
