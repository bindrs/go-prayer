package com.example.ui.azkaar

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassAppBackground
import com.example.ui.theme.AccentGold
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.PrayerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzkaarScreen(
    viewModel: PrayerViewModel? = null,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val prefs = remember { context.getSharedPreferences("go_prayer_azkaar_prefs", Context.MODE_PRIVATE) }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    // Default tab based on current time: Morning (0) or Evening (1)
    val defaultTab = remember {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (currentHour >= 12) 1 else 0
    }
    var selectedTab by rememberSaveable { mutableIntStateOf(defaultTab) }

    var isVibrationEnabled by remember {
        mutableStateOf(prefs.getBoolean("azkaar_vibration_enabled", true))
    }

    var showResetDialog by remember { mutableStateOf(false) }
    var showReminderSettingsDialog by remember { mutableStateOf(false) }
    var remindersEnabled by remember {
        mutableStateOf(com.example.service.AzkaarReminderManager.isAzkaarRemindersGloballyEnabled(context))
    }

    // Vibrator instance for physical tactile feedback on phone
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun performZikrFeedback() {
        if (!isVibrationEnabled) return
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    // TextToSpeech for reciting Arabic zikr
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var speakingZikrId by remember { mutableStateOf<Int?>(null) }

    DisposableEffect(context) {
        var speech: TextToSpeech? = null
        speech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                speech?.let {
                    try {
                        it.language = Locale("ar")
                        isTtsReady = true
                    } catch (_: Exception) {}
                }
            }
        }
        tts = speech
        onDispose {
            try {
                speech?.stop()
                speech?.shutdown()
            } catch (_: Exception) {}
        }
    }

    fun speakArabic(text: String, zikrId: Int) {
        if (tts != null && isTtsReady) {
            speakingZikrId = zikrId
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "zikr_$zikrId")
        }
    }

    // Selected list based on tab
    val currentAzkarList = when (selectedTab) {
        0 -> morningAzkarList
        1 -> eveningAzkarList
        2 -> postSalahAzkarList
        else -> emptyList()
    }

    val categoryKey = when (selectedTab) {
        0 -> "morning"
        1 -> "evening"
        2 -> "post_salah"
        else -> "tasbeeh"
    }

    // Counts state for active list
    val counts = remember { mutableStateMapOf<Int, Int>() }

    // Reload counts when tab changes or app opens
    LaunchedEffect(selectedTab, todayStr) {
        counts.clear()
        currentAzkarList.forEach { item ->
            val saved = prefs.getInt("azkaar_${todayStr}_${categoryKey}_${item.id}", 0)
            counts[item.id] = saved
        }
    }

    // Streak calculation
    var streakTrigger by remember { mutableIntStateOf(0) }
    val streakDays = remember(todayStr, streakTrigger) {
        getAzkaarStreakCount(prefs, todayStr)
    }

    val completedCount = currentAzkarList.count { item -> (counts[item.id] ?: 0) >= item.targetCount }
    val totalItems = currentAzkarList.size
    val progressFraction = if (totalItems > 0) (completedCount.toFloat() / totalItems.toFloat()).coerceIn(0f, 1f) else 0f

    // When all items in current category are completed, register streak
    LaunchedEffect(completedCount, totalItems) {
        if (totalItems > 0 && completedCount == totalItems) {
            val key = "azkaar_${todayStr}_${categoryKey}_completed"
            prefs.edit().putBoolean(key, true).apply()
            updateAzkaarStreak(prefs, todayStr)
            streakTrigger++
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = com.example.ui.theme.PrimaryDeepTeal,
                shadowElevation = 4.dp
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Daily Azkaar & Tasbeeh",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Authentic Sunnah Duas & Digital Counter",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = com.example.ui.theme.WarmOrangeAccent,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        if (onNavigateBack != null) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.testTag("azkaar_back_button")
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    actions = {
                        // Vibration toggle
                        IconButton(
                            onClick = {
                                isVibrationEnabled = !isVibrationEnabled
                                prefs.edit().putBoolean("azkaar_vibration_enabled", isVibrationEnabled).apply()
                                if (isVibrationEnabled) performZikrFeedback()
                            },
                            modifier = Modifier.testTag("azkaar_vibration_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = "Toggle Vibration",
                                tint = if (isVibrationEnabled) com.example.ui.theme.WarmOrangeAccent else Color.White.copy(alpha = 0.5f)
                            )
                        }

                        // Reset session
                        IconButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.testTag("azkaar_reset_session_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Current Tab",
                                tint = Color.White
                            )
                        }

                        // Daily Azkaar Reminder Settings
                        IconButton(
                            onClick = { showReminderSettingsDialog = true },
                            modifier = Modifier.testTag("azkaar_reminder_settings_btn")
                        ) {
                            Icon(
                                imageVector = if (remindersEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "Daily Azkaar Reminders",
                                tint = if (remindersEnabled) com.example.ui.theme.WarmOrangeAccent else Color.White.copy(alpha = 0.65f)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = com.example.ui.theme.PrimaryDeepTeal
                    )
                )
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        GlassAppBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Category Glass Tabs
                val tabTitles = listOf(
                    "🌅 Morning",
                    "🌆 Evening",
                    "🕌 Post-Salah",
                    "📿 Tasbeeh"
                )

                Surface(
                    color = Color.White.copy(alpha = 0.75f),
                    border = BorderStroke(1.dp, Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF047857),
                        edgePadding = 16.dp,
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = Color(0xFF047857),
                                    height = 3.dp
                                )
                            }
                        }
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedTab == index) Color(0xFF047857) else Color(0xFF64748B)
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

                // Daily Azkaar Reminder Schedule Banner
                AzkaarReminderStatusBanner(
                    isGloballyEnabled = remindersEnabled,
                    selectedTab = selectedTab,
                    onOpenSettings = { showReminderSettingsDialog = true },
                    onSendTestNotification = {
                        val testType = when (selectedTab) {
                            0 -> com.example.service.AzkaarReminderManager.TYPE_MORNING
                            1 -> com.example.service.AzkaarReminderManager.TYPE_EVENING
                            2 -> com.example.service.AzkaarReminderManager.TYPE_POST_SALAH
                            else -> com.example.service.AzkaarReminderManager.TYPE_MORNING
                        }
                        com.example.service.AzkaarReminderManager.sendTestNotification(context, testType)
                        android.widget.Toast.makeText(
                            context,
                            "🔔 Test Azkaar reminder notification sent! Pull down your notification bar.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                )

                // Main Content Area
                if (selectedTab == 3) {
                    // Digital Tasbeeh Counter Tab
                    DigitalTasbeehView(
                        prefs = prefs,
                        todayStr = todayStr,
                        isVibrationEnabled = isVibrationEnabled,
                        onVibrate = { performZikrFeedback() }
                    )
                } else {
                    // List of Azkaar Items (Morning, Evening, Post-Salah)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        item {
                            // Streak & Daily Progress Glass Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = RoundedCornerShape(22.dp),
                                        ambientColor = Color(0x150F172A),
                                        spotColor = Color(0x12059669)
                                    )
                                    .testTag("azkaar_streak_card"),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.88f)),
                                border = BorderStroke(1.2.dp, Color.White)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFFFEF3C7),
                                                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                                modifier = Modifier.size(42.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(text = "🔥", fontSize = 22.sp)
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "$streakDays Days Streak",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                )
                                                Text(
                                                    text = "Daily Remembrance Habit",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF047857))
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (completedCount == totalItems && totalItems > 0) Color(0xFFD1FAE5) else Color(0xFFF1F5F9),
                                            border = BorderStroke(1.dp, if (completedCount == totalItems && totalItems > 0) Color(0xFFA7F3D0) else Color(0xFFE2E8F0))
                                        ) {
                                            Text(
                                                text = "$completedCount / $totalItems Done",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (completedCount == totalItems && totalItems > 0) Color(0xFF047857) else Color(0xFF334155)
                                                ),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Progress Bar
                                    LinearProgressIndicator(
                                        progress = progressFraction,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = Color(0xFF047857),
                                        trackColor = Color(0xFFE2E8F0)
                                    )

                                    if (completedCount == totalItems && totalItems > 0) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF059669),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Alhamdulillah! All Duas completed for this category today.",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color(0xFF047857),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            // Quick Action Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tap anywhere on a card to count",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF64748B),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )

                                TextButton(
                                    onClick = {
                                        currentAzkarList.forEach { item ->
                                            counts[item.id] = item.targetCount
                                            prefs.edit().putInt("azkaar_${todayStr}_${categoryKey}_${item.id}", item.targetCount).apply()
                                        }
                                        performZikrFeedback()
                                    },
                                    modifier = Modifier.testTag("azkaar_mark_all_done_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFF047857)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Mark All Done",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF047857),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        // Azkaar items list
                        itemsIndexed(currentAzkarList) { index, item ->
                            val currentCount = counts[item.id] ?: 0
                            val isCompleted = currentCount >= item.targetCount

                            AzkarCardItem(
                                item = item,
                                currentCount = currentCount,
                                isCompleted = isCompleted,
                                onIncrement = {
                                    val newCount = currentCount + 1
                                    counts[item.id] = newCount
                                    prefs.edit().putInt("azkaar_${todayStr}_${categoryKey}_${item.id}", newCount).apply()
                                    performZikrFeedback()
                                },
                                onDecrement = {
                                    if (currentCount > 0) {
                                        val newCount = currentCount - 1
                                        counts[item.id] = newCount
                                        prefs.edit().putInt("azkaar_${todayStr}_${categoryKey}_${item.id}", newCount).apply()
                                        performZikrFeedback()
                                    }
                                },
                                onReset = {
                                    counts[item.id] = 0
                                    prefs.edit().remove("azkaar_${todayStr}_${categoryKey}_${item.id}").apply()
                                    performZikrFeedback()
                                },
                                onSpeak = {
                                    speakArabic(item.arabicText, item.id)
                                },
                                isSpeaking = speakingZikrId == item.id
                            )
                        }
                    }
                }
            }
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(text = "Reset Category Counters?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to reset all counts in this category back to 0 for today?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        currentAzkarList.forEach { item ->
                            counts[item.id] = 0
                            prefs.edit().remove("azkaar_${todayStr}_${categoryKey}_${item.id}").apply()
                        }
                        showResetDialog = false
                        performZikrFeedback()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Reset to 0", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Daily Azkaar Reminder Configuration Dialog
    if (showReminderSettingsDialog) {
        AzkaarReminderSettingsDialog(
            context = context,
            onDismiss = { showReminderSettingsDialog = false },
            onSave = { enabled ->
                remindersEnabled = enabled
                showReminderSettingsDialog = false
            }
        )
    }
}

