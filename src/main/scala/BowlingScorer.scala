object BowlingScorer {
  def parseGame(game: String): Int = game.split('|').map(parseFrame).sum

  def parseFrame(frame: String): Int = frame.map(parsePoint).sum

  def parsePoint(point: Char): Int = point match {
    case '-' => 0
    case d if '1' <= d && d <= '9' => d.asDigit
  }
}
