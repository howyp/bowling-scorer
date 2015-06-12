trait BowlingScoreCalculator {
  def score(frames: List[Frame]): Int = frames match {
    case (r: Regular) :: remain   => r.balls.sum + score(remain)
    case (s: Spare)   :: remain   => s.balls.sum + balls(remain).head + score(remain)
    case Strike       :: remain   => Strike.balls.sum + balls(remain).take(2).sum + score(remain)
    case Bonus(_, _)  :: _  | Nil => 0
  }

  def balls(frames: List[Frame]): Stream[Int] = frames match {
    case frame :: remain => frame.balls #::: balls(remain)
    case Nil             => Stream.Empty
  }
}