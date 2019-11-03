package exceptions

case class BillingInsertionError(msg: String) extends Exception(msg)
