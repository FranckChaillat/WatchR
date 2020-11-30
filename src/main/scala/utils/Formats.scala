package utils

import java.text.SimpleDateFormat
import java.util.Date

import org.json4s.CustomSerializer
import org.json4s.JsonAST.JString

object Formats {
  object StringToDate extends CustomSerializer[Date](format => (
    { case JString(x) => new SimpleDateFormat("yyyy-MM-dd").parse(x) },
    { case x: Date =>   JString(new SimpleDateFormat("yyyy-MM-dd").format(x)) }))
}
