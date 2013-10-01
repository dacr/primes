/*
 * Copyright 2013 David Crosson
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package primes

object Primes {

  import annotation.tailrec
  
  type PInteger = BigInt

  def pow(n:PInteger, p:PInteger) = {
    @tailrec
    def powit(cur:PInteger, p:PInteger):PInteger = {
      if (p==1) cur else powit(cur*n, p-1) 
    }
    powit(n,p)
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
      .grouped(1000)
      .toStream
      .map(_.par)
      .flatMap(_.filter(isPrime(_)))

  def mersennePrimeStream=
    candidatesStream
      .map(pow(2, _))
      .filter(isPrime(_))
      
  def sexyPrimeStream =
    candidatesStream
      .filter(isSexyPrime(_))
    
}
