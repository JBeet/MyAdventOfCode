package aoc2025

import utils.*

fun main() {
    Day09().solve()
}

class Day09 {
    fun solve() {
        val testInput = read2025("Day09_test")
        assertEquals(50, part1(testInput))
        val input = read2025("Day09")
        println(part1(input))
        assertEquals(24, part2(testInput))
        println(part2(input))

    }

    private fun part1(input: String): Long = parse(input).combinationsOfSize(2)
        .map { (a, b) -> Square(a, b) }
        .maxOf { it.size }

    private fun part2(input: String): Long = parse(input).let { corners ->
        val lines = corners.zipWithNext() + (corners.last() to corners.first())
        corners.combinationsOfSize(2)
            .map { (a, b) -> Square(a, b) }
            .filter { square -> lines.none { it in square } }
            .maxOf { it.size }
    }

    data class Square(val minRow: Int, val maxRow: Int, val minColumn: Int, val maxColumn: Int) {
        constructor(a: Position, b: Position) : this(
            minOf(a.row, b.row), maxOf(a.row, b.row),
            minOf(a.column, b.column), maxOf(a.column, b.column)
        )

        val size: Long = (maxRow - minRow + 1).toLong() * (maxColumn - minColumn + 1).toLong()

        operator fun contains(line: Pair<Position, Position>): Boolean {
            val (a, b) = line
            if (a.row <= minRow && b.row <= minRow) return false
            if (a.row >= maxRow && b.row >= maxRow) return false
            if (a.column <= minColumn && b.column <= minColumn) return false
            if (a.column >= maxColumn && b.column >= maxColumn) return false
            return true
        }
    }

    private fun parse(input: String): List<Position> =
        input.lines().map { it.ints() }.map { (c, r) -> Position(r, c) }
}
