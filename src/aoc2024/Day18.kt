package aoc2024

import utils.*
import kotlin.time.measureTime

fun main() {
    val testInput = read2024("Day18_test")
    verify(22, Day18.part1(testInput, 12, 7))
    println("Test input OK")
    val input = read2024("Day18")
    println(Day18.part1(input, 1024, 71))
    verify("6,1", Day18.part2(testInput, 7))
    println("Test input OK")
    println(Day18.part2(input, 71))
}

object Day18 {
    fun part1(input: String, time: Int, size: Int): Long {
        val positions: List<Position> = input.lines().map { it.intsOrZero() }.map { (x, y) -> Position(y, x) }
        val grid = ByteGrid(positions.take(time), size, size)
        val path = requireNotNull(grid.findPath()) { "No path for $time" }
        return path.score.toLong()
    }

    fun part2(input: String, size: Int): String {
        val blocked: List<Position> = input.lines().map { it.intsOrZero() }.map { (x, y) -> Position(y, x) }
        timeIt(blocked, size)
        return solvePart2(blocked, size)
    }

    private fun timeIt(blocked: List<Position>, size: Int) {
        //warm up
        repeat(2_000) {
            solvePart2(blocked, size)
        }
        val iterations = 20_000
        measureTime {
            repeat(iterations) {
                solvePart2(blocked, size)
            }
        }.also { println("$iterations in $it time = ${it / iterations}") }
    }

    private fun solvePart2(blocked: List<Position>, size: Int): String {
        val blockMap = blocked.mapIndexed { index, position -> position to index }.toMap()
        val bounds = ZeroBasedBounds(size, size)
        val visited = mutableSetOf<Position>()
        val edges = mutableListOf<Int>()
        var lastEdge = blocked.size
        val target = Position(size - 1, size - 1)
        var lastRemovedPosition = target
        val queue = mutableListOf(Position(0, 0))
        while (target !in queue) {
            val active = queue.removeLast()
            visited.add(active)
            for (nextPosition in active.neighbours4) {
                if (nextPosition !in visited && nextPosition in bounds) {
                    val pos = blockMap.getOrDefault(nextPosition, -1)
                    if (pos in 0..<lastEdge)
                        edges.add(pos)
                    else
                        queue.add(nextPosition)
                }
            }
            if (queue.isEmpty()) {
                lastEdge = edges.max()
                edges.remove(lastEdge)
                lastRemovedPosition = blocked[lastEdge]
                queue.add(lastRemovedPosition)
            }
        }
        return lastRemovedPosition.column.toString() + "," + lastRemovedPosition.row
    }

    data class Path(val score: Int, val positions: Set<Position>)

    class ByteGrid(positions: List<Position>, val height: Int, val width: Int) :
        OpenGrid<Boolean>(positions.associateWith { true }, false, height, width) {

        fun findPath(): Path? {
            val grid = this
            val startingPosition = Position(0, 0)
            val start = startingPosition
            val endPoint = Position(height - 1, width - 1)
            val steps = mutableMapOf(start to Path(0, setOf(startingPosition)))
            val visited = mutableSetOf<Position>()
            while (steps.isNotEmpty()) {
                val (step, path) = steps.minBy { it.value.score }
                if (step == endPoint) return path
                if (step !in grid) continue
                visited.add(step)
                steps.remove(step)
                Direction.entries.forEach { dir ->
                    val nextStep = step + dir
                    if (nextStep in grid && !grid[nextStep] && nextStep !in visited) {
                        steps.compute(nextStep) { _, oldPath ->
                            val newScore = path.score + 1
                            if (oldPath == null || oldPath.score > newScore)
                                Path(newScore, path.positions + nextStep)
                            else
                                oldPath
                        }
                    }
                }
            }
            return null
        }
    }

}

