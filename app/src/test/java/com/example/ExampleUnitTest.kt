package com.example

import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.HijriCalendarHelper
import com.example.domain.prayer.Madhhab
import com.example.domain.prayer.PrayerTimeCalculator
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun prayerTimeCalculator_returnsValidHours() {
    val times = PrayerTimeCalculator.calculateTimes(
      year = 2026,
      month = 9,
      day = 15,
      latitude = 21.4225, // Makkah
      longitude = 39.8262,
      timezoneOffsetHours = 3.0,
      method = CalculationMethod.MAKKAH,
      madhhab = Madhhab.STANDARD
    )

    assertTrue("Fajr should be before Sunrise", times.fajrHours < times.sunriseHours)
    assertTrue("Sunrise should be before Dhuhr", times.sunriseHours < times.dhuhrHours)
    assertTrue("Dhuhr should be before Asr", times.dhuhrHours < times.asrHours)
    assertTrue("Asr should be before Maghrib", times.asrHours < times.maghribHours)
    assertTrue("Maghrib should be before Isha", times.maghribHours < times.ishaHours)
  }

  @Test
  fun qiblaBearing_calculatedCorrectly() {
    // Makkah to Kaaba bearing is self, but from Cairo (30.0444, 31.2357) should point towards SE (~136 degrees)
    val bearing = PrayerTimeCalculator.calculateQiblaBearing(30.0444, 31.2357)
    assertTrue("Bearing from Cairo to Makkah should be approx 130-140 degrees", bearing in 125.0..145.0)
  }

  @Test
  fun hijriCalendar_returnsRealisticValues() {
    val cal = Calendar.getInstance().apply {
      set(2026, Calendar.SEPTEMBER, 15)
    }
    val hijri = HijriCalendarHelper.getHijriDate(cal)
    assertTrue("Hijri month should be between 1 and 12", hijri.month in 1..12)
    assertTrue("Hijri day should be between 1 and 30", hijri.day in 1..30)
    assertTrue("Hijri year should be realistic (>1445)", hijri.year >= 1445)
    assertNotNull(hijri.monthNameEnglish)
    assertNotNull(hijri.monthNameArabic)
  }

  @Test
  fun fahishaAyatList_containsAuthenticVerses() {
    val list = com.example.data.local.SeedData.fahishaAyatList
    assertTrue("Fahisha Ayat list should not be empty", list.isNotEmpty())
    assertEquals(8, list.size)
    list.forEach { ayat ->
      assertEquals("Fahisha & Modesty", ayat.themeTopic)
      assertTrue("Arabic text must be present", ayat.arabicText.isNotBlank())
      assertTrue("English translation must be present", ayat.englishTranslation.isNotBlank())
      assertTrue("Surah name must be present", ayat.surahName.isNotBlank())
      assertTrue("Ayat number must be present", ayat.ayatNumber.isNotBlank())
    }
  }
}
