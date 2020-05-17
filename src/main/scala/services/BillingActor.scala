package services

import java.text.SimpleDateFormat

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.Materializer
import dataaccess._
import entities.commands.RegisterBilling
import org.json4s.DefaultFormats
import org.openqa.selenium.chrome.ChromeDriver
import utils.DriverFactory
import utils.configuration.Configuration

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}


class BillingActor(config: Configuration)(implicit ec: ExecutionContext, materializer: Materializer, actorSystem: ActorSystem) extends Actor{

  private implicit val fmt: DefaultFormats.type = DefaultFormats

  lazy val repositories: Repositories = new Repositories {
    override def billingRepo: BillingRepo = ApiTransactionRepository
    override val crawlingRepo: ChromeDriver = DriverFactory.buildDriver()
    override def crawlingService: CrawlingService = ChromeService
    override def httpConnector: ApiRepository = new ApiRepository {
      override def baseUri: String = s"${config.joeUri}/joe"
      override def httpConnector: HttpConnector = new AkkaHttpConnector(Http())
    }
  }

  override def receive: Receive = {
    case RegisterBilling(limitDate: String) =>
      val fmt = new SimpleDateFormat("dd/MM/yyyy")
      val result = WatcherService.registerBilling(config.login, config.pwd, fmt.parse(limitDate))
        .run(repositories)
        .map { _ =>
          println("Billing insertion have been done properly.")
        }.recover {
           case e: Throwable =>
            println(s"An error occurred while registering billing: ${e.getMessage}")
            e.getMessage
        }
      Await.result(result, 1 minute)
      repositories.crawlingRepo.close()

  }
}
