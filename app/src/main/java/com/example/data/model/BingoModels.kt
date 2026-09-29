package com.example.data.model

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

enum class BingoColumn(val letter: Char, val minNum: Int, val maxNum: Int, val colorHex: Long) {
    B('B', 1, 15, 0xFFEF4444),
    I('I', 16, 30, 0xFF3B82F6),
    N('N', 31, 45, 0xFF10B981),
    G('G', 46, 60, 0xFFF59E0B),
    O('O', 61, 75, 0xFF8B5CF6);

    companion object {
        fun fromNumber(number: Int): BingoColumn {
            return when (number) {
                in 1..15 -> B
                in 16..30 -> I
                in 31..45 -> N
                in 46..60 -> G
                else -> O
            }
        }
    }
}

data class BingoBall(
    val number: Int,
    val column: BingoColumn = BingoColumn.fromNumber(number)
) {
    val displayLabel: String = "${column.letter}-$number"
    val color: Color = Color(column.colorHex)
}

data class BingoCell(
    val row: Int,
    val col: Int,
    val number: Int,
    val isFree: Boolean = false,
    val isMarked: Boolean = false,
    val isWinningLine: Boolean = false
)

data class BingoCard(
    val id: String = Random.nextInt(1000, 9999).toString(),
    val cells: List<List<BingoCell>> // 5 rows x 5 columns
) {
    companion object {
        fun generateRandom(): BingoCard {
            // Generate standard 75-ball bingo card:
            // Col 0: 5 random unique from 1..15
            // Col 1: 5 random unique from 16..30
            // Col 2: 4 random unique from 31..45 + FREE center (row 2)
            // Col 3: 5 random unique from 46..60
            // Col 4: 5 random unique from 61..75
            val colB = (1..15).shuffled().take(5)
            val colI = (16..30).shuffled().take(5)
            val colN = (31..45).shuffled().take(4)
            val colG = (46..60).shuffled().take(5)
            val colO = (61..75).shuffled().take(5)

            val grid = List(5) { r ->
                List(5) { c ->
                    if (r == 2 && c == 2) {
                        BingoCell(row = r, col = c, number = 0, isFree = true, isMarked = true)
                    } else {
                        val num = when (c) {
                            0 -> colB[r]
                            1 -> colI[r]
                            2 -> if (r < 2) colN[r] else colN[r - 1]
                            3 -> colG[r]
                            else -> colO[r]
                        }
                        BingoCell(row = r, col = c, number = num, isFree = false, isMarked = false)
                    }
                }
            }
            return BingoCard(cells = grid)
        }

        fun fromNumbers(numbers: List<Int>): BingoCard {
            if (numbers.size != 25) return generateRandom()
            val grid = List(5) { r ->
                List(5) { c ->
                    val idx = r * 5 + c
                    val num = numbers[idx]
                    val isFree = (r == 2 && c == 2) || num == 0
                    BingoCell(
                        row = r,
                        col = c,
                        number = num,
                        isFree = isFree,
                        isMarked = isFree
                    )
                }
            }
            return BingoCard(cells = grid)
        }
    }

    fun toNumbersList(): List<Int> {
        return cells.flatten().map { it.number }
    }

    fun markNumber(number: Int): BingoCard {
        val updated = cells.map { row ->
            row.map { cell ->
                if (cell.number == number) cell.copy(isMarked = true) else cell
            }
        }
        return copy(cells = updated)
    }

    /**
     * Checks if this card satisfies a winning bingo condition:
     * - Any horizontal row (5 in row)
     * - Any vertical column (5 in column)
     * - Diagonal 1 (top-left to bottom-right)
     * - Diagonal 2 (top-right to bottom-left)
     * - 4 Corners
     */
    fun checkWin(): WinResult {
        // 1. Rows
        for (r in 0..4) {
            if (cells[r].all { it.isMarked }) {
                return WinResult(hasWon = true, patternName = "خط أفقي (صف ${r + 1})", winningCells = cells[r].toSet())
            }
        }

        // 2. Columns
        for (c in 0..4) {
            val colCells = List(5) { r -> cells[r][c] }
            if (colCells.all { it.isMarked }) {
                val letter = BingoColumn.entries[c].letter
                return WinResult(hasWon = true, patternName = "خط عمودي (عمود $letter)", winningCells = colCells.toSet())
            }
        }

        // 3. Diagonal 1 (0,0 to 4,4)
        val diag1 = List(5) { i -> cells[i][i] }
        if (diag1.all { it.isMarked }) {
            return WinResult(hasWon = true, patternName = "خط قطري مائل", winningCells = diag1.toSet())
        }

        // 4. Diagonal 2 (0,4 to 4,0)
        val diag2 = List(5) { i -> cells[i][4 - i] }
        if (diag2.all { it.isMarked }) {
            return WinResult(hasWon = true, patternName = "خط قطري عكسي", winningCells = diag2.toSet())
        }

        // 5. Four corners
        val corners = listOf(cells[0][0], cells[0][4], cells[4][0], cells[4][4])
        if (corners.all { it.isMarked }) {
            return WinResult(hasWon = true, patternName = "الأركان الأربعة", winningCells = corners.toSet())
        }

        return WinResult(hasWon = false, patternName = "", winningCells = emptySet())
    }
}

data class WinResult(
    val hasWon: Boolean,
    val patternName: String,
    val winningCells: Set<BingoCell>
)
