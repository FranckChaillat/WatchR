package dataaccess

import scala.concurrent.Future

trait HttpConnector {
  def post[T <: AnyRef, U <: AnyRef](uri: String, entity: T)(implicit mf: Manifest[U]): Future[U]
  def get[U <: AnyRef](uri: String, queryParams: Map[String, String])(implicit mf: Manifest[U]): Future[U]
  def put[T, U](uri: String, entity: T): Future[U]
}
