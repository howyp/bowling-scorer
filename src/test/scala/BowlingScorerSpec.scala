import org.scalacheck.Gen
import org.scalatest._
import org.scalatest.prop.PropertyChecks

class BowlingScorerSpec extends WordSpec with Matchers with PropertyChecks {
  """
    |Write a program to score a game of Ten-Pin Bowling.
    |
    |Input: string (described below) representing a bowling game
    |Ouput: integer score
    |
    |The scoring rules:
    |
    |Each game, or "line" of bowling, includes ten turns,
    |or "frames" for the bowler.
    |
    |In each frame, the bowler gets up to two tries to
    |knock down all ten pins.
    |
    |If the first ball in a frame knocks down all ten pins,
    |this is called a "strike". The frame is over. The score
    |for the frame is ten plus the total of the pins knocked
    |down in the next two balls.
    |
    |If the second ball in a frame knocks down all ten pins,
    |this is called a "spare". The frame is over. The score
    |for the frame is ten plus the number of pins knocked
    |down in the next ball.
    |
    |If, after both balls, there is still at least one of the
    |ten pins standing the score for that frame is simply
    |the total number of pins knocked down in those two balls.
    |
    |If you get a spare in the last (10th) frame you get one
    |more bonus ball. If you get a strike in the last (10th)
    |frame you get two more bonus balls.
    |These bonus throws are taken as part of the same turn.
    |If a bonus ball knocks down all the pins, the process
    |does not repeat. The bonus balls are only used to
    |calculate the score of the final frame.
    |
    |The game score is the total of all frame scores.
    |
    |Examples:
    |
    |X indicates a strike
    |/ indicates a spare
    |- indicates a miss
    || indicates a frame boundary
    |The characters after the || indicate bonus balls
    |
    |X|X|X|X|X|X|X|X|X|X||XX
    |Ten strikes on the first ball of all ten frames.
    |Two bonus balls, both strikes.
    |Score for each frame == 10 + score for next two
    |balls == 10 + 10 + 10 == 30
    |Total score == 10 frames x 30 == 300
    |
    |9-|9-|9-|9-|9-|9-|9-|9-|9-|9-||
    |Nine pins hit on the first ball of all ten frames.
    |Second ball of each frame misses last remaining pin.
    |No bonus balls.
    |Score for each frame == 9
    |Total score == 10 frames x 9 == 90
    |
    |5/|5/|5/|5/|5/|5/|5/|5/|5/|5/||5
    |Five pins on the first ball of all ten frames.
    |Second ball of each frame hits all five remaining
    |pins, a spare.
    |One bonus ball, hits five pins.
    |Score for each frame == 10 + score for next one
    |ball == 10 + 5 == 15
    |Total score == 10 frames x 15 == 150
    |
    |X|7/|9-|X|-8|8/|-6|X|X|X||81
    |Total score == 167
  """.stripMargin

  "Bowling scorer" should {
    import BowlingScorer._
    "understand all misses" in {
      parseGame("--|--|--|--|--|--|--|--|--|--||") should equal (0)
    }
    "understand all ones" in {
      parseGame("11|11|11|11|11|11|11|11|11|11||") should equal (20)
    }
    "understand all ones and misses" in {
      parseGame("1-|1-|1-|1-|1-|1-|1-|1-|1-|1-||") should equal (10)
    }
    "understand all twos and misses" in {
      parseGame("2-|2-|2-|2-|2-|2-|2-|2-|2-|2-||") should equal (20)
    }
    "understand all misses and threes" in {
      parseGame("-3|-3|-3|-3|-3|-3|-3|-3|-3|-3||") should equal (30)
    }
    "understand one of each number" in {
      parseGame("1-|2-|3-|4-|5-|6-|7-|8-|9-|1-||") should equal (1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9 + 1)
    }
    "understand mostly fives and spares but with fives in 10th frame" in {
      parseGame("5/|5/|5/|5/|5/|5/|5/|5/|5/|5-||") should equal (9 * 15 + 5)
    }
    "understand mostly strikes but with fives in 10th frame" in {
      parseGame("X|X|X|X|X|X|X|X|X|5-||") should equal (255)
    }
    "understand mostly ones but with strike in 10th frame and ones for both bonus balls" in pending
    "understand mostly ones but with spare in 10th frame and a one for the bonus ball" in pending
    "reject two strikes in one frame" in pending
    "reject a spare as the first point in a frame" in pending
    "reject a strike as the second point in a frame" in pending
    "reject an 11-frame game" in {
      an [IllegalArgumentException] should be thrownBy parseGame("11|11|11|11|11|11|11|11|11|11|11||")
    }
    "reject an 9-frame game" in {
      an [IllegalArgumentException] should be thrownBy parseGame("11|11|11|11|11|11|11|11|11||")
    }
    "reject bonus balls if no spare in 10th frame" in pending
    "reject bonus balls if no strike in 10th frame" in pending
    "reject one bonus ball if strike in 10th frame" in pending
    "score 'X|X|X|X|X|X|X|X|X|X||XX' as 300" in {
      parseGame("X|X|X|X|X|X|X|X|X|X||XX") should equal (300)
    }
    "score '9-|9-|9-|9-|9-|9-|9-|9-|9-|9-||' as 90" in pending
    "score '5/|5/|5/|5/|5/|5/|5/|5/|5/|5/||5' as 150" in pending
    "score 'X|7/|9-|X|-8|8/|-6|X|X|X||81' as 167" in pending
    "parse a valid game" in {
      import Gen._
      val point = Gen.oneOf(choose(1,9).map(_.toString), const("-"))
      val frame = for (p1 <- point; p2 <- point) yield p1 + p2
      forAll(listOfN(10, frame)) { frames =>
        val game = frames.mkString("|") + "||"
        parseGame(game) should be >= 0
      }
    }
  }
}
