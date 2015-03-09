package com.packt.akka

import akka.actor.{ Actor, ActorRef, ActorLogging, ActorSystem, Props, OneForOneStrategy }
import akka.actor.SupervisorStrategy.{ Restart, Stop }
import akka.pattern.ask
import akka.util.Timeout

import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

object Mailer extends App {
  implicit val timeout = Timeout(5 seconds)
  val config = ConfigFactory.load
  val system = ActorSystem("Mailer", config)

  val mailer = system.actorOf(Props[Mailer], "mailer")
  val responsesFutures =
    for (i <- 1 to 10) yield mailer ? s"email #$i"

  Future.sequence(responsesFutures).map(_ => println("All messages have been sent"))

  Thread.sleep(10000)
  system.shutdown()
}

class Mailer extends Actor with ActorLogging {
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 5) {
    case emailException: EmailException => {
      log.debug("Restarting after receiving EmailException : {}", emailException.getMessage)
      Restart
    }
    case unknownException: Exception => {
      log.debug("Giving up. Can you recover from this? : {}", unknownException)
      Stop
    }
    case unknownCase: Any => {
      log.debug("Giving up on unexpected case : {}", unknownCase)
      Stop
    }
  }

  def receive = {
    case msg: String =>
      context.actorOf(Props[MailerWorker]).forward(msg)
  }
}

class EmailException(val msg: String) extends Exception(msg)

class MailerWorker extends Actor with ActorLogging {
  var emailJob: Option[String] = None

  def receive = {
    case msg: String =>
      emailJob = Some(msg)
      sendSynchronousEmail(msg)
      sender ! "sent"
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    emailJob.map(self forward _)
  }

  def sendSynchronousEmail(msg: String) = {
      Thread.sleep(300)
      randomlyFail(msg)
      log.info("Sent message: {}", msg)
  }

  def randomlyFail(msg: String) {
    if(Random.nextInt % 3 == 0)
      throw new EmailException(s"message failed $msg")
  }
}
