package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Int = 1,
    val language: String = "en", // "en", "ur"
    val theme: String = "system", // "system", "light", "dark"
    val calculationMethod: String = "MWL", // MWL, ISNA, EGYPT, MAKKAH, KARACHI, GULF
    val madhhab: String = "STANDARD", // STANDARD, HANAFI
    val highLatitudeRule: String = "MIDDLE_OF_NIGHT", // ANGLE_BASED, SEVENTH_OF_NIGHT, MIDDLE_OF_NIGHT
    val timeFormat: String = "12H", // 12H, 24H
    val quietModeEnabled: Boolean = true,
    val quietModeDurationMinutes: Int = 20,
    val keepScreenAwake: Boolean = true,
    val isOnboardingCompleted: Boolean = false,
    val arabicFontPreference: String = "Traditional",
    val safetyExceptionsAllowed: Boolean = true,
    val totalDisableOnPrayer: Boolean = true,
    val vpnFahishaAlertEnabled: Boolean = true,
    val quranDailyTargetLines: Int = 10,
    val userName: String = "",
    val spiritualIdentity: String = "بندۂ خدا (Servant of Allah)",
    val favoriteLoveAyahId: String = "az_zumar_53"
)

@Entity(
    tableName = "quran_streak_records",
    indices = [
        androidx.room.Index(value = ["date"], unique = true),
        androidx.room.Index(value = ["monthYear"])
    ]
)
data class QuranStreakRecord(
    @PrimaryKey val date: String, // "YYYY-MM-DD" e.g., "2026-09-16"
    val monthYear: String, // "YYYY-MM" e.g., "2026-09"
    val versesRead: Int,
    val targetLines: Int,
    val isCompleted: Boolean = false,
    val completedAtMillis: Long = System.currentTimeMillis(),
    val surahName: String = "Al-Fatiha",
    val startAyah: Int = 1,
    val endAyah: Int = 7
)

@Entity(tableName = "location_profile")
data class LocationProfile(
    @PrimaryKey val id: Int = 1,
    val city: String = "Makkah",
    val country: String = "Saudi Arabia",
    val latitude: Double = 21.4225,
    val longitude: Double = 39.8262,
    val timezone: String = "Asia/Riyadh",
    val source: String = "MANUAL", // GPS, MANUAL
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "prayer_configuration")
data class PrayerConfiguration(
    @PrimaryKey val prayerName: String, // Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha
    val adhanEnabled: Boolean = true,
    val notificationEnabled: Boolean = true,
    val quietModeEnabled: Boolean = true,
    val quietModeDurationMinutes: Int = 20,
    val audioTrackId: String = "makkah_adhan",
    val manualAdjustmentMinutes: Int = 0, // Enter Salah fine-tuning
    val azanOffsetMinutes: Int = 0, // Manual adjustment for Azan (minutes after Enter Salah)
    val jamatOffsetMinutes: Int = 20, // Manual adjustment for Jama'at (minutes after Enter Salah)
    val enterSalahNotificationEnabled: Boolean = true
)

@Entity(tableName = "adhan_track")
data class AdhanTrack(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val localUri: String,
    val durationSeconds: Int,
    val isDefault: Boolean = false
)

@Entity(tableName = "ayat_library")
data class Ayat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val arabicText: String,
    val transliteration: String,
    val englishTranslation: String,
    val urduTranslation: String,
    val surahName: String,
    val surahArabicName: String,
    val ayatNumber: String,
    val referenceSource: String,
    val verificationStatus: String = "VERIFIED_AUTHENTIC",
    val themeTopic: String = "Salah & Remembrance"
)

@Entity(tableName = "prayer_event_logs")
data class PrayerEventLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prayerName: String,
    val scheduledTime: Long,
    val triggeredAt: Long = System.currentTimeMillis(),
    val adhanStatus: String, // "PLAYED", "MUTED", "NOTIFICATION_ONLY", "FAILED"
    val quietModeStatus: String, // "DND_ACTIVATED", "PERMISSION_DENIED", "DISABLED"
    val screenStatus: String, // "LAUNCHED", "BACKGROUND_NOTIFICATION"
    val errorMessage: String? = null
)

@Entity(tableName = "prayer_time_records")
data class PrayerTimeRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val prayerName: String,
    val prayerTimeMillis: Long,
    val endTimeMillis: Long,
    val calculationMethod: String,
    val manualAdjustmentMinutes: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "salah_records",
    indices = [
        androidx.room.Index(value = ["date", "prayerName"], unique = true),
        androidx.room.Index(value = ["monthYear"])
    ]
)
data class SalahRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // "YYYY-MM-DD" e.g., "2026-09-15"
    val monthYear: String, // "YYYY-MM" e.g., "2026-09"
    val year: Int,
    val month: Int, // 1 - 12
    val day: Int, // 1 - 31
    val prayerName: String, // Fajr, Dhuhr, Asr, Maghrib, Isha
    val isOffered: Boolean = false,
    val offeredAtMillis: Long? = null,
    val offeringType: String = "ON_TIME", // "ON_TIME", "CONGREGATION", "QAZA", "MISSED"
    val notes: String? = null
)

