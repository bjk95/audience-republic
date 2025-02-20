object AudienceRepublicCodeChallenge extends App {
  private var nOpt: Option[Int] = None
  private var sOpt: Option[Int] = None
  var i = 0
  while (i < args.length) {
    args(i) match {
      case "-N" =>
        if (i + 1 < args.length) { nOpt = Some(args(i + 1).toInt); i += 1 }
      case "-S" =>
        if (i + 1 < args.length) { sOpt = Some(args(i + 1).toInt); i += 1 }
      case _ =>
    }
    i += 1
  }
  val n = nOpt.getOrElse {
    println("Number of vertices (-N) not provided, defaulting to 10.")
    10
  }
  val s = sOpt.getOrElse {
    println("Sparseness (-S) not provided, defaulting to 15.")
    15
  }

  GraphAlgorithms.run(n,s)
}
