name := "scala-akka"

version := "0.1"

scalaVersion := "2.12.4"

lazy val akkaVersion = "2.5.3"
lazy val akkaStreamVersion = "2.5.10"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaStreamVersion,
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)