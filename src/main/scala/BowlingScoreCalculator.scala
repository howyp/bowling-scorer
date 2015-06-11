trait BowlingScoreCalculator {
  def score(frames: List[Frame]): Int = frames match {
    case Nil |
         Bonus(_, _)     :: _     => 0
    case Regular(b1, b2) :: tail  => b1 + b2 + score(tail)
    case Spare(_)        :: tail  => 10 + ball1(tail) + score(tail)
    case Strike          :: tail  => 10 + ball1(tail) + ball2(tail) + score(tail)
  }

  def ball1(frames: List[Frame]): Int = frames match {
    case Nil                 => 0
    case Regular(b1, _) :: _ => b1
    case Bonus(b1, _)   :: _ => b1
    case Spare(b1)      :: _ => b1
    case Strike         :: _ => 10
  }

  def ball2(frames: List[Frame]): Int = frames match {
    case Nil                    => 0
    case Regular(_, b2) :: _    => b2
    case Bonus(_, b2)   :: _    => b2
    case Spare(b1)      :: _    => 10 - b1
    case Strike         :: tail => ball1(tail)
  }
}
