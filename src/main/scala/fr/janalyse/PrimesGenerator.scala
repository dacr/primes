/*
 * Copyright 2014 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */

package fr.janalyse.primes

case class CheckedValue[NUM](
  value: NUM,
  isPrime: Boolean,
  digitCount: Long,
  nth: NUM)(implicit numops: Integral[NUM])


object CheckedValue {
  def first[NUM](implicit numops: Integral[NUM]): CheckedValue[NUM] = {
    import numops._
    CheckedValue(one + one, true, 1, one)
  }
}


class PrimesGenerator[NUM](implicit numops: Integral[NUM]) {

  import annotation.tailrec
  import numops._

  private val two = one + one
  private val three = two + one
  private val four = two + two
  private val six = three + three

  def pow(n: NUM, p: NUM): NUM = {
    @tailrec
    def powit(cur: NUM, r: NUM): NUM = {
      if (r == one) cur else powit(cur * n, r - one)
    }
    if (p < zero) sys.error("Not supported")
    if (p == zero) one else powit(n, p)
  }

  def sqrt(number: NUM) = { //https://issues.scala-lang.org/browse/SI-3739
    def next(n: NUM, i: NUM): NUM = (n + i / n) / two

    var n = one
    var n1 = next(n, number)

    while ((n1 - n).abs > one) {
      n = n1
      n1 = next(n, number)
    }

    while (n1 * n1 > number) {
      n1 -= one
    }

    n1
  }

  def isPrime(v: NUM): Boolean = {
    val upTo = sqrt(v)
    @tailrec
    def checkUpTo(from: NUM): Boolean = {
      if (v % from == 0) false
      else if (from == upTo) true else checkUpTo(from + one)
    }
    (v <= three) || checkUpTo(two)
  }

  def isMersennePrimeExponent(v: NUM, primeTest: NUM => Boolean = isPrime): Boolean =
    primeTest(v) && primeTest(pow(two, v) - one)

  def isSexyPrime(v: NUM, primeTest: NUM => Boolean = isPrime): Boolean =
    primeTest(v) && primeTest(v + six)

  def isTwinPrime(v: NUM, primeTest: NUM => Boolean = isPrime): Boolean =
    primeTest(v) && primeTest(v + two)

  def isIsolatedPrime(v: NUM, primeTest: NUM => Boolean = isPrime): Boolean =
    !primeTest(v - two) && primeTest(v) && !primeTest(v + two)

  // ------------------------ STREAMS ------------------------

  def integers = {
    def next(cur: NUM): Stream[NUM] = cur #:: next(cur + one)
    next(one)
  }

  private def checkedValues(
    cur: CheckedValue[NUM],
    primeNth: NUM,
    notPrimeNth: NUM): Stream[CheckedValue[NUM]] =
    cur #:: {
      val nextvalue = cur.value + one
      val isPrimeResult = isPrime(nextvalue)
      val nth = if (isPrimeResult) primeNth+one else notPrimeNth+one
      checkedValues(
        CheckedValue[NUM](nextvalue, isPrimeResult, nextvalue.toString.size, nth),
        if (isPrimeResult) nth else primeNth,
        if (isPrimeResult) notPrimeNth else nth)
    }

  def checkedValues: Stream[CheckedValue[NUM]] =
    checkedValues(CheckedValue.first[NUM], one, zero)

  def candidates = integers.tail

  def primes =
    candidates
      .filter(isPrime(_))

  def notPrimes =
    candidates
      .filterNot(isPrime(_))

  // distances between consecutive primes
  def distances =
    primes
      .sliding(2, 1)
      .map(slice => slice.tail.head - slice.head)
      .toStream

  def primesPar =
    candidates
      .iterator //  workaround for Memory impact of the .par on just stream is too huge...
      .grouped(1000)
      .map(_.par)
      .flatMap(_.filter(isPrime(_)))
      .toStream

  def mersennePrimes =
    candidates
      .filter(isMersennePrimeExponent(_))
      .map(pow(two, _) - one)

  def sexyPrimes =
    candidates
      .filter(isSexyPrime(_))

  def twinPrimes =
    candidates
      .filter(isTwinPrime(_))

  def isolatedPrimes =
    candidates
      .filter(isIsolatedPrime(_))

}
