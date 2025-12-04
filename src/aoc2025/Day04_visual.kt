package aoc2025

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import utils.*
import utils.Direction
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.times

fun main() {
    mainGUI04()
}

data class Puzzle04(val grid: CharGrid) {
    val width get() = grid.width
    val height get() = grid.height

    constructor(input: String) : this(CharGrid(input))

    fun initial() = Puzzle04Solver.Inactive(grid)
    fun part1() = Puzzle04Solver.Part1SolveStart(grid)
    fun part2() = Puzzle04Solver.StartPart2(grid)
}

private val clockDirections = listOf(
    Direction8.N,
    Direction8.NE,
    Direction8.E,
    Direction8.SE,
    Direction8.S,
    Direction8.SW,
    Direction8.W,
    Direction8.NW
)

sealed class Puzzle04Solver(val grid: BoundedGrid<Char>) {
    abstract val result: Int?
    open val isFinished: Boolean get() = false
    open val delay: Int get() = 10
    abstract suspend fun next(): Puzzle04Solver
    open operator fun get(position: Position): String = grid[position].toString()
    open fun color(position: Position) = Color.Transparent

    class Inactive(grid: BoundedGrid<Char>) : Puzzle04Solver(grid) {
        override val result: Int? get() = null
        override val isFinished: Boolean = true
        override suspend fun next(): Puzzle04Solver = this
    }

    class Part1SolveStart(grid: BoundedGrid<Char>) : Puzzle04Solver(grid) {
        override val result: Int get() = 0
        override fun color(position: Position): Color = Color.Cyan
        override suspend fun next(): Puzzle04Solver = Part1CellStart(grid)
    }

    class Part1CellStart(grid: BoundedGrid<Char>, val active: Position, val marked: Map<Position, Int>) :
        Puzzle04Solver(grid) {
        constructor(grid: BoundedGrid<Char>) : this(grid, Position(0, 0), emptyMap())

        override val delay: Int get() = 5

        override val result: Int get() = marked.count { it.value < 4 }

        override fun color(position: Position): Color =
            if (position == active)
                Color.hsl(30f, 1f, 0.5f)
            else colorForCount(marked[position])

        override fun get(position: Position): String =
            if (position == active)
                "?"
            else
                marked[position]?.toString() ?: super.get(position)

        override suspend fun next(): Puzzle04Solver =
            if (grid[active] == '@')
                Part1CheckCell(this, clockDirections.filter { (active + it) in grid.bounds }, 0)
            else
                nextCellSolver(marked)

        fun nextCell(result: Int): Puzzle04Solver =
            nextCellSolver(marked + (active to result))

        private fun nextCellSolver(newMarked: Map<Position, Int>): Puzzle04Solver =
            nextPosition()?.let { Part1CellStart(grid, it, newMarked) } ?: Part1Completed(grid, newMarked)

        private fun nextPosition(): Position? =
            when {
                (active + Direction.E) in grid.bounds -> active + Direction.E
                (active + Direction.S) in grid.bounds -> Position(active.row + 1, grid.bounds.columnRange.first)
                else -> null
            }
    }

    class Part1Completed(grid: BoundedGrid<Char>, val marked: Map<Position, Int>) : Puzzle04Solver(grid) {
        override val result: Int get() = marked.count { it.value < 4 }
        override suspend fun next(): Puzzle04Solver = this
        override fun color(position: Position): Color = colorForCount(marked[position])
        override fun get(position: Position): String = when (marked[position]) {
            null -> super.get(position)
            in 0..3 -> "x"
            else -> super.get(position)
        }

        override val isFinished: Boolean
            get() = true
    }

    class Part1CheckCell(val cell: Part1CellStart, val directions: List<Direction8>, activeCounter: Int) :
        Puzzle04Solver(cell.grid) {
        override val delay: Int get() = 2
        override val result: Int get() = cell.result
        private val check: Position get() = cell.active + directions[0]
        private val newActiveCounter = if (grid[check] == '@') (activeCounter + 1) else activeCounter
        override fun color(position: Position): Color =
            if (position == check) Color.Cyan else cell.color(position)

        override fun get(position: Position): String =
            if (position == cell.active) newActiveCounter.toString() else cell[position]

        override suspend fun next(): Puzzle04Solver =
            if (directions.size > 1)
                Part1CheckCell(cell, directions.drop(1), newActiveCounter)
            else
                cell.nextCell(newActiveCounter)
    }

    class StartPart2(grid: BoundedGrid<Char>) : Puzzle04Solver(grid) {
        override val result: Int get() = 0
        override fun color(position: Position): Color = Color.Cyan
        override suspend fun next(): Puzzle04Solver = Inactive(grid)
    }
}

private fun colorForCount(i: Int?): Color = when (i) {
    null -> Color.Transparent
    in 0..3 -> Color.hsl(120f, 1f, 0.5f)
    else -> Color.hsl(0f, 1f, 0.5f)
}


fun mainGUI04() = application {
    val puzzle = Puzzle04(read2025("Day04_Test"))
    val cellSize = 1600 / puzzle.height
    val spacing = if (cellSize > 100) 8.dp else 0.dp
    val windowWidth = (puzzle.width * cellSize).dp
    val windowHeight = 1800.dp
    Window(
        ::exitApplication,
        title = "Advent of Code - Day 4",
        alwaysOnTop = true,
        state = WindowState(width = windowWidth, height = windowHeight)
    ) {
        val scope = rememberCoroutineScope()
        var solver: Puzzle04Solver by remember { mutableStateOf(puzzle.initial()) }
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(spacing * 2),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                Button(onClick = {
                    scope.launch {
                        solver = puzzle.initial()
                    }
                }) {
                    Text("Reset")
                }
                Button(onClick = {
                    scope.launch {
                        solver = puzzle.part1()
                        while (!solver.isFinished) {
                            solver = solver.next()
                            delay(solver.delay * 16.milliseconds)
                        }
                    }
                }) {
                    Text("Part 1")
                }
                Button(onClick = {}, enabled = false) {
                    Text("Part 2")
                }
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    solver.result?.let { Text("$it rolls of paper can be moved", fontSize = 36.sp) }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(spacing * 2),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                LazyColumn(modifier = Modifier.weight(puzzle.width.toFloat())) {
                    items(puzzle.height) { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            modifier = Modifier.padding(spacing)
                        ) {
                            (0..<(puzzle.width)).forEach { column ->
                                val position = Position(row, column)
                                val color = solver.color(position)
                                Card(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(color),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = solver[position])
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
