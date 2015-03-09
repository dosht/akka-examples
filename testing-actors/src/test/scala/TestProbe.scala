import akka.actor.{ ActorSystem, Actor, ActorRef, FSM, Props, ActorLogging }
import akka.testkit.{ TestActorRef, TestActor, TestKit, ImplicitSender, TestProbe }
import org.scalatest.{ WordSpecLike, Matchers, BeforeAndAfterAll }

import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success

class MockSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("MySpec"))

  implicit val timeout = Timeout(5 seconds)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "APIActor" must {
    "Use the real database actor but assert it receives the correct message" in {
      val realDB = TestActorRef[DB]
      val dbTestProbe = TestProbe()
      val api = TestActorRef(new API(dbTestProbe.ref))
      api ! Request("123456", "get", "users", Some("837462934"), None)
      dbTestProbe.expectMsg(GetUser("837462934", Seq("username", "email")))
      dbTestProbe.forward(realDB)
      val userData = User("837462934",
                          Some("zeus"),
                          Some("zeus@greek.com"),
                          None,
                          None)
      expectMsg(300 millis, Response("123456", userData))
    }
    "Mock with TestProp" in {
      val dbTestProbe = TestProbe()
      val api = TestActorRef(new API(dbTestProbe.ref))
      api ! Request("123456", "get", "users", Some("837462934"), None)
      dbTestProbe.expectMsg(GetUser("837462934", Seq("username", "email")))
      val userData = User("837462934",
                          Some("zeus"),
                          Some("zeus@greek.com"),
                          None,
                          None)
      dbTestProbe.reply(userData)
      expectMsg(Response("123456", userData))
    }
    "Mock with AutoPiolot" in {
      val dbTestProbe = TestProbe()
      val api = TestActorRef(new API(dbTestProbe.ref))
      val userData = User("837462934",
                          Some("zeus"),
                          Some("zeus@greek.com"),
                          None,
                          None)
      dbTestProbe.setAutoPilot(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot = {
          msg match {
            case "stop" => TestActor.NoAutoPilot
            case _ =>
              sender ! userData
              TestActor.KeepRunning
          }
        }
      })
      api ! Request("123456", "get", "users", Some("837462934"), None)
      dbTestProbe.expectMsg(GetUser("837462934", Seq("username", "email")))
      expectMsg(Response("123456", userData))
    }
  }
}

case class Request(clientId: String,
                   method: String,
                   collection: String,
                   id: Option[String],
                   body: Option[String])

case class Response(clientId: String, data: Data)

case class GetUser(id: String, fields: Seq[String])

trait Data
case class User(id: String,
                username: Option[String],
                email: Option[String],
                age: Option[Int],
                bio: Option[String]) extends Data

class API(val db: ActorRef) extends Actor {
  implicit val timeout = Timeout(5 seconds)
  import context.dispatcher

  def receive = {
    case Request(cid, "get", "users", Some(id), _) =>
      val requester = sender()
      (db ? GetUser(id, Seq("username", "email"))).mapTo[Data].map { data =>
        requester ! Response(cid, data)
      }
  }
}

class DB extends Actor with ActorLogging {
  import context.dispatcher

  def receive = {
    case request =>
      log.info(s"db received this request: $request")
      val requester = sender()
      Future {
        Thread.sleep(200)
        requester ! User("837462934",
                          Some("zeus"),
                          Some("zeus@greek.com"),
                          None,
                          None)
      }
  }
}
