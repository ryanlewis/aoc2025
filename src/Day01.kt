@Suppress("unused")
class Day01(
    input: List<String>,
) : Day(input) {
    private val parsed by lazy { input.parse() }

    override fun part1(): String {
        val (_, count) =
            parsed.fold(START_POSITION to 0) { (pos, count), move ->
                val newPos = (pos + move.direction.sign * move.value).mod(DIAL_SIZE)
                newPos to (count + if (newPos == 0) 1 else 0)
            }
        return count.toString()
    }

    override fun part2(): String {
        val (_, count) =
            parsed.fold(START_POSITION to 0) { (pos, count), move ->
                val delta = move.direction.sign * move.value
                val newPos = (pos + delta).mod(DIAL_SIZE)
                newPos to (count + countZeroCrossings(pos, delta))
            }
        return count.toString()
    }

    private fun countZeroCrossings(
        pos: Int,
        delta: Int,
    ): Int {
        val newPos = pos + delta
        return if (delta >= 0) {
            newPos.floorDiv(DIAL_SIZE)
        } else {
            // The -1 shift puts position 0 in the "previous" bucket, so:
            // - pos=0 → 99 (delta=-1): (-1).floorDiv(100) - (-2).floorDiv(100) = 0 crossings
            //   (started at boundary, didn't cross it)
            // - pos=1 → 99 (delta=-2): (0).floorDiv(100) - (-2).floorDiv(100) = 1 crossing
            //   (passed through 0)
            (pos - 1).floorDiv(DIAL_SIZE) - (newPos - 1).floorDiv(DIAL_SIZE)
        }
    }

    private enum class Direction(
        val sign: Int,
    ) {
        L(-1),
        R(1),
    }

    private data class Move(
        val direction: Direction,
        val value: Int,
    )

    private fun List<String>.parse() =
        map { line ->
            Move(Direction.valueOf(line.take(1)), line.drop(1).toInt())
        }

    private companion object {
        private const val DIAL_SIZE = 100
        private const val START_POSITION = 50
    }
}
