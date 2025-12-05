package kakeibo

import software.amazon.awssdk.auth.credentials.{
  AwsBasicCredentials,
  StaticCredentialsProvider
}
import software.amazon.awssdk.regions.Region
import zio.aws.core.config
import zio.aws.core.config.CommonAwsConfig
import zio.aws.{dynamodb, netty}
import zio.dynamodb.DynamoDBExecutor
import zio.{ZIO, ZIOAppArgs, ZLayer}

import java.net.URI

trait MainBase {

  def provide(
      program: ZIO[DynamoDBExecutor & ZIOAppArgs, Any, Unit]
  ): ZIO[ZIOAppArgs, Any, Unit] =
    program.provideSome[ZIOAppArgs](
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
