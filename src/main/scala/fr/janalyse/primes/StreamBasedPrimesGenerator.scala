package fr.janalyse.primes

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.{ Broadcast, FlowGraph, ForeachSink, Source }

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.{ Failure, Success, Try }
import akka.stream.scaladsl.FlowGraphImplicits

class StreamBasedPrimesGenerator[NUM](
  name: String = "DefaultPrimesGeneratorSystem",
  startFrom: NUM = 2,
  primeNth: NUM = 1,
  notPrimeNth: NUM = 0)(implicit numops: Integral[NUM]) extends PrimesDefinitions[NUM] {
  import numops._

  implicit val system = ActorSystem("Sys")
  import system.dispatcher
  implicit val materializer = FlowMaterializer()

  class NumIterator extends Iterator[NUM] {
    private var num: NUM = startFrom
    override def next(): NUM = {
      val ret = num
      num = num + one
      num
    }
  }

  private val numIterator = new NumIterator()
  val primeSource: Source[NUM]
  Source(() => numIterator)
    .filter(rnd => isPrime(rnd))

  val consoleSink = ForeachSink[Int](println)

  val output = new java.io.PrintWriter(new java.io.FileOutputStream("target/primes.txt"), true)
  val slowSink = ForeachSink[Int] { prime =>
    output.println(prime)
    // simulate slow consumer
    Thread.sleep(1000)
  }

  val materialized = FlowGraph { implicit builder =>
    import FlowGraphImplicits._
    val broadcast = Broadcast[Int] // the splitter - like a Unix tee
    primeSource ~> broadcast ~> slowSink // connect primes to splitter, and one side to file
    broadcast ~> consoleSink // connect other side of splitter to console
  }.run()

}

