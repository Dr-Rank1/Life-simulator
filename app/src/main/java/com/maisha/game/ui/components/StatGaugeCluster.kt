package com.maisha.game.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.data.model.Stats
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.StatHappiness
import com.maisha.game.ui.theme.StatHealth
import com.maisha.game.ui.theme.StatLooks
import com.maisha.game.ui.theme.StatSmarts

/**
 * Compact 4-stat ring gauge cluster for the primary Life HUD.
 * Replaces dense full-screen stat bars with arcade-grade circular gauges.
 */
@Composable
fun StatGaugeCluster(
    stats: Stats,
    modifier: Modifier = Modifier,
    onGaugeClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onGaugeClick),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF8FAFC)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatRingGaugeItem(
                label = "HEALTH",
                value = stats.health,
                accentColor = StatHealth,
                modifier = Modifier.weight(1f)
            )
            StatRingGaugeItem(
                label = "HAPPINESS",
                value = stats.happiness,
                accentColor = StatHappiness,
                modifier = Modifier.weight(1f)
            )
            StatRingGaugeItem(
                label = "SMARTS",
                value = stats.smarts,
                accentColor = StatSmarts,
                modifier = Modifier.weight(1f)
            )
            StatRingGaugeItem(
                label = "LOOKS",
                value = stats.looks,
                accentColor = StatLooks,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatRingGaugeItem(
    label: String,
    value: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val clamped = value.coerceIn(0, 100)
    val animatedProgress by animateFloatAsState(
        targetValue = clamped / 100f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "gaugeProgress"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(34.dp)) {
                val strokeWidth = 3.5.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                val arcSize = Size(diameter, diameter)

                val startAngle = 140f
                val totalSweep = 260f

                // Track
                drawArc(
                    color = accentColor.copy(alpha = 0.16f),
                    startAngle = startAngle,
                    sweepAngle = totalSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Progress fill
                val sweep = totalSweep * animatedProgress
                if (sweep > 0f) {
                    drawArc(
                        color = accentColor,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            Text(
                text = "$clamped%",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold,
            color = InkTertiary,
            letterSpacing = 0.5.sp,
            maxLines = 1
        )
    }
}
