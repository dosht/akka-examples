// package com.packt.akka

// import akka.actor.{ ActorSystem, Actor, Props }

// import com.typesafe.config.ConfigFactory

// object Backend extends App {
//   val clusterConfig = ConfigFactory.load.getConfig("Backend")
//   val system = ActorSystem("Cluster", clusterConfig)
//   system.actorOf(Props[BackendActor], name = "backend")

//   Thread.sleep(10000)
//   system.shutdown()
// }

// class BackendActor extends Actor {
//   def receive = {
//     case "ping" =>
//       println(s"ping received from $sender")
//       sender ! "pong"
//   }
// }

// object API extends App {
//   val clusterConfig = ConfigFactory.load.getConfig("API")
//   val system = ActorSystem("Cluster", clusterConfig)
//   system.actorOf(Props[ApiActor], name = "api")

//   Thread.sleep(10000)
//   system.shutdown()
// }

// class ApiActor extends Actor {
//   override def preStart() {
//     val backendActor = context.actorSelection(
//       "akka.tcp://Cluster@127.0.0.1:2552/user/backend")
//     backendActor ! "ping"
//   }

//   def receive = {
//     case "pong" =>
//       println(s"pong received from $sender")
//   }
// }
