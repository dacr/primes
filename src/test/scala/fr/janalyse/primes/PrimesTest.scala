/*
 * Copyright 2013-2020 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */

package fr.janalyse.primes

class PrimesTest extends PrimesTestCommons {

  test("Simple tests") {
    val pgen = new PrimesGenerator[Long]
    import pgen._
    primes.take(3) should contain theSameElementsInOrderAs List(2, 3, 5)
    primes.slice(3, 6) should contain theSameElementsInOrderAs List(7, 11, 13)
    primes.drop(999).head should equal(7919)
  }

  test("checked values tests") {
    val pgen = new PrimesGenerator[Long]
    import pgen._
    val values = checkedValues.take(1000)
    val prime100 = values.filter(_.isPrime).drop(99)
    val notPrime100 = values.filter(!_.isPrime).drop(99)
    prime100.head.value should equal(541)
    notPrime100.head.value should equal(133)

    values.filter(_.isPrime).take(5).map(_.nth) should equal(List(1, 2, 3, 4, 5))
    values.filterNot(_.isPrime).take(5).map(_.nth) should equal(List(1, 2, 3, 4, 5))

    values.filter(_.isPrime).take(5).map(_.value) should equal(List(2, 3, 5, 7, 11))
    values.filterNot(_.isPrime).take(5).map(_.value) should equal(List(4, 6, 8, 9, 10))
  }

  test("Resume checked value tests") {
    val pgen = new PrimesGenerator[Long]

    val starts = pgen.checkedValues.takeWhile(_.value <= 10)
    val lastPrime = starts.filter(_.isPrime).lastOption
    val lastNotPrime = starts.filterNot(_.isPrime).lastOption

    val resumed = pgen.checkedValues(lastPrime, lastNotPrime).takeWhile(_.value <= 20)
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
