package entities

import java.util.Date

case class CreditRow(operationDate: Date, valueDate: Date, label: String, amount: Float) extends BillingRow
