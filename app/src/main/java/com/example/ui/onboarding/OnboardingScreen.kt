package com.example.ui.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
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
import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.Madhhab
import com.example.domain.prayer.POPULAR_CITIES
import com.example.domain.prayer.PresetCity
import com.example.domain.spiritual.SpiritualVerses
import com.example.ui.components.BismillahBanner
import com.example.ui.components.IslamicStarDeco
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldSoft
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PrimaryDeepTeal
import com.example.ui.theme.TextDarkTeal
import com.example.ui.theme.TextSecondaryGray
import com.example.ui.theme.VeryLightBlueGray
import com.example.ui.theme.WarmOrangeAccent
import com.example.ui.viewmodel.PrayerViewModel

@Composable
fun OnboardingScreen(
    viewModel: PrayerViewModel,
    onFinishOnboarding: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 7

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()
    val configs by viewModel.prayerConfigurations.collectAsStateWithLifecycle()
    val tracks by viewModel.adhanTracks.collectAsStateWithLifecycle()
    val isAudioPreviewing by viewModel.isAudioPreviewing.collectAsStateWithLifecycle()
    val previewingTrackId by viewModel.previewingTrackId.collectAsStateWithLifecycle()
    val previewProgress by viewModel.previewProgress.collectAsStateWithLifecycle()
    val isGpsLocating by viewModel.isGpsLocating.collectAsStateWithLifecycle()
    val locationError by viewModel.locationErrorMessage.collectAsStateWithLifecycle()
    val locationSuccess by viewModel.locationSuccessMessage.collectAsStateWithLifecycle()
    val isAccessibilityBlockerEnabled by viewModel.isAccessibilityBlockerEnabled.collectAsStateWithLifecycle()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fine = perms[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = perms[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fine || coarse) {
            viewModel.requestGpsLocation(context)
        } else {
            viewModel.setLocationError("Location permission denied. Please choose a city below or enable Location in Settings.")
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* processed */ }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("onboarding_root")
    ) {
        // Top Progress Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IslamicStarDeco(sizeDp = 24, color = PrimaryDeepTeal)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Setup Go Prayer",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDarkTeal
                    )
                )
            }
            Text(
                text = "Step $currentStep of $totalSteps",
                style = MaterialTheme.typography.labelMedium.copy(color = TextSecondaryGray)
            )
        }

        LinearProgressIndicator(
            progress = { currentStep.toFloat() / totalSteps.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            color = WarmOrangeAccent,
            trackColor = VeryLightBlueGray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Step Content Area
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "step_transition"
            ) { step ->
                when (step) {
                    1 -> WelcomeStepContent()
                    2 -> ProfileStepContent(
                        userName = settings.userName,
                        spiritualIdentity = settings.spiritualIdentity,
                        selectedLoveAyahId = settings.favoriteLoveAyahId,
                        onNameChange = { viewModel.updateUserName(it) },
                        onIdentitySelect = { viewModel.updateSpiritualIdentity(it) },
                        onLoveAyahSelect = { viewModel.updateFavoriteLoveAyah(it) }
                    )
                    3 -> LocationStepContent(
                        currentLocation = location,
                        isLocating = isGpsLocating,
                        errorMessage = locationError,
                        successMessage = locationSuccess,
                        onRequestGps = {
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
                        onSelectCity = { city ->
                            viewModel.clearLocationMessages()
                            viewModel.setManualLocation(
                                city.city,
                                city.country,
                                city.latitude,
                                city.longitude,
                                city.timezone
                            )
                        }
                    )
                    4 -> CalculationStepContent(
                        selectedMethod = settings.calculationMethod,
                        selectedMadhhab = settings.madhhab,
                        selectedTimeFormat = settings.timeFormat,
                        onSelectMethod = { viewModel.updateCalculationMethod(it) },
                        onSelectMadhhab = { viewModel.updateMadhhab(it) },
                        onSelectTimeFormat = { viewModel.updateTimeFormat(it == "24H") }
                    )
                    5 -> AdhanStepContent(
                        tracks = tracks,
                        configs = configs,
                        isAudioPreviewing = isAudioPreviewing,
                        previewingTrackId = previewingTrackId,
                        previewProgress = previewProgress,
                        onPreview = { viewModel.previewTrack(it) },
                        onTogglePrayer = { prayer, enabled -> viewModel.togglePrayerAdhan(prayer, enabled) },
                        onSelectTrackForPrayer = { prayer, trackId -> viewModel.updatePrayerAudioTrack(prayer, trackId) }
                    )
                    6 -> PermissionsStepContent(
                        context = context,
                        isDndGranted = viewModel.quietModeManager.isDndPermissionGranted(),
                        isAccessibilityEnabled = isAccessibilityBlockerEnabled,
                        onOpenAccessibility = { viewModel.openAccessibilitySettings(context) },
                        onRequestNotification = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )
                    7 -> CompleteSetupStepContent(
                        location = location,
                        settings = settings,
                        prayerConfigs = configs
                    )
                }
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 1) {
                OutlinedButton(
                    onClick = { currentStep-- },
                    modifier = Modifier.testTag("onboarding_back_button")
                ) {
                    Text("Back")
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            Button(
                onClick = {
                    if (currentStep < totalSteps) {
                        currentStep++
                    } else {
                        viewModel.stopPreview()
                        viewModel.completeOnboarding()
                        onFinishOnboarding()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("onboarding_next_button")
            ) {
                Text(if (currentStep == totalSteps) "Enter Dashboard" else "Continue")
            }
        }
    }
}

@Composable
private fun WelcomeStepContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BismillahBanner()

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_go_prayer_logo_1789517345937),
                contentDescription = "AL-SUJOOD Logo",
                modifier = Modifier.size(90.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Welcome to AL-SUJOOD",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "An authentic, distraction-free companion engineered to help you observe daily Salah on time with accurate astronomical prayer times, automated Adhan audio, and respectful prayer quiet mode.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProfileStepContent(
    userName: String,
    spiritualIdentity: String,
    selectedLoveAyahId: String,
    onNameChange: (String) -> Unit,
    onIdentitySelect: (String) -> Unit,
    onLoveAyahSelect: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(2.dp))
            BismillahBanner()
        }

        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Profile & Spiritual Identity",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Who I Am: اللہ کا بندہ (Banda-e-Khuda) • Dedicated to purity and goodness",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Section 1: User's Name
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "What should AL-SUJOOD call you?",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = userName,
                        onValueChange = onNameChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_user_name_input"),
                        placeholder = { Text("Enter your name (e.g., Qasim, Ahmad, Abdullah)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Section 2: Who Am I? (میں کون ہوں؟) - Spiritual Affirmation
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F291E)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, AccentGold)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IslamicStarDeco(sizeDp = 22, color = AccentGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Who Am I? • میں کون ہوں؟",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = AccentGold,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "اللہ کا بندہ",
                                color = AccentGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "« میں اللہ کا عاجز بندہ ہوں، میری زندگی، میری نماز اور میرا جینا مرنا صرف اللہ رب العزت کے لیے ہے۔ »",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFF1F5F9),
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Text(
                        text = "I affirm: I am a servant of Allah. My purpose is to turn away from malice, arrogance, and sinful impulses, seeking instead purity of soul, peace of heart, and devotion to my Creator.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCBD5E1),
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Select your spiritual designation:",
                        style = MaterialTheme.typography.labelSmall.copy(color = AccentGoldSoft)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SpiritualVerses.identityOptions.forEach { opt ->
                            val isSelected = opt == spiritualIdentity
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onIdentitySelect(opt) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AccentGold.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) AccentGold else Color.White.copy(alpha = 0.15f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = opt,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isSelected) AccentGold else Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = AccentGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Love & Purification Ayats
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ayats of Divine Love & Purification",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = "دل کو برائی سے اچھائی کی طرف موڑنے والی آیات\nWhenever evil thoughts, anger, or despair strike, these divine verses guide the soul back to righteous peace (تزکیۂ نفس اور اچھے اعمال). Choose your spiritual anchor:",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                )
            }
        }

        items(SpiritualVerses.loveAndPurificationAyats) { ayah ->
            val isSelected = ayah.id == selectedLoveAyahId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLoveAyahSelect(ayah.id) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Surah ${ayah.surahName} (${ayah.surahNumber}:${ayah.ayahNumber})",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = ayah.spiritualTheme,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            RadioButton(
                                selected = isSelected,
                                onClick = { onLoveAyahSelect(ayah.id) },
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Arabic Text
                    Text(
                        text = ayah.arabicText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 28.sp
                        ),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Urdu Translation
                    Text(
                        text = ayah.urduTranslation,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        ),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // English Translation
                    Text(
                        text = "« ${ayah.englishTranslation} »",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 17.sp
                        )
                    )

                    // Heart Guidance Note
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "دل کی اصلاح: ${ayah.heartPurificationNote}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LocationStepContent(
    currentLocation: com.example.data.local.entities.LocationProfile,
    isLocating: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onRequestGps: () -> Unit,
    onSelectCity: (PresetCity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Select Your Location",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Accurate prayer times require precise solar coordinates.",
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Location Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Current: ${currentLocation.city}, ${currentLocation.country}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Lat: %.3f, Lng: %.3f (%s)".format(currentLocation.latitude, currentLocation.longitude, currentLocation.source),
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vibrant Pill GPS Button matching user interface spec
        Button(
            onClick = onRequestGps,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("use_gps_button"),
            enabled = !isLocating,
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10E0B0),
                contentColor = Color(0xFF00382B),
                disabledContainerColor = Color(0xFF10E0B0).copy(alpha = 0.5f),
                disabledContentColor = Color(0xFF00382B).copy(alpha = 0.6f)
            )
        ) {
            if (isLocating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF00382B)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Detecting GPS Coordinates...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Use Device GPS Location",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        if (successMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = successMessage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFA7F3D0),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Or Select a City Manually:",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(POPULAR_CITIES) { city ->
                val isSelected = currentLocation.city == city.city
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCity(city) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${city.city}, ${city.country}",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = city.timezone,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculationStepContent(
    selectedMethod: String,
    selectedMadhhab: String,
    selectedTimeFormat: String,
    onSelectMethod: (CalculationMethod) -> Unit,
    onSelectMadhhab: (Madhhab) -> Unit,
    onSelectTimeFormat: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Calculation Settings",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Choose your preferred astronomical calculation convention.",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        item {
            Text(text = "Calculation Method", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CalculationMethod.values().forEach { method ->
                    val isSelected = selectedMethod == method.name
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectMethod(method) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = { onSelectMethod(method) })
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = method.displayName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                Text(
                                    text = if (method.isIshaFixedMinutes) "Fajr: ${method.fajrAngle}°, Isha: +${method.ishaFixedMinutes} min" else "Fajr: ${method.fajrAngle}°, Isha: ${method.ishaAngle}°",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(text = "Asr Juristic Method (Madhhab)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Madhhab.values().forEach { madhhab ->
                    val isSelected = selectedMadhhab == madhhab.name
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectMadhhab(madhhab) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = madhhab.displayName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                            Text(
                                text = if (madhhab == Madhhab.HANAFI) "Shadow ratio 2x" else "Shadow ratio 1x",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(text = "Time Format", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("12H", "24H").forEach { format ->
                    val isSelected = selectedTimeFormat == format
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectTimeFormat(format) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = if (format == "12H") "12-Hour (AM/PM)" else "24-Hour Military",
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdhanStepContent(
    tracks: List<com.example.data.local.entities.AdhanTrack>,
    configs: List<com.example.data.local.entities.PrayerConfiguration>,
    isAudioPreviewing: Boolean,
    previewingTrackId: String?,
    previewProgress: Float,
    onPreview: (String) -> Unit,
    onTogglePrayer: (String, Boolean) -> Unit,
    onSelectTrackForPrayer: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Adhan Audio & Preferences",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Preview melodious Adhan tracks and configure alerts for each prayer.",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        item {
            Text(text = "Available Adhan Tracks", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        }

        items(tracks) { track ->
            val isCurrentPlaying = isAudioPreviewing && previewingTrackId == track.id
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = track.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(text = track.subtitle, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }

                        IconButton(
                            onClick = { onPreview(track.id) },
                            modifier = Modifier.testTag("preview_adhan_${track.id}")
                        ) {
                            Icon(
                                imageVector = if (isCurrentPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "Preview Audio",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (isCurrentPlaying) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { previewProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            Text(text = "Individual Prayer Adhan Toggles", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        }

        items(configs) { config ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = config.prayerName, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                        Text(
                            text = if (config.adhanEnabled) "Adhan will play" else "Notification only / Muted",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Switch(
                        checked = config.adhanEnabled,
                        onCheckedChange = { onTogglePrayer(config.prayerName, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionsStepContent(
    context: Context,
    isDndGranted: Boolean,
    isAccessibilityEnabled: Boolean,
    onOpenAccessibility: () -> Unit,
    onRequestNotification: () -> Unit
) {
    val isNotificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Required Permissions",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Grant notifications and accessibility service to enable automated prayer alarms and distraction blocking.",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        item {
            // Notification Permission Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNotificationGranted) Color(0xFF047857).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isNotificationGranted) Icons.Default.CheckCircle else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (isNotificationGranted) Color(0xFF059669) else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Prayer Notifications & Warnings", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = if (isNotificationGranted) "✓ Notification Permission Granted" else "Required for Azan, Enter Salah, and 5-min warning alerts",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isNotificationGranted) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isNotificationGranted) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                    if (!isNotificationGranted) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = onRequestNotification, modifier = Modifier.fillMaxWidth()) {
                            Text("Grant Notification Permission")
                        }
                    }
                }
            }
        }

        item {
            // App Blocker Accessibility Service Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAccessibilityEnabled) Color(0xFF047857).copy(alpha = 0.15f) else Color(0xFFD97706).copy(alpha = 0.15f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isAccessibilityEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isAccessibilityEnabled) Color(0xFF059669) else Color(0xFFD97706)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "App Blocker (Disable Apps During Salah)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = if (isAccessibilityEnabled) "✓ Accessibility Service Enabled" else "⚠️ Enable to automatically disable other apps during Salah",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isAccessibilityEnabled) Color(0xFF059669) else Color(0xFFD97706),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "When active, opening games, social media, or video apps during prayer time automatically minimizes them and triggers the urgent Go For Salah reminder.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onOpenAccessibility,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAccessibilityEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isAccessibilityEnabled) "Accessibility is Enabled (Tap to Manage)" else "Enable Accessibility Service")
                    }
                }
            }
        }

        item {
            // Auto-Silent Ringer Mode Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Auto-Silent Ringer During Salah",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Mutes phone ringers during prayer times so you can focus on Salah without interruptions, while ensuring Adhan alarms remain audible.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ready & Active", style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        item {
            // Exact Alarms Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Exact Alarms Scheduling", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "Ensures the Adhan triggers at the exact minute even when device is sleeping.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompleteSetupStepContent(
    location: com.example.data.local.entities.LocationProfile,
    settings: com.example.data.local.entities.UserSettings,
    prayerConfigs: List<com.example.data.local.entities.PrayerConfiguration>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IslamicStarDeco(sizeDp = 48, color = AccentGold)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Setup Completed!",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "AL-SUJOOD is ready to assist you in maintaining your Salah.",
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("User Profile:", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Text(if (settings.userName.isBlank()) "Banda-e-Khuda" else settings.userName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Spiritual Identity:", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Text(settings.spiritualIdentity, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AccentGold))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Heart Anchor:", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    val anchor = SpiritualVerses.getAyatById(settings.favoriteLoveAyahId)
                    Text("Surah ${anchor.surahName} (${anchor.ayahNumber})", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Selected City:", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Text("${location.city}, ${location.country}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Calculation:", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Text(settings.calculationMethod, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Juristic Method:", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Text(settings.madhhab, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Adhan Enabled:", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    val enabledCount = prayerConfigs.count { it.adhanEnabled }
                    Text("$enabledCount of ${prayerConfigs.size} prayers", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
