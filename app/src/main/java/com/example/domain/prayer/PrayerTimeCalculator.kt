package com.example.domain.prayer

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

object PrayerTimeCalculator {

    data class RawPrayerTimes(
        val fajrHours: Double,
        val sunriseHours: Double,
        val dhuhrHours: Double,
        val asrHours: Double,
        val maghribHours: Double,
        val ishaHours: Double
    )

    fun calculateTimes(
        year: Int,
        month: Int, // 1-12
        day: Int,
        latitude: Double,
        longitude: Double,
        timezoneOffsetHours: Double,
        method: CalculationMethod,
        madhhab: Madhhab,
        highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
        manualOffsetsMinutes: Map<String, Int> = emptyMap()
    ): RawPrayerTimes {
        val julianDate = calculateJulianDate(year, month, day) - (longitude / (15.0 * 24.0))

        val d = julianDate - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = fixAngle(Math.toDegrees(atan(cos(Math.toRadians(e)) * tan(Math.toRadians(l))))) / 15.0
        val raAdjusted = ra + (floor(l / 90.0) - floor(ra / 6.0)) * 6.0

        val declination = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        val equationOfTime = (q / 15.0) - raAdjusted

        // Solar noon (Dhuhr)
        val solarNoon = 12.0 + timezoneOffsetHours - (longitude / 15.0) - equationOfTime

        // Sunrise & Sunset angle is approx -0.833 degrees
        val sunAlt = -0.833
        val sunriseAngle = calculateHourAngle(sunAlt, latitude, declination)

        val sunrise = solarNoon - (sunriseAngle / 15.0)
        val sunset = solarNoon + (sunriseAngle / 15.0)

        // Fajr angle
        var fajrHourAngle = calculateHourAngle(-method.fajrAngle, latitude, declination)
        var fajr = if (fajrHourAngle.isNaN()) {
            applyHighLatitudeRule(highLatitudeRule, sunrise, sunset, method.fajrAngle, isFajr = true)
        } else {
            solarNoon - (fajrHourAngle / 15.0)
        }

        // Asr angle (solar altitude above horizon in degrees)
        val asrAlt = Math.toDegrees(atan(1.0 / (madhhab.shadowRatio + tan(Math.toRadians(abs(latitude - declination))))))
        val asrHourAngle = calculateHourAngle(asrAlt, latitude, declination)
        val asr = solarNoon + (asrHourAngle / 15.0)

        // Maghrib = Sunset (approx 1-2 min after sunset to ensure full disk setting)
        val maghrib = sunset + (2.0 / 60.0)

        // Isha
        val isha = if (method.isIshaFixedMinutes) {
            maghrib + (method.ishaFixedMinutes / 60.0)
        } else {
            val ishaHourAngle = calculateHourAngle(-method.ishaAngle, latitude, declination)
            if (ishaHourAngle.isNaN()) {
                applyHighLatitudeRule(highLatitudeRule, sunrise, sunset, method.ishaAngle, isFajr = false)
            } else {
                solarNoon + (ishaHourAngle / 15.0)
            }
        }

        // Apply manual offsets in minutes
        val fajrOffset = (manualOffsetsMinutes["Fajr"] ?: 0) / 60.0
        val sunriseOffset = (manualOffsetsMinutes["Sunrise"] ?: 0) / 60.0
        val dhuhrOffset = (manualOffsetsMinutes["Dhuhr"] ?: 0) / 60.0
        val asrOffset = (manualOffsetsMinutes["Asr"] ?: 0) / 60.0
        val maghribOffset = (manualOffsetsMinutes["Maghrib"] ?: 0) / 60.0
        val ishaOffset = (manualOffsetsMinutes["Isha"] ?: 0) / 60.0

        return RawPrayerTimes(
            fajrHours = fajr + fajrOffset,
            sunriseHours = sunrise + sunriseOffset,
            dhuhrHours = solarNoon + (2.0 / 60.0) + dhuhrOffset, // 2 min after zenith
            asrHours = asr + asrOffset,
            maghribHours = maghrib + maghribOffset,
            ishaHours = isha + ishaOffset
        )
    }

    private fun calculateHourAngle(targetAngle: Double, latitude: Double, declination: Double): Double {
        val cosHourAngle = (sin(Math.toRadians(targetAngle)) - sin(Math.toRadians(latitude)) * sin(Math.toRadians(declination))) /
                (cos(Math.toRadians(latitude)) * cos(Math.toRadians(declination)))

        if (cosHourAngle < -1.0 || cosHourAngle > 1.0) {
            return Double.NaN
        }
        return Math.toDegrees(acos(cosHourAngle))
    }

    private fun applyHighLatitudeRule(
        rule: HighLatitudeRule,
        sunrise: Double,
        sunset: Double,
        angle: Double,
        isFajr: Boolean
    ): Double {
        val night = 24.0 - sunset + sunrise
        val factor = when (rule) {
            HighLatitudeRule.MIDDLE_OF_NIGHT -> 0.5
            HighLatitudeRule.SEVENTH_OF_NIGHT -> 1.0 / 7.0
            HighLatitudeRule.ANGLE_BASED -> angle / 60.0
        }
        val duration = night * factor
        return if (isFajr) sunrise - duration else sunset + duration
    }

    private fun calculateJulianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(a: Double): Double {
        var angle = a - 360.0 * floor(a / 360.0)
        if (angle < 0.0) angle += 360.0
        return angle
    }

    fun hoursToMillis(calendar: Calendar, hoursDecimal: Double): Long {
        val cal = calendar.clone() as Calendar
        val normalizedHours = (hoursDecimal + 24.0) % 24.0
        val h = floor(normalizedHours).toInt()
        val totalMinutes = (normalizedHours - h) * 60.0
        val m = floor(totalMinutes).toInt()
        val s = floor((totalMinutes - m) * 60.0).toInt()

        cal.set(Calendar.HOUR_OF_DAY, h)
        cal.set(Calendar.MINUTE, m)
        cal.set(Calendar.SECOND, s)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun formatTime(millis: Long, is24Hour: Boolean, timeZone: TimeZone): String {
        val cal = Calendar.getInstance(timeZone).apply { timeInMillis = millis }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        return if (is24Hour) {
            String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        } else {
            val h12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            val amPm = if (hour >= 12) "PM" else "AM"
            String.format(Locale.getDefault(), "%02d:%02d %s", h12, minute, amPm)
        }
    }

    fun calculateQiblaBearing(latitude: Double, longitude: Double): Double {
        // Kaaba coordinates
        val kaabaLat = 21.4225
        val kaabaLng = 39.8262

        val latRad = Math.toRadians(latitude)
        val lngRad = Math.toRadians(longitude)
        val kaabaLatRad = Math.toRadians(kaabaLat)
        val kaabaLngRad = Math.toRadians(kaabaLng)

        val deltaLng = kaabaLngRad - lngRad
        val y = sin(deltaLng)
        val x = cos(latRad) * tan(kaabaLatRad) - sin(latRad) * cos(deltaLng)

        val bearing = Math.toDegrees(atan(y / x))
        var qibla = bearing
        if (x < 0) qibla += 180.0
        if (qibla < 0) qibla += 360.0
        return qibla
    }
}
