package services

import java.text.SimpleDateFormat
import java.util.Date

import dataaccess.Repositories
import entities.BillingRow
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.openqa.selenium.{By, JavascriptExecutor, WebElement}
import scalaz.{Kleisli, Reader}

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
object WatcherService {

  def registerBilling(login: String, pwd: String)(implicit ec: ExecutionContext) : Kleisli[Future, Repositories, Unit] = Kleisli {
    repositories : Repositories =>
      val date = new SimpleDateFormat("dd/MM/yyyy").parse("30/11/2019")
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
      val path ="//*[@id=\"titrePageFonctionnelle\"]/div[2]/div/div/div[1]/div[2]/div[1]/div[2]/div/div[1]/div/div[1]/div[1]/div[1]/div/a"
      getElement(driver)(path)(_.click())
      getBillingRows(List())(driver, limitDate)
  }

  @tailrec
  def getBillingRows(acc: List[BillingRow])(driver: ChromeDriver, limitDate: Date): List[BillingRow] = {
    val tablePath = "//*[@id=\"titrePageFonctionnelle\"]/div[3]/div/div[2]/div/div/div[1]/div/table"
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
    action(element)
  }


  private def connect(login: String, pwd: String): Reader[ChromeDriver, Unit] = Reader {
    driver =>
      driver.get("https://www.cmso.com/banque/assurance/credit-mutuel/web/j_6/accueil")
      getElement(driver)("//*[@id=\"connexion-link\"]")(e => e.click())
      getElement(driver)("//*[@id=\"identifiant\"]")(e => e.sendKeys(login))
      getElement(driver)("//*[@id=\"auth-b_1\"]/div[3]/button")(e => e.click())

      val pwPath = """//div[@class ="gwt-DialogBox authenticationWidget"]//div[not(@id)]//table[not(@id)]//tbody[not(@id)]//tr[@class="dialogMiddle"]//td[@class="dialogMiddleCenter"]//div[@class="dialogMiddleCenterInner dialogContent"]//div[not(@id)]//div[@class="inner marges-nofusion"]//div[@class="authent-box"]//div[not(@id)]//div[not(@id)]//div[not(@id)]//div[@class="form-container auth-b-item auth-b-i-show"]//form[@id="formPassword"]//child::div[2]//input[@type="password"]"""
      getElement(driver)(pwPath)(element => element.sendKeys(pwd))
      driver.findElement(By.xpath("//*[@id=\"formPassword\"]/div[3]/button[2]")).click()
  }

}
