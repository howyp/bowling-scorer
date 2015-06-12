# bowling-scorer

[![Build Status](https://travis-ci.org/howyp/bowling-scorer.svg?branch=master)](https://travis-ci.org/howyp/bowling-scorer)

A solution that I cooked up in Scala to [Cyberdojo](http://cyber-dojo.org/)'s Bowling Game excercise, showing some handy features of Scala such as parser combinators, algebraic data types and pattern matching.

[The Problem](#the-problem) <br>
[My Solution](#my-solution)

## The Problem

Write a program to score a game of Ten-Pin Bowling.

**Input**: `String` (described below) representing a bowling game <br>
**Ouput**: `Int` score

### The scoring rules

Each game, or "line" of bowling, includes ten turns,
or "frames" for the bowler.
In each frame, the bowler gets up to two tries to
knock down all ten pins.

If the first ball in a frame knocks down all ten pins,
this is called a "strike". The frame is over. The score
for the frame is ten plus the total of the pins knocked
down in the next two balls.
If the second ball in a frame knocks down all ten pins,
this is called a "spare". The frame is over. The score
for the frame is ten plus the number of pins knocked
down in the next ball.

If, after both balls, there is still at least one of the
ten pins standing the score for that frame is simply
the total number of pins knocked down in those two balls.
If you get a spare in the last (10th) frame you get one
more bonus ball. If you get a strike in the last (10th)
frame you get two more bonus balls.
These bonus throws are taken as part of the same turn.
If a bonus ball knocks down all the pins, the process
does not repeat. The bonus balls are only used to
calculate the score of the final frame.

The game score is the total of all frame scores.

### Examples

* `X` indicates a strike<br>
* `/` indicates a spare<br>
* `-` indicates a miss<br>
* `|` indicates a frame boundary<br>
* The characters after the `||` indicate bonus balls

* `X|X|X|X|X|X|X|X|X|X||XX` <br>
Ten strikes on the first ball of all ten frames.
Two bonus balls, both strikes. <br>
Score for each frame == 10 + score for next two
balls == 10 + 10 + 10 == 30 <br>
Total score == 10 frames x 30 == 300

* `9-|9-|9-|9-|9-|9-|9-|9-|9-|9-||` <br>
Nine pins hit on the first ball of all ten frames.
Second ball of each frame misses last remaining pin.
No bonus balls. <br>
Score for each frame == 9 <br>
Total score == 10 frames x 9 == 90

* `5/|5/|5/|5/|5/|5/|5/|5/|5/|5/||5`
Five pins on the first ball of all ten frames.
Second ball of each frame hits all five remaining
pins, a spare.
One bonus ball, hits five pins. <br>
Score for each frame == 10 + score for next one
ball == 10 + 5 == 15 <br>
Total score == 10 frames x 15 == 150

* `X|7/|9-|X|-8|8/|-6|X|X|X||81` <br>
Total score == 167

## My Solution
I split the problem into a parser which generates an intermediate data structure, and a calculator which determines the score based on that structure.


####1. Data Structure - `List[Frame]`
The intermediate structure is a list of `Frame`s, an algeraic data type:

```scala
sealed trait Frame { val balls: Stream[Int] }
case class Regular(ball1: Int, ball2: Int) extends Frame { val balls = ball1 #:: ball2        #:: Stream.empty }
case class Bonus(ball1: Int, ball2: Int)   extends Frame { val balls = ball1 #:: ball2        #:: Stream.empty }
case class Spare(ball1: Int)               extends Frame { val balls = ball1 #:: (10 - ball1) #:: Stream.empty }
case object Strike                         extends Frame { val balls = 10    #:: Stream.Empty }
```

This will be quite convinient when calculating the score, as different types of frames are scored differently. Also, some frames contain a single ball, or a ball that must be inferred, so each `Frame` can return its `balls` as a `Stream[Int]`. 

####2. Parsing - `BowlingScoreParser` 
For parsing, Scala a has very nice functional parsing library called *parser combinators*. This allows us to build complex parsers out of combinations of simple ones. 

Lets start by looking at the parsers for simple points. A point is either a `miss` or a `number`:

```scala
def point: Parser[Int] = miss | number
```

A `miss` is simply a `-` character, and should be treated as a score of 0. Otherwise, we expect a point to be `number`, ie. a integer between 1 and 9:

```scala
def miss: Parser[Int] = "-" ^^^ 0
def number: Parser[Int] = "[1-9]".r ^^ (_.toInt)
```

From this, we can build a general parser for frames. There are three cases here:

* Two successive `point`s which become a `Regular`
* A single `X` becomes a `Strike`
* A point followed by a `/` becomes a `Spare`

```scala
def frame: Parser[Frame] =
  (point ~ point) ^^  { case p1 ~ p2 => Regular(p1, p2) } |
  (point <~ "/" ) ^^  { Spare(_) } |
  "X"             ^^^ { Strike }
```

We also need a way of parsing the bonus balls. These are different from a normal frame because:

* They do not increase the score themselves, only influence the score of the last frame
* They may contain multiple strikes

They are therefore parsed as a `Bonus`. This is more complex as there are only specific combinations of bonus balls that are allowed:

```scala
def bonusBalls: Parser[Bonus] =
  (point ~  point) ^^  { case p1 ~ p2 => Bonus(p1, p2)      } |
  (point <~ "/"  ) ^^  {           p1 => Bonus(p1, 10 - p1) } |
  ("X"   ~> point) ^^  {           p2 => Bonus(10, p2)      } |
  ("X"   ~  "X"  ) ^^^ {                 Bonus(10, 10)      } |
  point            ^^  {           p1 => Bonus(p1, 0)       }
```

Finally, we need a parser for the whole scorecard. A `game` is exactly 10 `frame`s, separated by `|`, followed by the final `|` and optionally some `bonusBalls`:

```scala
def game: Parser[List[Frame] ~ Option[Bonus]] =
  repN(10, frame <~ "|") ~ ("|" ~> opt(bonusBalls))
```

Let's take a look how a parsed game might look. The final example in the spec, `X|7/|9-|X|-8|8/|-6|X|X|X||81`,  parses as:

```scala
List(Strike, Spare(7), Regular(9,0), Strike, Regular(0,8), Spare(8), Regular(0,6), Strike, Strike, Strike, Bonus(8,1))
```

####3. Calculating - `BowlingScoreCalculator`

Once we have parsed into this structure, the calculator turns out to be pretty simple to implement with a single pattern match. The match travels through the list matching its `head`, scoring it and then adding the score of the `tail` recursively. The nice thing about this is that we can "look ahead" in the list if necessary. 

Let's look at each of these matches individually:

```scala
def score(frames: List[Frame]): Int = frames match {
  case (f: Regular) :: remain   => f.balls.sum + score(remain)
  case (f: Spare)   :: remain   => f.balls.sum + balls(remain).head + score(remain)
  case (f@ Strike)  :: remain   => f.balls.sum + balls(remain).take(2).sum + score(remain)
  case Bonus(_, _)  :: _  | Nil => 0
}
```
First, we check if this frame is a `Regular`. If so, we add the points from both balls, plus the score of the remaining frames.

Next, we handle `Spare` frames by awarding the points from both balls, plus the points of the first ball in the remaining frames, plus the score of the remaining frames. `Strike` frames are handled in a very similar way, but include the score of the next *two* balls.

Lastly, we check if we have been passed an empty list (`Nil`), or if we have reached the `Bonus` balls. In both of these cases, we award `0` points (as bonus balls don't directly affect the score), and then finish the recursion.

But how do we work out the score of the next balls? More pattern matching!

```scala
def balls(frames: List[Frame]): Stream[Int] = frames match {
  case frame :: remain => frame.balls #::: balls(remain)
  case Nil             => Stream.Empty
}
```

We traverse the list of frames to merge the balls from each frame recursively. To save recomputing the whole set of balls repeatedly, we use a `Stream`, which is lazily calculated.
