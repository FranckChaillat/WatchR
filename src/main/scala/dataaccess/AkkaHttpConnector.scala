package dataaccess
import akka.http.scaladsl.HttpExt
import org.json4s.jackson.Serialization.{write, read}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import akka.stream.Materializer
import akka.util.ByteString
import org.json4s.Formats

import scala.concurrent.{ExecutionContext, Future}

class AkkaHttpConnector(http: HttpExt)(implicit formats: Formats, materializer: Materializer, ec: ExecutionContext) extends HttpConnector {

  override def post[T <: AnyRef, U <: AnyRef](uri: String, entity: T)(implicit mf: Manifest[U]): Future[U] = {
   val request =  HttpRequest(uri = uri, method = HttpMethods.POST, entity = HttpEntity(ContentTypes.`application/json`, write[T](entity)))
   http.singleRequest(request)
      .flatMap(result => {
        result.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(c  => read[U](c.toString()))
      })
  }

  override def get[U](uri: String, queryParams: Map[String, Any]): Future[U] = ???

  override def put[T, U](uri: String, entity: T): Future[U] = ???
}
