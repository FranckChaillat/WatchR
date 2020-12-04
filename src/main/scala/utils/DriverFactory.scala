package utils

import java.util.concurrent.TimeUnit

import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}

class DriverFactory(driverPath: String) {

  private var driver : Option[ChromeDriver] = None

  def open() = {
    driver match {
      case Some(v) =>
        v.close()
        v.quit()
        driver = Some(buildDriver());driver.get
      case None =>
        driver = Some(buildDriver());driver.get
    }
  }

  def close() = {
    this.driver.foreach(d => { d.close(); d.quit() })
    this.driver = None
    Runtime.getRuntime.exec("pkill --signal TERM -f /snap/chromium/1417/usr/lib/chromium-browser/chrome")
    ()
  }

  private def buildDriver(): ChromeDriver = {
    System.setProperty("webdriver.chrome.driver", driverPath)
    val options = new ChromeOptions()
    options.addArguments("--disable-gpu", "--headless", "--ignore-certificate-errors", "--blink-settings=imagesEnabled=false", "--start-maximized")
    val driver = new ChromeDriver(options)
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS)
    driver
  }

}
