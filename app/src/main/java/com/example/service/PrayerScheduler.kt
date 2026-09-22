package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.repository.PrayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class PrayerScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val database = AppDatabase.getDatabase(context)
    private val repository = PrayerRepository(database)

    companion object {
        private const val TAG = "PrayerScheduler"
        const val ACTION_PRAYER_ALARM = "com.example.goprayer.ACTION_PRAYER_ALARM"
        const val ACTION_AZAN_ALARM = "com.example.goprayer.ACTION_AZAN_ALARM"
        const val ACTION_JAMAAT_ALARM = "com.example.goprayer.ACTION_JAMAAT_ALARM"
        const val ACTION_PRAYER_REMINDER = "com.example.goprayer.ACTION_PRAYER_REMINDER"
        const val ACTION_ENTER_SALAH_NOTIFICATION = "com.example.goprayer.ACTION_ENTER_SALAH_NOTIFICATION"
        const val ACTION_SALAH_WARNING_5MIN = "com.example.goprayer.ACTION_SALAH_WARNING_5MIN"
        const val ACTION_GOING_TO_PRAY = "com.example.goprayer.ACTION_GOING_TO_PRAY"

        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_PRAYER_TIME_MILLIS = "extra_prayer_time_millis"
        const val EXTRA_ADHAN_ENABLED = "extra_adhan_enabled"
        const val EXTRA_QUIET_MODE_ENABLED = "extra_quiet_mode_enabled"
        const val EXTRA_QUIET_MODE_DURATION = "extra_quiet_mode_duration"
        const val EXTRA_AUDIO_TRACK_ID = "extra_audio_track_id"
        const val EXTRA_REMINDER_MINUTES_BEFORE = "extra_reminder_minutes_before"
        const val EXTRA_WARNING_COUNT = "extra_warning_count"
        const val EXTRA_AZAN_TIME_MILLIS = "extra_azan_time_millis"
        const val EXTRA_JAMAT_TIME_MILLIS = "extra_jamat_time_millis"

        private val PRAYER_REQUEST_CODES = mapOf(
            "Fajr" to 101,
            "Sunrise" to 102,
            "Dhuhr" to 103,
            "Asr" to 104,
            "Maghrib" to 105,
            "Isha" to 106
        )

        private val JAMAAT_REQUEST_CODES = mapOf(
            "Fajr" to 401,
            "Sunrise" to 402,
            "Dhuhr" to 403,
            "Asr" to 404,
            "Maghrib" to 405,
            "Isha" to 406
        )

        private val ENTER_SALAH_REQUEST_CODES = mapOf(
            "Fajr" to 151,
            "Sunrise" to 152,
            "Dhuhr" to 153,
            "Asr" to 154,
            "Maghrib" to 155,
            "Isha" to 156
        )

        private val REMINDER_REQUEST_CODES = mapOf(
            "Fajr" to 201,
            "Sunrise" to 202,
            "Dhuhr" to 203,
            "Asr" to 204,
            "Maghrib" to 205,
            "Isha" to 206
        )

        private val WARNING_REQUEST_CODES = mapOf(
            "Fajr" to 301,
            "Sunrise" to 302,
            "Dhuhr" to 303,
            "Asr" to 304,
            "Maghrib" to 305,
            "Isha" to 306
        )
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Schedules the next prayer alarms asynchronously, including:
     * 1. Enter Salah Time Notification (automatic according to location)
     * 2. Azan Time Notification & Audio (manually adjusted in settings)
     * 3. Pre-prayer reminders (15 mins before)
     */
    fun scheduleUpcomingPrayers(onComplete: ((scheduledCount: Int) -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = repository.getSettings()
                val location = repository.getLocation()
                val configs = repository.getPrayerConfigurations()
                val configMap = configs.associateBy { it.prayerName }

                val calendar = Calendar.getInstance()
                val now = System.currentTimeMillis()

                // Calculate today and tomorrow
                val todaySchedule = repository.calculateDaySchedule(calendar, location, settings, configs)

                val tomorrowCal = (calendar.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
                val tomorrowSchedule = repository.calculateDaySchedule(tomorrowCal, location, settings, configs)

                val allCandidates = todaySchedule.prayers + tomorrowSchedule.prayers
                val upcomingPrayers = allCandidates.filter { it.jamatTimeMillis > now || it.azanTimeMillis > now }

                var scheduled = 0
                for (prayer in upcomingPrayers) {
                    val config = configMap[prayer.name]
                    val adhanEnabled = config?.adhanEnabled ?: true
                    val quietModeEnabled = config?.quietModeEnabled ?: true
                    val duration = config?.quietModeDurationMinutes ?: 20
                    val trackId = config?.audioTrackId ?: "makkah_adhan"
                    val enterSalahNotificationEnabled = config?.enterSalahNotificationEnabled ?: true

                    // 1. Schedule Pre-Prayer Reminder (15 mins before Enter Salah if time allows)
                    val reminderTime = prayer.timestampMillis - 15 * 60 * 1000L
                    if (reminderTime > now) {
                        scheduleSingleReminder(
                            prayerName = prayer.name,
                            triggerAtMillis = reminderTime,
                            prayerTimeMillis = prayer.timestampMillis,
                            minutesBefore = 15
                        )
                    }

                    // 2. Schedule Enter Salah Time Notification (Automatic according to location)
                    if (enterSalahNotificationEnabled && prayer.timestampMillis > now) {
                        scheduleEnterSalahNotification(
                            prayerName = prayer.name,
                            triggerAtMillis = prayer.timestampMillis,
                            azanTimeMillis = prayer.azanTimeMillis,
                            jamatTimeMillis = prayer.jamatTimeMillis
                        )
                    }

                    // 3. Schedule Azan Alarm & App Focus Lock (Adhan & App Blocker at Azan Time)
                    if (prayer.azanTimeMillis > now && prayer.name != "Sunrise") {
                        scheduleSingleAlarm(
                            prayerName = prayer.name,
                            triggerAtMillis = prayer.azanTimeMillis,
                            adhanEnabled = adhanEnabled,
                            quietModeEnabled = quietModeEnabled,
                            quietModeDurationMinutes = duration,
                            audioTrackId = trackId,
                            jamatTimeMillis = prayer.jamatTimeMillis
                        )
                        scheduled++
                    }

                    // 4. Schedule Jama'at Alarm (App Blocker Lock & Quiet Mode at Jama'at Time)
                    if (prayer.jamatTimeMillis > now && prayer.name != "Sunrise") {
                        scheduleJamaatAlarm(
                            prayerName = prayer.name,
                            triggerAtMillis = prayer.jamatTimeMillis,
                            quietModeEnabled = quietModeEnabled,
                            quietModeDurationMinutes = duration
                        )
                        scheduled++
                    }
                }

                Log.d(TAG, "Scheduled $scheduled upcoming prayer alarms, enter salah notices, and reminders")
                onComplete?.invoke(scheduled)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule prayers: ${e.message}", e)
                onComplete?.invoke(0)
            }
        }
    }

    private fun scheduleEnterSalahNotification(
        prayerName: String,
        triggerAtMillis: Long,
        azanTimeMillis: Long,
        jamatTimeMillis: Long
    ) {
        val requestCode = ENTER_SALAH_REQUEST_CODES[prayerName] ?: 150
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_ENTER_SALAH_NOTIFICATION
            putExtra(EXTRA_PRAYER_NAME, prayerName)
            putExtra(EXTRA_PRAYER_TIME_MILLIS, triggerAtMillis)
            putExtra(EXTRA_AZAN_TIME_MILLIS, azanTimeMillis)
            putExtra(EXTRA_JAMAT_TIME_MILLIS, jamatTimeMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            Log.d(TAG, "Enter Salah Notification set for $prayerName at $triggerAtMillis")
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException for enter salah notification: ${e.message}")
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun scheduleSingleReminder(
        prayerName: String,
        triggerAtMillis: Long,
        prayerTimeMillis: Long,
        minutesBefore: Int
    ) {
        val requestCode = REMINDER_REQUEST_CODES[prayerName] ?: 200
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_REMINDER
            putExtra(EXTRA_PRAYER_NAME, prayerName)
            putExtra(EXTRA_PRAYER_TIME_MILLIS, prayerTimeMillis)
            putExtra(EXTRA_REMINDER_MINUTES_BEFORE, minutesBefore)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            Log.d(TAG, "Reminder set for $prayerName $minutesBefore min before at $triggerAtMillis")
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException for reminder alarm: ${e.message}")
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun scheduleSingleAlarm(
        prayerName: String,
        triggerAtMillis: Long,
        adhanEnabled: Boolean,
        quietModeEnabled: Boolean = true,
        quietModeDurationMinutes: Int = 20,
        audioTrackId: String,
        jamatTimeMillis: Long = triggerAtMillis + (20 * 60 * 1000L)
    ) {
        val requestCode = PRAYER_REQUEST_CODES[prayerName] ?: 100
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_AZAN_ALARM
            putExtra(EXTRA_PRAYER_NAME, prayerName)
            putExtra(EXTRA_PRAYER_TIME_MILLIS, triggerAtMillis)
            putExtra(EXTRA_ADHAN_ENABLED, adhanEnabled)
            putExtra(EXTRA_QUIET_MODE_ENABLED, quietModeEnabled)
            putExtra(EXTRA_QUIET_MODE_DURATION, quietModeDurationMinutes)
            putExtra(EXTRA_AUDIO_TRACK_ID, audioTrackId)
            putExtra(EXTRA_JAMAT_TIME_MILLIS, jamatTimeMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            Log.d(TAG, "Azan alarm set for $prayerName at $triggerAtMillis")
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission restricted; falling back to inexact alarm: ${e.message}")
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun scheduleJamaatAlarm(
        prayerName: String,
        triggerAtMillis: Long,
        quietModeEnabled: Boolean,
        quietModeDurationMinutes: Int
    ) {
        val requestCode = JAMAAT_REQUEST_CODES[prayerName] ?: 400
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_JAMAAT_ALARM
            putExtra(EXTRA_PRAYER_NAME, prayerName)
            putExtra(EXTRA_PRAYER_TIME_MILLIS, triggerAtMillis)
            putExtra(EXTRA_QUIET_MODE_ENABLED, quietModeEnabled)
            putExtra(EXTRA_QUIET_MODE_DURATION, quietModeDurationMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            Log.d(TAG, "Jama'at alarm set for $prayerName at $triggerAtMillis")
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException for Jama'at alarm: ${e.message}")
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    /**
     * Schedules the 5-minute recurring warning if user hasn't tapped 'I am going to offer pray'
     */
    fun schedule5MinuteWarning(prayerName: String, warningCount: Int = 1, delayMillis: Long = 5 * 60 * 1000L) {
        val requestCode = WARNING_REQUEST_CODES[prayerName] ?: 300
        val triggerAtMillis = System.currentTimeMillis() + delayMillis

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_SALAH_WARNING_5MIN
            putExtra(EXTRA_PRAYER_NAME, prayerName)
            putExtra(EXTRA_WARNING_COUNT, warningCount)
            putExtra(EXTRA_PRAYER_TIME_MILLIS, triggerAtMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            Log.d(TAG, "5-minute warning #$warningCount scheduled for $prayerName in ${delayMillis / 1000}s")
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException scheduling 5-min warning: ${e.message}")
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    /**
     * Cancels the 5-minute recurring warning when the user taps 'I am going to offer pray' or marks prayer offered
     */
    fun cancel5MinuteWarning(prayerName: String) {
        val requestCode = WARNING_REQUEST_CODES[prayerName] ?: 300
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_SALAH_WARNING_5MIN
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled 5-minute recurring warning for $prayerName")
        }
    }

    fun cancelAllAlarms() {
        for ((prayerName, requestCode) in PRAYER_REQUEST_CODES) {
            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = ACTION_AZAN_ALARM
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Cancelled azan alarm for $prayerName")
            }
        }
        for ((prayerName, requestCode) in JAMAAT_REQUEST_CODES) {
            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = ACTION_JAMAAT_ALARM
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Cancelled jamaat alarm for $prayerName")
            }
        }
        for ((prayerName, _) in WARNING_REQUEST_CODES) {
            cancel5MinuteWarning(prayerName)
        }
    }
}
