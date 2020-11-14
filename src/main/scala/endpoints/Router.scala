package endpoints

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

object Router {
  def routes(actor: ActorRef)(implicit ec: ExecutionContext) : Route = pathPrefix("watcher") {
    Register.route.run(actor)
  }
}