class Day02(
    input: List<String>,
) : Day(input) {
    private val parsed by lazy { input.parse() }

    override fun part1(): String = sumMatchingIds(::isHalfMatch).toString()

    override fun part2(): String = sumMatchingIds(::isRepeatingPattern).toString()

    private fun sumMatchingIds(predicate: (Long) -> Boolean): Long =
        parsed
            .flatMap { it.asSequence() }
            .filter(predicate)
            .sum()

    /**
     * Checks if a number's string representation can be split into two equal halves.
     * e.g., 1212 -> "12" and "12" -> true
     */
    private fun isHalfMatch(id: Long): Boolean {
        val s = id.toString()
        if (s.length < 2 || s.length % 2 != 0) return false
        val (a, b) = s.chunked(s.length / 2)
        return a == b
    }

    /**
     * Determines if a number's digits form a repeating pattern (e.g., 123123 = "123" * 2).
     *
     * Uses the "string doubling" trick:
     * 1. Concatenate the string with itself: "abcabc" -> "abcabcabcabc"
     * 2. Search for the original string starting from index 1 (skip the trivial match at 0)
     * 3. If found before the halfway point, the string is repeating
     *
     * Why this works:
     * - When you double a repeating string, the repetitions "overlap" in the middle,
     *   creating new valid starting points for the original string.
     * - For "abcabc" + "abcabc" = "abcabcabcabc", we find "abcabc" at index 3.
     * - For non-repeating "abcdef" + "abcdef" = "abcdefabcdef", the only matches
     *   are at index 0 (trivial) and index 6 (trivial) — nothing in between.
     */
    private fun isRepeatingPattern(id: Long): Boolean {
        val s = id.toString()
        if (s.length < 2) return false
        val doubled = s + s
        val foundAt = doubled.indexOf(s, startIndex = 1)
        return foundAt < s.length
    }

    private fun List<String>.parse(): List<LongRange> {
        val lines = this.joinToString("").split(',')
        return lines
            .map { it.split('-') }
            .map { it[0].toLong()..it[1].toLong() }
    }
}
