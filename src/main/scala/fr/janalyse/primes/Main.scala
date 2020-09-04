/*
 * Copyright 2013-2020 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */
package fr.janalyse.primes

import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object Main {

  val pgen = new PrimesGenerator[Long]
  import pgen._

  // -------------------------------------------------------------
  def ulamSpiralToPng(size: Int):Unit = {
    import java.io.File
    import javax.imageio.ImageIO

    ImageIO.write(ulamSpiral(size, checkedValues.iterator), "PNG", new File(s"ulam-spiral-$size.png"))
    ImageIO.write(sacksInspiredSpiral(size, 3, checkedValues.iterator), "PNG", new File(s"ulam-sacks-like-$size.png"))
  }

  // -------------------------------------------------------------
  /*
  def main(args: Array[String]): Unit = {
    // With PrimesGenerator[Long]
    // 1000 - 8s
    // 2000 - 27s
    // 3000 - 1m16s
    val size = Try { args(0).toInt }.getOrElse(1000)
    ulamSpiralToPng(size)
  }
  */


  def sdf: SimpleDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S Z")
  def now: Long = System.currentTimeMillis
  
  
  case class RunningContext(props:Map[String,String])
  object RunningContext{
    import scala.util.Try
    
    def hostname: Option[(String,String)] = {
      Try(java.net.InetAddress.getLocalHost.getHostName).map("hostname"-> _).toOption
    }
    
    def apply():RunningContext = {
      RunningContext(
        Map(
          "scalaVersion"->scala.util.Properties.versionString,
          "javaVersion"->scala.util.Properties.javaVersion,
          "javaVendor"->scala.util.Properties.javaVendor,
          "osName"->scala.util.Properties.osName
        )
          ++ hostname
          ++ scala.util.Properties.propOrNone("os.version").map("osVersion" -> _)
          ++ scala.util.Properties.propOrNone("os.arch").map("osArch" -> _)
      )
    }
  }
  
  def genjson(props:Map[String,Any]):String = {
    val results:Map[String,String] = for { (key, value) <- props} yield {
      value match {
        case x:Int=> (key, x.toString)
        case x:Long=> (key, x.toString)
        case x:Double=> (key, x.toString)
        case x:Float=> (key, x.toString)
        case x => (key, "\""+x.toString+"\"")
      }
    } 
    results.map{case (k,v) => s""""$k":$v"""}.mkString("{", ", ", "}")
  }
  
  def howLongFor[U, T](param: U)(proc: U => T)(infoOnResult: T => String)(implicit context:RunningContext): T = {
    now match {
      case start =>
        val result = proc(param)
        val duration = now - start
        val msg = genjson(Map(
          "criteria"->param,
          "duration"->duration,
          "result"->infoOnResult(result),
          "when"->DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())
        )++context.props)
        println(msg)
        result
    }
  }

  def main(args:Array[String]):Unit = {

    val numRE="""(\d+)""".r
    val roundMax= args.headOption match {
      case Some(numRE(value)) => value.toInt
      case _=> Int.MaxValue
    }

    implicit val context: RunningContext = RunningContext()

    val pgen = new PrimesGenerator[Long]
    import pgen._

    @annotation.tailrec
    def perfLoop(sz:Int,step:Int=25000, round:Int=1):Unit = {
      howLongFor(sz)(primes.drop(_).next())("lastPrime=" + _.toString)
      if (round < roundMax) perfLoop(sz+step, step=step, round=round+1)
    }
    perfLoop(50000)
  }
}