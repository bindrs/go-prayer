package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "System event received: $action. Rescheduling prayer alarms...")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            PrayerScheduler(context).scheduleUpcomingPrayers { count ->
                Log.d(TAG, "Rescheduled $count prayer alarms successfully after $action")
            }
            AzkaarReminderManager.scheduleAllAzkaarReminders(context)
            PrayerForegroundService.startService(context)
            try {
                VpnGuardService.stopService(context)
            } catch (_: Exception) {}
        }
    }
}
