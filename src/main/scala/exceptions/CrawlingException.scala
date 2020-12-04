package exceptions

final case class CrawlingException(msg: String, cause: Throwable) extends Exception(msg, cause)
