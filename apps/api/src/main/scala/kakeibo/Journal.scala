package kakeibo

import zio.dynamodb.ProjectionExpression
import zio.schema.{DeriveSchema, Schema}

import java.time.{Instant, LocalDate}
import java.util.UUID

final case class Journal(aggId: Int, entries: List[Entry]) {
  def addEntry(eventId: UUID, entry: Entry): (Journal, EntryAdded) = {
    val newJournal = this.copy(entries = entries :+ entry)
    val event = EntryAdded(entry, eventId.toString, Instant.now())
    (newJournal, event)
  }
}
object Journal {
  def init(eventId: UUID, aggId: Int): (Journal, JournalInitialized) = {
    val initialJournal = Journal(aggId, Nil)
    val event = JournalInitialized(aggId, eventId.toString, Instant.now())
    (initialJournal, event)
  }

  implicit lazy val schema: Schema.CaseClass2[Int, List[Entry], Journal] =
    DeriveSchema.gen[Journal]
  val (aggId, entries) = ProjectionExpression.accessors[Journal]
}

final case class Entry(
    date: LocalDate,
    debit: AccountTitle,
    credit: AccountTitle,
    amount: BigDecimal
)
object Entry {
  implicit lazy val schema: Schema.CaseClass4[
    LocalDate,
    AccountTitle,
    AccountTitle,
    BigDecimal,
    Entry
  ] =
    DeriveSchema.gen[Entry]

  val (date, debit, credit, amount) = ProjectionExpression.accessors[Entry]
}

final case class AccountTitle(content: String, group: AccountTitleGroup)

enum AccountTitleGroup {
  case Asset
  case Liability
  case NetAsset
  case Expense
  case Revenue
}
object AccountTitleGroup {
  def fromString(s: String): Option[AccountTitleGroup] =
    values.find(_.toString.equalsIgnoreCase(s))
}