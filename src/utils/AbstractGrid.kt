package utils

abstract class AbstractGrid<C>(override val bounds: Bounds) : BoundedGrid<C> {
    fun connections(pos: Position) = connections(pos, cell(pos))
    open fun connections(pos: Position, cell: C): Iterable<Position> = deltas(pos, cell).map { pos + it.delta }
    open fun deltas(pos: Position, cell: C): Collection<Delta> =
        if (cell is GridCell) cell.directions else emptySet()

    fun floodFill(
        startingPosition: Position,
        shouldContinue: (Position, Position) -> Boolean = { prevPosition, nextPosition ->
            get(prevPosition) == get(nextPosition)
        }
    ): Set<Position> {
        val visited = mutableSetOf<Position>()
        val traverse = DeepRecursiveFunction<Position, Unit> { currentPosition ->
            visited.add(currentPosition)
            currentPosition.neighbours4.forEach { nextPosition ->
                if (shouldContinue(currentPosition, nextPosition) && nextPosition !in visited)
                    callRecursive(nextPosition)
            }
        }
        traverse(startingPosition)
        return visited
    }

    interface GridTraverse<R> {
        val initialPosition: Position
        val initialValue: R
        fun shouldUpdate(pos: Position, oldValue: R, curValue: R): Boolean
        fun calculateNextValue(pos: Position, oldValue: R): R
        fun nextPositions(pos: Position, curValue: R): Iterable<Position>
    }

    fun <R> traverse(spec: GridTraverse<R>): Map<Position, R> {
        val target = mutableMapOf<Position, R>()
        val traverse = DeepRecursiveFunction<Pair<Position, R>, Unit> { (pos, acc) ->
            val known = target[pos]
            if (known == null || spec.shouldUpdate(pos, known, acc)) {
                target[pos] = acc
                val result = spec.calculateNextValue(pos, acc)
                spec.nextPositions(pos, acc).forEach { nextPosition ->
                    callRecursive(nextPosition to result)
                }
            }
        }
        traverse(spec.initialPosition to spec.initialValue)
        return target
    }

    override operator fun contains(pos: Position) = pos in bounds

    fun forEachWithEmpty(action: (Position) -> Unit) = bounds.rowRange.forEach { rowIndex ->
        bounds.columnRange.forEach { colIndex -> action(Position(rowIndex, colIndex)) }
    }

    fun countWithEmpty(predicate: (Position) -> Boolean) = bounds.rowRange.sumOf { rowIndex ->
        bounds.columnRange.count { colIndex -> predicate(Position(rowIndex, colIndex)) }
    }

    open fun cellAsString(pos: Position, cell: C): String = when (connections(pos, cell).toSet()) {
        emptySet<Direction>() -> cell.toString()
        else -> charFor(deltas(pos, cell).toSet())?.toString() ?: cell.toString()
    }

    private fun charFor(directions: Set<Delta>): Char? = when (directions) {
        emptySet<Direction>() -> ' '
        setOf(Direction.N) -> '╵'
        setOf(Direction.E) -> '╶'
        setOf(Direction.S) -> '╷'
        setOf(Direction.W) -> '╴'
        setOf(Direction.N, Direction.E) -> '└'
        setOf(Direction.N, Direction.S) -> '│'
        setOf(Direction.N, Direction.W) -> '┘'
        setOf(Direction.E, Direction.S) -> '┌'
        setOf(Direction.E, Direction.W) -> '─'
        setOf(Direction.S, Direction.W) -> '┐'
        setOf(Direction.N, Direction.E, Direction.S) -> '├'
        setOf(Direction.N, Direction.E, Direction.W) -> '┴'
        setOf(Direction.N, Direction.S, Direction.W) -> '┤'
        setOf(Direction.E, Direction.S, Direction.W) -> '┬'
        setOf(Direction.N, Direction.E, Direction.S, Direction.W) -> '┼'
        else -> null
    }
}
