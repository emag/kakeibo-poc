package kakeibo

import software.amazon.awssdk.auth.credentials.{
  AwsBasicCredentials,
  StaticCredentialsProvider
}
import software.amazon.awssdk.regions.Region
import zio.aws.core.config
import zio.aws.core.config.CommonAwsConfig
import zio.aws.{dynamodb, netty}
import zio.dynamodb.DynamoDBQuery.{get, put}
import zio.dynamodb.{DynamoDBExecutor, ProjectionExpression}
import zio.{ZIO, ZIOAppDefault, ZLayer}

import java.net.URI
import java.time.{Instant, LocalDate}

object Main extends ZIOAppDefault {

  val exampleEntry1 = Entry(
    id = 1,
    date = LocalDate.of(2025, 12, 1),
    debit = "A bank",
    credit = "cash",
    amount = 10_000,
    timestamp = Instant.now()
  )
  val exampleEntry2 = Entry(
    id = 1,
    date = LocalDate.of(2025, 12, 1),
    debit = "cash",
    credit = "bank",
    amount = 10_000,
    timestamp = Instant.now()
  )
  val exampleJournal1 = Journal(aggId = 1, entries = List(exampleEntry1))

  val putEntry = put("entry", exampleEntry1)
  val putJournal = put("journal", exampleJournal1)
  val tx = (putEntry zip putJournal).transaction

  private val program = for {
    _ <- tx.execute
    entry <- get("entry")(Entry.id.partitionKey === 1).execute
    journal <- get("journal")(Journal.aggId.partitionKey === 1).execute
    _ <- zio.Console.printLine(entry)
    _ <- zio.Console.printLine(journal)
  } yield ()

  override def run =
    program.provide(
      netty.NettyHttpClient.default,
      config.AwsConfig.configured(),
      ZLayer.succeed(
        CommonAwsConfig(
          region = Some(Region.US_EAST_1),
          credentialsProvider = StaticCredentialsProvider.create(
            AwsBasicCredentials.create("dummy", "dummy")
          ),
          endpointOverride = Some(URI.create("http://localhost:4566")),
          commonClientConfig = None
        )
      ),
      dynamodb.DynamoDb.live,
      DynamoDBExecutor.live
    )
}
