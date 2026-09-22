package com.example.ui.qibla

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.qibla.CompassSensorState
import com.example.domain.qibla.QiblaCalculator
import com.example.domain.qibla.QiblaSensorManager
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val IslamicGreen = Color(0xFF10B981)
private val IslamicGold = Color(0xFFF59E0B)
private val DeepNavyBg = Color(0xFF0F172A)
private val AccentEmerald = Color(0xFF059669)

/**
 * Beautiful, interactive Qibla Compass component powered by Android hardware Sensor APIs.
 */
@Composable
fun QiblaCompassView(
    userLatitude: Double,
    userLongitude: Double,
    cityName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sensorManager = remember { QiblaSensorManager(context) }

    var sensorState by remember { mutableStateOf(CompassSensorState()) }
    var manualHeading by remember { mutableFloatStateOf(0f) }
    var useManualHeading by remember { mutableStateOf(false) }

    // Calculate Qibla bearing and distance to Kaaba
    val qiblaBearing = remember(userLatitude, userLongitude) {
        QiblaCalculator.calculateQiblaBearing(userLatitude, userLongitude)
    }
    val distanceKm = remember(userLatitude, userLongitude) {
        QiblaCalculator.calculateDistanceToKaabaKm(userLatitude, userLongitude)
    }

    // Subscribe to device hardware sensors
    LaunchedEffect(userLatitude, userLongitude) {
        sensorManager.getCompassOrientationFlow(userLatitude, userLongitude).collect { state ->
            sensorState = state
            if (!state.isSensorAvailable) {
                useManualHeading = true
            }
        }
    }

    val effectiveHeading = if (useManualHeading) manualHeading else sensorState.trueHeading
    val headingOffset = QiblaCalculator.getHeadingOffset(effectiveHeading, qiblaBearing)
    val isAligned = QiblaCalculator.isFacingKaaba(effectiveHeading, qiblaBearing, toleranceDegrees = 4.0f)

    // Trigger subtle haptic feedback upon reaching alignment
    var wasAligned by remember { mutableStateOf(false) }
    LaunchedEffect(isAligned) {
        if (isAligned && !wasAligned) {
            triggerCompassHaptic(context)
        }
        wasAligned = isAligned
    }

    // Smooth compass dial rotation animation with continuous angle calculation
    var lastTargetHeading by remember { mutableFloatStateOf(0f) }
    var continuousHeading by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(effectiveHeading) {
        var delta = (effectiveHeading - lastTargetHeading) % 360f
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        continuousHeading += delta
        lastTargetHeading = effectiveHeading
    }

    val animatedHeading by animateFloatAsState(
        targetValue = continuousHeading,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "compassHeadingAnimation"
    )

    val dialBorderColor by animateColorAsState(
        targetValue = if (isAligned) IslamicGreen else IslamicGold.copy(alpha = 0.6f),
        label = "dialBorderColor"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("qibla_compass_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Alignment Status Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isAligned) Color(0xFF064E3B) else DeepNavyBg,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.5.dp,
                color = if (isAligned) IslamicGreen else Color(0xFF334155)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("qibla_status_banner")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isAligned) IslamicGreen else IslamicGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAligned) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Aligned",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Compass",
                            tint = IslamicGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isAligned) "✓ Facing the Kaaba" else "Find Qibla Direction",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isAligned) Color(0xFFA7F3D0) else Color.White
                        )
                    )
                    Text(
                        text = when {
                            isAligned -> "You are facing Makkah."
                            headingOffset > 0 -> "Turn Right ${String.format("%.0f", headingOffset)}°"
                            else -> "Turn Left ${String.format("%.0f", -headingOffset)}°"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isAligned) Color.White else Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )
                    )
                }

                // Kaaba angle readout badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IslamicGold.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${String.format("%.1f", qiblaBearing)}°",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = IslamicGold
                            )
                        )
                        Text(
                            text = "Qibla",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        )
                    }
                }
            }
        }

        // Circular Qibla Compass Dial
        Box(
            modifier = Modifier
                .size(280.dp)
                .testTag("compass_dial_canvas_container"),
            contentAlignment = Alignment.Center
        ) {
            // Background ambient glow halo when aligned
            if (isAligned) {
                Box(
                    modifier = Modifier
                        .size(270.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    IslamicGreen.copy(alpha = 0.35f),
                                    IslamicGreen.copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Fixed Compass Dial Canvas that rotates by -animatedHeading
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("compass_canvas")
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f - 12.dp.toPx()

                // Outer decorative border
                drawCircle(
                    color = DeepNavyBg,
                    radius = radius + 6.dp.toPx()
                )
                drawCircle(
                    color = dialBorderColor,
                    radius = radius,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Inner bezel ring
                drawCircle(
                    color = Color(0xFF1E293B),
                    radius = radius - 8.dp.toPx(),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Dial elements rotating according to device heading
                rotate(degrees = -animatedHeading, pivot = center) {
                    // Draw 360 degree ticks
                    for (degree in 0 until 360 step 5) {
                        val rad = Math.toRadians(degree.toDouble())
                        val isMajor = degree % 30 == 0
                        val isCardinal = degree % 90 == 0
                        val tickLength = when {
                            isCardinal -> 14.dp.toPx()
                            isMajor -> 9.dp.toPx()
                            else -> 5.dp.toPx()
                        }
                        val strokeW = when {
                            isCardinal -> 2.5.dp.toPx()
                            isMajor -> 1.5.dp.toPx()
                            else -> 1.dp.toPx()
                        }
                        val tickColor = when {
                            degree == 0 -> Color(0xFFEF4444) // North in Red
                            isCardinal -> IslamicGold
                            isMajor -> Color(0xFF94A3B8)
                            else -> Color(0xFF475569)
                        }

                        val outerX = center.x + radius * sin(rad).toFloat()
                        val outerY = center.y - radius * cos(rad).toFloat()
                        val innerX = center.x + (radius - tickLength) * sin(rad).toFloat()
                        val innerY = center.y - (radius - tickLength) * cos(rad).toFloat()

                        drawLine(
                            color = tickColor,
                            start = Offset(innerX, innerY),
                            end = Offset(outerX, outerY),
                            strokeWidth = strokeW,
                            cap = StrokeCap.Round
                        )
                    }

                    // Draw Cardinal Letters (N, E, S, W)
                    val textPaint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        textSize = 14.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }

                    drawCardinalLabel("N", 0f, radius - 24.dp.toPx(), center, textPaint, Color(0xFFEF4444))
                    drawCardinalLabel("E", 90f, radius - 24.dp.toPx(), center, textPaint, IslamicGold)
                    drawCardinalLabel("S", 180f, radius - 24.dp.toPx(), center, textPaint, Color(0xFF94A3B8))
                    drawCardinalLabel("W", 270f, radius - 24.dp.toPx(), center, textPaint, Color(0xFF94A3B8))

                    // Draw the Kaaba Target Marker on the rotating dial at qiblaBearing
                    rotate(degrees = qiblaBearing, pivot = center) {
                        val kaabaMarkerRadius = radius - 18.dp.toPx()
                        val kaabaCenter = Offset(center.x, center.y - kaabaMarkerRadius)

                        // Kaaba ray from center
                        drawLine(
                            color = if (isAligned) IslamicGreen else IslamicGold,
                            start = center,
                            end = kaabaCenter,
                            strokeWidth = if (isAligned) 3.dp.toPx() else 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Miniature Kaaba motif (Gold & Black cube)
                        drawKaabaMotif(kaabaCenter, isAligned)
                    }
                }

                // Fixed Center Target / Alignment Pointer (pointing straight forward at 12 o'clock)
                drawFixedForwardNeedle(center, radius, isAligned)
            }

            // Center display badge (Current Heading Degrees)
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = DeepNavyBg,
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    color = if (isAligned) IslamicGreen else IslamicGold
                ),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${String.format("%.0f", effectiveHeading)}°",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                    )
                    Text(
                        text = QiblaCalculator.getCardinalDirection(effectiveHeading),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isAligned) IslamicGreen else IslamicGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Live Telemetry Cards: Location, Distance, and Heading details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Current Location Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DeepNavyBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = IslamicGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Location",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                    Text(
                        text = cityName.ifEmpty { "Current City" },
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "${String.format("%.2f", userLatitude)}°, ${String.format("%.2f", userLongitude)}°",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    )
                }
            }

            // Distance to Kaaba Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DeepNavyBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🕋", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "To Makkah",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                    Text(
                        text = "${String.format("%,.0f", distanceKm)} km",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = QiblaCalculator.getCardinalDirection(qiblaBearing),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = IslamicGold
                        )
                    )
                }
            }
        }

        // Sensor Calibration & Status Information
        if (sensorState.isSensorAvailable) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (!sensorState.isDeviceFlat) Color(0xFF451A03) else Color(0xFF1E293B)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (!sensorState.isDeviceFlat) Color(0xFFD97706) else Color(0xFF334155)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (!sensorState.isDeviceFlat) Icons.Default.ScreenRotation else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (!sensorState.isDeviceFlat) Color(0xFFFBBF24) else IslamicGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (!sensorState.isDeviceFlat) {
                                "Hold Phone Flat for Highest Accuracy"
                            } else {
                                "Sensor Active: ${sensorState.sensorName.take(28)}"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (!sensorState.isDeviceFlat) Color(0xFFFDE68A) else Color(0xFFE2E8F0)
                            )
                        )
                        Text(
                            text = if (!sensorState.isDeviceFlat) {
                                "Hold phone flat horizontally for optimal magnetic sensor precision."
                            } else {
                                "True North declination: ${String.format("%.1f", sensorState.declination)}° • Wave in figure-8 if calibration needed."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        )
                    }
                }
            }
        } else {
            // Fallback for devices without hardware magnetometer/rotation sensors
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD97706))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Magnetic Sensor Not Available",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDE68A)
                            )
                        )
                    }
                    Text(
                        text = "Calculated Qibla for $cityName is ${String.format("%.1f", qiblaBearing)}° from True North. You can manually simulate phone rotation below to test alignment:",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1), fontSize = 11.sp)
                    )
                    Slider(
                        value = manualHeading,
                        onValueChange = {
                            manualHeading = it
                            useManualHeading = true
                        },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = IslamicGold,
                            activeTrackColor = IslamicGold,
                            inactiveTrackColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("North (0°)", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8)))
                        Text("East (90°)", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8)))
                        Text("South (180°)", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8)))
                        Text("West (270°)", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8)))
                    }
                }
            }
        }
    }
}

