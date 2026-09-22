package com.example.ui.prayer

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.SeedData
import com.example.ui.components.BismillahBanner
import com.example.ui.components.GlowingPrayerRing
import com.example.ui.components.IslamicStarDeco
import com.example.ui.components.PostSalahAzkarDialog
import com.example.ui.theme.AccentGold
import com.example.ui.viewmodel.PrayerViewModel
import com.example.ui.vpn.VpnFahishaDialog
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PrayerScreen(
    prayerName: String,
    viewModel: PrayerViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val allAyat by viewModel.allAyat.collectAsStateWithLifecycle()
    val fahishaAyatList by viewModel.fahishaAyatList.collectAsStateWithLifecycle()
    val isAudioPreviewing by viewModel.isAudioPreviewing.collectAsStateWithLifecycle()
    val previewProgress by viewModel.previewProgress.collectAsStateWithLifecycle()

    val isVpnDetected by viewModel.isVpnDetected.collectAsStateWithLifecycle()
    val vpnDetails by viewModel.vpnDetails.collectAsStateWithLifecycle()
    val showVpnFahishaModal by viewModel.showVpnFahishaModal.collectAsStateWithLifecycle()

    val isPrayerLocked by viewModel.isPrayerLocked.collectAsStateWithLifecycle()

    val showPostSalahAzkarDialog by viewModel.showPostSalahAzkarDialog.collectAsStateWithLifecycle()
    val postSalahPrayerName by viewModel.postSalahPrayerName.collectAsStateWithLifecycle()

    val daySchedule by viewModel.schedule.collectAsStateWithLifecycle()
    val isGoingToPray by viewModel.isGoingToPray.collectAsStateWithLifecycle()
    val todaySalahRecords by viewModel.todaySalahRecords.collectAsStateWithLifecycle()
    val isPrayerCompletedToday = todaySalahRecords.any { it.prayerName.equals(prayerName, ignoreCase = true) && it.isOffered }

    val isSalahWarningActive by viewModel.isSalahWarningActive.collectAsStateWithLifecycle()
    val warningCount by viewModel.warningCount.collectAsStateWithLifecycle()
    val warningAyatList by viewModel.warningAyatList.collectAsStateWithLifecycle()
    val isAccessibilityBlockerEnabled by viewModel.isAccessibilityBlockerEnabled.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.checkAccessibilityStatus(context)
    }

    val currentPrayerInfo = daySchedule?.prayers?.find { it.name.equals(prayerName, ignoreCase = true) }

    var currentAyatIndex by remember { mutableIntStateOf(0) }
    var currentTimeString by remember { mutableStateOf("") }
    var showGoingToPrayConfirmDialog by remember { mutableStateOf(false) }

    // Start prayer lock if within active Azan/Jama'at prayer window or blocker service is active
    LaunchedEffect(prayerName, daySchedule, isPrayerCompletedToday, isGoingToPray) {
        if (isPrayerCompletedToday) {
            viewModel.unlockIfAlreadyOffered(prayerName)
        } else if (settings.totalDisableOnPrayer && !isPrayerLocked && !isGoingToPray) {
            val isBlockerServiceActive = com.example.service.SalahAppBlockerService.isBlockerActive
            val now = System.currentTimeMillis()
            val azanTime = currentPrayerInfo?.azanTimeMillis ?: currentPrayerInfo?.timestampMillis ?: 0L
            
            // Checking if we're past azan time and haven't prayed yet
            val isAzanPrayerWindowActive = azanTime > 0L && now >= azanTime

            if (isBlockerServiceActive || isAzanPrayerWindowActive) {
                viewModel.startPrayerLock(prayerName)
            }
        }
    }

    // Immersive Mode: Hide system status & navigation bars and disable swipe navigation when prayer is locked
    DisposableEffect(isPrayerLocked) {
        val activity = context as? Activity
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (isPrayerLocked) {
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            val act = context as? Activity
            act?.window?.let { win ->
                val controller = WindowCompat.getInsetsController(win, win.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Intercept back gesture / back button when locked
    BackHandler(enabled = isPrayerLocked && !isGoingToPray) {
        // Prevent exiting screen during prayer lock - screen unlocks automatically when prayer is marked as offered or going to pray
    }

    // Keep screen awake
    DisposableEffect(settings.keepScreenAwake) {
        val activity = context as? Activity
        val window = activity?.window
        if (settings.keepScreenAwake) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Live clock updater
    LaunchedEffect(Unit) {
        val format = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        while (true) {
            currentTimeString = format.format(Date())
            delay(1000)
        }
    }

    // Select verses: if Salah warning is active, display authentic Quranic Azaab/warning Ayat!
    val activeAyatList = if (isSalahWarningActive) {
        if (warningAyatList.isNotEmpty()) warningAyatList else SeedData.warningAyatList
    } else if (isVpnDetected && settings.vpnFahishaAlertEnabled) {
        if (fahishaAyatList.isNotEmpty()) fahishaAyatList else SeedData.fahishaAyatList
    } else {
        if (allAyat.isNotEmpty()) allAyat else SeedData.verifiedAyatList
    }

    val currentAyat = if (activeAyatList.isNotEmpty()) {
        activeAyatList[currentAyatIndex % activeAyatList.size]
    } else null

    val arabicPrayerName = when (prayerName.lowercase()) {
        "tahajjud" -> "التهجد"
        "fajr" -> "الفجر"
        "sunrise" -> "الشروق"
        "dhuhr" -> "الظهر"
        "jummah" -> "الجمعة"
        "asr" -> "العصر"
        "maghrib" -> "المغرب"
        "isha" -> "العشاء"
        else -> "الصلاة"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isSalahWarningActive) {
                        listOf(
                            Color(0xFF2C0A0A), // Warning crimson alert background
                            Color(0xFF1E0E08),
                            Color(0xFF021611)
                        )
                    } else if (isVpnDetected && settings.vpnFahishaAlertEnabled) {
                        listOf(
                            Color(0xFF20050E),
                            Color(0xFF0F172A),
                            Color(0xFF021611)
                        )
                    } else {
                        listOf(
                            Color(0xFF021B14),
                            Color(0xFF064E3B),
                            Color(0xFF021611)
                        )
                    }
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("prayer_screen_root")
    ) {
        // Subtle Islamic background ornament
        Image(
            painter = painterResource(id = R.drawable.bg_islamic_ornament_1789517369928),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.16f),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation & Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isPrayerLocked) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("prayer_screen_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                } else {
                    Surface(
                        shape = CircleShape,
                        color = AccentGold.copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Prayer Lockdown Mode Active",
                                tint = AccentGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSalahWarningActive) Color(0xFFE11D48).copy(alpha = 0.2f) else AccentGold.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSalahWarningActive) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isSalahWarningActive) Color(0xFFFB7185) else AccentGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSalahWarningActive) "⚠️ Salah Warning Active" else "Salah Focus Screen",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSalahWarningActive) Color(0xFFFB7185) else AccentGold
                            )
                        )
                    }
                }

                IconButton(
                    onClick = {
                        if (activeAyatList.isNotEmpty()) {
                            currentAyatIndex = (currentAyatIndex + 1) % activeAyatList.size
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Rotate Ayah",
                        tint = AccentGold
                    )
                }
            }

            // Accessibility Permission Banner if App Blocker enabled but permission missing
            if (!isAccessibilityBlockerEnabled && settings.totalDisableOnPrayer) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.openAccessibilitySettings(context) }
                        .border(1.dp, Color(0xFFF97316), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF431407))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF97316).copy(alpha = 0.3f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFFDBA74),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACCESSIBILITY PERMISSION NEEDED",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFFFDBA74),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Tap to enable Accessibility Service so Go for Salah can disable apps automatically during Azan.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFFFEDD5),
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Open Settings",
                            tint = AccentGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Real-Time VPN Warning & Fahisha Banner
            if (isVpnDetected && settings.vpnFahishaAlertEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.openVpnFahishaModal() }
                        .border(1.dp, Color(0xFFE11D48), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3B0715))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE11D48).copy(alpha = 0.3f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFFB7185),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "VPN ACTIVE • FAHISHA AYAT DISPLAYED",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFFFB7185),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Allah is All-Seeing. Tap to inspect Quranic warnings and disconnect.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFFCE7F3),
                                    fontSize = 11.sp
                                )
                            )
                        }

                        IconButton(
                            onClick = { viewModel.openVpnSettings() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "VPN Settings",
                                tint = AccentGold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bismillah Calligraphy Banner
            BismillahBanner(modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))

            // Prayer Focus Banner
            if (isPrayerLocked || isSalahWarningActive) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSalahWarningActive) Color(0xFF2C0A0A).copy(alpha = 0.9f) else Color(0xFF0F172A).copy(alpha = 0.9f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(AccentGold.copy(alpha = 0.7f), Color(0xFF047857))
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSalahWarningActive) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isSalahWarningActive) Color(0xFFFB7185) else AccentGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSalahWarningActive) "SALAH WARNING NOTIFICATION ACTIVE" else "SALAH DEVOTION FOCUS ACTIVE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = AccentGold,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Apps are restricted to guard your Salah from distractions. Notification alerts will remind you until prayer is offered.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Prayer Name & Glow Ring Header
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                GlowingPrayerRing(
                    modifier = Modifier.size(150.dp),
                    ringColor = if (isVpnDetected && settings.vpnFahishaAlertEnabled) Color(0xFFE11D48) else AccentGold
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = arabicPrayerName,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = AccentGold,
                            fontSize = 32.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = prayerName.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentTimeString,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFA7F3D0),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Adhan Audio status control banner
            if (isAudioPreviewing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x33000000))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = AccentGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Adhan Audio Playing",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Button(
                                onClick = { viewModel.stopPreview() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Dismiss", fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { previewProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = AccentGold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 1. Separate Prayer Times Card (Enter Salah Auto, Azan Manual, Jamat Manual)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prayer_screen_separate_times_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x2A000000)),
                border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OFFICIAL PRAYER TIMES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AccentGold,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Location: Auto • Offsets: Manual",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 1. Enter Salah
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "1. Enter Salah",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFA7F3D0), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "وقت داخل",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFA7F3D0).copy(alpha = 0.7f), fontSize = 9.sp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = currentPrayerInfo?.timeFormatted ?: "--:--",
                                style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Automatic",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 9.sp)
                            )
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(Color.White.copy(alpha = 0.15f))
                        )

                        // 2. Azan Time
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "2. Azan Time",
                                style = MaterialTheme.typography.labelSmall.copy(color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "اذان وقت",
                                style = MaterialTheme.typography.labelSmall.copy(color = AccentGold.copy(alpha = 0.7f), fontSize = 9.sp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = currentPrayerInfo?.azanTimeFormatted ?: currentPrayerInfo?.timeFormatted ?: "--:--",
                                style = MaterialTheme.typography.titleMedium.copy(color = AccentGold, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Manual Adjust",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 9.sp)
                            )
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(Color.White.copy(alpha = 0.15f))
                        )

                        // 3. Jama'at Time
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "3. Jama'at Time",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF67E8F9), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "جماعت وقت",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF67E8F9).copy(alpha = 0.7f), fontSize = 9.sp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = currentPrayerInfo?.jamatTimeFormatted ?: "--:--",
                                style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF67E8F9), fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Manual Adjust",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 9.sp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recurring 5-Min Salah Warning Banner (If time passed or button not clicked yet)
            if (isSalahWarningActive) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prayer_screen_salah_warning_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                    border = BorderStroke(2.dp, Color(0xFFEF4444))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⚠️ SALAH DELAY WARNING #$warningCount (وقت گزرنے کی تنبیہ)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFFFCA5A5),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "نماز کا وقت گزر رہا ہے اور آپ نے ابھی تک 'میں نماز پڑھنے جا رہا ہوں' بٹن نہیں دبایا۔ نماز ضائع کرنے پر سخت عذاب کی وعید ہے!",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "A recurring reminder notification with severe warning repeats every 5 minutes until you tap 'I am going to offer pray' below.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFFECACA),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Android App Blocker Information Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prayer_screen_app_blocker_status_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x22000000)),
                border = BorderStroke(1.dp, if (isAccessibilityBlockerEnabled) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFF59E0B).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isAccessibilityBlockerEnabled) Icons.Default.Shield else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isAccessibilityBlockerEnabled) Color(0xFFA7F3D0) else Color(0xFFFBBF24),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAccessibilityBlockerEnabled) "ALL ANDROID APPS DISABLED DURING SALAH" else "APP BLOCKER SERVICE INACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isAccessibilityBlockerEnabled) Color(0xFFA7F3D0) else Color(0xFFFBBF24)
                            )
                        )
                        Text(
                            text = if (isAccessibilityBlockerEnabled)
                                "Tapping any app redirects here to offer prayer."
                            else
                                "Enable Accessibility to redirect any opened app directly to prayer.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 11.sp)
                        )
                    }
                    if (!isAccessibilityBlockerEnabled) {
                        TextButton(
                            onClick = { viewModel.openAccessibilitySettings(context) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Enable", fontSize = 11.sp, color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quranic Ayah Devotional Card (Conditional on Warning / Fahisha / Regular)
            currentAyat?.let { ayat ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prayer_screen_ayat_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSalahWarningActive) Color(0xFF260505) else if (ayat.themeTopic == "Fahisha & Modesty") Color(0xFF1E0A12) else Color(0xFF032B20).copy(alpha = 0.85f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            if (isSalahWarningActive) {
                                listOf(Color(0xFFEF4444), AccentGold)
                            } else if (ayat.themeTopic == "Fahisha & Modesty") {
                                listOf(Color(0xFFE11D48), AccentGold)
                            } else {
                                listOf(AccentGold.copy(alpha = 0.6f), Color(0xFF047857))
                            }
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSalahWarningActive) Color(0xFFEF4444).copy(alpha = 0.25f) else if (ayat.themeTopic == "Fahisha & Modesty") Color(0xFFE11D48).copy(alpha = 0.25f) else AccentGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (isSalahWarningActive) "⚠️ QURANIC WARNING: NEGLECTING SALAH (وعیدِ عذاب)" else if (ayat.themeTopic == "Fahisha & Modesty") "⚠️ QURANIC PROHIBITION OF FAHISHA" else "AUTHENTIC QURANIC VERSE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSalahWarningActive) Color(0xFFFCA5A5) else if (ayat.themeTopic == "Fahisha & Modesty") Color(0xFFFB7185) else AccentGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            IslamicStarDeco(sizeDp = 18, color = AccentGold)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Arabic Script
                        Text(
                            text = ayat.arabicText,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFEF3C7),
                                lineHeight = 38.sp,
                                textAlign = TextAlign.Right
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // English Translation
                        Text(
                            text = "\"${ayat.englishTranslation}\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                lineHeight = 22.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Urdu Translation
                        Text(
                            text = ayat.urduTranslation,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFD1FAE5),
                                textAlign = TextAlign.Right,
                                lineHeight = 20.sp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Reference & Category Nav
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${ayat.surahName} (${ayat.surahArabicName}) : ${ayat.ayatNumber}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGold
                                )
                            )

                            Row {
                                IconButton(
                                    onClick = {
                                        if (activeAyatList.isNotEmpty()) {
                                            currentAyatIndex = if (currentAyatIndex > 0) currentAyatIndex - 1 else activeAyatList.size - 1
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Ayah", tint = Color.White)
                                }
                                IconButton(
                                    onClick = {
                                        if (activeAyatList.isNotEmpty()) {
                                            currentAyatIndex = (currentAyatIndex + 1) % activeAyatList.size
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Ayah", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Action Controls
            if (isPrayerCompletedToday) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF047857)),
                    border = BorderStroke(1.dp, Color(0xFF10B981))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$prayerName Prayer Offered Today",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "اللہ آپ کی نماز قبول فرمائے (May Allah accept your prayer)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.9f)
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.showPostSalahAzkar(prayerName) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prayer_screen_read_post_salah_azkar_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGold,
                        contentColor = Color(0xFF022C22)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "📿 Read After Namaz Azkaar (أذكار بعد الصلاة)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onNavigateBack() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Return to Dashboard", color = Color.White)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // "I am going to offer pray" button (Explicitly requested by user)
                    if (!isGoingToPray) {
                        Button(
                            onClick = {
                                showGoingToPrayConfirmDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("i_am_going_to_offer_prayer_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD97706),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "🤲 I am going to offer pray",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                )
                                Text(
                                    text = "میں نماز پڑھنے جا رہا ہوں (Stops 5-Min Warning)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                                )
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                            border = BorderStroke(1.dp, Color(0xFF10B981))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFA7F3D0), modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "✓ Going to Offer Prayer (Restrictions Lifted)",
                                        style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFA7F3D0), fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "اللہ تعالیٰ آپ کی نماز قبول فرمائے۔ Tap below once prayer is completed.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontSize = 11.sp)
                                    )
                                }
                            }
                        }
                    }

                if (isPrayerLocked) {
                    // Primary button: I Offered Prayer
                    Button(
                        onClick = {
                            viewModel.markPrayerCompletedAndUnlock(prayerName)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prayer_screen_i_offered_prayer_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGold,
                            contentColor = Color(0xFF022C22)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "I Have Offered My Prayer",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.emergencyUnlockPrayer()
                                onNavigateBack()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("prayer_screen_emergency_unlock_button"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFFB7185)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder().copy(
                                brush = Brush.horizontalGradient(listOf(Color(0xFFE11D48), Color(0xFFFB7185)))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Emergency Unlock", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                if (isAudioPreviewing) {
                                    viewModel.stopPreview()
                                } else {
                                    viewModel.playPrayerAdhan(prayerName)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF064E3B),
                                contentColor = Color(0xFFA7F3D0)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                if (isAudioPreviewing) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isAudioPreviewing) "Stop Azan" else "Play Azan", fontSize = 11.sp)
                        }
                    }
                } else {
                    // Not locked: I Offered Prayer + Exit to Dashboard + Azan toggle
                    Button(
                        onClick = {
                            viewModel.markPrayerCompletedAndUnlock(prayerName)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prayer_screen_i_offered_prayer_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGold,
                            contentColor = Color(0xFF022C22)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "I Have Offered My Prayer",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Exit to Dashboard")
                        }

                        Button(
                            onClick = {
                                if (isAudioPreviewing) {
                                    viewModel.stopPreview()
                                } else {
                                    viewModel.playPrayerAdhan(prayerName)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF064E3B),
                                contentColor = Color(0xFFA7F3D0)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(if (isAudioPreviewing) Icons.Default.Stop else Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isAudioPreviewing) "Stop Azan" else "Play Azan", fontSize = 11.sp)
                        }
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Fahisha Quranic Dialog on VPN Detection
    if (showVpnFahishaModal) {
        VpnFahishaDialog(
            vpnDetail = vpnDetails,
            fahishaAyatList = fahishaAyatList,
            onOpenSettings = { viewModel.openVpnSettings() },
            onDismiss = { viewModel.closeVpnFahishaModal() }
        )
    }

    if (showPostSalahAzkarDialog) {
        PostSalahAzkarDialog(
            prayerName = postSalahPrayerName,
            onDismiss = { viewModel.dismissPostSalahAzkar() }
        )
    }

    // Going to Offer Prayer Confirmation Dialog with Quranic Ayat & Instant Restriction Release
    if (showGoingToPrayConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showGoingToPrayConfirmDialog = false },
            modifier = Modifier.testTag("going_to_pray_confirm_dialog"),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🤲", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "Going to Offer Prayer",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "میں نماز پڑھنے جا رہا ہوں",
                            style = MaterialTheme.typography.labelSmall.copy(color = AccentGold, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quranic Ayah Card: Fulfilling Salah with Submissiveness
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.90f),
                        border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IslamicStarDeco(sizeDp = 16, color = AccentGold)
                                Text(
                                    text = "سورة البقرة • آیت 238",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AccentGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Text(
                                text = "« حَافِظُوا عَلَى الصَّلَوَاتِ وَالصَّلَاةِ الْوُسْطَىٰ وَقُومُوا لِلَّهِ قَانِتِينَ »",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    lineHeight = 26.sp,
                                    color = Color.White
                                ),
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "”تمام نمازوں کی اور بالخصوص درمیانی نماز کی پابندی کرو اور اللہ کے سامنے باادب اطاعت گزار بن کر کھڑے رہا کرو۔“",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFFCBD5E1)
                                ),
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "\"Maintain with care the [obligatory] prayers and [in particular] the middle prayer and stand before Allah, devoutly obedient.\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            )
                        }
                    }

                    // Second Quranic Ayah: Fixed Timings of Salah
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "« إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَّوْقُوتًا »",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "”بے شک نماز مومنوں پر مقررہ اوقات میں فرض کی گئی ہے۔“ (سورة النساء: 103)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Clarification of Action & Immediate Unblocking
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF064E3B).copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "کیا آپ واقعی ابھی $prayerName کی نماز پڑھنے جا رہے ہیں؟",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                ),
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "• تصدیق کے ساتھ ہی موبائل اور تمام ایپس کی پابندی فوری ہٹا دی جائے گی۔\n• 5 منٹ کا تکراری وارننگ الارم بند ہو جائے گا۔\n• نماز مکمل کر کے آپ واپس آ کر اپنی نماز ریکارڈ کر سکتے ہیں۔",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onGoingToOfferPrayer(prayerName)
                        showGoingToPrayConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD97706),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_going_to_pray_button")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("جی ہاں، نماز پڑھنے جا رہا ہوں")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showGoingToPrayConfirmDialog = false },
                    modifier = Modifier.testTag("cancel_going_to_pray_button")
                ) {
                    Text("منسوخ (Cancel)")
                }
            }
        )
    }
}
