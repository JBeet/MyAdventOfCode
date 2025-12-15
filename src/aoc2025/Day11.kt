package aoc2025

import utils.assertEquals
import utils.read2025

fun main() {
    Day11().solve()
}

class Day11 {
    fun solve() {
        val testInput = read2025("Day11_test")
        assertEquals(5, part1(testInput))
        val input = read2025("Day11")
        println(part1(input))
        val testInput2 = read2025("Day11_test2")
        assertEquals(2, part2(testInput2))
        println(part2(input))
    }

    data class Device(val id: String, val connections: List<String>)

    data class Path(val position: String, val dac: Boolean = false, val fft: Boolean = false)
    data class Reactor(val devices: Map<String, Device>) {
        fun countPaths(): Long {
            val result = calculatePaths("you")
            println(result)
            return result.filter { it.key.position == "out" }.values.sum()
        }

        private fun calculatePaths(start: String): Map<Path, Long> =
            generateSequence(mapOf(Path(start) to 1L)) { nextPaths(it) }
                .takeWhile { it.isNotEmpty() }
                .fold(emptyMap()) { acc, next ->
                    (acc.keys + next.keys).associateWith { key ->
                        acc.getOrDefault(key, 0L) + next.getOrDefault(key, 0L)
                    }
                }

        private tailrec fun countPaths(currentCounts: Map<Path, Long>): Map<Path, Long> =
            if (currentCounts.keys.none { it.position in devices })
                currentCounts
            else
                countPaths(nextPaths(currentCounts).also { println(it) })


        fun countDacFftPaths(): Long {
            val result = calculatePaths("svr")
            println(result)
            return result.filter { it.key.position == "out" && it.key.dac && it.key.fft }.values.sum()
        }

        private fun nextPaths(currentCounts: Map<Path, Long>): Map<Path, Long> = buildMap {
            currentCounts.forEach { (path, count) ->
                val device = devices[path.position]
                device?.connections?.forEach { connection ->
                    val newPath = when (path.position) {
                        "dac" -> path.copy(position = connection, dac = true)
                        "fft" -> path.copy(position = connection, fft = true)
                        else -> path.copy(position = connection)
                    }
                    compute(newPath) { _, total -> (total ?: 0L) + count }
                }
            }
        }
    }

    private fun part1(input: String): Long = parseReactor(input).countPaths()
    private fun part2(input: String): Long = parseReactor(input).countDacFftPaths()

    private fun parseReactor(input: String): Reactor {
        val devices = mutableMapOf<String, Device>()
        input.lines().forEach {
            val main = it.substringBefore(": ")
            val connections = it.substringAfter(": ").split(" ")
            devices.getOrPut(main) { Device(main, connections) }
        }
        return Reactor(devices)
    }
}
