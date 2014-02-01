/*
 * Copyright 2014 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */

package fr.janalyse.primes

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

  // This sqrt method on integers comes from
  // https://issues.scala-lang.org/browse/SI-3739
  // (http://www.codecodex.com/wiki/Calculate_an_integer_square_root)
  //
  private def sqrt(number: NUM) = {
    //def next(n: PInteger, i: PInteger): PInteger = (n + i / n) >> 1
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

  def sexyPrimeStream =
    candidates
      .filter(isSexyPrime(_))

  def twinPrimeStream =
    candidates
      .filter(isTwinPrime(_))

  def isolatedPrimeStream =
    candidates
      .filter(isIsolatedPrime(_))

}
