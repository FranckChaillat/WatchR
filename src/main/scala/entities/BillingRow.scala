package entities

import java.math.BigInteger
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date

import exceptions.BillingParseException
import utils.extractors.{CustomDate, CustomFloat}

import scala.util.{Failure, Success, Try}


final case class BillingRow(identifier: String, accountId: Int, operationDate: Date, valueDate: Date, label: String, amount: Float, occurence: Int = 1, category: Option[String] = None)

object BillingRow {

  def apply(accountId: Int, operationDate: Date, valueDate: Date, label: String, amount: Float): BillingRow = {
    val fmt = new SimpleDateFormat("yyyy-MM-dd")
    val rowId = hashRow(fmt.format(operationDate), fmt.format(valueDate), label, amount)
    new BillingRow(rowId, accountId, operationDate, valueDate, label, amount)
  }

  def parseRow(arg: Array[String], accountId: Int, limitDate: Option[Date] = None): Try[Option[BillingRow]] = {
    val items = if(arg.length == 12) {
      Array(3, 6, 9, 11).map(arg.apply)
    } else if (arg.length == 13 && arg.exists(_.toLowerCase().trim == "libellé :")){
      Array(3, 6, 9, 12).map(arg.apply)
    } else if(arg.length == 13) {
      Array(3, 6, 8, 12).map(arg.apply)
    } else if(arg.length == 14) {
      Array(3, 6 ,9 ,13).map(arg.apply)
    } else {
      Array(3, 6, 9, 14).map(arg.apply)
    }

    items match {
      case Array(CustomDate(opDate), CustomDate(valDate), label, CustomFloat(value))  =>
        Success {
          if (limitDate.forall(lim => lim.compareTo(opDate) <= 0))
            Some(BillingRow(accountId, opDate, valDate, label, value))
           else
            None
        }
      case _ => Failure(BillingParseException("Unable to parse log."))
    }
  }

  private def hashRow(operationDate: String, valueDate: String, label: String, amount: Float) = {
    val str = Seq(operationDate, valueDate, label, amount).mkString(";")
    val md = java.security.MessageDigest.getInstance("SHA-1")
    md.reset()
    md.update(str.getBytes("UTF-8"))
    String.format("%040x", new BigInteger(1, md.digest()))
  }

  def mergeBilling(collected: Seq[BillingRow], actual: Seq[BillingRow]): Seq[BillingRow] = {
    val mapActual = actual.map(a => a.identifier -> a).toMap
    val mapCollected = collected.map(c => c.identifier -> c).toMap

    val leftJoined = collected.map { x =>
      mapActual.getOrElse(x.identifier, x)
    }

    val missing = mapActual.keySet.diff(mapCollected.keySet).map(mapActual.apply).toSeq

    leftJoined ++ missing

  }
}