package com.packt.akka.h

import akka.actor.{ Actor, ActorRef, ActorSystem, ActorLogging, Props }

object BecomeUnbecome extends App {
  val system = ActorSystem("become-unbecome")

  val server = system.actorOf(Props[WebsocketServer], "server")
  val client = system.actorOf(Props[WebsocketClient], "client")

  client ! ConnectTo(server)
  Thread.sleep(100)
  client ! Send("Hi")
  Thread.sleep(100)
  client ! ConnectTo(server)
  client ! Disconnect
  Thread.sleep(100)
  system.shutdown()
}

case object Connect
case object Connected
case object Disconnect
case class ConnectTo(server: ActorRef)
case class Send(msg: String)
case class Message(msg: String)

class WebsocketClient extends Actor with ActorLogging {
  var connectedServer: Option[ActorRef] = None

  def receive = disconnected orElse handleOtherMessages

  def disconnected: Actor.Receive = {
    case ConnectTo(server) =>
      log.info("Connecting to server: {}", server)
      server ! Connect
      context become (connecting orElse handleOtherMessages)
  }

  def connecting: Actor.Receive = {
    case Connected =>
      log.info("Connected to server: {}", sender)
      connectedServer = Some(sender)
      context become (connected orElse handleOtherMessages)
  }

  def connected: Actor.Receive = {
    case Disconnect =>
      log.info("Disconnecting...")
      connectedServer = None
      context.unbecome()

    case Send(msg) =>
      log.info("Sending message: {}", msg)
      connectedServer.get ! Message(msg)

    case Message(msg) =>
      log.info("Message received: {}", msg)
  }

  def handleOtherMessages: Actor.Receive = {
    case other => log.error("Received unexpected message: {}", other)
  }
}

class WebsocketServer extends Actor with ActorLogging {
  def receive = {
    case Connect =>
      log.info("A client connected: {}", sender)
      sender ! Connected

    case m: Message =>
      sender ! m
  }
}