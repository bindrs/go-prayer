package com.example.domain.prayer

import java.util.Calendar
import kotlin.math.floor

object HijriCalendarHelper {

    private val hijriMonthsEnglish = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    private val hijriMonthsArabic = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    private val hijriMonthsUrdu = listOf(
        "محرم الحرام", "صفر المظفر", "ربیع الاول", "ربیع الثانی",
        "جمادی الاول", "جمادی الثانی", "رجب المرجب", "شعبان المعظم",
        "رمضان المبارک", "شوال المکرم", "ذوالقعدہ", "ذوالحجہ"
    )

    data class HijriDate(
        val day: Int,
        val month: Int, // 1-12
        val year: Int,
        val monthNameEnglish: String,
        val monthNameArabic: String,
        val monthNameUrdu: String
    ) {
        fun formatEnglish(): String = "$day $monthNameEnglish $year AH"
        fun formatArabic(): String = "$day $monthNameArabic $year هـ"
        fun formatUrdu(): String = "$day $monthNameUrdu $year ھ"
    }

    /**
     * Converts Gregorian date to Hijri date using Kuwaiti / Tabular astronomical algorithm
     */
    fun getHijriDate(calendar: Calendar = Calendar.getInstance()): HijriDate {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)

        var m = month
        var y = year
        if (m < 3) {
            y -= 1
            m += 12
        }

        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524

        // Julian Day to Hijri
        val z = jd - 1948440 + 10632
        val n = floor((z - 1) / 10631.0)
        val zRem = z - 10631 * n + 354
        val j = (floor((10985 - zRem) / 5316.0)) * (floor((50 * zRem) / 17719.0)) +
                (floor(zRem / 5670.0)) * (floor((43 * zRem) / 15238.0))
        val zAdjusted = zRem - (floor((30 - j) / 15.0)) * (floor((17719 * j) / 50.0)) -
                (floor(j / 16.0)) * (floor((15238 * j) / 43.0)) + 29

        val hMonth = floor((24 * zAdjusted) / 709.0).toInt()
        val hDay = (zAdjusted - floor((709 * hMonth) / 24.0)).toInt()
        val hYear = (30 * n + j - 30).toInt()

        val validMonth = ((hMonth - 1).coerceIn(0, 11))
        val validDay = hDay.coerceIn(1, 30)

        return HijriDate(
            day = validDay,
            month = validMonth + 1,
            year = hYear,
            monthNameEnglish = hijriMonthsEnglish[validMonth],
            monthNameArabic = hijriMonthsArabic[validMonth],
            monthNameUrdu = hijriMonthsUrdu[validMonth]
        )
    }
}
