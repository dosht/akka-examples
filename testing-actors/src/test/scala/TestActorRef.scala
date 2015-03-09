import akka.actor.{ ActorSystem, Actor, FSM, Props }
import akka.testkit.{ TestActorRef, TestFSMRef, TestKit, ImplicitSender }
import org.scalatest.{ WordSpecLike, Matchers, BeforeAndAfterAll }

import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success

class SyncSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("MySpec"))

  implicit val timeout = Timeout(5 seconds)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "Echo" must {
    "reply with the same message and assert using ask pattern" in {
      val echo = TestActorRef[Echo]
      val future = echo ? "hello world"
      val Success(response) = future.value.get
      response should be("hello world")
    }
    "reply with the same message and assert using implicit sender" in {
      val echo = TestActorRef[Echo]
      echo ! "hello world"
      expectMsg(1 second, "hello world")
      expectNoMsg
    }
    "expect message class" in {
      within (500 millis) {
        val echo = TestActorRef[Echo]
        echo ! Some("hello world")
        val response = expectMsgClass(classOf[Some[String]])
        response should be(Some("hello world"))
      }
    }
  }
  "Calculator" must {
    "store calculate the result and store it in the internal state" in {
      val calculator = TestActorRef[Calculator]
      calculator.receive(Divide(10, 5))
      calculator.underlyingActor.result should be(2)
    }
    "throws an exception if the denominator is Zero" in {
      val calculator = TestActorRef[Calculator]
      intercept[IllegalArgumentException] {
        calculator.receive(Divide(1, 0))
      }
    }
  }
  "FSMActor" must {
    "start in 'inactive' state" in {
      val fsm = TestFSMRef(new TestFsmActor())
      fsm.stateName should be("inactive")
    }
    "change the state to active after sending any message" in {
      val fsm = TestFSMRef(new TestFsmActor())
      fsm ! "go"
      fsm.stateName should be("active")
      fsm.stateData should be("go")
    }
  }
}

class Echo extends Actor {
  def receive = {
    case x => sender ! x
  }
}

case class Divide(numerator: Int, denominator: Int)

class Calculator extends Actor {
  var result = 0

  def receive = {
    case Divide(_, 0) =>
      throw new IllegalArgumentException("The denominator can't be zero!")

    case Divide(numerator, denominator) =>
      result = numerator / denominator
  }
}

class TestFsmActor extends FSM[String, String] {
  startWith("inactive", "")

  when("inactive") {
    case Event(data: String, _) => goto("active") using data
  }

  when("active") {
    case _ => stay
  }

  initialize()
}
