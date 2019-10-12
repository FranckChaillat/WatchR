package entities

import java.util.Date

case class DebitRow(operationDate: Date, valueDate: Date, label: String, amount: Float) extends BillingRow
