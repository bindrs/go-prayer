package com.example.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SeedData
import com.example.data.local.entities.AdhanTrack
import com.example.data.local.entities.Ayat
import com.example.data.local.entities.LocationProfile
import com.example.data.local.entities.PrayerConfiguration
import com.example.data.local.entities.PrayerEventLog
import com.example.data.local.entities.SalahRecord
import com.example.data.local.entities.UserSettings
import com.example.data.repository.PrayerRepository
import com.example.data.repository.SalahMonthlyStats
import com.example.util.CalendarSyncManager
import com.example.util.JsonStorageInfo
import com.example.util.JsonStorageManager
import java.text.SimpleDateFormat
import com.example.domain.audio.AdhanAudioEngine
import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.DayPrayerSchedule
import com.example.domain.prayer.HighLatitudeRule
import com.example.domain.prayer.Madhhab
import com.example.domain.prayer.POPULAR_CITIES
import com.example.domain.prayer.findNearestCity
import com.example.service.AdhanPlaybackService
import com.example.service.PrayerAlarmReceiver
import com.example.service.PrayerScheduler
import com.example.service.QuietModeManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class PrayerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = PrayerRepository(database)
    val audioEngine = AdhanAudioEngine(application)
    val quietModeManager = QuietModeManager(application)
    private val prayerScheduler = PrayerScheduler(application)
    val vpnDetector = com.example.service.VpnDetector(application)

    private val prefs = application.getSharedPreferences("go_prayer_secure_prefs", android.content.Context.MODE_PRIVATE)

    private val initialSettings: UserSettings = run {
        val completed = prefs.getBoolean("is_onboarding_completed", false)
        SeedData.defaultSettings.copy(isOnboardingCompleted = completed)
    }

    val settings: StateFlow<UserSettings> = repository.userSettingsFlow
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialSettings)

    val location: StateFlow<LocationProfile> = repository.locationFlow
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.Eagerly, SeedData.defaultLocation)

    val prayerConfigurations: StateFlow<List<PrayerConfiguration>> = repository.prayerConfigsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, SeedData.defaultConfigurations)

    val adhanTracks: StateFlow<List<AdhanTrack>> = repository.adhanTracksFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, SeedData.defaultAdhanTracks)

    val allAyat: StateFlow<List<Ayat>> = repository.ayatListFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, SeedData.verifiedAyatList)

    val fahishaAyatList: StateFlow<List<Ayat>> = repository.fahishaAyatFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, SeedData.fahishaAyatList)

    val warningAyatList: StateFlow<List<Ayat>> = repository.warningAyatFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, SeedData.warningAyatList)

    private val _isGoingToPray = MutableStateFlow(false)
    val isGoingToPray: StateFlow<Boolean> = _isGoingToPray.asStateFlow()

    private val _isSalahWarningActive = MutableStateFlow(false)
    val isSalahWarningActive: StateFlow<Boolean> = _isSalahWarningActive.asStateFlow()

    private val _warningCount = MutableStateFlow(1)
    val warningCount: StateFlow<Int> = _warningCount.asStateFlow()

    private val _isAccessibilityBlockerEnabled = MutableStateFlow(false)
    val isAccessibilityBlockerEnabled: StateFlow<Boolean> = _isAccessibilityBlockerEnabled.asStateFlow()

    val eventLogs: StateFlow<List<PrayerEventLog>> = repository.eventLogsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val schedule: StateFlow<DayPrayerSchedule?> = repository.scheduleFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isVpnDetected: StateFlow<Boolean> = vpnDetector.isVpnActive
    val vpnDetails: StateFlow<String?> = vpnDetector.vpnInterfaceDetails

    private val _isVpnBannerDismissed = MutableStateFlow(false)
    val isVpnBannerDismissed: StateFlow<Boolean> = _isVpnBannerDismissed.asStateFlow()

    private val _showVpnFahishaModal = MutableStateFlow(false)
    val showVpnFahishaModal: StateFlow<Boolean> = _showVpnFahishaModal.asStateFlow()

    // Post-Salah Locked Azkar Popup State
    private val _showPostSalahAzkarDialog = MutableStateFlow(false)
    val showPostSalahAzkarDialog: StateFlow<Boolean> = _showPostSalahAzkarDialog.asStateFlow()

    private val _postSalahPrayerName = MutableStateFlow("Salah")
    val postSalahPrayerName: StateFlow<String> = _postSalahPrayerName.asStateFlow()

    // 20-Minute Total Disable Prayer Lock State
    private val _isPrayerLocked = MutableStateFlow(false)
    val isPrayerLocked: StateFlow<Boolean> = _isPrayerLocked.asStateFlow()

    private val _lockPrayerName = MutableStateFlow("Salah")
    val lockPrayerName: StateFlow<String> = _lockPrayerName.asStateFlow()

    private var prayerLockJob: kotlinx.coroutines.Job? = null

    private val _currentAyat = MutableStateFlow<Ayat?>(SeedData.verifiedAyatList.first())
    val currentAyat: StateFlow<Ayat?> = _currentAyat.asStateFlow()

    private val _isAudioPreviewing = MutableStateFlow(false)
    val isAudioPreviewing: StateFlow<Boolean> = _isAudioPreviewing.asStateFlow()

    private val _previewProgress = MutableStateFlow(0f)
    val previewProgress: StateFlow<Float> = _previewProgress.asStateFlow()

    private val _previewingTrackId = MutableStateFlow<String?>(null)
    val previewingTrackId: StateFlow<String?> = _previewingTrackId.asStateFlow()

    private val _liveCountdownSeconds = MutableStateFlow(0L)
    val liveCountdownSeconds: StateFlow<Long> = _liveCountdownSeconds.asStateFlow()

    private val _isGpsLocating = MutableStateFlow(false)
    val isGpsLocating: StateFlow<Boolean> = _isGpsLocating.asStateFlow()

    private val _locationErrorMessage = MutableStateFlow<String?>(null)
    val locationErrorMessage: StateFlow<String?> = _locationErrorMessage.asStateFlow()

    private val _locationSuccessMessage = MutableStateFlow<String?>(null)
    val locationSuccessMessage: StateFlow<String?> = _locationSuccessMessage.asStateFlow()

    fun setLocationError(error: String?) {
        _locationErrorMessage.value = error
        _locationSuccessMessage.value = null
    }

    fun clearLocationMessages() {
        _locationErrorMessage.value = null
        _locationSuccessMessage.value = null
    }

    // Salah Tracker & Monthly Records State
    private val _selectedTrackerMonth = MutableStateFlow(
        String.format(
            Locale.US,
            "%04d-%02d",
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH) + 1
        )
    )
    val selectedTrackerMonth: StateFlow<String> = _selectedTrackerMonth.asStateFlow()

    private val _selectedDayForDetail = MutableStateFlow<String?>(null)
    val selectedDayForDetail: StateFlow<String?> = _selectedDayForDetail.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val monthlySalahRecords: StateFlow<List<SalahRecord>> = _selectedTrackerMonth
        .flatMapLatest { monthYear ->
            repository.getMonthlySalahRecordsFlow(monthYear)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val monthlyStats: StateFlow<SalahMonthlyStats> = combine(
        _selectedTrackerMonth,
        monthlySalahRecords
    ) { monthYear, records ->
        repository.calculateMonthlyStats(monthYear, records)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        repository.calculateMonthlyStats(_selectedTrackerMonth.value, emptyList())
    )

    private val _todayDateString = MutableStateFlow(
        String.format(
            Locale.US,
            "%04d-%02d-%02d",
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH) + 1,
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
    )
    val todayDateString: StateFlow<String> = _todayDateString.asStateFlow()

    fun refreshTodayDate() {
        val cal = Calendar.getInstance()
        val dateStr = String.format(
            Locale.US,
            "%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        if (_todayDateString.value != dateStr) {
            _todayDateString.value = dateStr
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val todaySalahRecords: StateFlow<List<SalahRecord>> = _todayDateString
        .flatMapLatest { date ->
            repository.getDailySalahRecordsFlow(date)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        // Ensure database tables and records exist, restoring from on-device JSON if needed
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureInitialized()
            val currentRecords = repository.getAllSalahRecords()
            if (currentRecords.isEmpty()) {
                val backup = JsonStorageManager.restoreDataFromStorage(getApplication())
                if (backup != null && backup.salahRecords.isNotEmpty()) {
                    repository.restoreFromBackup(backup)
                }
            }
            delay(500)
            syncAllDataToJson()
        }

        // Start countdown ticker for real-time dashboard countdown and auto-trigger lock at Azan time
        viewModelScope.launch {
            while (true) {
                val currentSchedule = schedule.value
                val now = System.currentTimeMillis()
                if (currentSchedule?.nextPrayer != null) {
                    val remaining = (currentSchedule.nextPrayer.timestampMillis - now) / 1000
                    _liveCountdownSeconds.value = remaining.coerceAtLeast(0L)
                }

                // Check if any prayer is currently in active Azan / prayer window
                if (currentSchedule != null && settings.value.totalDisableOnPrayer && !_isGoingToPray.value) {
                    val activePrayer = currentSchedule.prayers.firstOrNull { prayer ->
                        val azanTime = if (prayer.azanTimeMillis > 0L) prayer.azanTimeMillis else prayer.timestampMillis
                        val jamatTime = if (prayer.jamatTimeMillis > 0L) prayer.jamatTimeMillis else azanTime + 20 * 60 * 1000L
                        val windowEnd = jamatTime + 20 * 60 * 1000L
                        val isOffered = todaySalahRecords.value.any { it.prayerName.equals(prayer.name, ignoreCase = true) && it.isOffered }
                        now in azanTime..windowEnd && !isOffered
                    }
                    if (activePrayer != null) {
                        if (!com.example.service.SalahAppBlockerService.isBlockerActive) {
                            com.example.service.SalahAppBlockerService.activateBlocker(
                                context = getApplication(),
                                prayerName = activePrayer.name
                            )
                        }

                        if (!_isPrayerLocked.value) {
                            startPrayerLock(activePrayer.name)
                        }
                    }
                }
                delay(1000)
            }
        }

        // Initialize daily random Ayat
        viewModelScope.launch {
            val random = repository.getRandomAyat()
            _currentAyat.value = random
        }

        // Ensure prayer scheduler runs on launch
        prayerScheduler.scheduleUpcomingPrayers()

        // Real-time VPN Fahisha Alert trigger
        viewModelScope.launch {
            isVpnDetected.collect { detected ->
                if (detected && settings.value.vpnFahishaAlertEnabled) {
                    _showVpnFahishaModal.value = true
                    _isVpnBannerDismissed.value = false
                }
            }
        }

        // Auto-check for active prayer lockdown period (from Azan time until 20 mins after Jama'at time)
        viewModelScope.launch {
            delay(1500) // allow schedule to calculate
            val curSchedule = schedule.value
            if (curSchedule != null && settings.value.totalDisableOnPrayer && !_isPrayerLocked.value) {
                val now = System.currentTimeMillis()
                val activePrayer = curSchedule.prayers.firstOrNull { prayer ->
                    val azanTime = if (prayer.azanTimeMillis > 0L) prayer.azanTimeMillis else prayer.timestampMillis
                    val jamatTime = if (prayer.jamatTimeMillis > 0L) prayer.jamatTimeMillis else azanTime + 20 * 60 * 1000L
                    val windowEnd = jamatTime + 20 * 60 * 1000L
                    now in azanTime..windowEnd
                }
                if (activePrayer != null) {
                    val azanTime = if (activePrayer.azanTimeMillis > 0L) activePrayer.azanTimeMillis else activePrayer.timestampMillis
                    val jamatTime = if (activePrayer.jamatTimeMillis > 0L) activePrayer.jamatTimeMillis else azanTime + 20 * 60 * 1000L
                    val windowEnd = jamatTime + 20 * 60 * 1000L
                    val elapsedSeconds = ((now - azanTime) / 1000L).toInt()
                    val totalDurationSecs = ((windowEnd - azanTime) / 1000L).toInt()
                    startPrayerLock(activePrayer.name)
                }
            }
        }
    }

    fun checkIsOnboardingCompleted(): Boolean {
        val fromPrefs = prefs.getBoolean("is_onboarding_completed", false)
        val fromState = settings.value.isOnboardingCompleted
        return fromPrefs || fromState
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean("is_onboarding_completed", true).apply()
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(isOnboardingCompleted = true))
            prayerScheduler.scheduleUpcomingPrayers()
        }
    }

    fun updateCalculationMethod(method: CalculationMethod) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(calculationMethod = method.name))
            prayerScheduler.scheduleUpcomingPrayers()
        }
    }

    fun updateMadhhab(madhhab: Madhhab) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(madhhab = madhhab.name))
            prayerScheduler.scheduleUpcomingPrayers()
        }
    }

    fun updateHighLatitudeRule(rule: HighLatitudeRule) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(highLatitudeRule = rule.name))
            prayerScheduler.scheduleUpcomingPrayers()
        }
    }

    fun updateTimeFormat(is24Hour: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(timeFormat = if (is24Hour) "24H" else "12H"))
        }
    }

    fun updateTheme(themeName: String) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(theme = themeName))
        }
    }

    fun updateLanguage(langCode: String) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(language = langCode))
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(userName = name.trim()))
        }
    }

    fun updateSpiritualIdentity(identity: String) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(spiritualIdentity = identity))
        }
    }

    fun updateFavoriteLoveAyah(ayahId: String) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(favoriteLoveAyahId = ayahId))
        }
    }

    fun saveUserProfile(name: String, identity: String, favoriteLoveAyahId: String) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(
                current.copy(
                    userName = name.trim(),
                    spiritualIdentity = identity,
                    favoriteLoveAyahId = favoriteLoveAyahId
                )
            )
        }
    }

    fun updateQuietMode(enabled: Boolean, durationMinutes: Int) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(
                current.copy(
                    quietModeEnabled = enabled,
                    quietModeDurationMinutes = durationMinutes
                )
            )
            prayerScheduler.scheduleUpcomingPrayers()
        }
    }

    fun updatePrayerTimesSettings(
        prayerName: String,
        manualAdjustmentMinutes: Int,
        azanOffsetMinutes: Int,
        jamatOffsetMinutes: Int,
        enterSalahNotificationEnabled: Boolean
    ) {
        viewModelScope.launch {
            val target = repository.getPrayerConfiguration(prayerName)
                ?: prayerConfigurations.value.find { it.prayerName == prayerName }
            if (target != null) {
                val updated = target.copy(
                    manualAdjustmentMinutes = manualAdjustmentMinutes,
                    azanOffsetMinutes = azanOffsetMinutes,
                    jamatOffsetMinutes = jamatOffsetMinutes,
                    enterSalahNotificationEnabled = enterSalahNotificationEnabled
                )
                repository.updatePrayerConfiguration(updated)
                prayerScheduler.scheduleUpcomingPrayers()
            }
        }
    }

    fun togglePrayerAdhan(prayerName: String, enabled: Boolean) {
        viewModelScope.launch {
            val configs = prayerConfigurations.value
            val target = configs.find { it.prayerName == prayerName }
            if (target != null) {
                repository.updatePrayerConfiguration(target.copy(adhanEnabled = enabled))
                prayerScheduler.scheduleUpcomingPrayers()
            }
        }
    }

    fun togglePrayerQuietMode(prayerName: String, enabled: Boolean) {
        viewModelScope.launch {
            val configs = prayerConfigurations.value
            val target = configs.find { it.prayerName == prayerName }
            if (target != null) {
                repository.updatePrayerConfiguration(target.copy(quietModeEnabled = enabled))
            }
        }
    }

    fun updatePrayerOffset(prayerName: String, offsetMinutes: Int) {
        viewModelScope.launch {
            val configs = prayerConfigurations.value
            val target = configs.find { it.prayerName == prayerName }
            if (target != null) {
                repository.updatePrayerConfiguration(target.copy(manualAdjustmentMinutes = offsetMinutes))
                prayerScheduler.scheduleUpcomingPrayers()
            }
        }
    }

    fun updatePrayerAzanOffset(prayerName: String, azanOffsetMinutes: Int) {
        viewModelScope.launch {
            val configs = prayerConfigurations.value
            val target = configs.find { it.prayerName == prayerName }
            if (target != null) {
                repository.updatePrayerConfiguration(target.copy(azanOffsetMinutes = azanOffsetMinutes))
                prayerScheduler.scheduleUpcomingPrayers()
            }
        }
    }

    fun updatePrayerJamatOffset(prayerName: String, jamatOffsetMinutes: Int) {
        viewModelScope.launch {
            val configs = prayerConfigurations.value
            val target = configs.find { it.prayerName == prayerName }
            if (target != null) {
                repository.updatePrayerConfiguration(target.copy(jamatOffsetMinutes = jamatOffsetMinutes))
                prayerScheduler.scheduleUpcomingPrayers()
            }
        }
    }

    fun updateEnterSalahNotification(prayerName: String, enabled: Boolean) {
        viewModelScope.launch {
            val configs = prayerConfigurations.value
            val target = configs.find { it.prayerName == prayerName }
            if (target != null) {
                repository.updatePrayerConfiguration(target.copy(enterSalahNotificationEnabled = enabled))
                prayerScheduler.scheduleUpcomingPrayers()
            }
        }
    }

    fun checkAccessibilityStatus(context: Context) {
        _isAccessibilityBlockerEnabled.value = com.example.service.SalahAppBlockerService.isAccessibilityServiceEnabled(context)
    }

    fun openAccessibilitySettings(context: Context) {
        try {
            val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("PrayerViewModel", "Error opening accessibility settings: ${e.message}")
        }
    }

    fun onGoingToOfferPrayer(prayerName: String) {
        val targetPrayer = prayerName.ifEmpty { _lockPrayerName.value }
        _isGoingToPray.value = true
        _isSalahWarningActive.value = false
        _isPrayerLocked.value = false
        prayerLockJob?.cancel()
        prayerLockJob = null

        com.example.service.SalahAppBlockerService.setTemporaryBypass(
            durationMinutes = 30,
            prayerName = targetPrayer,
            context = getApplication()
        )
        com.example.service.SalahAppBlockerService.deactivateBlocker(getApplication())
        prayerScheduler.cancel5MinuteWarning(targetPrayer)
        try {
            val warningPrefs = getApplication<Application>().getSharedPreferences("go_prayer_warning_state", Context.MODE_PRIVATE)
            warningPrefs.edit().putBoolean("is_warning_active", false).apply()
        } catch (_: Exception) {}
        try {
            val notificationManager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val warningId = com.example.service.PrayerAlarmReceiver.WARNING_NOTIFICATION_BASE_ID + (targetPrayer.hashCode() % 100)
            notificationManager.cancel(warningId)
            notificationManager.cancel(com.example.service.SalahAppBlockerService.SALAH_BLOCKER_NOTIFICATION_ID)
        } catch (_: Exception) {}
    }

    fun testAppBlocker(context: Context, prayerName: String = "Asr", durationMinutes: Int = 2) {
        com.example.service.SalahAppBlockerService.activateBlocker(
            context = context,
            prayerName = prayerName,
            isTest = true,
            durationMinutes = durationMinutes
        )
    }

    fun testAzkaarReminder(context: Context, type: String = com.example.service.AzkaarReminderManager.TYPE_MORNING) {
        com.example.service.AzkaarReminderManager.sendTestNotification(context, type)
    }

    fun scheduleAllAzkaarReminders(context: Context) {
        com.example.service.AzkaarReminderManager.scheduleAllAzkaarReminders(context)
    }

    fun launchSalahBlockerOverlayDirectly(context: Context, prayerName: String = "Asr") {
        com.example.service.SalahAppBlockerService.showAppBlockedWarningNotification(
            context = context,
            prayerName = prayerName,
            isTest = true
        )
    }

    fun testEnterSalahNotification(context: Context, prayerName: String = "Asr") {
        val now = System.currentTimeMillis()
        com.example.service.PrayerAlarmReceiver.showEnterSalahNotificationDirect(
            context = context,
            prayerName = prayerName,
            azanTime = now + 15 * 60 * 1000L,
            jamatTime = now + 35 * 60 * 1000L
        )
    }

    fun testSalahWarningNotification(context: Context, prayerName: String = "Asr", count: Int = 1) {
        _isSalahWarningActive.value = true
        _warningCount.value = count
        com.example.service.PrayerAlarmReceiver.showSalahWarningNotificationDirect(
            context = context,
            prayerName = prayerName,
            warningCount = count
        )
    }

    fun testPrePrayerReminder(context: Context, prayerName: String = "Asr") {
        com.example.service.PrayerAlarmReceiver.showPrePrayerReminderDirect(
            context = context,
            prayerName = prayerName,
            minutesBefore = 15
        )
    }

    fun restartPrayerForegroundService(context: Context) {
        com.example.service.PrayerForegroundService.startService(context)
        com.example.service.PrayerForegroundService.refresh(context)
    }

    fun clearAllSalahRecords() {
        viewModelScope.launch {
            repository.clearAllSalahRecords()
        }
    }

    fun triggerSalahWarning(prayerName: String, count: Int = 1) {
        _isSalahWarningActive.value = true
        _warningCount.value = count
    }

    fun updatePrayerAudioTrack(prayerName: String, trackId: String) {
        viewModelScope.launch {
            val configs = prayerConfigurations.value
            val target = configs.find { it.prayerName == prayerName }
            if (target != null) {
                repository.updatePrayerConfiguration(target.copy(audioTrackId = trackId))
            }
        }
    }

    fun setManualLocation(city: String, country: String, lat: Double, lng: Double, timezone: String) {
        viewModelScope.launch {
            val newLoc = LocationProfile(
                id = 1,
                city = city,
                country = country,
                latitude = lat,
                longitude = lng,
                timezone = timezone,
                source = "MANUAL",
                updatedAt = System.currentTimeMillis()
            )
            repository.updateLocation(newLoc)
            prayerScheduler.scheduleUpcomingPrayers()
        }
    }

    @Suppress("MissingPermission")
    fun requestGpsLocation(context: Context) {
        _isGpsLocating.value = true
        _locationErrorMessage.value = null
        _locationSuccessMessage.value = null

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val isGpsEnabled = try { locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false } catch (_: Exception) { false }
        val isNetworkEnabled = try { locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false } catch (_: Exception) { false }

        val cachedLocations = mutableListOf<Location>()

        if (locationManager != null) {
            try {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { cachedLocations.add(it) }
            } catch (_: Exception) {}
            try {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let { cachedLocations.add(it) }
            } catch (_: Exception) {}
            try {
                locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)?.let { cachedLocations.add(it) }
            } catch (_: Exception) {}
        }

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { fusedLoc ->
                if (fusedLoc != null) {
                    cachedLocations.add(fusedLoc)
                }
                evaluateAndFinalizeLocation(context, cachedLocations, isGpsEnabled, isNetworkEnabled)
            }.addOnFailureListener {
                evaluateAndFinalizeLocation(context, cachedLocations, isGpsEnabled, isNetworkEnabled)
            }
        } catch (e: Exception) {
            Log.w("PrayerViewModel", "FusedLocationProviderClient unavailable: ${e.message}")
            evaluateAndFinalizeLocation(context, cachedLocations, isGpsEnabled, isNetworkEnabled)
        }
    }

    @SuppressLint("MissingPermission")
    private fun evaluateAndFinalizeLocation(
        context: Context,
        cachedLocations: List<Location>,
        isGpsEnabled: Boolean,
        isNetworkEnabled: Boolean
    ) {
        // If we have any cached location, pick the one with highest accuracy / most recent
        val bestCached = cachedLocations.minByOrNull { it.accuracy }
        if (bestCached != null) {
            viewModelScope.launch(Dispatchers.IO) {
                processAndSaveLocation(context, bestCached)
            }
            return
        }

        // If no cached location, attempt fresh single update from LocationManager
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val providerToUse = when {
            isGpsEnabled -> LocationManager.GPS_PROVIDER
            isNetworkEnabled -> LocationManager.NETWORK_PROVIDER
            else -> null
        }

        if (providerToUse != null && locationManager != null) {
            try {
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationManager.removeUpdates(this)
                        viewModelScope.launch(Dispatchers.IO) {
                            processAndSaveLocation(context, location)
                        }
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }
                locationManager.requestSingleUpdate(providerToUse, listener, Looper.getMainLooper())

                // Timeout fallback after 4 seconds to guarantee the user is never stuck
                viewModelScope.launch {
                    delay(4000)
                    if (_isGpsLocating.value) {
                        try { locationManager.removeUpdates(listener) } catch (_: Exception) {}
                        fallbackToDeviceTimezone(context, "GPS satellite signal pending. Synced to regional timezone.")
                    }
                }
                return
            } catch (e: Exception) {
                Log.w("PrayerViewModel", "requestSingleUpdate error: ${e.message}")
            }
        }

        // Fallback gracefully based on device timezone so the user is never stranded
        fallbackToDeviceTimezone(
            context,
            if (!isGpsEnabled && !isNetworkEnabled)
                "Location services disabled on device. Synced to regional timezone."
            else
                "Could not acquire satellite fix. Synced to regional timezone."
        )
    }

    private fun fallbackToDeviceTimezone(context: Context, reasonMessage: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val defaultTz = TimeZone.getDefault().id ?: "UTC"
            val matchingCity = POPULAR_CITIES.find { it.timezone.equals(defaultTz, ignoreCase = true) }
                ?: POPULAR_CITIES.firstOrNull { it.timezone.contains(defaultTz.substringBefore("/")) }
                ?: POPULAR_CITIES.first()

            val newLoc = LocationProfile(
                id = 1,
                city = matchingCity.city,
                country = matchingCity.country,
                latitude = matchingCity.latitude,
                longitude = matchingCity.longitude,
                timezone = defaultTz,
                source = "TIMEZONE_SYNC",
                updatedAt = System.currentTimeMillis()
            )
            repository.updateLocation(newLoc)
            prayerScheduler.scheduleUpcomingPrayers()
            _isGpsLocating.value = false
            _locationSuccessMessage.value = "Synced to ${matchingCity.city}, ${matchingCity.country} (${reasonMessage})"
            _locationErrorMessage.value = null
        }
    }

    private suspend fun processAndSaveLocation(context: Context, loc: Location) {
        var resolvedCity: String? = null
        var resolvedCountry: String? = null

        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
            val addr = addresses?.firstOrNull()
            if (addr != null) {
                resolvedCity = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                resolvedCountry = addr.countryName
            }
        } catch (e: Exception) {
            Log.w("PrayerViewModel", "Geocoder lookup exception: ${e.message}")
        }

        val nearest = findNearestCity(loc.latitude, loc.longitude)
        val finalCity = resolvedCity ?: nearest.city
        val finalCountry = resolvedCountry ?: nearest.country
        val finalTimezone = TimeZone.getDefault().id ?: nearest.timezone

        val newLoc = LocationProfile(
            id = 1,
            city = finalCity,
            country = finalCountry,
            latitude = loc.latitude,
            longitude = loc.longitude,
            timezone = finalTimezone,
            source = "GPS",
            updatedAt = System.currentTimeMillis()
        )
        repository.updateLocation(newLoc)
        prayerScheduler.scheduleUpcomingPrayers()

        _isGpsLocating.value = false
        _locationErrorMessage.value = null
        _locationSuccessMessage.value = "✓ GPS Location detected: $finalCity, $finalCountry (${String.format(Locale.US, "%.3f", loc.latitude)}°, ${String.format(Locale.US, "%.3f", loc.longitude)}°)"
    }

    fun previewTrack(trackId: String, localUri: String? = null) {
        if (_isAudioPreviewing.value && _previewingTrackId.value == trackId) {
            stopPreview()
            return
        }

        _isAudioPreviewing.value = true
        _previewingTrackId.value = trackId
        _previewProgress.value = 0f

        val effectiveUri = localUri ?: adhanTracks.value.find { it.id == trackId }?.localUri

        audioEngine.playTrack(
            trackId = trackId,
            localUri = effectiveUri,
            volumeFactor = 0.95f,
            onProgress = { progress ->
                _previewProgress.value = progress
            },
            onCompletion = {
                _isAudioPreviewing.value = false
                _previewingTrackId.value = null
                _previewProgress.value = 0f
            }
        )
    }

    fun playPrayerAdhan(prayerName: String) {
        if (_isAudioPreviewing.value) {
            stopPreview()
            return
        }
        val config = prayerConfigurations.value.find { it.prayerName.equals(prayerName, ignoreCase = true) }
        val trackId = config?.audioTrackId ?: "makkah_adhan"
        val track = adhanTracks.value.find { it.id == trackId }
        previewTrack(trackId, track?.localUri)
    }

    fun importCustomAdhan(
        uri: android.net.Uri,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val contentResolver = context.contentResolver

                var fileName = "custom_azan_${System.currentTimeMillis()}"
                var fileExtension = "mp3"
                var fileSize = 0L

                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) {
                            val resolvedName = cursor.getString(nameIndex)
                            if (!resolvedName.isNullOrBlank()) {
                                fileName = resolvedName
                                val dotIndex = resolvedName.lastIndexOf('.')
                                if (dotIndex != -1 && dotIndex < resolvedName.length - 1) {
                                    fileExtension = resolvedName.substring(dotIndex + 1).lowercase(Locale.US)
                                }
                            }
                        }
                        if (sizeIndex != -1) {
                            fileSize = cursor.getLong(sizeIndex)
                        }
                    }
                }

                val targetDir = java.io.File(context.filesDir, "custom_adhans")
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }

                val cleanTrackId = "custom_adhan_${System.currentTimeMillis()}"
                val targetFile = java.io.File(targetDir, "$cleanTrackId.$fileExtension")

                contentResolver.openInputStream(uri)?.use { input ->
                    java.io.FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                } ?: throw Exception("Could not open audio/video file stream")

                // Extract duration and metadata
                var durationSeconds = 180
                var metadataTitle: String? = null
                try {
                    val retriever = android.media.MediaMetadataRetriever()
                    retriever.setDataSource(targetFile.absolutePath)
                    val durStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                    if (!durStr.isNullOrBlank()) {
                        val durMs = durStr.toLongOrNull() ?: 0L
                        if (durMs > 0) {
                            durationSeconds = (durMs / 1000).toInt()
                        }
                    }
                    metadataTitle = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE)
                    retriever.release()
                } catch (e: Exception) {
                    Log.w("PrayerViewModel", "Could not extract media metadata: ${e.message}")
                }

                val displayTitle = if (!metadataTitle.isNullOrBlank()) {
                    metadataTitle
                } else {
                    fileName.substringBeforeLast('.')
                }

                val formatLabel = fileExtension.uppercase(Locale.US)
                val newTrack = AdhanTrack(
                    id = cleanTrackId,
                    title = displayTitle,
                    subtitle = "Imported Azan ($formatLabel • ${durationSeconds}s)",
                    localUri = targetFile.absolutePath,
                    durationSeconds = durationSeconds,
                    isDefault = false
                )

                repository.insertAdhanTrack(newTrack)

                withContext(Dispatchers.Main) {
                    onResult(true, "Successfully imported \"$displayTitle\" ($formatLabel)")
                }
            } catch (e: Exception) {
                Log.e("PrayerViewModel", "Failed to import custom azan: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(false, "Failed to import file: ${e.localizedMessage ?: "Unknown error"}")
                }
            }
        }
    }

    fun deleteCustomAdhan(track: AdhanTrack) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (track.localUri.isNotBlank()) {
                    val file = java.io.File(track.localUri)
                    if (file.exists()) {
                        file.delete()
                    }
                }
                repository.deleteAdhanTrack(track.id)

                // Reset any prayer configured with this deleted track
                val currentConfigs = prayerConfigurations.value
                currentConfigs.forEach { config ->
                    if (config.audioTrackId == track.id) {
                        repository.updatePrayerConfiguration(config.copy(audioTrackId = "makkah_adhan"))
                    }
                }
            } catch (e: Exception) {
                Log.e("PrayerViewModel", "Error deleting custom track: ${e.message}")
            }
        }
    }

    fun assignTrackToAllPrayers(trackId: String) {
        viewModelScope.launch {
            val configs = prayerConfigurations.value
            configs.forEach { config ->
                repository.updatePrayerConfiguration(config.copy(audioTrackId = trackId))
            }
            prayerScheduler.scheduleUpcomingPrayers()
        }
    }

    fun stopPreview() {
        audioEngine.stopPlayback()
        _isAudioPreviewing.value = false
        _previewingTrackId.value = null
        _previewProgress.value = 0f
    }

    fun refreshAyat() {
        viewModelScope.launch {
            _currentAyat.value = repository.getRandomAyat()
        }
    }

    fun selectAyat(ayat: Ayat) {
        _currentAyat.value = ayat
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearEventLogs()
        }
    }

    fun dismissVpnBanner() {
        _isVpnBannerDismissed.value = true
    }

    fun openVpnFahishaModal() {
        _showVpnFahishaModal.value = true
    }

    fun closeVpnFahishaModal() {
        _showVpnFahishaModal.value = false
    }

    fun checkVpnStatus(): Boolean {
        return vpnDetector.checkCurrentVpnStatus()
    }

    fun openVpnSettings() {
        vpnDetector.openVpnSettings()
    }

    fun triggerTestVpnAlert() {
        com.example.service.VpnGuardService.sendTestAlert(getApplication())
    }

    fun updateTotalDisableOnPrayer(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(totalDisableOnPrayer = enabled))
        }
    }

    fun updateVpnFahishaAlert(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(vpnFahishaAlertEnabled = enabled))
        }
    }

    fun startPrayerLock(prayerName: String) {
        val isOffered = todaySalahRecords.value.any { it.prayerName.equals(prayerName, ignoreCase = true) && it.isOffered }
        if (isOffered || _isGoingToPray.value) {
            _isPrayerLocked.value = false
            return
        }
        _isPrayerLocked.value = true
        _lockPrayerName.value = prayerName
        
        quietModeManager.activateQuietMode(prayerName, 20)

        // Activate device-wide accessibility App Blocker service
        try {
            com.example.service.SalahAppBlockerService.activateBlocker(
                context = getApplication(),
                prayerName = prayerName
            )
        } catch (e: Exception) {
            Log.w("PrayerViewModel", "Error activating blocker in startPrayerLock: ${e.message}")
        }
        
        // Stop any old countdowns
        prayerLockJob?.cancel()
        prayerLockJob = null
    }

    fun unlockIfAlreadyOffered(prayerName: String) {
        val targetPrayer = prayerName.ifEmpty { _lockPrayerName.value }
        if (_isPrayerLocked.value) {
            prayerLockJob?.cancel()
            _isPrayerLocked.value = false
            _isGoingToPray.value = true
            _isSalahWarningActive.value = false
            com.example.service.SalahAppBlockerService.deactivateBlocker(getApplication())
            prayerScheduler.cancel5MinuteWarning(targetPrayer)
            try {
                val warningPrefs = getApplication<Application>().getSharedPreferences("go_prayer_warning_state", Context.MODE_PRIVATE)
                warningPrefs.edit().putBoolean("is_warning_active", false).apply()
            } catch (_: Exception) {}
            audioEngine.stopPlayback()
            _isAudioPreviewing.value = false
            quietModeManager.restorePreviousState()
        }
    }

    fun markPrayerCompletedAndUnlock(prayerName: String) {
        val targetPrayer = prayerName.ifEmpty { _lockPrayerName.value }
        prayerLockJob?.cancel()
        _isPrayerLocked.value = false
        _isGoingToPray.value = true
        _isSalahWarningActive.value = false
        com.example.service.SalahAppBlockerService.deactivateBlocker(getApplication())
        prayerScheduler.cancel5MinuteWarning(targetPrayer)
        try {
            val warningPrefs = getApplication<Application>().getSharedPreferences("go_prayer_warning_state", Context.MODE_PRIVATE)
            warningPrefs.edit().putBoolean("is_warning_active", false).apply()
        } catch (_: Exception) {}
        audioEngine.stopPlayback()
        _isAudioPreviewing.value = false
        quietModeManager.restorePreviousState()
        viewModelScope.launch {
            repository.recordPrayerCompletedToday(targetPrayer)
            repository.logPrayerEvent(
                PrayerEventLog(
                    prayerName = targetPrayer,
                    scheduledTime = System.currentTimeMillis(),
                    triggeredAt = System.currentTimeMillis(),
                    adhanStatus = "COMPLETED",
                    quietModeStatus = "PRAYER_OFFERED_RESTORED",
                    screenStatus = "PRAYER_OFFERED_UNLOCKED",
                    errorMessage = null
                )
            )
            // Auto-add to device native calendar
            CalendarSyncManager.autoAddPrayerToCalendar(getApplication(), targetPrayer)
            // Auto-sync data into device JSON file
            syncAllDataToJson()
        }
        showPostSalahAzkar(targetPrayer)
    }

    fun selectTrackerMonth(monthYear: String) {
        _selectedTrackerMonth.value = monthYear
    }

    fun previousTrackerMonth() {
        val parts = _selectedTrackerMonth.value.split("-")
        var year = parts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
        var month = parts.getOrNull(1)?.toIntOrNull() ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)
        month -= 1
        if (month < 1) {
            month = 12
            year -= 1
        }
        _selectedTrackerMonth.value = String.format(Locale.US, "%04d-%02d", year, month)
    }

    fun nextTrackerMonth() {
        val parts = _selectedTrackerMonth.value.split("-")
        var year = parts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
        var month = parts.getOrNull(1)?.toIntOrNull() ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)
        month += 1
        if (month > 12) {
            month = 1
            year += 1
        }
        _selectedTrackerMonth.value = String.format(Locale.US, "%04d-%02d", year, month)
    }

    fun selectDayForDetail(date: String?) {
        _selectedDayForDetail.value = date
    }

    fun showPostSalahAzkar(prayerName: String = "Salah") {
        _postSalahPrayerName.value = prayerName
        _showPostSalahAzkarDialog.value = true
    }

    fun dismissPostSalahAzkar() {
        _showPostSalahAzkarDialog.value = false
    }

    fun toggleSalahOffering(
        date: String,
        prayerName: String,
        isOffered: Boolean,
        offeringType: String = "ON_TIME",
        notes: String? = null
    ) {
        viewModelScope.launch {
            repository.toggleSalahOffered(date, prayerName, isOffered, offeringType, notes)
            if (isOffered) {
                // Auto add to device native calendar
                CalendarSyncManager.autoAddPrayerToCalendar(getApplication(), prayerName)
            }
            // Auto sync data into device JSON file
            syncAllDataToJson()
        }
        if (isOffered) {
            showPostSalahAzkar(prayerName)
        }
    }

    fun offerQazaToday(prayerName: String) {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val dateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
        toggleSalahOffering(dateStr, prayerName, isOffered = true, offeringType = "QAZA")
    }

    fun emergencyUnlockPrayer() {
        prayerLockJob?.cancel()
        _isPrayerLocked.value = false
        _isSalahWarningActive.value = false
        com.example.service.SalahAppBlockerService.deactivateBlocker(getApplication())
        prayerScheduler.cancel5MinuteWarning(_lockPrayerName.value)
        audioEngine.stopPlayback()
        _isAudioPreviewing.value = false
        quietModeManager.restorePreviousState()
        viewModelScope.launch {
            repository.logPrayerEvent(
                PrayerEventLog(
                    prayerName = _lockPrayerName.value,
                    scheduledTime = System.currentTimeMillis(),
                    triggeredAt = System.currentTimeMillis(),
                    adhanStatus = "OVERRIDDEN",
                    quietModeStatus = "EMERGENCY_RESTORED",
                    screenStatus = "UNLOCKED_BY_USER",
                    errorMessage = "Emergency unlock triggered"
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stopPlayback()
        vpnDetector.unregister()
    }

    // JSON Storage & Device Calendar Management
    private val _jsonStorageInfo = MutableStateFlow(
        JsonStorageManager.getStorageInfo(getApplication())
    )
    val jsonStorageInfo: StateFlow<JsonStorageInfo> = _jsonStorageInfo.asStateFlow()

    fun refreshJsonStorageInfo() {
        _jsonStorageInfo.value = JsonStorageManager.getStorageInfo(getApplication())
    }

    fun syncAllDataToJson() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val records = repository.getAllSalahRecords()
                val streaks = repository.getAllQuranStreaks()
                val userSettings = repository.getUserSettings()
                val logs = repository.getRecentEventLogs()
                JsonStorageManager.saveAllDataToJson(
                    context = getApplication(),
                    salahRecords = records,
                    quranStreaks = streaks,
                    settings = userSettings,
                    eventLogs = logs
                )
                withContext(Dispatchers.Main) {
                    refreshJsonStorageInfo()
                }
            } catch (e: Exception) {
                Log.e("PrayerViewModel", "Error saving data to JSON: ${e.message}")
            }
        }
    }

    fun getRawJsonContent(): String {
        return JsonStorageManager.readJsonContent(getApplication())
    }

    fun hasCalendarPermission(): Boolean {
        return CalendarSyncManager.hasCalendarPermission(getApplication())
    }

    fun autoAddPrayerToCalendar(prayerName: String): Boolean {
        return CalendarSyncManager.autoAddPrayerToCalendar(getApplication(), prayerName)
    }
}

class PrayerViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrayerViewModel::class.java)) {
            return PrayerViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
