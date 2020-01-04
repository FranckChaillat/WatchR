package dataaccess

import java.text.SimpleDateFormat
import java.util.Date

import entities.BillingRow
import exceptions.BillingInsertionError
import org.mongodb.scala.bson.{BsonDouble, BsonString}
import org.mongodb.scala.model.Projections.include
import org.mongodb.scala.{Document, MongoClient}
import scalaz.Kleisli

import scala.concurrent.{ExecutionContext, Future}


trait MongoBillingRepo extends BillingRepo {

  // Abstract members
  val dbDriver: MongoClient

  private implicit val dateFmt: SimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy")

  def insertBilling(rows : Seq[BillingRow])(implicit ec: ExecutionContext) : Kleisli[Future, MongoClient, Unit] = Kleisli {
    dbClient =>
      println("inserting rows...")
      dbClient
        .getDatabase("bank").withCodecRegistry(BillingRow.getCodec)
        .getCollection[BillingRow]("Transactions")
        .insertMany(rows).toFuture()
        .map(_ => ())
        .recoverWith {
          case e: Throwable =>
            Future.failed(BillingInsertionError(s"An error occurred while inserting billing informations, ${e.getMessage}"))
        }
  }

  def getBillingRows(fields : Seq[String])(implicit ec: ExecutionContext) : Kleisli[Future, MongoClient, Seq[BillingRow]] = Kleisli {
    dbClient =>
      dbClient.getDatabase("Bank")
        .getCollection("Transactions")
        .find[Document].projection(include(fields: _*)) toFuture() map(e => e.flatMap(parseBillingRow))
  }

  private def parseBillingRow(doc: Document) : Option[BillingRow] = {
    for {
      opDate <- doc.get[BsonString]("operationDate") map(d => dateFmt.parse(d.getValue))
      valDate <- doc.get[BsonString]("valueDate") map(d => dateFmt.parse(d.getValue))
      label <- doc.get[BsonString]("label") map(_ getValue)
      amount <- doc.get[BsonDouble]("amount") map(_ asDouble() getValue)
    } yield
      BillingRow(opDate, valDate, label, amount.toFloat)
  }
}
