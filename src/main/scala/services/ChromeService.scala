package services

import java.util.Date

import entities.BillingRow
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.openqa.selenium.{By, JavascriptExecutor, WebElement}
import scalaz.Reader
import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.util.{Failure, Success}

object ChromeService extends CrawlingService {

  def connect(login: String, pwd: String): Reader[ChromeDriver, Unit] = Reader {
    driver =>
      driver.get("https://www.cmso.com/banque/assurance/credit-mutuel/web/j_6/accueil")
      getElement(driver)("//*[@id=\"connexion-link\"]")(e => e.click())
      getElement(driver)("//*[@id=\"userLogin\"]")(e => e.sendKeys(login))
      getElement(driver)("//*[@id=\"auth-c_1\"]/button")(e => e.click())
      getElement(driver)("//*[@id=\"userPassword\"]")(element => element.sendKeys(pwd))
      driver.findElement(By.xpath("//*[@id=\"formLogin\"]/div[3]/div[2]/input")).click()
  }

  def getPaymentHistory(limitDate: Date): Reader[ChromeDriver, Seq[BillingRow]] = Reader {
    driver =>
      closeModals(driver)
      Thread.sleep(100)
      val accountPath = "//*[@id=\"layout\"]/bux2-card[1]/bux2-card-body/bux2-widget-account/bux2-link/a"
      getElement(driver)(accountPath)(_.click())
      Thread.sleep(100)
      getBillingRows(List())(driver, limitDate)
  }

  private def parseBilling(paymentTab: WebElement, limitDate: Option[Date]): Seq[BillingRow] = {
    paymentTab.findElements(By.tagName("ul")).asScala.toList
      .flatMap(x => x.findElements(By.tagName("li")).asScala.toList)
      .foldLeft(Seq.empty[BillingRow]) { (acc, e) =>
        BillingRow.parseRow(e.getText.split("\\n"), accountId = 1, limitDate) match {
          case Success(res) => res.map(x => acc.+:(x)).getOrElse(acc)
          case Failure(_) =>
            //TODO: manage errors
            println("Error while trying to parse billing row")
            acc
        }
      }
  }

  private def closeModals(driver: ChromeDriver): Unit = {
    @tailrec
    def close(index: Int): Unit = {
      val wait = new WebDriverWait(driver, 10)
      wait.until(ExpectedConditions.presenceOfElementLocated(By.className("c-modal__close")))
      val modals = driver.findElementsByClassName("c-modal__close").asScala
      if(index <= modals.length-1) {
        Thread.sleep(100)
        modals(index).click()
        close(index + 1)
      }
    }
    Thread.sleep(3000)
    close(0)
  }

  private def getBillingRows(acc: List[BillingRow])(driver: ChromeDriver, limitDate: Date): List[BillingRow] = {
    val tablePath = "//*[@id=\"tab01\"]/ul"//"
    val nextButton = "//*[@id=\"titrePageFonctionnelle\"]/div[3]/div/div[2]/div/div/div[1]/div/div[5]/table/tbody/tr/td[3]/div/img[1]"
    val parsedRows =  parseBilling(getElement(driver)(tablePath)(identity), Some(limitDate))
    val newAcc = acc ++ groupBillingRows(parsedRows)
    //    val needMoreRows = newAcc.sortBy(_.operationDate)
    //      .headOption.exists(_.operationDate.compareTo(limitDate) > 0)
    if(false) {
      getElement(driver)(nextButton) { x => x.click() }
      getBillingRows(newAcc)(driver, limitDate)
    } else {
      newAcc
    }
  }

  private def groupBillingRows(rows: Seq[BillingRow]) = {
    rows.groupBy(x => x.identifier)
      .map(g => g._2 match {
        case h +: t if t.nonEmpty => h.copy(occurence = g._2.size)
        case h +: _ => h
      }).toSeq
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
}
