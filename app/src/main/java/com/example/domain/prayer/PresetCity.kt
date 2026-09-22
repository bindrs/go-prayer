package com.example.domain.prayer

data class PresetCity(
    val city: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String
)

val POPULAR_CITIES = listOf(
    PresetCity("Makkah", "Saudi Arabia", 21.4225, 39.8262, "Asia/Riyadh"),
    PresetCity("Madinah", "Saudi Arabia", 24.5247, 39.5692, "Asia/Riyadh"),
    PresetCity("Jerusalem", "Palestine", 31.7767, 35.2345, "Asia/Jerusalem"),
    PresetCity("Islamabad", "Pakistan", 33.6844, 73.0479, "Asia/Karachi"),
    PresetCity("Lahore", "Pakistan", 31.5204, 74.3587, "Asia/Karachi"),
    PresetCity("Karachi", "Pakistan", 24.8607, 67.0011, "Asia/Karachi"),
    PresetCity("Rawalpindi", "Pakistan", 33.5651, 73.0169, "Asia/Karachi"),
    PresetCity("Faisalabad", "Pakistan", 31.4504, 73.1350, "Asia/Karachi"),
    PresetCity("Peshawar", "Pakistan", 34.0151, 71.5249, "Asia/Karachi"),
    PresetCity("Cairo", "Egypt", 30.0444, 31.2357, "Africa/Cairo"),
    PresetCity("Istanbul", "Turkey", 41.0082, 28.9784, "Europe/Istanbul"),
    PresetCity("Dubai", "United Arab Emirates", 25.2048, 55.2708, "Asia/Dubai"),
    PresetCity("Riyadh", "Saudi Arabia", 24.7136, 46.6753, "Asia/Riyadh"),
    PresetCity("Doha", "Qatar", 25.2854, 51.5310, "Asia/Qatar"),
    PresetCity("Kuwait City", "Kuwait", 29.3759, 47.9774, "Asia/Kuwait"),
    PresetCity("Dhaka", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka"),
    PresetCity("Delhi", "India", 28.6139, 77.2090, "Asia/Kolkata"),
    PresetCity("Mumbai", "India", 19.0760, 72.8777, "Asia/Kolkata"),
    PresetCity("Kuala Lumpur", "Malaysia", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
    PresetCity("Jakarta", "Indonesia", -6.2088, 106.8456, "Asia/Jakarta"),
    PresetCity("London", "United Kingdom", 51.5074, -0.1278, "Europe/London"),
    PresetCity("Paris", "France", 48.8566, 2.3522, "Europe/Paris"),
    PresetCity("Berlin", "Germany", 52.5200, 13.4050, "Europe/Berlin"),
    PresetCity("New York", "United States", 40.7128, -74.0060, "America/New_York"),
    PresetCity("Chicago", "United States", 41.8781, -87.6298, "America/Chicago"),
    PresetCity("Los Angeles", "United States", 34.0522, -118.2437, "America/Los_Angeles"),
    PresetCity("Toronto", "Canada", 43.6532, -79.3832, "America/Toronto"),
    PresetCity("Sydney", "Australia", -33.8688, 151.2093, "Australia/Sydney")
)

fun findNearestCity(lat: Double, lng: Double): PresetCity {
    return POPULAR_CITIES.minByOrNull { city ->
        val dLat = Math.toRadians(city.latitude - lat)
        val dLng = Math.toRadians(city.longitude - lng)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(city.latitude)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        c
    } ?: POPULAR_CITIES.first()
}
