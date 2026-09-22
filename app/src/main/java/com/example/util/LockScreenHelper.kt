package com.example.util

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.example.MainActivity

object LockScreenHelper {

    private const val TAG = "LockScreenHelper"
    private const val PREFS_NAME = "go_prayer_lock_state"
    private const val KEY_PENDING_PRAYER = "pending_prayer_name"
    private const val KEY_PENDING_TIMESTAMP = "pending_prayer_timestamp"

    /**
     * Checks if the device screen is currently locked (keyguard active or screen turned off).
     */
    fun isDeviceLocked(context: Context): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

        val isScreenOff = powerManager?.isInteractive == false
        val isKeyguardLocked = keyguardManager?.isKeyguardLocked == true

        return isScreenOff || isKeyguardLocked
    }

    /**
     * Stores a pending prayer prompt when an alarm or Azan arrives while the device is locked.
     */
    fun setPendingPrayerPrompt(context: Context, prayerName: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_PENDING_PRAYER, prayerName)
            .putLong(KEY_PENDING_TIMESTAMP, System.currentTimeMillis())
            .apply()
        Log.d(TAG, "Saved pending prayer prompt for: $prayerName (device is locked)")
    }

    /**
     * Retrieves and clears the pending prayer prompt if it occurred recently (within 45 minutes).
     */
    fun getAndClearPendingPrayerPrompt(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_PENDING_PRAYER, null)
        val time = prefs.getLong(KEY_PENDING_TIMESTAMP, 0L)

        // Clear stored pending prayer
        prefs.edit()
            .remove(KEY_PENDING_PRAYER)
            .remove(KEY_PENDING_TIMESTAMP)
            .apply()

        val now = System.currentTimeMillis()
        val isRecent = (now - time) < (45 * 60 * 1000L)

        return if (!name.isNullOrBlank() && isRecent) {
            name
        } else {
            null
        }
    }

    /**
     * Checks if there is an active pending prayer prompt without clearing it.
     */
    fun peekPendingPrayerPrompt(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_PENDING_PRAYER, null)
        val time = prefs.getLong(KEY_PENDING_TIMESTAMP, 0L)
        val now = System.currentTimeMillis()
        val isRecent = (now - time) < (45 * 60 * 1000L)
        return if (!name.isNullOrBlank() && isRecent) name else null
    }

    /**
     * Clears any pending prayer prompt.
     */
    fun clearPendingPrayerPrompt(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_PENDING_PRAYER)
            .remove(KEY_PENDING_TIMESTAMP)
            .apply()
    }

    /**
     * Launches the PrayerScreen if device is unlocked, or schedules it for when the user unlocks.
     */
    fun handlePrayerArrivalDisplay(context: Context, prayerName: String) {
        if (isDeviceLocked(context)) {
            // When device is locked with power button / keyguard: show notification ONLY
            setPendingPrayerPrompt(context, prayerName)
            Log.d(TAG, "Device is locked. Prayer screen deferred until user unlocks device. Notification only.")
        } else {
            // When device is already unlocked: open the prayer screen immediately
            Log.d(TAG, "Device is unlocked. Launching prayer screen immediately for $prayerName.")
            try {
                val prayerScreenIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("route_target", "prayer_screen")
                    putExtra("active_prayer_name", prayerName)
                }
                context.startActivity(prayerScreenIntent)
            } catch (e: Exception) {
                Log.w(TAG, "Could not launch prayer screen: ${e.message}")
            }
        }
    }
}
