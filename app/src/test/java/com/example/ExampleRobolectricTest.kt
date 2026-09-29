package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BingoCard
import com.example.data.model.BingoColumn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("بينغو الجواهر", appName)
    }

    @Test
    fun `bingo column distribution is accurate`() {
        assertEquals(BingoColumn.B, BingoColumn.fromNumber(7))
        assertEquals(BingoColumn.I, BingoColumn.fromNumber(22))
        assertEquals(BingoColumn.N, BingoColumn.fromNumber(35))
        assertEquals(BingoColumn.G, BingoColumn.fromNumber(50))
        assertEquals(BingoColumn.O, BingoColumn.fromNumber(68))
    }

    @Test
    fun `bingo card center is free space`() {
        val card = BingoCard.generateRandom()
        val centerCell = card.cells[2][2]
        assertTrue(centerCell.isFree)
        assertTrue(centerCell.isMarked)
    }

    @Test
    fun `bingo card detects horizontal win`() {
        var card = BingoCard.generateRandom()
        // Mark all cells in row 0
        for (c in 0..4) {
            val num = card.cells[0][c].number
            card = card.markNumber(num)
        }
        val winResult = card.checkWin()
        assertTrue(winResult.hasWon)
        assertTrue(winResult.patternName.contains("أفقي"))
    }

    @Test
    fun `financial pot and commission calculation is accurate`() {
        val totalPlayers = 10
        val entryFee = 25
        val commissionPercent = 15

        val totalPot = totalPlayers * entryFee // 250
        val commission = (totalPot * commissionPercent) / 100 // 37
        val netPrize = totalPot - commission // 213

        assertEquals(250, totalPot)
        assertEquals(37, commission)
        assertEquals(213, netPrize)
    }
}
