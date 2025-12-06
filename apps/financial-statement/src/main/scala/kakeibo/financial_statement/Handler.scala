package kakeibo.financial_statement

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue
import com.amazonaws.services.lambda.runtime.logging.LogLevel
import com.amazonaws.services.lambda.runtime.{
  Context,
  LambdaLogger,
  RequestHandler
}
import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.jdk.CollectionConverters.*

class Handler extends RequestHandler[DynamodbEvent, String] {

  private val objectMapper = new ObjectMapper()
    .registerModule(DefaultScalaModule)
    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)

  override def handleRequest(event: DynamodbEvent, context: Context): String = {
    val logger = context.getLogger

    val jsonOutput = objectMapper.writeValueAsString(event)
    logger.log("Received DynamoDB Stream Event as JSON:", LogLevel.DEBUG)
    logger.log(jsonOutput, LogLevel.DEBUG)

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
    payload.map { case (k, v) => println(s"key: $k, value: $v") }
  }

  private def processEntryAdded(
      payload: Map[String, AttributeValue],
      logger: LambdaLogger
  ) = {
    logger.log("Process EntryAdded", LogLevel.DEBUG)
    payload.map { case (k, v) => println(s"key: $k, value: $v") }
  }
}
