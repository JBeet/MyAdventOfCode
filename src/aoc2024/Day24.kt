package aoc2024

import utils.paragraphs
import utils.read2024
import utils.verify

fun main() {
    val testInput = read2024("Day24_test")
    verify(2024, Day24.part1(testInput))
    println("Test input OK")
    val input = read2024("Day24")
    println(Day24.part1(input))
    println(Day24.part2(input))

}

object Day24 {
    sealed interface Wire {
        data class Input(val prefix: String, val index: Int, val name: String) : Wire, Comparable<Input> {
            override fun compareTo(other: Input): Int {
                val vc = index.compareTo(other.index)
                return if (vc != 0) vc else prefix.compareTo(other.prefix)
            }

            override fun toString(): String = name
        }

        data class ZOutput(val index: Int, val name: String) : Wire, Comparable<ZOutput> {
            override fun compareTo(other: ZOutput): Int = index.compareTo(other.index)
            override fun toString(): String = name
        }

        data class Other(val name: String) : Wire {
            override fun toString(): String = name
        }
    }

    private fun Input(name: String): Day24.Wire.Input = when {
        name.startsWith("x") -> Day24.Wire.Input("x", name.drop(1).toInt(), name)
        name.startsWith("y") -> Day24.Wire.Input("y", name.drop(1).toInt(), name)
        else -> error("Not an input wire: $name")
    }

    private fun Wire(name: String): Wire = when {
        name.startsWith("x") -> Day24.Wire.Input("x", name.drop(1).toInt(), name)
        name.startsWith("y") -> Day24.Wire.Input("y", name.drop(1).toInt(), name)
        name.startsWith("z") -> Day24.Wire.ZOutput(name.drop(1).toInt(), name)
        else -> Day24.Wire.Other(name)
    }

    enum class GateType {
        XOR, AND, OR
    }

    data class Gate(val inp1: Wire, val type: GateType, val inp2: Wire, val out: Wire)

    data class Circuit(val inputs: Map<Day24.Wire.Input, Boolean>, val gates: List<Gate>) {
        private val indexedByOutput = gates.associateBy { it.out }
        private val outputGates = indexedByOutput.keys.filterIsInstance<Day24.Wire.ZOutput>().sorted()
        private val wireValues = inputs.toMutableMap<Wire, Boolean>()
        private val indexedByInput by lazy {
            val indexedByInput1 = gates.groupBy { it.inp1 }
            val indexedByInput2 = gates.groupBy { it.inp2 }
            (indexedByInput1.keys + indexedByInput2.keys).associateWith { key ->
                (indexedByInput1.getOrDefault(key, emptyList()) + indexedByInput2.getOrDefault(key, emptyList()))
            }.mapValues { (_, dependants) -> dependants.sortedBy { it.type } }
        }

        init {
            check(indexedByOutput.size == gates.size) { "Some outputs serviced by multiple gates" }
        }

        fun part1(): Long = outputGates.fold(1L to 0L) { (multiplier, value), wire ->
            multiplier * 2 to (if (valueFor(wire)) value + multiplier else value)
        }.second

        private fun valueFor(wire: Wire): Boolean = wireValues.getOrPut(wire) {
            val gate = checkNotNull(indexedByOutput[wire]) { "No output for $wire" }
            when (gate.type) {
                GateType.AND -> valueFor(gate.inp1) && valueFor(gate.inp2)
                GateType.OR -> valueFor(gate.inp1) || valueFor(gate.inp2)
                GateType.XOR -> valueFor(gate.inp1) xor valueFor(gate.inp2)
            }
        }

        private fun swap(wire1: Wire, wire2: Wire): Circuit? = Circuit(inputs, gates.map {
            when (it.out) {
                wire1 -> Gate(it.inp1, it.type, it.inp2, wire2)
                wire2 -> Gate(it.inp1, it.type, it.inp2, wire1)
                else -> it
            }
        }).takeUnless { it.hasCycle(wire1) || it.hasCycle(wire2) }

        private fun hasCycle(wire: Wire, wires: Set<Wire> = emptySet()): Boolean {
            if (wire is Day24.Wire.Input) return false
            if (wire in wires) return true
            val gate = indexedByOutput[wire] ?: return true
            val newWires = wires + wire
            return hasCycle(gate.inp1, newWires) || hasCycle(gate.inp2, newWires)
        }

        private val lastInput = inputs.keys.maxOf { it.index }

        fun part2(knownSwaps: List<Wire> = emptyList()): List<Wire>? {
            val problems = (0..(lastInput + 1)).filter { !isValid(it) }
            if (problems.isEmpty())
                return knownSwaps.sortedBy { it.toString() }
            if (knownSwaps.size >= 8)
                return null
            val toFix = problems.first()
            val optionsA = inputs.keys.asSequence().filter { it.index == toFix }
                .flatMap { outputsReachableFrom(it) }
                .filter { it !is Day24.Wire.Input && it !in knownSwaps }.distinct()
            val optionsB = gates.map { it.out }.filter { it !in knownSwaps }
            return optionsA.flatMap { optionA ->
                optionsB.filter { optionB -> optionB != optionsA }.mapNotNull { oB ->
                    val swap = swap(optionA, oB)
                    if (swap != null && swap.isValid(toFix)) Swap(optionA, oB, swap) else null
                }
            }.firstNotNullOfOrNull { swap ->
                swap.solve(knownSwaps)
            }
        }

        data class Swap(val a: Wire, val b: Wire, val circuit: Circuit) {
            fun solve(knownSwaps: List<Wire>) = circuit.part2(knownSwaps + a + b)
            override fun toString(): String = "Swap[$a, $b]"
        }

        private fun outputsReachableFrom(startingWire: Wire) = buildSet {
            val candidates = mutableListOf(startingWire)
            while (candidates.isNotEmpty()) {
                val wire = candidates.removeFirst()
                if (add(wire))
                    indexedByInput[wire]?.forEach { candidates.add(it.out) }
            }
        }

        private fun isValid(maxIndex: Int): Boolean {
            val power2doubled = withInput { it.index == maxIndex }
            if (!power2doubled.outputMatches(maxIndex) { false })
                return false
            val power2minus1doubled = withInput { it.index < maxIndex }
            if (!power2minus1doubled.outputMatches(maxIndex) { index -> index != 0 })
                return false
            val xPlus1 = withInput({ if (it.prefix == "x") it.index == 0 else it.index < maxIndex })
            return xPlus1.outputMatches(maxIndex) { index -> index == maxIndex }
        }

        private fun withInput(valueSelector: (Day24.Wire.Input) -> Boolean) =
            copy(inputs = inputs.keys.associateWith(valueSelector))

        private fun outputMatches(maxIndex: Int, test: (index: Int) -> Boolean) =
            outputGates.filter { it.index <= maxIndex }.all { valueFor(it) == test(it.index) }
    }

    fun part1(input: String): Long {
        val puzzle = parse(input)
        return puzzle.part1()
    }

    private fun parse(input: String): Circuit {
        val (sInputs, sGates) = input.paragraphs()
        val inputs =
            sInputs.lines().map { it.split(": ") }.associate { (name, s01) -> Input(name) to (s01.toInt() == 1) }
        val gates = sGates.lines().map { it.split(" ") }.map { (inp1, type, inp2, _, out) ->
            Gate(Wire(inp1), GateType.valueOf(type), Wire(inp2), Wire(out))
        }
        return Circuit(inputs, gates)
    }

    fun part2(input: String): String {
        val puzzle = parse(input)
        return requireNotNull(puzzle.part2()).joinToString(",")
    }
}
