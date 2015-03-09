package com.packt.akka

import akka.agent.Agent

import scala.concurrent.ExecutionContext.Implicits.global

object Agents extends App {
  // Immediate Values
  val agent = Agent(9)
  println("The immediate value of agent is: " + agent.get)
  agent map { value =>
    println(s"value of map is: $value")
  }

  // Changing agents values asynchronously
  agent send 10
  agent.future map { value =>
    println(s"Value of map.future is: $value")
  }

  agent send (_ + 9)
  agent.future map { value =>
    println(s"Value after sending a function is: $value")
  }

  // Alter and return a future
  agent alter 7 map { v =>
    println(s"value after alter is: $v")
  }

  agent alter (_ * 2) map { v =>
    println(s"Value after alter with a function is: $v")
  }

  // Agents are monadic
  val agent1 = Agent(10).map(_ * 10)
  val agent2 = Agent(10).flatMap(v => Agent(v * 10))
  val agent3 = for {
    v1 <- agent1
    v2 <- agent2
  } yield v1 * v2

  agent3.future map { v =>
    println(s"agent3: $v")
  }

  agent1.flatMap(v1 => agent2.map(v2 => v1 * v2)) map { v =>
    println(s"agent3: $v")
  }
}