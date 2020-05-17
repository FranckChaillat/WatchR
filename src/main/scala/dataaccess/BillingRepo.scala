package dataaccess

import java.util.Date

import entities.BillingRow
import scalaz.Kleisli

import scala.concurrent.{ExecutionContext, Future}


trait BillingRepo {
  def insertBilling(rows : Seq[BillingRow], limitDate: Date)(implicit ec: ExecutionContext) : Kleisli[Future, ApiRepository, Unit]
  def getBilling(accountId: Int, startDate: Date, endDate: Date)(implicit ec: ExecutionContext) : Kleisli[Future, ApiRepository, Seq[BillingRow]]
}
