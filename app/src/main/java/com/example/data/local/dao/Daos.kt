package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.AdhanTrack
import com.example.data.local.entities.Ayat
import com.example.data.local.entities.LocationProfile
import com.example.data.local.entities.PrayerConfiguration
import com.example.data.local.entities.PrayerEventLog
import com.example.data.local.entities.PrayerTimeRecord
import com.example.data.local.entities.QuranStreakRecord
import com.example.data.local.entities.SalahRecord
import com.example.data.local.entities.UserSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: UserSettings)
}

@Dao
interface LocationProfileDao {
    @Query("SELECT * FROM location_profile WHERE id = 1 LIMIT 1")
    fun getLocationFlow(): Flow<LocationProfile?>

    @Query("SELECT * FROM location_profile WHERE id = 1 LIMIT 1")
    suspend fun getLocation(): LocationProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(location: LocationProfile)
}

@Dao
interface PrayerConfigurationDao {
    @Query("SELECT * FROM prayer_configuration")
    fun getAllConfigurationsFlow(): Flow<List<PrayerConfiguration>>

    @Query("SELECT * FROM prayer_configuration")
    suspend fun getAllConfigurations(): List<PrayerConfiguration>

    @Query("SELECT * FROM prayer_configuration WHERE prayerName = :prayerName LIMIT 1")
    suspend fun getConfiguration(prayerName: String): PrayerConfiguration?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(configs: List<PrayerConfiguration>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: PrayerConfiguration)

    @Update
    suspend fun update(config: PrayerConfiguration)
}

@Dao
interface AdhanTrackDao {
    @Query("SELECT * FROM adhan_track")
    fun getAllTracksFlow(): Flow<List<AdhanTrack>>

    @Query("SELECT * FROM adhan_track")
    suspend fun getAllTracks(): List<AdhanTrack>

    @Query("SELECT * FROM adhan_track WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: String): AdhanTrack?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: AdhanTrack)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<AdhanTrack>)

    @Query("DELETE FROM adhan_track WHERE id = :id")
    suspend fun deleteTrackById(id: String)
}

@Dao
interface AyatDao {
    @Query("SELECT * FROM ayat_library ORDER BY id ASC")
    fun getAllAyatFlow(): Flow<List<Ayat>>

    @Query("SELECT * FROM ayat_library ORDER BY id ASC")
    suspend fun getAllAyat(): List<Ayat>

    @Query("SELECT * FROM ayat_library ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomAyat(): Ayat?

    @Query("SELECT * FROM ayat_library WHERE id = :id LIMIT 1")
    suspend fun getAyatById(id: Int): Ayat?

    @Query("SELECT COUNT(*) FROM ayat_library")
    suspend fun getCount(): Int

    @Query("SELECT * FROM ayat_library WHERE themeTopic = 'Fahisha & Modesty' ORDER BY id ASC")
    fun getFahishaAyatFlow(): Flow<List<Ayat>>

    @Query("SELECT * FROM ayat_library WHERE themeTopic = 'Fahisha & Modesty' ORDER BY id ASC")
    suspend fun getFahishaAyat(): List<Ayat>

    @Query("SELECT * FROM ayat_library WHERE themeTopic = 'Salah Warning & Azaab' ORDER BY id ASC")
    fun getWarningAyatFlow(): Flow<List<Ayat>>

    @Query("SELECT * FROM ayat_library WHERE themeTopic = 'Salah Warning & Azaab' ORDER BY id ASC")
    suspend fun getWarningAyat(): List<Ayat>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ayatList: List<Ayat>)
}

@Dao
interface PrayerEventLogDao {
    @Query("SELECT * FROM prayer_event_logs ORDER BY triggeredAt DESC LIMIT 50")
    fun getRecentLogsFlow(): Flow<List<PrayerEventLog>>

    @Query("SELECT * FROM prayer_event_logs ORDER BY triggeredAt DESC LIMIT 50")
    suspend fun getRecentLogs(): List<PrayerEventLog>

