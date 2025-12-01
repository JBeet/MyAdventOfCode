package aoc2024

import utils.AnsiColor
import utils.intLists
import utils.read2024
import kotlin.time.measureTime

fun main() {
    val testInput = read2024("Day02_test")
    check(Day02WithStateEnum.part1(testInput) == 2) { "Expected 2 but was " + Day02WithStateEnum.part1(testInput) }
    check(Day02WithStateEnum.part2(testInput) == 4) { "Expected 4 but was " + Day02WithStateEnum.part2(testInput) }

    val input = read2024("Day02")
    Day02WithStateEnum.showStates(input)
    println()
    println("Solution Part 1: " + Day02WithStateEnum.part1(input))
    println("Solution Part 2: " + Day02WithStateEnum.part2(input))

    val preparedLists = input.intLists()
    println("Timing with running fold: " + Day02WithStateEnum.part2WithRunningFold(preparedLists))
    val repeats = 100_000
    measureTime {
        repeat(repeats) { Day02WithStateEnum.part2WithRunningFold(preparedLists) }
    }.let { println("Time: $it total, is " + (it / repeats) + " per call") }
    println("Timing with custom sequence: " + Day02WithStateEnum.part2WithCustomSequence(preparedLists))
    measureTime {
        repeat(repeats) { Day02WithStateEnum.part2WithCustomSequence(preparedLists) }
    }.let { println("Time: $it total, is " + (it / repeats) + " per call") }
    println("Timing with iteration: " + Day02WithStateEnum.part2Iterative(preparedLists))
    measureTime {
        repeat(repeats) { Day02WithStateEnum.part2Iterative(preparedLists) }
    }.let { println("Time: $it total, is " + (it / repeats) + " per call") }
}

object Day02WithStateEnum {
    fun part1(input: String): Int =
        input.intLists().count { list -> list.allStates().all { it.isValidPart1 } }

    fun part2(input: String): Int = part2WithCustomSequence(input.intLists())

    fun showStates(input: String) {
        input.intLists().forEach { list ->
            print(stateColor(State.First).fgCode() + list[0] + " ")
            list.allStates().forEachIndexed { index, state ->
                print(stateColor(state).fgCode() + list[index + 1] + " ")
            }
            println(AnsiColor.DEFAULT.fgCode())
        }
    }

    fun part2WithRunningFold(intLists: List<List<Int>>) =
        intLists.count { list ->
            State.Invalid !in list.windowed(3)
                .runningFold(State.First) { acc, (v0, v1, v2) -> acc.nextState(v0, v1, v2) }
        }

    fun part2WithCustomSequence(intLists: List<List<Int>>) =
        intLists.count { list -> State.Invalid !in list.allStates() }

    // DIY sequence building is 3x quicker than windowed/runningFold
    private fun List<Int>.allStates(): Sequence<State> = sequence {
        var state = State.First
        yield(state)
        for (i in 2..lastIndex) {
            state = state.nextState(this@allStates[i - 2], this@allStates[i - 1], this@allStates[i])
            yield(state)
        }
    }

    fun part2Iterative(intLists: List<List<Int>>) =
        intLists.count { list -> list.checkIterative() }

    // much quicker than the sequence
    private fun List<Int>.checkIterative(): Boolean {
        var state = State.First
        for (i in 2..lastIndex) {
            state = state.nextState(this[i - 2], this[i - 1], this[i])
            if (state == State.Invalid) return false
        }
        return true
    }

    private fun stateColor(state: State) = when (state) {
        State.First -> AnsiColor.BRIGHT_BLACK
        State.KnownIncreasing -> AnsiColor.BRIGHT_MAGENTA
        State.KnownDecreasing -> AnsiColor.BRIGHT_CYAN
        State.KnownIncreasingDroppedPreviousOrEarlier -> AnsiColor.MAGENTA
        State.KnownDecreasingDroppedPreviousOrEarlier -> AnsiColor.CYAN
        State.KnownIncreasingDroppedPrevious -> AnsiColor.MAGENTA
        State.KnownDecreasingDroppedPrevious -> AnsiColor.CYAN
        State.DroppedPreviousForIncreaseOrEarlierForDecrease -> AnsiColor.WHITE
        State.DroppedPreviousForDecreaseOrEarlierForIncrease -> AnsiColor.WHITE
        State.DroppedEarlierDirectionUnknown -> AnsiColor.WHITE
        State.KnownIncreasingDroppedEarlier -> AnsiColor.MAGENTA
        State.KnownDecreasingDroppedEarlier -> AnsiColor.CYAN
        State.Invalid -> AnsiColor.RED
    }

    enum class State(val isValidPart1: Boolean) {
        First(true) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                when {
                    isIncreasing(v0, v1) -> startedWithIncrease(v0, v1, v2)
                    isDecreasing(v0, v1) -> startedWithDecrease(v0, v1, v2)
                    else -> dropV0orV1(v0, v1, v2)
                }

