package aoc2024

import utils.intLists
import utils.read2024
import kotlin.math.absoluteValue

fun main() {
    fun part1(input: String): Int {
        val lines = input.intLists()
        val nrs1 = lines.map { it[0] }.sorted()
        val nrs2 = lines.map { it[1] }.sorted()
        return nrs1.zip(nrs2).sumOf { (a, b) -> (a - b).absoluteValue }
    }

    fun part2(input: String): Long {
        val lines = input.intLists()
        val nrs1 = lines.map { it[0] }
        val nrs2 = lines.map { it[1] }.groupingBy { it }.eachCount()
        return nrs1.sumOf { it.toLong() * (nrs2[it] ?: 0) }
    }

    // Or read a large test input from the `src/Day01_test.txt` file:
    val testInput = read2024("Day01_test")
    check(part1(testInput) == 11)

    // Read the input from the `src/Day01.txt` file.
    val input = read2024("Day01")
    println(part1(input))
    check(part2(testInput) == 31L)
    println(part2(input))
}
