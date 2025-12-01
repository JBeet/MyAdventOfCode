package aoc2024

import utils.*

fun main() {
    val testInput = read2024("Day05_test")
    verify(143, Day05(testInput).part1())
    val input = read2024("Day05")
    println(Day05(input).part1())
    verify(123, Day05(testInput).part2())
    println(Day05(input).part2())
}

class Day05(private val orderRules: List<Pair<String, String>>, private val updates: List<List<String>>) {
    constructor(input: String) : this(input.lines())
    constructor(input: List<String>) : this(
        input.sublistBefore("").map { it.split('|') }.map { (before, after) -> before to after },
        input.sublistAfter("").map { it.split(',') })

    fun part1(): Int = updates.filter { it.matchesRules() }.sumOf { middle(it) }
    private fun middle(it: List<String>) = it[it.size / 2].toInt()
    fun part2(): Int = updates.filterNot { it.matchesRules() }.map { fix(it) }.sumOf { middle(it) }
    private fun fix(pages: List<String>): List<String> {
        val problem = orderRules.firstOrNull { !pages.matchesRule(it.first, it.second) } ?: return pages
        return fix(pages.toMutableList().apply {
            swapItem(problem.first, problem.second)
        })
    }

    private fun List<String>.matchesRules(): Boolean = orderRules.all { matchesRule(it.first, it.second) }
    private fun List<String>.matchesRule(before: String, after: String): Boolean {
        val a = indexOf(after)
        return (a < 0) || indexOf(before) < a
    }
}
