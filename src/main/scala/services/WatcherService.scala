package services

import java.util.Date

import dataaccess.Repositories
import entities.BillingRow
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.openqa.selenium.{By, JavascriptExecutor, WebElement}
import scalaz.{Kleisli, Reader}

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
object WatcherService {

  def registerBilling(login: String, pwd: String, date: Date)(implicit ec: ExecutionContext) : Kleisli[Future, Repositories, Unit] = Kleisli {
    repositories : Repositories =>
      val fetchRows = for {
        _ <- connect(login, pwd)
        r <- getPaymentHistory(date)
      } yield r

      val rows = fetchRows(repositories.crawlingRepo)

      repositories.billingRepo.insertBilling(rows)
        .run(repositories.billingRepo.dbDriver)
  }

  def getPaymentHistory(limitDate: Date): Reader[ChromeDriver, Seq[BillingRow]] = Reader {
    driver =>
      getElement(driver)("//*[@id=\"app\"]/div/bux-modal[2]/div/div/button")(e => e.click())
      Thread.sleep(100)
      val accountPath = "//*[@id=\"layout\"]/bux2-card[1]/bux2-card-body/bux2-widget-account/bux2-link/a"
      getElement(driver)(accountPath)(_.click())
      Thread.sleep(100)
      getBillingRows(List())(driver, limitDate)
  }

  //TODO: gérer la pagination sur la nouvelle version du site
  @tailrec
  def getBillingRows(acc: List[BillingRow])(driver: ChromeDriver, limitDate: Date): List[BillingRow] = {
    val tablePath = "//*[@id=\"tab01\"]/ul"//"
    val nextButton = "//*[@id=\"titrePageFonctionnelle\"]/div[3]/div/div[2]/div/div/div[1]/div/div[5]/table/tbody/tr/td[3]/div/img[1]"
    val parsedRows =  parseBilling(getElement(driver)(tablePath)(identity))
    val newAcc = acc ++ parsedRows
    val needMoreRows = newAcc.sortBy(_.operationDate)
      .headOption.exists(_.operationDate.compareTo(limitDate) > 0)
    if(needMoreRows) {
      getElement(driver)(nextButton) { x => x.click() }
      getBillingRows(newAcc)(driver, limitDate)
    } else {
      newAcc
    }
  }

  def parseBilling(paymentTab: WebElement): Seq[BillingRow] = {
    val lines = paymentTab.findElements(By.tagName("ul")).asScala.toList
      .flatMap(x => x.findElements(By.tagName("li")).asScala.toList)
      .map(x => x.getText)
      .map { x =>
        x.split("\\n") match {
          case BillingRow(e: BillingRow) => Right(e)
          case _ => Left(x)
        }
      }

    //TODO: Manage parsing Errors
    val (ko, ok) = lines.partition(x => x.isLeft)
    ok.collect {case Right(e) => e }
  }

  def parseBillingRows(billingTabContent : String) : List[BillingRow] = {
    val rows = billingTabContent.split("\\n")
    val groups = rows.grouped(4).toList
    groups.collect {
      case BillingRow(e: BillingRow) => e
    }
  }

  private def getElement[T](driver: ChromeDriver)(elementPath: String)(action: WebElement => T): T = {
    val script = s"""
                   |function func() {
                   | path = '$elementPath'
                   | return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
                   |}
                   |return func();
                 """.stripMargin

    val wait = new WebDriverWait(driver, 10)
    val location = By.xpath(elementPath)
    wait.until(ExpectedConditions.presenceOfElementLocated(location))

    val jsExe = driver.asInstanceOf[JavascriptExecutor]
    val executed = jsExe.executeScript(script)
    val element = executed.asInstanceOf[WebElement]

    jsExe.executeScript("arguments[0].scrollIntoView();", element)
    Thread.sleep(100)
    action(element)
  }

  private def connect(login: String, pwd: String): Reader[ChromeDriver, Unit] = Reader {
    driver =>
      driver.get("https://www.cmso.com/banque/assurance/credit-mutuel/web/j_6/accueil")
      getElement(driver)("//*[@id=\"connexion-link\"]")(e => e.click())
      getElement(driver)("//*[@id=\"userLogin\"]")(e => e.sendKeys(login))
      getElement(driver)("//*[@id=\"auth-c_1\"]/button")(e => e.click())
      getElement(driver)("//*[@id=\"userPassword\"]")(element => element.sendKeys(pwd))
      driver.findElement(By.xpath("//*[@id=\"formLogin\"]/div[3]/div[2]/button")).click()
  }

}
