package com.example.domain.prayer

data class PrayerTime(
    val name: String,
    val arabicName: String,
    val urduName: String,
    val timestampMillis: Long, // Enter Salah Time (Automatic by Location)
    val timeFormatted: String, // Enter Salah Time Formatted
    val azanTimeMillis: Long = timestampMillis, // Azan Time (Manually adjustable in Settings)
    val azanTimeFormatted: String = timeFormatted,
    val jamatTimeMillis: Long = timestampMillis + (20 * 60 * 1000L), // Jama'at Time (Manually adjustable in Settings)
    val jamatTimeFormatted: String = timeFormatted,
    val isNext: Boolean = false,
    val isPast: Boolean = false,
    val adhanEnabled: Boolean = true,
    val quietModeEnabled: Boolean = true,
    val quietModeDurationMinutes: Int = 20,
    val enterSalahNotificationEnabled: Boolean = true
)

data class DayPrayerSchedule(
    val dateString: String,
    val hijriDateString: String,
    val locationName: String,
    val prayers: List<PrayerTime>,
    val nextPrayer: PrayerTime?,
    val millisUntilNextPrayer: Long
)

enum class CalculationMethod(val displayName: String, val fajrAngle: Double, val ishaAngle: Double, val isIshaFixedMinutes: Boolean = false, val ishaFixedMinutes: Int = 0) {
    MWL("Muslim World League", 18.0, 17.0),
    ISNA("Islamic Society of North America (ISNA)", 15.0, 15.0),
    EGYPT("Egyptian General Authority of Survey", 19.5, 17.5),
    MAKKAH("Umm Al-Qura University, Makkah", 18.5, 0.0, true, 90),
    KARACHI("University of Islamic Sciences, Karachi", 18.0, 18.0),
    GULF("Gulf Region / Dubai", 18.2, 18.2)
}

enum class Madhhab(val displayName: String, val shadowRatio: Double) {
    STANDARD("Standard (Shafi'i, Maliki, Hanbali)", 1.0),
    HANAFI("Hanafi", 2.0)
}

enum class HighLatitudeRule(val displayName: String) {
    MIDDLE_OF_NIGHT("Middle of the Night"),
    SEVENTH_OF_NIGHT("One Seventh of the Night"),
    ANGLE_BASED("Angle Based")
}
