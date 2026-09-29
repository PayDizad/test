package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BingoCard
import com.example.data.model.BingoCell
import com.example.data.model.BingoColumn
import com.example.ui.theme.AmberJackpot
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent

@Composable
fun BingoCardView(
    card: BingoCard,
    calledNumbers: Set<Int>,
    winningCells: Set<BingoCell> = emptySet(),
    onCellClick: (BingoCell) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, DarkBorder, RoundedCornerShape(16.dp))
            .testTag("bingo_card_view"),
        color = DarkSurfaceCard,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header Row: B - I - N - G - O
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BingoColumn.entries.forEach { column ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.6f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(column.colorHex),
                                        Color(column.colorHex).copy(alpha = 0.7f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = column.letter.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            // 5x5 Grid Cells
            card.cells.forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    row.forEachIndexed { colIndex, cell ->
                        val isWinning = winningCells.any { it.row == cell.row && it.col == cell.col }
                        val isCalled = cell.number in calledNumbers

                        BingoCellItem(
                            cell = cell,
                            isCalled = isCalled,
                            isWinning = isWinning,
                            onClick = { onCellClick(cell) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BingoCellItem(
    cell: BingoCell,
    isCalled: Boolean,
    isWinning: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "win_glow")
    val winScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "win_scale"
    )

    // Animated background color based on marked / uncalled status
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isWinning -> AmberJackpot
            cell.isFree -> Color(0xFF1E293B)
            cell.isMarked -> EmeraldDark
            isCalled -> Color(0xFF1E3A5F) // Ready to daub highlight
            else -> Color(0xFF131C2E)
        },
        label = "cell_bg"
    )

    val borderModifier = when {
        isWinning -> Modifier.border(2.5.dp, GoldAccent, RoundedCornerShape(8.dp))
        isCalled && !cell.isMarked -> Modifier.border(1.5.dp, EmeraldLight.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
        cell.isMarked -> Modifier.border(1.dp, EmeraldPrimary, RoundedCornerShape(8.dp))
        else -> Modifier.border(1.dp, DarkBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
    }

    Box(
        modifier = modifier
            .then(if (isWinning) Modifier.scale(winScale) else Modifier)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(borderModifier)
            .clickable(enabled = !cell.isFree) { onClick() }
            .testTag("cell_${cell.row}_${cell.col}"),
        contentAlignment = Alignment.Center
    ) {
        if (cell.isFree) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "مساحة مجانية",
                    tint = GoldAccent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "FREE",
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    color = GoldAccent
                )
            }
        } else {
            // Marked stamp indicator background
            if (cell.isMarked) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    EmeraldLight.copy(alpha = 0.4f),
                                    EmeraldPrimary.copy(alpha = 0.15f)
                                )
                            )
                        )
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = cell.number.toString(),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (cell.isMarked || isCalled) FontWeight.Black else FontWeight.Bold,
                        fontSize = 17.sp,
                        color = when {
                            isWinning -> Color.Black
                            cell.isMarked -> Color.White
                            isCalled -> EmeraldLight
                            else -> Color(0xFFCBD5E1)
                        }
                    )
                )

                if (cell.isMarked) {
                    Icon(
                        imageVector = Icons.Default.Diamond,
                        contentDescription = "معلمة",
                        tint = if (isWinning) Color.Black else EmeraldLight,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}
