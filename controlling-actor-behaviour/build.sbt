name := "Controling Actor Behaviour"

version := "1.0"

scalaVersion := "2.11.2"

sbtVersion := "0.13.5"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.7")