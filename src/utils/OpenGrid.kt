package utils

@JvmName("parseStringCellMap")
fun <R : Any> parseCellMap(input: List<String>, cellConstruction: (Char) -> R?): Map<Position, R> =
    input.flatMapIndexed { rowIndex, rowData ->
        rowData.mapIndexedNotNull { colIndex, cellData ->
            val cell = cellConstruction(cellData)
            if (cell == null) null else Position(rowIndex, colIndex) to cell
        }
    }.toMap()

@JvmName("parseListCellMap")
fun <T, R : Any> parseCellMap(input: List<List<T>>, cellConstruction: (T) -> R?): Map<Position, R> =
    input.flatMapIndexed { rowIndex, rowData ->
        rowData.mapIndexedNotNull { colIndex, cellData ->
            val cell = cellConstruction(cellData)
            if (cell == null) null else Position(rowIndex, colIndex) to cell
        }
    }.toMap()

fun openCharCells(input: List<String>, empty: Char = '.') = parseCellMap(input) { it.takeUnless { it == empty } }
fun boundsFrom(input: List<String>) = ZeroBasedBounds(input.size, input[0].length)

open class OpenCharGrid(cells: Map<Position, Char>, final override val bounds: ZeroBasedBounds, empty: Char = '.') :
    OpenGrid<Char>(cells, empty, bounds) {
    val height = bounds.height
    val width = bounds.width
    override fun transpose() = OpenCharGrid(transposeCells(), bounds, empty)
    open fun rotate90() = OpenCharGrid(rotateCells90(), bounds.swap(), empty)
    fun rotateCells90(): Map<Position, Char> = rotateCells90(height)
    fun rotateCells180(): Map<Position, Char> = rotateCells180(height, width)
    fun rotateCells270(): Map<Position, Char> = rotateCells270(width)
}

open class OpenGrid<C>(
    val cells: Map<Position, C>, protected val empty: C, bounds: Bounds = DetectBounds(cells.keys)
) : AbstractGrid<C>(bounds) {
    constructor(cells: List<List<C>>, empty: C) : this(cells.flatMapIndexed { rowIndex: Int, rowData: List<C> ->
        rowData.mapIndexedNotNull { colIndex, c -> if (c == empty) null else (Position(rowIndex, colIndex) to c) }
    }.toMap(), empty, ZeroBasedBounds(cells.size, cells.maxOf { it.size }))

    constructor(cells: Map<Position, C>, empty: C, height: Int, width: Int) :
            this(cells, empty, ZeroBasedBounds(height, width))

    val rows get() = bounds.rowRange.asSequence().map { row(it) }
    val columns get() = bounds.columnRange.asSequence().map { column(it) }

    private val cellsByRow: Map<Int, Map<Int, C>> by lazy {
        cells.entries.groupBy { it.key.row }.mapValues { (_, list) ->
            list.associate { it.key.column to it.value }
        }
    }
    private val cellsByColumn by lazy {
        cells.entries.groupBy { it.key.column }.mapValues { (_, list) ->
            list.associate { it.key.row to it.value }
        }
    }

    override fun row(r: Int): GridLine<C> =
        if (bounds.hasRow(r)) OpenGridLine(this, r, cellsByRow[r] ?: emptyMap(), empty) else EmptyLine(r, empty)

    override fun column(c: Int) =
        if (bounds.hasColumn(c)) OpenGridLine(this, c, cellsByColumn[c] ?: emptyMap(), empty) else EmptyLine(c, empty)

    override fun cell(p: Position) = cells[p] ?: empty
    override fun cell(r: Int, c: Int) = cell(Position(r, c))
    override fun findAll(predicate: Grid<C>.(C) -> Boolean): Map<Position, C> = cells.filterValues { predicate(it) }

    override fun forEachNonEmpty(action: Grid<C>.(Position) -> Unit) = cells.forEach { (pos, _) -> action(pos) }
    override fun countNonEmpty(predicate: Grid<C>.(Position) -> Boolean): Int =
        cells.count { (pos, _) -> predicate(pos) }

    override fun transpose() = OpenGrid(transposeCells(), empty)
    fun transposeCells(): Map<Position, C> = cells.mapKeys { (pos, _) -> pos.transpose() }
    fun rotateCells90(height: Int): Map<Position, C> = cells.mapKeys { (pos, _) -> pos.rotate90(height) }
    fun rotateCells180(height: Int, width: Int): Map<Position, C> =
        cells.mapKeys { (pos, _) -> pos.rotate180(height, width) }

    fun rotateCells270(width: Int): Map<Position, C> = cells.mapKeys { (pos, _) -> pos.rotate270(width) }

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

private data class OpenGridLine<C>(
    private val grid: Grid<C>, override val index: Int, private val cells: Map<Int, C>, private val empty: C
) : GridLine<C> {
    override val isEmpty: Boolean get() = cells.all { it == empty }
    override fun cell(idx: Int): C = cells[idx] ?: empty
    override fun findAll(predicate: Grid<C>.(C) -> Boolean): Map<Int, C> = cells.filterValues { grid.predicate(it) }
}
