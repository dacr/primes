package fr.janalyse.primes
/*
import akka.actor._
import ActorDSL._
import akka.routing.SmallestMailboxPool
import scala.collection.immutable.Queue
import com.typesafe.config.ConfigFactory

class ActorsPrimesUnorderedUnsafeGenerator[NUM](
  handler: CheckedValue[NUM] => Unit = ActorsPrimesGenerator.mkPrintHandler[NUM](),
  name: String = "DefaultActordBasedUnorderedPrimesGeneratorSystem",
  startFrom: NUM = 2,
  primeNth: NUM = 1,
  notPrimeNth: NUM = 1)(implicit numops: Integral[NUM]) extends PrimesDefinitions[NUM] with Shutdownable {
  import numops._

  val precomputedCount = 150L

  val config = ConfigFactory.load()
  implicit val system = ActorSystem(name, config.getConfig("primes").withFallback(config))

  case class NextValueToCompute(value: NUM)
  case class PartialResult(value: NUM, isPrime: Boolean, digitCount: Long)

  object CheckerActor {
    def props() = Props(new CheckerActor)
  }

  class CheckerActor extends Actor {
    def receive = {
      case NextValueToCompute(value) =>
        sender ! PartialResult(value, isPrime(value), value.toString.size)
    }
  }

  /* ValuesManagerActor is the primes computation coordinator
   * it affects the right position to
   * primes and not primes values.
   */
  case class CheckedValueAckMessage(count: Long)

  class ValuesManagerActor(
    forActor: ActorRef,
    checkerWorkers: Int = Runtime.getRuntime.availableProcessors) extends Actor {
    val checkerRouter = context.actorOf(
      CheckerActor.props.withRouter(SmallestMailboxPool(checkerWorkers)),
      "CheckerActorRouter")

    private var nextPrimeNth = primeNth
    private var nextNotPrimeNth = notPrimeNth
    private var currentValue = startFrom // waiting the result for this value
    private var nextValue = startFrom // next value to send to checker worker

    private var inpg = 0L

    private final def processPartialResult(partial: PartialResult) {
      currentValue += one
      val nth = if (partial.isPrime) nextPrimeNth else nextNotPrimeNth
      if (partial.isPrime) nextPrimeNth += one else nextNotPrimeNth += one
      val newResult = CheckedValue[NUM](partial.value, partial.isPrime, partial.digitCount, nth)
      forActor ! newResult
    }

    private final def prepareNexts() {
      if (inpg < precomputedCount / 2) {
        for { _ <- inpg to precomputedCount } {
          checkerRouter ! NextValueToCompute(nextValue)
          nextValue += one
          inpg += 1
        }
      }
    }

    prepareNexts()

    def receive = {
      case pr: PartialResult =>
        inpg -= 1
        processPartialResult(pr)
        prepareNexts()
    }
  }

  class DealerActor[NUM](handler: CheckedValue[NUM] => Unit) extends Actor {
    def receive = {
      case chk: CheckedValue[NUM] => handler(chk)
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

*/