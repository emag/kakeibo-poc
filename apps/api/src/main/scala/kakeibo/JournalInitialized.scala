package kakeibo

import kakeibo.JournalInitialized.Payload
import zio.dynamodb.ProjectionExpression
import zio.schema.{DeriveSchema, Schema}

import java.time.Instant

final case class JournalInitialized(
    override val id: String,
    override val payload: Payload,
    override val timestamp: Instant,
    override val eventType: String = "journal_initialized"
) extends Event[Payload]
object JournalInitialized {
  final case class Payload(aggId: Int)
  object Payload {
    implicit lazy val schema: Schema.CaseClass1[Int, Payload] = DeriveSchema.gen[Payload]
    val aggId = ProjectionExpression.accessors[Payload]
  }
  implicit lazy val schema: Schema.CaseClass4[
    String,
    Payload,
    Instant,
    String,
    JournalInitialized
  ] =
    DeriveSchema.gen[JournalInitialized]

  val (id, payload, timestamp, eventType) =
    ProjectionExpression.accessors[JournalInitialized]
}
