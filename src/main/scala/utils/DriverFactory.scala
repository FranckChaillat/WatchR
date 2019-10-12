package utils

import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}

object DriverFactory {

  def buildDriver(): ChromeDriver = {
    val chromeDriverPath = "./chromedriver_win32/chromedriver.exe"
    System.setProperty("webdriver.chrome.driver", chromeDriverPath)
    val options = new ChromeOptions()
    options.addArguments("--disable-gpu", "--window-size=800,600","--ignore-certificate-errors")
    val driver = new ChromeDriver(options)
    driver
  }

}
