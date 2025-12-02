package kakeibo

import zio.dynamodb.ProjectionExpression
import zio.schema.{DeriveSchema, Schema}

import java.time.{Instant, LocalDate}

final case class Journal(aggId: Int, entries: List[Entry])
object Journal {
  implicit lazy val schema: Schema.CaseClass2[Int, List[Entry], Journal] = DeriveSchema.gen[Journal]

  val (aggId, entries) = ProjectionExpression.accessors[Journal]
}

final case class Entry(
    id: Int,
    date: LocalDate,
    debit: String,
    credit: String,
    amount: BigDecimal,
    timestamp: Instant
)
object Entry {
  implicit lazy val schema: Schema.CaseClass6[
    Int,
    LocalDate,
    String,
    String,
    BigDecimal,
    Instant,
    Entry
  ] =
    DeriveSchema.gen[Entry]

  val (id, date, debit, credit, amount, timestamp) =
    ProjectionExpression.accessors[Entry]
}
