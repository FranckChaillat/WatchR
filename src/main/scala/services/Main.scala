package services

import java.util.Date

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}
import dataaccess.MongoBillingRepo
import entities.commands.RegisterBilling
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}
import org.mongodb.scala.MongoClient
import utils.configuration.{ConfigObject, Configuration}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

//TODO: Templatiser l'addresse serveur + nom de BDD
object Main {

  def main(args: Array[String]): Unit = {

    implicit val jsonFormats: Formats = DefaultFormats
    implicit val config: Config = ConfigFactory.load()
    val system = ActorSystem("Billing")
    val configuration = Configuration.getConfiguration(read[ConfigObject](args(0)))

    val billingActor = system.actorOf(Props(new BillingActor(configuration)))
    system.scheduler.schedule(0 seconds, 1 minute)(billingActor ! RegisterBilling(new Date(System.currentTimeMillis)))
  }
}
