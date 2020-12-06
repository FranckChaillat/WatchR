import java.util.Date

import entities.BillingRow
import org.scalatest.FlatSpec

class BillingParserTest extends FlatSpec {

  it should "merge actual data and collected" in {
    val date1 = new Date()
    val date2 = new Date()
    val date3 = new Date()

    val actual = Seq (
      BillingRow(1, date1, date1, "payment 1", 12.0f).copy(category = Some("exampleCat")),
      BillingRow(1, date2, date2, "payment 2", 12.0f)
    )

    val collected = Seq (
      BillingRow(1, date1, date1, "payment 1", 12.0f),
      BillingRow(1, date2, date2, "payment 2", 12.0f),
      BillingRow(1, date3, date3, "payment 3", 12.0f),
    )

    val merged = BillingRow.mergeBilling(collected, actual)
    val expected = Seq(
      BillingRow(1, date1, date1, "payment 1", 12.0f).copy(category = Some("exampleCat")),
      BillingRow(1, date2, date2, "payment 2", 12.0f),
      BillingRow(1, date3, date3, "payment 3", 12.0f)
    )
    assert(merged == expected)
  }

  it should "merge actual data and collected and keep actual that has not been collected" in {
    val date1 = new Date()
    val date2 = new Date()
    val date3 = new Date()
    val date4 = new Date()

    val actual = Seq (
      BillingRow(1, date1, date1, "payment 1", 12.0f).copy(category = Some("exampleCat")),
      BillingRow(1, date2, date2, "payment 2", 12.0f),
      BillingRow(1, date4, date4, "payment custom", 42f)
    )

    val collected = Seq (
      BillingRow(1, date1, date1, "payment 1", 12.0f),
      BillingRow(2, date2, date2, "payment 2", 12.0f),
      BillingRow(1, date3, date3, "payment 3", 25.0f),
    )

    val merged = BillingRow.mergeBilling(collected, actual)

    val expected = Seq(
      BillingRow(1, date1, date1, "payment 1", 12.0f).copy(category = Some("exampleCat")),
      BillingRow(1, date2, date2, "payment 2", 12.0f),
      BillingRow(1, date3, date3, "payment 3", 25.0f),
      BillingRow(1, date4, date4, "payment custom", 42f)
    )
    assert(merged == expected)
  }

}
