/*
 * Copyright 2013 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */

package fr.janalyse.primes

import scala.concurrent.ExecutionContext
import java.util.concurrent.{ Executors, ThreadPoolExecutor, TimeUnit }
import scala.concurrent.forkjoin.ForkJoinPool
import org.scalatest.Matchers._


class IsPrimesTest extends PrimesTestCommons {

  //val ec = ExecutionContext.Implicits.global
  val fec = ExecutionContext.fromExecutor(new ForkJoinPool())
  val tec = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())
  
  test("isPrimePara tests") {
    val gen = new PrimesGenerator[Int]
    import gen._
    import scala.concurrent.ExecutionContext.Implicits.global
    val monos = integers.filter(isPrime(_)).take(100).toList
    val paras = integers.filter(isPrimePara(_)).take(100).toList
    paras should equal(monos)
  }

  test("isPrime mono versus parallel tests") {
    val gen = new PrimesGenerator[BigInt]
    import gen._
    val howmany = 10000
    val mono = integers.filter(isPrime(_))
    howlongfor(howmany)(mono.drop)(p=>s"serial processing, highest prime found $p")
    val para1 = integers.filter(isPrimePara(_)(fec))
    howlongfor(howmany)(para1.drop)(p=>s"parallel processing, highest prime found $p (ForkJoinPool)")
    val para2 = integers.filter(isPrimePara(_)(tec))
    howlongfor(howmany)(para2.drop)(p=>s"parallel processing, highest prime found $p (CachedThreadPool)")
  }

  test("very big prime test") {
    val gen = new PrimesGenerator[BigInt]
    import gen._
  //val bigone = BigInt("17436553453233413033")
  //val bigone = BigInt("174364130192343257")
    val bigone = BigInt("17436413019234331") // smaller is faster for the test to execute
    howlongfor(bigone)(isPrime(_))(x=> s"sequential isPrime") should be(true)      
    howlongfor(bigone)(isPrimePara(_)(fec))(x=> s"parallel isPrime (ForkJoinPool)") should be(true)
    howlongfor(bigone)(isPrimePara(_)(tec))(x=> s"parallel isPrime (CachedThreadPool)") should be(true)
  }
  
}
