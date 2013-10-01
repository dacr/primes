package primes

import java.awt._
import java.awt.geom.Ellipse2D
import java.awt.image._
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import scala.util.Try

object Main {
  import Primes.PInteger
  
  // -------------------------------------------------------------
  def ulamSpiral(size:Int):BufferedImage = {
    val width = size
    val height = size
    val xc=width/2
    val yc=height/2
    val bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val gr = bi.createGraphics()
    gr.setBackground(Color.BLACK)
    gr.clearRect(0, 0, width, height)
    gr.setColor(Color.WHITE)
        
    def draw(x:Int, y:Int, val2test:PInteger) {
      if (Primes.isPrime(val2test)) gr.drawRect(x,y,0,0)
    }
    
    @annotation.tailrec
    def drawit(x:Int, y:Int, sz:Int, remain:Int, ints:Iterator[PInteger]) {
      draw(x,  y, ints.next)
      for{i <- 1 to sz}   draw(x,  y+i, ints.next) // DOWN
      for{i <- 1 to sz}   draw(x-i,y+sz, ints.next) // LEFT
      for{i <- 1 to sz+1} draw(x-sz, y+sz-i, ints.next) // UP
      for{i <- 1 to sz}   draw(x-sz+i, y-1, ints.next) // RIGHT
      if (remain>0) drawit(x+1, y-1, sz+2, remain-2*sz-2*(sz-1), ints)
    }
    
    drawit(xc, yc, 1, width*height, (Primes.candidatesStream).toIterator)
    gr.setColor(Color.RED)
    gr.drawRect(xc,yc,0,0)
    bi
  }
  
  // -------------------------------------------------------------
  def ulamSpiralToPng(size:Int) {
    val bi = ulamSpiral(size)
    ImageIO.write(bi, "PNG", new File(s"primes-${size}.png"));    
  }
  
  
  // -------------------------------------------------------------
  def main(args: Array[String]): Unit = {
    // 1000 - 16s
    // 2000 - 1m34s
    // 3000 - 4m40s
    // 4000 - 10m37s
    val size = Try{args(0).toInt}.getOrElse(1000)
    ulamSpiralToPng(size)
  }

}