package aoc2025

import utils.assertEquals
import utils.read2025

fun main() {
    fun parse(input: String): List<LongRange> =
        input.split(',')
            .map { range -> range.split('-').map { nr -> nr.toLong() } }
            .map { (from, to) -> from..to }

    fun isInvalid(nr: String, groupCount: Int): Boolean =
        (nr.length % groupCount == 0) && nr.chunked(nr.length / groupCount).distinct().size == 1

    fun isInvalid(nr: String): Boolean =
        (2..nr.length).any { isInvalid(nr, it) }

    fun part1(input: String): Long =
        parse(input).sumOf { range -> range.filter { isInvalid(it.toString(), 2) }.sum() }

    fun part2(input: String): Long =
        parse(input).sumOf { range -> range.filter { isInvalid(it.toString()) }.sum() }

    // Or read a large test input from the `src/Day01_test.txt` file:
    val testInput = read2025("Day02_test")
    assertEquals(1227775554, part1(testInput))

    // Read the input from the `src/Day01.txt` file.
    val input = read2025("Day02")
    println(part1(input))
    assertEquals(4174379265, part2(testInput))
    println(part2(input))
}
