package aoc2024

import utils.*

fun main() {
    val testInput = read2024("Day13_test")
    verify(480, Day13.part1(testInput))
    val input = read2024("Day13")
    println(Day13.part1(input))
//    verify(1206, aoc2024.Day13.part2(testInput))
    println(Day13.part2(input))
}

object Day13 {
    fun Puzzle13(s: String): Puzzle13 {
        val (sA, sB, sP) = s.lines()
        val (aX, aY) = sA.longs()
        val (bX, bY) = sB.longs()
        val (pX, pY) = sP.longs()
        return Puzzle13(aX, aY, bX, bY, pX, pY)
    }

    fun parse(input: String) = input.paragraphs().map { Puzzle13(it) }
    fun part1(input: String) = parse(input).sumOf { it.minCost ?: 0 }
    fun part2(input: String) = parse(input).map { it.part2() }.sumOf { it.minCost ?: 0 }
}

data class Puzzle13(val aX: Long, val aY: Long, val bX: Long, val bY: Long, val pX: Long, val pY: Long) {
    private val solution = solve {
        variable("a") * aX + variable("b") * bX eq pX
        variable("a") * aY + variable("b") * bY eq pY
    }

    val minCost: Long?
        get() {
            val a = solution["a"].asLong() ?: return null
            val b = solution["b"].asLong() ?: return null
            return if (a >= 0 && b >= 0) 3 * a + b else null
        }

    fun part2() = copy(pX = pX + 10000000000000L, pY = pY + 10000000000000L)
}
