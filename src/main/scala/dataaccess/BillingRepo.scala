package dataaccess

import entities.BillingRow
import org.mongodb.scala.MongoClient
import scalaz.Kleisli

import scala.concurrent.{ExecutionContext, Future}


trait BillingRepo {
  def insertBilling(rows : Seq[BillingRow])(implicit ec: ExecutionContext) : Kleisli[Future, MongoClient, Unit]
  def getBillingRows(fields : Seq[String])(implicit ec: ExecutionContext) : Kleisli[Future, MongoClient, Seq[BillingRow]]
}
