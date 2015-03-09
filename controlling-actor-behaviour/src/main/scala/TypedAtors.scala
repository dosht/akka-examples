package com.packt.akka

import akka.actor.{ TypedActor, TypedProps, ActorSystem }
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class User(username: String, password: String, email: String)

trait Authentication {
  def signup(user: User): Boolean
  def sendWelcomeMail(user: User): Unit
  def login(username: String, password: String): Future[String]
  def getUser(username: String): Option[User]
}

class AuthenticationImpl extends Authentication {
   var users: Set[User] = Set.empty
   var loggedInUsers: Set[User] = Set.empty

  def signup(user: User): Boolean = {
    if (users contains user)
      false
    else {
      users = users + user
      true
    }
  }

  def sendWelcomeMail(user: User): Unit = {
    println(s"Sending welcome mail to $user")
  }

  def login(username: String, password: String): Future[String] =
    users.filter {
      user =>
        user.username == username &&
        user.password == password
    }.headOption match {
      case Some(user) =>
        loggedInUsers = loggedInUsers + user
        Future.successful("123456789")
      case None => throw new Exception("username of password is not valid!")
    }

  def getUser(username: String): Option[User] = {
    Thread.sleep(10000)
    Some(users.filter(user => user.username == username).head)
  }

}

object AuthenticationApp extends App {
  val system = ActorSystem("AuthenticationApp")

  val authentication: Authentication = TypedActor(system).typedActorOf(TypedProps[AuthenticationImpl] ())

  val zeus = User("Zeus", "secret1", "zeus@greek.com")

  val signupResult = authentication.signup(zeus)
  if (signupResult == true) {
    authentication.sendWelcomeMail(zeus)
    println(s"$zeus has signed up successfully!")
  } else {
    println(s"$zeus is already registered!")
  }

  val tokenF = authentication.login("Zeus", "secret1")
  tokenF map {
    token =>
      println(s"The token is $token")
  } recover {
    case e: Exception => println(e.getMessage)
  }

  authentication.getUser("Zeus") match {
    case Some(user) => println(s"User data is $user")
    case None => println(s"Request timed out!")
  }

  TypedActor(system).stop(authentication)

  system.shutdown()
}
