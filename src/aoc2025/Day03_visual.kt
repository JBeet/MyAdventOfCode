package aoc2025

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import utils.read2025
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.times


data class Bank(val digits: List<Int>) {
    constructor(s: String) : this(s.toCharArray().map { it.digitToInt() })

    operator fun get(idx: Int) = digits[idx]
    val size: Int = digits.size
}

data class Puzzle(val banks: List<Bank>) {
    constructor(s: String) : this(s.lines().map { Bank(it) })

    val inputWidth: Int = banks.maxOf { it.size }
    fun part1(): PuzzleSolver = PuzzleSolver.Sum(banks.map { BankSolver.Initial(it, 2) })
    fun part2(): PuzzleSolver = PuzzleSolver.Sum(banks.map { BankSolver.Initial(it, 12) })
}

sealed interface PuzzleSolver {
    val outputSize: Int
    val activeBank: Bank?
    val isFinished: Boolean
    val activeItems: List<WhiteboardRow>
    suspend fun next(): PuzzleSolver
    fun digitColor(idx: Int): Color
    val delay: Int

    data object Inactive : PuzzleSolver {
        override val activeBank: Bank? = null
        override val isFinished = true
        override val outputSize: Int = 2
        override val activeItems: List<WhiteboardRow> = emptyList()
        override suspend fun next(): PuzzleSolver = this
        override fun digitColor(idx: Int): Color = Color.Transparent
        override val delay: Int = 0
    }

    data class Sum(
        val parts: List<BankSolver>,
        val active: BankSolver,
        val total: Long,
        val completed: List<WhiteboardRow.BankCompleted>
    ) : PuzzleSolver {
        constructor(parts: List<BankSolver>) : this(parts.drop(1), parts[0], 0, emptyList())

        override val activeBank: Bank = active.bank
        override val isFinished: Boolean = false
        override val outputSize: Int = active.outputSize
        override val activeItems: List<WhiteboardRow> get() = completed + WhiteboardRow.RunningTotal(total) + active.items
        override suspend fun next(): PuzzleSolver {
            if (active !is BankSolver.Completed)
                return Sum(parts, active.next(), total, completed)
            val newTotal = total + active.value
            return if (parts.isEmpty())
                Completed(outputSize, newTotal, completed + active.solution)
            else
                Sum(parts.drop(1), parts[0], newTotal, completed + active.solution)
        }

        override fun digitColor(idx: Int): Color = active.color(idx)
        override val delay: Int = active.delay
    }

    data class Completed(
        override val outputSize: Int, val total: Long, val completed: List<WhiteboardRow.BankCompleted>
    ) : PuzzleSolver {
        override val activeBank: Bank? = null
        override val isFinished: Boolean = true
        override val activeItems: List<WhiteboardRow> = completed + listOf(WhiteboardRow.Completed(total))
        override suspend fun next(): PuzzleSolver = this
        override fun digitColor(idx: Int): Color = Color.Transparent
        override val delay: Int = 0
    }
}

sealed interface WhiteboardRow {
    val size: Int
    operator fun get(idx: Int): Char
    fun color(idx: Int): Color
    data class RunningTotal(val total: Long) : WhiteboardRow {
        val s = total.toString()
        override val size: Int get() = s.length
        override fun get(idx: Int): Char = s[idx]
        override fun color(idx: Int): Color = Color.Green
    }

    data class BankCompleted(val src: Partial) : WhiteboardRow by src {
        val value = src.value
        override fun color(idx: Int): Color = Color.Cyan
    }

    data class Completed(val total: Long) : WhiteboardRow {
        val s get() = total.toString()
        override val size: Int get() = s.length
        override fun get(idx: Int): Char = s[idx]
        override fun color(idx: Int): Color = Color.Green
    }

    data class Partial(val bank: BankSolver, val indices: List<Int>) : WhiteboardRow {
        override val size: Int get() = indices.size
        val value get() = indices.fold(0L) { acc, idx -> acc * 10 + bank[idx] }
        override fun get(idx: Int): Char = bank[indices[idx]].digitToChar()
        override fun color(idx: Int): Color = bank.color(indices[idx])
    }
}

sealed interface BankSolver {
    val bank: Bank
    val outputSize: Int
    val items: List<WhiteboardRow>
    suspend fun next(): BankSolver
    fun color(idx: Int): Color
    val delay: Int
    operator fun get(idx: Int): Int = bank[idx]

    data class Initial(override val bank: Bank, override val outputSize: Int) : BankSolver {
        override val items: List<WhiteboardRow> = emptyList()
        override suspend fun next(): BankSolver = GrowStart(bank, outputSize)
        override fun color(idx: Int): Color = Color.Transparent
        override val delay: Int = 2
    }

    data class GrowStart(override val bank: Bank, override val outputSize: Int) : BankSolver {
        override val items: List<WhiteboardRow.Partial> = listOf(WhiteboardRow.Partial(this, listOf(0)))
        override suspend fun next(): BankSolver = GrowMoreBefore(bank, outputSize, items, 1)
        override fun color(idx: Int): Color = if (idx == 0) Color(1f, 0f, 1f, 0.5f) else Color.Transparent
        override val delay: Int = 1
    }

