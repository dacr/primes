package fr.janalyse.primes

import akka.actor._
import ActorDSL._

class ActorsPrimesGenerator[NUM](
  name: String = "DefaultPrimesGenerator",
  startFrom: NUM = 2,
  primeNth: NUM,
  notPrimeNth: NUM)(implicit numops: Integral[NUM]) extends PrimesDefinitions[NUM] {
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
  
  class ValueManagerActor extends Actor {
    private var currentPrimeNth=primeNth
    private var currentNotPrimeNth=notPrimeNth
    private var buffer=List.empty[NUM]
    private var nextValue=startFrom
    def receive = {
      case PartialResult(value, true) if value == nextValue =>
        currentPrimeNth+=one
      case PartialResult(value, false) if value == nextValue =>
        currentNotPrimeNth+=one
      case pr:PartialResult => // Then delay
    }
  }
  
  
  private val next = system.actorOf(Props[NextValueActor], "NextValueActor")
  private val manager =  system.actorOf(Props[ValueManagerActor], "ValueManagerActor")
  
}
