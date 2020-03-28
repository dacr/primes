/*
 * Copyright 2013-2020 David Crosson
 *
 * Licensed under the GPL, Version 2.0
 */
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