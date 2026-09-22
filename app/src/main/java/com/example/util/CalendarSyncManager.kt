package com.example.util

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.TimeZone

/**
 * Handles automatic synchronization of offered prayers with Android's system Calendar.
 * Automatically inserts an event into the user's primary/active calendar whenever a prayer is offered,
 * removing the need for manual calendar entry.
 */
object CalendarSyncManager {
    private const val TAG = "CalendarSyncManager"

    fun hasCalendarPermission(context: Context): Boolean {
        val writeGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        val readGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        return writeGranted && readGranted
    }

    /**
     * Automatically adds a prayer record event into the device's native calendar.
     */
    fun autoAddPrayerToCalendar(
        context: Context,
        prayerName: String,
        offeredAtMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (!hasCalendarPermission(context)) {
            Log.w(TAG, "Cannot auto-add to calendar: READ/WRITE_CALENDAR permission not granted yet")
            return false
        }

        return try {
            val contentResolver = context.contentResolver

            // 1. Locate primary or first available writable calendar
            var targetCalId: Long? = null
            val projection = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.IS_PRIMARY,
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL
            )

            val cursor = contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                null
            )

            cursor?.use {
                val idIndex = it.getColumnIndex(CalendarContract.Calendars._ID)
                val primaryIndex = it.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)
                val accessIndex = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL)

                while (it.moveToNext()) {
                    val id = if (idIndex != -1) it.getLong(idIndex) else 1L
                    val isPrimary = if (primaryIndex != -1) it.getInt(primaryIndex) == 1 else false
                    val access = if (accessIndex != -1) it.getInt(accessIndex) else CalendarContract.Calendars.CAL_ACCESS_OWNER

                    if (access >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR) {
                        targetCalId = id
                        if (isPrimary) break
                    }
                }
            }

            // Fallback to calendar ID 1 if none found
            val finalCalId = targetCalId ?: 1L
            val endTimeMillis = offeredAtMillis + (15 * 60 * 1000L) // 15-minute Salah window
            val timeZone = TimeZone.getDefault().id

            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, offeredAtMillis)
                put(CalendarContract.Events.DTEND, endTimeMillis)
                put(CalendarContract.Events.TITLE, "🕌 $prayerName Prayer Offered")
                put(CalendarContract.Events.DESCRIPTION, "Salah recorded with AL-SUJOOD Go for Salah. May Allah accept.")
                put(CalendarContract.Events.CALENDAR_ID, finalCalId)
                put(CalendarContract.Events.EVENT_TIMEZONE, timeZone)
                put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED)
                put(CalendarContract.Events.HAS_ALARM, 0)
            }

            val uri: Uri? = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val success = uri != null
            if (success) {
                Log.d(TAG, "Successfully auto-added $prayerName prayer event to calendar: $uri")
            } else {
                Log.w(TAG, "Calendar provider returned null URI for $prayerName event insertion")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error auto-adding prayer to calendar: ${e.message}", e)
            false
        }
    }
}
