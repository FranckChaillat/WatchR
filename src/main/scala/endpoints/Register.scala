package endpoints

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import entities.commands.RegisterBilling
import entities.dto.RegisterRequest
import org.json4s.{DefaultFormats, Formats}
import org.json4s.jackson.Serialization.{read, write}
import scalaz.Reader
import akka.pattern.ask
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Register {

  private implicit def format: Formats = DefaultFormats
  private implicit val timeout: Timeout = Timeout(30 seconds)

  def route()(implicit ec: ExecutionContext): Reader[ActorRef, Route] = Reader { actorRef =>
    post {
      path("register") {
        decodeRequest {
          entity(as[String]) { str =>
            val res = (actorRef ? RegisterBilling(read[RegisterRequest](str).limitDate))
              .mapTo[Unit]
              .map(_ => HttpResponse(StatusCodes.OK, entity =  HttpEntity(ContentTypes.`application/json`, "Ok")))
            complete(res)
          }
        }
      }
    }
  }

}