@Composable
fun AzkarCardItem(
    item: AzkarItem,
    currentCount: Int,
    isCompleted: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onReset: () -> Unit,
    onSpeak: () -> Unit,
    isSpeaking: Boolean
) {
    val cardBgColor by animateColorAsState(
        targetValue = if (isCompleted) Color(0xFFF0FDF4) else Color.White.copy(alpha = 0.88f),
        label = "cardBg"
    )
    val cardBorderColor by animateColorAsState(
        targetValue = if (isCompleted) Color(0xFF10B981) else Color.White,
        label = "cardBorder"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(
                elevation = if (isCompleted) 4.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0x120F172A),
                spotColor = Color(0x10059669)
            )
            .clickable { onIncrement() }
            .testTag("azkar_card_${item.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(if (isCompleted) 1.5.dp else 1.2.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Title & Target
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = if (isCompleted) Color(0xFF047857) else Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, if (isCompleted) Color(0xFF047857) else Color(0xFFA7F3D0)),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text(
                                    text = "${item.id % 100}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857)
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.titleEn,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        )
                        Text(
                            text = item.reference,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF047857),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Audio Pronunciation Button
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Listen to Pronunciation",
                            tint = if (isSpeaking) Color(0xFFD97706) else Color(0xFF047857),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Text(
                            text = "${item.targetCount}x Target",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF92400E),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Authentic Arabic Text
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.arabicText,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 20.sp,
                        lineHeight = 36.sp
                    ),
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                )
            }

            // English Translation
            if (item.englishTranslation.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = item.englishTranslation,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF334155),
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Benefit / Virtue Note in English
            val noteText = item.noteEn ?: item.noteUr
            if (!noteText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFECFDF5),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                ) {
                    Text(
                        text = "💡 $noteText",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF065F46),
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Counter & Tap Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Count Display
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Recited: ",
                        style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF64748B))
                    )
                    Text(
                        text = "$currentCount / ${item.targetCount}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) Color(0xFF047857) else Color(0xFFD97706)
                        )
                    )
                    if (isCompleted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF047857)
                        ) {
                            Text(
                                text = "Done ✓",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset single item
                    if (currentCount > 0) {
                        IconButton(
                            onClick = onReset,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset this item",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Decrement
                    if (currentCount > 0) {
                        IconButton(
                            onClick = onDecrement,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrement",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Primary Tap/Count Button
                    Button(
                        onClick = onIncrement,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompleted) Color(0xFF047857) else Color(0xFFD97706),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCompleted) "+ More" else "+1 Count",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DigitalTasbeehView(
    prefs: android.content.SharedPreferences,
    todayStr: String,
    isVibrationEnabled: Boolean,
    onVibrate: () -> Unit
) {
    var selectedPresetIndex by rememberSaveable { mutableIntStateOf(0) }
    val preset = tasbeehPresets.getOrElse(selectedPresetIndex) { tasbeehPresets[0] }

    var count by rememberSaveable(selectedPresetIndex) {
        val saved = prefs.getInt("tasbeeh_count_${preset.id}", 0)
        mutableIntStateOf(saved)
    }

    var round by rememberSaveable(selectedPresetIndex) {
        val savedRound = prefs.getInt("tasbeeh_round_${preset.id}", 1)
        mutableIntStateOf(savedRound)
    }

    var showTasbeehResetDialog by remember { mutableStateOf(false) }

    fun incrementTasbeeh() {
        onVibrate()
        val newCount = count + 1
        if (preset.targetCount > 0 && newCount >= preset.targetCount) {
            count = 0
            round++
            prefs.edit().putInt("tasbeeh_count_${preset.id}", 0).putInt("tasbeeh_round_${preset.id}", round).apply()
        } else {
            count = newCount
            prefs.edit().putInt("tasbeeh_count_${preset.id}", newCount).apply()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Preset Selector Chips
            Text(
                text = "Select Dhikr Supplication",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Start
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tasbeehPresets.take(4).forEachIndexed { idx, item ->
                    FilterChip(
                        selected = selectedPresetIndex == idx,
                        onClick = { selectedPresetIndex = idx },
                        label = { Text(item.titleEn, fontSize = 11.sp, fontWeight = if (selectedPresetIndex == idx) FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF047857),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.8f),
                            labelColor = Color(0xFF334155)
                        ),
                        border = BorderStroke(1.dp, if (selectedPresetIndex == idx) Color(0xFF047857) else Color.White)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tasbeehPresets.drop(4).forEachIndexed { idx, item ->
                    val actualIdx = idx + 4
                    FilterChip(
                        selected = selectedPresetIndex == actualIdx,
                        onClick = { selectedPresetIndex = actualIdx },
                        label = { Text(item.titleEn, fontSize = 11.sp, fontWeight = if (selectedPresetIndex == actualIdx) FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF047857),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.8f),
                            labelColor = Color(0xFF334155)
                        ),
                        border = BorderStroke(1.dp, if (selectedPresetIndex == actualIdx) Color(0xFF047857) else Color.White)
                    )
                }
            }
        }

        item {
            // Big Display Card with Arabic Text and English Virtue
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = Color(0x140F172A),
                        spotColor = Color(0x10059669)
                    ),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.88f)),
                border = BorderStroke(1.2.dp, Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = preset.arabicText,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            fontSize = 26.sp,
                            lineHeight = 42.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = preset.transliteration,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF047857),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Text(
                            text = preset.virtueEn,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF92400E),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Giant Frosted Circular Touch Counter Dial
            val progressFraction = if (preset.targetCount > 0) {
                (count.toFloat() / preset.targetCount.toFloat()).coerceIn(0f, 1f)
            } else 0f

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = Color(0x200F172A),
                        spotColor = Color(0x25047857)
                    )
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White, Color(0xFFECFDF5), Color(0xFFD1FAE5))
                        )
                    )
                    .clickable { incrementTasbeeh() }
                    .testTag("digital_tasbeeh_touch_dial"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF047857),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "Round $round",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            fontSize = 58.sp
                        )
                    )
                    Text(
                        text = "/ ${preset.targetCount}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tap to Count",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF047857),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = progressFraction,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF047857),
                trackColor = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action row: Reset & Count
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showTasbeehResetDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset")
                }

                Button(
                    onClick = { incrementTasbeeh() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF047857),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Count",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+1 Count", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showTasbeehResetDialog) {
        AlertDialog(
            onDismissRequest = { showTasbeehResetDialog = false },
            title = { Text("Reset Tasbeeh Counter?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to reset this Dhikr count and rounds back to 0?") },
            confirmButton = {
                Button(
                    onClick = {
                        count = 0
                        round = 1
                        prefs.edit().putInt("tasbeeh_count_${preset.id}", 0).putInt("tasbeeh_round_${preset.id}", 1).apply()
                        showTasbeehResetDialog = false
                        onVibrate()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Reset", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showTasbeehResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun getAzkaarStreakCount(prefs: android.content.SharedPreferences, todayStr: String): Int {
    val completedDates = prefs.getStringSet("azkaar_completed_dates", emptySet()) ?: emptySet()
    if (completedDates.isEmpty()) return 0

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val cal = Calendar.getInstance()

    var streak = 0
    var checkDate = todayStr

    while (true) {
        if (completedDates.contains(checkDate)) {
            streak++
            try {
                val parsed = sdf.parse(checkDate) ?: break
                cal.time = parsed
                cal.add(Calendar.DAY_OF_YEAR, -1)
                checkDate = sdf.format(cal.time)
            } catch (e: Exception) {
                break
            }
        } else {
            // Check yesterday if today isn't completed yet
            if (checkDate == todayStr) {
                try {
                    val parsed = sdf.parse(checkDate) ?: break
                    cal.time = parsed
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    checkDate = sdf.format(cal.time)
                    if (!completedDates.contains(checkDate)) break
                } catch (e: Exception) {
                    break
                }
            } else {
                break
            }
        }
    }
    return streak
}

private fun updateAzkaarStreak(prefs: android.content.SharedPreferences, todayStr: String) {
    val set = prefs.getStringSet("azkaar_completed_dates", emptySet())?.toMutableSet() ?: mutableSetOf()
    if (!set.contains(todayStr)) {
        set.add(todayStr)
        prefs.edit().putStringSet("azkaar_completed_dates", set).apply()
    }
}

@Composable
fun AzkaarReminderStatusBanner(
    isGloballyEnabled: Boolean,
    selectedTab: Int,
    onOpenSettings: () -> Unit,
    onSendTestNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("azkaar_reminder_status_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGloballyEnabled) Color(0xFFF0FDF4) else Color(0xFFFFFBEB)
        ),
        border = BorderStroke(
            1.dp,
            if (isGloballyEnabled) Color(0xFFBBF7D0) else Color(0xFFFDE68A)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isGloballyEnabled) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isGloballyEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (isGloballyEnabled) Color(0xFF15803D) else Color(0xFFD97706),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = if (isGloballyEnabled) "Daily Azkaar Reminders Active" else "Azkaar Reminders Inactive",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isGloballyEnabled) Color(0xFF14532D) else Color(0xFF92400E)
                        )
                    )
                    Text(
                        text = if (isGloballyEnabled) "Morning (06:30) • Evening (17:30) • Post-Salah" else "Tap schedule to enable daily reminders",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Quick Test Notification button
                Button(
                    onClick = onSendTestNotification,
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("send_test_azkaar_notif_banner_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.example.ui.theme.WarmOrangeAccent
                    )
                ) {
                    Text(
                        text = "🔔 Test",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    )
                }

                // Open Schedule Dialog
                OutlinedButton(
                    onClick = onOpenSettings,
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("open_azkaar_schedule_dialog_btn"),
                    border = BorderStroke(1.dp, Color(0xFF155563))
                ) {
                    Text(
                        text = "⚙️ Schedule",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF155563),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AzkaarReminderSettingsDialog(
    context: Context,
    onDismiss: () -> Unit,
    onSave: (Boolean) -> Unit
) {
    var globalEnabled by remember {
        mutableStateOf(com.example.service.AzkaarReminderManager.isAzkaarRemindersGloballyEnabled(context))
    }
    var morningEnabled by remember {
        mutableStateOf(com.example.service.AzkaarReminderManager.isReminderTypeEnabled(context, com.example.service.AzkaarReminderManager.TYPE_MORNING))
    }
    var eveningEnabled by remember {
        mutableStateOf(com.example.service.AzkaarReminderManager.isReminderTypeEnabled(context, com.example.service.AzkaarReminderManager.TYPE_EVENING))
    }
    var postSalahEnabled by remember {
        mutableStateOf(com.example.service.AzkaarReminderManager.isReminderTypeEnabled(context, com.example.service.AzkaarReminderManager.TYPE_POST_SALAH))
    }
    var nightEnabled by remember {
        mutableStateOf(com.example.service.AzkaarReminderManager.isReminderTypeEnabled(context, com.example.service.AzkaarReminderManager.TYPE_NIGHT))
    }

    var morningTime by remember {
        val (h, m) = com.example.service.AzkaarReminderManager.getReminderTime(context, com.example.service.AzkaarReminderManager.TYPE_MORNING)
        mutableStateOf(String.format("%02d:%02d", h, m))
    }
    var eveningTime by remember {
        val (h, m) = com.example.service.AzkaarReminderManager.getReminderTime(context, com.example.service.AzkaarReminderManager.TYPE_EVENING)
        mutableStateOf(String.format("%02d:%02d", h, m))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = com.example.ui.theme.PrimaryDeepTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Daily Azkaar Reminders",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Receive scheduled notifications to recite authentic morning, evening, post-Salah, and bedtime Azkaar from the Sunnah.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                )

                // Master Toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = com.example.ui.theme.PrimaryDeepTeal.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, com.example.ui.theme.PrimaryDeepTeal.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "All Azkaar Reminders",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (globalEnabled) "Active and scheduled daily" else "All reminders paused",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                            )
                        }
                        Switch(
                            checked = globalEnabled,
                            onCheckedChange = { globalEnabled = it },
                            modifier = Modifier.testTag("master_azkaar_reminder_switch")
                        )
                    }
                }

                if (globalEnabled) {
                    // Morning Azkaar
                    ReminderOptionRow(
                        title = "🌅 Morning Azkaar (أذكار الصباح)",
                        subtitle = "Scheduled for $morningTime AM",
                        checked = morningEnabled,
                        onCheckedChange = { morningEnabled = it },
                        timePresets = listOf("05:30", "06:00", "06:30", "07:00"),
                        selectedTime = morningTime,
                        onSelectTime = { morningTime = it }
                    )

                    // Evening Azkaar
                    ReminderOptionRow(
                        title = "🌆 Evening Azkaar (أذكار المساء)",
                        subtitle = "Scheduled for $eveningTime PM",
                        checked = eveningEnabled,
                        onCheckedChange = { eveningEnabled = it },
                        timePresets = listOf("16:30", "17:00", "17:30", "18:00"),
                        selectedTime = eveningTime,
                        onSelectTime = { eveningTime = it }
                    )

                    // Post Salah Azkaar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🕌 Post-Salah Sunnah Azkaar",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Prompt to recite 33x SubhanAllah, 33x Alhamdulillah after prayer",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                            )
                        }
                        Switch(
                            checked = postSalahEnabled,
                            onCheckedChange = { postSalahEnabled = it }
                        )
                    }

                    // Night Azkaar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🌙 Bedtime Sunnah Azkaar",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Recite Ayat al-Kursi & 3 Quls before sleeping (10:00 PM)",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                            )
                        }
                        Switch(
                            checked = nightEnabled,
                            onCheckedChange = { nightEnabled = it }
                        )
                    }

                    // Send Instant Test Button
                    Button(
                        onClick = {
                            com.example.service.AzkaarReminderManager.sendTestNotification(context, com.example.service.AzkaarReminderManager.TYPE_MORNING)
                            android.widget.Toast.makeText(
                                context,
                                "🔔 Test Azkaar reminder notification sent! Check your notifications bar.",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("send_test_azkaar_from_dialog_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.example.ui.theme.WarmOrangeAccent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Send Test Azkaar Notification Now",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    com.example.service.AzkaarReminderManager.setAzkaarRemindersGloballyEnabled(context, globalEnabled)
                    com.example.service.AzkaarReminderManager.setReminderTypeEnabled(context, com.example.service.AzkaarReminderManager.TYPE_MORNING, morningEnabled)
                    com.example.service.AzkaarReminderManager.setReminderTypeEnabled(context, com.example.service.AzkaarReminderManager.TYPE_EVENING, eveningEnabled)
                    com.example.service.AzkaarReminderManager.setReminderTypeEnabled(context, com.example.service.AzkaarReminderManager.TYPE_POST_SALAH, postSalahEnabled)
                    com.example.service.AzkaarReminderManager.setReminderTypeEnabled(context, com.example.service.AzkaarReminderManager.TYPE_NIGHT, nightEnabled)

                    try {
                        val (mh, mm) = morningTime.split(":").map { it.toInt() }
                        com.example.service.AzkaarReminderManager.setReminderTime(context, com.example.service.AzkaarReminderManager.TYPE_MORNING, mh, mm)
                    } catch (_: Exception) {}

                    try {
                        val (eh, em) = eveningTime.split(":").map { it.toInt() }
                        com.example.service.AzkaarReminderManager.setReminderTime(context, com.example.service.AzkaarReminderManager.TYPE_EVENING, eh, em)
                    } catch (_: Exception) {}

                    com.example.service.AzkaarReminderManager.scheduleAllAzkaarReminders(context)
                    android.widget.Toast.makeText(context, "✓ Daily Azkaar schedule saved & active!", android.widget.Toast.LENGTH_SHORT).show()
                    onSave(globalEnabled)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.example.ui.theme.PrimaryDeepTeal
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_azkaar_reminder_schedule_btn")
            ) {
                Text("Save Schedule", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ReminderOptionRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    timePresets: List<String>,
    selectedTime: String,
    onSelectTime: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }

        if (checked) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                timePresets.forEach { time ->
                    val isSelected = time == selectedTime
                    Surface(
                        modifier = Modifier
                            .clickable { onSelectTime(time) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) com.example.ui.theme.PrimaryDeepTeal else Color(0xFFF1F5F9),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) com.example.ui.theme.PrimaryDeepTeal else Color(0xFFE2E8F0)
                        )
                    ) {
                        Text(
                            text = time,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF334155),
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

