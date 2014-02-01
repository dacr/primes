/*
 * Copyright 2013 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */

package fr.janalyse.primes

class PrimesGenerator[PInteger](implicit numops: Integral[PInteger]) {

  import annotation.tailrec
  import numops._

  val two = one + one
  val three = one + one + one
  val four = two + two
  val six = three + three

  def pow(n: PInteger, p: PInteger): PInteger = {
    @tailrec
    def powit(cur: PInteger, r: PInteger): PInteger = {
      if (r == 1) cur else powit(cur * n, r - one)
    }
    if (p < zero) sys.error("Not supported")
    if (p == 0) one else powit(n, p)
  }

  // This sqrt method on integers comes from
  // https://issues.scala-lang.org/browse/SI-3739
  // (http://www.codecodex.com/wiki/Calculate_an_integer_square_root)
  //
  def sqrt(number: PInteger) = {
    //def next(n: PInteger, i: PInteger): PInteger = (n + i / n) >> 1
    def next(n: PInteger, i: PInteger): PInteger = (n + i / n) / two

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

  def isPrime(v: PInteger): Boolean = {
    val upTo = sqrt(v)
    @tailrec
    def checkUpTo(from: PInteger): Boolean = {
      if (v % from == 0) false
      else if (from == upTo) true else checkUpTo(from + one)
    }
    (v <= three) || checkUpTo(two)
  }

  def isMersennePrimeExponent(v: PInteger): Boolean =
    isPrime(v) && isPrime(pow(two, v) - one)

  def isSexyPrime(v: PInteger): Boolean =
    isPrime(v) && isPrime(v + six)

  def isTwinPrime(v: PInteger): Boolean =
    isPrime(v) && isPrime(v + two)

  def isIsolatedPrime(v: PInteger): Boolean =
    !isPrime(v - two) && isPrime(v) && !isPrime(v + two)

  // ------------------------ STREAMS ------------------------

  def integerStream = {
    def next(cur: PInteger): Stream[PInteger] = cur #:: next(cur + one)
    next(one)
  }

  def candidatesStream = integerStream.tail

  def primeStream =
    candidatesStream
      .filter(isPrime(_))

  def primeStreamPar =
    candidatesStream
      .iterator //  workaround for Memory impact of the .par on just stream is too huge...
      .grouped(1000)
      .map(_.par)
      .flatMap(_.filter(isPrime(_)))
      .toStream

  def mersennePrimeStream =
    candidatesStream
      .map(pow(two, _))
      .filter(isPrime(_))

  def sexyPrimeStream =
    candidatesStream
      .filter(isSexyPrime(_))

  def twinPrimeStream =
    candidatesStream
      .filter(isTwinPrime(_))

  def isolatedPrimeStream =
    candidatesStream
      .filter(isIsolatedPrime(_))

}
