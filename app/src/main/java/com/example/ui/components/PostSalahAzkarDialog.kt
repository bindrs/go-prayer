package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.azkaar.AzkarItem
import com.example.ui.azkaar.postSalahAzkarList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PostSalahAzkarDialog(
    prayerName: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("islamic_app_azkaar_prefs", Context.MODE_PRIVATE) }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    var showAllahWatchingPopup by remember { mutableStateOf(false) }

    // TTS Engine for Arabic Pronunciation
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsEngine?.language = Locale("ar")
                isTtsReady = true
            }
        }
        ttsEngine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun playTts(text: String) {
        ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "post_salah_tts")
    }

    fun vibrateFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    // Counts Map for postSalahAzkarList
    val counts = remember { mutableStateMapOf<Int, Int>() }

    LaunchedEffect(Unit) {
        postSalahAzkarList.forEach { item ->
            val saved = prefs.getInt("azkaar_${todayStr}_post_salah_${item.id}", 0)
            counts[item.id] = saved
        }
    }

    val scrollState = rememberScrollState()

    // Calculate scroll progress percentage (0.0f to 1.0f)
    val scrollProgress by remember {
        derivedStateOf {
            if (scrollState.maxValue > 0) {
                (scrollState.value.toFloat() / scrollState.maxValue.toFloat()).coerceIn(0f, 1f)
            } else {
                1f
            }
        }
    }

    val isScrolledToBottom by remember {
        derivedStateOf {
            scrollState.value >= (scrollState.maxValue - 50) || scrollState.maxValue == 0
        }
    }

    val completedCount = postSalahAzkarList.count { item -> (counts[item.id] ?: 0) >= item.targetCount }
    val totalCount = postSalahAzkarList.size

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, Color(0xFFF59E0B), RoundedCornerShape(24.dp)),
            color = Color(0xFF064E3B)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF022C22), Color(0xFF064E3B), Color(0xFF047857))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF022C22),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "أَذْكَارُ مَا بَعْدَ الصَّلَاةِ",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color(0xFFFDFBF7),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                )
                                Text(
                                    text = "Authentic Post-Salah Azkar • $prayerName",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFA7F3D0),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFFD1D5DB)
                            )
                        }
                    }
                }

                // Progress Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF022C22))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Completed: $completedCount of $totalCount Azkaar",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFD1D5DB),
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "${(scrollProgress * 100).toInt()}% Scrolled",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isScrolledToBottom) Color(0xFF10B981) else Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = {
                            if (totalCount > 0) (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) else scrollProgress
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (completedCount == totalCount) Color(0xFF10B981) else Color(0xFFF59E0B),
                        trackColor = Color(0xFF065F46)
                    )
                }

                // Scrollable List of Authentic Azkar Cards
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    postSalahAzkarList.forEach { item ->
                        val currentCount = counts[item.id] ?: 0
                        val isDone = currentCount >= item.targetCount

                        AzkarInteractiveCard(
                            item = item,
                            currentCount = currentCount,
                            isCompleted = isDone,
                            onIncrement = {
                                val nextCount = currentCount + 1
                                counts[item.id] = nextCount
                                prefs.edit().putInt("azkaar_${todayStr}_post_salah_${item.id}", nextCount).apply()
                                vibrateFeedback()
                            },
                            onReset = {
                                counts[item.id] = 0
                                prefs.edit().putInt("azkaar_${todayStr}_post_salah_${item.id}", 0).apply()
                                vibrateFeedback()
                            },
                            onPlayAudio = {
                                playTts(item.arabicText)
                            }
                        )
                    }
                }

                // Bottom Action Button Container
                Surface(
                    color = Color(0xFF022C22),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                // Mark all as completed in prefs
                                prefs.edit().putBoolean("azkaar_${todayStr}_post_salah_completed", true).apply()
                                showAllahWatchingPopup = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("i_read_all_azkar_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF59E0B),
                                contentColor = Color(0xFF022C22)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "I Have Read All Azkar (میں نے تمام اذکار پڑھ لیے)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Final Popup: "Allah is Watching You" (أَلَمْ يَعْلَم بِأَنَّ اللَّهَ يَرَىٰ)
    if (showAllahWatchingPopup) {
        AlertDialog(
            onDismissRequest = {
                showAllahWatchingPopup = false
                onDismiss()
            },
            containerColor = Color(0xFF022C22),
            titleContentColor = Color(0xFFFDFBF7),
            textContentColor = Color(0xFFD1D5DB),
            shape = RoundedCornerShape(20.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF065F46)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveRedEye,
                        contentDescription = "Allah Sees All",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "أَلَمْ يَعْلَم بِأَنَّ اللَّهَ يَرَىٰ",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color(0xFFF59E0B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "“Does he not know that Allah sees?”",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF9CA3AF),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                    Text(
                        text = "Surah Al-Alaq (96:14)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF10B981),
                            fontSize = 11.sp
                        )
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تقبل الله صلاتكم وطاعاتكم",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFFFDFBF7),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "May Allah accept your Salah and Azkar. Always remember that Allah sees and knows everything you do in secret and in public. Keep your heart attached to prayer and upright conduct.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFD1D5DB),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAllahWatchingPopup = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("آمین (Aameen)", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        )
    }
}

@Composable
private fun AzkarInteractiveCard(
    item: AzkarItem,
    currentCount: Int,
    isCompleted: Boolean,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onPlayAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("azkar_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color(0xFF047857).copy(alpha = 0.35f) else Color(0xFF022C22)
        ),
        border = BorderStroke(1.dp, if (isCompleted) Color(0xFF10B981) else Color(0xFF065F46))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with Reference & Audio & Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFF065F46),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.reference,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFF59E0B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPlayAudio,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Listen",
                            tint = Color(0xFFA7F3D0),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    if (currentCount > 0) {
                        IconButton(
                            onClick = onReset,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset Count",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isCompleted) Color(0xFF047857) else Color(0xFFD97706)
                    ) {
                        Text(
                            text = "$currentCount / ${item.targetCount}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Arabic Text
            Text(
                text = item.arabicText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFFFDFBF7),
                    fontSize = 19.sp,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.Right,
                    textDirection = TextDirection.Rtl,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onIncrement() }
            )

            // English Translation
            if (item.englishTranslation.isNotBlank()) {
                Text(
                    text = item.englishTranslation,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Urdu Translation
            if (!item.urduTranslation.isNullOrBlank()) {
                Text(
                    text = item.urduTranslation,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFA7F3D0),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Right,
                        textDirection = TextDirection.Rtl
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Virtue / Note
            val note = item.noteUr ?: item.noteEn
            if (!note.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF064E3B),
                    border = BorderStroke(1.dp, Color(0xFF059669))
                ) {
                    Text(
                        text = "💡 $note",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFFDE68A),
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Tap to count button for repetitive azkaar (e.g. 33x, 34x, 3x)
            if (item.targetCount > 1) {
                Button(
                    onClick = onIncrement,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) Color(0xFF047857) else Color(0xFFD97706),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCompleted) "Completed (${item.targetCount}x) - Tap to Add More" else "Tap Count: $currentCount / ${item.targetCount}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
