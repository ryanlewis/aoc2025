@Suppress("unused")
class Day08(
    input: List<String>,
) : Day(input) {
    private data class Point3D(
        val x: Int,
        val y: Int,
        val z: Int,
    )

    /**
     * Disjoint Sets data structure (also known as Union-Find).
     *
     * Tracks a collection of non-overlapping sets, supporting efficient
     * union and find operations. Uses path compression and union by rank
     * for near-constant time operations.
     *
     * Elements are identified by integer indices (0 until size), not by their
     * actual values - so we track indices into the points list, not the 3D
     * coordinates themselves.
     *
     * Useful in this case for tracking which junction boxes are connected
     * into circuits - each set represents a circuit, and union merges two
     * circuits when we connect a pair of junction boxes.
     */
    private class DisjointSets(
        size: Int,
    ) {
        private val parent = IntArray(size) { it }
        private val rank = IntArray(size) { 0 }
        var setCount = size
            private set

        fun find(x: Int): Int {
            if (parent[x] != x) {
                parent[x] = find(parent[x])
            }
            return parent[x]
        }

        // Returns true if a merge actually happened (different sets)
        fun union(
            x: Int,
            y: Int,
        ): Boolean {
            val rootX = find(x)
            val rootY = find(y)
            if (rootX == rootY) return false

            when {
                rank[rootX] < rank[rootY] -> parent[rootX] = rootY
                rank[rootX] > rank[rootY] -> parent[rootY] = rootX
                else -> {
                    parent[rootY] = rootX
                    rank[rootX]++
                }
            }
            setCount--
            return true
        }

        fun setSizes(): List<Int> =
            parent.indices
                .groupBy { find(it) }
                .values
                .map { it.size }
    }

    private val points: List<Point3D> by lazy {
        input.map { line ->
            val (x, y, z) = line.split(",").map { it.toInt() }
            Point3D(x, y, z)
        }
    }

    private data class PointPair(
        val i: Int,
        val j: Int,
        val distSquared: Long,
    )

    private val sortedPairs: List<PointPair> by lazy {
        buildList {
            for (i in points.indices) {
                for (j in i + 1 until points.size) {
                    val dx = (points[j].x - points[i].x).toLong()
                    val dy = (points[j].y - points[i].y).toLong()
                    val dz = (points[j].z - points[i].z).toLong()
                    add(PointPair(i, j, dx * dx + dy * dy + dz * dz))
                }
            }
        }.sortedBy { it.distSquared }
    }

    override fun part1(): String {
        val sets = DisjointSets(points.size)

        for ((i, j, _) in sortedPairs.take(1000)) {
            sets.union(i, j)
        }

        val sizes = sets.setSizes().sortedDescending()
        return (sizes[0].toLong() * sizes[1] * sizes[2]).toString()
    }

    override fun part2(): String {
        val sets = DisjointSets(points.size)

        var lastMerge: Pair<Int, Int>? = null
        for ((i, j, _) in sortedPairs) {
            if (sets.union(i, j)) {
                lastMerge = i to j
                if (sets.setCount == 1) break
            }
        }

        val (i, j) = lastMerge!!
        return (points[i].x.toLong() * points[j].x).toString()
    }
}