/**
 * Triggers a crisp tactile haptic pulse when the user aligns with the Kaaba.
 */
private fun triggerCompassHaptic(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(70)
            }
        }
    } catch (_: Exception) {}
}

private fun DrawScope.drawCardinalLabel(
    text: String,
    angleDegrees: Float,
    distanceFromCenter: Float,
    center: Offset,
    paint: android.graphics.Paint,
    color: Color
) {
    val rad = Math.toRadians(angleDegrees.toDouble())
    val x = center.x + distanceFromCenter * sin(rad).toFloat()
    val y = center.y - distanceFromCenter * cos(rad).toFloat() + (paint.textSize / 3f)

    paint.color = color.hashCode()
    drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
}

private fun DrawScope.drawKaabaMotif(center: Offset, isAligned: Boolean) {
    val cubeSize = 22.dp.toPx()
    val half = cubeSize / 2f

    // Gold aura ring if aligned
    if (isAligned) {
        drawCircle(
            color = Color(0xFF10B981).copy(alpha = 0.5f),
            radius = half + 8.dp.toPx()
        )
    }

    // Black Kaaba Cube Body
    drawRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(center.x - half, center.y - half),
        size = androidx.compose.ui.geometry.Size(cubeSize, cubeSize)
    )

    // Gold Kiswa Band around Kaaba
    drawRect(
        color = Color(0xFFF59E0B),
        topLeft = Offset(center.x - half, center.y - half + (cubeSize * 0.28f)),
        size = androidx.compose.ui.geometry.Size(cubeSize, cubeSize * 0.18f)
    )

    // Border around Kaaba
    drawRect(
        color = if (isAligned) Color(0xFF10B981) else Color(0xFFF59E0B),
        topLeft = Offset(center.x - half, center.y - half),
        size = androidx.compose.ui.geometry.Size(cubeSize, cubeSize),
        style = Stroke(width = 1.5.dp.toPx())
    )
}

private fun DrawScope.drawFixedForwardNeedle(
    center: Offset,
    radius: Float,
    isAligned: Boolean
) {
    val needleColor = if (isAligned) Color(0xFF10B981) else Color(0xFFEF4444)
    val tipY = center.y - radius - 6.dp.toPx()

    // Upward fixed pointer indicating phone's front heading
    val path = Path().apply {
        moveTo(center.x, tipY)
        lineTo(center.x - 7.dp.toPx(), tipY + 14.dp.toPx())
        lineTo(center.x + 7.dp.toPx(), tipY + 14.dp.toPx())
        close()
    }

    drawPath(
        path = path,
        color = needleColor
    )
}
