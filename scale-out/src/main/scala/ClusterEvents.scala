package com.packt.akka

import akka.actor.{ ActorSystem, Actor, ActorRef, Props, ActorLogging, Terminated, RootActorPath }
import akka.cluster.{ Cluster, ClusterEvent, Member }

import com.typesafe.config.ConfigFactory

case object BackendRegisteration

object Backend extends App {
  val clusterConfig = ConfigFactory.load.getConfig("Backend")
  val system = ActorSystem("Cluster", clusterConfig)
  system.actorOf(Props[BackendActor], name = "backend")

  Thread.sleep(50000)
  system.shutdown()
}

class BackendActor extends Actor with ActorLogging {
  val cluster = Cluster(context.system)

  override def preStart() {
    cluster.subscribe(self, classOf[ClusterEvent.ClusterDomainEvent])
  }

  def receive = {
    case "ping" =>
      log.info("ping received from {}", sender)
      sender ! "pong"

    case ClusterEvent.MemberUp(member) =>
      if (member.roles.contains("Api")) {
        log.info("Api node found: {}", member.address)
        val backendPath = RootActorPath(member.address) / "user" / "api"
        context.actorSelection(backendPath) ! BackendRegisteration
    }
  }
}

object API extends App {
  val clusterConfig = ConfigFactory.load.getConfig("API")
  val system = ActorSystem("Cluster", clusterConfig)
  system.actorOf(Props[ApiActor], name = "api")

  Thread.sleep(10000)
  system.shutdown()
}

class ApiActor extends Actor with ActorLogging {
  var backends: List[ActorRef] = Nil
  val cluster = Cluster(context.system)

  override def preStart() {
    cluster.subscribe(self, classOf[ClusterEvent.ClusterDomainEvent])
  }

  override def postStop() {
    cluster.unsubscribe(self)
  }

  def receive = {
    case "pong" =>
      log.info("pong received from {}", sender)

    case BackendRegisteration if !(backends contains sender) =>
      context watch sender
      backends = sender :: backends
      sender ! "ping"

    case Terminated(backend) =>
      backends = backends.filterNot(_ == backend)

    case ClusterEvent.MemberRemoved(member, previousState) =>
      log.info("Member is removed, member address: {}, previousState: {}", member.address, previousState)

  }
}
