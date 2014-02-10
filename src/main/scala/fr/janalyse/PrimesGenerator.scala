/*
 * Copyright 2014 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */

package fr.janalyse.primes

import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.awt.Color

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

  /*
   * This is a generic method, that will have improved performance if
   * we can provide it with already computed primes stream
   * 
   * returns
   *   None => Not enough available primes to compute the factorization
   *   List() => value is a prime number
   *   List(...) => the factorization
   */
  def factorize(value: NUM, primesGetter: => Iterator[NUM]): Option[List[NUM]] = {
    val primes2test = primesGetter
    @tailrec
    def factit(current: NUM, prime: NUM, acc: List[NUM]): Option[List[NUM]] = {
      if (!primes2test.hasNext) None
      else if (current <= one || prime == value) Some(acc)
      else if (current % prime == zero) factit(current / prime, prime, prime :: acc)
      else factit(current, primes2test.next, acc)
    }
    val prime4start = primes2test.next
    factit(value, prime4start, Nil)
  }

  def isMersennePrimeExponent(v: NUM, primeTest: NUM => Boolean = isPrime): Boolean =
    primeTest(v) && primeTest(pow(two, v) - one)

  def isSexyPrime(v: NUM, primeTest: NUM => Boolean = isPrime): Boolean =
    primeTest(v) && primeTest(v + six)

  def isTwinPrime(v: NUM, primeTest: NUM => Boolean = isPrime): Boolean =
    primeTest(v) && primeTest(v + two)

  def isIsolatedPrime(v: NUM, primeTest: NUM => Boolean = isPrime): Boolean =
    !primeTest(v - two) && primeTest(v) && !primeTest(v + two)

  def ulamSpiral(size: Int, values: => Iterator[CheckedValue[NUM]]): BufferedImage = {
    val width = size
    val height = size
    val xc = width / 2
    val yc = height / 2
    val bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val gr = bi.createGraphics()
    gr.setBackground(Color.BLACK)
    gr.clearRect(0, 0, width, height)
    gr.setColor(Color.WHITE)

    def draw(x: Int, y: Int, ints: => Iterator[CheckedValue[NUM]]) {
      if (ints.hasNext) {
        val val2test = ints.next
        if (val2test.isPrime) gr.drawRect(x, y, 0, 0)
      }
    }

    @annotation.tailrec
    def drawit(x: Int, y: Int, sz: Int, remain: Int, ints: Iterator[CheckedValue[NUM]]) {
      draw(x, y, ints)
      for { i <- 1 to sz } draw(x, y + i, ints) // DOWN
      for { i <- 1 to sz } draw(x - i, y + sz, ints) // LEFT
      for { i <- 1 to sz + 1 } draw(x - sz, y + sz - i, ints) // UP
      for { i <- 1 to sz } draw(x - sz + i, y - 1, ints) // RIGHT
      if (remain > 0 && ints.hasNext) drawit(x + 1, y - 1, sz + 2, remain - 2 * sz - 2 * (sz - 1), ints)
    }

    drawit(xc, yc, 1, width * height, values)
    gr.setColor(Color.RED)
    gr.drawRect(xc, yc, 0, 0)
    bi
  }

  // ------------------------ STREAMS ------------------------

  def integers = {
    def next(cur: NUM): Stream[NUM] = cur #:: next(cur + one)
    next(one)
  }

  def checkedValues(
    cur: CheckedValue[NUM],
    primeNth: NUM,
    notPrimeNth: NUM): Stream[CheckedValue[NUM]] =
    cur #:: {
      val nextvalue = cur.value + one
      val isPrimeResult = isPrime(nextvalue)
      val nth = if (isPrimeResult) primeNth + one else notPrimeNth + one
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
