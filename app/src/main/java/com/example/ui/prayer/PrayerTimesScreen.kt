package com.example.ui.prayer

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GlassAppBackground
import com.example.ui.theme.*
import com.example.ui.viewmodel.PrayerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DateItem(
    val dayOfWeek: String, // "Fri"
    val dayNumber: Int,    // 24
    val isToday: Boolean,
    val dateCalendar: Calendar
)

@Composable
fun PrayerTimesScreen(
    viewModel: PrayerViewModel,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToPrayerDetails: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val schedule by viewModel.schedule.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()
    val countdownSeconds by viewModel.liveCountdownSeconds.collectAsStateWithLifecycle()
    val todaySalahRecords by viewModel.todaySalahRecords.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val hours = countdownSeconds / 3600
    val minutes = (countdownSeconds % 3600) / 60
    val timeUntilNextText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

    val prayersList = schedule?.prayers ?: emptyList()
    val nextPrayerName = schedule?.nextPrayer?.name ?: ""

    // Date Strip State
    var selectedDayOffset by remember { mutableIntStateOf(0) }
    var setReminderMessage by remember { mutableStateOf<String?>(null) }

    val daysList = remember {
        val list = mutableListOf<DateItem>()
        val cal = Calendar.getInstance()
        for (i in -2..4) {
            val date = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, i) }
            val dayName = SimpleDateFormat("EEE", Locale.ENGLISH).format(date.time)
            val dayNum = date.get(Calendar.DAY_OF_MONTH)
            list.add(
                DateItem(
                    dayOfWeek = dayName,
                    dayNumber = dayNum,
                    isToday = (i == 0),
                    dateCalendar = date
                )
            )
        }
        list
    }

    val selectedDateItem = daysList.getOrNull(selectedDayOffset + 2) ?: daysList[2]
    val formattedSelectedDateTitle = remember(selectedDateItem) {
        SimpleDateFormat("EEEE, d MMMM", Locale.ENGLISH).format(selectedDateItem.dateCalendar.time)
    }
    val currentMonthYear = remember(selectedDateItem) {
        SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(selectedDateItem.dateCalendar.time)
    }

    Scaffold(
        containerColor = SoftOffWhiteBg,
        modifier = modifier.testTag("prayer_times_screen")
    ) { innerPadding ->
        GlassAppBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Top Header: Dark Teal Card with Calendar & Reminder Action (Reference Design)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prayer_times_calendar_header_card"),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkTealPrimary),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            // Month Header with Prev/Next controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (selectedDayOffset > -2) selectedDayOffset--
                                    },
                                    modifier = Modifier.testTag("prayer_times_prev_day_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "Previous Day",
                                        tint = Color.White
                                    )
                                }

                                Text(
                                    text = currentMonthYear,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 18.sp
                                    )
                                )

                                IconButton(
                                    onClick = {
                                        if (selectedDayOffset < 2) selectedDayOffset++
                                    },
                                    modifier = Modifier.testTag("prayer_times_next_day_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Next Day",
                                        tint = Color.White
                                    )
                                }
                            }

                            // Day selector row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                daysList.forEachIndexed { index, item ->
                                    val isSelected = (index == selectedDayOffset + 2)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.clickable {
                                            selectedDayOffset = index - 2
                                        }
                                    ) {
                                        Text(
                                            text = item.dayOfWeek,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) WarmOrangeAccent else Color.White.copy(alpha = 0.7f),
                                                fontWeight = FontWeight.Medium
                                            )
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) WarmOrangeAccent else Color.Transparent
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${item.dayNumber}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = if (isSelected) DarkTealPrimary else Color.White,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // Set Reminder Orange CTA Button
                            Button(
                                onClick = {
                                    setReminderMessage = "All prayer Adhan notifications scheduled for $formattedSelectedDateTitle!"
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WarmOrangeAccent,
                                    contentColor = DarkTealPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("set_reminder_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Set Reminder",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                )
                            }

                            if (setReminderMessage != null) {
                                Text(
                                    text = setReminderMessage!!,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = WarmOrangeAccent,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // Date & Location Title Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = formattedSelectedDateTitle,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkTeal,
                                    fontSize = 20.sp
                                )
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = WarmOrangeAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${location.city}, ${location.country}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondaryGray,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftPeachContainer,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = "6 Salahs Today",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DarkTealPrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Vertical Timetable Timeline
                itemsIndexed(prayersList) { index, prayer ->
                    val isUpcoming = prayer.name.equals(nextPrayerName, ignoreCase = true)
                    val record = todaySalahRecords.find { it.prayerName.equals(prayer.name, ignoreCase = true) }
                    val isCompleted = record?.isOffered == true

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onNavigateToPrayerDetails?.invoke(prayer.name)
                            }
                            .testTag("prayer_item_${prayer.name.lowercase()}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Time Stamp Column
                        Column(
                            modifier = Modifier.width(60.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = prayer.timeFormatted,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isUpcoming) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isUpcoming) WarmOrangeAccent else TextSecondaryGray,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Vertical Timeline Axis with Orange indicator line & dot
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.height(72.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .weight(1f)
                                    .background(
                                        if (isUpcoming) WarmOrangeAccent else Color(0xFFCBD5E1)
                                    )
                            )

                            Box(
                                modifier = Modifier
                                    .size(if (isUpcoming) 14.dp else 10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isUpcoming) WarmOrangeAccent else if (isCompleted) DarkTealPrimary else Color(0xFF94A3B8)
                                    )
                            )

                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .weight(1f)
                                    .background(
                                        if (isUpcoming) WarmOrangeAccent else Color(0xFFCBD5E1)
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Main Prayer Card Container
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUpcoming) SoftPeachContainer else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isUpcoming) 4.dp else 2.dp),
                            border = BorderStroke(
                                width = if (isUpcoming) 1.5.dp else 0.5.dp,
                                color = if (isUpcoming) WarmOrangeAccent else Color(0xFFE2E8F0)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = prayer.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextDarkTeal,
                                                fontSize = 16.sp
                                            )
                                        )
                                        Text(
                                            text = prayer.arabicName,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = WarmOrangeAccent,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }

                                    Text(
                                        text = if (isUpcoming) "Upcoming • In $timeUntilNextText" else if (isCompleted) "Completed ✓" else "Adhan at ${prayer.timeFormatted}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isUpcoming) WarmOrangeAccent else TextSecondaryGray,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Completed",
                                            tint = DarkTealPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.togglePrayerAdhan(prayer.name, !prayer.adhanEnabled)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (prayer.adhanEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                            contentDescription = "Toggle Notification",
                                            tint = if (prayer.adhanEnabled) WarmOrangeAccent else Color(0xFF94A3B8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}