    @Insert
    suspend fun insertLog(log: PrayerEventLog)

    @Query("DELETE FROM prayer_event_logs")
    suspend fun clearAll()
}

@Dao
interface PrayerTimeRecordDao {
    @Query("SELECT * FROM prayer_time_records WHERE date = :date ORDER BY prayerTimeMillis ASC")
    suspend fun getRecordsForDate(date: String): List<PrayerTimeRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<PrayerTimeRecord>)

    @Query("DELETE FROM prayer_time_records WHERE date = :date")
    suspend fun deleteForDate(date: String)
}

@Dao
interface SalahRecordDao {
    @Query("SELECT * FROM salah_records WHERE monthYear = :monthYear ORDER BY date ASC, id ASC")
    fun getRecordsForMonthFlow(monthYear: String): Flow<List<SalahRecord>>

    @Query("SELECT * FROM salah_records WHERE date = :date ORDER BY id ASC")
    fun getRecordsForDateFlow(date: String): Flow<List<SalahRecord>>

    @Query("SELECT * FROM salah_records ORDER BY date DESC, id ASC")
    fun getAllRecordsFlow(): Flow<List<SalahRecord>>

    @Query("SELECT * FROM salah_records ORDER BY date ASC, id ASC")
    suspend fun getAllRecords(): List<SalahRecord>

    @Query("SELECT * FROM salah_records WHERE monthYear = :monthYear")
    suspend fun getRecordsForMonth(monthYear: String): List<SalahRecord>

    @Query("SELECT * FROM salah_records WHERE date = :date")
    suspend fun getRecordsForDate(date: String): List<SalahRecord>

    @Query("SELECT * FROM salah_records WHERE date = :date AND prayerName = :prayerName LIMIT 1")
    suspend fun getRecord(date: String, prayerName: String): SalahRecord?

    @Query("SELECT COUNT(*) FROM salah_records WHERE monthYear = :monthYear AND isOffered = 1")
    fun getMonthlyOfferedCountFlow(monthYear: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM salah_records WHERE monthYear = :monthYear AND isOffered = 1")
    suspend fun getMonthlyOfferedCount(monthYear: String): Int

    @Query("SELECT COUNT(*) FROM salah_records WHERE monthYear = :monthYear AND prayerName = :prayerName AND isOffered = 1")
    suspend fun getMonthlyOfferedCountForPrayer(monthYear: String, prayerName: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: SalahRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<SalahRecord>)

    @Query("DELETE FROM salah_records WHERE date = :date AND prayerName = :prayerName")
    suspend fun deleteRecord(date: String, prayerName: String)

    @Query("DELETE FROM salah_records")
    suspend fun deleteAllSalahRecords()

    @Query("SELECT COUNT(*) FROM salah_records")
    suspend fun getTotalRecordsCount(): Int
}

@Dao
interface QuranStreakDao {
    @Query("SELECT * FROM quran_streak_records WHERE date = :date LIMIT 1")
    fun getStreakForDateFlow(date: String): Flow<QuranStreakRecord?>

    @Query("SELECT * FROM quran_streak_records WHERE date = :date LIMIT 1")
    suspend fun getStreakForDate(date: String): QuranStreakRecord?

    @Query("SELECT * FROM quran_streak_records WHERE monthYear = :monthYear ORDER BY date ASC")
    fun getStreaksForMonthFlow(monthYear: String): Flow<List<QuranStreakRecord>>

    @Query("SELECT * FROM quran_streak_records WHERE isCompleted = 1 ORDER BY date DESC")
    fun getAllCompletedStreaksFlow(): Flow<List<QuranStreakRecord>>

    @Query("SELECT * FROM quran_streak_records WHERE isCompleted = 1 ORDER BY date DESC")
    suspend fun getAllCompletedStreaks(): List<QuranStreakRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: QuranStreakRecord)

    @Query("DELETE FROM quran_streak_records")
    suspend fun deleteAll()
}

