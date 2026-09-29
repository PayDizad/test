package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.BingoDao
import com.example.data.model.AppSettingsEntity
import com.example.data.model.BingoRoundEntity
import com.example.data.model.GemPackageEntity
import com.example.data.model.GemTransactionEntity
import com.example.data.model.RoundParticipantEntity
import com.example.data.model.UserProfileEntity

@Database(
    entities = [
        AppSettingsEntity::class,
        UserProfileEntity::class,
        BingoRoundEntity::class,
        RoundParticipantEntity::class,
        GemTransactionEntity::class,
        GemPackageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bingoDao(): BingoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bingo_gems_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
