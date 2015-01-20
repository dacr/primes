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
class PerfActorUnorderedPrimesTest extends PrimesTestCommons {

  test("akka actors (unordered) based computation test - BigInt") {
    info("results are unsorted")
    genericActorsTest(handler => new ActorsPrimesUnorderedGenerator[BigInt](handler))
  }
 
}
