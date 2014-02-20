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
  
  
  class ValuesManagerActor extends Actor {
    val vma = self
    val checkerRouter = system.actorOf(
        Props(classOf[CheckerActor], vma).withRouter(SmallestMailboxRouter(4)),
        "CheckerActorRouter")
    
    private var currentPrimeNth=primeNth
    private var currentNotPrimeNth=notPrimeNth
    private var buffer=List.empty[NUM]
    private var nextValue=startFrom
    
    private var checkedValuesQueue = Queue.empty[CheckedValue[NUM]]
    
    def receive = {
      case PartialResult(value, true) if value == nextValue =>
        currentPrimeNth+=one
        val res = CheckedValue[NUM](value, true, value.toString.size, currentPrimeNth)
        checkedValuesQueue = checkedValuesQueue.enqueue(res)
        
      case PartialResult(value, false) if value == nextValue =>
        currentNotPrimeNth+=one
        val res = CheckedValue[NUM](value, false, value.toString.size, currentNotPrimeNth)
        checkedValuesQueue = checkedValuesQueue.enqueue(res)
        
      case NextCheckedValue() =>
        //val next =
        //checkedValuesQueue = checkedValuesQueue.enqueue(res)
        
      case pr:PartialResult => // Then delay
        
    }
  }
  
  
  private val manager =  system.actorOf(Props[ValuesManagerActor], "ValueManagerActor")
  
}
