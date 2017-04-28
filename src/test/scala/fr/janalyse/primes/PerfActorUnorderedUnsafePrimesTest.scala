/*
 * Copyright 2013 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */

package fr.janalyse.primes



class PerfActorUnorderedUnsafePrimesTest extends PrimesTestCommons {

  test("akka actors (unordered and unsafe) based computation test - BigInt") {
    info("results are unsorted")
    info("no back pressure management in this implemntation")
    //genericActorsTest(handler => new ActorsPrimesUnorderedUnsafeGenerator[BigInt](handler))
  }
 
}
