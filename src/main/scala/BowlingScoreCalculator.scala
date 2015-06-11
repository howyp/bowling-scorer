trait BowlingScoreCalculator {
  def score(frames: List[Frame]): Int = frames match {
    case Regular(b1, b2) :: remain  => b1 + b2 + score(remain)
    case Spare(_)        :: remain  => 10 + balls(remain).head + score(remain)
    case Strike          :: remain  => 10 + balls(remain).sum  + score(remain)
    case Nil |
         Bonus(_, _)     :: _     => 0
  }

  def balls(frames: List[Frame]): Stream[Int] = frames match {
    case Regular(b1, b2) :: remain => b1 #:: b2                 #:: Stream.Empty
    case Bonus(b1, b2)   :: remain => b1 #:: b2                 #:: Stream.Empty
    case Spare(b1)       :: remain => b1 #:: (10 - b1)          #:: Stream.Empty
    case Strike          :: remain => 10 #:: balls(remain).head #:: Stream.Empty
    case Nil                       => Stream.Empty
  }
}