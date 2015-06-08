import scala.util.parsing.combinator.RegexParsers

object BowlingScorer extends RegexParsers {
  def parseGame(g: String): Int = {
    parseAll(game, g) match {
      case Success(l, _) => l.sum
      case Failure(msg, _) => throw new IllegalArgumentException(msg)
    }
  }

  def game: Parser[List[Int]] = repN(10, frame <~ "|") <~ "|"

  def frame: Parser[Int] = (point ~ point) ^^ { case p1 ~ p2 => p1 + p2 }

  def point: Parser[Int] = miss | number

  def number: Parser[Int] = "[1-9]".r ^^ (_.toInt)
  def miss: Parser[Int] = "-" ^^^ 0
}
