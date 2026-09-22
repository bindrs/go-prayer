package com.example

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.navigation.compose.rememberNavController
import com.example.navigation.GoPrayerNavGraph
import com.example.navigation.Screen
import com.example.ui.theme.GoPrayerTheme
import com.example.ui.viewmodel.PrayerViewModel
import com.example.ui.viewmodel.PrayerViewModelFactory

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.service.PrayerAlarmReceiver
import com.example.service.PrayerForegroundService

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: PrayerViewModel
    private var pendingPrayerRoute by mutableStateOf<String?>(null)
    private var pendingGeneralRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val factory = PrayerViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory)[PrayerViewModel::class.java]

        handleIncomingIntent(intent)

        // Asynchronously initialize notification channels and background alarms so UI renders instantly
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            PrayerAlarmReceiver.createNotificationChannels(this@MainActivity)
            com.example.service.AzkaarReminderManager.scheduleAllAzkaarReminders(this@MainActivity)
            PrayerForegroundService.startService(this@MainActivity)
            try {
                com.example.service.VpnGuardService.stopService(this@MainActivity)
            } catch (_: Exception) {}
        }

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val navController = rememberNavController()

            // Request Notification Permission on Android 13+ if not granted
            val notificationLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* permission result handled */ }

            // Request Calendar Permissions automatically so prayer offerings auto-add to device Calendar
            val calendarPermissionsLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { /* calendar permission results handled */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                // Request calendar permissions if not granted yet
                val writeCal = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.WRITE_CALENDAR)
                val readCal = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_CALENDAR)
                if (writeCal != PackageManager.PERMISSION_GRANTED || readCal != PackageManager.PERMISSION_GRANTED) {
                    calendarPermissionsLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_CALENDAR,
                            Manifest.permission.WRITE_CALENDAR
                        )
                    )
                }
            }

            val isDark = when (settings.theme) {
                "dark" -> true
                else -> false
            }

            // Respond to incoming new intents (e.g. alarm triggering while activity is running/backgrounded)
            LaunchedEffect(pendingPrayerRoute) {
                pendingPrayerRoute?.let { prayerName ->
                    navController.navigate(Screen.Prayer.createRoute(prayerName)) {
                        launchSingleTop = true
                    }
                    pendingPrayerRoute = null
                }
            }

            LaunchedEffect(pendingGeneralRoute) {
                pendingGeneralRoute?.let { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                    pendingGeneralRoute = null
                }
            }

            GoPrayerTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val initialRoute = intent?.getStringExtra("route_target")
                    val initialPrayerName = intent?.getStringExtra("active_prayer_name")

                    GoPrayerNavGraph(
                        navController = navController,
                        viewModel = viewModel,
                        isOnboardingCompleted = settings.isOnboardingCompleted,
                        initialRoute = initialRoute,
                        initialPrayerName = initialPrayerName
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.refreshTodayDate()
            viewModel.refreshJsonStorageInfo()
        }

        // Check if there was a deferred prayer prompt from when the device was locked
        val pendingPrayer = com.example.util.LockScreenHelper.getAndClearPendingPrayerPrompt(this)
        if (!pendingPrayer.isNullOrBlank()) {
            pendingPrayerRoute = pendingPrayer
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val routeTarget = intent.getStringExtra("route_target")
        val activePrayerName = intent.getStringExtra("active_prayer_name")
        if (routeTarget == "prayer_screen" && !activePrayerName.isNullOrBlank()) {
            pendingPrayerRoute = activePrayerName
        } else if (routeTarget == "azkaar_screen") {
            pendingGeneralRoute = Screen.Azkaar.route
        }
    }
}
