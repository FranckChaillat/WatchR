package services

import java.util.Date

import dataaccess.Repositories
import entities.BillingRow
import exceptions.CrawlingException
import scalaz.Kleisli
import scalaz.std.scalaFuture._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

object WatcherService {

  def registerBilling(login: String, pwd: String, date: Date)(implicit ec: ExecutionContext) : Kleisli[Future, Repositories, Unit] = Kleisli {
    repositories : Repositories =>
      val crawlingService = repositories.crawlingService
      val fetchRows = for {
        _ <- crawlingService.connect(login, pwd)
        r <- crawlingService.getPaymentHistory(date)
      } yield r

      val collectedRows = Try(fetchRows(repositories.crawlingRepo.open()))
          .recoverWith {
            case t: Throwable =>
              Failure(CrawlingException(s"An error occured while crawling the source ${t.getMessage}", t))
          }
      Future.fromTry(collectedRows)
        .flatMap { rows =>
          if(rows.nonEmpty) {
            repositories.billingRepo.getBilling(1, date, new Date())
              .map(actual =>  BillingRow.mergeBilling(rows, actual))
              .flatMap(rows => repositories.billingRepo.insertBilling(rows, date))
              .run(repositories.httpConnector)
          } else
            Future.unit
        }
  }
}
