package services

import java.util.Date

import dataaccess.Repositories
import entities.BillingRow
import scalaz.Kleisli
import scalaz.std.scalaFuture._

import scala.concurrent.{ExecutionContext, Future}

object WatcherService {

  def registerBilling(login: String, pwd: String, date: Date)(implicit ec: ExecutionContext) : Kleisli[Future, Repositories, Unit] = Kleisli {
    repositories : Repositories =>
      val crawlingService = repositories.crawlingService
      val fetchRows = for {
        _ <- crawlingService.connect(login, pwd)
        r <- crawlingService.getPaymentHistory(date)
      } yield r

      val collectedRows = fetchRows(repositories.crawlingRepo)
      if(collectedRows.nonEmpty) {
        repositories.billingRepo.getBilling(1, date, new Date())
          .map(actual =>  mergeBilling(collectedRows.toSet, actual.toSet))
          .flatMap(rows => repositories.billingRepo.insertBilling(rows, date))
          .run(repositories.httpConnector)
      } else
        Future.unit
  }

//  private def mergeBilling(collected: Seq[BillingRow], actual: Seq[BillingRow]) = {
//    val categoryMapping = actual.groupBy(_.identifier).map(x => x._1 -> x._2.head.category)
//    collected.map(x => {
//      val maybeCategory = categoryMapping.get(x.identifier).flatten
//      maybeCategory.map(c => x.copy(category = Some(c))).getOrElse(x)
//    })
//  }

  private def mergeBilling(collected: Set[BillingRow], actual: Set[BillingRow]) = {
    val categoryMapping = actual.groupBy(_.identifier).map(x => x._1 -> x._2.head.category)
    val merged = collected.map(x => {
      val maybeCategory = categoryMapping.get(x.identifier).flatten
      maybeCategory.map(c => x.copy(category = Some(c))).getOrElse(x)
    })

    (actual.diff(merged) ++ merged).toSeq
  }

}
