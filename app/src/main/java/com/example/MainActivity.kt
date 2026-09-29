package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BingoViewModel
import com.example.ui.components.GemBadge
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.BingoGameScreen
import com.example.ui.screens.GemStoreScreen
import com.example.ui.screens.RoundsLobbyScreen
import com.example.ui.screens.WinnersTransparencyScreen
import com.example.ui.theme.AmberJackpot
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MyApplicationTheme

enum class MainNavScreen(val title: String) {
    GAME("اللعبة"),
    ROUNDS("الجولات"),
    TRANSPARENCY("الفائزون"),
    STORE("المتجر"),
    ADMIN("لوحة التحكم")
}

class MainActivity : ComponentActivity() {

    private val viewModel: BingoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    BingoAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoAppContent(viewModel: BingoViewModel) {
    var currentScreen by remember { mutableStateOf(MainNavScreen.ROUNDS) }
    val snackbarHostState = remember { SnackbarHostState() }

    val user by viewModel.userState.collectAsStateWithLifecycle()
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val activeRounds by viewModel.activeRoundsState.collectAsStateWithLifecycle()
    val completedRounds by viewModel.completedRoundsState.collectAsStateWithLifecycle()
    val allRounds by viewModel.allRoundsState.collectAsStateWithLifecycle()
    val packages by viewModel.gemPackagesState.collectAsStateWithLifecycle()
    val transactions by viewModel.transactionsState.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()

    // Handle back button when not on main screen
    BackHandler(enabled = currentScreen != MainNavScreen.ROUNDS) {
        currentScreen = MainNavScreen.ROUNDS
    }

    // Snackbar notifications
    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        listOf(EmeraldLight, EmeraldPrimary, EmeraldDark)
                                    )
                                )
                                .border(1.dp, GoldAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "بينغو الجواهر",
                            fontWeight = FontWeight.Black,
                            fontSize = 19.sp,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    // Gems Badge
                    GemBadge(
                        gems = user.gems,
                        onAddClick = { currentScreen = MainNavScreen.STORE },
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    // Admin Shortcut Button
                    IconButton(
                        onClick = {
                            currentScreen = if (currentScreen == MainNavScreen.ADMIN) MainNavScreen.ROUNDS else MainNavScreen.ADMIN
                        },
                        modifier = Modifier.testTag("admin_toggle_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "لوحة التحكم",
                            tint = if (currentScreen == MainNavScreen.ADMIN) Color(0xFFC084FC) else Color(0xFF94A3B8)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("main_bottom_nav"),
                containerColor = DarkSurface,
                contentColor = Color.White
            ) {
                // Game Tab
                NavigationBarItem(
                    selected = currentScreen == MainNavScreen.GAME,
                    onClick = { currentScreen = MainNavScreen.GAME },
                    icon = { Icon(Icons.Default.Casino, contentDescription = "اللعبة") },
                    label = { Text("اللعبة", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldLight,
                        indicatorColor = EmeraldDark.copy(alpha = 0.5f),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_game")
                )

                // Rounds / Lobby Tab
                NavigationBarItem(
                    selected = currentScreen == MainNavScreen.ROUNDS,
                    onClick = { currentScreen = MainNavScreen.ROUNDS },
                    icon = { Icon(Icons.Default.FormatListNumbered, contentDescription = "الجولات") },
                    label = { Text("الجولات", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldLight,
                        indicatorColor = EmeraldDark.copy(alpha = 0.5f),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_rounds")
                )

                // Transparency / Winners Tab
                NavigationBarItem(
                    selected = currentScreen == MainNavScreen.TRANSPARENCY,
                    onClick = { currentScreen = MainNavScreen.TRANSPARENCY },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "الفائزون") },
                    label = { Text("الفائزون", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberJackpot,
                        selectedTextColor = GoldAccent,
                        indicatorColor = AmberJackpot.copy(alpha = 0.2f),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_transparency")
                )

                // Store Tab
                NavigationBarItem(
                    selected = currentScreen == MainNavScreen.STORE,
                    onClick = { currentScreen = MainNavScreen.STORE },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "المتجر") },
                    label = { Text("المتجر", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldLight,
                        indicatorColor = EmeraldDark.copy(alpha = 0.5f),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_store")
                )

                // Admin Tab
                NavigationBarItem(
                    selected = currentScreen == MainNavScreen.ADMIN,
                    onClick = { currentScreen = MainNavScreen.ADMIN },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "لوحة التحكم") },
                    label = { Text("الإدارة", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFC084FC),
                        selectedTextColor = Color(0xFFC084FC),
                        indicatorColor = Color(0xFF581C87).copy(alpha = 0.5f),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_admin")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                MainNavScreen.GAME -> {
                    BingoGameScreen(
                        gameState = gameState,
                        onStartGame = { viewModel.startActiveGame() },
                        onPauseGame = { viewModel.pauseActiveGame() },
                        onDrawNextBall = { viewModel.drawNextBallManual() },
                        onToggleAutoDaub = { viewModel.toggleAutoDaub() },
                        onCellClick = { cell -> viewModel.onCellClicked(cell) },
                        onShoutBingo = { viewModel.shoutBingo() },
                        onDismissDialog = { viewModel.dismissGameFinishedDialog() },
                        onNavigateToLobby = { currentScreen = MainNavScreen.ROUNDS }
                    )
                }

                MainNavScreen.ROUNDS -> {
                    RoundsLobbyScreen(
                        rounds = activeRounds,
                        userGems = user.gems,
                        onJoinRound = { round ->
                            viewModel.joinAndOpenRound(round) {
                                currentScreen = MainNavScreen.GAME
                            }
                        },
                        onOpenStore = { currentScreen = MainNavScreen.STORE },
                        onOpenAdmin = { currentScreen = MainNavScreen.ADMIN }
                    )
                }

                MainNavScreen.TRANSPARENCY -> {
                    WinnersTransparencyScreen(
                        completedRounds = completedRounds,
                        platformTreasuryGems = settings.platformTreasuryGems
                    )
                }

                MainNavScreen.STORE -> {
                    GemStoreScreen(
                        user = user,
                        packages = packages,
                        transactions = transactions,
                        onPurchasePackage = { pkg -> viewModel.purchaseGemPackage(pkg) }
                    )
                }

                MainNavScreen.ADMIN -> {
                    AdminDashboardScreen(
                        settings = settings,
                        user = user,
                        allRounds = allRounds,
                        onUpdateStarterGems = { gems -> viewModel.adminUpdateStarterGems(gems) },
                        onUpdateCommissionPercent = { pct -> viewModel.adminUpdateCommissionPercent(pct) },
                        onAdjustUserGems = { adj, reason -> viewModel.adminAdjustUserGems(adj, reason) },
                        onCreateRound = { title, fee, commission, bots ->
                            viewModel.adminCreateRound(title, fee, commission, bots)
                        },
                        onAddSimulatedPlayer = { roundId -> viewModel.adminAddSimulatedPlayer(roundId) },
                        onDeleteRound = { roundId -> viewModel.adminDeleteRound(roundId) }
                    )
                }
            }
        }
    }
}
