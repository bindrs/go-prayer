package com.example.domain.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import com.example.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class AdhanAudioEngine(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private var syntheticTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    @Volatile
    var isPlaying: Boolean = false
        private set

    companion object {
        private const val TAG = "AdhanAudioEngine"
        private const val SAMPLE_RATE = 44100
    }

    fun playTrack(
        trackId: String,
        localUri: String? = null,
        volumeFactor: Float = 1.0f,
        onProgress: ((progressPercent: Float) -> Unit)? = null,
        onCompletion: (() -> Unit)? = null
    ) {
        stopPlayback()
        requestAudioFocus()

        playbackJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                isPlaying = true
                Log.d(TAG, "Starting playback for trackId: $trackId, uri: $localUri with volume: $volumeFactor")

                // 1. Check if a direct file or custom URI exists
                val customFile = resolveCustomAudioFile(trackId, localUri)
                if (customFile != null && customFile.exists() && customFile.length() > 0) {
                    playFromFile(customFile, volumeFactor, onProgress, onCompletion)
                    return@launch
                }

                // 2. Check if a raw resource exists in APK
                val resourceId = when (trackId.lowercase()) {
                    "makkah_adhan", "makkah" -> R.raw.makkah_adhan
                    "alaqsa_adhan", "alaqsa", "al_aqsa" -> R.raw.alaqsa_adhan
                    "adhan_audio", "default_adhan", "adhan" -> R.raw.adhan_audio
                    "gentle_tone", "gentle" -> R.raw.gentle_tone
                    "soft_takbeer", "takbeer" -> R.raw.soft_takbeer
                    else -> 0
                }
                if (resourceId != 0) {
                    playFromRaw(resourceId, volumeFactor, onProgress, onCompletion)
                } else {
                    // 3. Harmonious spiritual tone synthesis tailored to the selected track
                    playSynthesizedAdhan(trackId, volumeFactor, onProgress, onCompletion)
                }
            } catch (e: CancellationException) {
                Log.d(TAG, "Playback stopped normally")
            } catch (e: Exception) {
                Log.e(TAG, "Playback error: ${e.message}", e)
                onCompletion?.invoke()
            } finally {
                isPlaying = false
                abandonAudioFocus()
            }
        }
    }

    private fun resolveCustomAudioFile(trackId: String, localUri: String?): java.io.File? {
        if (!localUri.isNullOrBlank()) {
            val cleanPath = if (localUri.startsWith("file://")) localUri.substring(7) else localUri
            val file = java.io.File(cleanPath)
            if (file.exists() && file.length() > 0) return file
        }

        val adhansDir = java.io.File(context.filesDir, "custom_adhans")
        if (adhansDir.exists() && adhansDir.isDirectory) {
            val directMatch = java.io.File(adhansDir, trackId)
            if (directMatch.exists() && directMatch.length() > 0) return directMatch

            // Search for files matching trackId with any audio/video extension
            val matching = adhansDir.listFiles { _, name ->
                name.startsWith(trackId) || name.contains(trackId) || trackId.contains(name.substringBeforeLast('.'))
            }
            if (!matching.isNullOrEmpty()) {
                val valid = matching.firstOrNull { it.length() > 0 }
                if (valid != null) return valid
            }
        }
        return null
    }

    private suspend fun playFromFile(
        file: java.io.File,
        volume: Float,
        onProgress: ((Float) -> Unit)?,
        onCompletion: (() -> Unit)?
    ) {
        var player: MediaPlayer? = null
        var fis: java.io.FileInputStream? = null
        try {
            val mp = MediaPlayer()
            fis = java.io.FileInputStream(file)
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .build()
            )
            mp.setDataSource(fis.fd)
            mp.prepare()
            mp.setVolume(volume, volume)
            mp.start()
            player = mp
            mediaPlayer = mp

            val maxDurationMs = 180_000L // Play full Azan or up to 3 minutes

            while (isPlaying && coroutineContext.isActive && mp.isPlaying) {
                val current = mp.currentPosition
                val total = mp.duration.toLong().coerceAtLeast(1L)
                val effectiveTotal = Math.min(total, maxDurationMs).coerceAtLeast(1L)
                onProgress?.invoke((current.toFloat() / effectiveTotal).coerceIn(0f, 1f))

                if (current >= maxDurationMs) {
                    Log.d(TAG, "Custom Azan playback completed")
                    break
                }
                delay(200)
            }
            if (isPlaying && coroutineContext.isActive) {
                onCompletion?.invoke()
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.e(TAG, "Failed playing file ${file.absolutePath}: ${e.message}, falling back to synthesis", e)
            playSynthesizedAdhan("makkah_adhan", volume, onProgress, onCompletion)
        } finally {
            try {
                if (player?.isPlaying == true) {
                    player.stop()
                }
                player?.release()
                fis?.close()
            } catch (_: Exception) {}
            if (mediaPlayer === player) {
                mediaPlayer = null
            }
        }
    }

    private suspend fun playFromRaw(
        resourceId: Int,
        volume: Float,
        onProgress: ((Float) -> Unit)?,
        onCompletion: (() -> Unit)?
    ) {
        var player: MediaPlayer? = null
        var afd: android.content.res.AssetFileDescriptor? = null
        try {
            val mp = MediaPlayer()
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .build()
            )
            afd = context.resources.openRawResourceFd(resourceId)
            if (afd != null) {
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                mp.prepare()
                mp.setVolume(volume, volume)
                mp.start()
                player = mp
                mediaPlayer = mp
            } else {
                val fallbackMp = MediaPlayer.create(context, resourceId)
                if (fallbackMp != null) {
                    fallbackMp.setVolume(volume, volume)
                    fallbackMp.start()
                    player = fallbackMp
                    mediaPlayer = fallbackMp
                } else {
                    playSynthesizedAdhan("makkah_adhan", volume, onProgress, onCompletion)
                    return
                }
            }

            val maxDurationMs = 20_000L // Play first 20 seconds of Azan as requested

            while (isPlaying && coroutineContext.isActive && (player?.isPlaying == true)) {
                val current = player.currentPosition
                val effectiveTotal = Math.min(player.duration.toLong(), maxDurationMs).coerceAtLeast(1L)
                onProgress?.invoke((current.toFloat() / effectiveTotal).coerceIn(0f, 1f))

                if (current >= maxDurationMs) {
                    Log.d(TAG, "Adhan completed 20-second recitation limit")
                    break
                }
                delay(200)
            }
            if (isPlaying && coroutineContext.isActive) {
                onCompletion?.invoke()
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.e(TAG, "Failed playing raw resource $resourceId: ${e.message}, falling back to synthesis", e)
            playSynthesizedAdhan("makkah_adhan", volume, onProgress, onCompletion)
        } finally {
            try {
                if (player?.isPlaying == true) {
                    player.stop()
                }
                player?.release()
                afd?.close()
            } catch (_: Exception) {}
            if (mediaPlayer === player) {
                mediaPlayer = null
            }
        }
    }

    /**
     * Synthesizes melodious Islamic Adhan harmonics using PCM audio.
     * Generates pure acoustic tones mimicking peaceful Maqam notes (Bayati/Rast prayer call intervals).
     */
    private suspend fun playSynthesizedAdhan(
        trackId: String,
        volume: Float,
        onProgress: ((Float) -> Unit)?,
        onCompletion: (() -> Unit)?
    ) {
        // Melodic notes sequence (frequencies in Hz): F4, G4, A4, Bb4, C5, D5 (Bayati/Rast scale)
        val melodyNotes = when (trackId) {
            "soft_takbeer" -> listOf(
                Pair(349.23, 1.2), // Allahu
                Pair(440.00, 1.5), // Akbar
                Pair(392.00, 1.0),
                Pair(349.23, 1.8)
            )
            "gentle_tone" -> listOf(
                Pair(523.25, 0.8), // C5
                Pair(659.25, 0.8), // E5
                Pair(783.99, 1.5)  // G5
            )
            "alaqsa_adhan" -> listOf(
                Pair(349.23, 1.0),
                Pair(440.00, 1.2),
                Pair(466.16, 1.4),
                Pair(523.25, 1.5),
                Pair(466.16, 1.0),
                Pair(440.00, 1.2),
                Pair(349.23, 2.0)
            )
            else -> listOf(
                // Full spiritual Adhan motif
                Pair(349.23, 1.2), // Al-
                Pair(392.00, 1.0), // la-
                Pair(440.00, 1.8), // hu Akbar
                Pair(392.00, 1.0),
                Pair(349.23, 1.5),
                Pair(349.23, 1.2), // Al-
                Pair(440.00, 1.5), // la-
                Pair(466.16, 1.8), // hu Akbar
                Pair(523.25, 1.2), // Ash-hadu
                Pair(466.16, 1.0), // an la
                Pair(440.00, 1.5), // ilaha
                Pair(392.00, 1.2), // ill-
                Pair(349.23, 2.2)  // Allah
            )
        }

        val totalDurationSeconds = melodyNotes.sumOf { it.second }
        val bufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            android.media.AudioFormat.CHANNEL_OUT_MONO,
            android.media.AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(SAMPLE_RATE * 2)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .build()
            )
            .setAudioFormat(
                android.media.AudioFormat.Builder()
                    .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        syntheticTrack = track

        try {
            track.play()

            var elapsedSeconds = 0.0
            for (note in melodyNotes) {
                if (!isPlaying || !coroutineContext.isActive) break

                val freq = note.first
                val durationSec = note.second
                val sampleCount = (durationSec * SAMPLE_RATE).toInt()
                val samples = ShortArray(sampleCount)

                for (i in 0 until sampleCount) {
                    val t = i.toDouble() / SAMPLE_RATE
                    // Smooth bell envelope (attack + sustain + exponential decay)
                    val envelope = sin((PI * i) / sampleCount) * exp(-0.8 * t)
                    // Fundamental + rich warm harmonics (second & third harmonic for acoustic depth)
                    val wave = sin(2.0 * PI * freq * t) +
                            0.4 * sin(4.0 * PI * freq * t) +
                            0.2 * sin(6.0 * PI * freq * t)

                    val amplitude = (wave * envelope * volume * 22000.0).coerceIn(-32767.0, 32767.0)
                    samples[i] = amplitude.toInt().toShort()
                }

                try {
                    if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        track.write(samples, 0, samples.size)
                    }
                } catch (_: IllegalStateException) {
                    break
                }

                elapsedSeconds += durationSec
                onProgress?.invoke((elapsedSeconds / totalDurationSeconds).toFloat().coerceIn(0f, 1f))
                delay(40)
            }

            if (isPlaying && coroutineContext.isActive) {
                onCompletion?.invoke()
            }
        } finally {
            try {
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
                track.release()
            } catch (_: Exception) {}
            if (syntheticTrack === track) {
                syntheticTrack = null
            }
        }
    }

    fun stopPlayback() {
        isPlaying = false
        val job = playbackJob
        playbackJob = null
        job?.cancel()

        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null

        try {
            syntheticTrack?.let { track ->
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
                track.release()
            }
        } catch (_: Exception) {}
        syntheticTrack = null

        abandonAudioFocus()
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .build()
                )
                .build()
            audioFocusRequest?.let { audioManager.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }
}
