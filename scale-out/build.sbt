name := "Scale Out"

version := "1.0"

scalaVersion := "2.11.2"

sbtVersion := "0.13.5"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.7",
  "com.typesafe.akka" %% "akka-remote" % "2.3.7",
  "com.typesafe.akka" %% "akka-cluster" % "2.3.7",
  "com.typesafe.akka" %% "akka-cluster-metrics" % "2.4-SNAPSHOT",
  "io.kamon" % "sigar-loader" % "1.6.5-rev001",
  "org.fusesource" % "sigar" % "1.6.4",
  "com.typesafe.akka" %% "akka-multi-node-testkit" % "2.3.7")