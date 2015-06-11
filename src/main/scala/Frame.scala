sealed trait Frame
case object Strike extends Frame
case class Spare(ball1: Int) extends Frame
case class Regular(ball1: Int, ball2: Int) extends Frame
case class Bonus(ball1: Int, ball2: Int) extends Frame
