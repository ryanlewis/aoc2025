class Day03(
    input: List<String>,
) : Day(input) {
    val parsed by lazy { input.parse() }

    override fun part1(): String = parsed.sumOf { it.selectLargest(2).toNumber() }.toString()

    override fun part2(): String = parsed.sumOf { it.selectLargest(12).toNumber() }.toString()

    /**
     * Selects [count] digits from this list that form the largest possible number
     * while preserving their original order. Uses a greedy monotonic stack algorithm:
     * process digits left-to-right, removing smaller digits when a larger one appears
     * (if removals remain), ensuring larger digits end up as far left as possible.
     */
    private fun List<Int>.selectLargest(count: Int): List<Int> {
        var toRemove = size - count
        val stack = mutableListOf<Int>()
        for (digit in this) {
            while (stack.isNotEmpty() && toRemove > 0 && stack.last() < digit) {
                stack.removeLast()
                toRemove--
            }
            stack.add(digit)
        }
        return stack.take(count)
    }

    private fun List<Int>.toNumber(): Long = fold(0L) { acc, d -> acc * 10 + d }

    private fun List<String>.parse(): List<List<Int>> = map { bank -> bank.map { it.digitToInt() } }
}
