class Day10 extends munit.FunSuite:

  /// core logic

  type Grid = Vector[Vector[Char]]
  type Position = (Int, Int)  // row, column
  extension (pos: Position)
    def row = pos(0)
    def column = pos(1)
    def up    = (pos(0) - 1, pos(1))
    def down  = (pos(0) + 1, pos(1))
    def left  = (pos(0), pos(1) - 1)
    def right = (pos(0), pos(1) + 1)
  extension (grid: Grid)
    def at(pos: Position): Char =
      if grid.indices.contains(pos.row) && grid.head.indices.contains(pos.column)
      then grid(pos.row)(pos.column)
      else '.'

  // returns the position along with the character the 'S' is
  // "concealing" -- the shape of that part of the pipe
  def startingPosition(grid: Grid): (Position, Char) =
    val row = grid.indexWhere(_.contains('S'))
    val cur = (row, grid(row).indexOf('S'))
    val entrances =
      (exits(grid, cur.up).productIterator.toSeq.asInstanceOf[Seq[Position]].contains(cur),
       exits(grid, cur.down).productIterator.toSeq.asInstanceOf[Seq[Position]].contains(cur),
       exits(grid, cur.left).productIterator.toSeq.asInstanceOf[Seq[Position]].contains(cur),
       exits(grid, cur.right).productIterator.toSeq.asInstanceOf[Seq[Position]].contains(cur)): @unchecked
    val concealed =
      (entrances: @unchecked) match
        case (true, true, false, false) => '|'
        case (true, false, true, false) => 'J'
        case (true, false, false, true) => 'L'
        case (false, true, true, false) => '7'
        case (false, true, false, true) => 'F'
        case (false, false, true, true) => '-'
    (cur, concealed)

  def exits(grid: Grid, cur: Position): (Position, Position) =
    grid.at(cur) match
      case '.' => (null, null)
      case '|' => (cur.up, cur.down)
      case '-' => (cur.left, cur.right)
      case 'L' => (cur.up, cur.right)
      case 'J' => (cur.up, cur.left)
      case '7' => (cur.down, cur.left)
      case 'F' => (cur.down, cur.right)
      case 'S' =>
        val cands = Seq(cur.up, cur.down, cur.left, cur.right)
        val Seq(exit1, exit2) =
          cands.filter: next =>
            grid.at(next) != '.' && locally:
              val (ret1, ret2) = exits(grid, next)
              ret1 == cur || ret2 == cur
        (exit1, exit2)

  def nextSquare(grid: Grid, cur: Position, prev: Position): Position =
    val (exit1, exit2) = exits(grid, cur)
    if exit1 == prev
    then exit2
    else exit1

  def findPipe(grid: Grid): Set[Position] =
    val (start, _) = startingPosition(grid)
    val (exit1, exit2) = exits(grid, start)
    (Iterator
      .iterate((start, exit1)): (cur, prev) =>
        (nextSquare(grid, cur, prev), cur)
      .map(_(0))
      .drop(1)
      .takeWhile(_ != start)
      .toSet) + start

  /// reading & parsing

  def getInput(name: String): Grid =
    io.Source.fromResource(name)
      .getLines
      .map(_.toVector)
      .toVector

  /// part 1

  def part1(name: String): Int =
    val grid = getInput(name)
    findPipe(grid).size / 2

  test("part 1 sample"):
    assertEquals(part1("day10-sample.txt"), 8)
  test("part 1"):
    assertEquals(part1("day10.txt"), 6828)

  /// part 2

  def repair(grid: Grid): Grid =
    val (_, concealed) = startingPosition(grid)
    grid.map(row => row.map(cell => if cell == 'S' then concealed else cell))

  def part2(name: String): Int =
    val input = getInput(name)
    val grid = repair(input)
    var result = 0
    var rowNumber = 0
    val pipe = findPipe(input)
    for row <- grid do
      var state = (false, false)  // top half, bottom half
      var columnNumber = 0
      for cell2 <- row do
        val cell = if pipe((rowNumber, columnNumber)) then cell2 else '.'
        val next =
          cell match
            case '.' | '-' => (state(0),  state(0), state(1),  state(1))  // flow, flow
            case '|'       => (state(0), !state(0), state(1), !state(1))  // flip, flip
            case 'F' | '7' => (state(0),  state(0), state(1), !state(1))  // flow, flip
            case 'L' | 'J' => (state(0), !state(0), state(1),  state(1))  // flip, flow
        if next == (true, true, true, true) then
          result += 1
        state = (next(1), next(3))
        columnNumber += 1
      rowNumber += 1
    result

  test("part 2 sample"):
    assertEquals(part2("day10-sample.txt"), 1)
  test("part 2 sample 2a"):
    assertEquals(part2("day10-sample2a.txt"), 4)
  test("part 2 sample 2b"):
    assertEquals(part2("day10-sample2b.txt"), 4)
  test("part 2 sample 3"):
    assertEquals(part2("day10-sample3.txt"), 8)
  test("part 2 sample 4"):
    assertEquals(part2("day10-sample4.txt"), 10)
  test("part 2"):
    assertEquals(part2("day10.txt"), 459)

end Day10
