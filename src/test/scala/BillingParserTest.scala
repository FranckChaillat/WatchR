import java.text.SimpleDateFormat

import entities.BillingRow
import org.scalatest.FlatSpec
import services.WatcherService

class BillingParserTest extends FlatSpec {

  "string input" should "be converted into valids billing rows" in {

   val input = """|24/07/2019
      |24/07/2019
      |CARTE 23/07 O TACOS MERIGNAC
      |- 6,00 €
      |23/07/2019
      |23/07/2019
      |CARTE 22/07 KEOLIS TBM DTTT BORDEAUX
      |- 7,60 €
      |19/07/2019
      |19/07/2019
      |CARTE 19/07 OSTERIA PIZZERIA BORDEAUX
      |- 3,50 €""".stripMargin


    val df = new SimpleDateFormat("dd/MM/yyyy")
    val result = WatcherService.parseBillingRows (input)
    assert(result.length == 3)
    assert(result.forall(_.isInstanceOf[BillingRow]))
    assert(df.format(result.head.valueDate) == "24/07/2019")
    assert(df.format(result.head.operationDate) == "24/07/2019")
    assert(result.head.amount == -6.0)
    assert(result.head.label == "CARTE 23/07 O TACOS MERIGNAC")

  }
}
