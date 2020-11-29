package dataaccess
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.headers.RawHeader
import org.json4s.jackson.Serialization.{read, write}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpHeader, HttpMethods, HttpRequest, Uri}
import akka.stream.Materializer
import akka.util.ByteString
import org.json4s.Formats

import scala.concurrent.{ExecutionContext, Future}

class AkkaHttpConnector(http: HttpExt)(implicit formats: Formats, materializer: Materializer, ec: ExecutionContext) extends HttpConnector {

  override def post[T <: AnyRef, U <: AnyRef](uri: String, entity: T)(implicit mf: Manifest[U]): Future[U] = {
   val request =  HttpRequest(uri = uri, method = HttpMethods.POST, entity = HttpEntity(ContentTypes.`application/json`, write[T](entity)))
   http.singleRequest(request)
      .flatMap { result =>
        result.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(c  => read[U](c.toString()))
      }
  }

  override def get[U <: AnyRef](uri: String, queryParams: Map[String, String])(implicit mf: Manifest[U]): Future[U] = {
    val u = Uri(s"""$uri?${queryParams.map(e => s"${e._1}=${e._2}").mkString("&")}""")
    val request =  HttpRequest(uri = u, method = HttpMethods.GET)
    http.singleRequest(request)
      .flatMap { result =>
        result.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(c  => {
          read[U](c.utf8String)
        })
      }
  }

  override def put[T, U](uri: String, entity: T): Future[U] = ???
}
