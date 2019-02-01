/*
 * Copyright 2013 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */

package fr.janalyse.primes

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, ExecutionContextExecutorService}
import java.util.concurrent.{Executors, ThreadPoolExecutor, TimeUnit}

import scala.concurrent.forkjoin.ForkJoinPool
import org.scalatest.Matchers._


class IsPrimesTest extends PrimesTestCommons {

  //val ec = ExecutionContext.Implicits.global
  val fec: ExecutionContextExecutor = ExecutionContext.fromExecutor(new ForkJoinPool())
  val tec: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())
  
  test("isPrimePara tests") {
    val gen = new PrimesGenerator[Int]
    import gen._
    import scala.concurrent.ExecutionContext.Implicits.global
    val monos = integers.filter(isPrime).take(100).toList
    val paras = integers.filter(isPrimePara).take(100).toList
    paras should equal(monos)
  }

  test("isPrime mono versus parallel tests") {
    val gen = new PrimesGenerator[BigInt]
    import gen._
    val howmany = 10000
    val mono = integers.filter(isPrime)
    howLongFor(howmany)(mono.drop)(p=>s"serial processing, highest prime found $p")
    val para1 = integers.filter(isPrimePara(_)(fec))
    howLongFor(howmany)(para1.drop)(p=>s"parallel processing, highest prime found $p (ForkJoinPool)")
    val para2 = integers.filter(isPrimePara(_)(tec))
    howLongFor(howmany)(para2.drop)(p=>s"parallel processing, highest prime found $p (CachedThreadPool)")
  }

  test("very big prime test") {
    val gen = new PrimesGenerator[BigInt]
    import gen._
  //val bigOne = BigInt("17436553453233413033")
  //val bigOne = BigInt("174364130192343257")
    val bigOne = BigInt("17436413019234331") // smaller is faster for the test to execute
    howLongFor(bigOne)(isPrime)(_ => s"sequential isPrime") should be(true)
    howLongFor(bigOne)(isPrimePara(_)(fec))(_ => s"parallel isPrime (ForkJoinPool)") should be(true)
    howLongFor(bigOne)(isPrimePara(_)(tec))(_ => s"parallel isPrime (CachedThreadPool)") should be(true)
  }
  
}
