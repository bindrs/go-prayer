package com.example.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.entities.SalahRecord
import com.example.domain.prayer.DayPrayerSchedule
import com.example.domain.prayer.POPULAR_CITIES
import com.example.domain.prayer.PrayerTime
import com.example.domain.prayer.PresetCity
import com.example.domain.spiritual.SpiritualVerses
import com.example.ui.components.BismillahBanner
import com.example.ui.components.GlassAppBackground
import com.example.ui.components.IslamicStarDeco
import com.example.ui.components.PostSalahAzkarDialog
import com.example.ui.components.StatusBadge
import com.example.ui.qibla.QiblaCompassCard
import com.example.ui.theme.*
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldSoft
import com.example.ui.theme.EmeraldDarkPrimary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.tracker.SolemnPrayerOfferDialog
import com.example.ui.viewmodel.PrayerViewModel
import com.example.ui.vpn.VpnFahishaDialog
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: PrayerViewModel,
    onNavigateToPrayerScreen: (prayerName: String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAyatLibrary: () -> Unit,
    onNavigateToSalahTracker: () -> Unit,
    onNavigateToAzkaar: () -> Unit = {},
    onNavigateToDuayn: () -> Unit = {},
    onNavigateToQibla: () -> Unit = {}
) {
    val schedule by viewModel.schedule.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentAyat by viewModel.currentAyat.collectAsStateWithLifecycle()
    val liveCountdown by viewModel.liveCountdownSeconds.collectAsStateWithLifecycle()
    val isAudioPreviewing by viewModel.isAudioPreviewing.collectAsStateWithLifecycle()

    val monthlyStats by viewModel.monthlyStats.collectAsStateWithLifecycle()
    val todaySalahRecords by viewModel.todaySalahRecords.collectAsStateWithLifecycle()

    val isVpnDetected by viewModel.isVpnDetected.collectAsStateWithLifecycle()
    val vpnDetails by viewModel.vpnDetails.collectAsStateWithLifecycle()
    val isVpnBannerDismissed by viewModel.isVpnBannerDismissed.collectAsStateWithLifecycle()
    val isPrayerLocked by viewModel.isPrayerLocked.collectAsStateWithLifecycle()
    val lockPrayerName by viewModel.lockPrayerName.collectAsStateWithLifecycle()
    val showVpnFahishaModal by viewModel.showVpnFahishaModal.collectAsStateWithLifecycle()
    val fahishaAyatList by viewModel.fahishaAyatList.collectAsStateWithLifecycle()

    val showPostSalahAzkarDialog by viewModel.showPostSalahAzkarDialog.collectAsStateWithLifecycle()
    val postSalahPrayerName by viewModel.postSalahPrayerName.collectAsStateWithLifecycle()

    var showCityDialog by remember { mutableStateOf(false) }

    GlassAppBackground(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen_root")
    ) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(DarkTealPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🕌", fontSize = 16.sp)
                            }
                            Text(
                                text = "Go Prayer",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkTeal,
                                    fontSize = 20.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Assalamu Alaikum, ${settings.userName.ifEmpty { "Qasim Idrees" }}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = TextSecondaryGray,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .clickable { onNavigateToSettings() }
                                .testTag("nav_notifications_button")
                        ) {
                            Box(
                                modifier = Modifier.padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = TextDarkTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = DarkTealPrimary,
                            modifier = Modifier
                                .clickable { onNavigateToSettings() }
                                .testTag("nav_settings_profile_button")
                        ) {
                            Box(
                                modifier = Modifier.padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = WarmOrangeAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // 20-Minute Lockdown Alert Banner (if active)
            if (isPrayerLocked) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPrayerScreen(lockPrayerName) }
                            .border(1.5.dp, AccentGold, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AccentGold.copy(alpha = 0.2f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = AccentGold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "SALAH LOCK ACTIVE",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = AccentGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Text(
                                    text = "Mobile disabled for $lockPrayerName Salah. Tap to return to Prayer Screen.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = AccentGold
                            )
                        }
                    }
                }
            }

            // Real-Time VPN Warning & Fahisha Banner (if detected and not dismissed)
            if (isVpnDetected && !isVpnBannerDismissed) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFFE11D48), RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B0715))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "VPN CONNECTION DETECTED",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = Color(0xFFFB7185),
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = vpnDetails ?: "Encrypted Tunnel Active",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = AccentGold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.dismissVpnBanner() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss Banner",
                                        tint = Color.LightGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Allah is watching all actions in privacy. The Quran strongly prohibits Fahisha (immoralities & secret sins). Tap below to reflect on Quranic warnings.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFFCE7F3),
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.openVpnFahishaModal() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE11D48),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Show Fahisha Ayat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.openVpnSettings() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Disconnect VPN", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Location & Date Header Card
                LocationAndDateBar(
                    locationName = "${location.city}, ${location.country}",
                    gregorianDate = schedule?.dateString ?: "Today",
                    hijriDate = schedule?.hijriDateString ?: "",
                    onOpenLocationPicker = { showCityDialog = true }
                )
            }

            item {
                // Hero Next Prayer Countdown Card
                schedule?.nextPrayer?.let { nextPrayer ->
                    val nextPrayerRecord = todaySalahRecords.find { it.prayerName.equals(nextPrayer.name, ignoreCase = true) }
                    val completedCount = todaySalahRecords.count { it.isOffered }
                    val todayCompletionPercent = if (todaySalahRecords.isNotEmpty()) ((completedCount.toFloat() / 5f) * 100).toInt().coerceIn(0, 100) else 60

                    HeroNextPrayerCard(
                        nextPrayer = nextPrayer,
                        countdownSeconds = liveCountdown,
                        salahRecord = nextPrayerRecord,
                        completionPercentage = todayCompletionPercent,
                        onOpenPrayerScreen = { onNavigateToPrayerScreen(nextPrayer.name) },
                        onOfferQaza = { onNavigateToPrayerScreen(nextPrayer.name) },
                        onViewPrayerTimes = onNavigateToSalahTracker
                    )
                }
            }

            item {
                val isFriday = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.FRIDAY
                if (isFriday) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("friday_jummah_kahf_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF0F291E)
                        ),
                        border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF064E3B),
                                            Color(0xFF022C22)
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = AccentGold.copy(alpha = 0.2f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("🕌", fontSize = 20.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "JUMMAH-TUL-MUBARAK",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = AccentGold,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                        )
                                        Text(
                                            text = "Jummah Mubarak • Best Day of the Week",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AccentGold,
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    Text(
                                        text = "Surah Al-Kahf",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.Black,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "مَنْ قَرَأَ سُورَةَ الْكَهْفِ فِي يَوْمِ الْجُمُعَةِ أَضَاءَ لَهُ مِنَ النُّورِ مَا بَيْنَ الْجُمُعَتَيْنِ",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = AccentGoldSoft,
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Right,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\"Whoever recites Surah Al-Kahf on Friday, a light will illuminate for him between the two Fridays.\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 12.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            )
                        }
                    }
                }
            }

            // Monthly Salah Tracker Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSalahTracker() }
                        .testTag("dashboard_monthly_salah_tracker_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF0F766E).copy(alpha = 0.12f),
                                        Color(0xFFD97706).copy(alpha = 0.08f)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF0F766E),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Monthly Salah Record",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = AccentGold.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "${monthlyStats.completionPercentage}%",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentGold,
                                                    fontSize = 10.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${monthlyStats.totalOffered} / ${monthlyStats.totalPossible} Offered • ${monthlyStats.monthDisplayName}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "View Details",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Qibla Direction Compass Quick Card
            item {
                QiblaCompassCard(
                    userLatitude = location.latitude,
                    userLongitude = location.longitude,
                    cityName = location.city,
                    onOpenCompass = onNavigateToQibla
                )
            }

            // Prominent Daily Azkaar & Invocations Panel
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAzkaar() }
                        .testTag("dashboard_azkaar_hub_card"),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.example.ui.theme.PrimaryDeepTeal
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = com.example.ui.theme.WarmOrangeAccent,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Daily Azkaar & Tasbeeh",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = "Invocations, Digital Counter & Sunnah Duas",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = com.example.ui.theme.WarmOrangeAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Open Azkaar",
                                tint = Color.White
                            )
                        }

                        // Category Chips Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f).clickable { onNavigateToAzkaar() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "🌅 Morning",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f).clickable { onNavigateToAzkaar() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "🌆 Evening",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f).clickable { onNavigateToAzkaar() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "📿 Tasbeeh",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Sub Quick Buttons for Duayn & Quranic Verses
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onNavigateToAzkaar,
                                modifier = Modifier.weight(1f).testTag("dashboard_open_azkaar_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = com.example.ui.theme.WarmOrangeAccent,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Azkaar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = onNavigateToDuayn,
                                modifier = Modifier.weight(1f).testTag("dashboard_open_duas_btn"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Daily Duas", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }



            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Prayer Schedule",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${settings.calculationMethod} • ${settings.madhhab}",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            // Timetable Cards for all 6 prayers
            items(schedule?.prayers ?: emptyList()) { prayer ->
                val prayerRecord = todaySalahRecords.find { it.prayerName.equals(prayer.name, ignoreCase = true) }
                PrayerTimeCard(
                    prayer = prayer,
                    salahRecord = prayerRecord,
                    onToggleAdhan = { viewModel.togglePrayerAdhan(prayer.name, !prayer.adhanEnabled) },
                    onClickPrayer = {
                        onNavigateToPrayerScreen(prayer.name)
                    },
                    onOfferQaza = {
                        onNavigateToPrayerScreen(prayer.name)
                    }
                )
            }

            item {
                // Quranic Ayah of the day card
                currentAyat?.let { ayat ->
                    DailyAyatCard(
                        ayat = ayat,
                        onRefresh = { viewModel.refreshAyat() },
                        onViewMore = onNavigateToAyatLibrary
                    )
                }
            }

            item {
                // Quick Devotion & Audio Controls
                QuickTestSection(
                    isAudioPreviewing = isAudioPreviewing,
                    isPrayerLocked = isPrayerLocked,
                    onTestAdhan = { viewModel.previewTrack("makkah_adhan") },
                    onStartPrayerLock = {
                        val active = schedule?.nextPrayer?.name ?: "Dhuhr"
                        viewModel.startPrayerLock(active)
                        onNavigateToPrayerScreen(active)
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
    }

    if (showVpnFahishaModal) {
        VpnFahishaDialog(
            vpnDetail = vpnDetails,
            fahishaAyatList = fahishaAyatList,
            onOpenSettings = { viewModel.openVpnSettings() },
            onDismiss = { viewModel.closeVpnFahishaModal() }
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fine = perms[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = perms[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fine || coarse) {
            viewModel.requestGpsLocation(context)
        }
    }

    if (showCityDialog) {
        CitySelectionDialog(
            currentCity = location.city,
            onSelect = { city ->
                viewModel.setManualLocation(
                    city.city,
                    city.country,
                    city.latitude,
                    city.longitude,
                    city.timezone
                )
                showCityDialog = false
            },
            onSelectGps = {
                val hasFine = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                val hasCoarse = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (hasFine || hasCoarse) {
                    viewModel.requestGpsLocation(context)
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            onDismiss = { showCityDialog = false }
        )
    }

    if (showPostSalahAzkarDialog) {
        PostSalahAzkarDialog(
            prayerName = postSalahPrayerName,
            onDismiss = { viewModel.dismissPostSalahAzkar() }
        )
    }
}

@Composable
fun LocationAndDateBar(
    locationName: String,
    gregorianDate: String,
    hijriDate: String,
    onOpenLocationPicker: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.clickable { onOpenLocationPicker() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = locationName,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                IslamicStarDeco(sizeDp = 20, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = gregorianDate,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )

            if (hijriDate.isNotEmpty()) {
                Text(
                    text = hijriDate,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
fun HeroNextPrayerCard(
    nextPrayer: PrayerTime,
    countdownSeconds: Long,
    salahRecord: SalahRecord?,
    completionPercentage: Int,
    onOpenPrayerScreen: () -> Unit,
    onOfferQaza: () -> Unit,
    onViewPrayerTimes: () -> Unit
) {
    val hours = countdownSeconds / 3600
    val minutes = (countdownSeconds % 3600) / 60
    val countdownText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    val isOffered = salahRecord?.isOffered == true
    val isMissed = !isOffered && nextPrayer.isPast

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isMissed) onOfferQaza() else onOpenPrayerScreen()
            }
            .testTag("hero_next_prayer_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = DarkTealPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isMissed) "🔴 Missed Salah" else "Next Prayer: ${nextPrayer.name} in $countdownText",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isMissed) Color(0xFFFCA5A5) else WarmOrangeAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = nextPrayer.name,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 32.sp
                        )
                    )

                    Text(
                        text = nextPrayer.timeFormatted,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        )
                    )
                }

                // Orange Circular Prayer Progress Ring (Reference Design)
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 8.dp.toPx()
                        // Track ring
                        drawArc(
                            color = Color.White.copy(alpha = 0.2f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokeWidth,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                        // Progress ring in warm golden orange
                        drawArc(
                            color = WarmOrangeAccent,
                            startAngle = -90f,
                            sweepAngle = (completionPercentage / 100f) * 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokeWidth,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$completionPercentage%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "Daily",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = WarmOrangeAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CTA Button: "View Prayer Times" in warm golden orange
            Button(
                onClick = onViewPrayerTimes,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmOrangeAccent,
                    contentColor = DarkTealPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("hero_view_prayer_times_button")
            ) {
                Text(
                    text = "View Prayer Times",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun PrayerTimeCard(
    prayer: PrayerTime,
    salahRecord: SalahRecord?,
    onToggleAdhan: () -> Unit,
    onClickPrayer: () -> Unit,
    onOfferQaza: () -> Unit
) {
    val isNext = prayer.isNext
    val isSunrise = prayer.name.equals("Sunrise", ignoreCase = true)
    val isOffered = salahRecord?.isOffered == true
    val isPast = prayer.isPast
    val isMissed = !isSunrise && isPast && !isOffered

    val cardColor = when {
        isMissed -> Color(0xFFFEF2F2)
        isOffered -> Color(0xFFF0FDF4)
        isNext -> LightBlueGrayBg
        else -> Color.White
    }

    val cardBorder = when {
        isMissed -> BorderStroke(1.5.dp, Color(0xFFEF4444))
        isOffered -> BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.5f))
        isNext -> BorderStroke(2.dp, WarmOrangeAccent)
        else -> BorderStroke(1.dp, Color(0xFFE2E8F0))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickPrayer() }
            .testTag("prayer_card_${prayer.name}"),
        shape = RoundedCornerShape(22.dp),
        border = cardBorder,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isMissed -> Color(0xFFEF4444)
                                isOffered -> Color(0xFF10B981)
                                isNext -> WarmOrangeAccent
                                else -> Color(0xFFE2E8F0)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isOffered) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Offered",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    } else if (isMissed) {
                        Icon(
                            imageVector = Icons.Default.HourglassBottom,
                            contentDescription = "Missed",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = prayer.arabicName.take(2),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isNext) DarkTealPrimary else TextDarkTeal
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prayer.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isMissed) Color(0xFF991B1B) else TextDarkTeal
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        when {
                            isMissed -> {
                                StatusBadge(
                                    text = "UNOFFERED",
                                    isActive = true,
                                    customContainerColor = Color(0xFFFEE2E2),
                                    customTextColor = Color(0xFF991B1B)
                                )
                            }
                            isOffered -> {
                                val badgeText = when (salahRecord?.offeringType) {
                                    "QAZA" -> "QAZA OFFERED ✓"
                                    "CONGREGATION" -> "JAMA'AT ✓"
                                    else -> "OFFERED ✓"
                                }
                                StatusBadge(
                                    text = badgeText,
                                    isActive = true,
                                    customContainerColor = Color(0xFFDCFCE7),
                                    customTextColor = Color(0xFF166534)
                                )
                            }
                            isNext -> {
                                StatusBadge(
                                    text = "NEXT",
                                    isActive = true,
                                    customContainerColor = WarmOrangeAccent.copy(alpha = 0.2f),
                                    customTextColor = Color(0xFFC2410C)
                                )
                            }
                        }
                    }
                    Text(
                        text = prayer.arabicName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondaryGray
                        )
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = prayer.timeFormatted,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isMissed -> Color(0xFF991B1B)
                                isNext -> DarkTealPrimary
                                else -> TextDarkTeal
                            }
                        )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onToggleAdhan,
                        modifier = Modifier.size(36.dp).testTag("toggle_adhan_${prayer.name}")
                    ) {
                        Icon(
                            imageVector = if (prayer.adhanEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                            contentDescription = if (prayer.adhanEnabled) "Mute Adhan" else "Enable Adhan",
                            tint = when {
                                prayer.adhanEnabled && isMissed -> Color(0xFFFCA5A5)
                                prayer.adhanEnabled -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (!isSunrise) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isMissed) {
                            Button(
                                onClick = onOfferQaza,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("timetable_offer_qaza_${prayer.name}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEF4444),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Offer Qaza",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        } else {
                            Text(
                                text = "Azan: ${prayer.azanTimeFormatted} • Jama'at: ${prayer.jamatTimeFormatted}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyAyatCard(
    ayat: com.example.data.local.entities.Ayat,
    onRefresh: () -> Unit,
    onViewMore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Book, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ayah of the Day",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Shuffle Ayat", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = ayat.arabicText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "\"${ayat.englishTranslation}\"",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = ayat.urduTranslation,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${ayat.surahName} (${ayat.surahArabicName}) : ${ayat.ayatNumber}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                TextButton(onClick = onViewMore) {
                    Text("Ayat Library")
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun QuickTestSection(
    isAudioPreviewing: Boolean,
    isPrayerLocked: Boolean,
    onTestAdhan: () -> Unit,
    onStartPrayerLock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quick Audio & Devotion Controls",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onTestAdhan,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_adhan_preview_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAudioPreviewing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(if (isAudioPreviewing) Icons.Default.Stop else Icons.Default.VolumeUp, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isAudioPreviewing) "Stop Audio" else "Test Adhan")
                }

                Button(
                    onClick = onStartPrayerLock,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_start_20min_lock_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGold,
                        contentColor = Color(0xFF022C22)
                    )
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("20-Min Salah Lock", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CitySelectionDialog(
    currentCity: String,
    onSelect: (PresetCity) -> Unit,
    onSelectGps: () -> Unit = {},
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select City & Coordinates", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Vibrant GPS Location Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectGps()
                            onDismiss()
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10E0B0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF00382B))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Use Device GPS Location",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00382B)
                            )
                        )
                    }
                }

                Text(
                    "Or Choose Preset City:",
                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                LazyColumn(
                    modifier = Modifier.height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(POPULAR_CITIES) { city ->
                        val isSelected = city.city == currentCity
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(city) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${city.city}, ${city.country}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                Text(city.timezone, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun UserProfileSpiritualCard(
    userName: String,
    spiritualIdentity: String,
    favoriteLoveAyahId: String,
    onSelectLoveAyah: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var showVerseDialog by remember { mutableStateOf(false) }
    var showAffirmationDialog by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val currentAyah = SpiritualVerses.getAyatById(favoriteLoveAyahId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_user_profile_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D251C)),
        border = BorderStroke(1.2.dp, AccentGold.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Profile & Greeting Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AccentGold.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, AccentGold),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (userName.isNotBlank()) "السلام علیکم، $userName" else "السلام علیکم",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = spiritualIdentity,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = AccentGold,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentGold.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable { showAffirmationDialog = true }
                ) {
                    Text(
                        text = "اللہ کا بندہ",
                        color = AccentGold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Divine Love & Heart Purification Verse Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.06f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFF43F5E),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Surah ${currentAyah.surahName} (${currentAyah.ayahNumber})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = AccentGold,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Text(
                            text = currentAyah.spiritualTheme.split("•").first().trim(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        )
                    }

                    // Arabic
                    Text(
                        text = currentAyah.arabicText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFF8FAFC),
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 24.sp
                        ),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Urdu Translation
                    Text(
                        text = currentAyah.urduTranslation,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFE2E8F0),
                            lineHeight = 18.sp
                        ),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    AnimatedVisibility(visible = isExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // English
                            Text(
                                text = "« ${currentAyah.englishTranslation} »",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            )

                            // Heart Purification Note
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AccentGold.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = AccentGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "دل کی اصلاح: ${currentAyah.heartPurificationNote}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = AccentGoldSoft,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { isExpanded = !isExpanded },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (isExpanded) "Show Less" else "Read Reflection & English",
                                color = AccentGold,
                                fontSize = 11.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = { showVerseDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Change Verse", color = Color(0xFF38BDF8), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for picking from all 6 Love Ayats
    if (showVerseDialog) {
        AlertDialog(
            onDismissRequest = { showVerseDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ayats of Love & Purification", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "دل کو برائی، غصے اور وسوسوں سے نکال کر اللہ کی محبت اور رحمت سے منور کرنے کے لیے ان آیات کو پڑھیں اور دل کا سکون پائیں۔",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            ),
                            textAlign = TextAlign.Right
                        )
                    }

                    items(SpiritualVerses.loveAndPurificationAyats) { ayah ->
                        val isSelected = ayah.id == favoriteLoveAyahId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectLoveAyah(ayah.id)
                                    showVerseDialog = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Surah ${ayah.surahName} (${ayah.ayahNumber})",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = ayah.spiritualTheme.split("•").first().trim(),
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                                Text(
                                    text = ayah.arabicText,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, lineHeight = 20.sp),
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = ayah.urduTranslation,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVerseDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Dialog for "Who I Am: اللہ کا بندہ"
    if (showAffirmationDialog) {
        AlertDialog(
            onDismissRequest = { showAffirmationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IslamicStarDeco(sizeDp = 22, color = AccentGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Who I Am • میں کون ہوں؟", fontWeight = FontWeight.Bold, color = AccentGold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "« میں اللہ کا بندہ ہوں »",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = SpiritualVerses.IDENTITY_AFFIRMATION_URDU,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = SpiritualVerses.IDENTITY_AFFIRMATION_ENGLISH,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "تزکیۂ قلب (Purifying the Heart):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Whenever you feel anger, ill-intent, jealousy, or temptation, remember who you are: a servant of Allah. Divert evil to goodness by reciting istighfar and invoking Allah's love.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAffirmationDialog = false }) {
                    Text("Alhamdulillah")
                }
            }
        )
    }
}
