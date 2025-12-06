name := "kakeibo-financial-statement"

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
      "com.lihaoyi" %% "scalasql" % "0.2.3",
      ("com.mysql" % "mysql-connector-j" % "9.5.0")
        .exclude("com.google.protobuf", "protobuf-java")
    ),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  )
