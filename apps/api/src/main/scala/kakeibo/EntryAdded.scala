package kakeibo

import zio.dynamodb.ProjectionExpression
import zio.schema.{DeriveSchema, Schema}

import java.time.Instant

final case class EntryAdded(
    entry: Entry,
    override val id: String,
    override val timestamp: Instant,
    override val eventType: String = "entry_added"
) extends Event
object EntryAdded {
  implicit lazy val schema: Schema.CaseClass4[
    Entry,
    String,
    Instant,
    String,
    EntryAdded
  ] =
    DeriveSchema.gen[EntryAdded]

  val (entry, id, timestamp, eventType) =
    ProjectionExpression.accessors[EntryAdded]
}
