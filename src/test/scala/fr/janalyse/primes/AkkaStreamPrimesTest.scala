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
import scala.concurrent.duration._
import scala.concurrent._
import scala.util.{ Failure, Success, Try }

@RunWith(classOf[JUnitRunner])
class AkkaStreamPrimesTest extends PrimesTestCommons {

  test("Simple akka stream primes tests") {
    val started=now
    val goal = 200000L
    info(s"Find the $goal th prime")
    val primePromise = Promise[CheckedValue[Long]]
    def handler(got : CheckedValue[Long]) {
      if (got.isPrime && got.nth==goal) primePromise.complete(Success(got))
    }
    val sgen = new StreamBasedPrimesGenerator[Long](handler)
    
    // Return back in the current thread 
    val result = Await.result(primePromise.future, 2.minutes)
    val ellapsed = now-started
    info(s"Found $result in ${ellapsed}ms") // Executed in the current thread is better for scalatest to avoid formatting issues

    sgen.shutdown

  }
}
