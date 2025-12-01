# Advent of Code 2025

Kotlin solutions using [JetBrains Amper](https://github.com/JetBrains/amper) build tool.

## Usage

```bash
./amper build          # Build
./amper run -- 1       # Run day 1
./amper run            # Run today's day
```

## Adding a New Day

1. Create `src/DayXX.kt`:

   ```kotlin
   class Day02(input: List<String>) : Day(input) {
       private val parsed by lazy { /* parse input */ }

       override fun part1(): String = TODO()
       override fun part2(): String = TODO()
   }
   ```

2. Add input file `src/Day02.txt`

Days are auto-discovered via reflection.
