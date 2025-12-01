package aoc2024

import utils.CharGrid
import utils.paragraphs
import utils.read2024
import utils.verify

fun main() {
    val testInput = read2024("Day25_test")
    verify(3, Day25.part1(testInput))
    println("Test input OK")
    val input = read2024("Day25")
    println(Day25.part1(input))
}

object Day25 {
    fun part1(input: String): Int {
        val locksAndKeys = input.paragraphs().map { LockOrKey(CharGrid(it)) }
        val locks = locksAndKeys.filterIsInstance<LockOrKey.Lock>()
        val keys = locksAndKeys.filterIsInstance<LockOrKey.Key>()
        return locks.sumOf { lock -> keys.count { key -> key.fits(lock) } }
    }

    sealed interface LockOrKey {
        data class Lock(val height: Int, val pinHeights: List<Int>) : LockOrKey
        data class Key(val height: Int, val pinHeights: List<Int>) : LockOrKey {
            fun fits(lock: Lock): Boolean {
                check(lock.height == height)
                check(lock.pinHeights.size == pinHeights.size)
                return pinHeights.indices.all { index -> pinHeights[index] + lock.pinHeights[index] <= height }
            }
        }
    }

    private fun LockOrKey(charGrid: CharGrid): LockOrKey {
        if (charGrid.row(0).isEmpty)
            return Key(charGrid)
        check(charGrid.row(charGrid.height - 1).isEmpty)
        return Lock(charGrid)
    }

    private fun Lock(charGrid: CharGrid): LockOrKey.Lock =
        LockOrKey.Lock(charGrid.height, charGrid.columns.map { it.findAll('.').min() })

    private fun Key(charGrid: CharGrid): LockOrKey.Key =
        LockOrKey.Key(charGrid.height, charGrid.columns.map { charGrid.height - it.findAll('#').min() })
}
