package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AdhanTrackDao
import com.example.data.local.dao.AyatDao
import com.example.data.local.dao.LocationProfileDao
import com.example.data.local.dao.PrayerConfigurationDao
import com.example.data.local.dao.PrayerEventLogDao
import com.example.data.local.dao.PrayerTimeRecordDao
import com.example.data.local.dao.QuranStreakDao
import com.example.data.local.dao.SalahRecordDao
import com.example.data.local.dao.UserSettingsDao
import com.example.data.local.entities.AdhanTrack
import com.example.data.local.entities.Ayat
import com.example.data.local.entities.LocationProfile
import com.example.data.local.entities.PrayerConfiguration
import com.example.data.local.entities.PrayerEventLog
import com.example.data.local.entities.PrayerTimeRecord
import com.example.data.local.entities.QuranStreakRecord
import com.example.data.local.entities.SalahRecord
import com.example.data.local.entities.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserSettings::class,
        LocationProfile::class,
        PrayerConfiguration::class,
        AdhanTrack::class,
        Ayat::class,
        PrayerEventLog::class,
        PrayerTimeRecord::class,
        SalahRecord::class,
        QuranStreakRecord::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun locationProfileDao(): LocationProfileDao
    abstract fun prayerConfigurationDao(): PrayerConfigurationDao
    abstract fun adhanTrackDao(): AdhanTrackDao
    abstract fun ayatDao(): AyatDao
    abstract fun prayerEventLogDao(): PrayerEventLogDao
    abstract fun prayerTimeRecordDao(): PrayerTimeRecordDao
    abstract fun salahRecordDao(): SalahRecordDao
    abstract fun quranStreakDao(): QuranStreakDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "go_prayer_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getDatabase(context)
                                database.userSettingsDao().insertOrUpdate(SeedData.defaultSettings)
                                database.locationProfileDao().insertOrUpdate(SeedData.defaultLocation)
                                database.prayerConfigurationDao().insertAll(SeedData.defaultConfigurations)
                                database.adhanTrackDao().insertAll(SeedData.defaultAdhanTracks)
                                database.ayatDao().insertAll(SeedData.verifiedAyatList)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
