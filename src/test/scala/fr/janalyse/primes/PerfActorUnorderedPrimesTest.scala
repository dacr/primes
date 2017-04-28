/*
 * Copyright 2013 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */

package fr.janalyse.primes


class PerfActorUnorderedPrimesTest extends PrimesTestCommons {

  test("akka actors (unordered) based computation test - BigInt") {
    info("results are unsorted")
    //genericActorsTest(handler => new ActorsPrimesUnorderedGenerator[BigInt](handler))
  }
 
}
