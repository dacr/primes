package fr.janalyse.primes

class NumericReverseIterator[NUM](startWith:NUM, endTest:NUM=>Boolean)(implicit numops: Integral[NUM]) extends Iterator[NUM] {
  import numops._
  private var num: NUM = startWith + one
  override def next(): NUM = {
    num -= one
    num
  }
  override def hasNext(): Boolean = endTest(num)
}