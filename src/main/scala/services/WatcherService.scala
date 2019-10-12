package services

import java.text.SimpleDateFormat
import java.util.Date

import dataaccess.{MongoBillingRepo, Repositories}
import entities.BillingRow
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.openqa.selenium.{By, JavascriptExecutor, WebElement}
import scalaz.Reader

import scala.annotation.tailrec

object WatcherService {

  def getBillingInfo(login: String, pwd: String) :  Reader[Repositories, Unit] = Reader {
    driver =>
      WatcherService.connect(login, pwd)
      val date = new SimpleDateFormat("dd/MM/yyyy").parse("07/07/2019")
      val billingRows = WatcherService.getPaymentHistory(driver.crawlingRepo, date)

  }

//      WatcherService.connect(login, pwd)
//      val date = new SimpleDateFormat("dd/MM/yyyy").parse("07/07/2019")
//      val billingRows = WatcherService.getPaymentHistory(driver, date)
//      val insertRes = billingRows.map(r => MongoBillingRepo.insertBilling(r))
//        .sequenceU
//        .run(mongoDriver)


  def connect(login: String, pwd: String)(implicit driver : ChromeDriver): Unit = {
    driver.get("https://www.cmso.com/banque/assurance/credit-mutuel/web/j_6/accueil")
    val connectionLink = driver.findElement(By.id("connexion-link"))
    connectionLink.click()


    driver.findElement(By.xpath("//*[@id=\"identifiant\"]"))
      .sendKeys(login)

    driver.findElement(By.xpath("//*[@id=\"auth-b_1\"]/div[3]/button")).click()

    val path = """//div[@class ="gwt-DialogBox authenticationWidget"]//div[not(@id)]//table[not(@id)]//tbody[not(@id)]//tr[@class="dialogMiddle"]//td[@class="dialogMiddleCenter"]//div[@class="dialogMiddleCenterInner dialogContent"]//div[not(@id)]//div[@class="inner marges-nofusion"]//div[@class="authent-box"]//div[not(@id)]//div[not(@id)]//div[not(@id)]//div[@class="form-container auth-b-item auth-b-i-show"]//form[@id="formPassword"]//child::div[2]//input[@type="password"]"""
    getElement(driver)(path)(element => element.sendKeys(pwd))
    driver.findElement(By.xpath("//*[@id=\"formPassword\"]/div[3]/button[2]")).click()
  }

  def getPaymentHistory(driver: ChromeDriver, limitDate: Date): List[BillingRow] = {
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

    val rows = parseBillingRows(payHistoryTable)
    val needMoreRows = rows.sortBy(_.operationDate)
      .headOption.exists(_.operationDate.compareTo(limitDate) > 0)
    if(needMoreRows)
      getElement(driver)(nextButton){x => x.click()}
      getBillingRows(acc ++ rows)(driver, limitDate)
  }

  def parseBillingRows(billingTabContent : String) : List[BillingRow] = {
    val rows = billingTabContent.split("\\n")
    val groups = rows.grouped(4).toList
    val billingRows = groups.collect {
      case BillingRow(e: BillingRow) => e
    }
    billingRows
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
}
