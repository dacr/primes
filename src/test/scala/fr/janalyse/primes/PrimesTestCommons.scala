package fr.janalyse.primes

import java.lang.management.ManagementFactory
import scala.concurrent._
import scala.concurrent.duration._
import org.scalatest.funsuite._
import org.scalatest.matchers._


trait PrimesTestCommons  extends AnyFunSuite with should.Matchers {
  val cpuCount = java.lang.Runtime.getRuntime.availableProcessors
  def now = System.currentTimeMillis
  val os = ManagementFactory.getOperatingSystemMXBean()
  def lastMinuteCpuAverage() = (os.getSystemLoadAverage() * 100).toInt

  def howlongfor[U, T](param: U)(proc: U => T)(infoOnResult: T => String): T = {
    now match {
      case start =>
        val result = proc(param)
        //val cpu = lastMinuteCpuAverage() // cpu=${cpu}%
        info(s"duration for $param : ${now - start}ms ${infoOnResult(result)}")
        result
    }
  }
  val perfTestSeries = List(10000, 25000, 50000)

}