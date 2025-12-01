package aoc2024

import utils.*

fun main() {
    val testInput = read2024("Day16_test")
    verify(7036, Day16.part1(testInput))
    println("Test input OK")
    val input = read2024("Day16")
    println(Day16.part1(input))
    verify(45, Day16.part2(testInput))
    println(Day16.part2(input))
}

object Day16 {
    data class Step(val position: Position, val direction: Direction)

    fun part1(input: String): Int {
        return findPath(input).score
    }

    data class Path(val score: Int, val positions: Set<Position>)

    private fun findPath(input: String): Path {
        val grid = CharGrid(input)
        val startingPosition = grid.find('S')
        val start = Step(startingPosition, Direction.E)
        val endPoint = grid.find('E')
        val steps = mutableMapOf(start to Path(0, setOf(startingPosition)))
        val visited = mutableSetOf<Step>()
        while (steps.isNotEmpty()) {
            val (step, path) = steps.minBy { it.value.score }
            if (step.position == endPoint) return path
            check(step.position in grid)
            visited.add(step)
            steps.remove(step)
            val nextPosition = step.position + step.direction
            if (grid[nextPosition] != '#') {
                val nextStep = Step(nextPosition, step.direction)
                steps.compute(nextStep) { _, oldPath ->
                    val newScore = path.score + 1
                    if (oldPath == null || oldPath.score > newScore)
                        Path(newScore, path.positions + nextPosition)
                    else if (oldPath.score == newScore)
                        Path(newScore, oldPath.positions + path.positions + nextPosition)
                    else
                        error("Unexpected $oldPath vs $newScore $nextStep")
                }
            }
            Direction.entries.forEach { dir ->
                val nextStep = Step(step.position, dir)
                if (nextStep !in visited) {
                    steps.compute(nextStep) { _, oldPath ->
                        val newScore = path.score + 1000
                        if (oldPath == null || oldPath.score > newScore)
                            Path(newScore, path.positions)
                        else if (oldPath.score == newScore)
                            Path(newScore, oldPath.positions + path.positions)
                        else
                            oldPath
                    }
                }
            }
        }
        error("No path found")
    }

    fun part2(input: String): Int {
        val positions = findPath(input).positions
        return positions.size
    }
}
