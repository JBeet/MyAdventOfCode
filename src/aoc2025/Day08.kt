package aoc2025

import aoc2025.Day08.Coordinates
import utils.assertEquals
import utils.ints
import utils.read2025

private val Int.squared: Long get() = this.toLong() * this.toLong()

fun main() {
    Day08().solve()
}

class Day08 {
    fun solve() {
        val testInput = read2025("Day08_test")
        assertEquals(40, part1(testInput, 10))
        val input = read2025("Day08")
        println(part1(input, 1000))
        assertEquals(25272, part2(testInput))
        println(part2(input))
    }

    data class Coordinates(val x: Int, private val y: Int, private val z: Int) {
        fun squaredDistanceTo(o: Coordinates): Long =
            (x - o.x).squared + (y - o.y).squared + (z - o.z).squared
    }

    private fun part1(input: String, count: Int): Long =
        parse(input).apply {
            squaredDistances.take(count).forEach { (a, b) ->
                addConnection(a, b)
            }
        }.circuitsBySize().take(3).map { it.second.size.toLong() }.reduce(Long::times)

    private fun parse(input: String): JunctionBoxes =
        JunctionBoxes(input.lines().map { it.ints() }.map { (x, y, z) -> Coordinates(x, y, z) })

    private fun part2(input: String): Long {
        parse(input).run {
            squaredDistances.forEach { (a, b) ->
                addConnection(a, b)
                if (circuits.size == 1) return positions[a].x.toLong() * positions[b].x
            }
        }
        return -1L
    }
}


private class JunctionBoxes(val positions: List<Coordinates>) {
    val circuitIndex = positions.indices.associateWith { it }.toMutableMap()
    val circuits = positions.indices.associateWith { setOf(it) }.toMutableMap()
    val squaredDistances: List<Pair<Int, Int>> by lazy {
        positions.indices.flatMap { i ->
            val a = positions[i]
            positions.indices.drop(i + 1).map { j ->
                val b = positions[j]
                (i to j) to (a.squaredDistanceTo(b))
            }
        }.sortedBy { it.second }.map { it.first }
    }

    fun circuitsBySize(): List<Pair<Int, Set<Int>>> =
        circuits.toList().sortedByDescending { (_, nodes) -> nodes.size }

    fun addConnection(a: Int, b: Int) {
        val ca = circuitIndex.getValue(a)
        val cb = circuitIndex.getValue(b)
        if (ca == cb) return
        val combinedCircuit = minOf(ca, cb)
        val removedCircuit = maxOf(ca, cb)
        val nodesForRemovedCircuit = circuits.getValue(removedCircuit)
        nodesForRemovedCircuit.forEach { circuitIndex[it] = combinedCircuit }
        circuits[combinedCircuit] = circuits.getValue(combinedCircuit) + nodesForRemovedCircuit
        circuits.remove(removedCircuit)
    }
}
