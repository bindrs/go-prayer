package com.example.ui.qibla

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.qibla.QiblaCalculator

private val AccentGold = Color(0xFFF59E0B)
private val DeepNavyBg = Color(0xFF0F172A)
private val EmeraldBorder = Color(0xFF10B981)

/**
 * High-polish Quick Access Card for Qibla Compass on the main Dashboard.
 */
@Composable
fun QiblaCompassCard(
    userLatitude: Double,
    userLongitude: Double,
    cityName: String,
    onOpenCompass: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qiblaBearing = remember(userLatitude, userLongitude) {
        QiblaCalculator.calculateQiblaBearing(userLatitude, userLongitude)
    }
    val distanceKm = remember(userLatitude, userLongitude) {
        QiblaCalculator.calculateDistanceToKaabaKm(userLatitude, userLongitude)
    }
    val cardinalDir = remember(qiblaBearing) {
        QiblaCalculator.getCardinalDirection(qiblaBearing)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenCompass)
            .testTag("dashboard_qibla_compass_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepNavyBg.copy(alpha = 0.85f)),
        border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Miniature Compass Icon with rotating Qibla pointer
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AccentGold.copy(alpha = 0.25f),
                                Color(0xFF1E293B)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Directional Kaaba arrow rotated towards Qibla
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(qiblaBearing)
                )
                Text(
                    text = "🕋",
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Qibla Direction",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Text(
                    text = "${String.format("%.1f", qiblaBearing)}° $cardinalDir • ${String.format("%,.0f", distanceKm)} km to Kaaba",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )
                )

                Text(
                    text = "Tap to find Kaaba with real-time sensor",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = AccentGold,
                        fontSize = 10.sp
                    )
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open Qibla",
                tint = AccentGold,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
