package com.packt.akka

import akka.actor.{ ActorSystem, Actor, ActorRef, Props, ActorNotFound }
import akka.pattern.ask
import akka.util.Timeout

import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

case class Get(username: String)
case class Member(name: String, age: Int, family: String)

class MembersActor extends Actor  {
  val members = Map(
    "aphrodite" -> Member("Aphrodite", 4000, "Olympian"),
    "atlas" -> Member("Atlas", 3700, "Titans"))

  def receive = {
    case Get(username) => sender ! members.get(username)
  }
}

object MembersService extends App {
  val config = ConfigFactory.load.getConfig("MembersService")
  val system = ActorSystem("MembersService", config)
  val membersActor = system.actorOf(Props[MembersActor], "membersActor")
  println(membersActor.path)
}

object MembersServiceLookup extends App {
  val config = ConfigFactory.load.getConfig("MembersServiceLookup")
  val system = ActorSystem("MembersServiceLookup", config)
  implicit val timeout = Timeout(5 seconds)
  import system.dispatcher

  val membersActorSelection = system.actorSelection(
    "akka.tcp://MembersService@0.0.0.0:8000/user/membersActor")

  // Use ActorPath directly
  membersActorSelection ? Get("aphrodite") map (m => println(s"Aphrodite data is $m"))

  // Make sure that the actor is found and handle the other case
  membersActorSelection.resolveOne
    .flatMap(_ ? Get("aphrodite"))
    .map(memberData => println(s"Aphrodite data is $memberData"))
    .recover {
      case e: ActorNotFound => println(s"Service is not available now!")
    }

  Thread.sleep(1000)
  system.shutdown()
}


object MembersServiceRemoteCreation extends App {
  val config = ConfigFactory.load.getConfig("MembersServiceRemoteCreation")
  val system = ActorSystem("MembersServiceRemoteCreation", config)
  implicit val timeout = Timeout(5 seconds)
  import system.dispatcher

  val membersActor = system.actorOf(Props[MembersActor], "membersActorRemote")
  println(s"The remote path of membersActor is ${membersActor.path}")
  membersActor ? Get("atlas") map (m => println(s"Aphrodite data is $m"))

  Thread.sleep(1000)
  system.shutdown()
}
