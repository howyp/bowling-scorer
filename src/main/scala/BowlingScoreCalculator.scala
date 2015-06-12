trait BowlingScoreCalculator {
  def score(frames: List[Frame]): Int = frames match {
    case (f: Regular) :: remain   => f.balls.sum + score(remain)
    case (f: Spare)   :: remain   => f.balls.sum + balls(remain).head + score(remain)
    case (f@ Strike)  :: remain   => f.balls.sum + balls(remain).take(2).sum + score(remain)
    case Bonus(_, _)  :: _  | Nil => 0
  }

  def balls(frames: List[Frame]): Stream[Int] = frames match {
    case frame :: remain => frame.balls #::: balls(remain)
    case Nil             => Stream.Empty
  }
}