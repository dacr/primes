/*
 * Copyright 2013 David Crosson
 * 
 * Licensed under the GPL, Version 2.0
 */
package fr.janalyse.primes

import scala.util.Try

object Main {

  val pgen = new PrimesGenerator[Long]
  import pgen._

  // -------------------------------------------------------------
  def ulamSpiralToPng(size: Int) {
    import java.io.File
    import javax.imageio.ImageIO

    ImageIO.write(ulamSpiral(size, checkedValues.iterator), "PNG", new File(s"ulam-spiral-${size}.png"));
    ImageIO.write(sacksInspiredSpiral(size, 3, checkedValues.iterator), "PNG", new File(s"ulam-sacks-like-${size}.png"));
  }

  // -------------------------------------------------------------
  def main(args: Array[String]): Unit = {
    // With PrimesGenerator[Long]
    // 1000 - 8s
    // 2000 - 27s
    // 3000 - 1m16s
    val size = Try { args(0).toInt }.getOrElse(1000)
    ulamSpiralToPng(size)
  }

}