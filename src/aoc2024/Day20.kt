package aoc2024

import utils.*

fun main() {
    val testInput = read2024("Day20_test")
    verify(10, Day20(testInput).part1(10))
    val input = read2024("Day20")
    println(Day20(input).part1(100))
    verify(285, Day20(testInput).part2(50))
    println(Day20(input).part2(100))
}

class Day20(val grid: CharGrid) {
    constructor(input: String) : this(CharGrid(input))

    fun part1(minSaving: Int): Int {
        val basePath = findBasePath()
        val shortcuts = basePath.positions.flatMapIndexed { fromIndex, fromPos ->
            Direction.entries.mapNotNull { direction ->
                val wallPos = fromPos + direction
                if (grid[wallPos] != '#')
                    null
                else {
                    val toPos = wallPos + direction
                    val toIndex = basePath.positions.indexOf(toPos)
                    if (toIndex < fromIndex) // incl -1
                        null
                    else
                        wallPos to (toIndex - fromIndex - 2)
                }
            }
        }
        val savings = shortcuts.groupingBy { it.second }.eachCount()
        return savings.filter { it.key >= minSaving }.values.sum()
    }

    data class Cheat(val from: Position, val to: Position, val cheatDuration: Int, val savedTime: Int)

    fun part2(minSaving: Int): Int {
        val basePath = findBasePath()
        val maxCheat = 20
        val cheats = basePath.positions.flatMapIndexed { fromIndex, fromPos ->
            basePath.positions.mapIndexedNotNull { toIndex: Int, toPosition: Position ->
                val path = fromPos.manhattanDistanceTo(toPosition)
                val savedTime = toIndex - fromIndex - path
                if (path <= maxCheat && savedTime >= minSaving)
                    Cheat(fromPos, toPosition, path, savedTime)
                else
                    null
            }
        }
        val savings = cheats.groupingBy { it.savedTime }.eachCount().toSortedMap()
//        savings.forEach { (u, t) -> println("There are $t cheats that save $u picoseconds") }
        return savings.values.sum()
    }

    data class Path(val score: Int, val positions: List<Position>)

    private fun findBasePath(): Path =
        checkNotNull(findPath(grid.find('S'), grid.find('E'))) { "Path not found" }

    private fun findPath(start: Position, endPoint: Position): Path? {
        val steps = mutableMapOf(start to Path(0, listOf(start)))
        val visited = mutableSetOf<Position>()
        while (steps.isNotEmpty()) {
            val (step, path) = steps.minBy { it.value.score }
            if (step == endPoint) return path
            if (step !in grid) continue
            visited.add(step)
            steps.remove(step)
            Direction.entries.forEach { dir ->
                val nextStep = step + dir
                if (grid[nextStep] != '#' && nextStep !in visited) {
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
