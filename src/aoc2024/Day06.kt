package aoc2024

import utils.*

fun main() {
    val testInput = read2024("Day06_test")
    verify(41, Day06(testInput).part1())
    val input = read2024("Day06")
    println(Day06(input).part1())
    verify(6, Day06(testInput).part2())
    println(Day06(input).part2())
}

class Day06(private val grid: FilledCharGrid) {
    constructor(input: String) : this(FilledCharGrid(input.lines()))

    fun part1(): Int {
        val directionMap = findPath()
        return directionMap.count { it.value.isNotEmpty() }
    }

    private fun findPath(): Map<Position, Set<Direction>> {
        val map = mutableMapOf<Position, Set<Direction>>()
        var pos = grid.find('^')
        var direction = Direction.N
        while (pos in grid) {
            val known = map[pos] ?: emptySet()
            if (direction !in known) {
                map[pos] = known + direction
                val nextPos = pos + direction
                if (grid[nextPos] == '#')
                    direction = direction.rotateCW
                else
                    pos = nextPos
            }
        }
        return map
    }

    private fun FilledCharGrid.hasCycle(): Boolean {
        val map = mutableMapOf<Position, Set<Direction>>()
        var pos = find('^')
        var direction = Direction.N
        while (pos in this) {
            val known = map[pos] ?: emptySet()
            if (direction in known) return true
            map[pos] = known + direction
            while (this[pos + direction] == '#') {
                direction = direction.rotateCW
            }
            pos += direction
        }
        return false
    }

    fun part2(): Int {
        val visitedPositions = findPath().keys
        return visitedPositions.count { visitedPosition ->
            grid[visitedPosition] == '.' && grid.with(visitedPosition, '#').hasCycle()
        }
    }
}

