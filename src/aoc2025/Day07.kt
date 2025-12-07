package aoc2025

import utils.CharGrid
import utils.assertEquals
import utils.read2025

fun main() {
    Day07().solve()
}

class Day07 {
    fun solve() {
        val testInput = read2025("Day07_test")
        assertEquals(21, part1(testInput))
        val input = read2025("Day07")
        println(part1(input))
        assertEquals(40, part2(testInput))
        println(part2(input))
    }

    private data class BeamSplitStatus(val beams: Map<Int, Long>, val splitCount: Long) {
        constructor(start: Int) : this(mapOf(start to 1L), 0)

        val worldCount: Long get() = beams.values.sum()
    }

    private fun part1(input: String): Long = process(input).splitCount

    private fun part2(input: String): Long = process(input).worldCount

    private fun process(input: String): BeamSplitStatus {
        val grid = CharGrid(input)
        val start = grid.find('S').column
        return grid.rows
            .map { it.findAll('^') }
            .fold(BeamSplitStatus(start)) { status, splitPoints ->
                split(status, splitPoints)
            }
    }

    private fun split(status: BeamSplitStatus, splitPoints: Set<Int>): BeamSplitStatus {
        val matching = status.beams.keys.intersect(splitPoints)
        val other = status.beams - matching
        val newBeams = other.toMutableMap()
        matching.forEach {
            newBeams[it - 1] = newBeams.getOrDefault(it - 1, 0L) + status.beams.getValue(it)
            newBeams[it + 1] = newBeams.getOrDefault(it + 1, 0L) + status.beams.getValue(it)
        }
        return BeamSplitStatus(newBeams, status.splitCount + matching.size)
    }
}
