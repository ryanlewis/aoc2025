import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines
import kotlin.reflect.full.primaryConstructor
import kotlin.time.Clock
import kotlin.time.measureTimedValue

@kotlin.time.ExperimentalTime
fun main(args: Array<String>) {
    val dayNumber =
        args.firstOrNull()?.toIntOrNull()
            ?: Clock.System.todayIn(TimeZone.currentSystemDefault()).day

    val input = loadInput(dayNumber)
    val day =
        getDay(dayNumber, input)
            ?: error("Day $dayNumber not implemented")

    println("=== Day $dayNumber ===")

    val (result1, duration1) = measureTimedValue { day.part1() }
    println("Part 1: $result1 (${duration1.inWholeMilliseconds}ms)")

    val (result2, duration2) = measureTimedValue { day.part2() }
    println("Part 2: $result2 (${duration2.inWholeMilliseconds}ms)")
}

private fun loadInput(dayNumber: Int): List<String> {
    val dayPadded = dayNumber.toString().padStart(2, '0')
    val inputFile =
        listOf(
            Path("src/day$dayPadded.txt"),
            Path("src/Day$dayPadded.txt"),
        ).firstOrNull { it.exists() }
            ?: error("Input file not found: src/day$dayPadded.txt or src/Day$dayPadded.txt")
    return inputFile.readLines()
}

private fun getDay(
    dayNumber: Int,
    input: List<String>,
): Day? {
    val className = "Day${dayNumber.toString().padStart(2, '0')}"
    return try {
        val clazz = Class.forName(className).kotlin
        val constructor = clazz.primaryConstructor ?: return null
        constructor.call(input) as? Day
    } catch (_: ClassNotFoundException) {
        null
    }
}
