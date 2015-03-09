package com.packt.akka

import akka.actor.{ ActorRef, Actor, ActorSystem, ActorLogging, Cancellable, Props, PoisonPill }
import scala.concurrent.duration._

object Cloud extends App {
  val system = ActorSystem()

  val server = system.actorOf(Props[ServerActor])
  val client = system.actorOf(Props[ClientActor])
  client ! ConnectTo(server)
  Thread.sleep(5000)
  client ! PoisonPill
  Thread.sleep(5000)

  system.shutdown()
}

case object HeartBeat
case class HearBeatTimeout(client: ActorRef)
case object Connect
case class ConnectTo(server: ActorRef)

class ServerActor extends Actor with ActorLogging {
  var clients: List[ActorRef] = Nil
  var heartBeatTimeout: Option[Cancellable] = None

  def receive = {
    case Connect =>
      log.info("Received connection request from client: {}", sender)
      clients = sender :: clients
      startHeartBeatTimout(sender)

    case HeartBeat =>
      log.info("Received HeartBeat from client: {}", sender)
      stillAlive(sender)

    case HearBeatTimeout(client) =>
      log.info("HeartBeat sopped for client: {}", client)
      clients = clients.filterNot(_ == client)
  }

  def startHeartBeatTimout(client: ActorRef): Unit = {
    import context.dispatcher
    heartBeatTimeout = Some(context.system.scheduler.scheduleOnce(2 seconds, self, HearBeatTimeout(client)))
  }

  def stillAlive(client: ActorRef): Unit = {
    heartBeatTimeout.map(_.cancel())
    startHeartBeatTimout(client)
  }
}

class ClientActor extends Actor with ActorLogging {
  var heartBeatTask: Option[Cancellable] = None
  def receive = {
    case ConnectTo(server) =>
      server ! Connect
      startHeartBeat(server)
  }

  override def postStop(): Unit = {
    log.info("cancel heartBeatTask")
    heartBeatTask.map(_.cancel())
  }

  def startHeartBeat(server: ActorRef): Unit = {
    import context.dispatcher
    val heartBeatTask = Some(context.system.scheduler.schedule(0 milliseconds, 1 seconds, server, HeartBeat))
  }
}