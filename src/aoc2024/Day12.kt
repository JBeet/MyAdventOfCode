package aoc2024

import utils.*

fun main() {
    val testInput = read2024("Day12_test")
    verify(1930, Day12(testInput).part1())
    val input = read2024("Day12")
    println(Day12(input).part1())
    verify(1206, Day12(testInput).part2())
    println(Day12(input).part2())
}

class Day12(val grid: CharGrid) {
    constructor(input: String) : this(CharGrid(input))

    private val regions = grid.nonEmptyCells.groupBy({ it.second }, { it.first }).flatMap { (_, positions) ->
        positions.continuousAreas().map { Region(it) }
    }

    fun part1(): Int = regions.sumOf { it.regularPrice }
    fun part2(): Int = regions.sumOf { it.discountedPrice }
    override fun toString(): String = grid.toString()
}

class Region(private val positions: Collection<Position>) {
    private val area = positions.size
    private val perimeter = positions.sumOf { p -> p.neighbours4.count { n -> n !in positions } }
    val regularPrice = area * perimeter
    private val nrOfSides = directions.sumOf { dir ->
        positions.filter { pos -> (pos + dir) !in positions }.continuousAreas().size
    }
    val discountedPrice get() = area * nrOfSides
    override fun toString(): String = "Region $positions = $perimeter / $nrOfSides"
}
