import java.nio.file.Paths

import akka.{Done, NotUsed}
import akka.stream._
import akka.stream.scaladsl._
import akka.actor.ActorSystem
import akka.util.ByteString

import scala.concurrent.Future


//Reactive Streams is needed for the project
object DataImporter extends App {

/**
  *
  */

  implicit val system = ActorSystem("QuickStart")
  /**
    * The Materializer is a factory for stream execution engines - makes the stream run
    * The materializer is picked up implicitly if it is omitted from the run method call
    */
  implicit val materializer = ActorMaterializer()

  implicit val ec = system.dispatcher

  val numbers = 1 to 50

  // We create a Source that will iterate over the number sequence
  val numberSource: Source[Int, NotUsed] = Source.fromIterator(() => numbers.iterator)

  // Only let pass even numbers through the flow
  /**
    * Flow represents a Stream (set of processing steps) with one open input and one open output
    *
    * Basically a Flow is an ordered chain of transformations to its input, the cumulative effect of which it emits
    * on its output.
    * Takes three type parameters
    * 1. - the input data type
    * 2. - the output data type
    * 3. -
    */
  val isEvenFlow: Flow[Int, Int, NotUsed] = Flow[Int].filter((num) => num % 2 == 0)

  // Create a Source of even random numbers by combining the random number Source with
  // the even
  val evenNumberSource: Source[Int, NotUsed] = numberSource.via(isEvenFlow)

  /**
    * Scan combinator runs a computation over the whole stream: starting with the number 1
    * the operation emits the initial value and then every calculation result.
    * This yields the series of factorial numbers which is stash away as a Source
    *
    * IOResult is a type that IO operations return in Akka Streams in order to tell you how many bytes
    */
  val factorials = evenNumberSource.scan(BigInt(1))((acc, next) => acc * next)
  val result: Future[IOResult] =
    factorials
      .map(num => ByteString(s"$num\n"))
      .runWith(FileIO.toPath(Paths.get("factorials.txt")))

  // A Sink that will write its input onto the console
  /**
    * Sink represents a Stream ( set of processing steps) with only one open input
    *
    * Basically it's a subscriber of the data sent/processed by a Source
    *
    * Usually it outputs its input to some system IO (TCP port, console, file, etc.) cr
    */
  val consoleSink: Sink[Int, Future[Done]] = Sink.foreach[Int](println)
//  def consoleSink(filename: String) : Sink[String, Future[IOResult]] =
//    Flow[String]
//      .map(s => ByteString(s + "\n"))
//      .toMat(FileIO.toPath(Paths.get(filename)))(Keep.right)

  // Connect the Source with the Sink and run it using the materializer
  //evenNumberSource.runWith(consoleSink)

  /**
    * Source Type is parameterized with two types
    * 1. - type of element that this source emits
    * 2. - the second one may signal that running the source produces some auxilary value
    *      e.g. -  a network source may provide information about the bound port or peer
    *      - Where no auxxiliary infomation is produced, the type akka.NotUse is used
    */
  //val source: Source[Int, NotUsed] = Source(1 to 100)

  /**
    * Complement the source with a consumer function and pass this little stream setup to
    * an Actor that runs it
    * - This activation is signaled by having "run" be part of the method name; there are
    * other methods that run Akka Streams, and they all follow this pattern.
    *
    * When running the source in a scala.App you might notice it does not terminate,
    * because the ActorSystem is never terminated. Luckily runForeach returns a Done
    * which resovles when the stream finishes
    */
  //val done: Future[Done] = source.runForeach(i => println(i))(materializer)

}
