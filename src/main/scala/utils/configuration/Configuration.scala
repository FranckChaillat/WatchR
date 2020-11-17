package utils.configuration


import java.io.File

import com.typesafe.config.Config

import scala.io.Source

final case class Configuration(joeUri : String, login: String, pwd: String, driverPath: String)

object Configuration {
  def getConfiguration()(implicit config: Config): Configuration = {
    val filePath = config.getString("service.crawling.credfilepath")
    val src = Source.fromFile(filePath)
    val envVars = src.getLines().toList
        .map(l => {
          val splited = l.split("=")
          (splited.head, splited.tail.head)
        }).toMap

    val conf = Configuration(
      config.getString("service.dataaccess.joeuri"),
      envVars("accountloging"),
      envVars("accountpwd"),
      config.getString("service.crawling.driverPath")
    )

    src.close()
    conf
  }
}
