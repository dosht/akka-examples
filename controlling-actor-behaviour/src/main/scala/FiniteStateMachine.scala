package com.packt.akka

import akka.actor.{ ActorSystem, Actor, Props, FSM, ActorLogging }
import akka.pattern.pipe
import scala.concurrent.Future

import scala.concurrent.duration._

sealed trait Message
case class Withdrawal(amount: Int) extends Message
case object ValidTransaction extends Message
case object InvalidTransaction extends Message
case object TakeCard extends Message

sealed trait Data
case object NoCard extends Data
case class Card(number: String) extends Data
case class ProcessingWithdrawal(card: Card, amount: Int) extends Data

sealed trait State
case object Idle extends State
case object Active extends State
case object Processing extends State

class ATM extends FSM[State, Data] with ActorLogging {
  import context.dispatcher

  startWith(Idle, NoCard)

  when(Idle) {
    case Event(Card(number), NoCard) =>
      println(s"using card $number..")
      goto(Active) using Card(number)
  }

  when(Active, stateTimeout = 1 second) {
    case Event(Withdrawal(amount), card: Card) =>
      println("Processing your transaction. Please wait...")
      Future {
        Thread.sleep(300)
        ValidTransaction
      } pipeTo self
      goto(Processing) using ProcessingWithdrawal(card, amount) forMax(1 second)

    case Event(TakeCard, card: Card) =>
      println("Please take your card.")
      goto(Idle) using NoCard

    case Event(StateTimeout, card: Card) =>
      println("Don't forget your card!")
      stay using card
  }

  when(Processing) {
    case Event(ValidTransaction, ProcessingWithdrawal(card, amount)) =>
      println(s"Please take your money: $amount.")
      goto(Active) using card

    case Event(InvalidTransaction | StateTimeout, ProcessingWithdrawal(card, amount)) =>
      println("Sorry your transaction couldn't complete!")
      goto(Active) using card
  }

  whenUnhandled {
    case Event(e, s) =>
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      stay
  }

  initialize()
}

object FiniteStateMachine extends App {
  val system = ActorSystem("finite-state-machine")

  val atm = system.actorOf(Props[ATM])
  Thread.sleep(10)

  atm ! Card("123456789")

  atm ! Withdrawal(500)

  atm ! TakeCard

  Thread.sleep(1000)

  system.shutdown()
}
