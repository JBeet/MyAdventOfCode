package aoc2024

import utils.*

fun main() {
    val testInput = read2024("Day15_test")
    verify(10092, Day15.part1(testInput))
    val input = read2024("Day15")
    println(Day15.part1(input))
    verify(9021, Day15.part2(testInput))
    println(Day15.part2(input))
}

object Day15 {
    fun part1(input: String): Int {
        val (grid, moves) = parse(input)
        val result = moves.fold(grid) { g, ch -> g.applyMove(ch) }
        return result.findAll('O').sumOf { pos -> pos.row * 100 + pos.column }
    }

    fun part2(input: String): Int {
        val changed = input.replace("#", "##").replace("O", "[]").replace(".", "..").replace("@", "@.")
        val (grid, moves) = parse(changed)
        val result = moves.fold(grid) { g, ch -> g.applyMove(ch) }
        return result.findAll('[').sumOf { pos -> pos.row * 100 + pos.column }
    }

    private fun parse(input: String): Pair<BoxGrid, String> {
        val (puzzle, moves) = input.paragraphs()
        val bounds = puzzle.calculateBounds()
        val cells = openCharCells(puzzle.lines())
        val grid = BoxGrid(cells, bounds)
        return grid to moves
    }

    class BoxGrid(cells: Map<Position, Char>, bounds: ZeroBasedBounds) : OpenCharGrid(cells, bounds) {
        fun applyMove(ch: Char): BoxGrid = when (ch) {
            '<' -> applyMove(Direction.W)
            '>' -> applyMove(Direction.E)
            'v' -> applyMove(Direction.S)
            '^' -> applyMove(Direction.N)
            '\n' -> this
            else -> TODO("unknown: '$ch'")
        }

        private fun applyMove(dir: Direction): BoxGrid {
            val changes = ChangeBuilder(dir).build()
            return if (changes.isEmpty()) this else BoxGrid(cells + changes, bounds)
        }

        inner class ChangeBuilder(private val dir: Direction) {
            private val changes = mutableMapOf<Position, Char>()

            fun build(): Map<Position, Char> {
                val curPos = find('@')
                changes[curPos] = empty
                return if (canMove(curPos)) changes else emptyMap()
            }

            private fun canMove(curPos: Position): Boolean {
                val nextPos = curPos + dir
                changes[nextPos] = get(curPos)
                return when (val nextCell = get(nextPos)) {
                    empty -> true
                    '#' -> false
                    'O' -> canMove(nextPos)
                    '[' -> canMove(nextPos) && canMoveOther(nextPos + Direction.E)
                    ']' -> canMove(nextPos) && canMoveOther(nextPos + Direction.W)
                    else -> TODO("unhandled: $nextCell")
                }
            }

            private fun canMoveOther(otherPartPos: Position): Boolean {
                if (dir.isEW || otherPartPos in changes) return true
                changes[otherPartPos] = empty
                return canMove(otherPartPos)
            }
        }
    }
}
