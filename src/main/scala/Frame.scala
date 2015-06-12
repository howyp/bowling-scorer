sealed trait Frame { val balls: Stream[Int] }
case class Regular(ball1: Int, ball2: Int) extends Frame { val balls = Stream(ball1, ball2) }
case class Bonus(ball1: Int, ball2: Int)   extends Frame { val balls = Stream(ball1, ball2) }
case class Spare(ball1: Int)               extends Frame { val balls = Stream(ball1, 10 - ball1) }
case object Strike                         extends Frame { val balls = Stream(10) }
