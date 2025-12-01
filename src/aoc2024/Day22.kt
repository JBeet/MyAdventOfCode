package aoc2024

import utils.longs
import utils.read2024
import utils.verify

fun main() {
    val testInput = read2024("Day22_test")
    verify(37327623, Day22.part1(testInput))
    val input = read2024("Day22")
    println(Day22.part1(input))
    val testInput2 = read2024("Day22_test2")
    verify(23, Day22.part2(testInput2))
    println(Day22.part2(input))
}

object Day22 {
    fun part1(input: String): Long =
        input.longs().sumOf { secretNumbers(it).take(2001).last() }

    private fun secretNumbers(base: Long) = generateSequence(base, ::nextSecretNumber)

    private fun nextSecretNumber(base: Long): Long {
        val mul64 = pruneAndMix(base, base * 64)
        val div32 = pruneAndMix(mul64, mul64 / 32)
        val mul2048 = pruneAndMix(div32, div32 * 2048)
        return mul2048
    }

    private fun pruneAndMix(a: Long, b: Long): Long = (a xor b) % 16777216

    fun part2(input: String): Int {
        val baseNumbers = input.longs()
        val prices = baseNumbers.map { base ->
            secretNumbers(base).take(2001).map { (it % 10).toInt() }.toList()
        }
        val priceDifferencesWithPrice: List<Map<List<Int>, Int>> = prices.map { priceList ->
            priceList.windowed(5).map { prices ->
                val (p0, p1, p2, p3, p4) = prices
                listOf(p1 - p0, p2 - p1, p3 - p2, p4 - p3) to p4
            }.groupBy { it.first }.mapValues { it.value.first().second }
        }
        val priceDifferences = priceDifferencesWithPrice.flatMapTo(mutableSetOf()) { it.keys }
        val bestDifference = priceDifferences.maxBy { diff ->
            priceDifferencesWithPrice.sumOf { it[diff] ?: 0 }
        }
        println(bestDifference)
        return priceDifferencesWithPrice.sumOf { it[bestDifference] ?: 0 }
    }
}
