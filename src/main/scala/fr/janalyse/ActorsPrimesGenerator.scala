package fr.janalyse.primes

import akka.actor._
import ActorDSL._
import akka.routing.SmallestMailboxRouter
import scala.collection.immutable.Queue

class ActorsPrimesGenerator[NUM](
  name: String = "DefaultPrimesGeneratorSystem",
  startFrom: NUM = 2,
  primeNth: NUM = 1,
  notPrimeNth: NUM = 0)(implicit numops: Integral[NUM]) extends PrimesDefinitions[NUM] {
  import numops._

  implicit val system = ActorSystem(name)

  case class WantNextValue()
  case class NextValue(value: NUM)

  class NextValueActor extends Actor {
    private var value = startFrom
    def receive = {
      case WantNextValue() =>
        sender ! NextValue(value)
        value += one
    }
  }

  case class PartialResult(value:NUM, isPrime:Boolean)
  case class NextCheckedValue()
  
  class CheckerActor extends Actor {
    def receive = {
      case NextValue(value) =>
        manager ! PartialResult(value, isPrime(value))
    }
  }
  
  /* ValuesManagerActor is the primes computation coordinators
   * it manage checked values order and affect the right position to
   * primes and not primes values.
   */
  class ValuesManagerActor(precomputedCount:Int=10000, checkerWorkers:Int=4) extends Actor {
    val vma = self
    val checkerRouter = system.actorOf(
        Props(classOf[CheckerActor], vma).withRouter(SmallestMailboxRouter(checkerWorkers)),
        "CheckerActorRouter")
    
    private var currentPrimeNth=primeNth
    private var currentNotPrimeNth=notPrimeNth
    private var nextValue=startFrom

    // buffered partial results, because we need ordering to compute primes & not primes position 
    private var waitBuffer=Map.empty[NUM, PartialResult]
    
    // checked values precomputed cache
    private var checkedValuesQueue = Queue.empty[CheckedValue[NUM]]

    
    private def addPrime(partial:PartialResult) {
      val digitCount = partial.value.toString.size
      if (partial.isPrime) {
        val res = CheckedValue[NUM](partial.value, true, digitCount, currentPrimeNth)
        currentPrimeNth+=one
        checkedValuesQueue = checkedValuesQueue.enqueue(res)
      } else {
        val res = CheckedValue[NUM](partial.value, false, digitCount, currentNotPrimeNth)
        currentNotPrimeNth+=one
        checkedValuesQueue = checkedValuesQueue.enqueue(res)
      }
      nextValue+=one
    }
    private def flush2order() {
      while(waitBuffer.size>0 && waitBuffer.contains(nextValue)) {
        val pr = waitBuffer(nextValue)
        addPrime(pr)
        waitBuffer = waitBuffer - pr.value
      }
    }
    
    private var currentUpTo=startFrom
    private def addWorkIfRequired() {
      //if (currentUpTo < nextValue + precomputedCount/2 ) {
      //}
    }
    
    
    def receive = {
      case pr:PartialResult if pr.value == nextValue =>
        addPrime(pr)
        flush2order()
        
      case pr:PartialResult => // Then delay
        waitBuffer+=pr.value -> pr
        
      case NextCheckedValue() =>
        //val next =
        //checkedValuesQueue = checkedValuesQueue.dequeue(res)
        addWorkIfRequired()
    }
  }
  
  
  private val manager =  system.actorOf(Props(classOf[ValuesManagerActor], 10000), "ValueManagerActor")
  
}
