package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.ui.azkaar.AzkaarScreen
import com.example.ui.theme.GoPrayerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Go Prayer", appName)
  }

  @Test
  fun `azkaar screen renders without crashing`() {
    composeTestRule.setContent {
      GoPrayerTheme {
        AzkaarScreen()
      }
    }
  }

  @Test
  fun `splash screen renders without crashing`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val vm = com.example.ui.viewmodel.PrayerViewModel(context as android.app.Application)
    composeTestRule.setContent {
      GoPrayerTheme {
        com.example.ui.splash.SplashScreen(viewModel = vm, onNavigateNext = {})
      }
    }
  }

  @Test
  fun `main activity launches without crashing`() {
    androidx.test.core.app.ActivityScenario.launch(MainActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        org.junit.Assert.assertNotNull(activity)
      }
    }
  }
}
