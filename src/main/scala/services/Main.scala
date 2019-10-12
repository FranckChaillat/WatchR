package services

import java.text.SimpleDateFormat

import com.typesafe.config.{Config, ConfigFactory}
import dataaccess.MongoBillingRepo
import exceptions.LoginException
import org.mongodb.scala.MongoClient
import org.openqa.selenium.chrome.ChromeDriver
import scalaz.Scalaz._
import utils.{Configuration, DriverFactory}

import scala.concurrent.Future
import scala.util.{Failure, Try}

object Main {

  def main(args: Array[String]): Unit = {

    lazy implicit val config: Config = ConfigFactory.load()
    lazy implicit val driver: ChromeDriver = DriverFactory.buildDriver()
    lazy val configuration = Configuration.getConfiguration
    lazy val mongoDriver = MongoClient(configuration.connectionString)

    Try {
      (args(0), args(1))
    }.recoverWith {
      case _: Throwable => Failure(LoginException())
    }.map {
      case (login, pwd) =>
        WatcherService.connect(login, pwd)
        val date = new SimpleDateFormat("dd/MM/yyyy").parse("07/07/2019")
        val billingRows = WatcherService.getPaymentHistory(driver, date)
        val insertRes = billingRows.map(r => MongoBillingRepo.insertBilling(r))
          .sequenceU
          .run(mongoDriver)

        Future.sequence(insertRes)
    }
  }
}
