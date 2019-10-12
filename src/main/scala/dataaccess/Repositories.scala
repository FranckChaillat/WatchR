package dataaccess

import org.openqa.selenium.chrome.ChromeDriver
import scalaz.{Id, Kleisli, Reader}

trait Repositories {
  def billingRepo : BillingRepo
  def crawlingRepo : ChromeDriver //TODO: maybe wrap it in a trait
}

object Repositories {
  val repositories: Reader[Repositories, Repositories] =
    Reader[Repositories, Repositories](identity)

  val billingRepo: Kleisli[Id.Id, Repositories, BillingRepo] =
    repositories map(_.billingRepo)

  val crawlingRepo: Kleisli[Id.Id, Repositories, ChromeDriver] =
    repositories map(_.crawlingRepo)
}