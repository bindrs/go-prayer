package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldSoft
import com.example.ui.theme.EmeraldPrimary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IslamicStarDeco(
    modifier: Modifier = Modifier,
    color: Color = AccentGold,
    sizeDp: Int = 32
) {
    Canvas(modifier = modifier.size(sizeDp.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width / 2f
        val innerRadius = radius * 0.5f

        // Draw 8-pointed Islamic Star (Rub el Hizb)
        val path = Path()
        val numPoints = 8
        for (i in 0 until numPoints * 2) {
            val angle = (i * PI / numPoints) - (PI / 2)
            val r = if (i % 2 == 0) radius else innerRadius
            val x = center.x + (r * cos(angle)).toFloat()
            val y = center.y + (r * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        drawPath(path = path, color = color.copy(alpha = 0.25f))
        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun BismillahBanner(
    modifier: Modifier = Modifier,
    textColor: Color = AccentGold
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "بِسْمِ ٱللَّٰهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        )
    }
}

@Composable
fun GlowingPrayerRing(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    ringColor: Color = AccentGold
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier.size((180 * if (isActive) pulseScale else 1f).dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width / 2.2f

        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ringColor.copy(alpha = 0.35f), Color.Transparent),
                center = center,
                radius = radius * 1.25f
            )
        )

        // Main geometric circle
        drawCircle(
            color = ringColor,
            radius = radius,
            style = Stroke(width = 3.dp.toPx())
        )

        // Inner dashed ring
        drawCircle(
            color = ringColor.copy(alpha = 0.6f),
            radius = radius * 0.85f,
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

@Composable
fun StatusBadge(
    text: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    customContainerColor: Color? = null,
    customTextColor: Color? = null
) {
    val containerColor = customContainerColor ?: if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = customTextColor ?: if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
