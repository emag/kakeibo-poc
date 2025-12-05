package kakeibo

import zio.dynamodb.DynamoDBQuery.{get, put}
import zio.dynamodb.{DynamoDBExecutor, ProjectionExpression}
import zio.{ZIO, ZIOAppArgs, ZIOAppDefault}

import java.util.UUID

object InitJournal extends ZIOAppDefault with MainBase {

  private val program: ZIO[DynamoDBExecutor & ZIOAppArgs, Throwable, Unit] =
    for {
      args <- getArgs
      aggId <- args.toList match {
        case aggId :: Nil => ZIO.succeed(aggId.toInt)
        case _ =>
          ZIO.fail(new IllegalArgumentException("Required journal agg Id"))
      }
      eventId = UUID.randomUUID();
      (initialJournal, event) = Journal.init(eventId, aggId)
      _ <- (put("journal", initialJournal) zip put("event", event)).execute
      // Check
      _ <- get("journal")(Journal.aggId.partitionKey === aggId).execute
        .flatMap(ZIO.fromEither)
        .flatMap(zio.Console.printLine(_))
      _ <- get("event")(
        JournalInitialized.id.partitionKey === eventId.toString
      ).execute.flatMap(ZIO.fromEither).flatMap(zio.Console.printLine(_))
    } yield ()

  override def run = provide(program)
}
