/*
 * Copyright 2013-2021 David Crosson
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

class IsPrimesTest extends PrimesTestCommons {

  test("isPrimePara tests") {
    val gen = new PrimesGenerator[Int]
    import gen._
    import scala.concurrent.ExecutionContext.Implicits.global
    val monos = integers.filter(isPrime).take(100).toList
    val paras = integers.filter(isPrimePara(_)).take(100).toList
    paras should equal(monos)
  }

}
