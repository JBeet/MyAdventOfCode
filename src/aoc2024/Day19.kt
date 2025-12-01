package aoc2024

import java.io.File

fun main() {
    val (towels, designs) = File("src/Day19.txt").readLines().run { get(0).split(", ") to drop(2) }
    designs.map { design ->
        design.indices.drop(1).map(design::take).fold(listOf(1L)) { sums, part ->
            sums + towels.filter(part::endsWith).sumOf { sums[sums.size - it.length] }
        }.last()
    }.run { println("${count { it > 0 }}\n${sum()}") }
}
