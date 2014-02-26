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

  case class NextValue(value: NUM)
  case class PartialResult(value:NUM, isPrime:Boolean)
  
  
  object CheckerActor {
    def props() = Props(new CheckerActor) 
  }
  
  class CheckerActor extends Actor {
    def receive = {
      case NextValue(value) =>
        sender ! PartialResult(value, isPrime(value))
    }
  }
  
  /* ValuesManagerActor is the primes computation coordinators
   * it manage checked values order and affect the right position to
   * primes and not primes values.
   */
  class ValuesManagerActor(
      forActor:ActorRef,
      precomputedCount:Int=1000,
      checkerWorkers:Int=Runtime.getRuntime.availableProcessors) extends Actor {
    val checkerRouter = context.actorOf(
        CheckerActor.props.withRouter(SmallestMailboxRouter(checkerWorkers)),
        "CheckerActorRouter")
    
    private var nextPrimeNth=primeNth
    private var nextNotPrimeNth=notPrimeNth
    private var currentValue=startFrom  // waiting the result for this value
    private var nextValue=startFrom     // next value to send to checker worker

    // buffered partial results, because we need ordering to compute primes & not primes position 
    private var waitBuffer=Map.empty[NUM, PartialResult]
    
    // checked values precomputed cache
    private var checkedValuesQueue = Queue.empty[CheckedValue[NUM]]

    
    private def processPartialResult(partial:PartialResult) {
      val digitCount = partial.value.toString.size
      currentValue+=one
      val nth = if (partial.isPrime) nextPrimeNth else nextNotPrimeNth
      if (partial.isPrime) nextPrimeNth+=one else nextNotPrimeNth+=one
      val newResult = CheckedValue[NUM](partial.value, partial.isPrime, digitCount, nth)
      forActor ! newResult
      //checkedValuesQueue.enqueue(newResult)
    }
    
    private def flush2order() {
      while(waitBuffer.size>0 && waitBuffer.contains(currentValue)) {
        val pr = waitBuffer(currentValue)
        processPartialResult(pr)
        waitBuffer = waitBuffer - pr.value
      }
    }
    
    private def computeNext() {
      checkerRouter ! NextValue(nextValue)
      nextValue+=one
    }
    
    for { _ <- 1 to precomputedCount } computeNext()
    
    def receive = {
      case pr:PartialResult if pr.value == currentValue =>
        processPartialResult(pr)
        flush2order()
        computeNext()
        
      case pr:PartialResult => // Then delay
        waitBuffer+=pr.value -> pr
        computeNext()
        
    }
  }
  
  class PrinterActor extends Actor{
    var counter:Long=0l
    def receive = {
      case chk:CheckedValue[NUM] => 
        import chk._
        if (chk.isPrime) { 
	  counter +=1
	  if (counter % 10000L == 0L) println(s"$value is the $nth prime number")  // decrease println usage to avoid mailbox congestion
	}
        //else println(s"$value is the $nth NOT prime number")
    }
  }
  

  
  private val printer = actor("ValuesPrinter") {
    new PrinterActor
  }
  
  private val manager =  actor("ValuesManagerActor") {
    new ValuesManagerActor(printer)
  }

}
