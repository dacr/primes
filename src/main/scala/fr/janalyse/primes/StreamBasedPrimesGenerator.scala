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

  implicit val system = ActorSystem(name)
  import system.dispatcher
  implicit val materializer = FlowMaterializer()

  class NumIterator extends Iterator[NUM] {
    private var num: NUM = startFrom
    override def next(): NUM = {
      val ret = num
      num = num + one
      num
    }
    val lmax:NUM = five*ten*ten*ten*ten
    override def hasNext(): Boolean = true // num < lmax // < 50000
  }
  
  val started=now

  private val numIterator = new NumIterator()
  val primeSource: Source[NUM] =
    Source(() => numIterator)
      .filter(rnd => isPrime(rnd))

  val nopSink = ForeachSink[NUM](w => {})
  val consoleSink = ForeachSink[NUM](println)

  def now=System.currentTimeMillis()
  def followperf(n:NUM) {
    println(s"$n is a prime number. ${(now-started)/1000}s")
  }
  val perfSink = ForeachSink[NUM](followperf)
  
  val output = new java.io.PrintWriter(new java.io.FileOutputStream("target/primes.txt"), true)
  val fileSink = ForeachSink[NUM] { prime =>
    val reldur = System.currentTimeMillis() - started
    output.println(s"$prime  ($reldur)")
  }

  val materialized = FlowGraph { implicit builder =>
    import FlowGraphImplicits._
    //val broadcast = Broadcast[NUM] // the splitter - like a Unix tee
    //primeSource ~> broadcast ~> slowSink // connect primes to splitter, and one side to file
    //broadcast ~> consoleSink // connect other side of splitter to console
    
    primeSource ~> perfSink
  }.run()

  def shutdown() {
    system.shutdown()
  }
}

