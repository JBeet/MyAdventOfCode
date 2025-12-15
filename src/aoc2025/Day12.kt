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
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import aoc2025.Puzzle12.Shape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import utils.*
import utils.Direction
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.times

fun main() {
    mainGUI12(read2025("Day12_test"))
}

private fun mainCLI12(input: String) {
    val puzzle = Puzzle12.parse(input)
    val regionCount = puzzle.regions.count()
    val solveCount = (0..<regionCount).count { regionIndex ->
        var solver: Puzzle12Solver = Puzzle12Solver.StartRegion(puzzle, regionIndex)
        print("Region $regionIndex of $regionCount; region size=")
        print(puzzle.regions[regionIndex].sizeText)
        print("; total shape size = ")
        print(solver.totalShapeSize)
        print(": ")
        while (!solver.isFinished) {
            solver = solver.next()
        }
        (solver is Puzzle12Solver.RegionSolved).also {
            println(if (it) "solved" else "not solved")
        }
    }
    println("Solved $solveCount of $regionCount regions")
}

data class Puzzle12(val shapes: List<Shape>, val regions: List<Region>) {
    data class Shape(private val spec: FilledCharGrid) {
        val size = spec.nonEmptyCells.count()
        val orientations: Set<List<Position>> = buildOrientations(spec)

        init {
            orientations.forEach { positions ->
                check(positions.any { it.row == 0 } && positions.any { it.column == 0 }) {
                    "positions should start at column zero and row zero: $positions"
                }
                check(positions.none { it.row < 0 } && positions.none { it.column < 0 }) {
                    "positions should not have negative offset: $positions"
                }
            }
        }

        operator fun get(r: Int, c: Int): Boolean = spec[r, c] != '.'

        companion object {
            private fun buildOrientations(spec: FilledCharGrid): Set<List<Position>> = buildSet {
                var s = spec
                repeat(4) {
                    add(s.positions())
                    s = s.rotate90()
                }
                s = s.transpose()
                repeat(4) {
                    add(s.positions())
                    s = s.rotate90()
                }
            }

            private fun FilledCharGrid.positions(): List<Position> = nonEmptyCells.map { it.first }.toList()
        }
    }

    data class Region(val width: Int, val length: Int, val quantities: List<Int>)

    fun initial() = Puzzle12Solver.StartRegion(this, 0)

    companion object {
        fun parse(input: String): Puzzle12 {
            val paragraphs = input.paragraphs()
            val shapePart = paragraphs.dropLast(1)
            val regionPart = paragraphs.last()
            return Puzzle12(shapePart.map { parseShape(it) }, regionPart.lines().map { parseRegion(it) })
        }

        private fun parseRegion(input: String): Region {
            val (width, length) = input.substringBefore(":").ints()
            val quantities = input.substringAfter(":").intsOrZero()
            return Region(width, length, quantities)
        }

        private fun parseShape(input: String): Shape =
            Shape(FilledCharGrid(input.lines().drop(1)))
    }
}

class Field(
    val height: Int,
    val width: Int,
    private val cells: IntArray,
    val emptyCellCount: Int,
    private val currentIndicator: Int
) {
    init {
        check(currentIndicator > 0) { "Indicator cannot be zero or negative: $currentIndicator" }
    }

    operator fun get(r: Int, c: Int) = cells[r * width + c]

    operator fun contains(position: Position) =
        position.row in 0 until height && position.column in 0 until width

    fun countEmptyCellsAfter(position: Position): Int {
        val offset = getOffset(position)
        return (offset..<cells.lastIndex).count { cells[it] == 0 }
    }

    fun canFit(offsets: List<Position>, base: Position): Boolean =
        offsets.map { base + it }.let { positions ->
            positions.all { it in this } && positions.none { get(it) }
        }

    operator fun get(position: Position): Boolean = cells[getOffset(position)] != 0
    fun indicator(position: Position): Int = cells[getOffset(position)]

    fun add(offsets: List<Position>, base: Position): Field {
        val newCells = cells.copyOf()
        offsets.map { base + it }.forEach { set(newCells, it) }
        val newEmptyCellCount = emptyCellCount - offsets.size
        return Field(height, width, newCells, newEmptyCellCount, currentIndicator + 1)
    }

    private fun set(cells: IntArray, position: Position) {
        val offset = getOffset(position)
        check(cells[offset] == 0) { "Cell $position already set" }
        cells[offset] = currentIndicator
    }

    private fun getOffset(position: Position): Int = position.row * width + position.column
    fun nextPositions(position: Position, maxLength: Int): List<Position> = buildList {
        var r = position.row
        var c = position.column
        var length = 0
        while (length < maxLength) {
            if (++c >= width) {
                if (++r >= height) break
                c = 0
                length = 0
            }
            val p = Position(r, c)
            if (get(p)) {
                length = 0
            } else {
                length++
                add(p)
            }
        }
    }


    companion object {
        fun create(height: Int, width: Int) =
            Field(height, width, IntArray(height * width) { 0 }, height * width, 1)
    }
}

