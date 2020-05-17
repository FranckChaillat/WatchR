package services

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.actor.{ActorSystem, ClassicActorSystemProvider, Props}
import akka.dispatch.MessageDispatcher
import akka.stream.{Materializer, SystemMaterializer}
import com.typesafe.config.{Config, ConfigFactory}
import entities.commands.RegisterBilling
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}
import utils.configuration.{ConfigObject, Configuration}

import scala.concurrent.duration._


object Main {

  def main(args: Array[String]): Unit = {
    implicit val jsonFormats: Formats = DefaultFormats
    implicit val config: Config = ConfigFactory.load()
    implicit val system: ActorSystem = ActorSystem("watcher")
    implicit val materializer: Materializer = SystemMaterializer(system).materializer

    implicit val executionContext: MessageDispatcher = system.dispatchers.lookup("billing-dispatcher")
    val configuration = Configuration.getConfiguration(read[ConfigObject](args(0)))

    val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val date = fmt.format(LocalDateTime.now())

    val billingActor = system.actorOf(Props(new BillingActor(configuration)), name = "BillingActor")
    system.scheduler.schedule(0 seconds, 10 minute)(billingActor ! RegisterBilling("01/04/2020"/*date*/))
  }
}
