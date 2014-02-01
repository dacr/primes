/*
 * Copyright 2014 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */

package fr.janalyse.primes

case class CheckedValue[NUM](
  value: NUM,
  isPrime: Boolean,
  nth: NUM)(implicit numops: Integral[NUM])

object CheckedValue {
  def first[NUM](implicit numops: Integral[NUM]): CheckedValue[NUM] = {
    import numops._
    CheckedValue(one + one, true, one)
  }
}

class PrimesGenerator[NUM](implicit numops: Integral[NUM]) {

  import annotation.tailrec
  import numops._

  private val two = one + one
  private val three = one + one + one
  private val four = two + two
  private val six = three + three

  private def pow(n: NUM, p: NUM): NUM = {
    @tailrec
    def powit(cur: NUM, r: NUM): NUM = {
      if (r == 1) cur else powit(cur * n, r - one)
    }
    if (p < zero) sys.error("Not supported")
    if (p == 0) one else powit(n, p)
  }

  private def sqrt(number: NUM) = { //https://issues.scala-lang.org/browse/SI-3739
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

  def isMersennePrimeExponent(v: NUM): Boolean =
    isPrime(v) && isPrime(pow(two, v) - one)

  def isSexyPrime(v: NUM): Boolean =
    isPrime(v) && isPrime(v + six)

  def isTwinPrime(v: NUM): Boolean =
    isPrime(v) && isPrime(v + two)

  def isIsolatedPrime(v: NUM): Boolean =
    !isPrime(v - two) && isPrime(v) && !isPrime(v + two)

  // ------------------------ STREAMS ------------------------

  def integers = {
    def next(cur: NUM): Stream[NUM] = cur #:: next(cur + one)
    next(one)
  }

  def checkedValues = {
    def next(cur: CheckedValue[NUM], nextPrimeNth: NUM, nextNotPrimeNth: NUM): Stream[CheckedValue[NUM]] = cur #:: {
      val nextvalue = cur.value + one
      val isPrimeResult = isPrime(nextvalue)
      val nth = if (isPrimeResult) nextPrimeNth else nextNotPrimeNth
      next(
        CheckedValue[NUM](nextvalue, isPrimeResult, nth),
        if (isPrimeResult) nextPrimeNth + one else nextPrimeNth,
        if (isPrimeResult) nextNotPrimeNth else nextNotPrimeNth + one)
    }
    next(CheckedValue.first[NUM], two, one)
  }

  def candidates = integers.tail

  def primes =
    candidates
      .filter(isPrime(_))

  def primesPar =
    candidates
      .iterator //  workaround for Memory impact of the .par on just stream is too huge...
      .grouped(1000)
      .map(_.par)
      .flatMap(_.filter(isPrime(_)))
      .toStream

  def mersennePrimes =
    candidates
      .map(pow(two, _))
      .filter(isPrime(_))

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
