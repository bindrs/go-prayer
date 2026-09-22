package com.example.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.AdhanTrack
import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.HighLatitudeRule
import com.example.domain.prayer.Madhhab
import com.example.domain.prayer.PresetCity
import com.example.domain.prayer.PrayerTimeCalculator
import com.example.domain.spiritual.SpiritualVerses
import com.example.ui.components.GlassAppBackground
import com.example.ui.components.IslamicStarDeco
import com.example.ui.components.PostSalahAzkarDialog
import com.example.ui.components.StatusBadge
import com.example.ui.dashboard.CitySelectionDialog
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldSoft
import com.example.ui.theme.EmeraldPrimary
import java.util.Calendar
import com.example.ui.viewmodel.PrayerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: PrayerViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()
    val schedule by viewModel.schedule.collectAsStateWithLifecycle()
    val configs by viewModel.prayerConfigurations.collectAsStateWithLifecycle()
    val tracks by viewModel.adhanTracks.collectAsStateWithLifecycle()
    val logs by viewModel.eventLogs.collectAsStateWithLifecycle()
    val isAudioPreviewing by viewModel.isAudioPreviewing.collectAsStateWithLifecycle()
    val previewingTrackId by viewModel.previewingTrackId.collectAsStateWithLifecycle()
    val previewProgress by viewModel.previewProgress.collectAsStateWithLifecycle()
    val isGpsLocating by viewModel.isGpsLocating.collectAsStateWithLifecycle()
    val locationSuccess by viewModel.locationSuccessMessage.collectAsStateWithLifecycle()
    val locationError by viewModel.locationErrorMessage.collectAsStateWithLifecycle()
    val showPostSalahAzkarDialog by viewModel.showPostSalahAzkarDialog.collectAsStateWithLifecycle()
    val postSalahPrayerName by viewModel.postSalahPrayerName.collectAsStateWithLifecycle()
    val jsonStorageInfo by viewModel.jsonStorageInfo.collectAsStateWithLifecycle()
    var showJsonViewerDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fine = perms[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = perms[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fine || coarse) {
            viewModel.requestGpsLocation(context)
        } else {
            viewModel.setLocationError("Location permission denied. Please grant permission in device settings.")
        }
    }

    var showCityDialog by remember { mutableStateOf(false) }
    var showAdjustOffsetDialog by remember { mutableStateOf(false) }
    var selectedOffsetPrayer by remember { mutableStateOf("Fajr") }
    var currentOffsetValue by remember { mutableIntStateOf(0) }
    var currentAzanOffsetValue by remember { mutableIntStateOf(0) }
    var currentJamatOffsetValue by remember { mutableIntStateOf(15) }
    var currentEnterSalahNotificationEnabled by remember { mutableStateOf(true) }

    val isAccessibilityBlockerEnabled by viewModel.isAccessibilityBlockerEnabled.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.checkAccessibilityStatus(context)
    }

    // Custom Adhan Management States
    var showAssignTrackDialog by remember { mutableStateOf(false) }
    var trackToAssign by remember { mutableStateOf<AdhanTrack?>(null) }
    var trackToDelete by remember { mutableStateOf<AdhanTrack?>(null) }
    var importResultMessage by remember { mutableStateOf<String?>(null) }

    // Profile & Spiritual Identity States
    var showEditNameDialog by remember { mutableStateOf(false) }
    var tempNameInput by remember { mutableStateOf("") }
    var showLoveAyahDialog by remember { mutableStateOf(false) }
    var showSpiritualIdentityDialog by remember { mutableStateOf(false) }
    var showImportResultDialog by remember { mutableStateOf(false) }
    var isImportingAudio by remember { mutableStateOf(false) }

    var showPrayerTrackPickerDialog by remember { mutableStateOf(false) }
    var prayerNameToChangeTrack by remember { mutableStateOf<String?>(null) }

    val importAdhanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isImportingAudio = true
            viewModel.importCustomAdhan(uri) { success, message ->
                isImportingAudio = false
                importResultMessage = message
                showImportResultDialog = true
            }
        }
    }

    val customTracks = tracks.filter { it.id.startsWith("custom_") || it.localUri.startsWith("/") || it.localUri.startsWith("file:") }
    val builtInTracks = tracks.filter { !customTracks.contains(it) }

    Scaffold(
        topBar = {
            Surface(
                color = com.example.ui.theme.PrimaryDeepTeal,
                shadowElevation = 3.dp
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Settings & Preferences",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("settings_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = com.example.ui.theme.PrimaryDeepTeal)
                )
            }
        },
        containerColor = Color.Transparent,
        modifier = Modifier.testTag("settings_screen_root")
    ) { padding ->
        GlassAppBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Profile & Spiritual Identity Section
            item {
                SettingsSectionHeader("Profile & Spiritual Identity • روحانی تشخص")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_profile_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // User Name Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Your Name • آپ کا نام",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = if (settings.userName.isNotBlank()) settings.userName else "Banda-e-Khuda (Not set)",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (settings.userName.isNotBlank()) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    tempNameInput = settings.userName
                                    showEditNameDialog = true
                                },
                                modifier = Modifier.testTag("settings_edit_name_button")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit")
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Spiritual Identity Row ("Who I Am: اللہ کا بندہ")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IslamicStarDeco(sizeDp = 24, color = AccentGold)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Who I Am • میں کون ہوں؟",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = settings.spiritualIdentity,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = AccentGold, fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "اللہ کا بندہ — برائی سے بچ کر اچھے من کی طرف",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { showSpiritualIdentityDialog = true },
                                modifier = Modifier.testTag("settings_change_identity_button")
                            ) {
                                Text("Select")
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Heart Anchor Ayah Row (Love & Purification Verses)
                        val anchor = SpiritualVerses.getAyatById(settings.favoriteLoveAyahId)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color(0xFFE11D48),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Heart Anchor Ayat • دل کا سکون",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Surah ${anchor.surahName} (${anchor.surahNumber}:${anchor.ayahNumber})",
                                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = { showLoveAyahDialog = true },
                                    modifier = Modifier.testTag("settings_change_ayah_button")
                                ) {
                                    Text("Change")
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = anchor.arabicText,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, lineHeight = 20.sp),
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = anchor.urduTranslation,
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = "دل کی اصلاح: ${anchor.heartPurificationNote}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = AccentGold, fontSize = 11.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Location Settings Section
            item {
                SettingsSectionHeader("Location & Solar Coordinates")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${location.city}, ${location.country}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Lat: %.4f, Lng: %.4f".format(location.latitude, location.longitude), style = MaterialTheme.typography.bodySmall)
                                Text("Timezone: ${location.timezone} (${location.source})", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary))
                            }
                            OutlinedButton(
                                onClick = { showCityDialog = true },
                                modifier = Modifier.testTag("settings_change_city_button")
                            ) {
                                Text("Select City")
                            }
                        }

                        // Vibrant Pill GPS Button
                        Button(
                            onClick = {
                                val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (fine || coarse) {
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("settings_use_gps_button"),
                            enabled = !isGpsLocating,
                            shape = RoundedCornerShape(23.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10E0B0),
                                contentColor = Color(0xFF00382B),
                                disabledContainerColor = Color(0xFF10E0B0).copy(alpha = 0.5f),
                                disabledContentColor = Color(0xFF00382B).copy(alpha = 0.6f)
                            )
                        ) {
                            if (isGpsLocating) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFF00382B))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Detecting GPS Coordinates...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            } else {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Use Device GPS Location", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        if (locationSuccess != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.4f))
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(locationSuccess!!, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFA7F3D0)))
                                }
                            }
                        }

                        if (locationError != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(locationError!!, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error))
                                }
                            }
                        }
                    }
                }
            }

            // Calculation Method Section
            item {
                SettingsSectionHeader("Islamic Calculation Method")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CalculationMethod.values().forEach { method ->
                            val isSelected = settings.calculationMethod == method.name
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.updateCalculationMethod(method) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.updateCalculationMethod(method) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(method.displayName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Text(
                                        "Fajr: ${method.fajrAngle}°, Isha: ${if (method.isIshaFixedMinutes) "+${method.ishaFixedMinutes} min" else "${method.ishaAngle}°"}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Juristic & High Latitude
            item {
                SettingsSectionHeader("Juristic & High Latitude Parameters")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Asr Method (Shadow Ratio)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Madhhab.values().forEach { madhhab ->
                                val isSelected = settings.madhhab == madhhab.name
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.updateMadhhab(madhhab) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(madhhab.displayName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("High Latitude Fallback", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        HighLatitudeRule.values().forEach { rule ->
                            val isSelected = settings.highLatitudeRule == rule.name
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.updateHighLatitudeRule(rule) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = { viewModel.updateHighLatitudeRule(rule) })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(rule.displayName, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // Separate Prayer Times: Enter Salah, Azan, Jama'at
            item {
                SettingsSectionHeader("Separate Prayer Times (Enter Salah • Azan • Jama'at)")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Configure distinct times for each prayer: Enter Salah time is determined automatically by your location. Azan time and Jama'at (congregation) times can be adjusted manually.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        configs.forEach { config ->
                            val prayerTime = schedule?.prayers?.find { it.name.equals(config.prayerName, ignoreCase = true) }
                                ?: if (config.prayerName.equals("Jummah", ignoreCase = true) || config.prayerName.equals("Dhuhr", ignoreCase = true)) {
                                    schedule?.prayers?.find { it.name.equals("Dhuhr", ignoreCase = true) || it.name.equals("Jummah", ignoreCase = true) }
                                } else null

                            val displayName = when (config.prayerName) {
                                "Dhuhr" -> "Dhuhr (Mon–Thu, Sat–Sun)"
                                "Jummah" -> "Jummah (Friday)"
                                else -> config.prayerName
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedOffsetPrayer = config.prayerName
                                        currentOffsetValue = config.manualAdjustmentMinutes
                                        currentAzanOffsetValue = config.azanOffsetMinutes
                                        currentJamatOffsetValue = config.jamatOffsetMinutes
                                        currentEnterSalahNotificationEnabled = config.enterSalahNotificationEnabled
                                        showAdjustOffsetDialog = true
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = displayName,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            if (config.enterSalahNotificationEnabled) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                                ) {
                                                    Text(
                                                        text = "🔔 Enter Alert",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "Tap to Adjust",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // 1. Enter Salah Time Pill
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "🕒 1. Enter Salah",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                )
                                                Text(
                                                    text = prayerTime?.timeFormatted ?: "--:--",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = if (config.manualAdjustmentMinutes != 0) "Auto (${if (config.manualAdjustmentMinutes > 0) "+${config.manualAdjustmentMinutes}" else "${config.manualAdjustmentMinutes}"}m)" else "Auto (Location)",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                )
                                            }
                                        }

                                        // 2. Azan Time Pill
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "📢 2. Azan Time",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                                )
                                                Text(
                                                    text = prayerTime?.azanTimeFormatted ?: "--:--",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                )
                                                Text(
                                                    text = "${if (config.azanOffsetMinutes > 0) "+" else ""}${config.azanOffsetMinutes} min",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                                                )
                                            }
                                        }

                                        // 3. Jama'at Time Pill
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "👥 3. Jama'at",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                                )
                                                Text(
                                                    text = prayerTime?.jamatTimeFormatted ?: "--:--",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                                )
                                                Text(
                                                    text = "+${config.jamatOffsetMinutes} min",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // ADHAN MELODIES & CUSTOM IMPORT SECTION
            // ==========================================
            item {
                SettingsSectionHeader("Adhan Melodies & Custom Import (MP3 / MP4)")
                
                // Quick Test Azan Card
                val isTestingAnyAzan = isAudioPreviewing
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isTestingAnyAzan) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isTestingAnyAzan) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = null,
                                            tint = if (isTestingAnyAzan) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        "Test Azan Sound (ٹیسٹ اذان)",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        if (isTestingAnyAzan) "Playing Azan: ${tracks.find { it.id == previewingTrackId }?.title ?: "Sacred Melodies"}" else "Test full volume Azan audio on this device",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (isTestingAnyAzan) {
                                        viewModel.stopPreview()
                                    } else {
                                        val firstTrack = tracks.firstOrNull()?.id ?: "makkah_adhan"
                                        viewModel.previewTrack(firstTrack)
                                    }
                                },
                                modifier = Modifier.testTag("test_azan_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTestingAnyAzan) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = if (isTestingAnyAzan) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isTestingAnyAzan) "Stop" else "Test Azan")
                            }
                        }

                        if (isTestingAnyAzan) {
                            LinearProgressIndicator(
                                progress = { previewProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))

                // Import Action Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Audiotrack,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Import Custom Azan (MP3 / MP4)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    "Import your favorite Qari's Azan from your device storage",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }

                        Text(
                            "Supported formats: MP3, MP4, AAC, M4A, WAV. Files are securely preserved offline to trigger automatically during Salah times (20s recitation).",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        Button(
                            onClick = {
                                try {
                                    importAdhanLauncher.launch(
                                        arrayOf("audio/*", "video/mp4", "audio/mp4", "audio/mpeg", "audio/wav", "audio/aac", "audio/x-m4a", "*/*")
                                    )
                                } catch (_: Exception) {
                                    importAdhanLauncher.launch(arrayOf("*/*"))
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("import_custom_azan_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = !isImportingAudio
                        ) {
                            if (isImportingAudio) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Importing & Indexing Audio...")
                            } else {
                                Icon(Icons.Default.UploadFile, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Select MP3 / MP4 from Device")
                            }
                        }
                    }
                }
            }

            // Custom Imported Azans List
            if (customTracks.isNotEmpty()) {
                item {
                    Text(
                        "My Imported Recitations (${customTracks.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                }

                items(customTracks, key = { it.id }) { track ->
                    val isPlaying = isAudioPreviewing && previewingTrackId == track.id
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            track.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Text(
                                        track.subtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.previewTrack(track.id, track.localUri) },
                                        modifier = Modifier.testTag("preview_custom_${track.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = "Preview Custom Azan",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    IconButton(
                                        onClick = { trackToDelete = track },
                                        modifier = Modifier.testTag("delete_custom_${track.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Custom Azan",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            if (isPlaying) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    LinearProgressIndicator(
                                        progress = { previewProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                    )
                                    Text(
                                        "Testing 20-second immersive Azan trigger...",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.assignTrackToAllPrayers(track.id)
                                        importResultMessage = "Assigned \"${track.title}\" to all 5 daily prayers."
                                        showImportResultDialog = true
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Set for All Prayers", style = MaterialTheme.typography.labelSmall)
                                }

                                Button(
                                    onClick = {
                                        trackToAssign = track
                                        showAssignTrackDialog = true
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Assign to...", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            // Built-in Melodies Section
            item {
                Text(
                    "Standard Sacred Melodies",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        builtInTracks.forEach { track ->
                            val isPlaying = isAudioPreviewing && previewingTrackId == track.id
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(track.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(track.subtitle, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { viewModel.previewTrack(track.id) }) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                contentDescription = "Preview",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        TextButton(onClick = {
                                            trackToAssign = track
                                            showAssignTrackDialog = true
                                        }) {
                                            Text("Assign")
                                        }
                                    }
                                }

                                if (isPlaying) {
                                    LinearProgressIndicator(
                                        progress = { previewProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .padding(bottom = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Per-Prayer Adhan Assignment Overview
            item {
                SettingsSectionHeader("Current Prayer Audio Selections")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        configs.forEach { config ->
                            val activeTrack = tracks.find { it.id == config.audioTrackId }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        prayerNameToChangeTrack = config.prayerName
                                        showPrayerTrackPickerDialog = true
                                    }
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        config.prayerName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        activeTrack?.title ?: config.audioTrackId,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (activeTrack?.id?.startsWith("custom_") == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (activeTrack?.id?.startsWith("custom_") == true) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        text = "Change",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Prayer Quiet Mode & Silent Ringer
            item {
                SettingsSectionHeader("Auto Silent During Salah")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Auto Silent Ringer During Salah", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Automatically silences incoming ringtones during prayer without blocking alarms", style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = settings.quietModeEnabled,
                                onCheckedChange = { viewModel.updateQuietMode(it, settings.quietModeDurationMinutes) }
                            )
                        }

                        if (settings.quietModeEnabled) {
                            Text("Silent Duration: ${settings.quietModeDurationMinutes} minutes", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(15, 20, 25, 30).forEach { mins ->
                                    val isSelected = settings.quietModeDurationMinutes == mins
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.updateQuietMode(true, mins) },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Text(
                                            "$mins min",
                                            modifier = Modifier.padding(8.dp),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "✓ Ringer switches to Silent during Salah and restores automatically.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF059669), fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }
                }
            }

            // 20-Minute Salah Lock & VPN Protection
            item {
                SettingsSectionHeader("Salah Lock & Islamic Guard")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Total Disable on Prayer (20 Min)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    "Completely disables mobile for 20 minutes during Salah with immersive Ayat screen to prevent worldly distractions.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Switch(
                                checked = settings.totalDisableOnPrayer,
                                onCheckedChange = { viewModel.updateTotalDisableOnPrayer(it) },
                                modifier = Modifier.testTag("setting_total_disable_switch")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "VPN Detection & Instant Alert",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    "Monitors continuously in background. When any VPN connects, immediately fires a high-priority notification with Fahisha Ayat & \"Allah is watching you\" reminder.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Switch(
                                checked = settings.vpnFahishaAlertEnabled,
                                onCheckedChange = { viewModel.updateVpnFahishaAlert(it) },
                                modifier = Modifier.testTag("setting_vpn_fahisha_switch")
                            )
                        }

                        // Android App Blocker (Disable All Apps During Salah)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isAccessibilityBlockerEnabled) Color(0xFF047857).copy(alpha = 0.15f) else Color(0xFFD97706).copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = if (isAccessibilityBlockerEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (isAccessibilityBlockerEnabled) Color(0xFF059669) else Color(0xFFD97706),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "All Android Apps Disabled on Salah",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = if (isAccessibilityBlockerEnabled) "✓ Blocker Service Enabled" else "⚠️ Action Required: Enable Accessibility",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isAccessibilityBlockerEnabled) Color(0xFF059669) else Color(0xFFD97706)
                                                )
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "When Salah time is active, opening ANY other app minimizes it immediately and displays the urgent \"Go for Salah\" Heads-Up warning notification (like VPN alert) with authentic Quranic Ayat, Urdu translation, \"🕌 I'm going to pray\" button, and direct prayer screen access.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.openAccessibilitySettings(context) },
                                        modifier = Modifier.weight(1f).testTag("open_accessibility_settings_btn"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isAccessibilityBlockerEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isAccessibilityBlockerEnabled) "Accessibility On" else "Enable Accessibility",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.testAppBlocker(context, prayerName = "Asr", durationMinutes = 2)
                                            if (!isAccessibilityBlockerEnabled) {
                                                viewModel.launchSalahBlockerOverlayDirectly(context, prayerName = "Asr")
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "⚠️ Note: Accessibility Service is not enabled yet! Tap 'Enable Accessibility' to allow blocking other apps. Blocker notification alert triggered!",
                                                    android.widget.Toast.LENGTH_LONG
                                                ).show()
                                            } else {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "🧪 Blocker active for 2 minutes! Try opening any other app (YouTube, WhatsApp, Browser) to test automatic app disabling & notification.",
                                                    android.widget.Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f).testTag("test_app_blocker_btn")
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Test 2-Min Blocker", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.testEnterSalahNotification(context, prayerName = "Asr")
                                            android.widget.Toast.makeText(context, "🔔 Enter Salah Notification Sent!", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f).testTag("test_enter_salah_notif_btn")
                                    ) {
                                        Text("Test Enter Salah Notif", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.testSalahWarningNotification(context, prayerName = "Asr", count = 1)
                                            android.widget.Toast.makeText(context, "⚠️ 5-Min Warning Notification Sent!", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f).testTag("test_warning_notif_btn")
                                    ) {
                                        Text("Test 5-Min Warning Notif", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                                    }
                                }

                                Button(
                                    onClick = {
                                        viewModel.launchSalahBlockerOverlayDirectly(context, prayerName = "Asr")
                                        android.widget.Toast.makeText(context, "Heads-Up Warning Notification Triggered!", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("preview_blocker_popup_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF064E3B))
                                ) {
                                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Trigger \"Go For Salah\" Warning Notification",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFFDFBF7))
                                    )
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.showPostSalahAzkar("Fajr") },
                            modifier = Modifier.fillMaxWidth().testTag("open_post_salah_azkar_btn")
                        ) {
                            Text("Open Locked Post-Salah Azkar Popup", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }

                        // Daily Azkaar Reminders Card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            modifier = Modifier.fillMaxWidth().testTag("azkaar_settings_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint = Color(0xFF15803D),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Daily Azkaar Reminders",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF14532D)
                                            )
                                        )
                                    }
                                }

                                Text(
                                    text = "Scheduled daily notifications to recite authentic Morning (أذكار الصباح), Evening (أذكار المساء), and Post-Salah Sunnah Azkaar with digital Tasbeeh counter.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.testAzkaarReminder(context, com.example.service.AzkaarReminderManager.TYPE_MORNING)
                                            android.widget.Toast.makeText(
                                                context,
                                                "🔔 Morning Azkaar Reminder sent! Pull down notification shade.",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        modifier = Modifier.weight(1f).testTag("test_morning_azkaar_notif_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.WarmOrangeAccent)
                                    ) {
                                        Text("Test Morning Azkaar", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold))
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.testAzkaarReminder(context, com.example.service.AzkaarReminderManager.TYPE_EVENING)
                                            android.widget.Toast.makeText(
                                                context,
                                                "🔔 Evening Azkaar Reminder sent! Pull down notification shade.",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        modifier = Modifier.weight(1f).testTag("test_evening_azkaar_notif_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.PrimaryDeepTeal)
                                    ) {
                                        Text("Test Evening Azkaar", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Background Service Status & Test Trigger
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                "Prayer Monitoring Foreground Service Active",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            )
                                            Text(
                                                "Sticky notification with live countdown & Adhan alarms persistent on Android 10+",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.restartPrayerForegroundService(context)
                                            android.widget.Toast.makeText(context, "🕌 Sticky Prayer Notification Refreshed!", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f).testTag("refresh_sticky_notif_btn")
                                    ) {
                                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Refresh Sticky Notif", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                                    }

                                    Button(
                                        onClick = { viewModel.triggerTestVpnAlert() },
                                        modifier = Modifier.weight(1f).testTag("test_vpn_alert_btn"),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text("Test VPN Alert", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Appearance & Formatting
            item {
                SettingsSectionHeader("Appearance & Time Format")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("24-Hour Time Format", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                            Switch(
                                checked = settings.timeFormat == "24H",
                                onCheckedChange = { viewModel.updateTimeFormat(it) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("App Language", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.updateLanguage("en") },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (settings.language == "en") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Text("English", color = if (settings.language == "en") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(
                                    onClick = { viewModel.updateLanguage("ur") },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (settings.language == "ur") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Text("اردو", color = if (settings.language == "ur") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Diagnostic Alarm Logs, JSON Storage & Reset History
            item {
                SettingsSectionHeader("Data & History Management")

                // On-Device JSON Storage & Backup Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                        .size(36.dp)
                                        .background(
                                            color = EmeraldPrimary.copy(alpha = 0.15f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Device JSON Storage",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = EmeraldPrimary
                                        ) {
                                            Text(
                                                text = "JSON FORMAT",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${jsonStorageInfo.totalRecordsStored} records stored in JSON format on device (${jsonStorageInfo.fileSizeFormatted})",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }

                        // JSON Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showJsonViewerDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("View JSON File", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.syncAllDataToJson()
                                    android.widget.Toast.makeText(context, "JSON file synced successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync JSON", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Clean Slate & Reset Data",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Wipe all historical prayer records and start tracking your Salah from zero.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        OutlinedButton(
                            onClick = {
                                viewModel.clearAllSalahRecords()
                                android.widget.Toast.makeText(context, "All prayer records reset to zero!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().testTag("reset_all_salah_records_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset All Prayer History (Start From Zero)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Recent Prayer Alarm Events (${logs.size})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            if (logs.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearLogs() }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Clear logs")
                                }
                            }
                        }

                        if (logs.isEmpty()) {
                            Text("No alarm events recorded yet. Events will appear after alarms execute.", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        } else {
                            val dateFormat = SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault())
                            logs.take(5).forEach { log ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text("${log.prayerName} at ${dateFormat.format(Date(log.triggeredAt))}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Text("Adhan: ${log.adhanStatus} • Quiet Mode: ${log.quietModeStatus}", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        }
    }

    if (showCityDialog) {
        CitySelectionDialog(
            currentCity = location.city,
            onSelect = { city ->
                viewModel.clearLocationMessages()
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
                val fine = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                val coarse = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (fine || coarse) {
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

    if (showAdjustOffsetDialog) {
        val selectedConfig = configs.find { it.prayerName == selectedOffsetPrayer }
        val currentPrayerItem = schedule?.prayers?.find { it.name.equals(selectedOffsetPrayer, ignoreCase = true) }
            ?: if (selectedOffsetPrayer.equals("Jummah", ignoreCase = true) || selectedOffsetPrayer.equals("Dhuhr", ignoreCase = true)) {
                schedule?.prayers?.find { it.name.equals("Dhuhr", ignoreCase = true) || it.name.equals("Jummah", ignoreCase = true) }
            } else null
        val baseEnterMillis = (currentPrayerItem?.timestampMillis ?: System.currentTimeMillis()) - (selectedConfig?.manualAdjustmentMinutes ?: 0) * 60 * 1000L
        val previewEnterMillis = baseEnterMillis + (currentOffsetValue * 60 * 1000L)
        val previewAzanMillis = previewEnterMillis + (currentAzanOffsetValue * 60 * 1000L)
        val previewJamatMillis = previewEnterMillis + (currentJamatOffsetValue * 60 * 1000L)
        val is24H = settings.timeFormat == "24H"
        val liveEnterTimeStr = PrayerTimeCalculator.formatTime(previewEnterMillis, is24H, TimeZone.getDefault())
        val liveAzanTimeStr = PrayerTimeCalculator.formatTime(previewAzanMillis, is24H, TimeZone.getDefault())
        val liveJamatTimeStr = PrayerTimeCalculator.formatTime(previewJamatMillis, is24H, TimeZone.getDefault())

        AlertDialog(
            onDismissRequest = { showAdjustOffsetDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Times for $selectedOffsetPrayer", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Live Computed Times Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "✨ Live Preview for $selectedOffsetPrayer:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("🕒 Enter Salah", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                                    Text(liveEnterTimeStr, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                }
                                Column {
                                    Text("📢 Azan Time", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.primary))
                                    Text(liveAzanTimeStr, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                                }
                                Column {
                                    Text("👥 Jama'at", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary))
                                    Text(liveJamatTimeStr, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary))
                                }
                            }
                        }
                    }

                    // 1. Enter Salah Time (Solar Time + Manual Offset)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "1. Enter Salah Time (وقت داخل)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Calculated Time: $liveEnterTimeStr",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        text = "${if (currentOffsetValue > 0) "+" else ""}$currentOffsetValue min",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Stepper Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { currentOffsetValue -= 5 },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("-5m", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { currentOffsetValue -= 1 },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("-1m", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        val cal = Calendar.getInstance().apply { timeInMillis = previewEnterMillis }
                                        val baseCal = Calendar.getInstance().apply { timeInMillis = baseEnterMillis }
                                        val baseMinutes = baseCal.get(Calendar.HOUR_OF_DAY) * 60 + baseCal.get(Calendar.MINUTE)
                                        android.app.TimePickerDialog(
                                            context,
                                            { _, pickedHour, pickedMinute ->
                                                val targetMinutes = pickedHour * 60 + pickedMinute
                                                var diff = targetMinutes - baseMinutes
                                                if (diff > 720) diff -= 1440
                                                if (diff < -720) diff += 1440
                                                currentOffsetValue = diff.coerceIn(-120, 120)
                                            },
                                            cal.get(Calendar.HOUR_OF_DAY),
                                            cal.get(Calendar.MINUTE),
                                            is24H
                                        ).show()
                                    },
                                    modifier = Modifier.weight(2f),
                                    contentPadding = PaddingValues(2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("⏰ $liveEnterTimeStr", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { currentOffsetValue += 1 },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("+1m", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { currentOffsetValue += 5 },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("+5m", fontSize = 11.sp)
                                }
                            }

                            // Quick Preset Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(-5, 0, +2, +5, +10).forEach { offset ->
                                    OutlinedButton(
                                        onClick = { currentOffsetValue = offset },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(2.dp)
                                    ) {
                                        Text("${if (offset > 0) "+" else ""}$offset m", fontSize = 10.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Notify when time enters",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                )
                                Switch(
                                    checked = currentEnterSalahNotificationEnabled,
                                    onCheckedChange = { currentEnterSalahNotificationEnabled = it }
                                )
                            }
                        }
                    }

                    // 2. Azan Time (Manual Direct Adjustment)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "2. Azan Time (اذان وقت)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    )
                                    Text(
                                        text = "Azan Time: $liveAzanTimeStr",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${if (currentAzanOffsetValue > 0) "+" else ""}$currentAzanOffsetValue min",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Direct Azan Stepper & Time Picker Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { currentAzanOffsetValue -= 5 },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("-5m", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { currentAzanOffsetValue -= 1 },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("-1m", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        val cal = Calendar.getInstance().apply { timeInMillis = previewAzanMillis }
                                        val baseCal = Calendar.getInstance().apply { timeInMillis = previewEnterMillis }
                                        val baseMinutes = baseCal.get(Calendar.HOUR_OF_DAY) * 60 + baseCal.get(Calendar.MINUTE)
                                        android.app.TimePickerDialog(
                                            context,
                                            { _, pickedHour, pickedMinute ->
                                                val targetMinutes = pickedHour * 60 + pickedMinute
                                                var diff = targetMinutes - baseMinutes
                                                if (diff > 720) diff -= 1440
                                                if (diff < -720) diff += 1440
                                                currentAzanOffsetValue = diff.coerceIn(-60, 180)
                                            },
                                            cal.get(Calendar.HOUR_OF_DAY),
                                            cal.get(Calendar.MINUTE),
                                            is24H
                                        ).show()
                                    },
                                    modifier = Modifier.weight(2f),
                                    contentPadding = PaddingValues(2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("⏰ $liveAzanTimeStr", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { currentAzanOffsetValue += 1 },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("+1m", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { currentAzanOffsetValue += 5 },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("+5m", fontSize = 11.sp)
                                }
                            }

                            // Quick Presets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(0, 2, 5, 10, 15).forEach { offset ->
                                    OutlinedButton(
                                        onClick = { currentAzanOffsetValue = offset },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(2.dp)
                                    ) {
                                        Text("${if (offset > 0) "+" else ""}$offset m", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    // 3. Jama'at Time (Congregation Direct Manual Adjustment)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "3. Jama'at Time (جماعت وقت)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    )
                                    Text(
                                        text = "Jama'at Time: $liveJamatTimeStr",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "+$currentJamatOffsetValue min",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Direct Jama'at Stepper & Time Picker Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { currentJamatOffsetValue = (currentJamatOffsetValue - 5).coerceAtLeast(0) },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("-5m", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { currentJamatOffsetValue = (currentJamatOffsetValue - 1).coerceAtLeast(0) },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("-1m", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        val cal = Calendar.getInstance().apply { timeInMillis = previewJamatMillis }
                                        val baseCal = Calendar.getInstance().apply { timeInMillis = previewEnterMillis }
                                        val baseMinutes = baseCal.get(Calendar.HOUR_OF_DAY) * 60 + baseCal.get(Calendar.MINUTE)
                                        android.app.TimePickerDialog(
                                            context,
                                            { _, pickedHour, pickedMinute ->
                                                val targetMinutes = pickedHour * 60 + pickedMinute
                                                var diff = targetMinutes - baseMinutes
                                                if (diff > 720) diff -= 1440
                                                if (diff < -720) diff += 1440
                                                currentJamatOffsetValue = diff.coerceAtLeast(0)
                                            },
                                            cal.get(Calendar.HOUR_OF_DAY),
                                            cal.get(Calendar.MINUTE),
                                            is24H
                                        ).show()
                                    },
                                    modifier = Modifier.weight(2f),
                                    contentPadding = PaddingValues(2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Text("⏰ $liveJamatTimeStr", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { currentJamatOffsetValue += 1 },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("+1m", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { currentJamatOffsetValue += 5 },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("+5m", fontSize = 11.sp)
                                }
                            }

                            // Quick Presets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(10, 15, 20, 30, 45).forEach { offset ->
                                    OutlinedButton(
                                        onClick = { currentJamatOffsetValue = offset },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(2.dp)
                                    ) {
                                        Text("+$offset m", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updatePrayerTimesSettings(
                            prayerName = selectedOffsetPrayer,
                            manualAdjustmentMinutes = currentOffsetValue,
                            azanOffsetMinutes = currentAzanOffsetValue,
                            jamatOffsetMinutes = currentJamatOffsetValue,
                            enterSalahNotificationEnabled = currentEnterSalahNotificationEnabled
                        )
                        showAdjustOffsetDialog = false
                    },
                    modifier = Modifier.testTag("save_prayer_time_offsets_button")
                ) {
                    Text("Save & Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustOffsetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Assign Track to Specific Prayer Dialog
    if (showAssignTrackDialog && trackToAssign != null) {
        val currentTrack = trackToAssign!!
        AlertDialog(
            onDismissRequest = { showAssignTrackDialog = false },
            title = {
                Text("Assign \"${currentTrack.title}\"")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select which prayers should use this Adhan recitation:", style = MaterialTheme.typography.bodyMedium)
                    
                    listOf("Tahajjud", "Fajr", "Dhuhr", "Jummah", "Asr", "Maghrib", "Isha").forEach { prayerName ->
                        val isAssigned = configs.find { it.prayerName == prayerName }?.audioTrackId == currentTrack.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updatePrayerAudioTrack(prayerName, currentTrack.id)
                                    showAssignTrackDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAssigned) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(prayerName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                if (isAssigned) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Assigned", tint = MaterialTheme.colorScheme.primary)
                                } else {
                                    Text("Set", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.assignTrackToAllPrayers(currentTrack.id)
                    showAssignTrackDialog = false
                }) {
                    Text("Set for All 5 Prayers")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAssignTrackDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Change Melody for Specific Prayer Dialog
    if (showPrayerTrackPickerDialog && prayerNameToChangeTrack != null) {
        val pName = prayerNameToChangeTrack!!
        AlertDialog(
            onDismissRequest = { showPrayerTrackPickerDialog = false },
            title = {
                Text("Select Adhan for $pName")
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tracks) { trk ->
                        val isSelected = configs.find { it.prayerName == pName }?.audioTrackId == trk.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updatePrayerAudioTrack(pName, trk.id)
                                    showPrayerTrackPickerDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(trk.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(trk.subtitle, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrayerTrackPickerDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (trackToDelete != null) {
        val target = trackToDelete!!
        AlertDialog(
            onDismissRequest = { trackToDelete = null },
            title = {
                Text("Delete Custom Azan?")
            },
            text = {
                Text("Are you sure you want to remove \"${target.title}\"? Any prayer currently assigned to this track will revert to the Makkah Adhan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomAdhan(target)
                        trackToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { trackToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Import Result Dialog
    if (showImportResultDialog && importResultMessage != null) {
        AlertDialog(
            onDismissRequest = { showImportResultDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adhan Import")
                }
            },
            text = {
                Text(importResultMessage ?: "")
            },
            confirmButton = {
                Button(onClick = { showImportResultDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showPostSalahAzkarDialog) {
        PostSalahAzkarDialog(
            prayerName = postSalahPrayerName,
            onDismiss = { viewModel.dismissPostSalahAzkar() }
        )
    }

    if (showJsonViewerDialog) {
        val rawJson = remember { viewModel.getRawJsonContent() }
        val clipboardManager = LocalClipboardManager.current
        AlertDialog(
            onDismissRequest = { showJsonViewerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Device JSON File Data", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Internal Path: ${jsonStorageInfo.internalPath}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (jsonStorageInfo.externalPath != null) {
                        Text(
                            text = "External Backup: ${jsonStorageInfo.externalPath}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = rawJson,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboardManager.setText(AnnotatedString(rawJson))
                    android.widget.Toast.makeText(context, "JSON copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                }) {
                    Text("Copy JSON")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJsonViewerDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Name • نام تبدیل کریں")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter your name as you would like AL-SUJOOD to address you:")
                    OutlinedTextField(
                        value = tempNameInput,
                        onValueChange = { tempNameInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_name_input_field"),
                        placeholder = { Text("Your Name (e.g. Qasim, Abdullah)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUserName(tempNameInput)
                        showEditNameDialog = false
                    },
                    modifier = Modifier.testTag("settings_save_name_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Spiritual Identity Selection Dialog ("Who I Am: اللہ کا بندہ")
    if (showSpiritualIdentityDialog) {
        AlertDialog(
            onDismissRequest = { showSpiritualIdentityDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IslamicStarDeco(sizeDp = 22, color = AccentGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Who I Am • میں کون ہوں؟", color = AccentGold, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "« میں اللہ کا بندہ ہوں »",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Select your spiritual designation to remind yourself of humility, devotion, and turning away from negative impulses toward a clean and pure heart (اچھا من):",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    SpiritualVerses.identityOptions.forEach { opt ->
                        val isSelected = opt == settings.spiritualIdentity
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateSpiritualIdentity(opt)
                                    showSpiritualIdentityDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = opt,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpiritualIdentityDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Love & Heart Purification Verses Selection Dialog
    if (showLoveAyahDialog) {
        AlertDialog(
            onDismissRequest = { showLoveAyahDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFE11D48))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Love & Purification Ayats", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Choose your anchor verse to divert the heart from evil to pure devotion (دل کو برائی سے اچھائی کی طرف موڑنے والی آیات):",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    items(SpiritualVerses.loveAndPurificationAyats) { ayah ->
                        val isSelected = ayah.id == settings.favoriteLoveAyahId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateFavoriteLoveAyah(ayah.id)
                                    showLoveAyahDialog = false
                                },
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
                                        text = "Surah ${ayah.surahName} (${ayah.surahNumber}:${ayah.ayahNumber})",
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
                                Text(
                                    text = "« ${ayah.englishTranslation} »",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = "دل کی اصلاح: ${ayah.heartPurificationNote}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = AccentGold, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLoveAyahDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
