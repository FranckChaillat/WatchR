package exceptions

case class BillingParseException(msg: String) extends Exception(msg)
