class Day12(
    input: List<String>,
) : Day(input) {
    // A shape is a set of (row, col) offsets from top-left
    private data class Shape(
        val cells: Set<Pair<Int, Int>>,
    ) {
        val height: Int = cells.maxOf { it.first } + 1
        val width: Int = cells.maxOf { it.second } + 1

        // Rotate 90 degrees clockwise: (r, c) -> (c, height-1-r)
        fun rotate(): Shape {
            val rotated = cells.map { (r, c) -> Pair(c, height - 1 - r) }.toSet()
            return normalise(rotated)
        }

        // Flip horizontally: (r, c) -> (r, width-1-c)
        fun flip(): Shape {
            val flipped = cells.map { (r, c) -> Pair(r, width - 1 - c) }.toSet()
            return normalise(flipped)
        }

        // Normalise so top-left is (0,0)
        private fun normalise(cells: Set<Pair<Int, Int>>): Shape {
            val minR = cells.minOf { it.first }
            val minC = cells.minOf { it.second }
            return Shape(cells.map { (r, c) -> Pair(r - minR, c - minC) }.toSet())
        }

        // Get all unique orientations (up to 8)
        fun allOrientations(): Set<Shape> {
            val orientations = mutableSetOf<Shape>()
            var current = this
            repeat(4) {
                orientations.add(current)
                orientations.add(current.flip())
                current = current.rotate()
            }
            return orientations
        }
    }

    private data class Region(
        val width: Int,
        val height: Int,
        val counts: List<Int>,
    )

    // Parse input into shapes and regions
    private val shapes: List<Shape>
    private val regions: List<Region>

    init {
        val sections = input.joinToString("\n").split("\n\n")

        // Parse shapes
        shapes =
            sections.dropLast(1).map { section ->
                val lines = section.lines()
                // First line is "N:" - skip it
                val gridLines = lines.drop(1)
                val cells = mutableSetOf<Pair<Int, Int>>()
                gridLines.forEachIndexed { row, line ->
                    line.forEachIndexed { col, char ->
                        if (char == '#') cells.add(Pair(row, col))
                    }
                }
                Shape(cells)
            }

        // Parse regions (last section contains all region lines)
        val regionLines = sections.last().lines().filter { it.contains("x") }
        regions =
            regionLines.map { line ->
                val (dims, countsStr) = line.split(": ")
                val (w, h) = dims.split("x").map { it.toInt() }
                val counts = countsStr.split(" ").map { it.toInt() }
                Region(w, h, counts)
            }
    }

    // Pre-compute all orientations for each shape
    private val shapeOrientations: List<Set<Shape>> by lazy {
        shapes.map { it.allOrientations() }
    }

    // Check if a shape can be placed at (startR, startC) on the grid
    private fun canPlace(
        grid: Array<BooleanArray>,
        shape: Shape,
        startR: Int,
        startC: Int,
        height: Int,
        width: Int,
    ): Boolean {
        for ((dr, dc) in shape.cells) {
            val r = startR + dr
            val c = startC + dc
            if (r < 0 || r >= height || c < 0 || c >= width) return false
            if (grid[r][c]) return false
        }
        return true
    }

    // Place a shape on the grid
    private fun place(
        grid: Array<BooleanArray>,
        shape: Shape,
        startR: Int,
        startC: Int,
    ) {
        for ((dr, dc) in shape.cells) {
            grid[startR + dr][startC + dc] = true
        }
    }

    // Remove a shape from the grid
    private fun remove(
        grid: Array<BooleanArray>,
        shape: Shape,
        startR: Int,
        startC: Int,
    ) {
        for ((dr, dc) in shape.cells) {
            grid[startR + dr][startC + dc] = false
        }
    }

    // Find the first empty cell (top-left) that's not blocked
    private fun findFirstEmpty(
        grid: Array<BooleanArray>,
        blocked: Array<BooleanArray>,
        height: Int,
        width: Int,
    ): Pair<Int, Int>? {
        for (r in 0 until height) {
            for (c in 0 until width) {
                if (!grid[r][c] && !blocked[r][c]) return Pair(r, c)
            }
        }
        return null
    }

    // Solve using backtracking - try to place all presents
    private fun solve(region: Region): Boolean {
        val height = region.height
        val width = region.width

        // Quick check: total cells needed vs available
        val totalCellsNeeded = region.counts.sum() * 7 // Each shape has 7 cells
        if (totalCellsNeeded > height * width) return false

        val grid = Array(height) { BooleanArray(width) }

        // Track which cells are "blocked" (intentionally left empty)
        val blocked = Array(height) { BooleanArray(width) }

        val remaining = region.counts.toMutableList()
        val slack = height * width - totalCellsNeeded

        return backtrack(grid, blocked, remaining, height, width, slack)
    }

    private fun backtrack(
        grid: Array<BooleanArray>,
        blocked: Array<BooleanArray>,
        remaining: MutableList<Int>,
        height: Int,
        width: Int,
        slack: Int,
    ): Boolean {
        // If all shapes placed, success!
        if (remaining.all { it == 0 }) {
            return true
        }

        // Find first empty cell (not filled and not blocked)
        val empty = findFirstEmpty(grid, blocked, height, width) ?: return false

        val (targetR, targetC) = empty

        // Try each shape type that we still need to place
        for (shapeIdx in remaining.indices) {
            if (remaining[shapeIdx] <= 0) continue

            // Try each orientation
            for (orientation in shapeOrientations[shapeIdx]) {
                // Find positions within this orientation that could cover (targetR, targetC)
                for ((dr, dc) in orientation.cells) {
                    val startR = targetR - dr
                    val startC = targetC - dc

                    if (canPlace(grid, orientation, startR, startC, height, width)) {
                        // Place it
                        place(grid, orientation, startR, startC)
                        remaining[shapeIdx]--

                        if (backtrack(grid, blocked, remaining, height, width, slack)) {
                            return true
                        }

                        // Backtrack
                        remaining[shapeIdx]++
                        remove(grid, orientation, startR, startC)
                    }
                }
            }
        }

        // No shape could cover this cell - try leaving it empty (if we have slack)
        if (slack > 0) {
            blocked[targetR][targetC] = true
            if (backtrack(grid, blocked, remaining, height, width, slack - 1)) {
                return true
            }
            blocked[targetR][targetC] = false
        }

        return false
    }

    override fun part1(): String = regions.count { solve(it) }.toString()

    override fun part2(): String = ""
}
