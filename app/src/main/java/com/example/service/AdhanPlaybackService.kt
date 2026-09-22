package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.AppDatabase
import com.example.data.local.entities.PrayerEventLog
import com.example.data.repository.PrayerRepository
import com.example.domain.audio.AdhanAudioEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdhanPlaybackService : Service() {

    private lateinit var audioEngine: AdhanAudioEngine
    private lateinit var quietModeManager: QuietModeManager
    private lateinit var repository: PrayerRepository
    private var quietModeJob: Job? = null
    private var serviceJob: Job? = null

    companion object {
        private const val TAG = "AdhanPlaybackService"
        const val CHANNEL_ID = "go_prayer_adhan_channel"
        const val NOTIFICATION_ID = 9991

        const val ACTION_START_ADHAN = "com.example.goprayer.ACTION_START_ADHAN"
        const val ACTION_STOP_ADHAN = "com.example.goprayer.ACTION_STOP_ADHAN"
        const val ACTION_RESTORE_QUIET = "com.example.goprayer.ACTION_RESTORE_QUIET"

        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_AUDIO_TRACK_ID = "extra_audio_track_id"
        const val EXTRA_ADHAN_ENABLED = "extra_adhan_enabled"
        const val EXTRA_QUIET_MODE_ENABLED = "extra_quiet_mode_enabled"
        const val EXTRA_QUIET_MODE_DURATION = "extra_quiet_mode_duration"

        @Volatile
        var isServiceRunning = false
            private set

        @Volatile
        var currentActivePrayer: String? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        audioEngine = AdhanAudioEngine(applicationContext)
        quietModeManager = QuietModeManager(applicationContext)
        repository = PrayerRepository(AppDatabase.getDatabase(applicationContext))
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_ADHAN

        when (action) {
            ACTION_STOP_ADHAN -> {
                audioEngine.stopPlayback()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_RESTORE_QUIET -> {
                quietModeManager.restorePreviousState()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START_ADHAN -> {
                val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "Salah"
                val audioTrackId = intent?.getStringExtra(EXTRA_AUDIO_TRACK_ID) ?: "makkah_adhan"
                val adhanEnabled = intent?.getBooleanExtra(EXTRA_ADHAN_ENABLED, true) ?: true
                val quietEnabled = intent?.getBooleanExtra(EXTRA_QUIET_MODE_ENABLED, true) ?: true
                val durationMinutes = intent?.getIntExtra(EXTRA_QUIET_MODE_DURATION, 20) ?: 20

                currentActivePrayer = prayerName
                isServiceRunning = true

                val notification = buildForegroundNotification(prayerName, adhanEnabled)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                        } else {
                            0
                        }
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }

                handlePrayerExecution(prayerName, audioTrackId, adhanEnabled, quietEnabled, durationMinutes)
            }
        }

        return START_NOT_STICKY
    }

    private fun handlePrayerExecution(
        prayerName: String,
        audioTrackId: String,
        adhanEnabled: Boolean,
        quietEnabled: Boolean,
        durationMinutes: Int
    ) {
        serviceJob?.cancel()
        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            var adhanStatus = "DISABLED"
            var quietStatus = "DISABLED"

            if (quietEnabled) {
                quietStatus = quietModeManager.activateQuietMode(prayerName, durationMinutes)
                // Schedule restoration after prayer period
                quietModeJob?.cancel()
                quietModeJob = CoroutineScope(Dispatchers.IO).launch {
                    delay(durationMinutes * 60 * 1000L)
                    quietModeManager.restorePreviousState()
                    Log.d(TAG, "Quiet mode auto-restored after $durationMinutes min")
                }
            }

            if (adhanEnabled) {
                adhanStatus = "PLAYING"
                val track = repository.getAdhanTrackById(audioTrackId)
                audioEngine.playTrack(
                    trackId = audioTrackId,
                    localUri = track?.localUri,
                    volumeFactor = 1.0f,
                    onCompletion = {
                        Log.d(TAG, "Adhan finished playing for $prayerName")
                    }
                )
            } else {
                adhanStatus = "NOTIFICATION_ONLY"
            }

            // Log event in Room database
            repository.logPrayerEvent(
                PrayerEventLog(
                    prayerName = prayerName,
                    scheduledTime = System.currentTimeMillis(),
                    triggeredAt = System.currentTimeMillis(),
                    adhanStatus = adhanStatus,
                    quietModeStatus = quietStatus,
                    screenStatus = "LAUNCHED"
                )
            )
        }
    }

    private fun buildForegroundNotification(prayerName: String, isAdhanPlaying: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route_target", "prayer_screen")
            putExtra("active_prayer_name", prayerName)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            1001,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AdhanPlaybackService::class.java).apply {
            action = ACTION_STOP_ADHAN
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1002,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🕌 Adhan: $prayerName Time")
            .setContentText("It is now time for $prayerName. Tap to enter Salah screen.")
            .setSubText("AL-SUJOOD")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Dismiss Adhan", stopPendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "Open Prayer", openAppPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Prayer Times & Adhan",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Live Adhan alerts and Salah quiet mode notifications"
                enableVibration(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(null, null) // Audio is managed smoothly by AdhanAudioEngine with audio focus
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        currentActivePrayer = null
        audioEngine.stopPlayback()
        serviceJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
