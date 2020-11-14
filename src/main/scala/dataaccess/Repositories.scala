package dataaccess

import services.CrawlingService
import utils.DriverFactory

trait Repositories {
  def billingRepo : BillingRepo
  def httpConnector: ApiRepository
  def crawlingService : CrawlingService
  def crawlingRepo : DriverFactory
}
