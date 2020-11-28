package utils.configuration

import com.typesafe.config.Config
import scala.io.Source

final case class Configuration(joeUri : String, login: String, pwd: String, driverPath: String, dayoffset: Option[Int], triggerIntervalSeconds : Option[Int])

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
        config.getString("service.crawling.driverPath"),
        if(!config.hasPath("service.crawling.dayOffset")) None else Some(config.getInt("service.crawling.dayOffset")),
        if(!config.hasPath("service.crawling.interval")) None else Some(config.getInt("service.crawling.interval"))
    )

    src.close()
    conf
  }
}
