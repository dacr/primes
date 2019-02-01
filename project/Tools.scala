
object Tools {
  def penvOrElse(key:String, alt:String):String = {
    import scala.util.Properties._
    propOrNone(key).orElse(envOrNone(key)).getOrElse(alt)
  }

}
