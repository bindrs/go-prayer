package com.example.service

import android.content.Context
import android.media.AudioManager
import android.util.Log

class QuietModeManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    companion object {
        private const val TAG = "QuietModeManager"
        private var savedRingerMode: Int? = null
        private var savedRingVolume: Int? = null
        private var savedNotificationVolume: Int? = null
    }

    fun isDndPermissionGranted(): Boolean {
        // Return true since we no longer rely on system DND policy access
        return true
    }

    /**
     * Activates prayer silent mode without requiring DND policy access
     */
    fun activateQuietMode(prayerName: String, durationMinutes: Int): String {
        return try {
            if (savedRingerMode == null) {
                savedRingerMode = audioManager.ringerMode
            }
            try {
                savedRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)
                savedNotificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            } catch (_: Exception) {}

            var success = false
            // 1. Attempt Silent ringer mode
            try {
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                success = true
                Log.d(TAG, "Ringer set to SILENT for $prayerName")
            } catch (e: Exception) {
                Log.w(TAG, "Could not set SILENT mode directly: ${e.message}")
            }

            // 2. Fallback to VIBRATE mode if SILENT requires policy on certain OEM devices
            if (!success) {
                try {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    success = true
                    Log.d(TAG, "Ringer set to VIBRATE for $prayerName")
                } catch (e: Exception) {
                    Log.w(TAG, "Could not set VIBRATE mode: ${e.message}")
                }
            }

            // 3. Mute ring/notification streams if ringer mode didn't change
            if (!success) {
                try {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)
                    audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0)
                    success = true
                } catch (_: Exception) {}
            }

            if (success) "SILENT_ACTIVATED" else "SILENT_SKIPPED"
        } catch (e: Exception) {
            Log.e(TAG, "Error activating silent mode: ${e.message}")
            "ERROR_${e.javaClass.simpleName}"
        }
    }

    /**
     * Restores previous ringer mode and volume settings
     */
    fun restorePreviousState(): String {
        return try {
            var restoredDetails = ""
            if (savedRingerMode != null) {
                try {
                    audioManager.ringerMode = savedRingerMode!!
                    restoredDetails += "Ringer Restored to $savedRingerMode; "
                    savedRingerMode = null
                } catch (e: Exception) {
                    Log.w(TAG, "Could not restore ringer mode: ${e.message}")
                }
            }
            if (savedRingVolume != null) {
                try {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, savedRingVolume!!, 0)
                    savedRingVolume = null
                } catch (_: Exception) {}
            }
            if (savedNotificationVolume != null) {
                try {
                    audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, savedNotificationVolume!!, 0)
                    savedNotificationVolume = null
                } catch (_: Exception) {}
            }
            if (restoredDetails.isEmpty()) "NO_PREVIOUS_STATE" else restoredDetails
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring quiet mode: ${e.message}")
            "ERROR_RESTORE_${e.javaClass.simpleName}"
        }
    }
}

