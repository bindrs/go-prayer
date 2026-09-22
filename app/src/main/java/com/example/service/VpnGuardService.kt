package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.NetworkInterface
import java.util.Collections

/**
 * Continuous Background Service that monitors network state in real-time.
 * As soon as a VPN connection is detected, it IMMEDIATELY triggers a high-priority
 * notification reminding the user of Allah's omniscience and warnings against Fahisha.
 */
class VpnGuardService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var periodicJob: Job? = null
    private var vpnNetworkCallback: ConnectivityManager.NetworkCallback? = null
    private var defaultNetworkCallback: ConnectivityManager.NetworkCallback? = null
    private var isCurrentlyVpnConnected = false
    private var lastNotificationTime = 0L

    private val database by lazy { AppDatabase.getDatabase(applicationContext) }
    private val repository by lazy { PrayerRepository(database) }
    private var latestSchedule: DayPrayerSchedule? = null
    private var scheduleObservationJob: Job? = null

    private val connectivityManager by lazy {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "VpnGuardService created. Starting continuous background prayer & VPN service...")
        createNotificationChannels()
        startForeground(SERVICE_NOTIFICATION_ID, createPrayerServiceNotification(null))
        observePrayerSchedule()
        registerNetworkMonitors()
        startContinuousPolling()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_TEST_ALERT) {
            triggerVpnAlertNotification(isTest = true)
        } else if (action == ACTION_REFRESH_SCHEDULE) {
            scope.launch {
                refreshOngoingNotification()
            }
        } else {
            checkAndAlertVpnStatus()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "App task removed from recents. Restarting VpnGuardService to ensure continuous protection...")
        val restartServiceIntent = Intent(applicationContext, VpnGuardService::class.java).apply {
            setPackage(packageName)
        }
        try {
            val restartPendingIntent = PendingIntent.getService(
                applicationContext,
                10088,
                restartServiceIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.set(
                android.app.AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 1000,
                restartPendingIntent
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule restart: ${e.message}")
        }
    }

    override fun onDestroy() {
        periodicJob?.cancel()
        scheduleObservationJob?.cancel()
        unregisterNetworkMonitors()
        super.onDestroy()
        Log.d(TAG, "VpnGuardService destroyed.")
    }

    private fun observePrayerSchedule() {
        scheduleObservationJob?.cancel()
        scheduleObservationJob = scope.launch {
            repository.scheduleFlow.collectLatest { schedule ->
                latestSchedule = schedule
                notificationManager.notify(SERVICE_NOTIFICATION_ID, createPrayerServiceNotification(schedule))
            }
        }
    }

    private suspend fun refreshOngoingNotification() {
        try {
            val settings = repository.getSettings()
            val location = repository.getLocation()
            val configs = repository.getPrayerConfigurations()
            val schedule = repository.calculateDaySchedule(
                java.util.Calendar.getInstance(),
                location,
                settings,
                configs
            )
            latestSchedule = schedule
            notificationManager.notify(SERVICE_NOTIFICATION_ID, createPrayerServiceNotification(schedule))
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing ongoing notification: ${e.message}")
        }
    }

    private fun registerNetworkMonitors() {
        try {
            val vpnRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
                .build()

            vpnNetworkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d(TAG, "VPN network callback: ON_AVAILABLE -> Triggering immediate alert!")
                    onVpnStateChanged(true)
                }

                override fun onLost(network: Network) {
                    Log.d(TAG, "VPN network callback: ON_LOST -> Dismissing alert")
                    checkAndAlertVpnStatus()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    val hasVpn = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                    onVpnStateChanged(hasVpn)
                }
            }

            connectivityManager.registerNetworkCallback(vpnRequest, vpnNetworkCallback!!)
        } catch (e: Exception) {
            Log.w(TAG, "Could not register VPN network callback: ${e.message}")
        }

        try {
            defaultNetworkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    checkAndAlertVpnStatus()
                }

                override fun onLost(network: Network) {
                    checkAndAlertVpnStatus()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    val hasVpn = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                    if (hasVpn) {
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
        vpnNetworkCallback?.let {
            try { connectivityManager.unregisterNetworkCallback(it) } catch (_: Exception) {}
        }
        defaultNetworkCallback?.let {
            try { connectivityManager.unregisterNetworkCallback(it) } catch (_: Exception) {}
        }
    }

    private fun startContinuousPolling() {
        periodicJob?.cancel()
        periodicJob = scope.launch {
            while (isActive) {
                checkAndAlertVpnStatus()

                // Check active prayer window continuously in background
                try {
                    val (inPrayerTime, prayerName) = SalahAppBlockerService.isCurrentlyInPrayerTime(applicationContext)
                    if (inPrayerTime && !SalahAppBlockerService.isUserGoingToPray) {
                        if (!SalahAppBlockerService.isBlockerActive) {
                            SalahAppBlockerService.activateBlocker(applicationContext, prayerName)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error in continuous prayer polling: ${e.message}")
                }

                // Keep the background ongoing notification updated with real-time countdown & schedule
                notificationManager.notify(SERVICE_NOTIFICATION_ID, createPrayerServiceNotification(latestSchedule))
                delay(30_000L) // update every 30 seconds
            }
        }
    }

    private fun checkAndAlertVpnStatus(): Boolean {
        var isVpn = false
        try {
            // Check 1: Active network capabilities
            val activeNetwork = connectivityManager.activeNetwork
            if (activeNetwork != null) {
                val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
                if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                    isVpn = true
                }
            }

            // Check 2: All networks
            if (!isVpn) {
                for (network in connectivityManager.allNetworks) {
                    val caps = connectivityManager.getNetworkCapabilities(network)
                    if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        isVpn = true
                        break
                    }
                }
            }

            // Check 3: Network interface names (tun, tap, ppp, wg, vpn, ipsec)
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
            // If newly connected or if 30 seconds have passed since last alert
            if (!isCurrentlyVpnConnected || (now - lastNotificationTime > 30_000L)) {
                isCurrentlyVpnConnected = true
                lastNotificationTime = now
                triggerVpnAlertNotification(isTest = false)
            }
        } else {
            if (isCurrentlyVpnConnected) {
                isCurrentlyVpnConnected = false
                dismissVpnAlertNotification()
            }
        }
    }

    private fun triggerVpnAlertNotification(isTest: Boolean) {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "GoPrayer:VpnAlertWakeLock"
        ).apply {
            acquire(5000L)
        }

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_VPN_ALERT, true)
            putExtra("route_target", "prayer_screen")
            putExtra("active_prayer_name", "Fajr")
        }

        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            20091,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val vpnSettingsIntent = Intent(Settings.ACTION_VPN_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val vpnSettingsPendingIntent = PendingIntent.getActivity(
            this,
            20092,
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
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Disconnect VPN",
                vpnSettingsPendingIntent
            )
            .addAction(
                android.R.drawable.ic_dialog_info,
                "Open Islamic Guard",
                openAppPendingIntent
            )

        notificationManager.notify(VPN_ALERT_NOTIFICATION_ID, builder.build())
    }

    private fun dismissVpnAlertNotification() {
        notificationManager.cancel(VPN_ALERT_NOTIFICATION_ID)
    }

    private fun createPrayerServiceNotification(schedule: DayPrayerSchedule?): Notification {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            20090,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val testAzanIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route_target", "prayer_screen")
            putExtra("active_prayer_name", schedule?.nextPrayer?.name ?: "Fajr")
        }
        val testAzanPendingIntent = PendingIntent.getActivity(
            this,
            20091,
            testAzanIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = System.currentTimeMillis()
        val nextPrayer = schedule?.nextPrayer
        val millisLeft = if (nextPrayer != null && nextPrayer.timestampMillis > now) {
            nextPrayer.timestampMillis - now
        } else 0L

        val hoursLeft = millisLeft / (1000 * 60 * 60)
        val minsLeft = (millisLeft % (1000 * 60 * 60)) / (1000 * 60)
        val countdownStr = if (hoursLeft > 0) "${hoursLeft}h ${minsLeft}m" else "${minsLeft}m"

        val title = if (nextPrayer != null) {
            "🕌 AL-SUJOOD • Next: ${nextPrayer.name} at ${nextPrayer.timeFormatted} (in $countdownStr)"
        } else {
            "🕌 AL-SUJOOD • Continuous Background Service"
        }

        val locName = schedule?.locationName ?: "AL-SUJOOD"
        val prayerSummary = schedule?.prayers?.filter { it.name != "Sunrise" }?.joinToString(" | ") { p ->
            "${p.name}: ${p.timeFormatted}"
        } ?: "Fajr, Dhuhr, Asr, Maghrib, Isha"

        val bigText = StringBuilder().apply {
            append("🕌 Today's Prayer Timetable — $locName\n")
            schedule?.prayers?.forEach { p ->
                val marker = if (p.name == nextPrayer?.name) "▶ " else "• "
                append("$marker${p.name}: ${p.timeFormatted} (Azan: ${p.azanTimeFormatted} | Jama'at: ${p.jamatTimeFormatted})\n")
            }
            append("\n📅 ${schedule?.hijriDateString ?: ""}\n")
            append("🛡️ Background Islamic Guard & Automatic Salah Alerts Active")
        }.toString()

        return NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText("📍 $locName • $prayerSummary")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "Open AL-SUJOOD", pendingIntent)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Prayer View", testAzanPendingIntent)
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 1. Silent Ongoing Service Channel
            val serviceChannel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                "AL-SUJOOD Background & Timetable Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Displays all-time prayer schedule, upcoming prayer countdown, and Islamic guard in background"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(serviceChannel)

            // 2. High-Priority Heads-Up VPN Fahisha Alert Channel
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            val alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val alertChannel = NotificationChannel(
                VPN_ALERT_CHANNEL_ID,
                "VPN Fahisha & Taqwa Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Immediate heads-up alert whenever a VPN connection is activated"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 800)
                setSound(alertSound, audioAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
                setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    companion object {
        private const val TAG = "VpnGuardService"
        const val SERVICE_CHANNEL_ID = "go_prayer_vpn_guard_service_channel"
        const val VPN_ALERT_CHANNEL_ID = "go_prayer_vpn_fahisha_alert_channel"
        const val SERVICE_NOTIFICATION_ID = 10080
        const val VPN_ALERT_NOTIFICATION_ID = 10099
        const val EXTRA_OPEN_VPN_ALERT = "extra_open_vpn_alert"
        const val ACTION_TEST_ALERT = "com.example.goprayer.ACTION_TEST_VPN_ALERT"
        const val ACTION_REFRESH_SCHEDULE = "com.example.goprayer.ACTION_REFRESH_SCHEDULE"

        fun startService(context: Context) {
            try {
                val intent = Intent(context, VpnGuardService::class.java)
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start VpnGuardService: ${e.message}")
            }
        }

        fun refreshOngoingNotification(context: Context) {
            try {
                val intent = Intent(context, VpnGuardService::class.java).apply {
                    action = ACTION_REFRESH_SCHEDULE
                }
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send refresh schedule intent: ${e.message}")
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, VpnGuardService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop VpnGuardService: ${e.message}")
            }
        }

        fun sendTestAlert(context: Context) {
            try {
                val intent = Intent(context, VpnGuardService::class.java).apply {
                    action = ACTION_TEST_ALERT
                }
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send test alert: ${e.message}")
            }
        }
    }
}
