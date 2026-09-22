package com.example.ui.tracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.BorderStroke
import com.example.data.local.entities.SalahRecord
import com.example.data.repository.SalahMonthlyStats
import com.example.ui.components.PostSalahAzkarDialog
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGreenVibrant
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.PrayerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalahTrackerScreen(
    viewModel: PrayerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedMonth by viewModel.selectedTrackerMonth.collectAsStateWithLifecycle()
    val monthlyRecords by viewModel.monthlySalahRecords.collectAsStateWithLifecycle()
    val monthlyStats by viewModel.monthlyStats.collectAsStateWithLifecycle()
    val selectedDayForDetail by viewModel.selectedDayForDetail.collectAsStateWithLifecycle()
    val todayDateString by viewModel.todayDateString.collectAsStateWithLifecycle()
    val showPostSalahAzkarDialog by viewModel.showPostSalahAzkarDialog.collectAsStateWithLifecycle()
    val postSalahPrayerName by viewModel.postSalahPrayerName.collectAsStateWithLifecycle()

    var filterMode by remember { mutableStateOf("ALL") } // "ALL", "OFFERED", "MISSED"
    var activeTab by remember { mutableIntStateOf(0) } // 0: Overview & Calendar, 1: Full History Log

    // State for the Solemn "I Have Offered My Prayer" Dialog
    var pendingSolemnPrayer by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair(date, prayerName)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Salah Progress & History",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Track & Foster Daily Devotion",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = com.example.ui.theme.WarmOrangeAccent,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("salah_tracker_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            val cur = String.format(Locale.US, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
                            viewModel.selectTrackerMonth(cur)
                        },
                        modifier = Modifier.testTag("salah_tracker_today_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Current Month",
                            tint = com.example.ui.theme.WarmOrangeAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.example.ui.theme.PrimaryDeepTeal
                )
            )
        },
        modifier = modifier.testTag("salah_tracker_screen_root")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Navigator Header
            item {
                Spacer(modifier = Modifier.height(4.dp))
                MonthNavigatorCard(
                    monthDisplayName = monthlyStats.monthDisplayName,
                    onPrevClick = { viewModel.previousTrackerMonth() },
                    onNextClick = { viewModel.nextTrackerMonth() }
                )
            }

            // Monthly Overview Hero Gauge
            item {
                MonthlyOverviewHeroCard(stats = monthlyStats)
            }

            // Today's Devotion Bar - Opens Solemn "I Have Offered My Prayer" Dialog
            item {
                TodayQuickCheckCard(
                    todayDate = todayDateString,
                    records = monthlyRecords.filter { it.date == todayDateString },
                    onOpenSolemnOffer = { prayerName ->
                        pendingSolemnPrayer = Pair(todayDateString, prayerName)
                    }
                )
            }

            // View Selector Tabs (Overview & Calendar vs Detailed Records List)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        label = { Text("Calendar & Breakdown") },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    FilterChip(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        label = { Text("Full History Log") },
                        leadingIcon = {
                            Icon(Icons.Default.EventNote, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            if (activeTab == 0) {
                // 5 Prayers Breakdown
                item {
                    Text(
                        text = "Prayers Breakdown in Month",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PrayersBreakdownRow(stats = monthlyStats)
                }

                // Interactive Calendar Grid
                item {
                    Text(
                        text = "Monthly Salah Calendar",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MonthlyCalendarGrid(
                        monthYear = selectedMonth,
                        records = monthlyRecords,
                        todayDate = todayDateString,
                        onDayClick = { dateStr ->
                            viewModel.selectDayForDetail(dateStr)
                        }
                    )
                }
            } else {
                // Detailed Monthly Records Filter Chips
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterMode == "ALL",
                            onClick = { filterMode = "ALL" },
                            label = { Text("All (${monthlyRecords.size})") }
                        )
                        FilterChip(
                            selected = filterMode == "OFFERED",
                            onClick = { filterMode = "OFFERED" },
                            label = { Text("Offered (${monthlyStats.totalOffered})") }
                        )
                        FilterChip(
                            selected = filterMode == "MISSED",
                            onClick = { filterMode = "MISSED" },
                            label = { Text("Missed (${monthlyStats.missedCount})") }
                        )
                    }
                }

                // Detailed Record Cards
                val filteredRecords = when (filterMode) {
                    "OFFERED" -> monthlyRecords.filter { it.isOffered }
                    "MISSED" -> monthlyRecords.filter { !it.isOffered }
                    else -> monthlyRecords
                }

                if (filteredRecords.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No records found for this filter",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                } else {
                    val groupedByDate = filteredRecords.groupBy { it.date }.toSortedMap(compareByDescending { it })
                    groupedByDate.forEach { (date, recordsForDay) ->
                        item {
                            DailyRecordCard(
                                date = date,
                                records = recordsForDay,
                                onOpenSolemnOffer = { prayerName ->
                                    pendingSolemnPrayer = Pair(date, prayerName)
                                }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Day Details Dialog / Sheet
    if (selectedDayForDetail != null) {
        val detailDate = selectedDayForDetail!!
        val dayRecords = monthlyRecords.filter { it.date == detailDate }
        DayDetailDialog(
            date = detailDate,
            records = dayRecords,
            onDismiss = { viewModel.selectDayForDetail(null) },
            onOpenSolemnOffer = { prayerName ->
                pendingSolemnPrayer = Pair(detailDate, prayerName)
            },
            onRemoveOffering = { prayerName ->
                viewModel.toggleSalahOffering(detailDate, prayerName, false, "ON_TIME")
            }
        )
    }

    // Solemn "I Have Offered My Prayer" Verification Dialog (Allah is watching you)
    if (pendingSolemnPrayer != null) {
        val (pDate, pPrayer) = pendingSolemnPrayer!!
        val existingRecord = monthlyRecords.find { it.date == pDate && it.prayerName == pPrayer }
        SolemnPrayerOfferDialog(
            date = pDate,
            prayerName = pPrayer,
            existingRecord = existingRecord,
            onDismiss = { pendingSolemnPrayer = null },
            onConfirmOffered = { offeringType ->
                viewModel.toggleSalahOffering(pDate, pPrayer, true, offeringType)
                pendingSolemnPrayer = null
            },
            onRemoveOffered = {
                viewModel.toggleSalahOffering(pDate, pPrayer, false, "ON_TIME")
                pendingSolemnPrayer = null
            }
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
fun MonthNavigatorCard(
    monthDisplayName: String,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("salah_tracker_month_navigator"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevClick,
                modifier = Modifier.testTag("salah_tracker_prev_month_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = monthDisplayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            IconButton(
                onClick = onNextClick,
                modifier = Modifier.testTag("salah_tracker_next_month_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MonthlyOverviewHeroCard(
    stats: SalahMonthlyStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("salah_tracker_hero_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF062E25)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D4438),
                            Color(0xFF031A15)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MONTHLY SALAH OFFERED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AccentGold,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${stats.totalOffered}",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = " / ${stats.totalPossible} Salahs",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFFA7F3D0)
                                ),
                                modifier = Modifier.padding(bottom = 4.dp, start = 6.dp)
                            )
                        }
                    }

                    // Circular Percentage Gauge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(72.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF134E4A),
                            strokeWidth = 7.dp
                        )
                        CircularProgressIndicator(
                            progress = { (stats.completionPercentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxSize(),
                            color = if (stats.completionPercentage >= 80) AccentGreenVibrant else AccentGold,
                            strokeWidth = 7.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${stats.completionPercentage}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }

                // Streak & Breakdown Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFB45309).copy(alpha = 0.25f),
                        border = ButtonDefaults.outlinedButtonBorder().copy(
                            brush = Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${stats.currentStreakDays} Day Streak",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFEF3C7)
                                )
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF065F46).copy(alpha = 0.35f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${stats.congregationCount} Jama'at",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA7F3D0)
                                )
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${stats.onTimeCount} On Time",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE2E8F0)
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
fun TodayQuickCheckCard(
    todayDate: String,
    records: List<SalahRecord>,
    onOpenSolemnOffer: (prayerName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("salah_tracker_today_quick_check"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Today's Salah Devotion",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Tap prayer to open 'I Have Offered' confirmation",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                val offeredTodayCount = records.count { it.isOffered }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (offeredTodayCount == 5) Color(0xFF047857) else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$offeredTodayCount / 5 Offered",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (offeredTodayCount == 5) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                prayers.forEach { prayer ->
                    val record = records.find { it.prayerName == prayer }
                    val isOffered = record?.isOffered == true

                    val bgColor by animateColorAsState(
                        targetValue = if (isOffered) Color(0xFF047857) else MaterialTheme.colorScheme.surfaceVariant,
                        label = "quick_check_bg"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isOffered) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "quick_check_content"
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = bgColor,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenSolemnOffer(prayer) }
                            .testTag("today_prayer_toggle_$prayer")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isOffered) Icons.Default.Check else getPrayerIcon(prayer),
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = prayer,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = contentColor
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
fun PrayersBreakdownRow(
    stats: SalahMonthlyStats,
    modifier: Modifier = Modifier
) {
    val prayers = listOf(
        Triple("Fajr", stats.fajrCount, Icons.Default.WbTwilight),
        Triple("Dhuhr", stats.dhuhrCount, Icons.Default.WbSunny),
        Triple("Asr", stats.asrCount, Icons.Default.WbSunny),
        Triple("Maghrib", stats.maghribCount, Icons.Default.WbTwilight),
        Triple("Isha", stats.ishaCount, Icons.Default.NightsStay)
    )

    val maxDays = (stats.totalPossible / 5).coerceAtLeast(1)

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(prayers) { (name, count, icon) ->
            val percentage = (count * 100) / maxDays
            Card(
                modifier = Modifier
                    .width(130.dp)
                    .testTag("prayer_breakdown_card_$name"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = "$count / $maxDays days",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )

                    LinearProgressIndicator(
                        progress = { (count.toFloat() / maxDays).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyCalendarGrid(
    monthYear: String,
    records: List<SalahRecord>,
    todayDate: String,
    onDayClick: (dateStr: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val parts = monthYear.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
    val month = parts.getOrNull(1)?.toIntOrNull() ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)

    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month - 1)
    cal.set(Calendar.DAY_OF_MONTH, 1)

    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    // 1: Sunday, 2: Monday, ..., 7: Saturday. Adjust to Monday = 0
    val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0, Sunday = 6

    val recordsByDay = records.groupBy { it.day }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("salah_tracker_calendar_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Days of Week Header (Mon - Sun)
            val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Calendar Days Grid (Rows of 7)
            val totalCells = firstDayOfWeek + daysInMonth
            val totalRows = (totalCells + 6) / 7

            for (row in 0 until totalRows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - firstDayOfWeek + 1

                        if (dayNumber in 1..daysInMonth) {
                            val dateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month, dayNumber)
                            val isToday = dateStr == todayDate
                            val dayRecordList = recordsByDay[dayNumber] ?: emptyList()
                            val offeredCount = dayRecordList.count { it.isOffered }

                            CalendarDayCell(
                                dayNumber = dayNumber,
                                isToday = isToday,
                                offeredCount = offeredCount,
                                records = dayRecordList,
                                onClick = { onDayClick(dateStr) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // Empty cell spacer
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Legend Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = Color(0xFF047857), label = "5/5 Full")
                LegendItem(color = Color(0xFFF59E0B), label = "1-4 Partial")
                LegendItem(color = MaterialTheme.colorScheme.primary, label = "Today", isTodayRing = true)
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    dayNumber: Int,
    isToday: Boolean,
    offeredCount: Int,
    records: List<SalahRecord>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
        border = if (isToday) ButtonDefaults.outlinedButtonBorder().copy(
            brush = Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, AccentGold))
        ) else null,
        modifier = modifier
            .padding(2.dp)
            .clickable { onClick() }
            .testTag("calendar_day_$dayNumber")
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$dayNumber",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(3.dp))

            // 5 Dots representing 5 prayers
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
                prayers.forEach { prayer ->
                    val isOffered = records.find { it.prayerName == prayer }?.isOffered == true
                    Box(
                        modifier = Modifier
                            .size(3.5.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOffered) Color(0xFF047857) else Color(0xFFCBD5E1)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String,
    isTodayRing: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isTodayRing) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .border(1.5.dp, color, CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}

@Composable
fun DailyRecordCard(
    date: String,
    records: List<SalahRecord>,
    onOpenSolemnOffer: (prayerName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_record_card_$date"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatDisplayDate(date),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                val offeredCount = records.count { it.isOffered }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (offeredCount == 5) Color(0xFF047857) else if (offeredCount > 0) Color(0xFFD97706) else Color(0xFF64748B)
                ) {
                    Text(
                        text = "$offeredCount / 5",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // List of prayers for this day
            val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                prayers.forEach { prayerName ->
                    val rec = records.find { it.prayerName == prayerName }
                    val isOffered = rec?.isOffered == true
                    val type = rec?.offeringType ?: "ON_TIME"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isOffered) Color(0xFF047857).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                            .clickable { onOpenSolemnOffer(prayerName) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isOffered) Icons.Default.CheckCircle else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isOffered) Color(0xFF047857) else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = prayerName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isOffered) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isOffered) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (type) {
                                        "CONGREGATION" -> Color(0xFF065F46)
                                        "QAZA" -> Color(0xFF78350F)
                                        else -> Color(0xFF134E4A)
                                    }
                                ) {
                                    Text(
                                        text = when (type) {
                                            "CONGREGATION" -> "Jama'at"
                                            "QAZA" -> "Qaza"
                                            else -> "On Time"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "Tap to confirm offering",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayDetailDialog(
    date: String,
    records: List<SalahRecord>,
    onDismiss: () -> Unit,
    onOpenSolemnOffer: (prayerName: String) -> Unit,
    onRemoveOffering: (prayerName: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = AccentGold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Day Salah Record",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = formatDisplayDate(date),
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tap any prayer to record solemn 'I Have Offered' testimony:",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
                prayers.forEach { prayerName ->
                    val rec = records.find { it.prayerName == prayerName }
                    val isOffered = rec?.isOffered == true
                    val currentType = rec?.offeringType ?: "ON_TIME"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenSolemnOffer(prayerName) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOffered) Color(0xFF047857).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = getPrayerIcon(prayerName),
                                    contentDescription = null,
                                    tint = if (isOffered) Color(0xFF047857) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = prayerName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    if (isOffered) {
                                        Text(
                                            text = when (currentType) {
                                                "CONGREGATION" -> "✓ Offered in Jama'at"
                                                "QAZA" -> "✓ Offered as Qaza"
                                                else -> "✓ Offered On Time"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF047857),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = "Not logged yet",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF94A3B8),
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { onOpenSolemnOffer(prayerName) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isOffered) Color(0xFF047857) else MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isOffered) "Offered ✓" else "I Offered",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGold,
                    contentColor = Color(0xFF022C22)
                )
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun SolemnPrayerOfferDialog(
    date: String,
    prayerName: String,
    existingRecord: SalahRecord?,
    onDismiss: () -> Unit,
    onConfirmOffered: (offeringType: String) -> Unit,
    onRemoveOffered: () -> Unit
) {
    var selectedType by remember {
        mutableStateOf(existingRecord?.offeringType ?: "ON_TIME")
    }

    val isAlreadyOffered = existingRecord?.isOffered == true

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = AccentGold.copy(alpha = 0.15f),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "إِنَّ اللَّهَ كَانَ عَلَيْكُمْ رَقِيبًا",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AccentGold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                )
                Text(
                    text = "Allah Is Watching You",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَّوْقُوتًا",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "\"Indeed, prayer has been decreed upon the believers a decree of specified times.\" (Surah An-Nisa 4:103)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Text(
                    text = "Do you bear witness before Allah that you have genuinely performed your $prayerName Salah for ${formatDisplayDate(date)}?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Select Offering Manner:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                val options = listOf(
                    Triple("CONGREGATION", "🕌 In Jama'at (باجماعت)", "Performed in congregation"),
                    Triple("ON_TIME", "⏱️ On Time (بروقت)", "Performed within time limit"),
                    Triple("QAZA", "⏳ Qaza (قضاء)", "Offered as make-up prayer")
                )

                options.forEach { (typeKey, label, desc) ->
                    val isSelected = selectedType == typeKey
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF047857).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected) ButtonDefaults.outlinedButtonBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFF047857), AccentGold))) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = typeKey }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF047857) else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF047857),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmOffered(selectedType) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF047857),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAlreadyOffered) "Update Prayer Record" else "I Have Offered My Prayer",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            if (isAlreadyOffered) {
                TextButton(
                    onClick = onRemoveOffered,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove", fontSize = 12.sp)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

fun formatDisplayDate(dateStr: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formatter = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        val date = parser.parse(dateStr)
        if (date != null) formatter.format(date) else dateStr
    } catch (_: Exception) {
        dateStr
    }
}

fun getPrayerIcon(prayerName: String): ImageVector {
    return when (prayerName) {
        "Fajr" -> Icons.Default.WbTwilight
        "Dhuhr" -> Icons.Default.WbSunny
        "Asr" -> Icons.Default.WbSunny
        "Maghrib" -> Icons.Default.WbTwilight
        "Isha" -> Icons.Default.NightsStay
        else -> Icons.Default.Schedule
    }
}
