package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ZipScanReportEntity::class,
        BusinessLeadEntity::class,
        OutreachCampaignEntity::class,
        AgencyCalculatorSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun zipScanReportDao(): ZipScanReportDao
    abstract fun businessLeadDao(): BusinessLeadDao
    abstract fun outreachCampaignDao(): OutreachCampaignDao
    abstract fun calculatorSettingsDao(): CalculatorSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "localpulse_ai_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
