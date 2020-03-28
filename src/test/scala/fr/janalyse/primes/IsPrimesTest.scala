/*
 * Copyright 2013 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */

package fr.janalyse.primes

class IsPrimesTest extends PrimesTestCommons {

  test("isPrimePara tests") {
    val gen = new PrimesGenerator[Int]
    import gen._
    import scala.concurrent.ExecutionContext.Implicits.global
    val monos = integers.filter(isPrime(_)).take(100).toList
    val paras = integers.filter(isPrimePara(_)).take(100).toList
    paras should equal(monos)
  }

}