private fun Field.color(position: Position): Color =
    colorForIndicator(indicator(position))

private fun colorForIndicator(indicator: Int): Color =
    if (indicator == 0)
        Color.Gray
    else
        Color.hsl(((217 * indicator) % 360).toFloat(), 1f, 0.5f)

data class RegionStep(
    val puzzle: Puzzle12,
    val regionIndex: Int,
    val field: Field,
    val quantitiesByShape: Map<Shape, Int>,
    val cellsToFill: Int = quantitiesByShape.entries.sumOf { it.key.size * it.value },
    val originalQuantities: Map<Shape, Int> = quantitiesByShape
) {
    fun impossible(currentPosition: Position): Boolean =
        cellsToFill > field.countEmptyCellsAfter(currentPosition)

    fun solved(): Boolean =
        quantitiesByShape.all { it.value == 0 }

    fun orientationsToTry(position: Position): List<Pair<Shape, List<Position>>> {
        val shapesToTry = quantitiesByShape.filter { it.value > 0 }.keys
        return shapesToTry.flatMap { shape ->
            shape.orientations
                .filter { field.canFit(it, position) }
                .filter { !field.canFit(it, position + Direction.N) }
                .map { shape to it }
        }
    }

    fun add(shape: Shape, orientation: List<Position>, position: Position): RegionStep =
        copy(
            field = field.add(orientation, position),
            quantitiesByShape = quantitiesByShape.toMutableMap().apply {
                set(shape, getOrDefault(shape, 0) - 1)
            },
            cellsToFill = cellsToFill - shape.size
        )

    fun nextPositions(position: Position): List<Position> = field.nextPositions(position, 5)
}

sealed class Puzzle12Solver(val puzzle: Puzzle12, val regionIndex: Int) {
    abstract fun next(): Puzzle12Solver
    val delay: Int = 1
    abstract val isFinished: Boolean

    fun startSolving(): Puzzle12Solver = StartRegion(puzzle, regionIndex)
    fun nextRegion(): Puzzle12Solver = StartRegion(puzzle, regionIndex + 1)

    val region = puzzle.regions[regionIndex]
    open val shapes: Collection<Shape> = emptySet()
    abstract fun currentQuantity(shape: Shape): Int
    abstract fun originalQuantity(shape: Shape): Int
    val width: Int get() = region.width
    val height: Int get() = region.length
    open fun color(position: Position): Color = Color.Gray

    class StartRegion(puzzle: Puzzle12, index: Int) : Puzzle12Solver(puzzle, (index % puzzle.regions.count())) {
        private val field = Field.create(region.length, region.width)
        private val quantitiesByShape = puzzle.shapes.withIndex().associate { (index, shape) ->
            shape to region.quantities[index]
        }.filterValues { it > 0 }
        private val initialPosition = Position(0, 0)
        private val base = RegionStep(puzzle, regionIndex, field, quantitiesByShape)
        private val failed = RegionFailed(base)
        override val isFinished: Boolean = false
        override val shapes: Collection<Shape> get() = quantitiesByShape.keys
        override fun originalQuantity(shape: Shape): Int = quantitiesByShape.getOrDefault(shape, 0)
        override fun currentQuantity(shape: Shape): Int = originalQuantity(shape)
        override fun next(): Puzzle12Solver {
            return StartCell(base, initialPosition, failed)
        }
    }

    abstract class RegionSolver(protected val base: RegionStep) : Puzzle12Solver(base.puzzle, base.regionIndex) {
        override val isFinished: Boolean = false
        override fun color(position: Position): Color = base.field.color(position)
        override val shapes: Collection<Shape> get() = base.quantitiesByShape.keys
        override fun originalQuantity(shape: Shape): Int = base.originalQuantities.getOrDefault(shape, 0)
        override fun currentQuantity(shape: Shape): Int = base.quantitiesByShape.getOrDefault(shape, 0)
    }

