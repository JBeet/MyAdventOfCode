package utils

import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readText

/**
 * Reads lines from the given input txt file.
 */
fun read2024(name: String) = Path("src/aoc2024/$name.txt").readText().trim()
fun read2025(name: String) = Path("src/aoc2025/$name.txt").readText().trim()

fun <T> List<T>.peek() = also { println(it) }
fun <T> Sequence<T>.peek() = onEach { println(it) }

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

fun String.nonEmptyLines(): List<String> = lines().filter { it.isNotBlank() }
fun String.findWithRegex(pattern: String) = Regex(pattern).findAll(this).map { it.value }
fun String.ints() = signedInts().also { list -> check(list.all { it > 0 }) { "Expected positive ints, but got $list" } }
fun String.intsOrZero() = signedInts().also { list -> check(list.all { it >= 0 }) }
fun String.signedInts(): List<Int> = bigIntegers().mapTo(mutableListOf()) { it.intValueExact() }
fun String.longs() = signedLongs().also { longs -> check(longs.all { it > 0 }) }
fun String.longsOrZero() = signedLongs().also { longs -> check(longs.all { it >= 0 }) }
fun String.signedLongs(): List<Long> = bigIntegers().mapTo(mutableListOf()) { it.longValueExact() }

fun String.bigIntegers() = findWithRegex("""-?\d+""").map { it.toBigInteger() }
fun String.paragraphs(): List<String> = split(Regex("\\R\\R"))
fun String.intLists(): List<List<Int>> = nonEmptyLines().map { it.ints() }
fun String.longLists() = nonEmptyLines().map { it.longs() }

@JvmName("transposeStrings")
fun List<String>.transpose(): List<String> = (0..<maxOf { it.length }).map { this.column(it) }
fun List<String>.column(c: Int): String =
    buildString { this@column.forEach { s -> append(if (c < s.length) s[c] else ' ') } }

@JvmName("transposeLists")
fun <T> List<List<T>>.transpose(): List<List<T>> = this[0].indices.map { this.column(it) }
fun <T> List<List<T>>.column(c: Int): List<T> = map { it[c] }

fun <T> List<T>.splitBy(splitter: (T) -> Boolean): Sequence<List<T>> = sequence {
    val cur = mutableListOf<T>()
    forEach { item ->
        if (splitter(item)) {
            yield(cur.toList())
            cur.clear()
        } else
            cur.add(item)
    }
    if (cur.isNotEmpty()) yield(cur)
}

fun <E> List<E>.sublistBefore(item: E, missingDelimiterValue: List<E> = this): List<E> {
    val pos = indexOf(item)
    return if (pos < 0) missingDelimiterValue else subList(0, pos)
}

fun <E> List<E>.sublistAfter(item: E, missingDelimiterValue: List<E> = this): List<E> {
    val pos = indexOf(item)
    return if (pos < 0) missingDelimiterValue else subList(pos + 1, size)
}

fun <E> MutableList<E>.swapItem(itemA: E, itemB: E) = swapIndex(indexOf(itemA), indexOf(itemB))
fun <E> MutableList<E>.swapIndex(indexA: Int, indexB: Int) {
    val temp = this[indexA]
    this[indexA] = this[indexB]
    this[indexB] = temp
}

fun Int.concat(o: Int) = (toString() + o.toString()).toInt()
fun Long.concat(o: Long) = (toString() + o.toString()).toLong()

val naturalNumberInts: Sequence<Int> = generateSequence(0) { it + 1 }
val naturalNumberLongs: Sequence<Long> = generateSequence(0L) { it + 1L }
val positiveIntegers: Sequence<Int> = generateSequence(1) { it + 1 }
val positiveLongs: Sequence<Long> = generateSequence(1L) { it + 1L }

fun Iterable<Long>.addToAll(delta: Long) = map { it + delta }

fun Sequence<Long>.toRepeating(): Sequence<Long> = iterator().toRepeating()

private fun Iterator<Long>.toRepeating(): Sequence<Long> {
    val minimalCycleCount = 20
    val values = mutableListOf<Long>()
    val maxDetectedCycleLength = 10_000
    (2..maxDetectedCycleLength).forEach { cycleLength ->
        val requestedSize = cycleLength * minimalCycleCount
        while (hasNext() && values.size < requestedSize) values.add(next())
        if (!hasNext()) return values.asSequence()
        val difference = values[cycleLength] - values[0]
        if ((0..<values.size - cycleLength).all { values[it + cycleLength] - values[it] == difference }) {
            val repeatingValues = values.take(cycleLength)
            return repeatingSequence(repeatingValues, difference)
        }
    }
    error("no cycle for ${values.take(100)}")
}

private fun repeatingSequence(values: List<Long>, difference: Long): Sequence<Long> =
    naturalNumberLongs.map { cycleNr -> cycleNr * difference }.flatMap { offset -> values.addToAll(offset) }

fun leastCommonMultiple(a: Long, b: Long): Long = (a * b) / greatestCommonDivisor(a, b)
tailrec fun greatestCommonDivisor(a: Long, b: Long): Long {
    check(b > 0L) { "No GCD for $a, $b" }
    val mod = a % b
    return if (mod == 0L) b else greatestCommonDivisor(b, mod)
}

fun <E> List<E>.combinations(): Sequence<List<E>> =
    if (isEmpty())
        sequenceOf(emptyList())
    else {
        val (item, remainder) = headToTail()
        remainder.combinations() + remainder.combinations().map { listOf(item) + it }
    }

fun <E> List<E>.combinationsOfSize(size: Int): Sequence<List<E>> = generateCombinationsOfSize(size, emptyList())
private fun <E> List<E>.generateCombinationsOfSize(size: Int, prefix: List<E>): Sequence<List<E>> =
    if (size == 0)
        sequenceOf(prefix)
    else if (size > this.size)
        emptySequence()
    else if (size == this.size)
        sequenceOf(prefix + this)
    else {
        val (item, remainder) = headToTail()
        remainder.generateCombinationsOfSize(size, prefix) +
                remainder.generateCombinationsOfSize(size - 1, prefix + item)
    }

fun <E> List<E>.headToTail(): Pair<E, List<E>> {
    val head = get(0)
    val tail = subList(1, size)
    return head to tail
}

fun <E> Collection<E>.permutations(): Sequence<List<E>> = toMutableList().generatePermutations(size - 1)

private fun <E> MutableList<E>.generatePermutations(k: Int): Sequence<List<E>> = when {
    k == 0 -> sequenceOf(toList())
    k % 2 != 0 -> sequence {
        yieldAll(generatePermutations(k - 1))
        repeat(k) {
            swapIndex(it, k)
            yieldAll(generatePermutations(k - 1))
        }
    }

    else -> sequence {
        yieldAll(generatePermutations(k - 1))
        repeat(k) {
            swapIndex(0, k)
            yieldAll(generatePermutations(k - 1))
        }
    }
}

fun verify(expected: Long, actual: Int) {
    check(actual.toLong() == expected) { "Expected $expected but was $actual" }
}

fun verify(expected: Long, actual: Long) {
    check(actual == expected) { "Expected $expected but was $actual" }
}

fun verify(expected: String, actual: String) {
    check(actual == expected) { "Expected $expected but was $actual" }
}
