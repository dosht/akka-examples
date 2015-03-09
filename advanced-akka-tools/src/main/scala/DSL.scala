package com.packt.akka

import akka.actor.ActorSystem
import akka.actor.ActorDSL._

object DSL extends App {
  case object Hello
  case object Switch
  case class CreateChild(name: String)
  case class Calculate(f: () => Int)
  case object Pause
  case object Play

  implicit val system = ActorSystem("DSL-demo")

  val zeus = actor("Zeus")(new ActWithStash {
    whenStarting { println("I'm starting") }
    become {
      case Hello => println(s"Hi, I'm Zues")
      case Switch => becomeStacked {
        case Hello => println("Hi, I'm Hera")
        case Switch => unbecome
      }
      case CreateChild(name) => actor(context, name)(new Act {
        whenStarting { println(s"Hello world, I'm ${self.path}") }
        become {
          case Calculate(f) =>
            val result = f()
            println(s"Result is: $result")
        }
        whenFailing { case (cause, msg) => println(s"Failing, cause: $cause, message: $msg") }
        whenRestarted { cause => println(s"Restarting, cause: $cause") }
      })
      case Pause => stash()
      case Play => unstashAll()
    }
    superviseWith(OneForOneStrategy() {
      case _: ArithmeticException => Restart
      case _: Exception => Stop
    })
    whenStopping { println("I'm stopping") }
  })

  zeus ! Hello
  zeus ! Switch
  zeus ! Hello
  zeus ! Switch
  zeus ! Hello

  zeus ! CreateChild("Heracles")
  val heracles = system.actorSelection("akka://DSL-demo/user/Zeus/Heracles")
  heracles ! Calculate(() => 5 + 5)
  heracles ! Calculate(() => 1 / 0)
  heracles ! Calculate(() => throw new Exception("Some Exception"))

  zeus ! Pause
  Thread.sleep(10)
  for (_ <- 1 to 10) zeus ! Hello
  println("Resume Zeus")
  zeus ! Play

  Thread.sleep(1000)
  system.shutdown()
}