    class StartCell(base: RegionStep, private val position: Position, private val failed: Puzzle12Solver) :
        RegionSolver(base) {
        private val orientations = base.orientationsToTry(position)
        override fun next(): Puzzle12Solver =
            when {
                base.impossible(position) -> failed
                base.solved() -> RegionSolved(base)
                else -> TryOrientations(0)
            }

        inner class TryOrientations(private val index: Int) : RegionSolver(base) {
            override fun next(): Puzzle12Solver =
                if (index >= orientations.size)
                    failed
                else
                    TryOrientation(
                        base.add(orientations[index].first, orientations[index].second, position),
                        position,
                        TryOrientations(index + 1)
                    )
        }
    }

    class TryOrientation(base: RegionStep, lastPosition: Position, private val failed: Puzzle12Solver) :
        RegionSolver(base) {
        private val nextPositions = base.nextPositions(lastPosition)
        override fun next(): Puzzle12Solver =
            nextPositions.asReversed().fold(failed) { acc, position ->
                StartCell(base, position, acc)
            }
    }

    class RegionSolved(base: RegionStep) : RegionSolver(base) {
        override val isFinished: Boolean = true
        override fun next(): Puzzle12Solver = this
    }

    class RegionFailed(base: RegionStep) : RegionSolver(base) {
        override val isFinished: Boolean = true
        override fun next(): Puzzle12Solver = this
        override fun color(position: Position): Color = Color.DarkGray
    }
}

val Puzzle12Solver.totalShapeSize: Int get() = shapes.sumOf { it.size * originalQuantity(it) }
val Puzzle12Solver.remainingShapeSize: Int get() = shapes.sumOf { it.size * currentQuantity(it) }
val Puzzle12.Region.sizeText: String get() = "" + width + "x" + length + " = " + width * length
val Puzzle12Solver.fieldSizeText: String get() = region.sizeText

fun mainGUI12(input: String) = application {
    val puzzle = Puzzle12.parse(input)
    val spacing = 2.dp
    Window(
        ::exitApplication,
        title = "Advent of Code - Day 12",
        alwaysOnTop = true,
        state = WindowState(width = 400.dp, height = 800.dp)
    ) {
        val scope = rememberCoroutineScope()
        var solver: Puzzle12Solver by remember { mutableStateOf(puzzle.initial()) }
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
                        solver = solver.startSolving()
                        while (!solver.isFinished) {
                            solver = solver.next()
                            delay(solver.delay * 1.milliseconds)
                        }
                    }
                }) {
                    Text("Solve")
                }
                Button(onClick = { solver = solver.nextRegion() }) {
                    Text("Next Region")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(spacing * 2),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                LazyColumn(modifier = Modifier.weight(solver.width.toFloat())) {
                    items(solver.height) { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            modifier = Modifier.padding(spacing)
                        ) {
                            (0..<(solver.width)).forEach { column ->
                                val position = Position(row, column)
                                val color = solver.color(position)
                                Card(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(color),
                                        contentAlignment = Alignment.Center
                                    ) {
//                                        Text("$row,$column")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Row { Text("Current Region: " + (1 + solver.regionIndex)) }
            Row { Text("Field size: " + solver.fieldSizeText) }
            Row { Text("Total shape size: " + solver.totalShapeSize) }
            Row { Text("Remaining shape size: " + solver.remainingShapeSize) }
            solver.shapes.forEach { shape ->
                Row(Modifier.padding(spacing), verticalAlignment = Alignment.CenterVertically) {
                    (0..2).forEach { c ->
                        Column {
                            (0..2).forEach { r ->
                                Row(Modifier.padding(spacing)) {
                                    val color = if (shape[r, c]) Color.Blue else Color.Gray
                                    Box(Modifier.height(4.dp).width(4.dp).background(color))
                                }
                            }
                        }
                    }
                    Column(Modifier.weight(1f).padding(spacing)) {
                        val currentCount = solver.currentQuantity(shape)
                        val originalCount = solver.originalQuantity(shape)
                        Text("$currentCount x ($originalCount)")
                    }
                }
            }
        }
    }
}
