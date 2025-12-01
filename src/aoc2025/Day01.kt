package aoc2025

import utils.assertEquals
import utils.read2025

fun main() {
    fun rotateLeft(start: Int, amount: Int): Int = (start - amount)
    fun rotateRight(start: Int, amount: Int): Int = (start + amount)
    fun rotate(start: Int, rotation: String): Int =
        if (rotation[0] == 'L')
            rotateLeft(start, rotation.drop(1).toInt())
        else
            rotateRight(start, rotation.drop(1).toInt())

    fun part1(input: String): Int =
        input.lines().runningFold(50) { position, rotation -> rotate(position, rotation) % 100 }.count { it == 0 }

    fun countClicks(position: Int, newPosition: Int): Int =
        when {
            newPosition >= 100 -> newPosition / 100
            newPosition < 0 && position == 0 -> -newPosition / 100
            newPosition < 0 -> 1 + (-newPosition / 100)
            position != 0 && newPosition == 0 -> 1
            else -> 0
        }

    fun part2(input: String): Int =
        input.lines().fold(50 to 0) { (position, count), rotation ->
            val newPosition = rotate(position, rotation)
            (newPosition.mod(100)) to (count + countClicks(position, newPosition))
        }.second

    // Or read a large test input from the `src/Day01_test.txt` file:
    val testInput = read2025("Day01_test")
    assertEquals(3, part1(testInput))

    // Read the input from the `src/Day01.txt` file.
    val input = read2025("Day01")
    println(part1(input))
    assertEquals(6, part2(testInput))
    assertEquals(3, part2("L50\nR1\nL1\nR1\nL1"))
    assertEquals(4, part2("L50\nR1\nL101\nR1\nL1"))
    println(part2(input))
}
