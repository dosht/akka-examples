name := "Scale Up"

version := "1.0"

scalaVersion := "2.11.2"

sbtVersion := "0.13.5"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.7",
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.3.9",
  "org.scalatest" % "scalatest_2.11" % "2.2.1",
  "com.typesafe.akka" %% "akka-remote" % "2.3.7")