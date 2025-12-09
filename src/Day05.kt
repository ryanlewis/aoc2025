@Suppress("unused")
class Day05(
    input: List<String>,
) : Day(input) {
    private val ranges: List<LongRange> by lazy { input.parseRanges() }
    private val ids: List<Long> by lazy { input.parseIds() }

    override fun part1(): String = ids.count { id -> ranges.any { range -> id in range } }.toString()

    override fun part2(): String = ranges.merged().sumOf { it.last - it.first + 1 }.toString()
}

private fun List<String>.parseRanges(): List<LongRange> {
    val blankIndex = this.indexOf("")
    return this.take(blankIndex).map { line ->
        val (start, end) = line.split("-").map { it.toLong() }
        start..end
    }
}

private fun List<String>.parseIds(): List<Long> {
    val blankIndex = this.indexOf("")
    return this.drop(blankIndex + 1).map { it.toLong() }
}

/**
 * Merge overlapping or adjacent ranges into a minimal list.
 * After sorting by start, we only need to compare each range with the last merged one:
 * if they overlap or touch (range.first <= last.last + 1), extend; otherwise start a new range.
 */
private fun List<LongRange>.merged(): List<LongRange> {
    if (isEmpty()) return emptyList()
    val sorted = this.sortedBy { it.first }
    return sorted.drop(1).fold(mutableListOf(sorted.first())) { acc, range ->
        val last = acc.last()
        if (range.first <= last.last + 1) {
            // overlaps, merge
            acc[acc.lastIndex] = last.first..maxOf(last.last, range.last)
        } else {
            // gap, new range
            acc += range
        }
        acc
    }
}
