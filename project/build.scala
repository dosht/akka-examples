import sbt._
import Keys._

object ActorsExcamples extends Build {
    lazy val playingWitActors = Project(id = "playing-with-actors", base = file("playing-with-actors"))
    lazy val scaleOut = Project(id = "scale-out", base = file("scale-out"))
    lazy val scaleUp = Project(id = "scale-up", base = file("scale-up"))
    lazy val controllingActorBehaviour = Project(id = "controlling-actor-behaviour", base = file("controlling-actor-behaviour"))
    lazy val testingActors = Project(id = "testing-actors", base = file("testing-actors"))
    lazy val advancedAkkaTools = Project(id = "advanced-akka-tools", base = file("advanced-akka-tools"))
}