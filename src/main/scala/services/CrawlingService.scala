package services

import java.util.Date

import entities.BillingRow
import org.openqa.selenium.chrome.ChromeDriver
import scalaz.Reader

trait CrawlingService {
  def connect(login: String, pwd: String): Reader[ChromeDriver, Unit]
  def getPaymentHistory(limitDate: Date): Reader[ChromeDriver, Seq[BillingRow]]
}
