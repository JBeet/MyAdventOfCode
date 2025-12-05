package aoc2025

import utils.assertEquals
import utils.longs
import utils.paragraphs
import utils.read2025

fun main() {
    Day05().solve()
}

class Day05 {
    fun solve() {
        val testInput = read2025("Day05_test")
        assertEquals(3, part1(testInput))
        val input = read2025("Day05")
        println(part1(input))
        assertEquals(14, part2(testInput))
        println(part2(input))
    }

    data class Cafeteria(val ranges: List<LongRange>, val ingredients: List<Long>) {
        constructor(input: String) : this(input.paragraphs())
        constructor(paragraphs: List<String>) : this(paragraphs.first(), paragraphs.drop(1).single())
        constructor(ranges: String, ingredients: String) : this(ranges.lines().map { it.split('-') }
            .map { (first, last) -> first.toLong()..last.toLong() }, ingredients.longs())

        fun countFreshKnown(): Int = ingredients.count { ingredient -> ranges.any { range -> ingredient in range } }
        fun countFreshAll(): Long = ranges.compact().sumOf { it.last - it.first + 1 }

        private fun List<LongRange>.compact(): List<LongRange> =
            this.sortedBy { it.first }.fold(emptyList()) { acc, current ->
                when {
                    acc.isEmpty() -> acc + listOf(current)
                    acc.last().last < current.first -> acc + listOf(current)
                    current.last <= acc.last().last -> acc
                    else -> acc.dropLast(1) + listOf(acc.last().first..current.last())
                }
            }
    }

    private fun part1(input: String): Int = Cafeteria(input).countFreshKnown()
    private fun part2(input: String): Long = Cafeteria(input).countFreshAll()
}
