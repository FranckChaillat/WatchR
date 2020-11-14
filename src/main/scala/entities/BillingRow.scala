package entities

import java.math.BigInteger
import java.sql.Timestamp
import java.util.Date
import exceptions.BillingParseException
import utils.extractors.{CustomDate, CustomFloat}

import scala.util.{Failure, Success, Try}


final case class BillingRow(identifier: String, accountId: Int, operationDate: Date, valueDate: Date, label: String, amount: Float, occurence: Int = 1, category: Option[String] = None)

object BillingRow {

  def apply(accountId: Int, operationDate: Date, valueDate: Date, label: String, amount: Float): BillingRow = {
    val rowId = hashRow(operationDate, valueDate, label, amount)
    new BillingRow(rowId, accountId, operationDate, valueDate, label, amount)
  }

  def parseRow(arg: Array[String], accountId: Int, limitDate: Option[Date] = None): Try[Option[BillingRow]] = {
    if (arg.length > 13) {
      Array(3, 6, 9, 13).map(arg.apply) match {
        case Array(CustomDate(opDate), CustomDate(valDate), label, CustomFloat(value))  =>
          Success {
            if (limitDate.forall(lim => lim.compareTo(opDate) <= 0)) {
              Some(BillingRow(accountId, opDate, valDate, label, value))
            } else
              None
          }
        case _ => Failure(BillingParseException("Unable to parse log."))
      }
    }
    else
      Failure(BillingParseException("Format was not the one expected"))
  }

  private def hashRow(operationDate: Date, valueDate: Date, label: String, amount: Float) = {
    val str = Seq(
      new Timestamp(operationDate.getTime).toString,
      new Timestamp(valueDate.getTime).toString,
      label,
      amount).mkString(";")
    val md = java.security.MessageDigest.getInstance("SHA-1")
    md.reset()
    md.update(str.getBytes("UTF-8"))
    String.format("%040x", new BigInteger(1, md.digest()))
  }
}