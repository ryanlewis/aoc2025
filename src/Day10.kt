@Suppress("unused")
class Day10(
    input: List<String>,
) : Day(input) {
    private val machines = input.parse()

    override fun part1(): String = machines.sumOf { it.minPresses() }.toString()

    private fun Machine.minPresses(): Int {
        val numLights = lights.size
        val numButtons = buttons.size
        var minPresses = Int.MAX_VALUE

        // Try all 2^n subsets of buttons
        for (mask in 0 until (1 shl numButtons)) {
            val result = BooleanArray(numLights)

            // Apply each selected button's toggles
            for (buttonIdx in 0 until numButtons) {
                if ((mask and (1 shl buttonIdx)) != 0) {
                    for (lightIdx in buttons[buttonIdx].wiring) {
                        result[lightIdx] = !result[lightIdx]
                    }
                }
            }

            // Check if result matches target
            if (result.toList() == lights) {
                minPresses = minOf(minPresses, mask.countOneBits())
            }
        }

        if (minPresses == Int.MAX_VALUE) error("No solution found for machine")
        return minPresses
    }

    override fun part2(): String = machines.sumOf { it.minJoltagePresses() }.toString()

    private fun Machine.minJoltagePresses(): Int {
        val numCounters = joltages.size
        val numButtons = buttons.size

        // Build augmented matrix [A | b] where A[i][j] = 1 if button j affects counter i
        // Using rationals as Pair<Int, Int> (numerator, denominator) to avoid floating point
        // Actually, let's use Double for simplicity and round at the end
        val matrix =
            Array(numCounters) { row ->
                DoubleArray(numButtons + 1) { col ->
                    when {
                        col == numButtons -> joltages[row].toDouble() // augmented column (target)
                        row in buttons[col].wiring -> 1.0
                        else -> 0.0
                    }
                }
            }

        // Gaussian elimination with partial pivoting
        val pivotCols = mutableListOf<Int>() // which columns have pivots
        var pivotRow = 0

        for (col in 0 until numButtons) {
            if (pivotRow >= numCounters) break

            // Find best pivot in this column
            var bestRow = pivotRow
            for (row in pivotRow + 1 until numCounters) {
                if (kotlin.math.abs(matrix[row][col]) > kotlin.math.abs(matrix[bestRow][col])) {
                    bestRow = row
                }
            }

            if (kotlin.math.abs(matrix[bestRow][col]) < 1e-9) continue // no pivot in this column

            // Swap rows
            val temp = matrix[pivotRow]
            matrix[pivotRow] = matrix[bestRow]
            matrix[bestRow] = temp

            // Scale pivot row
            val scale = matrix[pivotRow][col]
            for (j in 0..numButtons) {
                matrix[pivotRow][j] /= scale
            }

            // Eliminate other rows
            for (row in 0 until numCounters) {
                if (row != pivotRow && kotlin.math.abs(matrix[row][col]) > 1e-9) {
                    val factor = matrix[row][col]
                    for (j in 0..numButtons) {
                        matrix[row][j] -= factor * matrix[pivotRow][j]
                    }
                }
            }

            pivotCols.add(col)
            pivotRow++
        }

        // Identify free variables (columns without pivots)
        val freeVars = (0 until numButtons).filter { it !in pivotCols }

        // Now search over free variables
        // For each assignment of free variables, compute pivot variables and check validity
        val maxVal = joltages.max()

        fun searchFreeVars(
            freeIdx: Int,
            freeValues: IntArray,
        ): Int {
            if (freeIdx == freeVars.size) {
                // Compute pivot variable values
                val presses = IntArray(numButtons)

                // Set free variables
                for (i in freeVars.indices) {
                    presses[freeVars[i]] = freeValues[i]
                }

                // Compute pivot variables from reduced matrix
                for (i in pivotCols.indices) {
                    val pivotCol = pivotCols[i]
                    // Row i has pivot in column pivotCol
                    // presses[pivotCol] = matrix[i][numButtons] - sum of (matrix[i][j] * presses[j]) for free j
                    var value = matrix[i][numButtons]
                    for (freeCol in freeVars) {
                        value -= matrix[i][freeCol] * presses[freeCol]
                    }
                    val intValue = kotlin.math.round(value).toInt()
                    if (intValue < 0 || kotlin.math.abs(value - intValue) > 1e-6) {
                        return Int.MAX_VALUE // non-integer or negative solution
                    }
                    presses[pivotCol] = intValue
                }

                // Verify solution (sanity check)
                for (counter in 0 until numCounters) {
                    var sum = 0
                    for (btn in 0 until numButtons) {
                        if (counter in buttons[btn].wiring) {
                            sum += presses[btn]
                        }
                    }
                    if (sum != joltages[counter]) return Int.MAX_VALUE
                }

                return presses.sum()
            }

            var best = Int.MAX_VALUE
            for (v in 0..maxVal) {
                freeValues[freeIdx] = v
                best = minOf(best, searchFreeVars(freeIdx + 1, freeValues))
            }
            return best
        }

        val result = searchFreeVars(0, IntArray(freeVars.size))
        if (result == Int.MAX_VALUE) error("No solution found")
        return result
    }

    private fun List<String>.parse(): List<Machine> =
        map { line ->
            val parts = line.split(" ")

            val lightConfig = parts[0].drop(1).dropLast(1).map { it == '#' }

            val buttons: List<Button> =
                parts.drop(1).dropLast(1).map {
                    val wiring =
                        it
                            .drop(1)
                            .dropLast(1)
                            .split(",")
                            .map { it.toInt() }
                    Button(wiring)
                }

            val joltages =
                parts
                    .last()
                    .drop(1)
                    .dropLast(1)
                    .split(",")
                    .map { it.toInt() }
            Machine(lightConfig, buttons, joltages)
        }

    data class Machine(
        val lights: List<Boolean>,
        val buttons: List<Button>,
        val joltages: List<Int>,
    )

    data class Button(
        val wiring: List<Int>,
    )
}
