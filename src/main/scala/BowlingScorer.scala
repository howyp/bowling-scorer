import scala.util.parsing.combinator.RegexParsers

object BowlingScorer extends RegexParsers with BowlingScoreCalculator {
  def parseGame(g: String): Int = {
    val parse = BowlingScoreParser().parse(g)
    val s = score(parse)
    println(
      s"""Parsed game $g
         |         as $parse
         |      balls ${balls(parse).force}
         |      score $s""".stripMargin)
    s
  }
}