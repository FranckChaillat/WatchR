package services

import java.util.Date

import dataaccess.Repositories
import entities.BillingRow
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.openqa.selenium.{By, JavascriptExecutor, WebElement}
import scalaz.{Kleisli, Reader}

import scala.annotation.tailrec
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
      Thread.sleep(1000)
      getElement(driver)("//*[@id=\"app\"]/div/bux-modal[2]/div/div/button")(e => e.click())
      val accountPath = "//*[@id=\"layout\"]/bux2-card[1]/bux2-card-body/bux2-widget-account/bux2-link/a"
      getElement(driver)(accountPath)(_.click())
      getBillingRows(List())(driver, limitDate)
  }

  //TODO: gérer la pagination sur la nouvelle version du site
  @tailrec
  def getBillingRows(acc: List[BillingRow])(driver: ChromeDriver, limitDate: Date): List[BillingRow] = {
    val tablePath = "//*[@id=\"tab01\"]/ul"//"//*[@id=\"titrePageFonctionnelle\"]/div[3]/div/div[2]/div/div/div[1]/div/table"
    val nextButton = "//*[@id=\"titrePageFonctionnelle\"]/div[3]/div/div[2]/div/div/div[1]/div/div[5]/table/tbody/tr/td[3]/div/img[1]"
    val payHistoryTable = getElement(driver)(tablePath)(_.getText)

    val rows = acc ++ parseBillingRows(payHistoryTable)
    val needMoreRows = rows.sortBy(_.operationDate)
      .headOption.exists(_.operationDate.compareTo(limitDate) > 0)
    if(needMoreRows) {
      getElement(driver)(nextButton) { x => x.click() }
      getBillingRows(rows)(driver, limitDate)
    } else {
      rows
    }
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
