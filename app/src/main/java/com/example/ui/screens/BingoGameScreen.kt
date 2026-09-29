package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ActiveGameState
import com.example.ui.components.BallCallerCage
import com.example.ui.components.BingoCardView
import com.example.ui.components.JackpotPotCard
import com.example.ui.theme.AmberJackpot
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent

@Composable
fun BingoGameScreen(
    gameState: ActiveGameState,
    onStartGame: () -> Unit,
    onPauseGame: () -> Unit,
    onDrawNextBall: () -> Unit,
    onToggleAutoDaub: () -> Unit,
    onCellClick: (com.example.data.model.BingoCell) -> Unit,
    onShoutBingo: () -> Unit,
    onDismissDialog: () -> Unit,
    onNavigateToLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val round = gameState.round
    val card = gameState.userCard

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        if (round == null || card == null) {
            // Empty State: No active round joined
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(EmeraldDark.copy(alpha = 0.4f))
                        .border(2.dp, EmeraldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = null,
                        tint = EmeraldLight,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "لم تشترك في أي جولة حالياً",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "اختر جولة من قائمة الجولات واشترك بالجواهر لتنافس اللاعبين وتفوز بمجموع الاشتراكات!",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onNavigateToLobby,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(48.dp)
                        .testTag("go_to_lobby_btn")
                ) {
                    Icon(Icons.Default.Casino, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("استعراض الجولات المتاحة", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Active Game Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Round Header Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = round.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "رسوم الدخول: ${round.entryFee} جوهرة",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = EmeraldLight
                            )
                        )
                    }

                    // Status Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (gameState.isRunning) EmeraldDark else Color(0xFF1E293B)
                            )
                            .border(
                                1.dp,
                                if (gameState.isRunning) EmeraldPrimary else DarkBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (gameState.isRunning) "مباشر الآن 🟢" else "في الانتظار ⏳",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (gameState.isRunning) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }

                // Financial Jackpot Card (Gross Pot, Commission, Net Prize)
                JackpotPotCard(
                    totalPlayers = round.totalPlayers,
                    entryFee = round.entryFee,
                    commissionPercent = round.commissionPercent
                )

                // Ball Caller Cage
                BallCallerCage(
                    currentBall = gameState.currentBall,
                    calledBalls = gameState.calledBalls
                )

                // Game Controls & Auto-Daub Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start / Pause Controls
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (gameState.isRunning) onPauseGame() else onStartGame()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (gameState.isRunning) Color(0xFFDC2626) else EmeraldPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("start_pause_game_btn")
                        ) {
                            Icon(
                                imageVector = if (gameState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (gameState.isRunning) "إيقاف مؤقت" else "بدء السحب",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        OutlinedButton(
                            onClick = onDrawNextBall,
                            enabled = !gameState.isRunning && !gameState.gameFinished,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("manual_draw_ball_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("سحب كرة", fontSize = 12.sp)
                        }
                    }

                    // Auto-Daub Toggle Switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "تعليم تلقائي",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCBD5E1)
                        )
                        Switch(
                            checked = gameState.autoDaub,
                            onCheckedChange = { onToggleAutoDaub() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = EmeraldDark
                            ),
                            modifier = Modifier.testTag("auto_daub_switch")
                        )
                    }
                }

                // Interactive 5x5 Bingo Card
                BingoCardView(
                    card = card,
                    calledNumbers = gameState.calledBalls.map { it.number }.toSet(),
                    winningCells = gameState.winResult?.winningCells ?: emptySet(),
                    onCellClick = onCellClick
                )

                // Win pattern notification if ready
                AnimatedVisibility(
                    visible = gameState.winResult?.hasWon == true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AmberJackpot.copy(alpha = 0.2f))
                            .border(1.5.dp, GoldAccent, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GoldAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "لديك بينغو مكتمل! (${gameState.winResult?.patternName}) - اضغط زر بينغو فوراً!",
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Giant SHOUT BINGO Button
                val infiniteTransition = rememberInfiniteTransition(label = "bingo_pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = if (gameState.canShoutBingo) 1.05f else 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "btn_scale"
                )

                Button(
                    onClick = onShoutBingo,
                    enabled = gameState.canShoutBingo && !gameState.gameFinished,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberJackpot,
                        disabledContainerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .scale(pulseScale)
                        .border(
                            2.dp,
                            if (gameState.canShoutBingo) GoldAccent else DarkBorder,
                            RoundedCornerShape(16.dp)
                        )
                        .testTag("shout_bingo_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = if (gameState.canShoutBingo) Color.Black else Color(0xFF64748B),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "بـيـنـغـو! (BINGO) 🏆",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = if (gameState.canShoutBingo) Color.Black else Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Round Completion Celebration Dialog
        if (gameState.gameFinished) {
            AlertDialog(
                onDismissRequest = onDismissDialog,
                confirmButton = {
                    Button(
                        onClick = onDismissDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("متابعة", fontWeight = FontWeight.Bold)
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (gameState.isUserWinner) Icons.Default.EmojiEvents else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (gameState.isUserWinner) GoldAccent else EmeraldPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (gameState.isUserWinner) "🎉 مبروووك الفوز!" else "انتهت الجولة",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (gameState.isUserWinner) {
                            Text(
                                text = "أنت بطل الجولة! تم تتويجك فائزاً وحصلت على صافي الوعاء بعد خصم عمولة المنصة:",
                                color = Color(0xFFE2E8F0),
                                fontSize = 14.sp
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(EmeraldDark.copy(alpha = 0.5f))
                                    .border(1.5.dp, EmeraldPrimary, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Diamond, contentDescription = null, tint = EmeraldLight)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "+ ${gameState.netPrizeWon} جوهرة أضيفت لمحفظتك!",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = EmeraldLight
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "الفائز في هذه الجولة هو: ${gameState.winnerName}",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "حصل الفائز على صافي مجموع الاشتراكات (${round?.netPrize} جوهرة) بعد خصم عمولة المنصة (${round?.commissionPercent}%).",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                        }
                    }
                },
                containerColor = Color(0xFF162032),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
