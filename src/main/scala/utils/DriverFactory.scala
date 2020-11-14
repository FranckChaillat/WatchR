package utils

import java.util.concurrent.TimeUnit

import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}

class DriverFactory {

  private var driver : Option[ChromeDriver] = None

  def open() = {
    driver match {
      case Some(v) =>
        v.close()
        driver = Some(buildDriver());driver.get
      case None =>
        driver = Some(buildDriver());driver.get
    }
  }

  def close() = {
    this.driver.foreach(_.close())
    this.driver = None
  }

  private def buildDriver(): ChromeDriver = {
    val chromeDriverPath = "./chromedriver"
    System.setProperty("webdriver.chrome.driver", chromeDriverPath)
    val options = new ChromeOptions()
    options.addArguments("--disable-gpu", "--window-size=800,600","--ignore-certificate-errors")
    val driver = new ChromeDriver(options)
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS)
    driver
  }

}
