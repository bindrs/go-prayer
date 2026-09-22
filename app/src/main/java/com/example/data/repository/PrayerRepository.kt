package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.SeedData
import com.example.data.local.entities.AdhanTrack
import com.example.data.local.entities.Ayat
import com.example.data.local.entities.LocationProfile
import com.example.data.local.entities.PrayerConfiguration
import com.example.data.local.entities.PrayerEventLog
import com.example.data.local.entities.QuranStreakRecord
import com.example.data.local.entities.SalahRecord
import com.example.data.local.entities.UserSettings
import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.DayPrayerSchedule
import com.example.domain.prayer.HighLatitudeRule
import com.example.domain.prayer.HijriCalendarHelper
import com.example.domain.prayer.Madhhab
import com.example.domain.prayer.PrayerTime
import com.example.domain.prayer.PrayerTimeCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class SalahMonthlyStats(
    val monthYear: String, // "2026-09"
    val monthDisplayName: String, // "September 2026"
    val totalOffered: Int,
    val totalPossible: Int,
    val completionPercentage: Int,
    val currentStreakDays: Int,
    val onTimeCount: Int,
    val congregationCount: Int,
    val qazaCount: Int,
    val missedCount: Int,
    val fajrCount: Int,
    val dhuhrCount: Int,
    val asrCount: Int,
    val maghribCount: Int,
    val ishaCount: Int
)

class PrayerRepository(private val database: AppDatabase) {

    val userSettingsFlow: Flow<UserSettings?> = database.userSettingsDao().getSettingsFlow()
    val locationFlow: Flow<LocationProfile?> = database.locationProfileDao().getLocationFlow()
    val prayerConfigsFlow: Flow<List<PrayerConfiguration>> = database.prayerConfigurationDao().getAllConfigurationsFlow()
    val adhanTracksFlow: Flow<List<AdhanTrack>> = database.adhanTrackDao().getAllTracksFlow()
    val ayatListFlow: Flow<List<Ayat>> = database.ayatDao().getAllAyatFlow()
    val fahishaAyatFlow: Flow<List<Ayat>> = database.ayatDao().getFahishaAyatFlow()
    val eventLogsFlow: Flow<List<PrayerEventLog>> = database.prayerEventLogDao().getRecentLogsFlow()
    val allSalahRecordsFlow: Flow<List<SalahRecord>> = database.salahRecordDao().getAllRecordsFlow()

    fun getMonthlySalahRecordsFlow(monthYear: String): Flow<List<SalahRecord>> {
        return database.salahRecordDao().getRecordsForMonthFlow(monthYear)
    }

    fun getDailySalahRecordsFlow(date: String): Flow<List<SalahRecord>> {
        return database.salahRecordDao().getRecordsForDateFlow(date)
    }

    suspend fun toggleSalahOffered(
        date: String,
        prayerName: String,
        isOffered: Boolean,
        offeringType: String = "ON_TIME",
        notes: String? = null
    ) {
        val parts = date.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
        val month = parts.getOrNull(1)?.toIntOrNull() ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)
        val day = parts.getOrNull(2)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val monthYear = String.format(Locale.US, "%04d-%02d", year, month)

