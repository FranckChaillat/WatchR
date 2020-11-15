package utils.configuration

import com.typesafe.config.Config

case class Configuration(joeUri : String, login: String, pwd: String, driverPath: String)

object Configuration {
  def getConfiguration()(implicit config: Config): Configuration = {
    Configuration(
      config.getString("service.dataaccess.joeuri"),
      config.getString("service.crawling.accountloging"),
      config.getString("service.crawling.accountpwd"),
      config.getString("service.crawling.driverPath")
    )
  }
}
