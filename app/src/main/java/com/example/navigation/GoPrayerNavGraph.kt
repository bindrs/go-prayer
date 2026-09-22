package com.example.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.ui.ayat.AyatScreen
import com.example.ui.azkaar.AzkaarScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.duayn.DuaynScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.prayer.PrayerScreen
import com.example.ui.prayer.PrayerTimesScreen
import com.example.ui.qibla.QiblaCompassScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.theme.DarkTealPrimary
import com.example.ui.theme.TextSecondaryGray
import com.example.ui.theme.WarmOrangeAccent
import com.example.ui.tracker.SalahTrackerScreen
import com.example.ui.viewmodel.PrayerViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object PrayerTimes : Screen("prayer_times")
    object Settings : Screen("settings")
    object AyatLibrary : Screen("ayat_library")
    object SalahTracker : Screen("salah_tracker")
    object Azkaar : Screen("azkaar")
    object Duayn : Screen("duayn")
    object Qibla : Screen("qibla")
    object Prayer : Screen("prayer/{prayerName}") {
        fun createRoute(prayerName: String) = "prayer/$prayerName"
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isCentralAction: Boolean = false
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard.route, "Home", Icons.Default.GridView),
    BottomNavItem(Screen.PrayerTimes.route, "Prayer Times", Icons.Default.CalendarMonth),
    BottomNavItem(Screen.Qibla.route, "Qibla", Icons.Default.Explore, isCentralAction = true),
    BottomNavItem(Screen.SalahTracker.route, "Progress", Icons.Default.BarChart),
    BottomNavItem(Screen.Settings.route, "Profile", Icons.Default.Person)
)

@Composable
fun GoPrayerNavGraph(
    navController: NavHostController,
    viewModel: PrayerViewModel,
    isOnboardingCompleted: Boolean,
    initialRoute: String? = null,
    initialPrayerName: String? = null,
    modifier: Modifier = Modifier
) {
    val startDestination = if (initialRoute == "prayer_screen" && initialPrayerName != null) {
        Screen.Prayer.createRoute(initialPrayerName)
    } else {
        Screen.Splash.route
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.PrayerTimes.route,
        Screen.Qibla.route,
        Screen.SalahTracker.route,
        Screen.Settings.route,
        Screen.Azkaar.route,
        Screen.Duayn.route
    )

    val isDark = isSystemInDarkTheme()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .shadow(elevation = 10.dp, shape = RoundedCornerShape(32.dp), spotColor = Color(0x1A155563))
                        .testTag("main_floating_bottom_navigation_bar"),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFEEF4F4))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                            if (item.isCentralAction) {
                                // Central Elevated Circular Orange Action Button (Qibla / Action)
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(WarmOrangeAccent)
                                        .clickable {
                                            if (!selected) {
                                                navController.navigate(item.route) {
                                                    popUpTo(Screen.Dashboard.route) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                        .testTag("nav_item_${item.route}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        if (!selected) {
                                            navController.navigate(item.route) {
                                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    modifier = Modifier.testTag("nav_item_${item.route}")
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title,
                                            modifier = Modifier.size(22.dp),
                                            tint = if (selected) DarkTealPrimary else TextSecondaryGray
                                        )
                                        if (selected) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(WarmOrangeAccent)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    viewModel = viewModel,
                    onNavigateNext = { destination ->
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    viewModel = viewModel,
                    onFinishOnboarding = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToPrayerScreen = { prayerName ->
                        navController.navigate(Screen.Prayer.createRoute(prayerName))
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToAyatLibrary = {
                        navController.navigate(Screen.AyatLibrary.route)
                    },
                    onNavigateToSalahTracker = {
                        navController.navigate(Screen.SalahTracker.route)
                    },
                    onNavigateToAzkaar = {
                        navController.navigate(Screen.Azkaar.route)
                    },
                    onNavigateToDuayn = {
                        navController.navigate(Screen.Duayn.route)
                    },
                    onNavigateToQibla = {
                        navController.navigate(Screen.Qibla.route)
                    }
                )
            }

            composable(Screen.PrayerTimes.route) {
                PrayerTimesScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToPrayerDetails = { prayerName ->
                        navController.navigate(Screen.Prayer.createRoute(prayerName))
                    }
                )
            }

            composable(Screen.Azkaar.route) {
                AzkaarScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            composable(Screen.Duayn.route) {
                DuaynScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            composable(Screen.SalahTracker.route) {
                SalahTrackerScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.Prayer.route,
                arguments = listOf(navArgument("prayerName") { type = NavType.StringType })
            ) { backStackEntry ->
                val prayerName = backStackEntry.arguments?.getString("prayerName") ?: "Salah"
                PrayerScreen(
                    prayerName = prayerName,
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.AyatLibrary.route) {
                AyatScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSelectAyat = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Qibla.route) {
                QiblaCompassScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
