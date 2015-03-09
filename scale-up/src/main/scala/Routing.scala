package com.packt.akka

import akka.actor.{ ActorSystem, Actor, ActorRef, Props, Terminated, PoisonPill }
import akka.routing.{ ActorRefRoutee, Router, RoundRobinRoutingLogic }

case class Work(task: Option[String])

class Worker extends Actor {
  def receive = {
    case Work(Some(task)) =>
      println(s"Doing $task...")

    case Work(None) =>
      self ! PoisonPill
  }
}

class Master extends Actor {
  var router = {
    val routees = Vector.fill(5) {
      val r = context.actorOf(Props[Worker])
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  def receive = {
    case w: Work =>
      router.route(w, sender())

    case Terminated(a) =>
      println(s"$a is stopped and another routee will be created")
      router = router.removeRoutee(a)
      val r = context.actorOf(Props[Worker])
      context watch r
      router = router.addRoutee(r)
  }
}

object Routing extends App {
  val system = ActorSystem("routing")

  val master = system.actorOf(Props[Master], "master")
  master ! Work(Some("task 1"))
  master ! Work(Some("task 2"))
  master ! Work(Some("task 3"))
  master ! Work(None)

  Thread.sleep(100)

  system.shutdown()
}
