package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.SeedData

class SalahAppBlockerService : AccessibilityService() {

    companion object {
        private const val TAG = "SalahAppBlocker"
        const val SALAH_BLOCKER_CHANNEL_ID = "go_prayer_salah_app_blocker_channel_v3"
        const val SALAH_BLOCKER_NOTIFICATION_ID = 55099

        @Volatile
        var isBlockerActive: Boolean = false

        @Volatile
        var activePrayerName: String = "Salah"

        @Volatile
        var temporaryBypassUntilMillis: Long = 0L

        @Volatile
        var bypassedPrayerName: String = ""

        @Volatile
        var isServiceConnected: Boolean = false

        @Volatile
        var isTestModeActive: Boolean = false

        @Volatile
        var testExpiresAtMillis: Long = 0L

        private var lastBlockedTimestamp: Long = 0L
        private var lastBlockedPackage: String = ""

        val isUserGoingToPray: Boolean
            get() = System.currentTimeMillis() < temporaryBypassUntilMillis

        fun isCurrentlyInPrayerTime(context: Context): Pair<Boolean, String> {
            try {
                val now = System.currentTimeMillis()

                // Check active test mode
                if (isTestModeActive && now < testExpiresAtMillis) {
                    return Pair(true, activePrayerName.ifEmpty { "Test Salah" })
                }

                // If user has active temporary bypass for current prayer, do not block
                if (now < temporaryBypassUntilMillis) {
                    return Pair(false, "")
                }

                return kotlinx.coroutines.runBlocking {
                    val database = com.example.data.local.AppDatabase.getDatabase(context)
                    val repository = com.example.data.repository.PrayerRepository(database)
                    val location = repository.getLocation()
                    val settings = repository.getSettings()
                    if (!settings.totalDisableOnPrayer) return@runBlocking Pair(false, "")
                    val configs = repository.getPrayerConfigurations()
                    val schedule = repository.calculateDaySchedule(java.util.Calendar.getInstance(), location, settings, configs)

                    for (prayer in schedule.prayers) {
                        if (prayer.name.equals("Sunrise", ignoreCase = true)) continue

                        // If prayer is already offered today, do not block
                        if (repository.isPrayerOfferedToday(prayer.name)) {
                            continue
                        }

                        // If user bypassed this specific prayer, do not block
                        if (prayer.name.equals(bypassedPrayerName, ignoreCase = true) && now < temporaryBypassUntilMillis) {
                            continue
                        }

                        val config = configs.find { it.prayerName.equals(prayer.name, ignoreCase = true) }
                        val durationMinutes = config?.quietModeDurationMinutes ?: 20
                        val durationMillis = durationMinutes * 60 * 1000L

                        val azanTime = if (prayer.azanTimeMillis > 0L) prayer.azanTimeMillis else prayer.timestampMillis
                        val jamatTime = if (prayer.jamatTimeMillis > 0L) prayer.jamatTimeMillis else azanTime + 20 * 60 * 1000L
                        val startTime = minOf(prayer.timestampMillis, azanTime)
                        val windowEnd = jamatTime + durationMillis

                        if (now in startTime..windowEnd) {
                            return@runBlocking Pair(true, prayer.name)
                        }
                    }
                    Pair(false, "")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error checking real-time prayer schedule for blocking: ${e.message}")
            }
            return Pair(false, "")
        }

        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            if (isServiceConnected) return true
            try {
                // Method 1: Check via AccessibilityManager
                val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
                if (am != null) {
                    val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                    for (service in enabledServices) {
                        val sInfo = service.resolveInfo?.serviceInfo
                        if (sInfo != null && sInfo.packageName == context.packageName &&
                            sInfo.name.contains("SalahAppBlockerService", ignoreCase = true)
                        ) {
                            return true
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error checking accessibility status via AccessibilityManager: ${e.message}")
            }

            try {
                // Method 2: Fallback check via Settings.Secure
                val enabledServices = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                ) ?: return false
                val colonSplitter = TextUtils.SimpleStringSplitter(':')
                colonSplitter.setString(enabledServices)
                while (colonSplitter.hasNext()) {
                    val componentName = colonSplitter.next()
                    if (componentName.contains(context.packageName, ignoreCase = true) &&
                        componentName.contains("SalahAppBlockerService", ignoreCase = true)
                    ) {
                        return true
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error checking accessibility status via Settings: ${e.message}")
            }
            return false
        }

        fun activateBlocker(context: Context, prayerName: String, isTest: Boolean = false, durationMinutes: Int = 2) {
            val now = System.currentTimeMillis()

            if (isTest) {
                // Testing always bypasses previous suppression
                isTestModeActive = true
                testExpiresAtMillis = now + (durationMinutes * 60 * 1000L)
                temporaryBypassUntilMillis = 0L
                bypassedPrayerName = ""
                isBlockerActive = true
                activePrayerName = prayerName
                Log.d(TAG, "Test mode activated for $durationMinutes minutes for $prayerName")

                try {
                    val prefs = context.getSharedPreferences("go_prayer_blocker_state", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putBoolean("is_blocker_active", true)
                        .putBoolean("is_test", true)
                        .putLong("test_expires_at", testExpiresAtMillis)
                        .putString("active_prayer_name", prayerName)
                        .putLong("temporary_bypass_until", 0L)
                        .apply()
                } catch (_: Exception) {}
                return
            }

            // Normal prayer activation: check if this prayer was already bypassed and bypass still valid
            if (prayerName.equals(bypassedPrayerName, ignoreCase = true) && now < temporaryBypassUntilMillis) {
                Log.d(TAG, "Suppressed blocker activation for $prayerName: user going to pray bypass active")
                return
            }

            isBlockerActive = true
            activePrayerName = prayerName
            isTestModeActive = false
            testExpiresAtMillis = 0L
            Log.d(TAG, "Activated Salah App Blocker for $prayerName")

            try {
                val prefs = context.getSharedPreferences("go_prayer_blocker_state", Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("is_blocker_active", true)
                    .putString("active_prayer_name", prayerName)
                    .putBoolean("is_test", false)
                    .apply()
            } catch (_: Exception) {}
        }

        fun deactivateBlocker(context: Context? = null) {
            isBlockerActive = false
            isTestModeActive = false
            testExpiresAtMillis = 0L
            Log.d(TAG, "Deactivated Salah App Blocker")

            if (context != null) {
                try {
                    val prefs = context.getSharedPreferences("go_prayer_blocker_state", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putBoolean("is_blocker_active", false)
                        .putBoolean("is_test", false)
                        .putLong("test_expires_at", 0L)
                        .apply()

                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(SALAH_BLOCKER_NOTIFICATION_ID)
                } catch (_: Exception) {}
            }
        }

        fun setTemporaryBypass(durationMinutes: Int = 30, prayerName: String = "", context: Context? = null) {
            val bypassUntil = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
            temporaryBypassUntilMillis = bypassUntil
            bypassedPrayerName = prayerName.ifEmpty { activePrayerName }
            isBlockerActive = false
            isTestModeActive = false
            testExpiresAtMillis = 0L
            Log.d(TAG, "Temporary blocker bypass granted for $durationMinutes minutes for $bypassedPrayerName")

            if (context != null) {
                try {
                    val prefs = context.getSharedPreferences("go_prayer_blocker_state", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putLong("temporary_bypass_until", bypassUntil)
                        .putString("bypassed_prayer_name", bypassedPrayerName)
                        .putBoolean("is_blocker_active", false)
                        .putBoolean("is_test", false)
                        .apply()

                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(SALAH_BLOCKER_NOTIFICATION_ID)
                } catch (_: Exception) {}
            }
        }

        fun shouldBlockApps(context: Context): Boolean {
            val now = System.currentTimeMillis()

            // 1. Check test mode
            if (isTestModeActive) {
                if (now < testExpiresAtMillis) {
                    return true
                } else {
                    isTestModeActive = false
                    testExpiresAtMillis = 0L
                    isBlockerActive = false
                }
            }

            // 2. Check persistent test state
            try {
                val prefs = context.getSharedPreferences("go_prayer_blocker_state", Context.MODE_PRIVATE)
                val testActive = prefs.getBoolean("is_test", false)
                val testExpires = prefs.getLong("test_expires_at", 0L)
                if (testActive && now < testExpires) {
                    isTestModeActive = true
                    testExpiresAtMillis = testExpires
                    activePrayerName = prefs.getString("active_prayer_name", "Salah") ?: "Salah"
                    return true
                }
            } catch (_: Exception) {}

            // 3. Check temporary bypass
            if (now < temporaryBypassUntilMillis) {
                return false
            }

            try {
                val prefs = context.getSharedPreferences("go_prayer_blocker_state", Context.MODE_PRIVATE)
                val bypassUntil = prefs.getLong("temporary_bypass_until", 0L)
                if (now < bypassUntil) {
                    temporaryBypassUntilMillis = bypassUntil
                    bypassedPrayerName = prefs.getString("bypassed_prayer_name", "") ?: ""
                    return false
                }
            } catch (_: Exception) {}

            // 4. Check explicit active flag
            if (isBlockerActive) {
                // If active prayer has already been offered, auto-deactivate
                try {
                    val database = com.example.data.local.AppDatabase.getDatabase(context)
                    val repository = com.example.data.repository.PrayerRepository(database)
                    val isOffered = kotlinx.coroutines.runBlocking {
                        repository.isPrayerOfferedToday(activePrayerName)
                    }
                    if (isOffered) {
                        deactivateBlocker(context)
                        return false
                    }
                } catch (_: Exception) {}
                return true
            }

            // 5. Check persistent SharedPreferences state
            try {
                val prefs = context.getSharedPreferences("go_prayer_blocker_state", Context.MODE_PRIVATE)
                val isPersistentActive = prefs.getBoolean("is_blocker_active", false)
                if (isPersistentActive) {
                    val pName = prefs.getString("active_prayer_name", "Salah") ?: "Salah"
                    val database = com.example.data.local.AppDatabase.getDatabase(context)
                    val repository = com.example.data.repository.PrayerRepository(database)
                    val isOffered = kotlinx.coroutines.runBlocking {
                        repository.isPrayerOfferedToday(pName)
                    }
                    if (isOffered) {
                        deactivateBlocker(context)
                        return false
                    }
                    isBlockerActive = true
                    activePrayerName = pName
                    return true
                }
            } catch (_: Exception) {}

            // 6. Dynamic real-time check against calculated prayer schedule
            val (inPrayerTime, prayerName) = isCurrentlyInPrayerTime(context)
            if (inPrayerTime) {
                isBlockerActive = true
                activePrayerName = prayerName
                return true
            }

            return false
        }

        fun showAppBlockedWarningNotification(
            context: Context,
            prayerName: String = activePrayerName,
            isTest: Boolean = isTestModeActive
        ) {
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                createBlockerNotificationChannel(context, notificationManager)

                // Wake screen briefly for heads-up visibility
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                powerManager?.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "GoPrayer:SalahAppBlockAlertWakeLock"
                )?.apply {
                    try {
                        acquire(4000L)
                    } catch (_: Exception) {}
                }

                // Play audible alert sound via Alarm stream and vibrate strongly
                playAudibleWarningAlert(context)

                // Action 1: "I am going to pray" broadcast pending intent
                val goingToPrayIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    action = PrayerScheduler.ACTION_GOING_TO_PRAY
                    putExtra(PrayerScheduler.EXTRA_PRAYER_NAME, prayerName)
                }
                val goingToPrayPendingIntent = PendingIntent.getBroadcast(
                    context,
                    55101,
                    goingToPrayIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Action 2: Open Go for Salah MainActivity
                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("route_target", "prayer_screen")
                    putExtra("active_prayer_name", prayerName)
                }
                val openAppPendingIntent = PendingIntent.getActivity(
                    context,
                    55102,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val testPrefix = if (isTest) "[TEST] " else ""
                val title = "⚠️ ${testPrefix}Prayer Time Active — Go for Salah! (نماز کا وقت ہے)"

                // Authentic warning reminder
                val warningAyat = SeedData.warningAyatList
                val selectedAyah = if (warningAyat.isNotEmpty()) warningAyat.first() else null
                val arabicAyah = selectedAyah?.arabicText ?: "إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَّوْقُوتًا"
                val englishAyah = selectedAyah?.englishTranslation ?: "Indeed, prayer has been decreed upon the believers a decree of specified times."
                val urduAyah = selectedAyah?.urduTranslation ?: "بے شک نماز مومنوں پر مقررہ اوقات میں فرض کی گئی ہے۔"

                val bigText = buildString {
                    append("«$arabicAyah»\n\n")
                    append("\"$englishAyah\"\n")
                    append("اردو: $urduAyah\n\n")
                    append("Apps are restricted to guard your $prayerName Salah from worldly distractions. Please leave your phone and hasten to prayer before Allah!")
                }

                val alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                val builder = NotificationCompat.Builder(context, SALAH_BLOCKER_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText("Apps restricted for $prayerName Salah. Tap 'Going to Pray' or open Salah screen.")
                    .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setSound(alertSound, AudioManager.STREAM_ALARM)
                    .setVibrate(longArrayOf(0, 600, 250, 600, 250, 800))
                    .setContentIntent(openAppPendingIntent)
                    .setFullScreenIntent(openAppPendingIntent, true)
                    .addAction(
                        android.R.drawable.ic_input_add,
                        "🕌 I'm Going to Pray",
                        goingToPrayPendingIntent
                    )
                    .addAction(
                        android.R.drawable.ic_menu_view,
                        "Open Go for Salah",
                        openAppPendingIntent
                    )

                notificationManager.notify(SALAH_BLOCKER_NOTIFICATION_ID, builder.build())
                Log.d(TAG, "Sent high-priority Heads-up notification for app restriction during $prayerName")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to show app blocked warning notification: ${e.message}", e)
            }
        }

        private fun playAudibleWarningAlert(context: Context) {
            try {
                // Vibrate
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
                    }
                }

                // Play audible tone via RingtoneManager
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context, alarmUri)
                if (ringtone != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ringtone.audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                            .build()
                    } else {
                        @Suppress("DEPRECATION")
                        ringtone.streamType = AudioManager.STREAM_ALARM
                    }
                    ringtone.play()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not play standalone alert sound: ${e.message}")
            }
        }

        private fun createBlockerNotificationChannel(context: Context, notificationManager: NotificationManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val alarmAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .build()

                val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                val channel = NotificationChannel(
                    SALAH_BLOCKER_CHANNEL_ID,
                    "Salah Guard - App Restriction Alert",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Urgent warning notifications when apps are opened during active Salah time"
                    enableLights(true)
                    lightColor = android.graphics.Color.RED
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 600, 250, 600, 250, 800)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                    setSound(alarmSound, alarmAttributes)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceConnected = true
        Log.d(TAG, "Salah App Blocker Accessibility Service connected")
    }

    private fun isWhitelistedPackage(packageName: String): Boolean {
        val myPkg = packageName.lowercase()
        val appPkg = applicationContext.packageName.lowercase()

        if (myPkg == appPkg || myPkg.contains("goprayer") || myPkg.contains("alsujood")) {
            return true
        }

        // Essential phone calls, emergency, dialer, and system frameworks
        if (myPkg.contains("systemui") ||
            myPkg.contains("dialer") ||
            myPkg.contains("telecom") ||
            myPkg.contains("emergency") ||
            myPkg.contains("phone") ||
            myPkg.contains("incallui") ||
            myPkg.contains("inputmethod") ||
            myPkg.contains("keyboard") ||
            myPkg.contains("latin") ||
            myPkg.contains("gboard") ||
            myPkg.contains("accessibility") ||
            myPkg.contains("settings") ||
            myPkg.contains("permissioncontroller") ||
            myPkg.contains("packageinstaller")
        ) {
            return true
        }

        // Home launchers & recents
        if (myPkg.contains("launcher") ||
            myPkg.contains("trebuchet") ||
            myPkg.contains("quickstep") ||
            myPkg.contains("miui.home") ||
            myPkg.contains("sec.android.app.launcher") ||
            myPkg.contains("huawei.android.launcher") ||
            myPkg.contains("oppo.launcher") ||
            myPkg.contains("bbk.launcher2") ||
            myPkg.contains("oneplus.launcher") ||
            myPkg.contains("nexuslauncher")
        ) {
            return true
        }

        // Dynamic check for default HOME activity
        try {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfos = packageManager.queryIntentActivities(intent, 0)
            for (info in resolveInfos) {
                if (info.activityInfo?.packageName.equals(packageName, ignoreCase = true)) {
                    return true
                }
            }
        } catch (_: Exception) {}

        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val eventType = event.eventType
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED ||
            eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            val packageName = event.packageName?.toString() ?: return
            val now = System.currentTimeMillis()

            // Check if blocking is active
            if (shouldBlockApps(applicationContext)) {
                // If package is whitelisted (System UI, phone, home launcher, or our app), allow it
                if (isWhitelistedPackage(packageName)) {
                    return
                }

                // Debounce rapid window state events for the same package within 1.5s
                if (packageName == lastBlockedPackage && (now - lastBlockedTimestamp) < 1500L) {
                    return
                }
                lastBlockedPackage = packageName
                lastBlockedTimestamp = now

                Log.d(TAG, "Intercepted prohibited app launch: $packageName during $activePrayerName Salah. Minimizing and displaying warning.")

                // 1. Immediately press HOME to minimize the distracting app
                try {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                } catch (e: Exception) {
                    Log.w(TAG, "performGlobalAction(GLOBAL_ACTION_HOME) failed: ${e.message}")
                }

                // 2. Bring Go for Salah to foreground
                try {
                    val prayerScreenIntent = Intent(applicationContext, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("route_target", "prayer_screen")
                        putExtra("active_prayer_name", activePrayerName)
                    }
                    startActivity(prayerScreenIntent)
                } catch (e: Exception) {
                    Log.w(TAG, "Could not launch MainActivity: ${e.message}")
                }

                // 3. Display the high-priority Heads-up Notification with audible alert & full screen intent
                showAppBlockedWarningNotification(
                    context = applicationContext,
                    prayerName = activePrayerName,
                    isTest = isTestModeActive
                )
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Salah App Blocker Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceConnected = false
    }
}
