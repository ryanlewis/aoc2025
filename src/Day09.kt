@Suppress("unused")
class Day09(
    input: List<String>,
) : Day(input) {
    private data class Point(
        val x: Long,
        val y: Long,
    )

    private data class Rect(
        val minX: Long,
        val maxX: Long,
        val minY: Long,
        val maxY: Long,
    ) {
        val area: Long get() = (maxX - minX + 1) * (maxY - minY + 1)

        val corners: List<Point>
            get() =
                listOf(
                    Point(minX, minY),
                    Point(minX, maxY),
                    Point(maxX, minY),
                    Point(maxX, maxY),
                )

        companion object {
            fun fromCorners(
                p1: Point,
                p2: Point,
            ) = Rect(
                minX = minOf(p1.x, p2.x),
                maxX = maxOf(p1.x, p2.x),
                minY = minOf(p1.y, p2.y),
                maxY = maxOf(p1.y, p2.y),
            )
        }
    }

    private val redTiles: List<Point> by lazy {
        input.map { line ->
            val (x, y) = line.split(",").map { it.toLong() }
            Point(x, y)
        }
    }

    // Polygon edges as pairs of consecutive points (wrapping around)
    private val edges: List<Pair<Point, Point>> by lazy {
        redTiles.zipWithNext() + (redTiles.last() to redTiles.first())
    }

    override fun part1(): String =
        redTilePairs()
            .maxOf { (p1, p2) -> Rect.fromCorners(p1, p2).area }
            .toString()

    override fun part2(): String =
        redTilePairs()
            .map { (p1, p2) -> Rect.fromCorners(p1, p2) }
            .filter { rect -> rect.corners.all { isInsideOrOnPolygon(it) } }
            .filter { rect -> !anyEdgeCrossesRectangle(rect) }
            .maxOf { it.area }
            .toString()

    private fun redTilePairs(): Sequence<Pair<Point, Point>> =
        sequence {
            for (i in redTiles.indices) {
                for (j in i + 1 until redTiles.size) {
                    yield(redTiles[i] to redTiles[j])
                }
            }
        }

    /**
     * Checks if any polygon edge passes through the interior of the rectangle.
     *
     * This is needed because a non-convex polygon can have all 4 rectangle corners
     * inside, but still have an edge cutting through the middle:
     *
     * ```
     *   ####
     *   #  #
     * ###  #    <- The polygon edge here would cut through a rectangle
     * #    #       formed by corners in the top-right and bottom-left
     * ######
     * ```
     *
     * We only check if edges cross the strict interior (not touching the boundary),
     * since edges on the boundary are valid (they're green tiles).
     */
    private fun anyEdgeCrossesRectangle(rect: Rect): Boolean =
        edges.any { (p1, p2) ->
            if (p1.x == p2.x) {
                // Vertical edge - crosses if x is strictly inside rect and y ranges overlap
                val edgeX = p1.x
                val edgeYRange = minOf(p1.y, p2.y)..maxOf(p1.y, p2.y)
                edgeX in (rect.minX + 1) until rect.maxX &&
                    edgeYRange.first < rect.maxY && edgeYRange.last > rect.minY
            } else {
                // Horizontal edge - crosses if y is strictly inside rect and x ranges overlap
                val edgeY = p1.y
                val edgeXRange = minOf(p1.x, p2.x)..maxOf(p1.x, p2.x)
                edgeY in (rect.minY + 1) until rect.maxY &&
                    edgeXRange.first < rect.maxX && edgeXRange.last > rect.minX
            }
        }

    /**
     * Determines if a point is inside or on the boundary of the polygon using ray casting.
     *
     * Ray casting works by shooting a horizontal ray from the point to the right
     * and counting how many polygon edges it crosses:
     * - Odd crossings -> inside
     * - Even crossings -> outside
     *
     * ```
     * ######
     * #    #
     * #  P------>  crosses 1 edge -> inside
     * #    #
     * ######
     *
     *    P-------> crosses 0 edges -> outside
     * ######
     * #    #
     * ```
     *
     * Edge case: We exclude the bottom endpoint of vertical edges to avoid
     * double-counting when the ray passes exactly through a vertex.
     */
    private fun isInsideOrOnPolygon(point: Point): Boolean {
        // Check if point is on any edge (boundary counts as inside)
        if (edges.any { (p1, p2) -> isOnSegment(point, p1, p2) }) {
            return true
        }

        // Ray casting - count vertical edges to the right that the horizontal ray crosses
        val crossings =
            edges.count { (p1, p2) ->
                p1.x == p2.x && // Vertical edge
                    p1.x > point.x && // To the right of point
                    point.y in (minOf(p1.y, p2.y) + 1)..maxOf(p1.y, p2.y) // Ray crosses edge (excluding bottom)
            }

        return crossings % 2 == 1
    }

    private fun isOnSegment(
        point: Point,
        p1: Point,
        p2: Point,
    ): Boolean =
        if (p1.x == p2.x) {
            // Vertical segment
            point.x == p1.x && point.y in minOf(p1.y, p2.y)..maxOf(p1.y, p2.y)
        } else {
            // Horizontal segment
            point.y == p1.y && point.x in minOf(p1.x, p2.x)..maxOf(p1.x, p2.x)
        }
}
