@Suppress("unused")
class Day06(
    input: List<String>,
) : Day(input) {
    private val problems: List<Problem> by lazy { input.parseProblems() }

    override fun part1(): String = problems.sumOf { it.solve() }.toString()

    override fun part2(): String = input.parseCephalopodProblems().sumOf { it.solve() }.toString()
}

private data class Problem(
    val numbers: List<Long>,
    val operator: Char,
)

private fun Problem.solve(): Long =
    when (operator) {
        '+' -> numbers.sum()
        '*' -> numbers.fold(1L) { acc, n -> acc * n }
        else -> error("Unknown operator: $operator")
    }

private fun List<String>.parseProblems(): List<Problem> {
    val rows = map { it.trim().split("\\s+".toRegex()) }
    val operators = rows.last()
    val numberRows = rows.dropLast(1)

    return operators.indices.map { i ->
        val numbers = numberRows.map { row -> row[i].toLong() }
        val op = operators[i].first()
        Problem(numbers, op)
    }
}

private fun List<String>.parseCephalopodProblems(): List<Problem> {
    val maxLen = maxOf { it.length }
    val padded = map { it.padEnd(maxLen) }

    // Transpose: columns become rows
    val transposed =
        (0 until maxLen).map { col ->
            padded.map { row -> row[col] }.joinToString("")
        }

    // Split by delimiter rows (all spaces), process right-to-left
    return transposed
        .splitBy { it.all { c -> c == ' ' } }
        .reversed()
        .map { group ->
            val numbers =
                group.map { row ->
                    row.dropLast(1).filter { it.isDigit() }.toLong()
                }
            val op = group.first().last()
            Problem(numbers, op)
        }
}

/**
 * Splits a list into sublists, using elements matching the predicate as delimiters.
 * Delimiter elements are not included in the result. Empty sublists are filtered out.
 *
 * Example: listOf("a", "", "b", "c", "", "d").splitBy { it.isEmpty() }
 *          -> [["a"], ["b", "c"], ["d"]]
 */
private fun <T> List<T>.splitBy(predicate: (T) -> Boolean): List<List<T>> =
    fold(mutableListOf(mutableListOf<T>())) { acc, item ->
        if (predicate(item)) {
            if (acc.last().isNotEmpty()) acc.add(mutableListOf())
        } else {
            acc.last().add(item)
        }
        acc
    }.filter { it.isNotEmpty() }
