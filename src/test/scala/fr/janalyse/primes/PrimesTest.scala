/*
 * Copyright 2013 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */

package fr.janalyse.primes

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import java.lang.management.ManagementFactory
import scala.concurrent._
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class PrimesTest extends FunSuite with ShouldMatchers {

  val cpuCount = java.lang.Runtime.getRuntime.availableProcessors
  def now = System.currentTimeMillis
  val os = ManagementFactory.getOperatingSystemMXBean()
  def lastMinuteCpuAverage() = (os.getSystemLoadAverage() * 100).toInt

  def howlongfor[T](param: Int, proc: Int => T)(infoOnResult: T => String): T = {
    now match {
      case start =>
        val result = proc(param)
        //val cpu = lastMinuteCpuAverage() // cpu=${cpu}%
        info(s"duration for $param : ${now - start}ms ${infoOnResult(result)}")
        result
    }
  }

  test("Simple tests") {
    val pgen = new PrimesGenerator[Long]
    import pgen._
    primes.take(3) should equal(List(2, 3, 5))
    primes.drop(3).head should equal(7)
    primes.drop(999).head should equal(7919)
  }

  test("checked values tests") {
    val pgen = new PrimesGenerator[Long]
    import pgen._
    val values = checkedValues.take(1000)
    val prime100 = values.filter(_.isPrime).drop(99)
    val notPrime100 = values.filter(!_.isPrime).drop(99)
    prime100.head.value should equal(541)
    notPrime100.head.value should equal(133)

    values.filter(_.isPrime).take(5).map(_.nth) should equal(List(1, 2, 3, 4, 5))
    values.filterNot(_.isPrime).take(5).map(_.nth) should equal(List(1, 2, 3, 4, 5))

    values.filter(_.isPrime).take(5).map(_.value) should equal(List(2, 3, 5, 7, 11))
    values.filterNot(_.isPrime).take(5).map(_.value) should equal(List(4, 6, 8, 9, 10))
  }

  test("Resume checked value tests") {
    val pgen = new PrimesGenerator[Long]

    val starts = pgen.checkedValues.takeWhile(_.value <= 10)
    val lastPrime = starts.filter(_.isPrime).lastOption
    val lastNotPrime = starts.filterNot(_.isPrime).lastOption

    val resumed = pgen.checkedValues(lastPrime, lastNotPrime).takeWhile(_.value <= 20)
    resumed.filter(_.isPrime).map(_.value) should equal(List(11, 13, 17, 19))
    resumed.filter(_.isPrime).map(_.nth) should equal(List(5, 6, 7, 8))
    resumed.filterNot(_.isPrime).map(_.value) should equal(List(12, 14, 15, 16, 18, 20))
    resumed.filterNot(_.isPrime).map(_.nth) should equal(List(6, 7, 8, 9, 10, 11))
  }

  test("factorize tests") {
    val pgen = new PrimesGenerator[BigInt]
    pgen.factorize(9, pgen.primes.toIterator) should equal(Some(List(3, 3)))
    pgen.factorize(1236, pgen.primes.toIterator) should equal(Some(List(103, 3, 2, 2)))
    pgen.factorize(1237, pgen.primes.toIterator) should equal(Some(Nil))
    pgen.factorize(923412, pgen.primes.take(10).toIterator) should equal(None)
  }

  val perfTestSeries = List(10000, 25000, 50000)

  test("Performance classic tests - Long") {
    val pgen = new PrimesGenerator[Long]
    import pgen._
    for (sz <- perfTestSeries)
      howlongfor(sz, primes.drop(_).head)("lastPrime=" + _.toString)
  }

  test("Performance parallel tests - Long") {
    val pgen = new PrimesGenerator[Long]
    import pgen._
    for (sz <- perfTestSeries)
      howlongfor(sz, primesPar.drop(_).head)("lastPrime=" + _.toString)
  }

  test("Performance classic tests - BigInt") {
    val pgen = new PrimesGenerator[BigInt]
    import pgen._
    for (sz <- perfTestSeries)
      howlongfor(sz, primes.drop(_).head)("lastPrime=" + _.toString)
  }

  test("Performance parallel tests - BigInt") {
    val pgen = new PrimesGenerator[BigInt]
    import pgen._
    for (sz <- perfTestSeries)
      howlongfor(sz, primesPar.drop(_).head)("lastPrime=" + _.toString)
  }

  
  
  
  def genericActorsTest[CL <% Shutdownable]
       (genfact: (CheckedValue[BigInt]=>Unit) => CL) = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val started = now
    val results = Promise[List[String]]()
    val handler = {
      var perfTestSeriesLimits = perfTestSeries.map(n => BigInt(n))
      var infos = List.empty[String]
      (nv: CheckedValue[BigInt]) =>
        {
          perfTestSeriesLimits.headOption match {
            case Some(limit) =>
              if (nv.isPrime) {
                if (nv.nth >= limit) {
                  infos ::= s"duration for $limit : ${now - started}ms lastPrime=${nv.value}"
                  perfTestSeriesLimits = perfTestSeriesLimits.tail
                }
              }
            case None =>
              // Stop the test
              if (!results.isCompleted) results.success(infos.reverse)
          }
        }
    }
    val gen = genfact(handler(_))

//    for {
//      msgs <- results.future
//      msg <- msgs
//    } info(msg)
//    
//    Await.result(results.future, 60.seconds)
    
    val r = Await.result(results.future, 60.seconds)
    for {msg <- r} info(msg) // Executed in the current thread is better for scalatest
    
    gen.shutdown
  }
   
  test("akka actors based computation test - BigInt") {
    genericActorsTest(handler => new ActorsPrimesGenerator[BigInt](handler))
  }
  
  test("akka actors streams based computation test - BigInt") {
    genericActorsTest(handler => new StreamBasedPrimesGenerator[BigInt](handler))
    info("Remember that the order is not preserved...")
    info("the behavior is a little kind different than with PrimesGenerator")
  }
 
}
