import utils.*

fun main() {
    Day06().solve()
}

class Day06 {
    fun solve() {
        val testInput = read2025("Day06_test")
        assertEquals(4277556, part1(testInput))
        val input = read2025("Day06")
        println(part1(input))
        assertEquals(3263827, part2(testInput))
        println(part2(input))
    }


    data class MathProblem(val numbers: List<Long>, val operation: Operation) {
        fun calculate(): Long = operation.calculate(numbers)
    }

    enum class Operation {
        PLUS {
            override fun calculate(numbers: List<Long>) = numbers.sum()
        },
        TIMES {
            override fun calculate(numbers: List<Long>) = numbers.reduce(Long::times)
        };

        abstract fun calculate(numbers: List<Long>): Long

        companion object {
            operator fun invoke(char: Char) = when (char) {
                '+' -> PLUS
                '*' -> TIMES
                else -> error("unexpected operation $char")
            }
        }
    }

    private fun part1(input: String): Long = parsePart1(input).sumOf(MathProblem::calculate)

    fun parsePart1(input: String): List<MathProblem> =
        input.lines().map { line -> line.split(' ').filter { it.isNotBlank() } }.transpose()
            .map { problemPart1(it) }

    private fun problemPart1(items: List<String>): MathProblem =
        MathProblem(items.dropLast(1).map { it.toLong() }, Operation(items.last().single()))

    private fun part2(input: String): Long = parsePart2(input).sumOf(MathProblem::calculate)

    fun parsePart2(input: String): Sequence<MathProblem> =
        input.lines().transpose().reversed().splitBy { it.isBlank() }.map { problemPart2(it) }

    private fun problemPart2(items: List<String>): MathProblem =
        MathProblem(items.flatMap { it.longs() }, Operation(items.last().last()))
}

