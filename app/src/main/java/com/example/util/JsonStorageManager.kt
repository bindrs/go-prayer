package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.local.entities.PrayerEventLog
import com.example.data.local.entities.QuranStreakRecord
import com.example.data.local.entities.SalahRecord
import com.example.data.local.entities.UserSettings
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class JsonStorageInfo(
    val internalPath: String,
    val externalPath: String?,
    val fileSizeFormatted: String,
    val totalRecordsStored: Int,
    val totalOfferedStored: Int,
    val lastUpdatedText: String,
    val exists: Boolean
)

data class ParsedBackupData(
    val salahRecords: List<SalahRecord> = emptyList(),
    val quranStreaks: List<QuranStreakRecord> = emptyList(),
    val settings: UserSettings? = null,
    val eventLogs: List<PrayerEventLog> = emptyList()
)

/**
 * Persists app data in clean, standard JSON format on the device.
 * Stores Salah records, Quran streaks, event logs, and settings to both
 * app-internal storage and device external-files directory.
 */
object JsonStorageManager {
    private const val TAG = "JsonStorageManager"
    private const val FILE_NAME = "salah_prayer_data.json"

    fun getInternalFile(context: Context): File {
        return File(context.filesDir, FILE_NAME)
    }

    fun getExternalFile(context: Context): File? {
        return try {
            val extDir = context.getExternalFilesDir(null)
            if (extDir != null) File(extDir, FILE_NAME) else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Restores data from on-device JSON backup file (internal or external).
     */
    fun restoreDataFromStorage(context: Context): ParsedBackupData? {
        return try {
            val internalFile = getInternalFile(context)
            val externalFile = getExternalFile(context)
            val fileToRead = when {
                internalFile.exists() && internalFile.length() > 0 -> internalFile
                externalFile != null && externalFile.exists() && externalFile.length() > 0 -> externalFile
                else -> null
            }

            if (fileToRead == null) return null

            val jsonContent = fileToRead.readText(Charsets.UTF_8)
            val root = JSONObject(jsonContent)

            // Parse Salah Records
            val salahList = mutableListOf<SalahRecord>()
            val salahArray = root.optJSONArray("salah_records")
            if (salahArray != null) {
                for (i in 0 until salahArray.length()) {
                    val obj = salahArray.optJSONObject(i) ?: continue
                    val date = obj.optString("date", "")
                    val prayerName = obj.optString("prayer_name", "")
                    if (date.isNotBlank() && prayerName.isNotBlank()) {
                        val year = obj.optInt("year", 2026)
                        val month = obj.optInt("month", 9)
                        val day = obj.optInt("day", 1)
                        val monthYear = obj.optString("month_year", String.format(Locale.US, "%04d-%02d", year, month))
                        val isOffered = obj.optBoolean("is_offered", false)
                        val offeredMillis = if (obj.has("offered_at_millis")) obj.optLong("offered_at_millis") else null
                        val offeringType = obj.optString("offering_type", if (isOffered) "ON_TIME" else "MISSED")
                        val notes = obj.optString("notes", null)

                        salahList.add(
                            SalahRecord(
                                id = obj.optLong("id", 0L),
                                date = date,
                                monthYear = monthYear,
                                year = year,
                                month = month,
                                day = day,
                                prayerName = prayerName,
                                isOffered = isOffered,
                                offeredAtMillis = if (offeredMillis != null && offeredMillis > 0) offeredMillis else null,
                                offeringType = offeringType,
                                notes = if (!notes.isNullOrBlank()) notes else null
                            )
                        )
                    }
                }
            }

            // Parse Quran Streaks
            val quranList = mutableListOf<QuranStreakRecord>()
            val quranArray = root.optJSONArray("quran_streaks")
            if (quranArray != null) {
                for (i in 0 until quranArray.length()) {
                    val obj = quranArray.optJSONObject(i) ?: continue
                    val date = obj.optString("date", "")
                    if (date.isNotBlank()) {
                        val monthYear = obj.optString("month_year", date.take(7))
                        val versesRead = obj.optInt("verses_read", 10)
                        val targetLines = obj.optInt("target_lines", 10)
                        val isCompleted = obj.optBoolean("is_completed", true)
                        val completedAt = obj.optLong("completed_at", System.currentTimeMillis())
                        val surahName = obj.optString("surah_name", "Al-Fatiha")
                        val startAyah = obj.optInt("start_ayah", 1)
                        val endAyah = obj.optInt("end_ayah", 7)

                        quranList.add(
                            QuranStreakRecord(
                                date = date,
                                monthYear = monthYear,
                                versesRead = versesRead,
                                targetLines = targetLines,
                                isCompleted = isCompleted,
                                completedAtMillis = completedAt,
                                surahName = surahName,
                                startAyah = startAyah,
                                endAyah = endAyah
                            )
                        )
                    }
                }
            }

            // Parse User Settings
            var userSettings: UserSettings? = null
            val settingsObj = root.optJSONObject("user_settings")
            if (settingsObj != null) {
                userSettings = UserSettings(
                    id = 1,
                    language = settingsObj.optString("language", "en"),
                    theme = settingsObj.optString("theme", "system"),
                    calculationMethod = settingsObj.optString("calculation_method", "MWL"),
                    madhhab = settingsObj.optString("madhhab", "STANDARD"),
                    highLatitudeRule = settingsObj.optString("high_latitude_rule", "MIDDLE_OF_NIGHT"),
                    timeFormat = settingsObj.optString("time_format", "12H"),
                    quietModeEnabled = settingsObj.optBoolean("quiet_mode_enabled", true),
                    quietModeDurationMinutes = settingsObj.optInt("quiet_mode_duration", 20),
                    totalDisableOnPrayer = settingsObj.optBoolean("total_disable_on_prayer", true),
                    vpnFahishaAlertEnabled = settingsObj.optBoolean("vpn_fahisha_alert_enabled", true),
                    quranDailyTargetLines = settingsObj.optInt("quran_daily_target_lines", 10),
                    userName = settingsObj.optString("user_name", ""),
                    spiritualIdentity = settingsObj.optString("spiritual_identity", "بندۂ خدا (Servant of Allah)"),
                    favoriteLoveAyahId = settingsObj.optString("favorite_love_ayah_id", "az_zumar_53")
                )
            }

            Log.d(TAG, "Restored backup with ${salahList.size} Salah records and ${quranList.size} Quran streaks from ${fileToRead.name}")
            ParsedBackupData(
                salahRecords = salahList,
                quranStreaks = quranList,
                settings = userSettings
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring data from JSON backup: ${e.message}", e)
            null
        }
    }

    /**
     * Saves complete Salah, Quran, and settings data into formatted JSON on device.
     */
    fun saveAllDataToJson(
        context: Context,
        salahRecords: List<SalahRecord>,
        quranStreaks: List<QuranStreakRecord> = emptyList(),
        settings: UserSettings? = null,
        eventLogs: List<PrayerEventLog> = emptyList()
    ): Boolean {
        return try {
            val root = JSONObject()
            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            root.put("app_name", "AL-SUJOOD")
            root.put("version", "1.0")
            root.put("android_target", "Android 10 (API 29)+")
            root.put("export_format", "JSON")
            root.put("last_saved_timestamp", now)
            root.put("last_saved_formatted", dateFormat.format(Date(now)))

            // Salah Records Array
            val recordsArray = JSONArray()
            var offeredCount = 0
            for (rec in salahRecords) {
                if (rec.isOffered) offeredCount++
                val obj = JSONObject().apply {
                    put("id", rec.id)
                    put("date", rec.date)
                    put("month_year", rec.monthYear)
                    put("year", rec.year)
                    put("month", rec.month)
                    put("day", rec.day)
                    put("prayer_name", rec.prayerName)
                    put("is_offered", rec.isOffered)
                    put("offered_at_millis", rec.offeredAtMillis ?: 0L)
                    put("offering_type", rec.offeringType)
                    put("notes", rec.notes ?: "")
                }
                recordsArray.put(obj)
            }
            root.put("total_salah_records", salahRecords.size)
            root.put("total_offered_count", offeredCount)
            root.put("salah_records", recordsArray)

            // Quran Streaks Array
            val quranArray = JSONArray()
            for (streak in quranStreaks) {
                val qObj = JSONObject().apply {
                    put("date", streak.date)
                    put("month_year", streak.monthYear)
                    put("verses_read", streak.versesRead)
                    put("target_lines", streak.targetLines)
                    put("is_completed", streak.isCompleted)
                    put("surah_name", streak.surahName)
                    put("start_ayah", streak.startAyah)
                    put("end_ayah", streak.endAyah)
                    put("completed_at", streak.completedAtMillis)
                }
                quranArray.put(qObj)
            }
            root.put("quran_streaks", quranArray)

            // User Settings Object
            if (settings != null) {
                val settingsObj = JSONObject().apply {
                    put("total_disable_on_prayer", settings.totalDisableOnPrayer)
                    put("calculation_method", settings.calculationMethod)
                    put("madhhab", settings.madhhab)
                    put("high_latitude_rule", settings.highLatitudeRule)
                    put("time_format", settings.timeFormat)
                    put("quiet_mode_enabled", settings.quietModeEnabled)
                    put("quiet_mode_duration", settings.quietModeDurationMinutes)
                    put("vpn_fahisha_alert_enabled", settings.vpnFahishaAlertEnabled)
                    put("theme", settings.theme)
                    put("language", settings.language)
                    put("quran_daily_target_lines", settings.quranDailyTargetLines)
                    put("user_name", settings.userName)
                    put("spiritual_identity", settings.spiritualIdentity)
                    put("favorite_love_ayah_id", settings.favoriteLoveAyahId)
                }
                root.put("user_settings", settingsObj)
            }

            // Event Logs Array
            val logsArray = JSONArray()
            for (log in eventLogs.take(50)) {
                val lObj = JSONObject().apply {
                    put("id", log.id)
                    put("prayer_name", log.prayerName)
                    put("triggered_at", log.triggeredAt)
                    put("adhan_status", log.adhanStatus)
                    put("quiet_mode_status", log.quietModeStatus)
                    put("screen_status", log.screenStatus)
                    put("error_message", log.errorMessage ?: "")
                }
                logsArray.put(lObj)
            }
            root.put("recent_event_logs", logsArray)

            // Write 2-space indented pretty JSON
            val jsonString = root.toString(2)

            // 1. Write to internal filesDir
            val internalFile = getInternalFile(context)
            FileOutputStream(internalFile).use { fos ->
                fos.write(jsonString.toByteArray(Charsets.UTF_8))
                fos.flush()
            }

            // 2. Mirror to external filesDir if available
            val extFile = getExternalFile(context)
            if (extFile != null) {
                FileOutputStream(extFile).use { fos ->
                    fos.write(jsonString.toByteArray(Charsets.UTF_8))
                    fos.flush()
                }
            }

            Log.d(TAG, "Successfully saved ${salahRecords.size} Salah records in JSON to: ${internalFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write JSON data to device: ${e.message}", e)
            false
        }
    }

    /**
     * Reads current raw JSON string from device storage.
     */
    fun readJsonContent(context: Context): String {
        return try {
            val file = getInternalFile(context)
            if (file.exists()) {
                file.readText(Charsets.UTF_8)
            } else {
                "{\n  \"status\": \"No JSON data file initialized yet\"\n}"
            }
        } catch (e: Exception) {
            "{\n  \"error\": \"${e.message}\"\n}"
        }
    }

    /**
     * Returns metadata information about the on-device JSON file.
     */
    fun getStorageInfo(context: Context): JsonStorageInfo {
        val file = getInternalFile(context)
        val extFile = getExternalFile(context)
        val exists = file.exists()

        var recordCount = 0
        var offeredCount = 0
        var lastUpdated = "Never"

        if (exists) {
            val size = file.length()
            val sizeFormatted = when {
                size > 1024 * 1024 -> String.format(Locale.US, "%.1f MB", size / (1024.0 * 1024.0))
                size > 1024 -> String.format(Locale.US, "%.1f KB", size / 1024.0)
                else -> "$size Bytes"
            }
            try {
                val json = JSONObject(file.readText(Charsets.UTF_8))
                recordCount = json.optInt("total_salah_records", 0)
                offeredCount = json.optInt("total_offered_count", 0)
                lastUpdated = json.optString("last_saved_formatted", "Recently")
            } catch (_: Exception) {}

            return JsonStorageInfo(
                internalPath = file.absolutePath,
                externalPath = extFile?.absolutePath,
                fileSizeFormatted = sizeFormatted,
                totalRecordsStored = recordCount,
                totalOfferedStored = offeredCount,
                lastUpdatedText = lastUpdated,
                exists = true
            )
        } else {
            return JsonStorageInfo(
                internalPath = file.absolutePath,
                externalPath = extFile?.absolutePath,
                fileSizeFormatted = "0 Bytes",
                totalRecordsStored = 0,
                totalOfferedStored = 0,
                lastUpdatedText = "Pending initial sync",
                exists = false
            )
        }
    }
}
