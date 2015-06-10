import scala.util.parsing.combinator.RegexParsers

object BowlingScorer extends RegexParsers with BowlingScoreCalculator {
  def parseGame(g: String): Int = score(BowlingScoreParser().parse(g))
}