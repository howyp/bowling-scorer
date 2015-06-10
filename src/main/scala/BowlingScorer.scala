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
    (point <~ "/" ) ^^ { Spare(_) }

  def point: Parser[Int] = miss | number

  def number: Parser[Int] = "[1-9]".r ^^ (_.toInt)
  def miss: Parser[Int] = "-" ^^^ 0

  def score(frames: List[Frame]): Int = frames match {
    case Nil => 0
    case Points(ball1, ball2) :: remainingFrames => ball1 + ball2                                    + score(remainingFrames)
    case Spare(_)             :: remainingFrames => 10    + remainingFrames.headOption.fold(0)(_.ball1) + score(remainingFrames)
  }
}

sealed trait Frame { def ball1: Int }
case class Points(ball1: Int, ball2: Int) extends Frame
case class Spare(ball1: Int) extends Frame