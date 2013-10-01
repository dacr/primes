/*
 * Copyright 2012 David Crosson
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

package primes

import Primes._

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PrimesTest extends FunSuite with ShouldMatchers {

  def howlongfor[T](param:Int, proc : Int =>T) : T = {
    def now = System.currentTimeMillis
    now match {
      case start =>
        val result = proc(param)
        info(s"duration for $param : ${now-start}ms")
        result
    }
  }
  
  test("Simple tests") {
    primeStream.take(3) should equal(List(2,3,5))
    primeStream.drop(3).head should equal(7)
    primeStream.drop(999).head should equal(7919)
  }
  
  val perfTestSeries=List(25000, 50000, 75000, 100000)
  
  test("Performance classic tests") {
    for (sz <- perfTestSeries) 
       howlongfor(sz, primeStream.drop(_).head)
  }
  
  test("Performance parallel tests") {
    for (sz <- perfTestSeries) 
      howlongfor(sz, primeStreamPar.drop(_).head)
  }
}
