package kakeibo

import zio.dynamodb.ProjectionExpression
import zio.schema.{DeriveSchema, Schema}

import java.time.Instant

final case class JournalInitialized(
    argId: Int,
    override val id: String,
    override val timestamp: Instant,
    override val eventType: String = "journal_initialized"
) extends Event
object JournalInitialized {
  implicit lazy val schema: Schema.CaseClass4[
    Int,
    String,
    Instant,
    String,
    JournalInitialized
  ] =
    DeriveSchema.gen[JournalInitialized]

  val (argId, id, timestamp, eventType) =
    ProjectionExpression.accessors[JournalInitialized]
}
