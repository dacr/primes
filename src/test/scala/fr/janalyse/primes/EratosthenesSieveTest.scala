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

import org.scalatest.OptionValues._

class EratosthenesSieveTest extends PrimesTestCommons {

  test("basic tests") {
    val dp    = new PrimesDefinitions[Int]()
    val found = dp.eratosthenesSieve(10)
    found.find(_.value == 9).value.isPrime should be(false)
    found.find(_.value == 7).value.isPrime should be(true)
  }

  test("correctness test") {
    val howmany = 1000

    val dp = new PrimesDefinitions[Int]()
    val gp = new PrimesGenerator[Int]()

    val found = dp.eratosthenesSieve(howmany)

    found should equal(gp.checkedValues.takeWhile { _.value <= howmany }.toList)
  }

//  test("EratosthenesSieve performance test") {
//    val howmany = 500000
//    val dp = new PrimesDefinitions[BigInt]()
//    val gp = new PrimesGenerator[BigInt]()
//
//    howlongfor(howmany)(x=> gp.checkedValues.takeWhile(_.value <= x).toList)(x => "Classic last="+x.last)
//    howlongfor(howmany)(dp.eratosthenesSieve(_))(x => "Eratosthenes last="+x.last)
//    info("(Mono-thread algorithms)")
//  }
}
