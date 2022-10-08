/*
 * Copyright 2013-2022 David Crosson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.janalyse.primes

import java.lang.management.{ManagementFactory, OperatingSystemMXBean}

import scala.concurrent._
import scala.concurrent.duration._
import org.scalatest.funsuite._
import org.scalatest.matchers._

trait PrimesTestCommons extends AnyFunSuite with should.Matchers {
  val cpuCount: Int               = java.lang.Runtime.getRuntime.availableProcessors
  def now: Long                   = System.currentTimeMillis
  val os: OperatingSystemMXBean   = ManagementFactory.getOperatingSystemMXBean
  def lastMinuteCpuAverage(): Int = (os.getSystemLoadAverage * 100).toInt

  def howlongfor[U, T](param: U)(proc: U => T)(infoOnResult: T => String): T = {
    now match {
      case start =>
        val result = proc(param)
        // val cpu = lastMinuteCpuAverage() // cpu=${cpu}%
        info(s"duration for $param : ${now - start}ms ${infoOnResult(result)}")
        result
    }
  }

  val perfTestSeries = List(10000, 25000, 50000)

}
