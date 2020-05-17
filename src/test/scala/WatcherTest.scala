import java.util.Date

import dataaccess.{ApiRepository, BillingRepo, Repositories}
import entities.BillingRow
import org.mockito.ArgumentCaptor
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.openqa.selenium.chrome.ChromeDriver
import scalaz.{Kleisli, Reader}
import services.{CrawlingService, WatcherService}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, ExecutionContext, Future}

class WatcherTest extends FlatSpec with MockitoSugar {

  "it" should "merge correctly actual and collected rows" in {
    val dDate = new Date()
    val collected = Seq(
      BillingRow("id1", 1, dDate,dDate, "label1", amount = 2.3f),
      BillingRow("id2", 1, dDate,dDate, "label2", amount = 9.8f),
      BillingRow("id3", 1,dDate,dDate, "label3", amount = 2.3f),
      BillingRow("id4", 1,dDate,dDate, "label4", amount = 15.0f)
    )

    val existing = Seq(
      BillingRow("id1", 1,dDate,dDate, "label1", amount = 2.3f, category = Some("cars")),
      BillingRow("id2", 1,dDate,dDate, "label2", amount = 9.8f, category = Some("cars")),
      BillingRow("id5", 1,dDate,dDate, "some hand added item", amount = 400.0f, category = Some("others"))
    )

    val repositories = mock[Repositories]
    val crawlingService = mock[CrawlingService]
    val billingRepo = mock[BillingRepo]
    val captor = ArgumentCaptor.forClass(classOf[Seq[BillingRow]])

    when(repositories.crawlingService).thenReturn(crawlingService)
    when(repositories.billingRepo).thenReturn(billingRepo)
    when(repositories.crawlingService.connect(any[String], any[String])).thenReturn(Reader[ChromeDriver, Unit]{ _ => ()})
    when(crawlingService.getPaymentHistory(any[Date])).thenReturn(Reader[ChromeDriver, Seq[BillingRow]]{ _ => collected })
    when(billingRepo.getBilling(any[Int], any[Date], any[Date])(any[ExecutionContext])).thenReturn(Kleisli[Future, ApiRepository, Seq[BillingRow]] { _ => Future(existing) })
    when(billingRepo.insertBilling(any[Seq[BillingRow]], any[Date])(any[ExecutionContext])).thenReturn(Kleisli[Future, ApiRepository, Unit]{ _ => Future.unit })


    Await.result(WatcherService.registerBilling("login", "pwd",dDate)
        .run(repositories), 5.seconds)

    verify(repositories.billingRepo).insertBilling(captor.capture(), any())(any[ExecutionContext])


    val capturedValues = captor.getValue
    assert(capturedValues.sortBy(_.identifier) == Vector(
      BillingRow("id1", 1, dDate, dDate, "label1", amount = 2.3f, category = Some("cars")),
      BillingRow("id2", 1, dDate, dDate, "label2", amount = 9.8f, category = Some("cars")),
      BillingRow("id3", 1, dDate, dDate, "label3", amount = 2.3f),
      BillingRow("id4", 1, dDate, dDate, "label4", amount = 15.0f),
      BillingRow("id5", 1, dDate, dDate, "some hand added item", amount = 400.0f, category = Some("others"))
    ))

  }

}
