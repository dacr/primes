/*
 * Copyright 2013-2020 David Crosson
 *
 * Licensed under the GPL, Version 2.0
 */
package fr.janalyse.primes

class NumericReverseIterator[NUM](startWith:NUM, endTest:NUM=>Boolean)(implicit numops: Integral[NUM]) extends Iterator[NUM] {
  import numops._
  private var num: NUM = startWith + one
  override def next(): NUM = {
    num -= one
    num
  }
  override def hasNext: Boolean = endTest(num)
}