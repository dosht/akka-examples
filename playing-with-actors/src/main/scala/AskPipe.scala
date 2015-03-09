package com.packt.akka

import akka.actor.{ ActorSystem, Actor, ActorRef, Props }
import akka.pattern.{ ask, pipe }
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._

class Apollo extends Actor {
  import Apollo._
  import context.dispatcher

  def receive = {
    case MakeProphecy =>
      val requester = sender()
      Future{
          println("Thinking...")
          Thread.sleep(100)
        Prophecy("Something will happen!", requester)
      } pipeTo self

    case Prophecy(prophecy, requester) =>
      println("Prophecy is received")
      requester ! prophecy
  }
}

object Apollo {
  case object MakeProphecy
  case class Prophecy(prophecy: String, requester: ActorRef)
}

class Athena extends Actor {
  import Athena._

  def receive = {
    case Question => sender ! Answer
  }
}

object Athena {
  case object Question
  case object Answer
}

object AskPipe extends App {
  val system = ActorSystem("ask-pipe")

  implicit val timeout = Timeout(5 seconds)
  import system.dispatcher

  val athena = system.actorOf(Props[Athena])
  athena ? Athena.Question map {
    case Athena.Answer => println("Answer is received")
  }

  val apollo = system.actorOf(Props[Apollo])
  apollo ? Apollo.MakeProphecy map println

  Thread.sleep(1000)

  system.shutdown()
}
