package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.MainActivity
import com.example.util.LockScreenHelper

/**
 * BroadcastReceiver triggered when the user unlocks their device (ACTION_USER_PRESENT).
 * It presents the Prayer Screen if a prayer time arrived while the screen was locked.
 */
class DeviceUnlockReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "DeviceUnlockReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_USER_PRESENT) {
            Log.d(TAG, "Device unlocked by user (ACTION_USER_PRESENT). Checking for pending prayer prompts...")

            // Check if user already marked 'going to pray'
            if (SalahAppBlockerService.isUserGoingToPray) {
                Log.d(TAG, "User has marked going to pray. Clearing pending prompts.")
                LockScreenHelper.clearPendingPrayerPrompt(context)
                return
            }

            val pendingPrayer = LockScreenHelper.getAndClearPendingPrayerPrompt(context)
            if (!pendingPrayer.isNullOrBlank()) {
                Log.d(TAG, "Displaying deferred prayer screen for $pendingPrayer after user unlocked device.")
                try {
                    val prayerIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("route_target", "prayer_screen")
                        putExtra("active_prayer_name", pendingPrayer)
                    }
                    context.startActivity(prayerIntent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to launch MainActivity on device unlock: ${e.message}", e)
                }
            }
        }
    }
}
