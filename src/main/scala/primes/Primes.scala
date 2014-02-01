/*
 * Copyright 2013 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */

package primes

object Primes {

  import annotation.tailrec
  
  type PInteger = BigInt

  def pow(n:PInteger, p:PInteger):PInteger = {
    @tailrec
    def powit(cur:PInteger, r:PInteger):PInteger = {
      if (r==1) cur else powit(cur*n, r-1) 
    }
    if (p<0) sys.error("Not supported")
    if (p==0) 1 else powit(n,p)
  }
  
  def isPrime(v: PInteger): Boolean = {
    @tailrec
    def checkUpTo(curr: PInteger, upTo: PInteger): Boolean =
      if (curr >= upTo) true
      else (v /% curr) match {
        case (_, mod) if mod == 0 => false
        case (nextUpTo, _) => checkUpTo(curr + 1, nextUpTo + 1)
      }
    checkUpTo(2, v / 2 + 1)
  }
  
  def isMersennePrimeExponent(v:PInteger): Boolean =
    isPrime(v) && isPrime(pow(2,v)-1)

  def isSexyPrime(v:PInteger):Boolean =
    isPrime(v) && isPrime(v+6)

  def isTwinPrime(v:PInteger):Boolean =
    isPrime(v) && isPrime(v+2)
    
  def isIsolatedPrime(v:PInteger):Boolean =
    !isPrime(v-2) && isPrime(v) && !isPrime(v+2)

  // ------------------------ STREAMS ------------------------
    
  def integerStream = {
    def next(cur: PInteger): Stream[PInteger] = cur #:: next(cur + 1)
    next(1)
  }
  
  def candidatesStream = integerStream.tail

  def primeStream =
    candidatesStream
      .filter(isPrime(_))

  def primeStreamPar =
    candidatesStream
      .iterator //  workaround for Memory impact of the .par on just stream is too huge...
      .grouped(1000)
      .map(_.par)
      .flatMap(_.filter(isPrime(_)))
      .toStream

  def mersennePrimeStream=
    candidatesStream
      .map(pow(2, _))
      .filter(isPrime(_))
      
  def sexyPrimeStream =
    candidatesStream
      .filter(isSexyPrime(_))
    
  def twinPrimeStream =
    candidatesStream
      .filter(isTwinPrime(_))
      
  def isolatedPrimeStream =
    candidatesStream
      .filter(isIsolatedPrime(_))

}
