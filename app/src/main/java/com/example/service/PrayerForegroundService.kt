package com.example.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.repository.PrayerRepository
import com.example.domain.prayer.DayPrayerSchedule
import com.example.domain.prayer.PrayerTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.NetworkInterface
import java.util.Calendar
import java.util.Collections

/**
 * Dedicated Foreground Service for real-time prayer time monitoring on Android 10+.
 * Ensures persistent background execution, maintains a sticky ongoing notification with
 * live countdown to the next prayer, and guarantees reliable Adhan and warning alerts
 * even during deep sleep / Doze mode.
 */
class PrayerForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var monitoringJob: Job? = null
    private var scheduleObservationJob: Job? = null

    private val database by lazy { AppDatabase.getDatabase(applicationContext) }
    private val repository by lazy { PrayerRepository(database) }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val connectivityManager by lazy {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val powerManager by lazy {
        getSystemService(Context.POWER_SERVICE) as? PowerManager
    }

    private var latestSchedule: DayPrayerSchedule? = null
    private var lastTriggeredAzanPrayer: String = ""
    private var lastTriggeredAzanTime: Long = 0L

    private var lastTriggeredJamatPrayer: String = ""
    private var lastTriggeredJamatTime: Long = 0L

    private var lastTriggeredWarningPrayer: String = ""
    private var lastTriggeredWarningCount: Int = 0
    private var lastTriggeredWarningTime: Long = 0L

    // VPN detection state
    private var vpnNetworkCallback: ConnectivityManager.NetworkCallback? = null
    private var defaultNetworkCallback: ConnectivityManager.NetworkCallback? = null
    private var isCurrentlyVpnConnected = false
    private var lastVpnNotificationTime = 0L

    // Lock screen unlock listener
    private val unlockReceiver = DeviceUnlockReceiver()

    companion object {
        private const val TAG = "PrayerForegroundService"
        const val MONITORING_CHANNEL_ID = "go_prayer_monitoring_channel_v4"
        const val VPN_ALERT_CHANNEL_ID = "go_prayer_vpn_alert_channel_v3"

        const val NOTIFICATION_ID = 7701
        const val VPN_ALERT_NOTIFICATION_ID = 7799

        const val ACTION_START = "com.example.goprayer.ACTION_START_MONITORING"
        const val ACTION_STOP = "com.example.goprayer.ACTION_STOP_MONITORING"
        const val ACTION_REFRESH = "com.example.goprayer.ACTION_REFRESH_MONITORING"
        const val ACTION_TEST_ADHAN = "com.example.goprayer.ACTION_TEST_ADHAN"
        const val ACTION_TEST_VPN_ALERT = "com.example.goprayer.ACTION_TEST_VPN_ALERT"

        fun startService(context: Context) {
            try {
                val intent = Intent(context, PrayerForegroundService::class.java).apply {
                    action = ACTION_START
                }
                ContextCompat.startForegroundService(context, intent)
                Log.d(TAG, "Started PrayerForegroundService")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start PrayerForegroundService: ${e.message}")
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, PrayerForegroundService::class.java).apply {
                    action = ACTION_STOP
                }
                context.stopService(intent)
                Log.d(TAG, "Stopped PrayerForegroundService")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop PrayerForegroundService: ${e.message}")
            }
        }

        fun refresh(context: Context) {
            try {
                val intent = Intent(context, PrayerForegroundService::class.java).apply {
                    action = ACTION_REFRESH
                }
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send refresh intent to PrayerForegroundService: ${e.message}")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "PrayerForegroundService onCreate - initializing silent background monitoring")
        
        // Ensure legacy duplicate VpnGuardService is stopped if running
        try {
            val stopVpnIntent = Intent(applicationContext, VpnGuardService::class.java)
            applicationContext.stopService(stopVpnIntent)
        } catch (_: Exception) {}

        createNotificationChannels()

        // Start foreground immediately with a minimal silent notification
        val initialNotification = buildMonitoringNotification(null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC or ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                } else {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                }
                startForeground(NOTIFICATION_ID, initialNotification, serviceType)
            } catch (e: Exception) {
                Log.w(TAG, "Falling back to standard startForeground: ${e.message}")
                startForeground(NOTIFICATION_ID, initialNotification)
            }
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        observePrayerSchedule()
        registerNetworkMonitors()
        try {
            val filter = android.content.IntentFilter(Intent.ACTION_USER_PRESENT)
            registerReceiver(unlockReceiver, filter)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register unlockReceiver: ${e.message}")
        }
        startMonitoringLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        Log.d(TAG, "onStartCommand received action: $action")

        when (action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TEST_VPN_ALERT -> {
                triggerVpnAlertNotification(isTest = true)
            }
            ACTION_TEST_ADHAN -> {
                triggerTestAdhan()
            }
            ACTION_REFRESH -> {
                serviceScope.launch {
                    refreshSchedule()
                }
            }
            else -> {
                serviceScope.launch {
                    refreshSchedule()
                    checkAndAlertVpnStatus()
                }
            }
        }

        // Return START_STICKY to guarantee restart if killed by OS
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "App swiped from recents. Scheduling immediate revival via AlarmManager...")
        val restartIntent = Intent(applicationContext, PrayerForegroundService::class.java).apply {
            action = ACTION_START
            setPackage(packageName)
        }
        try {
            val pendingIntent = PendingIntent.getService(
                applicationContext,
                7708,
                restartIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 1000L,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 1000L,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule revival alarm: ${e.message}")
        }
    }

    override fun onDestroy() {
        monitoringJob?.cancel()
        scheduleObservationJob?.cancel()
        unregisterNetworkMonitors()
        try {
            unregisterReceiver(unlockReceiver)
        } catch (_: Exception) {}
        super.onDestroy()
        Log.d(TAG, "PrayerForegroundService destroyed")
    }

    private fun observePrayerSchedule() {
        scheduleObservationJob?.cancel()
        scheduleObservationJob = serviceScope.launch {
            repository.scheduleFlow.collectLatest { schedule ->
                latestSchedule = schedule
                notificationManager.notify(NOTIFICATION_ID, buildMonitoringNotification(schedule))
            }
        }
    }

    private suspend fun refreshSchedule() {
        try {
            val settings = repository.getSettings()
            val location = repository.getLocation()
            val configs = repository.getPrayerConfigurations()
            val schedule = repository.calculateDaySchedule(
                Calendar.getInstance(),
                location,
                settings,
                configs
            )
            latestSchedule = schedule
            notificationManager.notify(NOTIFICATION_ID, buildMonitoringNotification(schedule))
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing schedule in foreground service: ${e.message}")
        }
    }

    /**
     * Active background monitoring loop:
     * - Runs every 20 seconds.
     * - Checks real-time arrival of Azan time, Jama'at time, and Enter Salah.
     * - Updates the sticky countdown notification every minute.
     * - Triggers Adhan and heads-up alerts reliably on Android 10+.
     */
    private fun startMonitoringLoop() {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            while (isActive) {
                try {
                    val now = System.currentTimeMillis()
                    val schedule = latestSchedule ?: fetchCurrentSchedule()

                    if (schedule != null) {
                        checkPrayerTriggers(schedule, now)
                    }

                    checkAndAlertVpnStatus()

                    // Update ongoing sticky notification with up-to-date countdown
                    if (schedule != null) {
                        notificationManager.notify(NOTIFICATION_ID, buildMonitoringNotification(schedule))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in monitoring loop iteration: ${e.message}")
                }
                delay(20_000L) // poll every 20 seconds for high precision
            }
        }
    }

    private suspend fun fetchCurrentSchedule(): DayPrayerSchedule? {
        return try {
            val settings = repository.getSettings()
            val location = repository.getLocation()
            val configs = repository.getPrayerConfigurations()
            val sched = repository.calculateDaySchedule(
                Calendar.getInstance(),
                location,
                settings,
                configs
            )
            latestSchedule = sched
            sched
        } catch (e: Exception) {
            Log.w(TAG, "Could not fetch current schedule: ${e.message}")
            null
        }
    }

    private suspend fun checkPrayerTriggers(schedule: DayPrayerSchedule, now: Long) {
        val configs = repository.getPrayerConfigurations()
        val settings = repository.getSettings()

        for (prayer in schedule.prayers) {
            if (prayer.name.equals("Sunrise", ignoreCase = true)) continue

            // If prayer is already offered today, DO NOT trigger Azan, blocker, or warnings!
            val isOffered = repository.isPrayerOfferedToday(prayer.name)
            if (isOffered) {
                continue
            }

            val config = configs.find { it.prayerName.equals(prayer.name, ignoreCase = true) }
            val azanTime = if (prayer.azanTimeMillis > 0L) prayer.azanTimeMillis else prayer.timestampMillis
            val jamatTime = if (prayer.jamatTimeMillis > 0L) prayer.jamatTimeMillis else azanTime + (20 * 60 * 1000L)

            // Trigger 1: Azan Alarm Arrival (within a 1-minute window)
            if (now >= azanTime && now < (azanTime + 60_000L)) {
                if (lastTriggeredAzanPrayer != prayer.name || (now - lastTriggeredAzanTime) > 120_000L) {
                    lastTriggeredAzanPrayer = prayer.name
                    lastTriggeredAzanTime = now
                    Log.i(TAG, "🔔 AZAN TIME ARRIVED for ${prayer.name}! Triggering Adhan audio & alert...")
                    triggerAdhanPlaybackAndAlert(prayer, config)
                }
            }

            // Trigger 2: Jama'at / Enter Salah Window
            if (now >= jamatTime && now < (jamatTime + 60_000L)) {
                if (lastTriggeredJamatPrayer != prayer.name || (now - lastTriggeredJamatTime) > 120_000L) {
                    lastTriggeredJamatPrayer = prayer.name
                    lastTriggeredJamatTime = now
                    Log.i(TAG, "🕌 JAMA'AT TIME ARRIVED for ${prayer.name}! Triggering Enter Salah Notification...")
                    PrayerAlarmReceiver.showEnterSalahNotificationDirect(
                        context = applicationContext,
                        prayerName = prayer.name,
                        azanTime = azanTime,
                        jamatTime = jamatTime
                    )
                }
            }

            // Trigger 3: Active App Blocker activation during prayer window
            val windowEnd = jamatTime + ((config?.quietModeDurationMinutes ?: 20) * 60 * 1000L)
            if (now in azanTime..windowEnd) {
                if (settings.totalDisableOnPrayer && !SalahAppBlockerService.isUserGoingToPray) {
                    if (!SalahAppBlockerService.isBlockerActive) {
                        SalahAppBlockerService.activateBlocker(applicationContext, prayer.name)
                    }
                }
            }

            // Trigger 4: 5-Minute Neglect Warning
            val warningStartTime = jamatTime + 5 * 60 * 1000L
            if (now >= warningStartTime && now <= windowEnd) {
                if (!SalahAppBlockerService.isUserGoingToPray) {
                    if (lastTriggeredWarningPrayer != prayer.name || (now - lastTriggeredWarningTime) > 300_000L) {
                        lastTriggeredWarningPrayer = prayer.name
                        lastTriggeredWarningTime = now
                        lastTriggeredWarningCount++
                        Log.i(TAG, "⚠️ 5-Min Neglect Warning triggered for ${prayer.name} (#$lastTriggeredWarningCount)")
                        PrayerAlarmReceiver.showSalahWarningNotificationDirect(
                            context = applicationContext,
                            prayerName = prayer.name,
                            warningCount = lastTriggeredWarningCount
                        )
                    }
                }
            }
        }
    }

    private fun triggerAdhanPlaybackAndAlert(
        prayer: PrayerTime,
        config: com.example.data.local.entities.PrayerConfiguration?
    ) {
        // Acquire wake lock to ensure phone wakes up on Android 10+
        powerManager?.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "GoPrayer:AdhanWakeLock"
        )?.apply {
            try {
                acquire(10_000L)
            } catch (_: Exception) {}
        }

        val trackId = config?.audioTrackId ?: "makkah_adhan"
        val adhanEnabled = config?.adhanEnabled ?: true
        val quietMode = config?.quietModeEnabled ?: true
        val duration = config?.quietModeDurationMinutes ?: 20

        // Start Adhan audio playback foreground service
        val adhanIntent = Intent(applicationContext, AdhanPlaybackService::class.java).apply {
            action = AdhanPlaybackService.ACTION_START_ADHAN
            putExtra(AdhanPlaybackService.EXTRA_PRAYER_NAME, prayer.name)
            putExtra(AdhanPlaybackService.EXTRA_AUDIO_TRACK_ID, trackId)
            putExtra(AdhanPlaybackService.EXTRA_ADHAN_ENABLED, adhanEnabled)
            putExtra(AdhanPlaybackService.EXTRA_QUIET_MODE_ENABLED, quietMode)
            putExtra(AdhanPlaybackService.EXTRA_QUIET_MODE_DURATION, duration)
        }
        try {
            ContextCompat.startForegroundService(applicationContext, adhanIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start AdhanPlaybackService: ${e.message}")
        }

        // Activate blocker service
        SalahAppBlockerService.activateBlocker(applicationContext, prayer.name)

        // Show PrayerScreen if device is unlocked, or defer until user unlocks device
        com.example.util.LockScreenHelper.handlePrayerArrivalDisplay(applicationContext, prayer.name)

        // Show Enter Salah / Azan arrival notification
        PrayerAlarmReceiver.showEnterSalahNotificationDirect(
            context = applicationContext,
            prayerName = prayer.name,
            azanTime = prayer.azanTimeMillis,
            jamatTime = prayer.jamatTimeMillis
        )
    }

    private fun triggerTestAdhan() {
        val nextP = latestSchedule?.nextPrayer
        val prayerName = nextP?.name ?: "Asr"
        val azanT = nextP?.azanTimeMillis ?: System.currentTimeMillis()
        val jamatT = nextP?.jamatTimeMillis ?: (azanT + 20 * 60 * 1000L)

        PrayerAlarmReceiver.showEnterSalahNotificationDirect(
            context = applicationContext,
            prayerName = prayerName,
            azanTime = azanT,
            jamatTime = jamatT
        )
    }

    private fun buildMonitoringNotification(schedule: DayPrayerSchedule?): Notification {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            7710,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, MONITORING_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("AL-SUJOOD")
            .setContentText("Background Prayer Protection Active")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setSilent(true)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                MONITORING_CHANNEL_ID,
                "AL-SUJOOD Silent Background Service",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Silent background service maintaining background prayer triggers and guard"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }
            notificationManager.createNotificationChannel(serviceChannel)

            val alertChannel = NotificationChannel(
                VPN_ALERT_CHANNEL_ID,
                "AL-SUJOOD VPN & Distraction Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority heads-up warnings for VPN connections and Salah neglect"
                enableLights(true)
                lightColor = android.graphics.Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 800)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    // VPN Monitoring Callbacks
    private fun registerNetworkMonitors() {
        try {
            val vpnRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
                .build()

            vpnNetworkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    onVpnStateChanged(true)
                }

                override fun onLost(network: Network) {
                    checkAndAlertVpnStatus()
                }

                override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                    onVpnStateChanged(caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
                }
            }
            connectivityManager.registerNetworkCallback(vpnRequest, vpnNetworkCallback!!)
        } catch (e: Exception) {
            Log.w(TAG, "Could not register VPN callback: ${e.message}")
        }

        try {
            defaultNetworkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    checkAndAlertVpnStatus()
                }

                override fun onLost(network: Network) {
                    checkAndAlertVpnStatus()
                }

                override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                    if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        onVpnStateChanged(true)
                    } else {
                        checkAndAlertVpnStatus()
                    }
                }
            }
            connectivityManager.registerDefaultNetworkCallback(defaultNetworkCallback!!)
        } catch (e: Exception) {
            Log.w(TAG, "Could not register default network callback: ${e.message}")
        }
    }

    private fun unregisterNetworkMonitors() {
        vpnNetworkCallback?.let { try { connectivityManager.unregisterNetworkCallback(it) } catch (_: Exception) {} }
        defaultNetworkCallback?.let { try { connectivityManager.unregisterNetworkCallback(it) } catch (_: Exception) {} }
    }

    private fun checkAndAlertVpnStatus(): Boolean {
        var isVpn = false
        try {
            val activeNetwork = connectivityManager.activeNetwork
            if (activeNetwork != null) {
                val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
                if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                    isVpn = true
                }
            }
            if (!isVpn) {
                for (network in connectivityManager.allNetworks) {
                    val caps = connectivityManager.getNetworkCapabilities(network)
                    if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        isVpn = true
                        break
                    }
                }
            }
            if (!isVpn) {
                val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
                for (iface in interfaces) {
                    if (iface.isUp) {
                        val name = iface.name.lowercase()
                        if (name.startsWith("tun") || name.startsWith("ppp") ||
                            name.startsWith("tap") || name.startsWith("wg") ||
                            name.contains("vpn") || name.startsWith("ipsec")
                        ) {
                            isVpn = true
                            break
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in VPN detection: ${e.message}")
        }

        onVpnStateChanged(isVpn)
        return isVpn
    }

    private fun onVpnStateChanged(isVpn: Boolean) {
        if (isVpn) {
            val now = System.currentTimeMillis()
            if (!isCurrentlyVpnConnected || (now - lastVpnNotificationTime > 30_000L)) {
                isCurrentlyVpnConnected = true
                lastVpnNotificationTime = now
                triggerVpnAlertNotification(isTest = false)
            }
        } else {
            if (isCurrentlyVpnConnected) {
                isCurrentlyVpnConnected = false
                notificationManager.cancel(VPN_ALERT_NOTIFICATION_ID)
            }
        }
    }

    private fun triggerVpnAlertNotification(isTest: Boolean) {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route_target", "prayer_screen")
            putExtra("active_prayer_name", "Fajr")
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            7791,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val vpnSettingsIntent = Intent(Settings.ACTION_VPN_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val vpnSettingsPendingIntent = PendingIntent.getActivity(
            this,
            7792,
            vpnSettingsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isTest) "⚠️ [TEST] VPN Connected — Allah is Watching You!" else "⚠️ VPN Connected — Allah is Watching You! (اللہ دیکھ رہا ہے)"
        val bigText = buildString {
            append("«أَلَمْ يَعْلَم بِأَنَّ اللَّهَ يَرَىٰ»\n")
            append("\"Does he not know that Allah sees?\" (Surah Al-Alaq 96:14)\n\n")
            append("«وَلَا تَقْرَبُوا الْفَوَاحِشَ مَا ظَهَرَ مِنْهَا وَمَا بَطَنَ»\n")
            append("\"Do not approach Fahisha (immorality), whether open or secret.\" (Surah Al-An'am 6:151)\n\n")
            append("Allah is fully aware of every secret and private action. Guard your soul for the sake of Allah.")
        }

        val alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(this, VPN_ALERT_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText("«أَلَمْ يَعْلَم بِأَنَّ اللَّهَ يَرَىٰ» — Does he not know that Allah sees? (96:14)")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSound(alertSound)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 800))
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Disconnect VPN", vpnSettingsPendingIntent)
            .addAction(android.R.drawable.ic_dialog_info, "Open AL-SUJOOD", openAppPendingIntent)

        notificationManager.notify(VPN_ALERT_NOTIFICATION_ID, builder.build())
    }
}
