package aoc2025

import utils.*

fun main() {
    Day04().solve()
}

class Day04 {
    fun solve() {
        val testInput = read2025("Day04_test")
        assertEquals(13, part1(testInput))
        val input = read2025("Day04")
        println(part1(input))
        assertEquals(43, part2(testInput))
        println(part2(input))
    }

    private fun part1(input: String): Int = CharGrid(input).countNonEmpty { canBeRemoved(it) }

    private fun Grid<Char>.canBeRemoved(position: Position): Boolean =
        Direction8.entries.map { position + it }.count { this[it] == '@' } < 4

    private fun part2(input: String): Int =
        CharGrid(input).removeRecursively()

    private fun CharGrid.removeRecursively() =
        removeRecursively(findPositionsToRemove())

    private fun CharGrid.findPositionsToRemove(): List<Position> =
        nonEmptyCells.filter { (pos, _) -> canBeRemoved(pos) }.map { it.first }.toList()

    private fun CharGrid.removeRecursively(positionsToRemove: List<Position>): Int =
        if (positionsToRemove.isEmpty())
            0
        else
            positionsToRemove.size + clearCells(positionsToRemove).removeRecursively()
}
