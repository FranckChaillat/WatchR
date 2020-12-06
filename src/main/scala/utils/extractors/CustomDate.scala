package utils.extractors

import java.util.Date
import java.text.SimpleDateFormat

import scala.util.Try

object CustomDate {

  def unapply(arg: String): Option[Date] = {
    tryParse("dd/MM/yy")(arg)
      .orElse(tryParse("dd/MM/yyyy")(arg))
  }

  def tryParse(format: String)(dateString: String): Option[Date] = {
    val result = Try {
      val sdf = new SimpleDateFormat(format)
      sdf.setLenient(false)
      sdf.parse(dateString)
    }
    result.toOption
  }
}
