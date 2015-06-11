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
sealed trait Frame
case class Regular(ball1: Int, ball2: Int) extends Frame
case class Bonus(ball1: Int, ball2: Int) extends Frame
case class Spare(ball1: Int) extends Frame
case object Strike extends Frame

```

This will be quite convinient when calculating the score, as different types of frames have different numbers of balls and different rules for how they are scored. 

####2. Parsing - `BowlingScoreParser` 
For parsing, Scala a has very nice functional parsing library called *parser combinators*. This allows us to build complex parsers out of combinations of simple ones. 

We start with parsers for our basic points. A point is either a `miss` or a `number`:

```scala
def point: Parser[Int] = miss | number
```

A `miss` is simply a `-` character, and should be treated as a score of 0. Otherwise, we expect a point to be `number`, ie. a integer between 1 and 9:

```scala
def miss: Parser[Int] = "-" ^^^ 0
def number: Parser[Int] = "[1-9]".r ^^ (_.toInt)
```

From this, we can build a general parser for frames. This parses `X` as a `Strike`, a point followed by a `/` as a `Spare`, and two normal points as a `Regular`:

```scala
def frame: Parser[Frame] =
  (point ~ point) ^^  { case p1 ~ p2 => Regular(p1, p2) } |
  (point <~ "/" ) ^^  { Spare(_) } |
  "X"             ^^^ { Strike }
```

We also need a way of parsing the bonus balls. These are different from a normal frame because:

* They do not increase the score themselves, only influence the score of the last frame
* They may contain multiple strikes

They are therefore parsed as a `Bonus`. There are only specific combinations of bonus balls that are allowed.

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
