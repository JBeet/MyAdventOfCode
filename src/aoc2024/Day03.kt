package aoc2024

import utils.read2024

fun main() {
    val test = """xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))"""
    check(Day03.part1(test) == 161)
    val input = read2024("Day03")
    println(Day03.part1(input))
    val test2 = """xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))"""
    check(Day03.part2(test2) == 48)
    println(Day03.part2(input))
}

object Day03 {
    fun part1(input: String): Int = Regex("""mul\((\d{1,3}),(\d{1,3})\)""").findAll(input).map { it.destructured }
        .sumOf { (a, b) -> a.toInt() * b.toInt() }

    fun part2(input: String): Int = input.split("do()").map { it.substringBefore("don't()") }.sumOf { part1(it) }
}
