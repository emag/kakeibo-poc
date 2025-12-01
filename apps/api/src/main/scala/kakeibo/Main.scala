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
import zio.schema.{DeriveSchema, Schema}
import zio.{ZIO, ZIOAppDefault, ZLayer}

import java.net.URI

object Main extends ZIOAppDefault {

  final case class Person(id: Int, firstName: String)
  object Person {
    implicit lazy val schema: Schema.CaseClass2[Int, String, Person] =
      DeriveSchema.gen[Person]
    val (id, firstName) = ProjectionExpression.accessors[Person]
  }
  val examplePerson = Person(1, "avi")

  private val program = for {
    _ <- put("personTable", examplePerson).execute
    person <- get("personTable")(Person.id.partitionKey === 1).execute
    _ <- zio.Console.printLine(s"hello $person")
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
