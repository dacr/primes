/*
 * Copyright 2014 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */

package fr.janalyse.primes


class PrimesGenerator[NUM](implicit numops: Integral[NUM]) extends PrimesDefinitions[NUM] {

  import annotation.tailrec
  import numops._


  // ------------------------ STREAMS ------------------------

  def integers: Stream[NUM] = {
    def next(cur: NUM): Stream[NUM] = cur #:: next(cur + one)
    next(one)
  }

  protected def checkedValues(
    cur: CheckedValue[NUM],
    primeNth: NUM,
    notPrimeNth: NUM): Stream[CheckedValue[NUM]] =
    cur #:: {
      val nextvalue = cur.value + one
      val isPrimeResult = isPrime(nextvalue)
      val nth = if (isPrimeResult) primeNth + one else notPrimeNth + one
      checkedValues(
        CheckedValue[NUM](nextvalue, isPrimeResult, nextvalue.toString.length, nth),
        if (isPrimeResult) nth else primeNth,
        if (isPrimeResult) notPrimeNth else nth)
    }

  def checkedValues(
      foundLastPrime: Option[CheckedValue[NUM]],
      foundLastNotPrime:Option[CheckedValue[NUM]]): Stream[CheckedValue[NUM]] = {
      val foundLast = for {
        flp <- foundLastPrime
        flnp <- foundLastNotPrime
      } yield if (flp.value > flnp.value) flp else flnp

      val primeNth = foundLastPrime.map(_.nth).getOrElse(one)
      val notPrimeNth = foundLastNotPrime.map(_.nth).getOrElse(zero)
      val resuming = foundLast.isDefined
      
      // And now return the resumed (or not) stream 
      checkedValues(foundLast.getOrElse(CheckedValue.first), primeNth, notPrimeNth) match {
        case s if resuming => s.tail
        case s => s
      }
  }
  
  def checkedValues: Stream[CheckedValue[NUM]] =
    checkedValues(CheckedValue.first[NUM], one, zero)

  def candidates: Stream[NUM] = integers.tail

  def primes: Stream[NUM] =
    candidates
      .filter(isPrime)

  def notPrimes: Stream[NUM] =
    candidates
      .filterNot(isPrime)

  // distances between consecutive primes
  def distances: Stream[NUM] =
    primes
      .sliding(2, 1)
      .map(slice => slice.tail.head - slice.head)
      .toStream

  def primesPar: Stream[NUM] =
    candidates
      .iterator //  workaround for Memory impact of the .par on just stream is too huge...
      .grouped(1000)
      .map(_.par)
      .flatMap(_.filter(isPrime))
      .toStream

  def mersennePrimes: Stream[NUM] =
    candidates
      .filter(isMersennePrimeExponent(_))
      .map(pow(two, _) - one)

  def sexyPrimes: Stream[NUM] =
    candidates
      .filter(isSexyPrime(_))

  def twinPrimes: Stream[NUM] =
    candidates
      .filter(isTwinPrime(_))

  def isolatedPrimes: Stream[NUM] =
    candidates
      .filter(isIsolatedPrime(_))

}
