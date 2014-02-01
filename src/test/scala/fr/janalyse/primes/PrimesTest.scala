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

@RunWith(classOf[JUnitRunner])
class PrimesTest extends FunSuite with ShouldMatchers {

  val cpuCount = java.lang.Runtime.getRuntime.availableProcessors
  val os = ManagementFactory.getOperatingSystemMXBean()
  def lastMinuteCpuAverage() = (os.getSystemLoadAverage() * 100).toInt

  def howlongfor[T](param: Int, proc: Int => T)(infoOnResult: T => String): T = {
    def now = System.currentTimeMillis
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
    val pgen=new PrimesGenerator[Long]
    import pgen._
    val values=checkedValues.take(1000)
    val prime100=values.filter(_.isPrime).drop(99)
    val notPrime100=values.filter(!_.isPrime).drop(99)
    prime100.head.value should equal(541)
    notPrime100.head.value should equal(133)
  }
  
  val perfTestSeries = List(25000, 50000, 75000)

  test("Performance classic tests - Long") {
    val pgen = new PrimesGenerator[Long]
    import pgen._
    for (sz <- perfTestSeries)
      howlongfor(sz, primes.drop(_).head)("lastPrime=" + _.toString)
  }

  test("Performance classic tests - BigInt") {
    val pgen = new PrimesGenerator[BigInt]
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
  
  test("Performance parallel tests - BigInt") {
    val pgen = new PrimesGenerator[BigInt]
    import pgen._
    for (sz <- perfTestSeries)
      howlongfor(sz, primesPar.drop(_).head)("lastPrime=" + _.toString)
  }

}
