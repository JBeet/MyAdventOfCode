package aoc2024

import utils.CharGrid
import utils.Position
import utils.read2024
import utils.verify

fun main() {
    val testInput = read2024("Day21_test")
    verify(126384, Day21.part1(testInput))
    println("Test input OK")
    val input = read2024("Day21")
    val part1 = Day21.part1(input)
    println(part1)
    verify(182844, part1)
    println(Day21.part2(input))
}

object Day21 {
    enum class Directional {
        UP, RIGHT, DOWN, LEFT, ACTION;

        companion object {
            operator fun get(char: Char) = when (char) {
                '^' -> UP
                '>' -> RIGHT
                'v' -> DOWN
                '<' -> LEFT
                'A' -> ACTION
                else -> error("Unsupported: $char")
            }
        }
    }

    private val numericKeypad = CharGrid("789\n456\n123\nX0A").nonEmptyCells.associate { (pos, char) -> char to pos }
    private val directionalKeypad =
        CharGrid("X^A\n<v>").nonEmptyCells.filter { it.second != 'X' }
            .associate { (pos, char) -> Directional[char] to pos }

    fun part1(input: String): Long {
        val solution = input.lines().associateWith { solvePart1(it) }
        println(solution)
        return solution.map { (code, length) ->
            check(code.endsWith('A'))
            val numericPart = code.dropLast(1)
            check(numericPart.all { it.isDigit() })
            numericPart.toInt(10) * length
        }.sum()
    }

    fun part2(input: String): Long {
        val solution = input.lines().associateWith { solvePart2(it) }
        println(solution)
        return solution.map { (code, length) ->
            check(code.endsWith('A'))
            val numericPart = code.dropLast(1)
            check(numericPart.all { it.isDigit() })
            numericPart.toInt(10) * length
        }.sum()
    }

    private fun solvePart1(target: String): Long = Solver(target, 2).solve()
    private fun solvePart2(target: String): Long = Solver(target, 25).solve()

    class Solver(target: String, private val targetDepth: Int) {
        private val sequences = numericRobotSequences(target).toList()
        fun solve(): Long = sequences.minOf { recurseDirectional(it, 0) }

        private fun recurseDirectional(nrs: List<Directional>, depth: Int): Long =
            if (depth == targetDepth)
                nrs.size.toLong()
            else {
                val moves = findMoves(nrs)
                moves.sumOf { moveCount(it.first, it.second, depth) }
            }

        private fun findMoves(moves: List<Directional>): List<Pair<Position, Position>> =
            (listOf(Directional.ACTION) + moves).map { directionalKeypad.getValue(it) }.zipWithNext()

        data class MoveMemory(val from: Position, val to: Position, val depth: Int)

        private val memory = mutableMapOf<MoveMemory, Long>()

        private fun moveCount(from: Position, to: Position, depth: Int) =
            memory.getOrPut(MoveMemory(from, to, depth)) {
                findMoveCount(from, to, depth)
            }

        private fun findMoveCount(from: Position, to: Position, depth: Int): Long {
            val options = directionalMoveOptions(from, to)
            check(options.isNotEmpty()) { "No moves found for $from to $to" }
            return options.minOf { lower ->
                recurseDirectional(lower, depth + 1)
            }
        }

        private fun directionalMoveOptions(from: Position, to: Position): List<List<Directional>> =
            if (from == to)
                listOf(listOf(Directional.ACTION))
            else {
                val hMoves = hMoves(from, to)
                val vMoves = vMoves(from, to)
                buildList {
                    if (to != directionalKeypad[Directional.LEFT])
                        add((hMoves + vMoves) + Directional.ACTION)
                    if (from != directionalKeypad[Directional.LEFT])
                        add((vMoves + hMoves) + Directional.ACTION)
                }.distinct()
            }

        private fun numericRobotSequences(target: String) =
            findSequenceInNumeric(("A$target").map { numericKeypad.getValue(it) }.zipWithNext())

        private fun findSequenceInNumeric(moves: List<Pair<Position, Position>>): Sequence<List<Directional>> {
            val options = moves.map { (from, to) ->
                val hMoves = hMoves(from, to)
                val vMoves = vMoves(from, to)
                buildList {
                    if (from.row != 3 || to.column != 0)
                        add((hMoves + vMoves) + Directional.ACTION)
                    if (to.row != 3 || from.column != 0)
                        add((vMoves + hMoves) + Directional.ACTION)
                }.distinct()
            }
            check(options.all { it.isNotEmpty() })
            return sequencesFor(options, 0, emptyList()).map { it.flatten() }
        }

        private fun <E> sequencesFor(lists: List<List<E>>, index: Int, active: List<E>): Sequence<List<E>> {
            if (index >= lists.size) return sequenceOf(active)
            val cur = lists[index]
            if (cur.size <= 1)
                return sequencesFor(lists, index + 1, active + cur.single())
            return cur.asSequence().flatMap { option ->
                sequencesFor(lists, index + 1, active + option)
            }
        }

        private fun hMoves(from: Position, to: Position) = when {
            from.column < to.column -> List(to.column - from.column) { Directional.RIGHT }
            else -> List(from.column - to.column) { Directional.LEFT }
        }

        private fun vMoves(from: Position, to: Position) = when {
            from.row < to.row -> List(to.row - from.row) { Directional.DOWN }
            else -> List(from.row - to.row) { Directional.UP }
        }
    }
}
