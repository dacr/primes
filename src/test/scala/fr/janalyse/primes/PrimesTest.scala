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

class PrimesTest extends PrimesTestCommons {

  test("Integers iterator") {
    val pgen = new PrimesGenerator[Int]
    pgen.integers.take(5).toList should contain theSameElementsInOrderAs List(1,2,3,4,5)
    pgen.integersFrom(3).take(5).toList should contain theSameElementsInOrderAs List(3,4,5,6,7)
  }

  test("Candidates iterator") {
    val pgen = new PrimesGenerator[Int]
    pgen.candidates.take(5).toList should contain theSameElementsInOrderAs List(2,3,4,5,6)
    pgen.candidatesAfter(3).take(5).toList should contain theSameElementsInOrderAs List(4,5,6,7,8)
  }

  test("Simple tests with Int type") {
    val pgen = new PrimesGenerator[Int]
    import pgen._
    primes.take(3).toList should contain theSameElementsInOrderAs List(2, 3, 5)
    primes.slice(3, 6).toList should contain theSameElementsInOrderAs List(7, 11, 13)
    primes.drop(999).to(LazyList).head should equal(7919)
  }

  test("Simple tests with Long type") {
    val pgen = new PrimesGenerator[Long]
    import pgen._
    primes.take(3).toList should contain theSameElementsInOrderAs List(2, 3, 5)
    primes.slice(3, 6).toList should contain theSameElementsInOrderAs List(7, 11, 13)
    primes.drop(999).to(LazyList).head should equal(7919)
  }

  test("Simple tests with BigInt type") {
    val pgen = new PrimesGenerator[Long]
    import pgen._
    primes.take(3).toList should contain theSameElementsInOrderAs List(2, 3, 5)
    primes.slice(3, 6).toList should contain theSameElementsInOrderAs List(7, 11, 13)
    primes.drop(999).to(LazyList).head should equal(7919)
  }

  test("primes from a given start value") {
    val pgen = new PrimesGenerator[Long]
    import pgen._
    primesAfter(13).take(3).toList should contain theSameElementsInOrderAs List(17, 19, 23)
  }

  test("checked values tests") {
    val pgen = new PrimesGenerator[Long]
    import pgen._
    val values = checkedValues.take(1000).toList

    values.filter(_.isPrime).take(5).map(_.nth) should equal(List(1, 2, 3, 4, 5))
    values.filterNot(_.isPrime).take(5).map(_.nth) should equal(List(1, 2, 3, 4, 5))

    values.filter(_.isPrime).take(5).map(_.value) should equal(List(2, 3, 5, 7, 11))
    values.filterNot(_.isPrime).take(5).map(_.value) should equal(List(4, 6, 8, 9, 10))

    val prime100 = values.filter(_.isPrime).drop(99)
    val notPrime100 = values.filter(!_.isPrime).drop(99)
    prime100.head.value should equal(541)
    notPrime100.head.value should equal(133)

  }

  test("Resume checked value tests") {
    val pgen = new PrimesGenerator[Long]

    val starts = pgen.checkedValues.takeWhile(_.value <= 10).toList
    val lastPrime = starts.filter(_.isPrime).lastOption
    val lastNotPrime = starts.filterNot(_.isPrime).lastOption

    val resumed = pgen.checkedValues(lastPrime, lastNotPrime).takeWhile(_.value <= 20).toList
    resumed.filter(_.isPrime).map(_.value) should equal(List(11, 13, 17, 19))
    resumed.filter(_.isPrime).map(_.nth) should equal(List(5, 6, 7, 8))
    resumed.filterNot(_.isPrime).map(_.value) should equal(List(12, 14, 15, 16, 18, 20))
    resumed.filterNot(_.isPrime).map(_.nth) should equal(List(6, 7, 8, 9, 10, 11))
  }

  test("factorize tests") {
    val pgen = new PrimesGenerator[BigInt]
    pgen.factorize(9, pgen.primes.iterator) should equal(Some(List(3, 3)))
    pgen.factorize(1236, pgen.primes.iterator) should equal(Some(List(103, 3, 2, 2)))
    pgen.factorize(1237, pgen.primes.iterator) should equal(Some(Nil))
    pgen.factorize(923412, pgen.primes.take(10).iterator) should equal(None)
  }

}
