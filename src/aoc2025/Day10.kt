package aoc2025

import utils.*

fun main() {
    Day10().solve()
}

class Day10 {
    fun solve() {
        val testInput = read2025("Day10_test")
        assertEquals(7, part1(testInput))
        val input = read2025("Day10")
        println(part1(input))
        assertEquals(33, part2(testInput))
        println(part2(input))
    }

    private fun part1(input: String): Int = machines(input).sumOf { it.minimalButtonPressesForLighting() }
    private fun part2(input: String): Long {
        val machines = machines(input)
        return machines.mapIndexed { index, machine ->
            machine.minimalButtonPressesForJoltage().also {
                println("Solved ${index + 1} of ${machines.size} => $it")
            }
        }.sum()
    }

    private fun machines(input: String): List<Machine> = input.lines().map { Machine(it) }

    private fun Machine(input: String) = Machine(input.split(" "))
    private fun Machine(parts: List<String>) = Machine(parts.first(), parts.drop(1).dropLast(1), parts.last())
    private fun Machine(lights: String, buttons: List<String>, joltage: String) =
        Machine(toDiagram(lights), parseButtons(buttons), toJoltage(joltage))

    data class Button(val index: Int, val targets: Set<Int>)
    data class Machine(val lightDiagram: Set<Int>, val buttons: List<Button>, val joltage: List<Int>) {
        fun minimalButtonPressesForLighting(): Int =
            buttons.combinations().filter { pressedButtons -> lighting(pressedButtons) == lightDiagram }
                .minOf { it.size }

        fun lighting(presses: List<Button>): Set<Int> =
            presses.flatMap { it.targets }.groupingBy { it }.eachCount().filterValues { it % 2 == 1 }.keys

        fun minimalButtonPressesForJoltage(): Long = MinimalPressesSolver(buttons, joltage).solve()
    }

    data class State(
        val buttonsToUse: Set<Button>,
        val joltageRemaining: List<Int>,
        val totalPresses: Long,
        val knownPresses: Map<Button, Int>,
        val previousJoltageToSolve: Int
    ) {
        companion object Companion {
            private val buttonsByJoltageIndex = cached { buttons: Set<Button> ->
                buttons.flatMap { button -> button.targets.map { ji -> ji to button } }
                    .groupBy({ it.first }, { it.second })
            }
            private val joltageIndexByButtonSets = cached { buttons: Set<Button> ->
                buttonsByJoltageIndex(buttons).entries.groupBy({ it.value.toSet() }, { it.key })
            }
        }

        val solution: String
            get() = knownPresses.entries.sortedBy { it.key.index }.joinToString(",") { (button, presses) ->
                "${presses}x" + button.targets.joinToString(",", "{", "}")
            } + " => $totalPresses total"
        val isFailed: Boolean by lazy { calculateIsFailed() }

        fun calculateIsFailed(): Boolean =
            joltageIndexByButtonSets(buttonsToUse).any { (_, indices) ->
                indices.map { joltageRemaining[it] }.distinct().size > 1
            }

        val isFinished: Boolean get() = joltageRemaining.all { it == 0 }

        private val joltageTarget: Int = if (previousJoltageToSolve >= 0 || buttonsToUse.isEmpty())
            previousJoltageToSolve
        else
            buttonsByJoltageIndex(buttonsToUse).minBy { it.value.size }.key

        fun nextStates(): Sequence<State> = sequence {
            val joltage = joltageRemaining[joltageTarget]
            check(joltage >= 0) { "Joltage $joltageTarget is not available: $joltageRemaining for ${this@State}" }
            val buttons = buttonsToUse.filter { joltageTarget in it.targets }
            if (buttons.size == 1) {
                val state = nextState(buttons.single(), joltage, false)
                if (state != null)
                    yield(state)
            } else {
                val button =
                    buttons.maxByOrNull { it.targets.size }
                        ?: error("No buttons left for joltage $joltageTarget / $joltage in ${this@State}")
                (joltage downTo 0).forEach { presses ->
                    val value = nextState(button, presses, true)
                    if (value != null)
                        yield(value)
                }
            }
        }

        private fun nextState(button: Button, presses: Int, keepTarget: Boolean): State? {
            val newJoltage = newJoltage(button, presses) ?: return null
            return State(
                buttonsToUse - button,
                newJoltage,
                totalPresses + presses,
                knownPresses + (button to presses),
                if (keepTarget) joltageTarget else -1
            )
        }

        fun newJoltage(button: Button, presses: Int): List<Int>? =
            joltageRemaining.mapIndexed { index, j ->
                if (index in button.targets) (j - presses).also { if (it < 0) return null } else j
            }

        override fun toString(): String = buildString {
            append("State(buttons=[")
            buttonsToUse.forEach { append(it.index).append(",") }
            if (buttonsToUse.isNotEmpty()) setLength(length - 1)
            append("], joltage=[")
            joltageRemaining.forEach { append(it).append(",") }
            if (joltageRemaining.isNotEmpty()) setLength(length - 1)
            append("], presses=[")
            knownPresses.forEach { (button, presses) ->
                append(presses).append("x").append(button.index).append(",")
            }
            if (knownPresses.isNotEmpty()) setLength(length - 1)
            append("])")
        }
    }

    private data class MinimalPressesSolver(val buttons: List<Button>, val joltage: List<Int>) {
        var bestSoFar = Long.MAX_VALUE
        fun solve(): Long {
            println("Solving $this")
            return recurse(State(buttons.toSet(), joltage, 0, emptyMap(), -1))
        }

        private fun recurse(state: State): Long {
            if (state.isFinished) {
                println("Solved $joltage as ${state.solution}")
                bestSoFar = minOf(bestSoFar, state.totalPresses)
                return state.totalPresses
            }
            if (state.isFailed) return Long.MAX_VALUE
            if (state.totalPresses >= bestSoFar) return Long.MAX_VALUE
            return state.nextStates().minOfOrNull { recurse(it) } ?: Long.MAX_VALUE
        }
    }


    private fun toDiagram(s: String): Set<Int> =
        s.removePrefix("[").removeSuffix("]").mapIndexedNotNullTo(mutableSetOf()) { i, c ->
            when (c) {
                '.' -> null
                '#' -> i
                else -> error("Unexpected char '$c'")
            }
        }

    private fun parseButtons(buttons: List<String>) =
        buttons.mapIndexed { index, s -> Button(index, s.intsOrZero().toSet()) }

    private fun toJoltage(s: String): List<Int> = s.removePrefix("{").removeSuffix("}").intsOrZero()
}
