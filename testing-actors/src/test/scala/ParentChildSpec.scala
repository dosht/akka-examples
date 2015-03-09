import akka.actor.{ ActorSystem, Actor, ActorRef, ActorRefFactory, Props, ActorLogging }
import akka.testkit.{ TestActorRef, TestActor, TestKit, ImplicitSender, TestProbe }
import org.scalatest.{ WordSpecLike, Matchers, BeforeAndAfterAll }

import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success

class MockedChild extends Actor {
  def receive = {
    case "ping" => sender ! "pong"
  }
}

class ParentChildSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("MySpec"))

  implicit val timeout = Timeout(5 seconds)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A DependentChild" should {
    "be tested without its parent" in {
      val probe = TestProbe()
      val child = system.actorOf(Props(classOf[DependentChild], probe.ref))
      probe.send(child, "ping")
      probe.expectMsg("pong")
    }
  }

  "A DependentParent" should {
    "be tested with custom props" in {
      val probe = TestProbe()
      val parent = TestActorRef(new DependentParent(Props[MockedChild]))
      probe.send(parent, "pingit")
      // test some parent state change
      within(100 millis) {
        parent.underlyingActor.ponged should be(true)
      }
    }
  }

  "A GenericDependentParent" should {
    "be tested with a child probe" in {
      val probe = TestProbe()
      val maker = (_: ActorRefFactory) => probe.ref
      val parent = system.actorOf(Props(classOf[GenericDependentParent], maker))
      probe.send(parent, "pingit")
      probe.expectMsg("ping")
    }
  }

  "A fabricated parent" should {
    "test its child responses" in {
      val proxy = TestProbe()
      val parent = system.actorOf(Props(new Actor {
        val child = context.actorOf(Props[Child], "child")
        def receive = {
          case x if sender == child => proxy.ref forward x
          case x => child forward x
        }
      }))

      proxy.send(parent, "ping")
      proxy.expectMsg("pong")
    }
  }
}

class Parent extends Actor {
  val child = context.actorOf(Props[Child], "child")
  var ponged = false

  def receive = {
    case "pingit" => child ! "ping"
    case "pong" => ponged = true
  }
}

class Child extends Actor {
  def receive = {
    case "ping" => context.parent ! "pong"
  }
}

class DependentChild(parent: ActorRef) extends Actor {
  def receive = {
    case "ping" => parent ! "pong"
  }
}

class DependentParent(childProps: Props) extends Actor {
  val child = context.actorOf(childProps, "child")
  var ponged = false

  def receive = {
    case "pingit" => child ! "ping"
    case "pong" => ponged = true
  }
}

class GenericDependentParent(childMaker: ActorRefFactory => ActorRef) extends Actor {
  val child = childMaker(context)
  var ponged = false

  def receive = {
    case "pingit" => child ! "ping"
    case "pong" => ponged = true
  }
}