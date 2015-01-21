/*
 * Copyright 2013 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */

package fr.janalyse.primes

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.lang.management.ManagementFactory
import scala.concurrent._
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class PerfAkkaStreamPrimesTest extends PrimesTestCommons {

  test("akka actors streams based computation test - BigInt") {
    genericActorsTest(handler => new StreamBasedPrimesGenerator[BigInt](handler))
    info("The order is preserved, with back-pressure control and the whole without any effort !")
    info("the behavior is a little kind different than with PrimesGenerator")
  }
 
}
