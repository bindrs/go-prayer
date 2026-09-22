package com.example.domain.qibla

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * High-precision mathematical and spherical trigonometric calculator for Qibla direction,
 * distance to the Holy Kaaba, and compass headings.
 */
object QiblaCalculator {

    // Exact geographic coordinates of the Holy Kaaba in Makkah al-Mukarramah
    const val KAABA_LATITUDE = 21.422487
    const val KAABA_LONGITUDE = 39.826206

    /**
     * Calculates the great-circle forward azimuth (Qibla bearing) from the user's location
     * to the Kaaba in degrees clockwise from True North [0, 360).
     */
    fun calculateQiblaBearing(userLat: Double, userLng: Double): Float {
        val lat1 = Math.toRadians(userLat)
        val lat2 = Math.toRadians(KAABA_LATITUDE)
        val deltaLng = Math.toRadians(KAABA_LONGITUDE - userLng)

        val y = sin(deltaLng)
        val x = cos(lat1) * tan(lat2) - sin(lat1) * cos(deltaLng)
        val qiblaRad = atan2(y, x)
        val qiblaDeg = Math.toDegrees(qiblaRad)

        return ((qiblaDeg + 360.0) % 360.0).toFloat()
    }

    /**
     * Calculates the great-circle surface distance from the user's location to the Kaaba in kilometers.
     */
    fun calculateDistanceToKaabaKm(userLat: Double, userLng: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(KAABA_LATITUDE - userLat)
        val dLng = Math.toRadians(KAABA_LONGITUDE - userLng)
        val a = sin(dLat / 2.0).pow(2.0) +
                cos(Math.toRadians(userLat)) * cos(Math.toRadians(KAABA_LATITUDE)) *
                sin(dLng / 2.0).pow(2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return earthRadiusKm * c
    }

    /**
     * Calculates the angular offset between the current heading and the Qibla bearing.
     * Positive indicates Qibla is clockwise (turn right), negative indicates turn left [-180, 180].
     */
    fun getHeadingOffset(currentHeading: Float, qiblaBearing: Float): Float {
        var diff = (qiblaBearing - currentHeading) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return diff
    }

    /**
     * Determines whether the user is facing the Kaaba within an acceptable alignment threshold.
     */
    fun isFacingKaaba(currentHeading: Float, qiblaBearing: Float, toleranceDegrees: Float = 4.0f): Boolean {
        return abs(getHeadingOffset(currentHeading, qiblaBearing)) <= toleranceDegrees
    }

    /**
     * Returns the cardinal or ordinal direction string (e.g. N, NE, E, SE, S, SW, W, NW) for given degrees.
     */
    fun getCardinalDirection(degrees: Float): String {
        val normalized = ((degrees % 360f) + 360f) % 360f
        return when {
            normalized >= 337.5f || normalized < 22.5f -> "N"
            normalized >= 22.5f && normalized < 67.5f -> "NE"
            normalized >= 67.5f && normalized < 112.5f -> "E"
            normalized >= 112.5f && normalized < 157.5f -> "SE"
            normalized >= 157.5f && normalized < 202.5f -> "S"
            normalized >= 202.5f && normalized < 247.5f -> "SW"
            normalized >= 247.5f && normalized < 292.5f -> "W"
            else -> "NW"
        }
    }

    /**
     * Returns an Urdu cardinal direction description for better local comprehension.
     */
    fun getCardinalDirectionUrdu(degrees: Float): String {
        val normalized = ((degrees % 360f) + 360f) % 360f
        return when {
            normalized >= 337.5f || normalized < 22.5f -> "شمال (North)"
            normalized >= 22.5f && normalized < 67.5f -> "شمال مشرق (NE)"
            normalized >= 67.5f && normalized < 112.5f -> "مشرق (East)"
            normalized >= 112.5f && normalized < 157.5f -> "جنوب مشرق (SE)"
            normalized >= 157.5f && normalized < 202.5f -> "جنوب (South)"
            normalized >= 202.5f && normalized < 247.5f -> "جنوب مغرب (SW)"
            normalized >= 247.5f && normalized < 292.5f -> "مغرب (West)"
            else -> "شمال مغرب (NW)"
        }
    }
}
