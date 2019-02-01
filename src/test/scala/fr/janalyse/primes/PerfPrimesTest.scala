/*
 * Copyright 2013 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */

package fr.janalyse.primes

class PerfPrimesTest extends PrimesTestCommons {

  test("Performance classic tests - Long") {
    val pgen = new PrimesGenerator[Long]
    import pgen._
    for (sz <- perfTestSeries)
      howLongFor(sz)(primes.drop(_).head)("lastPrime=" + _.toString)
  }

  test("Performance parallel tests - Long") {
    val pgen = new PrimesGenerator[Long]
    import pgen._
    for (sz <- perfTestSeries)
      howLongFor(sz)(primesPar.drop(_).head)("lastPrime=" + _.toString)
  }

  test("Performance classic tests - BigInt") {
    val pgen = new PrimesGenerator[BigInt]
    import pgen._
    for (sz <- perfTestSeries)
      howLongFor(sz)(primes.drop(_).head)("lastPrime=" + _.toString)
  }

  test("Performance parallel tests - BigInt") {
    val pgen = new PrimesGenerator[BigInt]
    import pgen._
    for (sz <- perfTestSeries)
      howLongFor(sz)(primesPar.drop(_).head)("lastPrime=" + _.toString)
  }

}
