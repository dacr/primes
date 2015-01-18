package fr.janalyse.primes

import akka.actor._
import ActorDSL._
import akka.routing.SmallestMailboxPool
import scala.collection.immutable.Queue
import com.typesafe.config.ConfigFactory

class ActorsPrimesGenerator[NUM](
  handler: CheckedValue[NUM] => Unit = ActorsPrimesGenerator.mkPrintHandler[NUM](),
  name: String = "DefaultActordBasedPrimesGeneratorSystem",
  startFrom: NUM = 2,
  primeNth: NUM = 1,
  notPrimeNth: NUM = 1)(implicit numops: Integral[NUM]) extends PrimesDefinitions[NUM] with Shutdownable {
  import numops._

  val config = ConfigFactory.load()
  implicit val system = ActorSystem(name, config.getConfig("primes").withFallback(config))

  case class NextValue(value: NUM)
  case class PartialResult(value: NUM, isPrime: Boolean)

  object CheckerActor {
    def props() = Props(new CheckerActor)
  }

  class CheckerActor extends Actor {
    def receive = {
      case NextValue(value) =>
        sender ! PartialResult(value, isPrime(value))
    }
  }

  /* ValuesManagerActor is the primes computation coordinator
   * it manages checked values order and affect the right position to
   * primes and not primes values.
   */
  case class CheckedValueAckMessage(count: Long)

  class ValuesManagerActor(
    forActor: ActorRef,
    precomputedCount: Int = 30000,
    checkerWorkers: Int = Runtime.getRuntime.availableProcessors) extends Actor {
    val checkerRouter = context.actorOf(
      CheckerActor.props.withRouter(SmallestMailboxPool(checkerWorkers)),
      "CheckerActorRouter")

    private var nextPrimeNth = primeNth
    private var nextNotPrimeNth = notPrimeNth
    private var currentValue = startFrom // waiting the result for this value
    private var nextValue = startFrom // next value to send to checker worker

    // buffered partial results, because we need ordering to compute primes & not primes position 
    private var waitBuffer = Map.empty[NUM, PartialResult]

    private var inpg = 0L
    private var sentAckDelta = 0L

    private def processPartialResult(partial: PartialResult) {
      val digitCount = partial.value.toString.size
      currentValue += one
      val nth = if (partial.isPrime) nextPrimeNth else nextNotPrimeNth
      if (partial.isPrime) nextPrimeNth += one else nextNotPrimeNth += one
      val newResult = CheckedValue[NUM](partial.value, partial.isPrime, digitCount, nth)
      forActor ! newResult
      sentAckDelta += 1L
    }

    private def flush2order() {
      while (waitBuffer.size > 0 && waitBuffer.contains(currentValue)) {
        val pr = waitBuffer(currentValue)
        processPartialResult(pr)
        waitBuffer = waitBuffer - pr.value
      }
    }

    private def prepareNexts() {
      if (sentAckDelta < 20000) {
        for { _ <- inpg to precomputedCount } {
          checkerRouter ! NextValue(nextValue)
          nextValue += one
          inpg += 1
        }
      }
    }

    prepareNexts()

    def receive = {
      case pr: PartialResult if pr.value == currentValue =>
        inpg -= 1
        processPartialResult(pr)
        flush2order()
        prepareNexts()

      case pr: PartialResult => // Then delay
        inpg -= 1
        waitBuffer += pr.value -> pr
        prepareNexts()

      case CheckedValueAckMessage(count) =>
        sentAckDelta -= count
        prepareNexts()

    }
  }

  class DealerActor[NUM](handler: CheckedValue[NUM] => Unit) extends Actor {
    val groupedAckSize = 500L
    var valuesCounter: Long = 0l
    def receive = {
      case chk: CheckedValue[NUM] =>
        handler(chk)
        valuesCounter += 1
        if (valuesCounter % groupedAckSize == 0L) sender ! CheckedValueAckMessage(groupedAckSize)
    }
  }

  private val dealer = actor("DealerPrinter") {
    new DealerActor(handler)
  }

  private val manager = actor("ValuesManagerActor") {
    new ValuesManagerActor(dealer)
  }

  override def shutdown() {
    system.shutdown()
  }

}




object ActorsPrimesGenerator {
  def mkPrintHandler[NUM]() : CheckedValue[NUM] => Unit = {
    def now = System.currentTimeMillis()
    var counter: Long = 0l
    val startedTime = now
    var lastOutputTime = now
    def timeSpentSinceStartInS = (now - startedTime) / 1000
    def timeSpentSinceLastOutput = {
      val newLastOutputTime = now
      val r = (newLastOutputTime - lastOutputTime)
      lastOutputTime = newLastOutputTime
      r
    }
    def handler(chk: CheckedValue[NUM]) {
      import chk._
      if (chk.isPrime) {
        counter += 1
        if (counter % 10000L == 0L) {
          println(s"$value is the $nth prime number. ${timeSpentSinceStartInS}s - ${timeSpentSinceLastOutput}ms")
        }
      }
    }
    handler(_)
  }
}
