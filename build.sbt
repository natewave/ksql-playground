name := "ksql-playground"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.3"

scalacOptions := Seq(
  "-encoding", "UTF-8",
  "-target:jvm-1.8",
  "-Ywarn-adapted-args",
  "-Ywarn-inaccessible",
  "-Ywarn-nullary-override",
  "-Ywarn-infer-any",
  "-Ywarn-dead-code",
  "-Ywarn-unused",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard",
  "-unchecked",
  "-deprecation",
  "-feature",
  "-g:vars",
  "-Xlint:_",
  "-opt:_"
)

val ksqlPlayground = (project in file(".")).settings(
  libraryDependencies ++=
    Seq("com.typesafe.akka" %% "akka-stream-kafka" % "0.17") ++
      Seq(
        "io.circe" %% "circe-generic",
        "io.circe" %% "circe-parser"
      ).map(_ % "0.8.0")
)
