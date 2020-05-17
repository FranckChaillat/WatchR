package entities.dto

case class BulkAddTransactionRequest(transactions : Seq[AddTransactionRequest], overwrite: Boolean, limitDate: Option[String])
