package dataaccess

import java.text.SimpleDateFormat

import entities.{BillingRow, CreditRow, DebitRow}
import org.mongodb.scala.bson.{BsonDouble, BsonString}
import org.mongodb.scala.model.Projections.include
import org.mongodb.scala.{Document, MongoDatabase}
import scalaz.Kleisli

import scala.concurrent.Future

object MongoBillingRepo  {


  private implicit val dateFmt: SimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy")

  def insertBilling(r : BillingRow) : Kleisli[Future, MongoDatabase, Unit] = Kleisli {
    mongoDatabase =>
      mongoDatabase.getCollection[BillingRow]("Transactions")
        .insertOne(r) toFuture() map(_ => ())
  }

  def getBillingRows(fields : Seq[String]) : Kleisli[Future, MongoDatabase, Seq[BillingRow]] = Kleisli {
    mongoDatabase =>
      (mongoDatabase.getCollection[Document]("Transactions")
        .find[Document]
        .projection(include(fields: _*)) toFuture()) map(e => e.flatMap(parseBillingRow))
  }

  private def parseBillingRow(doc: Document) : Option[BillingRow] = {
    for {
      opDate <- doc.get[BsonString]("operationDate") map(d => dateFmt.parse(d.getValue))
      valDate <- doc.get[BsonString]("valueDate") map(d => dateFmt.parse(d.getValue))
      label <- doc.get[BsonString]("label") map(_ getValue)
      amount <- doc.get[BsonDouble]("amount") map(_ asDouble() getValue)
    } yield if(amount > 0.0)
      CreditRow(opDate, valDate, label, amount.toFloat)
    else
      DebitRow(opDate, valDate, label, amount.toFloat)
  }
}
