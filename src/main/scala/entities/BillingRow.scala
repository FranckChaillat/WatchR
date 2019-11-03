package entities

import java.util.Date

import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import utils.extractors.{CustomDate, CustomFloat}

case class BillingRow(operationDate: Date, valueDate: Date, label: String, amount: Float)


object BillingRow {

  def getCodec: CodecRegistry =
    fromRegistries(fromProviders(classOf[BillingRow]), DEFAULT_CODEC_REGISTRY)

  def unapply(arg: Array[String]): Option[BillingRow] = {
    arg match {
      case Array(CustomDate(operationDate), CustomDate(valueDate), label, CustomFloat(value)) =>
        Some(BillingRow(operationDate, valueDate, label, value))
      case _ => None
    }
  }
}