package fr.janalyse.primes

import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.awt.Color

import annotation.tailrec

class PrimesDefinitions[NUM](implicit numops: Integral[NUM]) {

  import numops._

  protected val two = one + one
  protected val three = two + one
  protected val four = two + two
  protected val six = three + three

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

    @tailrec
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

}