        val existing = database.salahRecordDao().getRecord(date, prayerName)
        val record = SalahRecord(
            id = existing?.id ?: 0,
            date = date,
            monthYear = monthYear,
            year = year,
            month = month,
            day = day,
            prayerName = prayerName,
            isOffered = isOffered,
            offeredAtMillis = if (isOffered) System.currentTimeMillis() else null,
            offeringType = if (isOffered) offeringType else "MISSED",
            notes = notes ?: existing?.notes
        )
        database.salahRecordDao().insertOrUpdate(record)
        if (prayerName.equals("Jummah", ignoreCase = true)) {
            val existingDhuhr = database.salahRecordDao().getRecord(date, "Dhuhr")
            val dhuhrRecord = record.copy(id = existingDhuhr?.id ?: 0, prayerName = "Dhuhr")
            database.salahRecordDao().insertOrUpdate(dhuhrRecord)
        } else if (prayerName.equals("Dhuhr", ignoreCase = true)) {
            val existingJummah = database.salahRecordDao().getRecord(date, "Jummah")
            val jummahRecord = record.copy(id = existingJummah?.id ?: 0, prayerName = "Jummah")
            database.salahRecordDao().insertOrUpdate(jummahRecord)
        }
    }

    suspend fun recordPrayerCompletedToday(prayerName: String) {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val dateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
        toggleSalahOffered(dateStr, prayerName, true, "ON_TIME")
    }

    suspend fun isPrayerOfferedToday(prayerName: String): Boolean {
        return try {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val dateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
            val record = database.salahRecordDao().getRecord(dateStr, prayerName)
            if (record?.isOffered == true) return true
            if (prayerName.equals("Jummah", ignoreCase = true)) {
                return database.salahRecordDao().getRecord(dateStr, "Dhuhr")?.isOffered == true
            } else if (prayerName.equals("Dhuhr", ignoreCase = true)) {
                return database.salahRecordDao().getRecord(dateStr, "Jummah")?.isOffered == true
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    suspend fun clearAllSalahRecords() {
        database.salahRecordDao().deleteAllSalahRecords()
        database.prayerEventLogDao().clearAll()
    }

    suspend fun getAllSalahRecords(): List<SalahRecord> {
        return database.salahRecordDao().getAllRecords()
    }

    suspend fun getAllQuranStreaks(): List<QuranStreakRecord> {
        return database.quranStreakDao().getAllCompletedStreaks()
    }

    suspend fun getUserSettings(): UserSettings? {
        return database.userSettingsDao().getSettings()
    }

    suspend fun getRecentEventLogs(): List<PrayerEventLog> {
        return database.prayerEventLogDao().getRecentLogs()
    }

    fun calculateMonthlyStats(monthYear: String, records: List<SalahRecord>): SalahMonthlyStats {
        val parts = monthYear.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
        val month = parts.getOrNull(1)?.toIntOrNull() ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val monthDisplayName = monthFormat.format(cal.time)

        val totalPossible = maxDaysInMonth * 5
        val offeredRecords = records.filter { it.isOffered }
        val totalOffered = offeredRecords.size
        val completionPercentage = if (totalPossible > 0) ((totalOffered * 100) / totalPossible) else 0

        var onTimeCount = 0
        var congregationCount = 0
        var qazaCount = 0
        var missedCount = 0

        var fajrCount = 0
        var dhuhrCount = 0
        var asrCount = 0
        var maghribCount = 0
        var ishaCount = 0

        for (rec in records) {
            if (rec.isOffered) {
                when (rec.offeringType) {
                    "CONGREGATION" -> congregationCount++
                    "QAZA" -> qazaCount++
                    else -> onTimeCount++
                }
                when (rec.prayerName) {
                    "Fajr" -> fajrCount++
                    "Dhuhr" -> dhuhrCount++
                    "Asr" -> asrCount++
                    "Maghrib" -> maghribCount++
                    "Isha" -> ishaCount++
                }
            } else {
                missedCount++
            }
        }

        // Compute streak (consecutive days with at least 4 prayers offered up to current day)
        var streakDays = 0
        val recordsByDay = records.groupBy { it.day }
        for (d in maxDaysInMonth downTo 1) {
            val dayList = recordsByDay[d] ?: emptyList()
            val dayOffered = dayList.count { it.isOffered }
            if (dayOffered >= 4) {
                streakDays++
            } else if (dayList.isNotEmpty()) {
                break
            }
        }

        return SalahMonthlyStats(
            monthYear = monthYear,
            monthDisplayName = monthDisplayName,
            totalOffered = totalOffered,
            totalPossible = totalPossible,
            completionPercentage = completionPercentage,
            currentStreakDays = streakDays,
            onTimeCount = onTimeCount,
            congregationCount = congregationCount,
            qazaCount = qazaCount,
            missedCount = missedCount,
            fajrCount = fajrCount,
            dhuhrCount = dhuhrCount,
            asrCount = asrCount,
            maghribCount = maghribCount,
            ishaCount = ishaCount
        )
    }

    suspend fun getSettings(): UserSettings {
        return database.userSettingsDao().getSettings() ?: SeedData.defaultSettings.also {
            database.userSettingsDao().insertOrUpdate(it)
        }
    }

    suspend fun updateSettings(settings: UserSettings) {
        database.userSettingsDao().insertOrUpdate(settings)
    }

    suspend fun getLocation(): LocationProfile {
        return database.locationProfileDao().getLocation() ?: SeedData.defaultLocation.also {
            database.locationProfileDao().insertOrUpdate(it)
        }
    }

    suspend fun updateLocation(location: LocationProfile) {
        database.locationProfileDao().insertOrUpdate(location)
    }

    suspend fun getPrayerConfigurations(): List<PrayerConfiguration> {
        val configs = database.prayerConfigurationDao().getAllConfigurations()
        return if (configs.isEmpty()) {
            SeedData.defaultConfigurations.also {
                database.prayerConfigurationDao().insertAll(it)
            }
        } else {
            configs
        }
    }

    suspend fun getPrayerConfiguration(prayerName: String): PrayerConfiguration? {
        return database.prayerConfigurationDao().getConfiguration(prayerName)
    }

    suspend fun updatePrayerConfiguration(config: PrayerConfiguration) {
        database.prayerConfigurationDao().insertOrUpdate(config)
    }

    suspend fun getRandomAyat(): Ayat {
        ensureAyatLoaded()
        return database.ayatDao().getRandomAyat() ?: SeedData.verifiedAyatList.first()
    }

    suspend fun ensureAyatLoaded() {
        val count = database.ayatDao().getCount()
        if (count < SeedData.verifiedAyatList.size) {
            database.ayatDao().insertAll(SeedData.verifiedAyatList)
        }
    }

    suspend fun getFahishaAyat(): List<Ayat> {
        ensureAyatLoaded()
        val list = database.ayatDao().getFahishaAyat()
        return if (list.isEmpty()) SeedData.fahishaAyatList else list
    }

    val warningAyatFlow: Flow<List<Ayat>> = database.ayatDao().getWarningAyatFlow()

    suspend fun getWarningAyat(): List<Ayat> {
        ensureAyatLoaded()
        val list = database.ayatDao().getWarningAyat()
        return if (list.isEmpty()) SeedData.warningAyatList else list
    }

    suspend fun logPrayerEvent(log: PrayerEventLog) {
        database.prayerEventLogDao().insertLog(log)
    }

    suspend fun clearEventLogs() {
        database.prayerEventLogDao().clearAll()
    }

    suspend fun ensureInitialized() {
        try {
            val currentSettings = database.userSettingsDao().getSettings()
            if (currentSettings == null) {
                database.userSettingsDao().insertOrUpdate(SeedData.defaultSettings)
            }
            val currentLoc = database.locationProfileDao().getLocation()
            if (currentLoc == null) {
                database.locationProfileDao().insertOrUpdate(SeedData.defaultLocation)
            }
            val currentConfigs = database.prayerConfigurationDao().getAllConfigurations()
            if (currentConfigs.isEmpty()) {
                database.prayerConfigurationDao().insertAll(SeedData.defaultConfigurations)
            }
            val currentTracks = database.adhanTrackDao().getAllTracks()
            if (currentTracks.isEmpty()) {
                database.adhanTrackDao().insertAll(SeedData.defaultAdhanTracks)
            }
            ensureAyatLoaded()
        } catch (e: Exception) {
            android.util.Log.e("PrayerRepository", "ensureInitialized error: ${e.message}")
        }
    }

    suspend fun restoreFromBackup(backup: com.example.util.ParsedBackupData) {
        try {
            if (backup.salahRecords.isNotEmpty()) {
                database.salahRecordDao().insertAll(backup.salahRecords)
            }
            if (backup.quranStreaks.isNotEmpty()) {
                for (streak in backup.quranStreaks) {
                    database.quranStreakDao().insertOrUpdate(streak)
                }
            }
            if (backup.settings != null) {
                database.userSettingsDao().insertOrUpdate(backup.settings)
            }
        } catch (e: Exception) {
            android.util.Log.e("PrayerRepository", "Error applying backup data: ${e.message}")
        }
    }

    suspend fun getAdhanTracks(): List<AdhanTrack> {
        val tracks = database.adhanTrackDao().getAllTracks()
        return if (tracks.isEmpty()) {
            SeedData.defaultAdhanTracks.also {
                database.adhanTrackDao().insertAll(it)
            }
        } else {
            tracks
        }
    }

    suspend fun getAdhanTrackById(id: String): AdhanTrack? {
        return database.adhanTrackDao().getTrackById(id)
    }

    suspend fun insertAdhanTrack(track: AdhanTrack) {
        database.adhanTrackDao().insertTrack(track)
    }

    suspend fun deleteAdhanTrack(id: String) {
        database.adhanTrackDao().deleteTrackById(id)
    }

    /**
     * Calculates the prayer schedule for a given date, location, and user settings.
     */
    fun calculateDaySchedule(
        calendar: Calendar,
        location: LocationProfile,
        settings: UserSettings,
        configs: List<PrayerConfiguration>
    ): DayPrayerSchedule {
        val timeZone = try {
            TimeZone.getTimeZone(location.timezone)
        } catch (_: Exception) {
            TimeZone.getDefault()
        }

        val cal = Calendar.getInstance(timeZone).apply {
            timeInMillis = calendar.timeInMillis
        }

        val tzOffsetHours = timeZone.getOffset(cal.timeInMillis).toDouble() / (1000.0 * 3600.0)

        val method = try {
            CalculationMethod.valueOf(settings.calculationMethod)
        } catch (_: Exception) {
            CalculationMethod.MWL
        }

        val madhhab = try {
            Madhhab.valueOf(settings.madhhab)
        } catch (_: Exception) {
            Madhhab.STANDARD
        }

        val highLatRule = try {
            HighLatitudeRule.valueOf(settings.highLatitudeRule)
        } catch (_: Exception) {
            HighLatitudeRule.MIDDLE_OF_NIGHT
        }

        val configMap = configs.associateBy { it.prayerName }
        val manualOffsets = configMap.mapValues { it.value.manualAdjustmentMinutes }

        val rawTimes = PrayerTimeCalculator.calculateTimes(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH) + 1,
            day = cal.get(Calendar.DAY_OF_MONTH),
            latitude = location.latitude,
            longitude = location.longitude,
            timezoneOffsetHours = tzOffsetHours,
            method = method,
            madhhab = madhhab,
            highLatitudeRule = highLatRule,
            manualOffsetsMinutes = manualOffsets
        )

        val is24Hour = settings.timeFormat == "24H"

        val fajrMillis = PrayerTimeCalculator.hoursToMillis(cal, rawTimes.fajrHours)
        val sunriseMillis = PrayerTimeCalculator.hoursToMillis(cal, rawTimes.sunriseHours)
        val dhuhrMillis = PrayerTimeCalculator.hoursToMillis(cal, rawTimes.dhuhrHours)
        val asrMillis = PrayerTimeCalculator.hoursToMillis(cal, rawTimes.asrHours)
        val maghribMillis = PrayerTimeCalculator.hoursToMillis(cal, rawTimes.maghribHours)
        val ishaMillis = PrayerTimeCalculator.hoursToMillis(cal, rawTimes.ishaHours)

        // Calculate Tahajjud (Last Third of the Night: between yesterday's Maghrib and today's Fajr, occurring after 12:00 AM on this day)
        val yesterdayCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayRaw = PrayerTimeCalculator.calculateTimes(
            year = yesterdayCal.get(Calendar.YEAR),
            month = yesterdayCal.get(Calendar.MONTH) + 1,
            day = yesterdayCal.get(Calendar.DAY_OF_MONTH),
            latitude = location.latitude,
            longitude = location.longitude,
            timezoneOffsetHours = tzOffsetHours,
            method = method,
            madhhab = madhhab,
            highLatitudeRule = highLatRule,
            manualOffsetsMinutes = manualOffsets
        )
        val yesterdayMaghribMillis = PrayerTimeCalculator.hoursToMillis(yesterdayCal, yesterdayRaw.maghribHours)
        val nightDurationMillis = (fajrMillis - yesterdayMaghribMillis).coerceAtLeast(1L)
        val tahajjudMillis = yesterdayMaghribMillis + (nightDurationMillis * 2 / 3)

        val isFriday = cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
        val dhuhrName = if (isFriday) "Jummah" else "Dhuhr"
        val dhuhrArabic = if (isFriday) "الجمعة" else "الظهر"
        val dhuhrUrdu = if (isFriday) "جمعۃ المبارک" else "ظہر"

        val now = System.currentTimeMillis()

        val prayersList = listOf(
            createPrayerTime("Tahajjud", "التهجد", "تہجد", tahajjudMillis, is24Hour, timeZone, configMap["Tahajjud"], now),
            createPrayerTime("Fajr", "الفجر", "فجر", fajrMillis, is24Hour, timeZone, configMap["Fajr"], now),
            createPrayerTime("Sunrise", "الشروق", "طلوع آفتاب", sunriseMillis, is24Hour, timeZone, configMap["Sunrise"], now),
            createPrayerTime(dhuhrName, dhuhrArabic, dhuhrUrdu, dhuhrMillis, is24Hour, timeZone, configMap[dhuhrName] ?: configMap["Dhuhr"], now),
            createPrayerTime("Asr", "العصر", "عصر", asrMillis, is24Hour, timeZone, configMap["Asr"], now),
            createPrayerTime("Maghrib", "المغرب", "مغرب", maghribMillis, is24Hour, timeZone, configMap["Maghrib"], now),
            createPrayerTime("Isha", "العشاء", "عشاء", ishaMillis, is24Hour, timeZone, configMap["Isha"], now)
        )

        // Determine next prayer (excluding sunrise as an active Adhan prayer, or including it for visual schedule)
        val futurePrayers = prayersList.filter { it.timestampMillis > now }
        val nextPrayer = futurePrayers.firstOrNull() ?: run {
            // Next is tomorrow's Fajr
            prayersList.first().copy(isNext = true)
        }

        val updatedPrayers = prayersList.map { prayer ->
            if (nextPrayer != null && prayer.name == nextPrayer.name && prayer.timestampMillis == nextPrayer.timestampMillis) {
                prayer.copy(isNext = true)
            } else {
                prayer
            }
        }

        val millisUntilNext = if (nextPrayer != null) {
            if (nextPrayer.timestampMillis > now) {
                nextPrayer.timestampMillis - now
            } else {
                // If past midnight, approx difference to tomorrow's Fajr
                val nextDayCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
                val tomorrowRaw = PrayerTimeCalculator.calculateTimes(
                    year = nextDayCal.get(Calendar.YEAR),
                    month = nextDayCal.get(Calendar.MONTH) + 1,
                    day = nextDayCal.get(Calendar.DAY_OF_MONTH),
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timezoneOffsetHours = tzOffsetHours,
                    method = method,
                    madhhab = madhhab,
                    highLatitudeRule = highLatRule,
                    manualOffsetsMinutes = manualOffsets
                )
                val tomorrowFajrMillis = PrayerTimeCalculator.hoursToMillis(nextDayCal, tomorrowRaw.fajrHours)
                (tomorrowFajrMillis - now).coerceAtLeast(0L)
            }
        } else 0L

        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        val dateString = dateFormat.format(cal.time)
        val hijriDate = HijriCalendarHelper.getHijriDate(cal)

        return DayPrayerSchedule(
            dateString = dateString,
            hijriDateString = when (settings.language) {
                "ur" -> hijriDate.formatUrdu()
                else -> hijriDate.formatEnglish()
            },
            locationName = "${location.city}, ${location.country}",
            prayers = updatedPrayers,
            nextPrayer = nextPrayer,
            millisUntilNextPrayer = millisUntilNext
        )
    }

    private fun createPrayerTime(
        name: String,
        arabicName: String,
        urduName: String,
        timeMillis: Long,
        is24Hour: Boolean,
        timeZone: TimeZone,
        config: PrayerConfiguration?,
        now: Long
    ): PrayerTime {
        val azanOffset = config?.azanOffsetMinutes ?: 0
        val jamatOffset = config?.jamatOffsetMinutes ?: 20
        val azanMillis = timeMillis + (azanOffset * 60 * 1000L)
        val jamatMillis = timeMillis + (jamatOffset * 60 * 1000L)

        return PrayerTime(
            name = name,
            arabicName = arabicName,
            urduName = urduName,
            timestampMillis = timeMillis,
            timeFormatted = PrayerTimeCalculator.formatTime(timeMillis, is24Hour, timeZone),
            azanTimeMillis = azanMillis,
            azanTimeFormatted = PrayerTimeCalculator.formatTime(azanMillis, is24Hour, timeZone),
            jamatTimeMillis = jamatMillis,
            jamatTimeFormatted = PrayerTimeCalculator.formatTime(jamatMillis, is24Hour, timeZone),
            isNext = false,
            isPast = timeMillis < now,
            adhanEnabled = config?.adhanEnabled ?: true,
            quietModeEnabled = config?.quietModeEnabled ?: true,
            quietModeDurationMinutes = config?.quietModeDurationMinutes ?: 20,
            enterSalahNotificationEnabled = config?.enterSalahNotificationEnabled ?: true
        )
    }

    /**
     * Reactive schedule flow that updates automatically when settings, location, or configs change
     */
    val scheduleFlow: Flow<DayPrayerSchedule> = combine(
        userSettingsFlow,
        locationFlow,
        prayerConfigsFlow
    ) { settings, location, configs ->
        val safeSettings = settings ?: SeedData.defaultSettings
        val safeLocation = location ?: SeedData.defaultLocation
        val safeConfigs = if (configs.isEmpty()) SeedData.defaultConfigurations else configs

        calculateDaySchedule(Calendar.getInstance(), safeLocation, safeSettings, safeConfigs)
    }

    // Quran Streak Methods
    fun getQuranStreakForDateFlow(date: String): Flow<QuranStreakRecord?> {
        return database.quranStreakDao().getStreakForDateFlow(date)
    }

    fun getQuranStreaksForMonthFlow(monthYear: String): Flow<List<QuranStreakRecord>> {
        return database.quranStreakDao().getStreaksForMonthFlow(monthYear)
    }

    val allCompletedQuranStreaksFlow: Flow<List<QuranStreakRecord>> = database.quranStreakDao().getAllCompletedStreaksFlow()

    suspend fun saveQuranStreak(record: QuranStreakRecord) {
        database.quranStreakDao().insertOrUpdate(record)
    }
}
