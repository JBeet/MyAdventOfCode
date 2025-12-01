package aoc2024

import utils.read2024
import utils.verify

fun main() {
    val testInput = read2024("Day04_test")
    val day04test = Day04(testInput)
    verify(18L, day04test.part1())

    val input = read2024("Day04")
    val day04 = Day04(input)
    println(day04.part1())

    verify(9L, day04test.part2())
    println(day04.part2())
}


private fun String.charList() = toCharArray().toList()

class Day04(private val grid: List<List<Char>>) {
    constructor(input: String) : this(input.lines().filter { it.isNotBlank() }.map { it.charList() })

    fun part1(): Int =
        findHorizontally() + findVertically() + findDiagonallyLR() + findDiagonallyRL()

    private fun findHorizontally(): Int = find { (x, y) -> x + 1 to y }
    private fun findVertically(): Int = find { (x, y) -> x to y + 1 }
    private fun findDiagonallyLR(): Int = find { (x, y) -> x + 1 to y + 1 }
    private fun findDiagonallyRL(): Int = find { (x, y) -> x + 1 to y - 1 }

    private fun find(direction: (Pair<Int, Int>) -> Pair<Int, Int>): Int =
        find("XMAS", direction) + find("SAMX", direction)

    private fun find(word: String, direction: (Pair<Int, Int>) -> Pair<Int, Int>): Int =
        grid.indices.sumOf { y -> grid[y].indices.count { x -> match(word, x, y, direction) } }

    private fun match(word: String, x: Int, y: Int, direction: (Pair<Int, Int>) -> Pair<Int, Int>): Boolean {
        if (word.isEmpty()) return true
        if (!check(word[0], x, y)) return false
        val (newX, newY) = direction(x to y)
        return match(word.drop(1), newX, newY, direction)
    }

    private fun check(ch: Char, x: Int, y: Int): Boolean =
        (y in grid.indices && x in grid[y].indices && grid[y][x] == ch)

    fun part2() =
        grid.indices.sumOf { y -> grid[y].indices.count { x -> match2(x, y) } }

    private fun match2(x: Int, y: Int): Boolean =
        match2("MMASS", x, y) || match2("SSAMM", x, y) || match2("MSAMS", x, y) || match2("SMASM", x, y)

    private fun match2(word: String, x: Int, y: Int): Boolean =
        check(word[0], x, y) && check(word[1], x + 2, y) &&
                check(word[2], x + 1, y + 1) &&
                check(word[3], x, y + 2) && check(word[4], x + 2, y + 2)
}
