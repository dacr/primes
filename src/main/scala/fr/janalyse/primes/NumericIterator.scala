package fr.janalyse.primes

class NumericIterator[NUM](startFrom:NUM)(implicit numops: Integral[NUM]) extends Iterator[NUM] {
  import numops._
  private var num: NUM = startFrom - one
  override def next(): NUM = {
    num += one
    num
  }
  override def hasNext(): Boolean = true
}