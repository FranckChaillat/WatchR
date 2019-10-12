package utils.extractors

object CustomFloat {
  def unapply(arg: String): Option[Float] = {
    val ValidAmout = "(?<number>-?\\d*,\\d*)€".r
    ValidAmout.findFirstMatchIn(arg.replaceAll(" ", "")) match {
      case Some(f)=>
        Some(f.group(1).replace(",",".").toFloat)
      case _ =>
        None
    }
  }
}
