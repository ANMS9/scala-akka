package com.lightbend.akka.sample

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }

/**
  * Defining Actors and messages
  *
  * Can send boxed primitive values (such as String, Integer, Booleen etc) as messages
  * as well as plain data structures like arrays & collections
  *
  * Case class and objects make excellent messages since they are immutable
  *
  * To keep in mind:
  *
  * - Since messages are the Actor's public API, it's a good practice to define messages
  * with good names and rich semantic
  * - Messages should be immutable, since they are shared between different threads
  * - Good practice to put an actor's associated messages in its companion object. This
  * makes it easier to understand what type of messages the actor expects and handles
  * - It is also common pattern to use a 'props' method in the companion object that
  * describes how to construct the Actor
  *
  */


object Greeter {
  /**
    * Props is a configuration class to specify options for the creation of actors.
    * Think of it as an immutable and thus freely shareable recipe for creating an
    * actor that can include associated deployment
    *
    * @param message - used when building greeting messages
    * @param printerActor - reference to the Actor handling the outputting of the greeting
    * @return creates and returns a Props instance
    */
  def props(message: String, printerActor: ActorRef): Props = Props(new Greeter(message, printerActor))
  final case class WhoToGreet(who: String)
  case object Greet
}

class Greeter(message: String, printerActor: ActorRef) extends Actor {
  import Greeter._
  import Printer._
  var greeting = ""

  /**
    * Defines behavior; how the Actor should react to the different messages it receives.
    * An Actor can have state. Accessing or mutating the internal state of an Actor can have
    * State
    * @return
    */
  def receive = {
    case WhoToGreet(who) =>
      greeting = s"$message, $who"
    case Greet           =>
      printerActor ! Greeting(greeting)

  }
}

object Printer {
  def props: Props = Props[Printer]
  final case class Greeting(greeting: String)
}

/**
  * Extends akka.actor.ActorLogging to automatically get a reference to a logger.
  * By doing this we can write 'log.info()' in the the Actor without any additional
  * importing or wiring
  */
class Printer extends Actor with ActorLogging {
  import Printer._
  def receive = {
    case Greeting(greeting) =>
      log.info(s"Greeting received (from ${sender()}): $greeting")
  }
}

object ActorGreeterExample extends App {
  import Greeter._
  val system: ActorSystem = ActorSystem("helloAkka")
  val printer: ActorRef = system.actorOf(Printer.props, "printerActor")
  val howdyGreeter: ActorRef =
    system.actorOf(Greeter.props("Howdy", printer), "howdyGreeter")
  val helloGreeter: ActorRef =
    system.actorOf(Greeter.props("Hello", printer), "helloGreeter")
  val goodDayGreeter: ActorRef =
    system.actorOf(Greeter.props("Good day", printer), "goodDayGreeter")

  howdyGreeter ! WhoToGreet("Akka")
  howdyGreeter ! Greet
  howdyGreeter ! WhoToGreet("Lightbend")
  howdyGreeter ! Greet
  helloGreeter ! WhoToGreet("Scala")
  helloGreeter ! Greet

  goodDayGreeter ! WhoToGreet("Play")
  goodDayGreeter ! Greet
}

