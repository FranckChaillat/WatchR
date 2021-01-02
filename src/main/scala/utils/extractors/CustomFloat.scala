package utils.extractors

object CustomFloat {
  def unapply(arg: String): Option[Float] = {
    val ValidAmout = "([^\\x00-\\x7F]?\\d+.\\d+)".r
    ValidAmout.findFirstMatchIn(arg.replaceAll(" ", "")) match {
      case Some(f)=>
        val mtch = f.group(0)
        Some(
          if(mtch.headOption.contains(8722.toChar)) {
            s"-${mtch.tail}".replace(",",".").toFloat
          } else  {
            mtch.replace(",",".").toFloat
        })
      case _ =>
        None
    }
  }
}
