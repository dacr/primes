/*
 * Copyright 2014 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */

package fr.janalyse.primes


class PrimesGenerator[NUM](implicit numops: Integral[NUM]) extends PrimesDefinitions[NUM] {

  import annotation.tailrec
  import numops._

  def integers: Iterator[NUM] = new NumericIterator[NUM](one)

  protected def checkedValues(
    initialCheckedValue: CheckedValue[NUM],
    initialPrimeNth: NUM,
    initialNotPrimeNth: NUM): Iterator[CheckedValue[NUM]] = {
    new Iterator[CheckedValue[NUM]] {
      var current: CheckedValue[NUM] = initialCheckedValue
      var primeNth: NUM = initialPrimeNth
      var notPrimeNth: NUM = initialNotPrimeNth

      override def hasNext: Boolean = true // TODO

      override def next(): CheckedValue[NUM] = {
        val previous = current
        val nextValue = current.value + one
        val isPrimeResult = isPrime(nextValue)
        val nth = if (isPrimeResult) {
          primeNth += one
          primeNth
        } else {
          notPrimeNth += one
          notPrimeNth
        }
        current = CheckedValue[NUM](
          nextValue,
          isPrimeResult,
          nextValue.toString.length,
          nth
        )
        previous
      }
    }
  }


  def checkedValues(
    foundLastPrime: Option[CheckedValue[NUM]],
    foundLastNotPrime: Option[CheckedValue[NUM]]): Iterator[CheckedValue[NUM]] = {
    val foundLast = for {
      flp <- foundLastPrime
      flnp <- foundLastNotPrime
    } yield if (flp.value > flnp.value) flp else flnp

    val primeNth = foundLastPrime.map(_.nth).getOrElse(one)
    val notPrimeNth = foundLastNotPrime.map(_.nth).getOrElse(zero)
    val resuming = foundLast.isDefined

    // And now return the resumed (or not) stream
    checkedValues(foundLast.getOrElse(CheckedValue.first), primeNth, notPrimeNth) match {
      case s if resuming => s.next(); s
      case s => s
    }
  }

  def checkedValues: Iterator[CheckedValue[NUM]] =
    checkedValues(CheckedValue.first[NUM], one, zero)

  def candidates: Iterator[NUM] = {
    val it = integers
    it.next()
    it
  }

  def primes: Iterator[NUM] =
    candidates
      .filter(isPrime)

  def notPrimes: Iterator[NUM] =
    candidates
      .filterNot(isPrime)

  // distances between consecutive primes
  def distances: Iterator[NUM] =
    primes
      .sliding(2, 1)
      .map(slice => slice.tail.head - slice.head)
      .to(Iterator)

  //  def primesPar =
  //    candidates
  //      .iterator //  workaround for Memory impact of the .par on just stream is too huge...
  //      .grouped(1000)
  //      .map(_.par)
  //      .flatMap(_.filter(isPrime(_)))
  //      .toIterator

  def mersennePrimes: Iterator[NUM] =
    candidates
      .filter(isMersennePrimeExponent(_))
      .map(pow(two, _) - one)

  def sexyPrimes: Iterator[NUM] =
    candidates
      .filter(isSexyPrime(_))

  def twinPrimes: Iterator[NUM] =
    candidates
      .filter(isTwinPrime(_))

  def isolatedPrimes: Iterator[NUM] =
    candidates
      .filter(isIsolatedPrime(_))

}
