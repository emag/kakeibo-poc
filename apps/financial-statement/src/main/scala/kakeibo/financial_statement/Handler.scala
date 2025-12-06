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

  private val repository = new SlickRepository()

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
  ) = {
    logger.log("Process JournalInitialized", LogLevel.DEBUG)

    try {
      val journalId = payload.get("journalId").map(_.getS).getOrElse("0").toInt

      val bs = BS(
        aggId = journalId,
        asset = BigDecimal(0),
        liability = BigDecimal(0)
      )

      val pl = PL(
        aggId = journalId,
        expense = BigDecimal(0),
        revenue = BigDecimal(0)
      )

      repository.insertBS(bs)
      repository.insertPL(pl)

      logger.log(
        s"Successfully initialized BS and PL for journalId: $journalId",
        LogLevel.INFO
      )

    } catch {
      case ex: Exception =>
        logger.log(
          s"Error processing JournalInitialized: ${ex.getMessage}",
          LogLevel.ERROR
        )
        throw ex
    }
  }

  private def processEntryAdded(
      payload: Map[String, AttributeValue],
      logger: LambdaLogger
  ) = {
    logger.log("Process EntryAdded", LogLevel.DEBUG)
    payload.map { case (k, v) => println(s"key: $k, value: $v") }
  }
}
