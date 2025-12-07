package utils

class CharGrid(private val text: String, private val empty: Char = '.') : AbstractGrid<Char>(text.calculateBounds()) {
    private val linePos = text.indexOf('\n')
    private val lineWidth = linePos + 1
    val height = (text.length + 1) / lineWidth
    val width = linePos
    val size = bounds
    override fun cell(p: Position): Char = cell(p.row, p.column)
    override fun cell(r: Int, c: Int): Char =
        if (r in bounds.rowRange && c in bounds.columnRange) text[indexFromPosition(r, c)] else empty

    val rows get() = (0..<height).map { row(it) }
    val columns get() = (0..<width).map { column(it) }
    override fun row(r: Int): GridLine<Char> = if (r in bounds.rowRange) CharRow(r) else EmptyLine(r, empty)
    inner class CharRow(override val index: Int) : GridLine<Char> {
        private val rowStart = index * lineWidth
        override val isEmpty: Boolean get() = bounds.columnRange.all { cell(it) == empty }
        override fun cell(idx: Int): Char = if (idx in bounds.columnRange) text[rowStart + idx] else empty
        override fun findAll(predicate: Grid<Char>.(Char) -> Boolean): Map<Int, Char> = buildMap {
            bounds.columnRange.forEach { c ->
                val cell = cell(c)
                if (predicate(cell)) set(c, cell)
            }
        }
    }

    override fun column(c: Int): GridLine<Char> = if (c in bounds.rowRange) CharCol(c) else EmptyLine(c, empty)
    inner class CharCol(override val index: Int) : GridLine<Char> {
        override val isEmpty: Boolean get() = bounds.rowRange.all { cell(it) == empty }
        override fun cell(idx: Int): Char = if (idx in bounds.rowRange) text[indexFromPosition(idx, index)] else empty
        override fun findAll(predicate: Grid<Char>.(Char) -> Boolean): Map<Int, Char> = buildMap {
            bounds.rowRange.forEach { r ->
                val cell = cell(r)
                if (predicate(cell)) set(r, cell)
            }
        }
    }

    private fun indexFromPosition(r: Int, c: Int) = r * lineWidth + c

    override fun forEachNonEmpty(action: Grid<Char>.(Position) -> Unit) {
        text.forEachIndexed { index, c ->
            if (indexInGrid(index) && c != empty)
                action(positionFromIndex(index))
        }
    }

    val nonEmptyCells
        get() = sequence {
            text.forEachIndexed { index, c ->
                if (indexInGrid(index) && c != empty)
                    yield(positionFromIndex(index) to c)
            }
        }

    private fun positionFromIndex(index: Int): Position {
        val r = index / lineWidth
        val c = index % lineWidth
        return Position(r, c)
    }

    override fun countNonEmpty(predicate: Grid<Char>.(Position) -> Boolean): Int =
        text.indices.count { idx ->
            if (indexInGrid(idx)) {
                val ch = text[idx]
                ch != empty && predicate(positionFromIndex(idx))
            } else false
        }

    private fun indexInGrid(idx: Int) = idx % lineWidth < width

    override fun transpose(): Grid<Char> = FilledCharGrid(text.lines()).transpose()

    override fun find(value: Char): Position {
        val index = text.indexOf(value)
        require(index >= 0) { "character $value not found in $this" }
        return positionFromIndex(index)
    }

    override fun findAll(value: Char): Set<Position> = buildSet {
        var index = text.indexOf(value)
        while (index >= 0) {
            add(positionFromIndex(index))
            index = text.indexOf(value, index + 1)
        }
    }

    override fun findAll(predicate: Grid<Char>.(Char) -> Boolean): Map<Position, Char> = buildMap {
        text.forEachIndexed { index, c ->
            if (indexInGrid(index) && predicate(c))
                put(positionFromIndex(index), c)
        }
    }

    fun with(position: Position, value: Char): CharGrid {
        val newText = StringBuilder(text)
        newText[indexFromPosition(position.row, position.column)] = value
        return CharGrid(newText.toString(), empty)
    }

    fun clearCells(positions: List<Position>): CharGrid {
        val newText = StringBuilder(text)
        positions.forEach { pos -> newText[indexFromPosition(pos.row, pos.column)] = empty }
        return CharGrid(newText.toString(), empty)
    }

    override fun toString() = buildString {
        bounds.rowRange.forEach { rowIndex ->
            bounds.columnRange.forEach { colIndex ->
                val pos = Position(rowIndex, colIndex)
                append(cellAsString(pos, cell(pos)))
            }
            append('\n')
        }
    }
}

fun String.calculateBounds(): ZeroBasedBounds {
    val linePos = indexOf('\n')
    val height = (length + 1) / (linePos + 1)
    return ZeroBasedBounds(height, linePos)
}
