package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettingsEntity
import com.example.data.model.BingoRoundEntity
import com.example.data.model.UserProfileEntity
import com.example.ui.theme.AmberJackpot
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent

@Composable
fun AdminDashboardScreen(
    settings: AppSettingsEntity,
    user: UserProfileEntity,
    allRounds: List<BingoRoundEntity>,
    onUpdateStarterGems: (Int) -> Unit,
    onUpdateCommissionPercent: (Int) -> Unit,
    onAdjustUserGems: (Int, String) -> Unit,
    onCreateRound: (String, Int, Int, Int) -> Unit,
    onAddSimulatedPlayer: (Long) -> Unit,
    onDeleteRound: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var starterGemsInput by remember(settings.starterGems) {
        mutableStateOf(settings.starterGems.toString())
    }
    var commissionInput by remember(settings.defaultCommissionPercent) {
        mutableStateOf(settings.defaultCommissionPercent.toString())
    }

    // New Round inputs
    var newRoundTitle by remember { mutableStateOf("") }
    var newRoundEntryFee by remember { mutableStateOf("25") }
    var newRoundCommission by remember { mutableStateOf(settings.defaultCommissionPercent.toString()) }
    var newRoundBots by remember { mutableStateOf("6") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("admin_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Banner: Platform Control
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF311042),
                                Color(0xFF1E1B4B),
                                Color(0xFF0F172A)
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.horizontalGradient(listOf(Color(0xFFA855F7), EmeraldPrimary)),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFA855F7).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color(0xFFC084FC),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "لوحة التحكم والإدارة",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "إدارة اقتصاد الجواهر والعمولات والجولات",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF94A3B8)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = DarkBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Treasury & Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Platform Treasury
                        Column {
                            Text(
                                text = "خزينة عمولات المنصة",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format("%,d", settings.platformTreasuryGems),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = Color(0xFFC084FC)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Diamond,
                                    contentDescription = null,
                                    tint = Color(0xFFC084FC),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Total Rounds Run
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "إجمالي الجولات",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "${allRounds.size} جولة",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Section 1: Gems Configuration (تحديد الجواهر الافتراضية)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, DarkBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = EmeraldLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إعدادات الجواهر والعمولة (الخاصة بي)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }

                    Text(
                        text = "هنا تحدد عدد الجواهر الممنوحة تلقائياً للمستخدمين الجدد عند تحميل التطبيق، ونسبة العمولة المقتطعة للمنصة من كل وعاء.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 17.sp
                    )

                    // Starter Gems Setting
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = starterGemsInput,
                            onValueChange = { starterGemsInput = it },
                            label = { Text("جواهر البداية للمشتركين الجدد") },
                            leadingIcon = { Icon(Icons.Default.Diamond, contentDescription = null, tint = EmeraldLight) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("starter_gems_input")
                        )

                        Button(
                            onClick = {
                                val amount = starterGemsInput.toIntOrNull() ?: 100
                                onUpdateStarterGems(amount)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("save_starter_gems_btn")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "حفظ")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حفظ")
                        }
                    }

                    // Default Commission % Setting
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = commissionInput,
                            onValueChange = { commissionInput = it },
                            label = { Text("نسبة عمولة المنصة الافتراضية (%)") },
                            leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null, tint = Color(0xFFC084FC)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFC084FC),
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("commission_percent_input")
                        )

                        Button(
                            onClick = {
                                val percent = commissionInput.toIntOrNull() ?: 15
                                onUpdateCommissionPercent(percent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("save_commission_btn")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "حفظ")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حفظ")
                        }
                    }

                    // Quick User Balance Controls
                    HorizontalDivider(color = DarkBorder)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "رصيد اللاعب الحالي (${user.name})",
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1)
                            )
                            Text(
                                text = "${user.gems} جوهرة",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = EmeraldLight
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { onAdjustUserGems(100, "هدية من الإدارة") },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+100 💎")
                            }
                            OutlinedButton(
                                onClick = { onAdjustUserGems(-50, "خصم تجريبي") },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("-50 💎")
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Create New Bingo Round (إنشاء جولة جديدة)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, DarkBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = AmberJackpot)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إنشاء جولة بينغو جديدة",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }

                    OutlinedTextField(
                        value = newRoundTitle,
                        onValueChange = { newRoundTitle = it },
                        label = { Text("اسم الجولة (مثال: كأس الزمرد #105)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_round_title_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newRoundEntryFee,
                            onValueChange = { newRoundEntryFee = it },
                            label = { Text("رسوم الاشتراك 💎") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("new_round_fee_input")
                        )

                        OutlinedTextField(
                            value = newRoundCommission,
                            onValueChange = { newRoundCommission = it },
                            label = { Text("عمولة المنصة %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("new_round_commission_input")
                        )

                        OutlinedTextField(
                            value = newRoundBots,
                            onValueChange = { newRoundBots = it },
                            label = { Text("عدد المتسابقين") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("new_round_bots_input")
                        )
                    }

                    Button(
                        onClick = {
                            val fee = newRoundEntryFee.toIntOrNull() ?: 25
                            val commission = newRoundCommission.toIntOrNull() ?: settings.defaultCommissionPercent
                            val bots = newRoundBots.toIntOrNull() ?: 6
                            onCreateRound(newRoundTitle, fee, commission, bots)
                            newRoundTitle = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberJackpot),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("create_round_submit_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "نشر وإنشاء الجولة في المنصة",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Section 3: Manage Existing Rounds (إدارة الجولات الحالية)
        item {
            Text(
                text = "إدارة الجولات (${allRounds.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        items(allRounds, key = { it.id }) { round ->
            AdminRoundItem(
                round = round,
                onAddPlayer = { onAddSimulatedPlayer(round.id) },
                onDelete = { onDeleteRound(round.id) }
            )
        }
    }
}

@Composable
fun AdminRoundItem(
    round: BingoRoundEntity,
    onAddPlayer: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${round.title} (#${round.id})",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "الاشتراك: ${round.entryFee} جوهرة • العمولة: ${round.commissionPercent}% • الحالة: ${round.status}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف الجولة",
                        tint = Color(0xFFEF4444)
                    )
                }
            }

            // Calculation status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${round.totalPlayers} لاعبين",
                    fontSize = 11.sp,
                    color = Color(0xFF38BDF8)
                )
                Text(
                    text = "الوعاء: ${round.totalPot} 💎",
                    fontSize = 11.sp,
                    color = Color.White
                )
                Text(
                    text = "عمولة: ${round.platformCommission} 💎",
                    fontSize = 11.sp,
                    color = Color(0xFFC084FC)
                )
                Text(
                    text = "الصافي: ${round.netPrize} 💎",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = GoldAccent
                )
            }

            if (round.status != "COMPLETED") {
                Button(
                    onClick = onAddPlayer,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = null,
                        tint = EmeraldLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "إضافة متسابق للجولة (+وعاء الجائزة)",
                        fontSize = 12.sp,
                        color = Color(0xFFF1F5F9)
                    )
                }
            } else {
                Text(
                    text = "مكتملة • الفائز: ${round.winnerName ?: "غير محدد"}",
                    fontSize = 11.sp,
                    color = GoldAccent
                )
            }
        }
    }
}
