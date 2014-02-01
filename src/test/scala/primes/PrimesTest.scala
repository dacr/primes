/*
 * Copyright 2013 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */


package primes

import Primes._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import java.lang.management.ManagementFactory

@RunWith(classOf[JUnitRunner])
class PrimesTest extends FunSuite with ShouldMatchers {

  val cpuCount = java.lang.Runtime.getRuntime.availableProcessors
  val os = ManagementFactory.getOperatingSystemMXBean()
  def lastMinuteCpuAverage()=(os.getSystemLoadAverage()*100).toInt
  

  def howlongfor[T](param:Int, proc : Int =>T)(infoOnResult: T => String) : T = {
    def now = System.currentTimeMillis
    now match {
      case start =>
        val result = proc(param)
        //val cpu = lastMinuteCpuAverage() // cpu=${cpu}%
        info(s"duration for $param : ${now-start}ms ${infoOnResult(result)}")
        result
    }
  }
  
  test("Simple tests") {
    primeStream.take(3) should equal(List(2,3,5))
    primeStream.drop(3).head should equal(7)
    primeStream.drop(999).head should equal(7919)
  }
  
  val perfTestSeries=List(25000, 50000, 75000, 100000, 125000)
  
  test("Performance classic tests") {
    for (sz <- perfTestSeries) 
       howlongfor(sz, primeStream.drop(_).head)("lastPrime="+_.toString)
  }
  
  test("Performance parallel tests") {
    for (sz <- perfTestSeries) 
      howlongfor(sz, primeStreamPar.drop(_).head)("lastPrime="+_.toString)
  }
}
