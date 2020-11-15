package services

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.actor.{ActorSystem, Props}
import akka.dispatch.MessageDispatcher
import akka.http.scaladsl.Http
import akka.stream.{Materializer, SystemMaterializer}
import com.typesafe.config.{Config, ConfigFactory}
import endpoints.Router
import entities.commands.RegisterBilling
import utils.configuration.Configuration

import scala.concurrent.Await
import scala.concurrent.duration._


object Main {

  def main(args: Array[String]): Unit = {
    implicit val config: Config = ConfigFactory.load()
    implicit val system: ActorSystem = ActorSystem("watcher")
    implicit val materializer: Materializer = SystemMaterializer(system).materializer
    implicit val executionContext: MessageDispatcher = system.dispatchers.lookup("billing-dispatcher")
    val billingActor = system.actorOf(Props(new BillingActor(Configuration.getConfiguration())), name = "BillingActor")
    val bindingFuture = Http().bindAndHandle(Router.routes(billingActor), "127.0.0.1", 9000)

    val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    system.scheduler.schedule(0 minute, 30 seconds)(billingActor ! RegisterBilling(fmt.format(LocalDateTime.now())))
    Await.result(system.whenTerminated, Duration.Inf)
    system.registerOnTermination(() => bindingFuture.flatMap(_.unbind()))
  }
}
