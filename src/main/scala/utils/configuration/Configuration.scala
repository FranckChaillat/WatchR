package utils.configuration

import com.typesafe.config.Config

case class Configuration(connectionString : String, login: String, pwd: String)

object Configuration {

  def getConfiguration(configObject: ConfigObject)(implicit config: Config): Configuration = {
    val connStr = config.getString("service.dataaccess.mongodb.connectionString")
    Configuration(connStr.replace("#mongopwd#", configObject.mongopwd)
        .replace("#mongousername#", configObject.mongousername),
      configObject.banklogin,
      configObject.bankpwd)
  }

}
