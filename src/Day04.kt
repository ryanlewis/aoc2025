private const val MAX_ADJACENT_ROLLS = 4

@Suppress("unused")
class Day04(
    input: List<String>,
) : Day(input) {
    private data class Coord(
        val row: Int,
        val col: Int,
    ) {
        fun neighbours(): List<Coord> =
            listOf(
                Coord(row - 1, col - 1),
                Coord(row - 1, col),
                Coord(row - 1, col + 1),
                Coord(row, col - 1),
                Coord(row, col + 1),
                Coord(row + 1, col - 1),
                Coord(row + 1, col),
                Coord(row + 1, col + 1),
            )
    }

    private val rolls: Set<Coord> by lazy {
        input
            .flatMapIndexed { row, line ->
                line.mapIndexedNotNull { col, char ->
                    if (char == '@') Coord(row, col) else null
                }
            }.toSet()
    }

    private fun Coord.isAccessible(rolls: Set<Coord>) = neighbours().count { it in rolls } < MAX_ADJACENT_ROLLS

    override fun part1(): String = rolls.count { it.isAccessible(rolls) }.toString()

    override fun part2(): String {
        val remaining = rolls.toMutableSet()
        var totalRemoved = 0

        while (true) {
            val accessible = remaining.filter { it.isAccessible(remaining) }
            if (accessible.isEmpty()) break
            totalRemoved += accessible.size
            remaining.removeAll(accessible.toSet())
        }

        return totalRemoved.toString()
    }
}
