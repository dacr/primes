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
class PerfActorUnorderedUnsafePrimesTest extends PrimesTestCommons {

  test("akka actors (unordered and unsafe) based computation test - BigInt") {
    info("results are unsorted")
    info("no back pressure management in this implemntation")
    genericActorsTest(handler => new ActorsPrimesUnorderedUnsafeGenerator[BigInt](handler))
  }
 
}
