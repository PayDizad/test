package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val starterGems: Int = 100, // الجواهر الممنوحة للمستخدمين الجدد والتي يحددها الآدمن
    val defaultCommissionPercent: Int = 15, // عمولة المنصة الافتراضية
    val platformTreasuryGems: Int = 0, // أرباح المنصة المجمعة من العمولات
    val autoDrawIntervalSec: Int = 4 // سرعة سحب الكرات
)

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 1L,
    val name: String = "اللاعب المحترف",
    val avatarId: Int = 1,
    val gems: Int = 100,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val totalGemsWon: Int = 0,
    val isAdmin: Boolean = false
)

@Entity(tableName = "bingo_rounds")
data class BingoRoundEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val entryFee: Int, // رسوم الدخول بالجواهر
    val commissionPercent: Int, // نسبة عمولة المنصة
    val status: String, // WAITING, PLAYING, COMPLETED, CANCELLED
    val totalPlayers: Int,
    val totalPot: Int, // مجموع اشتراكات اللاعبين (totalPlayers * entryFee)
    val platformCommission: Int, // عمولة المنصة المقتطعة
    val netPrize: Int, // الجائزة الصافية للفائز (totalPot - platformCommission)
    val winnerName: String? = null,
    val winnerAvatarId: Int? = null,
    val winningBall: Int? = null,
    val winningPattern: String? = null,
    val calledBallsJson: String = "", // قائمة الأرقام المسحوبة مفصولة بفواصل
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "round_participants")
data class RoundParticipantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roundId: Long,
    val playerName: String,
    val isUser: Boolean,
    val avatarId: Int,
    val cardNumbersJson: String // 25 أرقام مفصولة بفواصل تمثل بطاقة 5x5
)

@Entity(tableName = "gem_transactions")
data class GemTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // STARTER_BONUS, PURCHASE, ROUND_ENTRY, ROUND_WIN, ADMIN_GIFT, ADMIN_DEDUCT
    val amount: Int, // موجب أو سالب
    val description: String,
    val roundId: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "gem_packages")
data class GemPackageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gemAmount: Int,
    val bonusGems: Int = 0,
    val priceDisplay: String,
    val priceUsd: Double,
    val isPopular: Boolean = false,
    val badgeLabel: String = ""
)
