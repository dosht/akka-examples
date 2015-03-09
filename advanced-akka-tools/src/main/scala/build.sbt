name := "Playing with Actors"

version := "1.0"

scalaVersion := "2.11.2"

sbtVersion := "0.13.5"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.7",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.9",
  "org.scalatest" %% "scalatest" % "2.2.1",
  "com.typesafe.akka" %% "akka-agent" % "2.3.9",
  "com.typesafe.akka" %% "akka-remote" % "2.3.7")