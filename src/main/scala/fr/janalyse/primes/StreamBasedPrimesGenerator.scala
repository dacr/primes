package fr.janalyse.primes

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.{ Broadcast, FlowGraph, ForeachSink, Source }

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }
import akka.stream.scaladsl.FlowGraphImplicits
import akka.stream.scaladsl._

class NumericIterator[NUM](startFrom:NUM)(implicit numops: Integral[NUM]) extends Iterator[NUM] {
  import numops._
  private var num: NUM = startFrom - one
  override def next(): NUM = {
    num += one
    num
  }
  override def hasNext(): Boolean = true
}

object StreamBasedPrimesGenerator {
  def now=System.currentTimeMillis()
  def defaultHandlerFactory[NUM]():CheckedValue[NUM]=>Unit = {
    val started=now
    def followperf(n:CheckedValue[NUM]) {
      println(s"prime status $n ${(now-started)/1000}s")
    }
    followperf
  }
}

class StreamBasedPrimesGenerator[NUM](
  handler: CheckedValue[NUM] => Unit = StreamBasedPrimesGenerator.defaultHandlerFactory[NUM](),
  name: String = "DefaultStreamBasedPrimesGeneratorSystem",
  startFrom: NUM = 2,
  primeNth: NUM = 1,
  notPrimeNth: NUM = 1)(implicit numops: Integral[NUM]) extends PrimesDefinitions[NUM] with Shutdownable {
  import numops._

  implicit val system = ActorSystem(name)
  import system.dispatcher
  implicit val materializer = FlowMaterializer()
  
  case class TestedValue(value:NUM) {
    val state = isPrime(value)
    val digitCount = value.toString.size
  }
  
  val materialized = FlowGraph { implicit builder =>
    import FlowGraphImplicits._

    val valueIterator = new NumericIterator(startFrom)
    val values: Source[NUM] = Source(() => valueIterator)

    val isPrimeNthIterator = new NumericIterator(primeNth)
    val isPrimeNth = Source(() => isPrimeNthIterator)

    val isNotPrimeNthIterator = new NumericIterator(notPrimeNth)
    val isNotPrimeNth = Source(() => isNotPrimeNthIterator)

    val testedValues = Flow[NUM].mapAsync(x=> Future{TestedValue(x) })
    
    val checkedValues = Flow[(NUM, TestedValue)].map {
      case (nth, tv) => CheckedValue[NUM](tv.value, tv.state, tv.digitCount, nth)
    }
    val onlyPrimes = Flow[TestedValue].filter(_.state)
    val onlyNotPrimes = Flow[TestedValue].filter(! _.state)

    val out = ForeachSink[CheckedValue[NUM]](handler)

    val cast = Broadcast[TestedValue]
    val merge = Merge[CheckedValue[NUM]]
    val zipPrimeNth = Zip[NUM, TestedValue]
    val zipNotPrimeNth= Zip[NUM, TestedValue]

    isPrimeNth ~> zipPrimeNth.left
    isNotPrimeNth ~> zipNotPrimeNth.left

    values ~> testedValues ~> cast ~> onlyPrimes    ~> zipPrimeNth.right
                              cast ~> onlyNotPrimes ~> zipNotPrimeNth.right
                                                    zipPrimeNth.out    ~> checkedValues ~> merge
                                                    zipNotPrimeNth.out ~> checkedValues ~> merge
                                                                                           merge ~> out
  }.run()

  override def shutdown() { system.shutdown()}
}

