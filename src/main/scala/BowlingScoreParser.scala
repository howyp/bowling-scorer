import scala.util.parsing.combinator.RegexParsers

case class BowlingScoreParser() extends RegexParsers {

  def parse(g: String): List[Frame] = parseAll(game, g) match {
    case Success(frames ~ bonus, _) => frames ++ bonus
    case Failure(msg, _) => throw new IllegalArgumentException(msg)
    case Error(msg, _) => throw new IllegalArgumentException(msg)
  }

  def game: Parser[List[Frame] ~ Option[Bonus]] =
    repN(10, frame <~ "|") ~ ("|" ~> opt(bonusBalls))

  def frame: Parser[Frame] =
    (point ~ point) ^^  { case p1 ~ p2 => Regular(p1, p2) } |
    (point <~ "/" ) ^^  { Spare(_) } |
    "X"             ^^^ { Strike }

  def bonusBalls: Parser[Bonus] =
    (point ~  point) ^^  { case p1 ~ p2 => Bonus(p1, p2)      } |
    (point <~ "/"  ) ^^  {           p1 => Bonus(p1, 10 - p1) } |
    ("X"   ~> point) ^^  {           p2 => Bonus(10, p2)      } |
    ("X"   ~  "X"  ) ^^^ {                 Bonus(10, 10)      } |
    point            ^^  {           p1 => Bonus(p1, 0)       }

  def point: Parser[Int] = miss | number
  def miss: Parser[Int] = "-" ^^^ 0
  def number: Parser[Int] = "[1-9]".r ^^ (_.toInt)
}
