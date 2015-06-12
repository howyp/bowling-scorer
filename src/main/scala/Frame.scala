sealed trait Frame { val balls: Stream[Int] }
case class Regular(ball1: Int, ball2: Int) extends Frame { val balls = ball1 #:: ball2        #:: Stream.empty }
case class Bonus(ball1: Int, ball2: Int)   extends Frame { val balls = ball1 #:: ball2        #:: Stream.empty }
case class Spare(ball1: Int)               extends Frame { val balls = ball1 #:: (10 - ball1) #:: Stream.empty }
case object Strike                         extends Frame { val balls = 10    #:: Stream.Empty }
