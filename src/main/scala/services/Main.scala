package services

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

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

    val configuration = Configuration.getConfiguration()
    val billingActor = system.actorOf(Props(new BillingActor(configuration)), name = "BillingActor")
    val bindingFuture = Http().bindAndHandle(Router.routes(billingActor), "127.0.0.1", 9000)

    val fmt = new SimpleDateFormat("dd/MM/yyyy")
    val date = configuration.dayoffset.map { o =>
      val c = Calendar.getInstance()
      c.setTime(new Date())
      c.add(Calendar.DATE, -o)
      fmt.format(c.getTime)
    }.getOrElse {
      fmt.format(new Date())
    }

    system.scheduler.schedule(0 minute,  configuration.triggerIntervalSeconds.getOrElse(60) seconds)(billingActor ! RegisterBilling(date))
    Await.result(system.whenTerminated, Duration.Inf)
    system.registerOnTermination(() => bindingFuture.flatMap(_.unbind()))
  }
}
