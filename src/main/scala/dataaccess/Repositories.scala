package dataaccess

import org.openqa.selenium.chrome.ChromeDriver
import services.CrawlingService

trait Repositories {
  def billingRepo : BillingRepo
  def httpConnector: ApiRepository
  def crawlingService : CrawlingService
  def crawlingRepo : ChromeDriver //TODO: maybe wrap it in a trait
}
