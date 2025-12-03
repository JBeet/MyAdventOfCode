package aoc2025

import utils.cached
import utils.read2025

fun main() {
    fun String.isRepeating(len: Int): Boolean = chunked(len).distinct().size == 1
    val counts = cached { len: Int -> (2..len).filter { len % it == 0 } }
    val ranges = read2025("Day02")
        .split(",")
        .map { s -> s.split("-").map(String::toLong) }
        .map { (from, to) -> from..to }
        .sortedBy { it.first }
    val validation = ranges.flatten()
        .associateWith { it.toString() }
        .mapValues { (_, s) -> counts.getValue(s.length).filter { s.isRepeating(s.length / it) } }
    val part1 = validation.filterValues { 2 in it }.keys.sum()
    val part2 = validation.filterValues { it.isNotEmpty() }.keys.sum()
    println(part1)
    println(part2)
}
