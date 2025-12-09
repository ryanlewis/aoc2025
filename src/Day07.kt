@Suppress("unused")
class Day07(
    input: List<String>,
) : Day(input) {
    private val startCol = input[0].indexOf('S')

    override fun part1(): String {
        // Track which columns have active beams (set handles merging automatically)
        var activeColumns = setOf(startCol)
        var splitCount = 0

        for (row in 1 until input.size) {
            val newColumns = mutableSetOf<Int>()
            for (col in activeColumns) {
                if (input[row][col] == '^') {
                    splitCount++
                    newColumns.add(col - 1)
                    newColumns.add(col + 1)
                } else {
                    newColumns.add(col)
                }
            }
            activeColumns = newColumns
        }

        return splitCount.toString()
    }

    override fun part2(): String {
        // Track how many timelines have a particle at each column
        var timelines = mapOf(startCol to 1L)

        for (row in 1 until input.size) {
            val newTimelines = mutableMapOf<Int, Long>()
            for ((col, count) in timelines) {
                if (input[row][col] == '^') {
                    // Each timeline splits into two (left and right)
                    // merge() adds count to existing value, or sets it if key absent
                    newTimelines.merge(col - 1, count, Long::plus)
                    newTimelines.merge(col + 1, count, Long::plus)
                } else {
                    newTimelines.merge(col, count, Long::plus)
                }
            }
            timelines = newTimelines
        }

        return timelines.values.sum().toString()
    }
}
