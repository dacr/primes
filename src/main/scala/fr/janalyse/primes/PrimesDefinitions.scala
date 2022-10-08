/*
 * Copyright 2013-2022 David Crosson
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

import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.awt.Color
import annotation.tailrec
import java.awt.Graphics2D
import java.io.File
import javax.imageio.ImageIO
import scala.concurrent._
import scala.concurrent.duration._
import scala.util._

class PrimesDefinitions[NUM](implicit numops: Integral[NUM]) {

  import numops._

  protected final val two   = one + one
  protected final val three = two + one
  protected final val four  = two + two
  protected final val five  = four + one
  protected final val six   = three + three
  protected final val seven = four + three
  protected final val eight = four + four
  protected final val nine  = five + four
  protected final val ten   = five + five

  @tailrec
  private final def powit(n: NUM, cur: NUM, r: NUM): NUM = {
    if (r == one) cur else powit(n, cur * n, r - one)
  }

  final def pow(n: NUM, p: NUM): NUM = {
    if (p < zero) sys.error("Not supported")
    if (p == zero) one else powit(n, n, p)
  }

  private final def sqrtNext(n: NUM, i: NUM): NUM = (n + i / n) / two

  final def sqrt(number: NUM): NUM = { // https://issues.scala-lang.org/browse/SI-3739
    var n  = one
    var n1 = sqrtNext(n, number)

    while ((n1 - n).abs > one) {
      n = n1
      n1 = sqrtNext(n, number)
    }

    while (n1 * n1 > number) {
      n1 -= one
    }

    n1
  }

  final def isPrime(v: NUM): Boolean = {
    val upTo                          = sqrt(v)
    @tailrec
    def checkUpTo(from: NUM): Boolean = {
      if (v % from == zero) false
      else if (from == upTo) true
      else checkUpTo(from + one)
    }
    (v <= three) || checkUpTo(two)
  }

  val coresCount: NUM = fromInt(java.lang.Runtime.getRuntime.availableProcessors)

  final def isPrimePara(v: NUM)(implicit ec: ExecutionContext): Boolean = {
    val result      = Promise[Boolean]()
    val testUpTo    = sqrt(v)
    val segmentSize = (testUpTo - two) / coresCount

    @tailrec
    def checkUpToPara(from: NUM, to: NUM): Unit = {
      if (v % from == zero) result.complete(Success(false))
      else if (from < to && !result.isCompleted) checkUpToPara(from + one, to)
    }

    @tailrec
    def makeWorkers(cur: NUM, wks: List[Future[Unit]] = List.empty): List[Future[Unit]] = {
      if (cur > testUpTo) wks
      else {
        val next = cur + segmentSize
        makeWorkers(next + one, Future { checkUpToPara(cur, next) } :: wks)
      }
    }

    Await.ready(Future.sequence(makeWorkers(two)), Duration.Inf)
    !result.isCompleted
  }

  /** */
  private class EratosthenesCell(val value: NUM) {
    private var marked    = false
    def mark(): Unit      = { marked = true }
    def isMarked: Boolean = marked
    def isPrime: Boolean  = !marked // true for primes only once the sieve of eratosthenes is finished
    override def toString = s"ECell($value, $marked)"
  }

  /** */
  @tailrec
  private final def buildSieve(it: NumericReverseIterator[NUM], cur: List[EratosthenesCell] = Nil): List[EratosthenesCell] =
    if (it.hasNext) buildSieve(it, new EratosthenesCell(it.next()) :: cur) else cur

  @tailrec
  private final def eratMark(limit: NUM, multiples: LazyList[NUM], cur: List[EratosthenesCell]): Unit = {
    cur.headOption match {
      case None                                       =>
      case _ if multiples.head > limit                =>
      case Some(cell) if cell.value == multiples.head =>
        cell.mark()
        eratMark(limit, multiples.tail, cur.tail)
      case Some(_)                                    =>
        eratMark(limit, multiples, cur.tail)
    }
  }

  @tailrec
  private final def eratWorker(limit: NUM, upTo: NUM, cur: List[EratosthenesCell]): Unit = {
    if (cur.nonEmpty && cur.head.value <= upTo) {
      if (!cur.head.isMarked) {
        eratMark(limit, new NumericIterator[NUM](two).to(LazyList).map(_ * cur.head.value), cur.tail)
      }
      eratWorker(limit, upTo, cur.tail)
    }
  }

  final def eratosthenesSieve(limit: NUM): List[CheckedValue[NUM]] = {
    val upTo                          = sqrt(limit)
    val sieve                         = buildSieve(new NumericReverseIterator[NUM](limit, _ > two))
    eratWorker(limit, upTo, sieve)
    var nthIsPrime                    = zero
    var nthIsNotPrime                 = zero
    def computeNth(isp: Boolean): NUM = {
      if (isp) { nthIsPrime += one; nthIsPrime }
      else { nthIsNotPrime += one; nthIsNotPrime }
    }
    sieve.map(ec => CheckedValue(ec.value, ec.isPrime, computeNth(ec.isPrime)))
  }

  /** */
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
      else factit(current, primes2test.next(), acc)
    }

    val prime4start = primes2test.next()
    factit(value, prime4start, Nil)
  }

  //  def sacksSpiral(size:Int, values: => Iterator[CheckedValue[NUM]], len:Int): BufferedImage = {

  //  }

  private def spiral(size: Int, draw: (Graphics2D, Int, Int, Int) => Unit): BufferedImage = {
    val width  = size
    val height = size
    val xc     = width / 2
    val yc     = height / 2
    val bi     = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val gr     = bi.createGraphics()

    gr.setBackground(Color.BLACK)
    gr.clearRect(0, 0, width, height)
    gr.setColor(Color.WHITE)

    @tailrec
    def spiral(x: Int, y: Int, sz: Int, remain: Int, len: Int): Unit = {
      draw(gr, x, y, len)
      for { i <- 1 to sz } draw(gr, x, y + i, len + 1)               // DOWN
      for { i <- 1 to sz } draw(gr, x - i, y + sz, len + 2)          // LEFT
      for { i <- 1 to sz + 1 } draw(gr, x - sz, y + sz - i, len + 3) // UP
      for { i <- 1 to sz } draw(gr, x - sz + i, y - 1, len + 4)      // RIGHT
      if (remain > 0) spiral(x + 1, y - 1, sz + 2, remain - 2 * sz - 2 * (sz - 1), len + 5)
    }

    spiral(xc, yc, 1, width * height, 0)
    gr.setColor(Color.RED)
    gr.drawRect(xc, yc, 0, 0)
    bi
  }

  def ulamSpiral(size: Int, values: Iterator[CheckedValue[NUM]]): BufferedImage = {
    def draw(gr: Graphics2D, x: Int, y: Int, len: Int): Unit = {
      if (values.hasNext && values.next().isPrime) gr.drawRect(x, y, 0, 0)
    }
    spiral(size, draw)
  }

  def ulamSpiralToPngFile(size: Int, values: Iterator[CheckedValue[NUM]], filename: String) = {
    ImageIO.write(ulamSpiral(size, values), "PNG", new File(filename))

  }

  def sacksInspiredSpiral(size: Int, interval: Int, values: Iterator[CheckedValue[NUM]]): BufferedImage = {
    def draw(gr: Graphics2D, x: Int, y: Int, len: Int): Unit = {
      if (values.hasNext && (len % interval == 0) && values.next().isPrime) gr.drawRect(x, y, 0, 0)
    }
    spiral(size, draw)
  }

  def sacksInspiredSpiralToPngFile(size: Int, interval: Int, values: Iterator[CheckedValue[NUM]], filename: String) = {
    ImageIO.write(sacksInspiredSpiral(size, interval, values), "PNG", new File(filename))
  }

}
