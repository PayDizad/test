package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppSettingsEntity
import com.example.data.model.BingoRoundEntity
import com.example.data.model.GemPackageEntity
import com.example.data.model.GemTransactionEntity
import com.example.data.model.RoundParticipantEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BingoDao {

    // Settings
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: AppSettingsEntity)

    // User Profile
    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun getUserFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    suspend fun getUser(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserProfileEntity)

    // Bingo Rounds
    @Query("SELECT * FROM bingo_rounds ORDER BY id DESC")
    fun getAllRoundsFlow(): Flow<List<BingoRoundEntity>>

    @Query("SELECT * FROM bingo_rounds WHERE status != 'COMPLETED' AND status != 'CANCELLED' ORDER BY id DESC")
    fun getActiveRoundsFlow(): Flow<List<BingoRoundEntity>>

    @Query("SELECT * FROM bingo_rounds WHERE status = 'COMPLETED' ORDER BY completedAt DESC, id DESC")
    fun getCompletedRoundsFlow(): Flow<List<BingoRoundEntity>>

    @Query("SELECT * FROM bingo_rounds WHERE id = :id LIMIT 1")
    fun getRoundByIdFlow(id: Long): Flow<BingoRoundEntity?>

    @Query("SELECT * FROM bingo_rounds WHERE id = :id LIMIT 1")
    suspend fun getRoundById(id: Long): BingoRoundEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(round: BingoRoundEntity): Long

    @Update
    suspend fun updateRound(round: BingoRoundEntity)

    @Query("DELETE FROM bingo_rounds WHERE id = :id")
    suspend fun deleteRoundById(id: Long)

    // Round Participants
    @Query("SELECT * FROM round_participants WHERE roundId = :roundId ORDER BY id ASC")
    fun getParticipantsFlow(roundId: Long): Flow<List<RoundParticipantEntity>>

    @Query("SELECT * FROM round_participants WHERE roundId = :roundId ORDER BY id ASC")
    suspend fun getParticipants(roundId: Long): List<RoundParticipantEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: RoundParticipantEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(participants: List<RoundParticipantEntity>)

    @Query("DELETE FROM round_participants WHERE roundId = :roundId")
    suspend fun deleteParticipantsByRound(roundId: Long)

    // Gem Transactions
    @Query("SELECT * FROM gem_transactions ORDER BY timestamp DESC LIMIT 100")
    fun getTransactionsFlow(): Flow<List<GemTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: GemTransactionEntity): Long

    // Gem Packages
    @Query("SELECT * FROM gem_packages ORDER BY gemAmount ASC")
    fun getPackagesFlow(): Flow<List<GemPackageEntity>>

    @Query("SELECT COUNT(*) FROM gem_packages")
    suspend fun getPackagesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackages(packages: List<GemPackageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(pkg: GemPackageEntity): Long
}
