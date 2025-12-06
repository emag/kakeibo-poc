package kakeibo

import kakeibo.EntryAdded.Payload
import zio.dynamodb.ProjectionExpression
import zio.schema.{DeriveSchema, Schema}

import java.time.Instant

final case class EntryAdded(
    override val id: String,
    override val payload: Payload,
    override val timestamp: Instant,
    override val eventType: String = "entry_added"
) extends Event[Payload]
object EntryAdded {
  final case class Payload(entry: Entry)
  object Payload {
    implicit lazy val schema: Schema.CaseClass1[Entry, Payload] =
      DeriveSchema.gen[Payload]
    val entry = ProjectionExpression.accessors[Payload]
  }

  implicit lazy val schema: Schema.CaseClass4[
    String,
    Payload,
    Instant,
    String,
    EntryAdded
  ] =
    DeriveSchema.gen[EntryAdded]

  val (id, payload, timestamp, eventType) =
    ProjectionExpression.accessors[EntryAdded]
}
