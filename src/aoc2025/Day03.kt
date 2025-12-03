package aoc2025

import utils.assertEquals
import utils.cached2
import utils.read2025


fun main() {
    Day03().solve()
}

class Day03 {
    fun solve() {
        val testInput = read2025("Day03_test")
        assertEquals(357, part1(testInput))
        val input = read2025("Day03")
        println(part1(input))
        assertEquals(3121910778619, part2(testInput))
        println(part2(input))
    }

    fun part1(input: String): Long = input.lines().sumOf { bank -> parseBank(bank).maxOutput(2) }
    fun part2(input: String) = input.lines().sumOf { bank -> parseBank(bank).maxOutput(12) }
    fun parseBank(s: String): Bank = Bank(s.toCharArray().map { it.digitToInt() })
    data class Bank(val digits: List<Int>) {
        fun maxOutput(outputSize: Int): Long = maxOutput(outputSize to digits.size)
        val maxOutput = cached2 { outputSize: Int, inputSize: Int ->
            when {
                outputSize > inputSize -> -1L
                outputSize == 0 -> 0L
                else -> (0..<inputSize).maxOf { idx -> this((outputSize - 1) to idx) * 10 + digits[idx] }
            }
        }
    }
}
