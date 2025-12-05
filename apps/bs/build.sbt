import Dependencies.*

name := "kakeibo-bs"

ThisBuild / scalaVersion := "3.7.4"
ThisBuild / scalacOptions ++= Seq(
  // format: off
  "-encoding", "utf8",
  // format: on
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xfatal-warnings",
  "-Wvalue-discard",
  "-Wnonunit-statement",
  "-Wunused:imports",
  "-Wunused:locals",
  "-Wunused:params",
  "-Wunused:privates",
  "-Wunused:patvars",
  "-Wunused:implicits"
)

lazy val root = project
  .in(file("."))
  .settings(
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core" % "1.4.0",
      "com.amazonaws" % "aws-lambda-java-events" % "3.16.1",
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      "dev.zio" %% "zio-test-magnolia" % zioVersion % Test
    ),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  )