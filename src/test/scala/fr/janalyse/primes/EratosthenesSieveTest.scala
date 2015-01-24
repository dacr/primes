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
import org.scalatest.OptionValues._

@RunWith(classOf[JUnitRunner])
class EratosthenesSieveTest extends PrimesTestCommons {

  test("basic tests") {
    val dp = new PrimesDefinitions[Int]()
    val found = dp.eratosthenesSieve(10)
    found.find(_.value == 9).value.isPrime should be (false)
    found.find(_.value == 7).value.isPrime should be (true)
  }

  test("correctness test") {
    val howmany = 1000
    
    val dp = new PrimesDefinitions[Int]()
    val gp = new PrimesGenerator[Int]()
    
    val found = dp.eratosthenesSieve(howmany)
    
    found should equal(gp.checkedValues.takeWhile { _.value <= howmany })
  }

  test("EratosthenesSieve performance test") {
    val howmany = 500000
    val dp = new PrimesDefinitions[BigInt]()
    val gp = new PrimesGenerator[BigInt]()
    
    howlongfor(howmany, x=> gp.checkedValues.takeWhile(_.value <= x).toList)(x => "Classic last="+x.last)
    howlongfor(howmany, dp.eratosthenesSieve(_))(x => "Eratosthenes last="+x.last)
    info("(Mono-thread algorithms)")
  }
}
