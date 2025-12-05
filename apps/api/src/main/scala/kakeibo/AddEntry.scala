package kakeibo

import zio.dynamodb.DynamoDBQuery.{get, put}
import zio.dynamodb.{DynamoDBExecutor, ProjectionExpression}
import zio.{ZIO, ZIOAppArgs, ZIOAppDefault}

import java.time.LocalDate
import java.util.UUID

object AddEntry extends ZIOAppDefault with MainBase {

  private val program: ZIO[DynamoDBExecutor & ZIOAppArgs, Throwable, Unit] =
    for {
      args <- getArgs
      (journalAggId, entry) <- args.toList match {
        case journalAggId :: year :: month :: day :: debitAccountTitle :: debitAccountTitleGroup :: creditAccountTitle :: creditAccountTitleGroup :: amount :: Nil =>
          for {
            debit <- ZIO
              .fromOption(AccountTitleGroup.fromString(debitAccountTitleGroup))
              .map(AccountTitle(debitAccountTitle, _))
              .orElseFail(
                new IllegalArgumentException(
                  s"Invalid debit account group: $debitAccountTitleGroup"
                )
              )
            credit <- ZIO
              .fromOption(AccountTitleGroup.fromString(creditAccountTitleGroup))
              .map(AccountTitle(creditAccountTitle, _))
              .orElseFail(
                new IllegalArgumentException(
                  s"Invalid credit account group: $creditAccountTitleGroup"
                )
              )
          } yield (
            (
              journalAggId.toInt,
              Entry(
                date = LocalDate.of(year.toInt, month.toInt, day.toInt),
                debit = debit,
                credit = credit,
                amount = BigDecimal(amount)
              )
            )
          )
        case _ =>
          ZIO.fail(new IllegalArgumentException("Specify valid parameters"))
      }
      currentJournal <- get("journal")(
        Journal.aggId.partitionKey === journalAggId
      ).execute.flatMap(ZIO.fromEither)
      eventId = UUID.randomUUID()
      (newJournal, event) = currentJournal.addEntry(eventId, entry)
      _ <- (put("journal", newJournal) zip put("event", event)).execute
      // Check
      _ <- get("journal")(Journal.aggId.partitionKey === journalAggId).execute
        .flatMap(ZIO.fromEither)
        .flatMap(zio.Console.printLine(_))
      _ <- get("event")(EntryAdded.id.partitionKey === eventId.toString).execute
        .flatMap(ZIO.fromEither)
        .flatMap(zio.Console.printLine(_))
    } yield ()

  override def run = provide(program)
}
