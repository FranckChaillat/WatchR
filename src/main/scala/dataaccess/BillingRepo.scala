package dataaccess

import entities.BillingRow
import org.mongodb.scala.MongoDatabase
import scalaz.Kleisli

import scala.concurrent.Future

trait BillingRepo {
  def insertBilling(b: BillingRow) : Kleisli[Future, MongoDatabase, Unit]
  def getBillingRows(fields : Seq[String]) : Kleisli[Future, MongoDatabase, Seq[BillingRow]]
}
