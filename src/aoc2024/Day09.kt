package aoc2024

import utils.read2024
import utils.verify

fun main() {
    val testInput = read2024("Day09_test")
    verify(1928, Day09(testInput).part1())
    val input = read2024("Day09")
    println(Day09(input).part1())
    verify(2858, Day09(testInput).part2())
    println(Day09(input).part2())
}

sealed interface DiskBlock {
    val len: Int

    data class Filled(override val len: Int, val id: Int) : DiskBlock {
        init {
            check(len > 0) { "Do not create 0-length block for $id" }
        }
    }

    data class Empty(override val len: Int) : DiskBlock {
        init {
            check(len > 0) { "Do not create 0-length empty block" }
        }
    }
}

class Day09(private val input: String) {
    fun part1(): Long {
        val spec = parse()
        val list = createList(spec)
        compact(list)
        return list.checksum()
    }

    fun part2(): Long {
        val spec = parse()
        val list = createList(spec)
        compact2(list)
        println(list)
        return list.checksum()
    }

    private fun parse() = input.toCharArray().map { it.digitToInt() }
    private fun List<DiskBlock>.checksum(): Long {
        var pos = 0
        var sum = 0L
        forEach { block ->
            if (block is DiskBlock.Filled) sum += block.id * (0..<block.len).sumOf { (pos.toLong() + it) }
            pos += block.len
        }
        return sum
    }

    private fun createList(spec: List<Int>): MutableList<DiskBlock> {
        val list = mutableListOf<DiskBlock>()
        var curID = 0
        for (i in spec.indices) {
            val len = spec[i]
            if (i % 2 == 0) {
                if (len > 0)
                    list.add(DiskBlock.Filled(len, curID))
                curID++
            } else if (len > 0)
                list.add(DiskBlock.Empty(len))
        }
        return list
    }

    private fun compact2(list: MutableList<DiskBlock>) {
        val maxId = list.filterIsInstance<DiskBlock.Filled>().maxOf { it.id }
        (0..maxId).reversed().forEach { id ->
            val indexToMoveFrom = list.indexOfLast { it is DiskBlock.Filled && it.id == id }
            check(indexToMoveFrom >= 0) { "Block $id not found in $list" }
            val blockToMove = list[indexToMoveFrom]
            val indexToMoveTo = list.indexOfFirst { it is DiskBlock.Empty && it.len >= blockToMove.len }
            if (indexToMoveTo in 0..<indexToMoveFrom) {
                // move it
                list[indexToMoveFrom] = DiskBlock.Empty(blockToMove.len)
                val emptyBlock = list.removeAt(indexToMoveTo)
                list.add(indexToMoveTo, blockToMove)
                val remainderEmpty = emptyBlock.len - blockToMove.len
                if (remainderEmpty > 0)
                    list.add(indexToMoveTo + 1, DiskBlock.Empty(remainderEmpty))
            }
            list.mergeEmptySpaces()
        }
    }

    // not necessary
    private fun MutableList<DiskBlock>.mergeEmptySpaces() {
        var i = 1
        while (i < size) {
            while (i < size && (get(i - 1) !is DiskBlock.Empty || get(i) !is DiskBlock.Empty))
                i++
            if (i >= size) return
            val totalLen = get(i - 1).len + removeAt(i).len
            set(i - 1, DiskBlock.Empty(totalLen))
        }
    }

    private fun compact(list: MutableList<DiskBlock>) {
        var i = 0
        while (i < list.size) {
            val block = list[i]
            if (block.len == 0)
                list.removeAt(i)
            else if (block is DiskBlock.Empty) {
                val lastBlock = list.removeLast()
                if (lastBlock is DiskBlock.Filled) {
                    val len = block.len.coerceAtMost(lastBlock.len)
                    list[i] = DiskBlock.Filled(len, lastBlock.id)
                    val remainderEmpty = block.len - len
                    if (remainderEmpty > 0)
                        list.add(i + 1, DiskBlock.Empty(remainderEmpty))
                    val remainderLast = lastBlock.len - len
                    if (remainderLast > 0)
                        list.add(DiskBlock.Filled(remainderLast, lastBlock.id))
                }
            } else i++
        }
    }
}
