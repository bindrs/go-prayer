package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.SeedData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrayerAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PrayerAlarmReceiver"
        const val REMINDER_CHANNEL_ID = "go_prayer_reminder_channel_v3"
        const val ENTER_SALAH_CHANNEL_ID = "go_prayer_enter_salah_channel_v3"
        const val SALAH_WARNING_CHANNEL_ID = "go_prayer_warning_channel_v3"

        const val REMINDER_NOTIFICATION_BASE_ID = 8800
        const val ENTER_SALAH_NOTIFICATION_BASE_ID = 8900
        const val WARNING_NOTIFICATION_BASE_ID = 9000

        private var lastTriggeredTime = 0L
        private var lastTriggeredPrayer = ""

        fun createNotificationChannels(context: Context) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val alarmAudioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .build()

                val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

                // Reminder Channel
                val reminderChannel = NotificationChannel(
                    REMINDER_CHANNEL_ID,
                    "Prayer Time Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Pre-prayer countdown reminders and Wudu preparations"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 300, 200, 300)
                    setShowBadge(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                    setSound(notificationSound, alarmAudioAttributes)
                }
                notificationManager.createNotificationChannel(reminderChannel)

                // Enter Salah Channel
                val enterSalahChannel = NotificationChannel(
                    ENTER_SALAH_CHANNEL_ID,
                    "Enter Salah Time Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifies when Salah time officially enters according to location"
                    enableVibration(true)
                    enableLights(true)
                    lightColor = android.graphics.Color.GREEN
                    vibrationPattern = longArrayOf(0, 400, 200, 400)
                    setShowBadge(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                    setSound(alarmSound, alarmAudioAttributes)
                }
                notificationManager.createNotificationChannel(enterSalahChannel)

                // Warning Channel
                val warningChannel = NotificationChannel(
                    SALAH_WARNING_CHANNEL_ID,
                    "Salah Neglect Warning & Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Recurring 5-minute warnings and Azaab Quranic Ayat when Salah is delayed"
                    enableVibration(true)
                    enableLights(true)
                    lightColor = android.graphics.Color.RED
                    vibrationPattern = longArrayOf(0, 800, 400, 800, 400, 800)
                    setShowBadge(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                    setSound(alarmSound, alarmAudioAttributes)
                }
                notificationManager.createNotificationChannel(warningChannel)
            }
            AzkaarReminderManager.createNotificationChannel(context)
        }

        fun showEnterSalahNotificationDirect(
            context: Context,
            prayerName: String,
            azanTime: Long = 0L,
            jamatTime: Long = 0L
        ) {
            createNotificationChannels(context)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("route_target", "prayer_screen")
                putExtra("active_prayer_name", prayerName)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                ("enter_$prayerName").hashCode(),
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val azanStr = if (azanTime > 0) timeFormat.format(Date(azanTime)) else ""
            val jamatStr = if (jamatTime > 0) timeFormat.format(Date(jamatTime)) else ""

            val detailText = buildString {
                append("$prayerName Waqt has officially entered according to your location.")
                if (azanStr.isNotEmpty()) append(" • Azan: $azanStr")
                if (jamatStr.isNotEmpty()) append(" • Jama'at: $jamatStr")
                append("\n\n\"حَافِظُوا عَلَى الصَّلَوَاتِ وَالصَّلَاةِ الْوُسْطَىٰ\"\n\"Maintain with care the [obligatory] prayers.\" (Surah Al-Baqarah 2:238)")
            }

            val alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notification = NotificationCompat.Builder(context, ENTER_SALAH_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("🕌 $prayerName Time Entered (داخل وقت)")
                .setContentText("$prayerName prayer time has officially started. Hasten to prayer!")
                .setStyle(NotificationCompat.BigTextStyle().bigText(detailText))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setSound(alertSound, AudioManager.STREAM_ALARM)
                .setVibrate(longArrayOf(0, 400, 200, 400))
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_view, "Open Go for Salah", pendingIntent)
                .build()

            val notificationId = ENTER_SALAH_NOTIFICATION_BASE_ID + (prayerName.hashCode() % 100)
            notificationManager.notify(notificationId, notification)
            Log.d(TAG, "Displayed Enter Salah notification for $prayerName")
        }

        fun showSalahWarningNotificationDirect(
            context: Context,
            prayerName: String,
            warningCount: Int = 1
        ) {
            createNotificationChannels(context)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val goingToPrayIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = PrayerScheduler.ACTION_GOING_TO_PRAY
                putExtra(PrayerScheduler.EXTRA_PRAYER_NAME, prayerName)
            }
            val goingToPrayPendingIntent = PendingIntent.getBroadcast(
                context,
                ("going_pray_$prayerName").hashCode(),
                goingToPrayIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("route_target", "prayer_screen")
                putExtra("active_prayer_name", prayerName)
                putExtra("warning_active", true)
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                ("warn_open_$prayerName").hashCode(),
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val warningAyat = SeedData.warningAyatList
            val selectedAyah = if (warningAyat.isNotEmpty()) {
                warningAyat[(warningCount - 1).coerceAtLeast(0) % warningAyat.size]
            } else null

            val arabicAyah = selectedAyah?.arabicText ?: "فَوَيْلٌ لِّلْمُصَلِّينَ ۝ الَّذِينَ هُمْ عَن صَلَاتِهِمْ سَاهُونَ"
            val urduAyah = selectedAyah?.urduTranslation ?: "پس تباہی اور بربادی ہے ان نمازیوں کے لیے جو اپنی نماز سے غافل ہیں۔"
            val englishAyah = selectedAyah?.englishTranslation ?: "So woe to those who pray, [but] who are heedless of their prayer."
            val reference = selectedAyah?.referenceSource ?: "Surah Al-Ma'un (107:4-5)"

            val bigText = buildString {
                append("⚠️ URGENT REMINDER #$warningCount: You have not yet marked 'I am going to offer pray' for $prayerName.\n\n")
                append("$arabicAyah\n")
                append("\"$englishAyah\"\n")
                append("اردو: $urduAyah\n")
                append("[$reference]\n\n")
                append("Please leave all worldly distractions immediately and stand before Allah!")
            }

            val alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notification = NotificationCompat.Builder(context, SALAH_WARNING_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("⚠️ WARNING: Go Offer $prayerName Salah! (Warning #$warningCount)")
                .setContentText("Do not delay your Salah! \"$englishAyah\"")
                .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false)
                .setSound(alertSound, AudioManager.STREAM_ALARM)
                .setVibrate(longArrayOf(0, 800, 400, 800, 400, 800))
                .setContentIntent(openAppPendingIntent)
                .addAction(android.R.drawable.ic_input_add, "🕌 I Am Going to Pray", goingToPrayPendingIntent)
                .addAction(android.R.drawable.ic_menu_view, "Open Go for Salah", openAppPendingIntent)
                .build()

            val notificationId = WARNING_NOTIFICATION_BASE_ID + (prayerName.hashCode() % 100)
            notificationManager.notify(notificationId, notification)
            Log.d(TAG, "Triggered 5-minute recurring warning #$warningCount for $prayerName with Azaab Ayah")
        }

        fun showPrePrayerReminderDirect(
            context: Context,
            prayerName: String,
            minutesBefore: Int = 15
        ) {
            createNotificationChannels(context)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("route_target", "prayer_screen")
                putExtra("active_prayer_name", prayerName)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                prayerName.hashCode(),
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("🕌 $prayerName Prayer Reminder: In $minutesBefore Minutes")
                .setContentText("Prepare for $prayerName Salah. Perform Wudu & prepare your heart.")
                .setStyle(NotificationCompat.BigTextStyle().bigText(
                    "It is almost time for $prayerName ($minutesBefore minutes remaining).\n\"Indeed, prayer has been decreed upon the believers a decree of specified times.\" (4:103)"
                ))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(longArrayOf(0, 300, 200, 300))
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_view, "Open Go for Salah", pendingIntent)
                .build()

            val notificationId = REMINDER_NOTIFICATION_BASE_ID + (prayerName.hashCode() % 100)
            notificationManager.notify(notificationId, notification)
            Log.d(TAG, "Displayed pre-prayer reminder for $prayerName")
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannels(context)

        val action = intent.action ?: PrayerScheduler.ACTION_AZAN_ALARM
        val prayerName = intent.getStringExtra(PrayerScheduler.EXTRA_PRAYER_NAME) ?: "Salah"
        val scheduledTime = intent.getLongExtra(PrayerScheduler.EXTRA_PRAYER_TIME_MILLIS, 0L)

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "GoPrayer:PrayerAlarmFullWakeLock"
        ).apply {
            acquire(60 * 1000L) // 1 minute
        }

        try {
            when (action) {
                PrayerScheduler.ACTION_PRAYER_REMINDER -> {
                    // Pre-Prayer Reminder (15 mins before)
                    val minutesBefore = intent.getIntExtra(PrayerScheduler.EXTRA_REMINDER_MINUTES_BEFORE, 15)
                    showPrePrayerReminder(context, prayerName, minutesBefore)
                    return
                }

                PrayerScheduler.ACTION_ENTER_SALAH_NOTIFICATION -> {
                    // Enter Salah Time Notification (Automatic according to location)
                    val azanTime = intent.getLongExtra(PrayerScheduler.EXTRA_AZAN_TIME_MILLIS, 0L)
                    val jamatTime = intent.getLongExtra(PrayerScheduler.EXTRA_JAMAT_TIME_MILLIS, 0L)
                    showEnterSalahNotification(context, prayerName, azanTime, jamatTime)
                    return
                }

                PrayerScheduler.ACTION_JAMAAT_ALARM -> {
                    // Jama'at Time Arrival: Mobile Disable, App Blocker, Quiet Mode, Prayer Screen Lock
                    handleJamaatArrival(context, intent, prayerName, scheduledTime)
                    return
                }

                PrayerScheduler.ACTION_GOING_TO_PRAY -> {
                    // User clicked "I am going to offer pray" button
                    SalahAppBlockerService.setTemporaryBypass(durationMinutes = 30, prayerName = prayerName, context = context)
                    SalahAppBlockerService.deactivateBlocker(context)
                    PrayerScheduler(context).cancel5MinuteWarning(prayerName)

                    // Persist clear warning state
                    val prefs = context.getSharedPreferences("go_prayer_warning_state", Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("is_warning_active", false).apply()

                    // Cancel warning notification and app restriction notification
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val notificationId = WARNING_NOTIFICATION_BASE_ID + (prayerName.hashCode() % 100)
                    notificationManager.cancel(notificationId)
                    notificationManager.cancel(SalahAppBlockerService.SALAH_BLOCKER_NOTIFICATION_ID)
                    Log.d(TAG, "User marked 'Going to offer pray' for $prayerName. Blocker bypassed and notifications cancelled.")
                    return
                }

                AzkaarReminderManager.ACTION_AZKAAR_REMINDER -> {
                    val azkaarType = intent.getStringExtra(AzkaarReminderManager.EXTRA_AZKAAR_TYPE)
                        ?: AzkaarReminderManager.TYPE_MORNING
                    AzkaarReminderManager.showAzkaarNotification(context, azkaarType)
                    AzkaarReminderManager.scheduleAllAzkaarReminders(context)
                    return
                }

                AzkaarReminderManager.ACTION_MARK_AZKAAR_DONE -> {
                    val azkaarType = intent.getStringExtra(AzkaarReminderManager.EXTRA_AZKAAR_TYPE)
                        ?: AzkaarReminderManager.TYPE_MORNING
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val notificationId = when (azkaarType) {
                        AzkaarReminderManager.TYPE_MORNING -> AzkaarReminderManager.NOTIFICATION_ID_BASE + 1
                        AzkaarReminderManager.TYPE_EVENING -> AzkaarReminderManager.NOTIFICATION_ID_BASE + 2
                        AzkaarReminderManager.TYPE_POST_SALAH -> AzkaarReminderManager.NOTIFICATION_ID_BASE + 3
                        AzkaarReminderManager.TYPE_NIGHT -> AzkaarReminderManager.NOTIFICATION_ID_BASE + 4
                        else -> AzkaarReminderManager.NOTIFICATION_ID_BASE + 5
                    }
                    notificationManager.cancel(notificationId)

                    val azkaarPrefs = AzkaarReminderManager.getPrefs(context)
                    val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    val set = azkaarPrefs.getStringSet("azkaar_completed_dates", emptySet())?.toMutableSet() ?: mutableSetOf()
                    set.add(todayStr)
                    azkaarPrefs.edit()
                        .putStringSet("azkaar_completed_dates", set)
                        .putBoolean("azkaar_${todayStr}_${azkaarType}_completed", true)
                        .apply()

                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        android.widget.Toast.makeText(context, "✓ $azkaarType Azkaar marked as recited for today!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                PrayerScheduler.ACTION_SALAH_WARNING_5MIN -> {
                    // 5-minute recurring warning if prayer not marked as going to offer
                    val warningCount = intent.getIntExtra(PrayerScheduler.EXTRA_WARNING_COUNT, 1)

                    if (SalahAppBlockerService.isUserGoingToPray) {
                        Log.d(TAG, "5-min warning suppressed: user already marked going to pray for $prayerName")
                        return
                    }

                    // Suppress if already offered today
                    val isAlreadyOffered = try {
                        val db = com.example.data.local.AppDatabase.getDatabase(context)
                        val repo = com.example.data.repository.PrayerRepository(db)
                        kotlinx.coroutines.runBlocking { repo.isPrayerOfferedToday(prayerName) }
                    } catch (_: Exception) { false }

                    if (isAlreadyOffered) {
                        Log.d(TAG, "5-min warning suppressed: $prayerName already offered today")
                        SalahAppBlockerService.deactivateBlocker(context)
                        return
                    }

                    // Persist active warning state in SharedPreferences for UI sync
                    val prefs = context.getSharedPreferences("go_prayer_warning_state", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putBoolean("is_warning_active", true)
                        .putString("warning_prayer_name", prayerName)
                        .putInt("warning_count", warningCount)
                        .apply()

                    showSalahWarningNotification(context, prayerName, warningCount)

                    // Reschedule for next 5 minutes
                    PrayerScheduler(context).schedule5MinuteWarning(prayerName, warningCount + 1, 5 * 60 * 1000L)
                    return
                }

                PrayerScheduler.ACTION_AZAN_ALARM, PrayerScheduler.ACTION_PRAYER_ALARM -> {
                    // Azan Time Arrival (Adhan audio call to prayer)
                    handleAzanArrival(context, intent, prayerName, scheduledTime)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute prayer alarm receiver: ${e.message}", e)
        } finally {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }

    private fun handleAzanArrival(context: Context, intent: Intent, prayerName: String, scheduledTime: Long) {
        val isAlreadyOffered = try {
            val db = com.example.data.local.AppDatabase.getDatabase(context)
            val repo = com.example.data.repository.PrayerRepository(db)
            kotlinx.coroutines.runBlocking { repo.isPrayerOfferedToday(prayerName) }
        } catch (_: Exception) { false }

        if (isAlreadyOffered) {
            Log.d(TAG, "Azan alarm suppressed for $prayerName: already marked as offered today.")
            SalahAppBlockerService.deactivateBlocker(context)
            return
        }

        val adhanEnabled = intent.getBooleanExtra(PrayerScheduler.EXTRA_ADHAN_ENABLED, true)
        val quietEnabled = intent.getBooleanExtra(PrayerScheduler.EXTRA_QUIET_MODE_ENABLED, true)
        val duration = intent.getIntExtra(PrayerScheduler.EXTRA_QUIET_MODE_DURATION, 20)
        val trackId = intent.getStringExtra(PrayerScheduler.EXTRA_AUDIO_TRACK_ID) ?: "makkah_adhan"

        val now = System.currentTimeMillis()
        Log.d(TAG, "Azan alarm received for $prayerName (scheduled: $scheduledTime, now: $now)")

        if (prayerName == lastTriggeredPrayer && (now - lastTriggeredTime) < 2 * 60 * 1000L) {
            Log.w(TAG, "Duplicate azan suppressed for $prayerName")
            return
        }
        lastTriggeredPrayer = prayerName
        lastTriggeredTime = now

        // 1. Engage App Blocker service state at Azan time
        SalahAppBlockerService.activateBlocker(context, prayerName)

        // 2. Play Adhan call to prayer audio
        val serviceIntent = Intent(context, AdhanPlaybackService::class.java).apply {
            this.action = AdhanPlaybackService.ACTION_START_ADHAN
            putExtra(AdhanPlaybackService.EXTRA_PRAYER_NAME, prayerName)
            putExtra(AdhanPlaybackService.EXTRA_AUDIO_TRACK_ID, trackId)
            putExtra(AdhanPlaybackService.EXTRA_ADHAN_ENABLED, adhanEnabled)
            putExtra(AdhanPlaybackService.EXTRA_QUIET_MODE_ENABLED, quietEnabled)
            putExtra(AdhanPlaybackService.EXTRA_QUIET_MODE_DURATION, duration)
        }
        try {
            ContextCompat.startForegroundService(context, serviceIntent)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start Adhan service: ${e.message}")
        }

        // 3. Display prayer screen if device is unlocked, or defer until user unlocks device
        com.example.util.LockScreenHelper.handlePrayerArrivalDisplay(context, prayerName)

        // 4. Schedule recurring 5-minute warning starting 5 minutes after Azan time
        PrayerScheduler(context).schedule5MinuteWarning(prayerName, 1, 5 * 60 * 1000L)

        // 5. Reschedule upcoming prayers
        PrayerScheduler(context).scheduleUpcomingPrayers()
    }

    private fun handleJamaatArrival(context: Context, intent: Intent, prayerName: String, scheduledTime: Long) {
        val quietEnabled = intent.getBooleanExtra(PrayerScheduler.EXTRA_QUIET_MODE_ENABLED, true)
        val duration = intent.getIntExtra(PrayerScheduler.EXTRA_QUIET_MODE_DURATION, 20)

        val now = System.currentTimeMillis()
        Log.d(TAG, "Jama'at alarm received for $prayerName (scheduled: $scheduledTime, now: $now)")

        // 1. Engage App Blocker service state for Jama'at duration
        SalahAppBlockerService.activateBlocker(context, prayerName)

        // 2. Schedule first 5-minute warning starting after initial 5 minutes if not marked going to offer
        PrayerScheduler(context).schedule5MinuteWarning(prayerName, 1, 5 * 60 * 1000L)

        // 3. Reschedule upcoming prayers
        PrayerScheduler(context).scheduleUpcomingPrayers()
        PrayerScheduler(context).scheduleUpcomingPrayers()
    }

    private fun showEnterSalahNotification(
        context: Context,
        prayerName: String,
        azanTime: Long,
        jamatTime: Long
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createEnterSalahChannel(context, notificationManager)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route_target", "prayer_screen")
            putExtra("active_prayer_name", prayerName)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            ("enter_$prayerName").hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val azanStr = if (azanTime > 0) timeFormat.format(Date(azanTime)) else ""
        val jamatStr = if (jamatTime > 0) timeFormat.format(Date(jamatTime)) else ""

        val detailText = StringBuilder().apply {
            append("$prayerName Waqt has officially entered according to your location.")
            if (azanStr.isNotEmpty()) append(" • Azan: $azanStr")
            if (jamatStr.isNotEmpty()) append(" • Jama'at: $jamatStr")
            append("\n\n\"حَافِظُوا عَلَى الصَّلَوَاتِ وَالصَّلَاةِ الْوُسْطَىٰ\"\n\"Maintain with care the [obligatory] prayers.\" (Surah Al-Baqarah 2:238)")
        }.toString()

        val notification = NotificationCompat.Builder(context, ENTER_SALAH_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🕌 $prayerName Time Entered (داخل وقت)")
            .setContentText("$prayerName prayer time has officially started according to location.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(detailText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "Open AL-SUJOOD", pendingIntent)
            .build()

        val notificationId = ENTER_SALAH_NOTIFICATION_BASE_ID + (prayerName.hashCode() % 100)
        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Displayed Enter Salah notification for $prayerName")
    }

    private fun showSalahWarningNotification(
        context: Context,
        prayerName: String,
        warningCount: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createWarningChannel(context, notificationManager)

        // "I am going to offer pray" button action
        val goingToPrayIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = PrayerScheduler.ACTION_GOING_TO_PRAY
            putExtra(PrayerScheduler.EXTRA_PRAYER_NAME, prayerName)
        }
        val goingToPrayPendingIntent = PendingIntent.getBroadcast(
            context,
            ("going_pray_$prayerName").hashCode(),
            goingToPrayIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route_target", "prayer_screen")
            putExtra("active_prayer_name", prayerName)
            putExtra("warning_active", true)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            ("warn_open_$prayerName").hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Select authentic Azaab Ayah from SeedData
        val warningAyat = SeedData.warningAyatList
        val selectedAyah = if (warningAyat.isNotEmpty()) {
            warningAyat[(warningCount - 1) % warningAyat.size]
        } else null

        val arabicAyah = selectedAyah?.arabicText ?: "فَوَيْلٌ لِّلْمُصَلِّينَ ۝ الَّذِينَ هُمْ عَن صَلَاتِهِمْ سَاهُونَ"
        val urduAyah = selectedAyah?.urduTranslation ?: "پس تباہی اور بربادی ہے ان نمازیوں کے لیے جو اپنی نماز سے غافل ہیں۔"
        val englishAyah = selectedAyah?.englishTranslation ?: "So woe to those who pray, [but] who are heedless of their prayer."
        val reference = selectedAyah?.referenceSource ?: "Surah Al-Ma'un (107:4-5)"

        val bigText = StringBuilder().apply {
            append("⚠️ URGENT REMINDER #$warningCount: You have not yet marked 'I am going to offer pray' for $prayerName.\n\n")
            append("$arabicAyah\n")
            append("\"$englishAyah\"\n")
            append("اردو: $urduAyah\n")
            append("[$reference]\n\n")
            append("Please leave all worldly distractions immediately and stand before Allah!")
        }.toString()

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, SALAH_WARNING_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("⚠️ WARNING: Go Offer $prayerName Salah! (Warning #$warningCount)")
            .setContentText("Do not delay your Salah! \"$englishAyah\"")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 800, 400, 800, 400, 800))
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_input_add, "I Am Going to Offer Pray", goingToPrayPendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "Open AL-SUJOOD", openAppPendingIntent)
            .build()

        val notificationId = WARNING_NOTIFICATION_BASE_ID + (prayerName.hashCode() % 100)
        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Triggered 5-minute recurring warning #$warningCount for $prayerName with Azaab Ayah")
    }

    private fun showPrePrayerReminder(context: Context, prayerName: String, minutesBefore: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createReminderChannel(context, notificationManager)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route_target", "prayer_screen")
            putExtra("active_prayer_name", prayerName)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            prayerName.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🕌 $prayerName Prayer Reminder: In $minutesBefore Minutes")
            .setContentText("Prepare for $prayerName Salah. Perform Wudu & prepare your heart.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "It is almost time for $prayerName ($minutesBefore minutes remaining).\n\"Indeed, prayer has been decreed upon the believers a decree of specified times.\" (4:103)"
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "Open AL-SUJOOD", pendingIntent)
            .build()

        val notificationId = REMINDER_NOTIFICATION_BASE_ID + (prayerName.hashCode() % 100)
        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Displayed pre-prayer reminder for $prayerName")
    }

    private fun createReminderChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Prayer Time Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pre-prayer countdown reminders and Wudu preparations"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createEnterSalahChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ENTER_SALAH_CHANNEL_ID,
                "Enter Salah Time Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when Salah time officially enters according to location"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createWarningChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SALAH_WARNING_CHANNEL_ID,
                "Salah Neglect Warning & Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recurring 5-minute warnings and Azaab Quranic Ayat when Salah is delayed"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 400, 800, 400, 800)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}

