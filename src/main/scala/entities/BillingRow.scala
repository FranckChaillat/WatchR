package entities

import java.util.Date

import utils.extractors.{CustomDate, CustomFloat}

trait BillingRow {
  def operationDate: Date

  def valueDate: Date

  def label: String

  def amount: Float
}


object BillingRow {

  def unapply(arg: Array[String]): Option[BillingRow] = {
    arg match {
      case Array(CustomDate(operationDate), CustomDate(valueDate), label, CustomFloat(value)) =>
        Some {
          if(value < 0)
            DebitRow(operationDate, valueDate, label, value)
          else
            CreditRow(operationDate, valueDate, label, value)
        }
      case _ => None
    }
  }
}