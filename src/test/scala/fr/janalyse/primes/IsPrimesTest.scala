/*
 * Copyright 2013 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */

package fr.janalyse.primes

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.ShouldMatchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IsPrimesTest extends PrimesTestCommons {

  test("isPrimePara tests") {
    val gen = new PrimesGenerator[Int]
    import gen._
    val monos = integers.filter(isPrime(_)).take(10).toList
    val paras = integers.filter(isPrimePara(_)).take(10).toList
    paras should equal(monos)
  }

  test("very big prime test") {
    val gen = new PrimesGenerator[BigInt]
    import gen._
  //val bigone = BigInt("17436553453233413033")
    val bigone = BigInt("174364130192343257")
    howlongfor(bigone)(isPrime(_))(x=> s"sequential isPrime") should be(true)
    howlongfor(bigone)(isPrimePara(_))(x=> s"parallel isPrime") should be(true)
  }
  
  test("isPrime mono versus parallel tests") {
    val gen = new PrimesGenerator[Int]
    import gen._
    val howmany = 20000
    val mono = integers.filter(isPrime(_))
    howlongfor(howmany)(mono.drop)(p=>s"serial processing, highest prime found $p")
    val para = integers.filter(isPrimePara(_))
    howlongfor(howmany)(para.drop)(p=>s"parallel processing, highest prime found $p")
  }
}
