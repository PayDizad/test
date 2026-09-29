package com.example.data

import com.example.data.dao.BingoDao
import com.example.data.model.AppSettingsEntity
import com.example.data.model.BingoCard
import com.example.data.model.BingoRoundEntity
import com.example.data.model.GemPackageEntity
import com.example.data.model.GemTransactionEntity
import com.example.data.model.RoundParticipantEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class BingoRepository(private val dao: BingoDao) {

    val settingsFlow: Flow<AppSettingsEntity?> = dao.getSettingsFlow()
    val userFlow: Flow<UserProfileEntity?> = dao.getUserFlow()
    val activeRoundsFlow: Flow<List<BingoRoundEntity>> = dao.getActiveRoundsFlow()
    val completedRoundsFlow: Flow<List<BingoRoundEntity>> = dao.getCompletedRoundsFlow()
    val allRoundsFlow: Flow<List<BingoRoundEntity>> = dao.getAllRoundsFlow()
    val transactionsFlow: Flow<List<GemTransactionEntity>> = dao.getTransactionsFlow()
    val packagesFlow: Flow<List<GemPackageEntity>> = dao.getPackagesFlow()

    fun getRoundFlow(roundId: Long): Flow<BingoRoundEntity?> = dao.getRoundByIdFlow(roundId)
    fun getParticipantsFlow(roundId: Long): Flow<List<RoundParticipantEntity>> = dao.getParticipantsFlow(roundId)

    suspend fun initializeDefaultsIfNeeded() {
        var settings = dao.getSettings()
        if (settings == null) {
            settings = AppSettingsEntity(
                id = 1,
                starterGems = 100, // 100 جوهرة بداية يحددها الآدمن
                defaultCommissionPercent = 15, // 15% عمولة المنصة
                platformTreasuryGems = 250, // رصيد افتراضي أولي للخزينة
                autoDrawIntervalSec = 3
            )
            dao.insertOrUpdateSettings(settings)
        }

        var user = dao.getUser()
        if (user == null) {
            val starterGems = settings.starterGems
            user = UserProfileEntity(
                id = 1L,
                name = "أنا (المشترك)",
                avatarId = 1,
                gems = starterGems,
                gamesPlayed = 0,
                gamesWon = 0,
                totalGemsWon = 0,
                isAdmin = false
            )
            dao.insertOrUpdateUser(user)
            dao.insertTransaction(
                GemTransactionEntity(
                    type = "STARTER_BONUS",
                    amount = starterGems,
                    description = "مكافأة الانضمام الترحيبية ($starterGems جوهرة)"
                )
            )
        }

        if (dao.getPackagesCount() == 0) {
            val defaultPackages = listOf(
                GemPackageEntity(gemAmount = 50, bonusGems = 0, priceDisplay = "$0.99", priceUsd = 0.99, badgeLabel = "مبتدئ"),
                GemPackageEntity(gemAmount = 150, bonusGems = 20, priceDisplay = "$2.99", priceUsd = 2.99, isPopular = true, badgeLabel = "الأكثر طلباً 🔥"),
                GemPackageEntity(gemAmount = 500, bonusGems = 100, priceDisplay = "$8.99", priceUsd = 8.99, badgeLabel = "قيمة ممتازة"),
                GemPackageEntity(gemAmount = 1200, bonusGems = 350, priceDisplay = "$19.99", priceUsd = 19.99, badgeLabel = "باقة المحترفين 💎"),
                GemPackageEntity(gemAmount = 3000, bonusGems = 1000, priceDisplay = "$44.99", priceUsd = 44.99, badgeLabel = "الملكية VIP 👑")
            )
            dao.insertPackages(defaultPackages)
        }

        // Seed initial rounds if no rounds exist
        val existingRounds = dao.getRoundById(1)
        if (existingRounds == null) {
            seedInitialDemoRounds()
        }
    }

    private suspend fun seedInitialDemoRounds() {
        val botNames = listOf(
            "سلطان بن فهد", "ريما السعيد", "فارس الرياض", "سارة م.",
            "عمر الكويتي", "نورة العلي", "خالد الحربي", "منى أحمد",
            "ياسين الدوسري", "ليلى كمال", "كريم المنصور", "هند الزهراني"
        )

        // Round 1: Active waiting round (10 gems)
        createRoundInternal(
            title = "جولة السحب السريع #101",
            entryFee = 10,
            commissionPercent = 15,
            status = "WAITING",
            botNames = botNames.shuffled().take(6)
        )

        // Round 2: Active waiting round (25 gems)
        createRoundInternal(
            title = "تحدي الزمرد المباشر #102",
            entryFee = 25,
            commissionPercent = 15,
            status = "WAITING",
            botNames = botNames.shuffled().take(8)
        )

        // Round 3: Completed round for transparency display
        val completedRoundId = createRoundInternal(
            title = "كأس الأبطال الذهبي #100",
            entryFee = 50,
            commissionPercent = 15,
            status = "COMPLETED",
            botNames = botNames.shuffled().take(10)
        )

        // Update completed round with winner info
        val round3 = dao.getRoundById(completedRoundId)
        if (round3 != null) {
            val totalPot = round3.totalPlayers * round3.entryFee // 10 * 50 = 500
            val commission = (totalPot * round3.commissionPercent) / 100 // 75
            val netPrize = totalPot - commission // 425
            dao.updateRound(
                round3.copy(
                    winnerName = "فارس الرياض",
                    winnerAvatarId = 3,
                    winningBall = 42,
                    winningPattern = "خط أفقي (صف 3)",
                    totalPot = totalPot,
                    platformCommission = commission,
                    netPrize = netPrize,
                    calledBallsJson = "7,18,33,42,65,12,28,40,55,71",
                    completedAt = System.currentTimeMillis() - 1000 * 60 * 35
                )
            )
        }
    }

    private suspend fun createRoundInternal(
        title: String,
        entryFee: Int,
        commissionPercent: Int,
        status: String,
        botNames: List<String>
    ): Long {
        val totalPlayers = botNames.size
        val totalPot = totalPlayers * entryFee
        val commission = (totalPot * commissionPercent) / 100
        val netPrize = totalPot - commission

        val round = BingoRoundEntity(
            title = title,
            entryFee = entryFee,
            commissionPercent = commissionPercent,
            status = status,
            totalPlayers = totalPlayers,
            totalPot = totalPot,
            platformCommission = commission,
            netPrize = netPrize,
            calledBallsJson = ""
        )
        val roundId = dao.insertRound(round)

        val participants = botNames.mapIndexed { idx, name ->
            val randomCard = BingoCard.generateRandom()
            RoundParticipantEntity(
                roundId = roundId,
                playerName = name,
                isUser = false,
                avatarId = (idx % 6) + 1,
                cardNumbersJson = randomCard.toNumbersList().joinToString(",")
            )
        }
        dao.insertParticipants(participants)
        return roundId
    }

    suspend fun createNewRound(
        title: String,
        entryFee: Int,
        commissionPercent: Int,
        initialBots: Int = 4
    ): Long {
        val botPool = listOf(
            "فيصل المطيري", "دانة الأحمد", "طارق الشمري", "جمانة ناصر",
            "سامي العتيبي", "لجين القحطاني", "بدر الهاجري", "روان الصالح"
        )
        val selectedBots = botPool.shuffled().take(initialBots)
        return createRoundInternal(
            title = title,
            entryFee = entryFee,
            commissionPercent = commissionPercent,
            status = "WAITING",
            botNames = selectedBots
        )
    }

    suspend fun purchaseGemPackage(pkg: GemPackageEntity): Boolean {
        val user = dao.getUser() ?: return false
        val totalGems = pkg.gemAmount + pkg.bonusGems
        val updatedUser = user.copy(gems = user.gems + totalGems)
        dao.insertOrUpdateUser(updatedUser)

        dao.insertTransaction(
            GemTransactionEntity(
                type = "PURCHASE",
                amount = totalGems,
                description = "شراء ${pkg.gemAmount} جوهرة (+${pkg.bonusGems} مكافأة) مقابل ${pkg.priceDisplay}"
            )
        )
        return true
    }

    suspend fun joinRound(roundId: Long): JoinResult {
        val user = dao.getUser() ?: return JoinResult.Error("المستخدم غير موجود")
        val round = dao.getRoundById(roundId) ?: return JoinResult.Error("الجولة غير موجودة")

        if (round.status == "COMPLETED") {
            return JoinResult.Error("هذه الجولة منتهية بالفعل")
        }

        val participants = dao.getParticipants(roundId)
        if (participants.any { it.isUser }) {
            return JoinResult.AlreadyJoined
        }

        if (user.gems < round.entryFee) {
            return JoinResult.InsufficientGems(needed = round.entryFee, current = user.gems)
        }

        // Deduct gems
        val updatedUser = user.copy(
            gems = user.gems - round.entryFee,
            gamesPlayed = user.gamesPlayed + 1
        )
        dao.insertOrUpdateUser(updatedUser)

        // Log transaction
        dao.insertTransaction(
            GemTransactionEntity(
                type = "ROUND_ENTRY",
                amount = -round.entryFee,
                description = "رسوم اشتراك في ${round.title}",
                roundId = roundId
            )
        )

        // Generate user card and save participant
        val userCard = BingoCard.generateRandom()
        dao.insertParticipant(
            RoundParticipantEntity(
                roundId = roundId,
                playerName = user.name,
                isUser = true,
                avatarId = user.avatarId,
                cardNumbersJson = userCard.toNumbersList().joinToString(",")
            )
        )

        // Recalculate round pot and platform commission
        val newTotalPlayers = round.totalPlayers + 1
        val newPot = newTotalPlayers * round.entryFee
        val newCommission = (newPot * round.commissionPercent) / 100
        val newNetPrize = newPot - newCommission

        dao.updateRound(
            round.copy(
                totalPlayers = newTotalPlayers,
                totalPot = newPot,
                platformCommission = newCommission,
                netPrize = newNetPrize
            )
        )

        return JoinResult.Success(userCard)
    }

    suspend fun startRound(roundId: Long) {
        val round = dao.getRoundById(roundId) ?: return
        dao.updateRound(round.copy(status = "PLAYING"))
    }

    suspend fun updateRoundCalledBalls(roundId: Long, ballsList: List<Int>) {
        val round = dao.getRoundById(roundId) ?: return
        dao.updateRound(
            round.copy(
                calledBallsJson = ballsList.joinToString(",")
            )
        )
    }

    suspend fun completeRoundWithWinner(
        roundId: Long,
        winnerName: String,
        winnerAvatar: Int,
        isUserWinner: Boolean,
        winningBall: Int,
        pattern: String
    ) {
        val round = dao.getRoundById(roundId) ?: return
        val settings = dao.getSettings() ?: AppSettingsEntity()

        // 1. Update round record
        val completedRound = round.copy(
            status = "COMPLETED",
            winnerName = winnerName,
            winnerAvatarId = winnerAvatar,
            winningBall = winningBall,
            winningPattern = pattern,
            completedAt = System.currentTimeMillis()
        )
        dao.updateRound(completedRound)

        // 2. Add commission to platform treasury
        val updatedSettings = settings.copy(
            platformTreasuryGems = settings.platformTreasuryGems + round.platformCommission
        )
        dao.insertOrUpdateSettings(updatedSettings)

        // 3. If user won, award the net prize!
        if (isUserWinner) {
            val user = dao.getUser()
            if (user != null) {
                val updatedUser = user.copy(
                    gems = user.gems + round.netPrize,
                    gamesWon = user.gamesWon + 1,
                    totalGemsWon = user.totalGemsWon + round.netPrize
                )
                dao.insertOrUpdateUser(updatedUser)

                dao.insertTransaction(
                    GemTransactionEntity(
                        type = "ROUND_WIN",
                        amount = round.netPrize,
                        description = "الفوز بالجائزة الكبرى في ${round.title} 🏆",
                        roundId = roundId
                    )
                )
            }
        }
    }

    // Admin operations
    suspend fun updateStarterGems(starterGems: Int) {
        val settings = dao.getSettings() ?: AppSettingsEntity()
        dao.insertOrUpdateSettings(settings.copy(starterGems = starterGems))
    }

    suspend fun updateCommissionPercent(percent: Int) {
        val settings = dao.getSettings() ?: AppSettingsEntity()
        dao.insertOrUpdateSettings(settings.copy(defaultCommissionPercent = percent))
    }

    suspend fun adminAdjustUserGems(adjustment: Int, reason: String) {
        val user = dao.getUser() ?: return
        val newBalance = (user.gems + adjustment).coerceAtLeast(0)
        dao.insertOrUpdateUser(user.copy(gems = newBalance))
        dao.insertTransaction(
            GemTransactionEntity(
                type = if (adjustment >= 0) "ADMIN_GIFT" else "ADMIN_DEDUCT",
                amount = adjustment,
                description = "تعديل إداري: $reason"
            )
        )
    }

    suspend fun addSimulatedPlayerToRound(roundId: Long): Boolean {
        val round = dao.getRoundById(roundId) ?: return false
        val names = listOf(
            "ماجد العصيمي", "سلوى البلوشي", "عبدالرحمن الشريف",
            "ريم الدوسري", "مشعل التميمي", "خلود الغامدي"
        )
        val existing = dao.getParticipants(roundId).map { it.playerName }
        val available = names.filterNot { it in existing }
        val botName = available.firstOrNull() ?: "لاعب جديد #${Random.nextInt(100, 999)}"

        val card = BingoCard.generateRandom()
        dao.insertParticipant(
            RoundParticipantEntity(
                roundId = roundId,
                playerName = botName,
                isUser = false,
                avatarId = Random.nextInt(1, 6),
                cardNumbersJson = card.toNumbersList().joinToString(",")
            )
        )

        val newPlayers = round.totalPlayers + 1
        val newPot = newPlayers * round.entryFee
        val newCommission = (newPot * round.commissionPercent) / 100
        val newNet = newPot - newCommission

        dao.updateRound(
            round.copy(
                totalPlayers = newPlayers,
                totalPot = newPot,
                platformCommission = newCommission,
                netPrize = newNet
            )
        )
        return true
    }

    suspend fun deleteRound(roundId: Long) {
        dao.deleteParticipantsByRound(roundId)
        dao.deleteRoundById(roundId)
    }
}

sealed class JoinResult {
    data class Success(val userCard: BingoCard) : JoinResult()
    data class InsufficientGems(val needed: Int, val current: Int) : JoinResult()
    data object AlreadyJoined : JoinResult()
    data class Error(val message: String) : JoinResult()
}
