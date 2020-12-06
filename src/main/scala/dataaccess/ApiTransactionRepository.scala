package dataaccess
import java.text.SimpleDateFormat
import java.util.Date

import entities.BillingRow
import entities.dto.{AddTransactionRequest, BulkAddTransactionRequest}
import scalaz.Kleisli

import scala.concurrent.{ExecutionContext, Future}

object ApiTransactionRepository extends BillingRepo {

  def insertBilling(rows: Seq[BillingRow], limitDate: Date)(implicit ec: ExecutionContext): Kleisli[Future, ApiRepository, Unit] = Kleisli {
    httpRepository =>
      val fmt = new SimpleDateFormat("yyyy-MM-dd")
      val request = BulkAddTransactionRequest(
        transactions = rows.map(r => AddTransactionRequest(r.accountId, fmt.format(r.operationDate), fmt.format(r.valueDate), r.amount, r.label, r.category)),
        overwrite = true,
        limitDate = Some(fmt.format(limitDate))
      )
      httpRepository.httpConnector.post[BulkAddTransactionRequest, String](s"${httpRepository.baseUri}/payments/bulk", request)
        .map(_ => ())
  }

  def getBilling(accountId: Int, startDate: Date, endDate: Date)(implicit ec: ExecutionContext): Kleisli[Future, ApiRepository, Seq[BillingRow]] = Kleisli {
    httpRepository =>
      val fmt = new SimpleDateFormat("yyyy-MM-dd")
      val params = Map(
        "startDate"-> fmt.format(startDate),
        "endDate" -> fmt.format(endDate),
        "accountId" -> accountId.toString
      )
      httpRepository.httpConnector.get[Seq[BillingRow]](s"${httpRepository.baseUri}/payments", params)
  }
}
