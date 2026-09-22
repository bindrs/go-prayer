# Go Prayer — Comprehensive Application Functions & Architecture Guide

This document provides a detailed overview of all features, services, database interactions, algorithms, and workflows implemented across the **Go Prayer** Android application.

---

## Table of Contents
1. [Core Purpose & Design Philosophy](#1-core-purpose--design-philosophy)
2. [Prayer Calculations & Timetable Engine](#2-prayer-calculations--timetable-engine)
3. [Intelligent Prayer Tracking & Automatic Qaza Workflow](#3-intelligent-prayer-tracking--automatic-qaza-workflow)
4. [Salah App Blocker & Focus Guard (Khushu Mode)](#4-salah-app-blocker--focus-guard-khushu-mode)
5. [VPN Guard & Fahisha Shield](#5-vpn-guard--fahisha-shield)
6. [Adhan Audio & Notification Delivery System](#6-adhan-audio--notification-delivery-system)
7. [Persistent Background Service & Dynamic Countdown](#7-persistent-background-service--dynamic-countdown)
8. [Qibla Direction & Islamic Utilities](#8-qibla-direction--islamic-utilities)
9. [Local Data Persistence (Room Database & State Management)](#9-local-data-persistence-room-database--state-management)
10. [Complete UI & Navigation Audit](#10-complete-ui--navigation-audit)

---

## 1. Core Purpose & Design Philosophy

**Go Prayer** is an offline-first, privacy-focused Islamic productivity and spiritual protection suite. It combines:
- Precise astronomical prayer time calculations.
- An interactive Salah tracker that holds users accountable for on-time and Qaza (missed) prayers.
- Digital detox and app blocking during prayer times.
- Spiritual protection through proactive VPN and illicit browsing detection (Fahisha Shield).

---

## 2. Prayer Calculations & Timetable Engine

The core astronomical calculation engine is implemented in `PrayerTimeCalculator.kt` and `PrayerRepository.kt`.

### 2.1 Supported Calculation Methods
- **University of Islamic Sciences, Karachi (UISK)** (Fajr: 18°, Isha: 18°)
- **Muslim World League (MWL)** (Fajr: 18°, Isha: 17°)
- **Islamic Society of North America (ISNA)** (Fajr: 15°, Isha: 15°)
- **Umm Al-Qura University, Makkah** (Fajr: 18.5°, Isha: 90 min after Maghrib)
- **Egyptian General Authority of Survey** (Fajr: 19.5°, Isha: 17.5°)
- **Institute of Geophysics, University of Tehran** (Fajr: 17.7°, Maghrib: 4.5°, Isha: 14°)
- **Gulf Region** (Fajr: 19.5°, Isha: 90 min after Maghrib)
- **Kuwait / Qatar / Majlis Ugama Islam Singapura (MUIS)**

### 2.2 Madhhab & Asr Calculation
- **Shafi'i / Maliki / Hanbali / Ja'fari**: Shadow factor = 1 ($T = \text{noon shadow} + \text{object length}$).
- **Hanafi**: Shadow factor = 2 ($T = \text{noon shadow} + 2 \times \text{object length}$).

### 2.3 High Latitude Adjustments
For extreme latitudes (e.g., Scandinavia, Northern Canada, UK in summer):
- **Middle of the Night Rule**
- **One Seventh of the Night Rule**
- **Angle-Based Rule**

### 2.4 Three-Tier Offset System per Prayer
Every prayer (Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha) features three independent timing controls:
1. **Enter Salah Time (Base Time + Manual Offset)**: Astronomical solar entry time modified by user adjustment ($-30$ to $+30$ minutes).
2. **Azan Offset Time**: When the Adhan sounds relative to entry time.
3. **Jama'at Offset Time**: When congregational prayer takes place at the local mosque.

---

## 3. Intelligent Prayer Tracking & Automatic Qaza Workflow

### 3.1 Linked Prayer States & Automatic Transitions
Each prayer for today exists in one of three primary states:
1. **Upcoming**: The prayer time has not arrived yet. Displays a live countdown timer and normal theme colors.
2. **Unoffered / Missed (🔴 Red Alert)**: The prayer time has passed, but the user has not marked the prayer as offered.
   - The card dynamically shifts to **Crimson Red (`#450A0A` / `#EF4444`)**.
   - Displays the **"🔴 UNOFFERED (قضاء باقی)"** warning badge.
   - Provides an immediate **"Offer Qaza Now (قضاء)"** button.
3. **Offered (✓ Emerald Green)**: Marked as completed.
   - Displays offering category: **On-Time**, **With Jama'at (Congregation)**, or **Qaza (Made up)**.

### 3.2 One-Tap Qaza Entry (No Manual Calendar Hassle)
- Tapping on any red unoffered prayer card or pressing the *Offer Qaza* button launches the **Solemn Prayer Dialog**.
- Selecting the offering type immediately records the prayer in the local Room database (`SalahRecord`).
- The monthly calendar view automatically updates day streaks and completion percentages without requiring manual table editing.

### 3.3 Monthly Habit Calendar & Analytics
- **Visual Grid**: 30-day overview with color-coded markers for each prayer (Green for offered, Purple for Qaza, Red for missed).
- **Streak & Consistency Metrics**: Computes daily prayer adherence percentage, Jama'at frequency, and overall punctuality.

---

## 4. Salah App Blocker & Focus Guard (Khushu Mode)

Implemented via `SalahAppBlockerService.kt`, `SalahBlockerOverlayActivity.kt`, and `QuietModeManager.kt`.

### 4.1 Automated Distraction Shield & App Interception
- Uses the Android `AccessibilityService` (`SalahAppBlockerService`) with `canRetrieveWindowContent="true"` and `typeWindowStateChanged | typeWindowsChanged` events to intercept prohibited application launches.
- **Trigger**: When Azan time arrives or when Total Disable mode is active during a prayer window, the blocker automatically arms for the designated duration (e.g. 20-25 minutes).
- **Interception Mechanism**:
  1. When the user attempts to launch or switch to any other app (such as YouTube, WhatsApp, games, or social media), `SalahAppBlockerService` minimizes the prohibited app instantly using `performGlobalAction(GLOBAL_ACTION_HOME)`.
  2. Launches `SalahBlockerOverlayActivity` with `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP | FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS`.
  3. Displays an authentic Quranic verse on Salah with full Arabic calligraphy, English and Urdu translations, Surah and Ayah citation.
  4. Whitelists essential system components (Phone Dialers, Emergency calls, System UI, and Accessibility configuration).

### 4.2 Popup Options & Grace Mechanism
- **🕌 Go to Prayer Screen Now**: Redirects the user directly into the active prayer screen with prayer guidelines, Adhan player, and Dhikr counters.
- **✓ I Am Going to Offer Prayer (20 Min Grace)**: Unlocks the device temporarily for 20 minutes so the user can perform Wudu or walk to the mosque without popup interruptions.
- **⚠️ Emergency Call & Safety Unlock**: Complies with Android safety policies by providing a one-tap phone dialer for urgent emergencies.

### 4.3 Do Not Disturb (DND) & Ringer Automation
- Automatically silences phone ringers or engages Android **Do Not Disturb** mode during Jama'at times via `QuietModeManager`.
- Restores original ringer volumes once the prayer period concludes.

### 4.4 Testing & Verification Utilities
- **Test 2-Min Blocker**: Allows the user in Settings to test the accessibility blocker immediately for 2 minutes to confirm behavior with any app on device.
- **Preview "Go For Salah" Popup**: Allows instant preview of the Quranic Ayat overlay dialog directly from Settings.

---

## 5. VPN Guard & Fahisha Shield

Implemented via `VpnGuardService.kt` and `VpnDetector.kt`.

### 5.1 Dual-Method VPN Detection
1. **NetworkCapabilities API**: Inspects active Android network transports for `TRANSPORT_VPN`.
2. **NetworkInterface Inspection**: Scans network interfaces for virtual adapters (`tun0`, `ppp0`, `p2p0`, `tap0`, `wireguard`).

### 5.2 Spiritual Protection & Fahisha Warnings
- When a VPN connection is initiated, the service alerts the user with an Islamic reflection notification (reminding that Allah is All-Seeing).
- If configured, it launches the **Fahisha Shield Dialog** showing Quranic reminders (Surah Al-Isra, Surah An-Nur) to encourage modesty and conscious online behavior.

---

## 6. Adhan Audio & Notification Delivery System

Implemented in `AdhanPlaybackService.kt` and `PrayerAlarmReceiver.kt`.

### 6.1 Audio Playback Architecture
- **Makkah, Madinah, Al-Aqsa, Abdul Basit, Mishary Rashid Al-Afasy, and Custom Audio** tracks.
- Uses Android `MediaPlayer` combined with `AudioManager` audio focus requests.
- Full volume override option for Fajr wake-up alarms.
- Supports progressive fade-in to gently awaken worshippers.

### 6.2 Precise Scheduling (`AlarmManager`)
- Schedules exact alarms via `AlarmManager.setExactAndAllowWhileIdle()` across device reboots (`BootReceiver.kt`).
- Fallback vibration patterns and high-priority heads-up notifications if device is in silent mode.

---

## 7. Persistent Background Service & Dynamic Countdown

Implemented in `VpnGuardService.kt`.

### 7.1 Real-Time Ongoing Notification
- Runs as a persistent foreground service with an un-intrusive notification.
- Displays:
  - **Live Next Prayer & Countdown**: e.g., `🕌 Go Prayer • Next: Asr at 4:32 PM (in 1h 15m)`.
  - **Today's Complete Timetable**: Compact summary of Fajr, Dhuhr, Asr, Maghrib, and Isha.
  - **Hijri Date & Location**: e.g., `📍 Karachi • 24 Safar 1448 AH`.
  - **Quick Action Buttons**: "Open Go Prayer" and "Prayer View".

---

## 8. Qibla Direction & Islamic Utilities

- **Astronomical Qibla Calculation**: Uses Great-Circle geodesic equations relative to the Kaaba coordinates ($21.4225^\circ\text{N}, 39.8262^\circ\text{E}$).
- **Sensor Fusion**: Integrates device accelerometer and geomagnetic field sensors with low-pass filtering for smooth needle animation.
- **Hijri Calendar Converter**: Converts Gregorian dates to Islamic lunar dates with customizable $\pm 2$ day adjustments for moon sightings.

---

## 9. Local Data Persistence (Room Database & State Management)

The app utilizes **Room Database** (`AppDatabase.kt`) with Kotlin Flow:

| Entity | Purpose |
| :--- | :--- |
| `UserSettings` | Stores calculation method, madhhab, high-latitude rule, DND preferences, and theme. |
| `UserLocation` | Caches current latitude, longitude, city name, country, and time zone. |
| `PrayerConfig` | Stores per-prayer manual offsets, Azan offsets, Jama'at offsets, and Adhan audio track. |
| `SalahRecord` | Daily tracking database recording prayer status (`OFFERED`, `MISSED`, `QAZA`, `CONGREGATION`). |
| `AdhanTrack` | Metadata for built-in and user-imported Adhan audio files. |
| `EventLog` | Audit log of Adhan triggers, VPN detections, and blocker events. |

---

## 10. Complete UI & Navigation Audit

| Screen | Primary Functionality |
| :--- | :--- |
| **Dashboard** | Hero countdown card, linked red/green prayer status cards, quick Qaza dialog, daily timetable, and city selection. |
| **Prayer View** | Full-screen immersive prayer guide, live Adhan audio player, step-by-step rakat guide, and post-Salah dhikr counter. |
| **Salah Tracker** | Monthly calendar heatmap, unoffered prayer list, daily completion analytics, and prayer streak metrics. |
| **Settings** | Calculation method selector, 3-tier offset adjustment modal with live preview, app blocker controls, and audio test suite. |
| **VPN / Guard** | Fahisha Shield configuration, VPN detection sensitivity, and spiritual reminder preferences. |

---

*Document compiled for the Go Prayer codebase.*