            private fun dropV0orV1(v0: Int, v1: Int, v2: Int): State =
                when {
                    isIncreasing(v0, v2) ->
                        if (isDecreasing(v1, v2)) DroppedEarlierDirectionUnknown else KnownIncreasingDroppedEarlier

                    isDecreasing(v0, v2) ->
                        if (isIncreasing(v1, v2)) DroppedEarlierDirectionUnknown else KnownDecreasingDroppedEarlier

                    else -> when {
                        isIncreasing(v1, v2) -> KnownIncreasingDroppedEarlier
                        isDecreasing(v1, v2) -> KnownDecreasingDroppedEarlier
                        else -> Invalid
                    }
                }

            private fun startedWithIncrease(v0: Int, v1: Int, v2: Int): State =
                when {
                    isIncreasing(v1, v2) -> KnownIncreasing
                    isDecreasing(v1, v2) -> DroppedPreviousForIncreaseOrEarlierForDecrease
                    isDecreasing(v0, v2) -> DroppedPreviousForIncreaseOrEarlierForDecrease
                    else -> KnownIncreasingDroppedPrevious
                }

            private fun startedWithDecrease(v0: Int, v1: Int, v2: Int): State =
                when {
                    isDecreasing(v1, v2) -> KnownDecreasing
                    isIncreasing(v1, v2) -> DroppedPreviousForDecreaseOrEarlierForIncrease
                    isIncreasing(v0, v2) -> DroppedPreviousForDecreaseOrEarlierForIncrease
                    else -> KnownDecreasingDroppedPrevious
                }
        },
        KnownIncreasing(true) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                when {
                    isIncreasing(v1, v2) -> KnownIncreasing
                    isIncreasing(v0, v2) -> KnownIncreasingDroppedPreviousOrEarlier
                    else -> KnownIncreasingDroppedPrevious
                }
        },
        KnownDecreasing(true) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                when {
                    isDecreasing(v1, v2) -> KnownDecreasing
                    isDecreasing(v0, v2) -> KnownDecreasingDroppedPreviousOrEarlier
                    else -> KnownDecreasingDroppedPrevious
                }
        },
        KnownIncreasingDroppedPreviousOrEarlier(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                if (isIncreasing(v1, v2) || isIncreasing(v0, v2))
                    KnownIncreasingDroppedEarlier
                else
                    Invalid
        },
        KnownDecreasingDroppedPreviousOrEarlier(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                if (isDecreasing(v1, v2) || isDecreasing(v0, v2))
                    KnownDecreasingDroppedEarlier
                else
                    Invalid
        },
        KnownIncreasingDroppedPrevious(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                if (isIncreasing(v0, v2))
                    KnownIncreasingDroppedEarlier
                else
                    Invalid
        },
        KnownDecreasingDroppedPrevious(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                if (isDecreasing(v0, v2))
                    KnownDecreasingDroppedEarlier
                else
                    Invalid
        },
        DroppedPreviousForIncreaseOrEarlierForDecrease(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State {
                val prevPrevOk = isIncreasing(v0, v2)
                val prevOk = isDecreasing(v1, v2)
                return when {
                    prevPrevOk && prevOk -> DroppedEarlierDirectionUnknown
                    prevPrevOk -> KnownIncreasingDroppedEarlier
                    prevOk -> KnownDecreasingDroppedEarlier
                    else -> Invalid
                }
            }
        },
        DroppedPreviousForDecreaseOrEarlierForIncrease(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State {
                val prevPrevOk = isDecreasing(v0, v2)
                val prevOk = isIncreasing(v1, v2)
                return when {
                    prevPrevOk && prevOk -> DroppedEarlierDirectionUnknown
                    prevPrevOk -> KnownDecreasingDroppedEarlier
                    prevOk -> KnownIncreasingDroppedEarlier
                    else -> Invalid
                }
            }
        },
        DroppedEarlierDirectionUnknown(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                when {
                    isIncreasing(v1, v2) -> KnownIncreasingDroppedEarlier
                    isDecreasing(v1, v2) -> KnownDecreasingDroppedEarlier
                    else -> Invalid
                }
        },
        KnownIncreasingDroppedEarlier(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                if (isIncreasing(v1, v2)) KnownIncreasingDroppedEarlier else Invalid
        },
        KnownDecreasingDroppedEarlier(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                if (isDecreasing(v1, v2)) KnownDecreasingDroppedEarlier else Invalid
        },
        Invalid(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State = Invalid
        };

        abstract fun nextState(v0: Int, v1: Int, v2: Int): State
    }

    private val acceptedRange = 1..3
    private fun isIncreasing(prev: Int, cur: Int) = cur - prev in acceptedRange
    private fun isDecreasing(prev: Int, cur: Int) = prev - cur in acceptedRange
}
