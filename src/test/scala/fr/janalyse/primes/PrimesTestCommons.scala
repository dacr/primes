package fr.janalyse.primes

import java.lang.management.ManagementFactory
import scala.concurrent._
import scala.concurrent.duration._
import org.scalatest.FunSuite


trait PrimesTestCommons  extends FunSuite {
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
  
  def genericActorsTest[CL <% Shutdownable](genfact: (CheckedValue[BigInt] => Unit) => CL) = {
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
                if (nv.nth > limit) {
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

    val r = Await.result(results.future, 60.seconds)
    for { msg <- r } info(msg) // Executed in the current thread is better for scalatest to avoid formatting issues

    gen.shutdown
  }

}