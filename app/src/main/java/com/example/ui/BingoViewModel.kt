package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BingoRepository
import com.example.data.JoinResult
import com.example.data.model.AppSettingsEntity
import com.example.data.model.BingoBall
import com.example.data.model.BingoCard
import com.example.data.model.BingoCell
import com.example.data.model.BingoRoundEntity
import com.example.data.model.GemPackageEntity
import com.example.data.model.GemTransactionEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.WinResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class ActiveGameState(
    val round: BingoRoundEntity? = null,
    val userCard: BingoCard? = null,
    val calledBalls: List<BingoBall> = emptyList(),
    val currentBall: BingoBall? = null,
    val isRunning: Boolean = false,
    val autoDaub: Boolean = false,
    val winResult: WinResult? = null,
    val isUserWinner: Boolean = false,
    val gameFinished: Boolean = false,
    val winnerName: String? = null,
    val netPrizeWon: Int = 0,
    val canShoutBingo: Boolean = false
)

class BingoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BingoRepository

    val settingsState: StateFlow<AppSettingsEntity>
    val userState: StateFlow<UserProfileEntity>
    val activeRoundsState: StateFlow<List<BingoRoundEntity>>
    val completedRoundsState: StateFlow<List<BingoRoundEntity>>
    val allRoundsState: StateFlow<List<BingoRoundEntity>>
    val gemPackagesState: StateFlow<List<GemPackageEntity>>
    val transactionsState: StateFlow<List<GemTransactionEntity>>

    private val _gameState = MutableStateFlow(ActiveGameState())
    val gameState: StateFlow<ActiveGameState> = _gameState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    private var ballDrawJob: Job? = null
    private val availableBallsPool = mutableListOf<Int>()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BingoRepository(db.bingoDao())

        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }

        settingsState = repository.settingsFlow
            .combine(MutableStateFlow(Unit)) { s, _ -> s ?: AppSettingsEntity() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettingsEntity())

        userState = repository.userFlow
            .combine(MutableStateFlow(Unit)) { u, _ -> u ?: UserProfileEntity() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfileEntity())

        activeRoundsState = repository.activeRoundsFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        completedRoundsState = repository.completedRoundsFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allRoundsState = repository.allRoundsFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        gemPackagesState = repository.packagesFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        transactionsState = repository.transactionsFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun joinAndOpenRound(round: BingoRoundEntity, onJoined: () -> Unit) {
        viewModelScope.launch {
            when (val result = repository.joinRound(round.id)) {
                is JoinResult.Success -> {
                    setupGameForRound(round, result.userCard)
                    _snackbarEvent.emit("تم الانضمام للجولة بنجاح! تم خصم ${round.entryFee} جوهرة")
                    onJoined()
                }
                is JoinResult.AlreadyJoined -> {
                    // Re-open with existing card or generate card
                    setupGameForRound(round, BingoCard.generateRandom())
                    onJoined()
                }
                is JoinResult.InsufficientGems -> {
                    _snackbarEvent.emit("رصيد الجواهر غير كافٍ! يلزمك ${result.needed} جوهرة ورصيدك ${result.current}")
                }
                is JoinResult.Error -> {
                    _snackbarEvent.emit(result.message)
                }
            }
        }
    }

    private fun setupGameForRound(round: BingoRoundEntity, userCard: BingoCard) {
        stopBallDrawing()
        availableBallsPool.clear()
        availableBallsPool.addAll((1..75).shuffled())

        _gameState.value = ActiveGameState(
            round = round,
            userCard = userCard,
            calledBalls = emptyList(),
            currentBall = null,
            isRunning = false,
            autoDaub = false,
            winResult = null,
            isUserWinner = false,
            gameFinished = false,
            winnerName = null,
            canShoutBingo = false
        )
    }

    fun startActiveGame() {
        val currentRound = _gameState.value.round ?: return
        if (_gameState.value.isRunning) return

        viewModelScope.launch {
            repository.startRound(currentRound.id)
            _gameState.value = _gameState.value.copy(isRunning = true)
            startBallDrawingLoop()
        }
    }

    fun pauseActiveGame() {
        stopBallDrawing()
        _gameState.value = _gameState.value.copy(isRunning = false)
    }

    private fun startBallDrawingLoop() {
        stopBallDrawing()
        ballDrawJob = viewModelScope.launch {
            while (_gameState.value.isRunning && availableBallsPool.isNotEmpty() && !_gameState.value.gameFinished) {
                delay(3200) // Call every 3.2 seconds
                drawNextBall()
            }
        }
    }

    fun drawNextBallManual() {
        if (availableBallsPool.isEmpty() || _gameState.value.gameFinished) return
        drawNextBall()
    }

    private fun drawNextBall() {
        if (availableBallsPool.isEmpty()) return
        val nextNumber = availableBallsPool.removeAt(0)
        val ball = BingoBall(number = nextNumber)

        val updatedBalls = _gameState.value.calledBalls + ball
        var currentCard = _gameState.value.userCard

        // Auto-daub if enabled
        if (_gameState.value.autoDaub && currentCard != null) {
            currentCard = currentCard.markNumber(nextNumber)
        }

        val winResult = currentCard?.checkWin()
        val canShout = winResult?.hasWon == true && !_gameState.value.gameFinished

        _gameState.value = _gameState.value.copy(
            currentBall = ball,
            calledBalls = updatedBalls,
            userCard = currentCard,
            winResult = winResult,
            canShoutBingo = canShout
        )

        // Bot win simulation after at least 16 balls called
        val round = _gameState.value.round
        if (round != null && updatedBalls.size >= 16 && !canShout && !_gameState.value.gameFinished) {
            // Realistic bot probability to shout Bingo
            val botChance = when (updatedBalls.size) {
                in 16..22 -> 15 // 15%
                in 23..30 -> 35 // 35%
                else -> 60 // 60%
            }
            if (Random.nextInt(100) < botChance) {
                // A bot competitor won!
                val botNames = listOf("فارس الرياض", "سارة م.", "عمر الكويتي", "نورة العلي", "خالد الحربي")
                val botWinner = botNames.random()
                triggerRoundCompletion(
                    winnerName = botWinner,
                    winnerAvatar = Random.nextInt(1, 6),
                    isUser = false,
                    winningBall = ball.number,
                    pattern = listOf("خط أفقي (صف 2)", "خط عمودي (عمود B)", "الأركان الأربعة").random()
                )
            }
        }
    }

    fun onCellClicked(cell: BingoCell) {
        if (_gameState.value.gameFinished) return
        val calledNumbers = _gameState.value.calledBalls.map { it.number }.toSet()

        // Can only mark if number was called or already marked
        if (cell.number in calledNumbers) {
            val updatedCard = _gameState.value.userCard?.let { card ->
                val newCells = card.cells.map { row ->
                    row.map { c ->
                        if (c.row == cell.row && c.col == cell.col) c.copy(isMarked = !c.isMarked) else c
                    }
                }
                card.copy(cells = newCells)
            }

            val winResult = updatedCard?.checkWin()
            val canShout = winResult?.hasWon == true

            _gameState.value = _gameState.value.copy(
                userCard = updatedCard,
                winResult = winResult,
                canShoutBingo = canShout
            )
        } else {
            viewModelScope.launch {
                _snackbarEvent.emit("الرقم ${cell.number} لم يُسحب بعد في هذه الجولة!")
            }
        }
    }

    fun toggleAutoDaub() {
        val newAutoDaub = !_gameState.value.autoDaub
        var updatedCard = _gameState.value.userCard

        if (newAutoDaub && updatedCard != null) {
            val calledSet = _gameState.value.calledBalls.map { it.number }.toSet()
            val markedCells = updatedCard.cells.map { row ->
                row.map { cell ->
                    if (cell.number in calledSet) cell.copy(isMarked = true) else cell
                }
            }
            updatedCard = updatedCard.copy(cells = markedCells)
        }

        val winResult = updatedCard?.checkWin()
        _gameState.value = _gameState.value.copy(
            autoDaub = newAutoDaub,
            userCard = updatedCard,
            winResult = winResult,
            canShoutBingo = winResult?.hasWon == true
        )
    }

    fun shoutBingo() {
        val state = _gameState.value
        if (!state.canShoutBingo || state.gameFinished) return

        val user = userState.value
        val round = state.round ?: return
        val currentBallNum = state.currentBall?.number ?: 7
        val pattern = state.winResult?.patternName ?: "بينغو مكتمل"

        triggerRoundCompletion(
            winnerName = user.name,
            winnerAvatar = user.avatarId,
            isUser = true,
            winningBall = currentBallNum,
            pattern = pattern
        )
    }

    private fun triggerRoundCompletion(
        winnerName: String,
        winnerAvatar: Int,
        isUser: Boolean,
        winningBall: Int,
        pattern: String
    ) {
        val round = _gameState.value.round ?: return
        stopBallDrawing()

        viewModelScope.launch {
            repository.completeRoundWithWinner(
                roundId = round.id,
                winnerName = winnerName,
                winnerAvatar = winnerAvatar,
                isUserWinner = isUser,
                winningBall = winningBall,
                pattern = pattern
            )

            _gameState.value = _gameState.value.copy(
                isRunning = false,
                gameFinished = true,
                isUserWinner = isUser,
                winnerName = winnerName,
                netPrizeWon = round.netPrize
            )

            if (isUser) {
                _snackbarEvent.emit("🎉 مبروووك! لقد فزت بالجائزة الكبرى: ${round.netPrize} جوهرة!")
            } else {
                _snackbarEvent.emit("انتهت الجولة! الفائز هو $winnerName 🏆")
            }
        }
    }

    fun dismissGameFinishedDialog() {
        _gameState.value = _gameState.value.copy(gameFinished = false)
    }

    private fun stopBallDrawing() {
        ballDrawJob?.cancel()
        ballDrawJob = null
    }

    // Purchase Gem Package
    fun purchaseGemPackage(pkg: GemPackageEntity) {
        viewModelScope.launch {
            val success = repository.purchaseGemPackage(pkg)
            if (success) {
                _snackbarEvent.emit("تم شراء ${pkg.gemAmount + pkg.bonusGems} جوهرة بنجاح!")
            }
        }
    }

    // Admin Operations
    fun adminCreateRound(title: String, entryFee: Int, commissionPercent: Int, botCount: Int) {
        viewModelScope.launch {
            val roundId = repository.createNewRound(
                title = title.ifBlank { "جولة التحدي #${Random.nextInt(200, 999)}" },
                entryFee = entryFee.coerceAtLeast(1),
                commissionPercent = commissionPercent.coerceIn(0, 50),
                initialBots = botCount.coerceAtLeast(1)
            )
            _snackbarEvent.emit("تم إنشاء الجولة بنجاح برقم #$roundId")
        }
    }

    fun adminUpdateStarterGems(starterGems: Int) {
        viewModelScope.launch {
            repository.updateStarterGems(starterGems.coerceAtLeast(0))
            _snackbarEvent.emit("تم تحديث جواهر البداية للمستخدمين الجدد إلى $starterGems جوهرة")
        }
    }

    fun adminUpdateCommissionPercent(percent: Int) {
        viewModelScope.launch {
            repository.updateCommissionPercent(percent.coerceIn(0, 50))
            _snackbarEvent.emit("تم تحديث نسبة عمولة المنصة الافتراضية إلى $percent%")
        }
    }

    fun adminAdjustUserGems(adjustment: Int, reason: String) {
        viewModelScope.launch {
            repository.adminAdjustUserGems(adjustment, reason)
            val action = if (adjustment >= 0) "إضافة $adjustment" else "خصم ${-adjustment}"
            _snackbarEvent.emit("تم $action جوهرة بنجاح")
        }
    }

    fun adminAddSimulatedPlayer(roundId: Long) {
        viewModelScope.launch {
            val added = repository.addSimulatedPlayerToRound(roundId)
            if (added) {
                _snackbarEvent.emit("تمت إضافة لاعب جديد للجولة وزاد وعاء الجائزة!")
            }
        }
    }

    fun adminDeleteRound(roundId: Long) {
        viewModelScope.launch {
            repository.deleteRound(roundId)
            _snackbarEvent.emit("تم حذف الجولة بنجاح")
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopBallDrawing()
    }
}
