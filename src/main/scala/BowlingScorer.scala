import scala.util.parsing.combinator.RegexParsers

object BowlingScorer extends RegexParsers {
  def parseGame(g: String): Int = {
    parseAll(game, g) match {
      case Success(frames, _) => score(frames)
      case Failure(msg, _) => throw new IllegalArgumentException(msg)
      case Error(msg, _) => throw new IllegalArgumentException(msg)
    }
  }

  def game: Parser[List[Frame]] = repN(10, frame <~ "|") ~ "|" ~ opt(bonusBalls) ^? {
    case frames ~ _ ~ bonusBalls => frames ++ bonusBalls
  }

  def frame: Parser[Frame] =
    (point ~ point) ^^ { case p1 ~ p2 => Regular(p1, p2) } |
    (point <~ "/" ) ^^ { Spare(_) } |
    "X"             ^^^ { Strike }

  def bonusBalls: Parser[Frame] =
    (point ~  point) ^^ { case p1 ~ p2 => Bonus(p1, p2) } |
    (point <~ "/"  ) ^^ { p1 => Bonus(p1, 10 - p1) } |
    ("X"   ~> point) ^^ { p2 => Bonus(10, p2) } |
    ("X"   ~  "X"  ) ^^^ { Bonus(10, 10) } |
    point            ^^ { p1 => Bonus(p1, 0) }


  def point: Parser[Int] = miss | number

  def number: Parser[Int] = "[1-9]".r ^^ (_.toInt)
  def miss: Parser[Int] = "-" ^^^ 0

  def score(frames: List[Frame]): Int = frames match {
    case Nil |
         Bonus(_, _)     :: _     => 0
    case Regular(b1, b2) :: tail  => b1 + b2 + score(tail)
    case Spare(_)        :: tail  => 10 + ball1(tail) + score(tail)
    case Strike          :: tail  => 10 + ball1(tail) + ball2(tail) + score(tail)
  }

  def ball1(frames: List[Frame]): Int = frames match {
    case Nil                => 0
    case Regular(b1, _) :: _ => b1
    case Bonus(b1, _)   :: _ => b1
    case Spare(b1)      :: _ => b1
    case Strike         :: _ => 10
  }

  def ball2(frames: List[Frame]): Int = frames match {
    case Nil                   => 0
    case Regular(_, b2) :: _    => b2
    case Bonus(_, b2)   :: _    => b2
    case Spare(b1)      :: _    => 10 - b1
    case Strike         :: tail => ball1(tail)
  }
}

sealed trait Frame
case class Regular(ball1: Int, ball2: Int) extends Frame
case class Bonus(ball1: Int, ball2: Int) extends Frame
case class Spare(ball1: Int) extends Frame
case object Strike extends Frame