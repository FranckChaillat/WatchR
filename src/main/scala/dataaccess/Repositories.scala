package dataaccess

import org.openqa.selenium.chrome.ChromeDriver
import scalaz.{Id, Kleisli, Reader}

trait Repositories {
  def billingRepo : MongoBillingRepo
  def crawlingRepo : ChromeDriver //TODO: maybe wrap it in a trait
}
