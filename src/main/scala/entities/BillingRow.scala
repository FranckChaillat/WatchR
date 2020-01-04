package entities

import java.util.Date

import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.bson.types.ObjectId
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import utils.extractors.{CustomDate, CustomFloat}

case class BillingRow(_id: ObjectId, operationDate: Date, valueDate: Date, label: String, amount: Float)


object BillingRow {

  def apply(operationDate: Date, valueDate: Date, label: String, amount: Float): BillingRow =
    new BillingRow(new ObjectId(), operationDate, valueDate, label, amount)
  
  def getCodec: CodecRegistry =
    fromRegistries(fromProviders(classOf[BillingRow]), DEFAULT_CODEC_REGISTRY)

  def unapply(arg: Array[String]): Option[BillingRow] = {
    if(arg.length == 11) {
      Array(2, 3, 8, 10).map(arg.apply) match {
        case Array(CustomDate(opDate), CustomDate(valDate), label, CustomFloat(value)) =>
          Some(BillingRow(opDate, valDate, label, value))
        case _ => None
      }
    }
    else
      None
  }

//  def unapply(arg: Array[String]): Option[BillingRow] = {
//    arg match {
//      case Array(CustomDate(operationDate), CustomDate(valueDate), label, CustomFloat(value)) =>
//        Some(BillingRow(operationDate, valueDate, label, value))
//      case _ => None
//    }
//  }
}