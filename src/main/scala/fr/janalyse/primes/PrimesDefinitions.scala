package fr.janalyse.primes

import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.awt.Color
import annotation.tailrec
import java.awt.Graphics2D

class PrimesDefinitions[NUM](implicit numops: Integral[NUM]) {

  import numops._

  protected val two = one + one
  protected val three = two + one
  protected val four = two + two
  protected val five = four + one
  protected val six = three + three
  protected val seven = four + three
  protected val eight = four + four
  protected val nine = five + four
  protected val ten = five + five

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

  private class EratosthenesCell(val value: NUM) {
    private var marked = false
    def mark() { marked = true }
    def isMarked = marked
    def isPrime() = marked == false // true for primes only once the sieve of eratosthenes is finished
    override def toString() = s"ECell($value, $marked)"
  }
  
  /**
   *
   */
  def eratosthenesSieve(v: NUM): List[CheckedValue[NUM]] = {
    val upTo = sqrt(v)
    @tailrec
    def buildSieve(it: NumericReverseIterator[NUM], cur: List[EratosthenesCell] = Nil): List[EratosthenesCell] =
      if (it.hasNext) buildSieve(it, new EratosthenesCell(it.next) :: cur) else cur
    @tailrec
    def mark(multiples: Stream[NUM], cur: List[EratosthenesCell]) {
      cur.headOption match {
        case None                    =>
        case _ if multiples.head > v =>
        case Some(cell) if cell.value == multiples.head =>
          cell.mark()
          mark(multiples.tail, cur.tail)
        case Some(cell) =>
          mark(multiples, cur.tail)
      }
    }
    @tailrec
    def worker(cur: List[EratosthenesCell]) {
      if (!cur.isEmpty && cur.head.value <=upTo) {
        if (!cur.head.isMarked) {
          mark(new NumericIterator[NUM](two).toStream.map(_ * cur.head.value), cur.tail)
        }
        worker(cur.tail)
      }
    }
    val sieve = buildSieve(new NumericReverseIterator[NUM](v, _ > two))
    worker(sieve)
    var nthIsPrime = zero
    var nthIsNotPrime = zero
    def computeNth(isp: Boolean): NUM = {
      if (isp) { nthIsPrime += one; nthIsPrime } else { nthIsNotPrime += one; nthIsNotPrime }
    }
    sieve.map(ec => CheckedValue(ec.value, ec.isPrime(), computeNth(ec.isPrime())))
  }

  /**
   *
   */
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

  //  def sacksSpiral(size:Int, values: => Iterator[CheckedValue[NUM]], len:Int): BufferedImage = {

  //  }

  private def spiral(size: Int, draw: (Graphics2D, Int, Int, Int) => Unit): BufferedImage = {
    val width = size
    val height = size
    val xc = width / 2
    val yc = height / 2
    val bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val gr = bi.createGraphics()
    gr.setBackground(Color.BLACK)
    gr.clearRect(0, 0, width, height)
    gr.setColor(Color.WHITE)

    @tailrec
    def spiral(x: Int, y: Int, sz: Int, remain: Int, len: Int) {
      draw(gr, x, y, len)
      for { i <- 1 to sz } draw(gr, x, y + i, len + 1) // DOWN
      for { i <- 1 to sz } draw(gr, x - i, y + sz, len + 2) // LEFT
      for { i <- 1 to sz + 1 } draw(gr, x - sz, y + sz - i, len + 3) // UP
      for { i <- 1 to sz } draw(gr, x - sz + i, y - 1, len + 4) // RIGHT
      if (remain > 0) spiral(x + 1, y - 1, sz + 2, remain - 2 * sz - 2 * (sz - 1), len + 5)
    }

    spiral(xc, yc, 1, width * height, 0)
    gr.setColor(Color.RED)
    gr.drawRect(xc, yc, 0, 0)
    bi
  }

  def ulamSpiral(size: Int, values: Iterator[CheckedValue[NUM]]): BufferedImage = {
    def draw(gr: Graphics2D, x: Int, y: Int, len: Int) {
      if (values.hasNext && values.next.isPrime) gr.drawRect(x, y, 0, 0)
    }
    spiral(size, draw)
  }

  def sacksInspiredSpiral(size: Int, interval: Int, values: Iterator[CheckedValue[NUM]]): BufferedImage = {
    def draw(gr: Graphics2D, x: Int, y: Int, len: Int) {
      if (values.hasNext && (len % interval == 0) && values.next.isPrime) gr.drawRect(x, y, 0, 0)
    }
    spiral(size, draw)
  }

}
