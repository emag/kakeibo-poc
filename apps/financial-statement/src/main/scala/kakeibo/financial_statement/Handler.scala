package kakeibo.financial_statement

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue
import com.amazonaws.services.lambda.runtime.logging.LogLevel
import com.amazonaws.services.lambda.runtime.{
  Context,
  LambdaLogger,
  RequestHandler
}

import scala.jdk.CollectionConverters.*

class Handler extends RequestHandler[DynamodbEvent, String] {

  override def handleRequest(event: DynamodbEvent, context: Context): String = {
    val logger = context.getLogger

    event.getRecords.asScala.foreach { record =>
      val image = record.getDynamodb.getNewImage
      val eventType = Option(image.get("eventType")).map(_.getS)
      val payload = image.get("payload").getM.asScala.toMap
      eventType match {
        case Some("journal_initialized") =>
          processJournalInitialized(payload, logger)
        case Some("entry_added") =>
          processEntryAdded(payload, logger)
        case Some(unknown) =>
          logger.log(s"Unknown eventType received: $unknown", LogLevel.ERROR)
        case None =>
          logger.log(
            "eventType field is missing in the record.",
            LogLevel.ERROR
          )
      }
    }

    s"Processed ${event.getRecords.size()} records successfully"
  }

  private def processJournalInitialized(
      payload: Map[String, AttributeValue],
      logger: LambdaLogger
  ): Unit = {
    logger.log("Process JournalInitialized", LogLevel.DEBUG)
    val aggId = payload
      .getOrElse("aggId", throw new IllegalArgumentException("messing aggId"))
      .getN
      .toInt
    Db.insert(aggId)
    logger.log("Processed JournalInitialized successfully", LogLevel.DEBUG)
  }

  private def processEntryAdded(
      payload: Map[String, AttributeValue],
      logger: LambdaLogger
  ): Unit = {
    logger.log("Process EntryAdded", LogLevel.DEBUG)
    val aggId = payload
      .getOrElse("aggId", throw new IllegalArgumentException("messing aggId"))
      .getN
      .toInt
    val entry = payload
      .getOrElse("entry", throw new IllegalArgumentException("messing entry"))
      .getM
      .asScala
    val amount = BigDecimal(
      entry
        .getOrElse(
          "amount",
          throw new IllegalArgumentException("messing entry - amount")
        )
        .getN
    )
    val debitGroup = entry
      .getOrElse(
        "debit",
        throw new IllegalArgumentException("missing entry - debit")
      )
      .getM
      .asScala
      .getOrElse(
        "group",
        throw new IllegalArgumentException("missing debit - group")
      )
      .getS
    val creditGroup = entry
      .getOrElse(
        "credit",
        throw new IllegalArgumentException("missing entry - credit")
      )
      .getM
      .asScala
      .getOrElse(
        "group",
        throw new IllegalArgumentException("missing credit - group")
      )
      .getS

    val assetDelta =
      if (debitGroup == "Asset") amount
      else if (creditGroup == "Asset") -amount
      else BigDecimal(0)
    val liabilityDelta =
      if (debitGroup == "Liability") -amount
      else if (creditGroup == "Liability") amount
      else BigDecimal(0)
    val expenseDelta =
      if (debitGroup == "Expense") amount
      else if (creditGroup == "Expense") -amount
      else BigDecimal(0)
    val revenueDelta =
      if (debitGroup == "Revenue") -amount
      else if (creditGroup == "Revenue") amount
      else BigDecimal(0)

    Db.update(
      aggId = aggId,
      assetDelta = assetDelta,
      liabilityDelta = liabilityDelta,
      expenseDelta = expenseDelta,
      revenueDelta = revenueDelta
    )
    logger.log("Processed EntryAdded successfully", LogLevel.DEBUG)
  }
}
