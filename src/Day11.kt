@Suppress("unused")
class Day11(
    input: List<String>,
) : Day(input) {
    private val graph: Map<String, List<String>> by lazy { parseGraph(input) }

    override fun part1(): String = countPaths("you", "out").toString()

    /**
     * Count all distinct paths from [start] to [end] using DFS.
     * Each path is a unique sequence of edges, so we count recursively.
     */
    private fun countPaths(
        start: String,
        end: String,
    ): Long {
        if (start == end) return 1L

        val neighbours = graph[start] ?: return 0L
        return neighbours.sumOf { next -> countPaths(next, end) }
    }

    override fun part2(): String = countPathsWithRequired("svr", "out", setOf("dac", "fft")).toString()

    /**
     * Count paths from [start] to [end] that visit all [required] nodes.
     * Uses memoisation keyed on (currentNode, visitedRequiredMask).
     */
    private fun countPathsWithRequired(
        start: String,
        end: String,
        required: Set<String>,
    ): Long {
        val requiredList = required.toList()
        val cache = mutableMapOf<Pair<String, Int>, Long>()

        fun dfs(
            current: String,
            visitedMask: Int,
        ): Long {
            // Update mask if current node is a required node
            val newMask =
                requiredList.foldIndexed(visitedMask) { idx, mask, req ->
                    if (current == req) mask or (1 shl idx) else mask
                }

            if (current == end) {
                // Only count if all required nodes visited
                return if (newMask == (1 shl required.size) - 1) 1L else 0L
            }

            val cacheKey = current to newMask
            cache[cacheKey]?.let { return it }

            val neighbours = graph[current] ?: return 0L
            val result = neighbours.sumOf { next -> dfs(next, newMask) }

            cache[cacheKey] = result
            return result
        }

        return dfs(start, 0)
    }

    private fun parseGraph(lines: List<String>): Map<String, List<String>> =
        lines.associate { line ->
            val parts = line.split(": ")
            val device = parts[0]
            val outputs = parts[1].split(" ")
            device to outputs
        }
}
