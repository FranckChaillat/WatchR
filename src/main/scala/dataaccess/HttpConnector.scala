package dataaccess

import scala.concurrent.Future

trait HttpConnector {
  def post[T <: AnyRef, U <: AnyRef](uri: String, entity: T)(implicit mf: Manifest[U]): Future[U]
  def get[U](uri: String, queryParams: Map[String, Any]): Future[U]
  def put[T, U](uri: String, entity: T): Future[U]
}
