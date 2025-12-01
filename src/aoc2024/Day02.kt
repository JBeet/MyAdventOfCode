package aoc2024

import utils.intLists
import utils.read2024
import kotlin.math.absoluteValue

fun main() {
    val testInput = read2024("Day02_test")
    check(Day02.part1(testInput) == 2) { "Expected 2 but was " + Day02.part1(testInput) }
    val input = read2024("Day02")
    println(Day02.part1(input))
    check(Day02.part2(testInput) == 4) { "Expected 4 but was " + Day02.part2(testInput) }
    println(Day02.part2(input))
}

object Day02 {
    fun part1(input: String): Int = input.intLists().count { it.isSafe() }
    fun part2(input: String) =
        input.intLists().count { original -> original.isSafe() || dampened(original).any { it.isSafe() } }

    fun dampened(list: List<Int>): List<List<Int>> = list.indices.map { list.toMutableList().apply { removeAt(it) } }
    private fun List<Int>.isSafe() =
        this.hasValidOrder() && zipWithNext { a, b -> (a - b).absoluteValue }.all { it in 1..3 }

    private fun List<Int>.hasValidOrder() = this == sorted() || this == sortedDescending()
}
