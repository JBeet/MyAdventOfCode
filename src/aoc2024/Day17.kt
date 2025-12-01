package aoc2024

import utils.*


fun main() {
    val testInput = read2024("Day17_test")
    verify("4,6,3,5,6,3,5,2,1,0", Day17.part1(testInput))
    println("Test input OK")
    val input = read2024("Day17")
    println(Day17.part1(input))
    val testInput2 = read2024("Day17_test2")
    verify(117440, Day17.part2(testInput2))
    println("Test input OK")
    println(Day17.part2(input))
}

object Day17 {
    fun part1(input: String): String {
        val (sRegisters, sProgram) = input.paragraphs()
        val (a, b, c) = sRegisters.lines().map { it.longsOrZero().first() }
        val program = sProgram.intsOrZero()
        val computer = Computer(program) { true }
        val completed = computer.execute(Registers(a, b, c))
        return completed.output.joinToString(",") { it.toString() }
    }

    fun part2(input: String): Long {
        val (sRegisters, sProgram) = input.paragraphs()
        val (_, b, c) = sRegisters.lines().map { it.longsOrZero().first() }
        val program = sProgram.intsOrZero()
        return program.withIndex().fold(naturalNumberLongs) { seq, (index, value) ->
            val computer = Computer(program) { it.output.size <= index }
            seq.filter { a ->
                val output = computer.execute(Registers(a, b, c)).output
                index < output.size && output[index] == value
            }.toRepeating()
        }.first()
    }

    enum class Instruction {
        ADV, BXL, BST, JNZ, BXC, OUT, BDV, CDV;
    }

    data class Registers(
        val a: Long,
        val b: Long,
        val c: Long,
        val ip: Int,
        val output: List<Int>
    ) {
        constructor(a: Long, b: Long, c: Long) : this(a, b, c, 0, emptyList())

        fun execute(instruction: Instruction, operand: Int) =
            when (instruction) {
                Instruction.ADV -> copy(ip = ip + 2, a = a shr comboValue(operand).toInt())
                Instruction.BXL -> copy(ip = ip + 2, b = b xor operand.toLong())
                Instruction.BST -> copy(ip = ip + 2, b = comboValue(operand) % 8)
                Instruction.JNZ -> if (a == 0L) copy(ip = ip + 2) else copy(ip = operand)
                Instruction.BXC -> copy(ip = ip + 2, b = b xor c)
                Instruction.OUT -> copy(ip = ip + 2, output = output + (comboValue(operand).toInt() % 8))
                Instruction.BDV -> copy(ip = ip + 2, b = a shr comboValue(operand).toInt())
                Instruction.CDV -> copy(ip = ip + 2, c = a shr comboValue(operand).toInt())
            }

        private fun comboValue(operand: Int): Long =
            when (operand) {
                0, 1, 2, 3 -> operand.toLong()
                4 -> a
                5 -> b
                6 -> c
                else -> error("Unexpected combo operand $operand")
            }
    }

    class Computer(private val program: List<Int>, private val verify: (Registers) -> Boolean) {
        fun execute(startingRegisters: Registers): Registers {
            var runningProgram = startingRegisters
            while (runningProgram.ip < program.size - 1 && verify(runningProgram)) {
                val instruction = Instruction.entries[program[runningProgram.ip]]
                runningProgram = runningProgram.execute(instruction, program[runningProgram.ip + 1])
            }
            return runningProgram
        }
    }
}
