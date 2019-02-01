package fr.janalyse.primes

import java.lang.management.{ManagementFactory, OperatingSystemMXBean}

import scala.concurrent._
import scala.concurrent.duration._
import org.scalatest.FunSuite


trait PrimesTestCommons  extends FunSuite {
  val cpuCount: Int = java.lang.Runtime.getRuntime.availableProcessors
  def now: Long = System.currentTimeMillis
  val os: OperatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean
  def lastMinuteCpuAverage(): Int = (os.getSystemLoadAverage * 100).toInt

  def howLongFor[U, T](param: U)(proc: U => T)(infoOnResult: T => String): T = {
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