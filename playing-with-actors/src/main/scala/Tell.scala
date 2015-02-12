package com.packt.akka

import akka.actor.{ ActorSystem, Actor, Props, ActorRef, PoisonPill }

case object HelloWorld
case class SayHelloTo(actor: ActorRef)
case class SayHelloFrom(actor: ActorRef)
case object SayHello
case class CreateChildren(names: Seq[String])

class Greek extends Actor {
  def receive = {
    case HelloWorld => println("Hello World!")
    case SayHelloTo(actor) =>
      println(s"Saying hello to $actor")
      actor ! SayHelloFrom(self)

    case SayHelloFrom(actor) =>
      println(s"$actor is saying hello to me")

    case SayHello =>
      println(s"$sender is saying hello to $self")

    case CreateChildren(names) =>
      names foreach (name => context.actorOf(Props[Greek], name))
      context.children foreach {
        child => child ! SayHello
    }
  }
}

object Tell extends App {
  val system = ActorSystem("tell-pattern")

  val zeus = system.actorOf(Props[Greek], "Zeus")
  zeus ! HelloWorld

  val hera = system.actorOf(Props[Greek], "Hera")
  hera ! SayHelloTo(zeus)

  zeus ! CreateChildren(Seq("Athena", "Heracles", "Helen"))

  system.stop(hera)
  zeus ! PoisonPill

  system.shutdown()
}