    data class GrowMoreBefore(
        override val bank: Bank,
        override val outputSize: Int,
        override val items: List<WhiteboardRow.Partial>,
        val index: Int
    ) : BankSolver {
        init {
            check(items.size == minOf(index, outputSize)) {
                "Bad items: $items, size: ${items.size}, outputSize: $outputSize, index: $index"
            }
            check(index < bank.size) { "Index too high: $index, bank size: ${bank.size}" }
        }

        override suspend fun next(): BankSolver =
            GrowMoreAfter(bank, outputSize, nextItems(), index)

        private fun nextItems(): List<WhiteboardRow.Partial> = buildList {
            val newDigit = bank[index]
            if (newDigit > items[0].value)
                add(WhiteboardRow.Partial(this@GrowMoreBefore, listOf(index)))
            else
                add(items[0].copy(bank = this@GrowMoreBefore))
            (2..minOf(index, outputSize)).forEach { size ->
                val known = items[size - 1]
                val smaller = items[size - 2]
                val newValue = smaller.value * 10 + newDigit
                if (newValue > known.value)
                    add(WhiteboardRow.Partial(this@GrowMoreBefore, smaller.indices + listOf(index)))
                else
                    add(known.copy(bank = this@GrowMoreBefore))
            }
            if (index < outputSize)
                add(WhiteboardRow.Partial(this@GrowMoreBefore, items.last().indices + listOf(index)))
        }

        val usedIndices = items.flatMapTo(mutableSetOf()) { it.indices }.sorted()
        override fun color(idx: Int): Color {
            if (idx == index) return Color.Red
            val pos = usedIndices.indexOf(idx)
            if (pos < 0)
                return Color.Transparent
            val v = (1f + pos) / usedIndices.size
            return Color(v, 0f, v, 0.5f)
        }

        override val delay: Int = 1
    }

    data class GrowMoreAfter(
        override val bank: Bank,
        override val outputSize: Int,
        override val items: List<WhiteboardRow.Partial>,
        val index: Int
    ) : BankSolver {
        init {
            check(items.size == minOf(index + 1, outputSize)) {
                "Bad items: $items, size: ${items.size}, outputSize: $outputSize, index: $index"
            }
            check(index < bank.size) { "Index too high: $index, bank size: ${bank.size}" }
        }

        override suspend fun next(): BankSolver =
            if (index < bank.size - 1)
                GrowMoreBefore(bank, outputSize, items, index + 1)
            else
                Completed(bank, outputSize, WhiteboardRow.BankCompleted(items.last()))

        val usedIndices = items.flatMapTo(mutableSetOf()) { it.indices }.sorted()
        override fun color(idx: Int): Color {
            if (idx == index) return Color.Red
            val pos = usedIndices.indexOf(idx)
            if (pos < 0)
                return Color.Transparent
            val v = (1f + pos) / usedIndices.size
            return Color(v, 0f, v, 0.5f)
        }

        override val delay: Int = 1
    }

    data class Completed(
        override val bank: Bank,
        override val outputSize: Int,
        val solution: WhiteboardRow.BankCompleted
    ) :
        BankSolver {
        val value: Long get() = solution.value
        override val items: List<WhiteboardRow> = listOf(WhiteboardRow.RunningTotal(value))
        override suspend fun next(): BankSolver = this
        override fun color(idx: Int): Color = Color.Transparent
        override val delay: Int = 5
    }
}

fun main() = mainGUI()

private fun mainCL() {
    runBlocking {
        val puzzle = Puzzle(read2025("Day03_test"))
        var solver = puzzle.part2()
        while (!solver.isFinished) {
            solver = solver.next()
        }
        println(solver)
    }
}

fun mainGUI() = application {
    Window(::exitApplication, title = "Advent of Code - Day 3", alwaysOnTop = true) {
        val scope = rememberCoroutineScope()
        val puzzle = Puzzle(read2025("Day03_test"))
        var solver: PuzzleSolver by remember { mutableStateOf(PuzzleSolver.Inactive) }
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    scope.launch {
                        solver = puzzle.part1()
                        while (!solver.isFinished) {
                            solver = solver.next()
                            delay(solver.delay * 200.milliseconds)
                        }
                    }
                }) {
                    Text("Part 1")
                }
                Button(onClick = {
                    scope.launch {
                        solver = puzzle.part2()
                        while (!solver.isFinished) {
                            solver = solver.next()
                            delay(solver.delay * 100.milliseconds)
                        }
                    }
                }) {
                    Text("Part 2")
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyColumn(modifier = Modifier.weight(puzzle.inputWidth.toFloat())) {
                    items(puzzle.banks) { row ->
                        val isActive = row == solver.activeBank
                        val color = if (isActive) Color.DarkGray else Color.Transparent
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.background(color).padding(8.dp)
                        ) {
                            row.digits.map { it.toString() }.forEachIndexed { idx, digit ->
                                val color = if (isActive) solver.digitColor(idx) else Color.Transparent
                                Card(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(color),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = digit)
                                    }
                                }
                            }
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier.weight(1 + solver.outputSize.toFloat()),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(solver.activeItems) { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val extra = 1 + solver.outputSize - row.size
                            if (extra > 0)
                                Box(modifier = Modifier.weight(extra.toFloat()))
                            (0..<row.size).forEach { idx ->
                                Card(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(row.color(idx)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = row[idx].toString())
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
