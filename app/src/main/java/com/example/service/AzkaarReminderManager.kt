package com.example.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

object AzkaarReminderManager {

    private const val TAG = "AzkaarReminderManager"
    const val PREFS_NAME = "go_prayer_azkaar_prefs"

    const val AZKAAR_CHANNEL_ID = "go_prayer_azkaar_reminder_channel_v1"
    const val ACTION_AZKAAR_REMINDER = "com.example.goprayer.ACTION_AZKAAR_REMINDER"
    const val ACTION_MARK_AZKAAR_DONE = "com.example.goprayer.ACTION_MARK_AZKAAR_DONE"

    const val EXTRA_AZKAAR_TYPE = "extra_azkaar_type"

    const val TYPE_MORNING = "morning"
    const val TYPE_EVENING = "evening"
    const val TYPE_POST_SALAH = "post_salah"
    const val TYPE_NIGHT = "night"

    const val REQUEST_CODE_MORNING = 7001
    const val REQUEST_CODE_EVENING = 7002
    const val REQUEST_CODE_NIGHT = 7003

    const val NOTIFICATION_ID_BASE = 9500

    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isAzkaarRemindersGloballyEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean("azkaar_reminders_enabled", true)
    }

    fun setAzkaarRemindersGloballyEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("azkaar_reminders_enabled", enabled).apply()
        if (enabled) {
            scheduleAllAzkaarReminders(context)
        } else {
            cancelAllReminders(context)
        }
    }

    fun isReminderTypeEnabled(context: Context, type: String): Boolean {
        if (!isAzkaarRemindersGloballyEnabled(context)) return false
        val key = when (type) {
            TYPE_MORNING -> "morning_reminder_enabled"
            TYPE_EVENING -> "evening_reminder_enabled"
            TYPE_POST_SALAH -> "post_salah_reminder_enabled"
            TYPE_NIGHT -> "night_reminder_enabled"
            else -> "azkaar_reminders_enabled"
        }
        return getPrefs(context).getBoolean(key, true)
    }

    fun setReminderTypeEnabled(context: Context, type: String, enabled: Boolean) {
        val key = when (type) {
            TYPE_MORNING -> "morning_reminder_enabled"
            TYPE_EVENING -> "evening_reminder_enabled"
            TYPE_POST_SALAH -> "post_salah_reminder_enabled"
            TYPE_NIGHT -> "night_reminder_enabled"
            else -> "azkaar_reminders_enabled"
        }
        getPrefs(context).edit().putBoolean(key, enabled).apply()
        scheduleAllAzkaarReminders(context)
    }

    fun getReminderTime(context: Context, type: String): Pair<Int, Int> {
        val defaultTime = when (type) {
            TYPE_MORNING -> "06:30"
            TYPE_EVENING -> "17:30"
            TYPE_NIGHT -> "22:00"
            else -> "08:00"
        }
        val key = "${type}_reminder_time"
        val timeStr = getPrefs(context).getString(key, defaultTime) ?: defaultTime
        return try {
            val parts = timeStr.split(":")
            Pair(parts[0].toInt(), parts[1].toInt())
        } catch (_: Exception) {
            Pair(6, 30)
        }
    }

    fun setReminderTime(context: Context, type: String, hour: Int, minute: Int) {
        val key = "${type}_reminder_time"
        val timeStr = String.format("%02d:%02d", hour, minute)
        getPrefs(context).edit().putString(key, timeStr).apply()
        scheduleAllAzkaarReminders(context)
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            val channel = NotificationChannel(
                AZKAAR_CHANNEL_ID,
                "Daily Azkaar & Tasbeeh Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily morning, evening, post-Salah and bedtime Azkaar remembrances"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 400)
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#155563")
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleAllAzkaarReminders(context: Context) {
        val prefs = getPrefs(context)
        val globalEnabled = prefs.getBoolean("azkaar_reminders_enabled", true)
        if (!globalEnabled) {
            cancelAllReminders(context)
            return
        }

        createNotificationChannel(context)

        // 1. Morning Azkaar
        if (prefs.getBoolean("morning_reminder_enabled", true)) {
            val (h, m) = getReminderTime(context, TYPE_MORNING)
            scheduleDailyAlarm(context, TYPE_MORNING, h, m, REQUEST_CODE_MORNING)
        } else {
            cancelAlarm(context, REQUEST_CODE_MORNING)
        }

        // 2. Evening Azkaar
        if (prefs.getBoolean("evening_reminder_enabled", true)) {
            val (h, m) = getReminderTime(context, TYPE_EVENING)
            scheduleDailyAlarm(context, TYPE_EVENING, h, m, REQUEST_CODE_EVENING)
        } else {
            cancelAlarm(context, REQUEST_CODE_EVENING)
        }

        // 3. Night Azkaar
        if (prefs.getBoolean("night_reminder_enabled", true)) {
            val (h, m) = getReminderTime(context, TYPE_NIGHT)
            scheduleDailyAlarm(context, TYPE_NIGHT, h, m, REQUEST_CODE_NIGHT)
        } else {
            cancelAlarm(context, REQUEST_CODE_NIGHT)
        }

        Log.d(TAG, "All daily Azkaar reminders scheduled successfully")
    }

    private fun scheduleDailyAlarm(context: Context, type: String, hour: Int, minute: Int, requestCode: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = ACTION_AZKAAR_REMINDER
                putExtra(EXTRA_AZKAAR_TYPE, type)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(now)) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val triggerTime = target.timeInMillis

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }

            Log.d(TAG, "Scheduled $type Azkaar alarm for ${target.time} (reqCode: $requestCode)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule $type Azkaar alarm: ${e.message}")
        }
    }

    fun cancelAlarm(context: Context, requestCode: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = ACTION_AZKAAR_REMINDER
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        } catch (_: Exception) {}
    }

    fun cancelAllReminders(context: Context) {
        cancelAlarm(context, REQUEST_CODE_MORNING)
        cancelAlarm(context, REQUEST_CODE_EVENING)
        cancelAlarm(context, REQUEST_CODE_NIGHT)
        Log.d(TAG, "Cancelled all Azkaar alarms")
    }

    fun showAzkaarNotification(context: Context, type: String, isTest: Boolean = false) {
        try {
            createNotificationChannel(context)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            val (tabIndex, title, subtitle, arabicAyat, translation) = when (type) {
                TYPE_MORNING -> Quadruple(
                    0,
                    "🌅 Morning Azkaar Reminder (أذكار الصباح)",
                    "Begin your day with the remembrance of Allah for protection and peace.",
                    "«أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ»",
                    "\"Unquestionably, by the remembrance of Allah hearts are assured.\" (Surah Ar-Ra'd 13:28)"
                )
                TYPE_EVENING -> Quadruple(
                    1,
                    "🌆 Evening Azkaar Reminder (أذكار المساء)",
                    "Shield your evening with authentic Sunnah Azkaar before Maghrib.",
                    "«فَسُبْحَانَ اللَّهِ حِينَ تُمْسُونَ وَحِينَ تُصْبِحُونَ»",
                    "\"So exalted is Allah when you reach the evening and when you reach the morning.\" (Surah Ar-Rum 30:17)"
                )
                TYPE_POST_SALAH -> Quadruple(
                    2,
                    "🕌 Post-Salah Sunnah Azkaar (أذكار بعد الصلاة)",
                    "Complete your Salah with Ayat al-Kursi & Tasbeeh (33x SubhanAllah, 33x Alhamdulillah, 34x Allahu Akbar).",
                    "«أَسْتَغْفِرُ اللَّهَ (3x) اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ»",
                    "\"I seek forgiveness of Allah. O Allah, You are Peace, and from You comes peace.\""
                )
                TYPE_NIGHT -> Quadruple(
                    0,
                    "🌙 Bedtime Sunnah Azkaar (أذكار النوم)",
                    "Recite Ayat al-Kursi and the Mu'awwidhat (Surah Ikhlas, Falaq, Nas) before sleep.",
                    "«بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي وَبِكَ أَرْفَعُهُ»",
                    "\"In Your name my Lord, I lie down, and in Your name I rise up.\""
                )
                else -> Quadruple(
                    0,
                    "📿 Daily Azkaar & Tasbeeh Reminder",
                    "Remember Allah with authentic morning & evening invocations.",
                    "«فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ»",
                    "\"Remember Me; I will remember you. And be grateful to Me and do not deny Me.\" (Surah Al-Baqarah 2:152)"
                )
            }

            val prefix = if (isTest) "[TEST] " else ""
            val fullTitle = "$prefix$title"

            // Intent to open Azkaar Screen with target tab
            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("route_target", "azkaar_screen")
                putExtra("azkaar_tab", tabIndex)
            }
            val openPendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID_BASE + tabIndex,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Intent to mark completed directly from notification
            val markDoneIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = ACTION_MARK_AZKAAR_DONE
                putExtra(EXTRA_AZKAAR_TYPE, type)
            }
            val markDonePendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID_BASE + 50 + tabIndex,
                markDoneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val bigText = buildString {
                append(subtitle)
                append("\n\n")
                append(arabicAyat)
                append("\n")
                append(translation)
                append("\n\nTap below to open your digital Tasbeeh & recite daily Azkaar.")
            }

            val notificationId = when (type) {
                TYPE_MORNING -> NOTIFICATION_ID_BASE + 1
                TYPE_EVENING -> NOTIFICATION_ID_BASE + 2
                TYPE_POST_SALAH -> NOTIFICATION_ID_BASE + 3
                TYPE_NIGHT -> NOTIFICATION_ID_BASE + 4
                else -> NOTIFICATION_ID_BASE + 5
            }

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            val notification = NotificationCompat.Builder(context, AZKAAR_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(fullTitle)
                .setContentText(subtitle)
                .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setVibrate(longArrayOf(0, 300, 200, 400))
                .setContentIntent(openPendingIntent)
                .addAction(android.R.drawable.ic_menu_view, "📿 Open Azkaar", openPendingIntent)
                .addAction(android.R.drawable.ic_menu_agenda, "✓ Recited", markDonePendingIntent)
                .build()

            notificationManager.notify(notificationId, notification)
            Log.d(TAG, "Sent Azkaar reminder notification for $type (id: $notificationId)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show Azkaar notification: ${e.message}", e)
        }
    }

    fun sendTestNotification(context: Context, type: String = TYPE_MORNING) {
        showAzkaarNotification(context, type, isTest = true)
    }

    private data class Quadruple(
        val tabIndex: Int,
        val title: String,
        val subtitle: String,
        val arabic: String,
        val translation: String
    )
}
