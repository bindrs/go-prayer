# 🕌 Go Prayer

> A modern Islamic prayer companion built with Kotlin and Jetpack Compose. Providing prayer times, real-time Qibla direction, Adhan alerts, daily Azkaar & Duas, and a Salah streak tracker.

---

## 📥 Download APK

You can download and install the latest compiled debug build directly:

- **[Direct APK Download Link (Primary)](https://ais-dev-jsjp426u4rxkyvun6hu4pc-32032759018.asia-east1.run.app/app-debug.apk)**
- **[Direct APK Download Link (Alternative)](https://ais-pre-jsjp426u4rxkyvun6hu4pc-32032759018.asia-east1.run.app/app-debug.apk)**
- **AI Studio In-Browser Download**: Click the **Install** button located in the top-right corner of the **Preview** panel in AI Studio.
- **Local File Path**: `public/app-debug.apk` / `.build-outputs/app-debug.apk`

---

## ✨ Key Features

### 1. ⏱️ Accurate Prayer Times & Live Countdown
- Calculates **Fajr, Sunrise, Dhuhr, Asr, Maghrib, and Isha** prayer times.
- Supports major international calculation methods:
  - Muslim World League (MWL)
  - Islamic Society of North America (ISNA)
  - Egyptian General Authority of Survey
  - Umm Al-Qura University, Makkah
  - University of Islamic Sciences, Karachi
  - Institute of Geophysics, University of Tehran
  - Shia Ithna-Ashari (Jafari)
- Flexible juristic settings: Standard / Shafi'i vs. Hanafi Asr calculations.
- Live countdown timer to the upcoming prayer with next prayer highlight.
- GPS automatic geolocation with manual city presets worldwide.

### 2. 🧭 Sensor-Based Qibla Compass
- Real-time magnetic azimuth and device sensor integration (Compass & Accelerometer).
- Great-circle navigation pointing directly to the Holy Kaaba in Makkah (21.4225° N, 39.8262° E).
- High-precision visual compass card with degrees and interactive feedback.

### 3. 📢 Adhan Audio & Notifications
- Foreground audio service with customizable Adhan audio tracks (Makkah, Madinah, Al-Aqsa, Abdul Basit, Mishary, Takbeer, and gentle chime).
- In-app audio preview modal with progress bar and volume adjustment.
- High-priority alarms configured via Android `AlarmManager` with notification channels.

### 4. 📿 Daily Azkaar, Duas & Quranic Ayat
- **Morning (Sabah) & Evening (Masaa) Azkaar** with tap counter and completion tracking.
- **Post-Salah Azkar** reminders and guidance following prayer completion.
- Daily curated Quranic reflections with Arabic text, transliteration, and English translation.
- Comprehensive library of authentic Masnoon Duas categorized by daily life occasions.

### 5. 📊 Salah Tracker & Analytics
- Log completed daily prayers (Fajr, Dhuhr, Asr, Maghrib, Isha).
- Monthly analytics with completion percentages and streak milestones.
- Optional Google Calendar sync to automatically record your completed prayers.

### 6. 🛡️ Focus & Distraction Shield
- DND / Quiet mode activation during designated Salah time windows.
- Salah app blocker / focus shield to maintain prayer concentration.
- VPN detection warnings.

---

## 🛠️ Technology Stack & Architecture

- **Language:** 100% Kotlin
- **UI Toolkit:** Jetpack Compose (Modern Material Design 3)
- **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture
- **State Management:** Kotlin Coroutines, `StateFlow`, `collectAsStateWithLifecycle`
- **Navigation:** Jetpack Navigation Compose
- **Local Persistence:** Room Database + Android `SharedPreferences`
- **Audio Engine:** Android `MediaPlayer` & Foreground Services
- **Testing:** Local JVM unit and component testing with Robolectric & Roborazzi

---

## 📱 Installation & Requirements

- **Minimum Android Version:** Android 8.0 (API level 26 - Oreo)
- **Target Android Version:** Android 14 / 15 (API level 34+)
- **Required Hardware Features:**
  - Magnetometer (Compass sensor) & Accelerometer (for Qibla orientation)
  - Location Services (GPS / Network location for prayer coordinate calculation)

### Manual Installation Steps:
1. Download the `app-debug.apk` file from the download link above.
2. If prompted on your device, enable **Install unknown apps** for your browser/file manager.
3. Open the downloaded `.apk` file and tap **Install**.
4. Grant Notification and Location permissions when requested for automated prayer alerts and accurate Qibla direction.

---

## 💻 Building from Source

To build and test the project locally using Gradle:

```bash
# Clone the repository
git clone <repository-url>
cd <project-folder>

# Run unit and Robolectric tests
gradle :app:testDebugUnitTest

# Assemble Debug APK
gradle :app:assembleDebug

# The compiled APK will be located at:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License
This project is open-source and intended for Islamic community benefit.
