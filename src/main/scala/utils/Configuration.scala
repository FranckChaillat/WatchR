package utils

import com.typesafe.config.Config

case class Configuration(connectionString : String)

object Configuration {

  def getConfiguration(implicit config: Config): Configuration = {
    val connStr = config.getString("service.dataaccess.mongodb.connectionString")
    Configuration(connStr)
  }

}
