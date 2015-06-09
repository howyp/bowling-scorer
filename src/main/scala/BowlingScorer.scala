import scala.util.parsing.combinator.RegexParsers

object BowlingScorer extends RegexParsers {
  def parseGame(g: String): Int = {
    parseAll(game, g) match {
      case Success(frames, _) => score(frames)
      case Failure(msg, _) => throw new IllegalArgumentException(msg)
      case Error(msg, _) => throw new IllegalArgumentException(msg)
    }
  }

  def game: Parser[List[Frame]] = repN(10, frame <~ "|") <~ "|"

  def frame: Parser[Frame] =
    (point ~ point) ^^ { case p1 ~ p2 => Points(p1, p2) } |
    point ~> "/" ^^^ Spare

  def point: Parser[Int] = miss | number

  def number: Parser[Int] = "[1-9]".r ^^ (_.toInt)
  def miss: Parser[Int] = "-" ^^^ 0

  def score(frames: List[Frame]): Int = frames match {
    case Nil => 0
    case Points(ball1, ball2) :: remainingFrames => ball1 + ball2 + score(remainingFrames)
    case Spare :: remainingFrames => 10 + score(remainingFrames.headOption.toList) + score(remainingFrames)
  }
}

sealed trait Frame
case class Points(ball1: Int, ball2: Int) extends Frame
case object Spare extends Frame