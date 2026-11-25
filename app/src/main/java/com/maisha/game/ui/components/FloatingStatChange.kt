// app/src/main/java/com/maisha/game/ui/components/FloatingStatChange.kt (new)
package com.maisha.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.SlateNavyElevated
import com.maisha.game.ui.theme.SuccessGreen
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

private const val LARGE_DELTA_THRESHOLD = 25_000
private const val STAGGER_DELAY_MS = 100

data class StatDeltaEvent(
    val type: StatType,
    val delta: Int,
    val id: Long = System.nanoTime(),
    /** When set, shown instead of a raw signed integer (e.g. formatted net-worth swings). */
    val displayText: String? = null
)

@Composable
fun FloatingStatChange(
    event: StatDeltaEvent,
    label: String,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    startDelayMs: Int = 0
) {
    val offsetY = remember(event.id) { Animatable(0f) }
    val alpha = remember(event.id) { Animatable(0f) }
    val scale = remember(event.id) { Animatable(0.85f) }

    LaunchedEffect(event.id, startDelayMs) {
        alpha.snapTo(0f)
        offsetY.snapTo(0f)
        scale.snapTo(0.85f)
        if (startDelayMs > 0) {
            delay(startDelayMs.toLong())
        }
        alpha.snapTo(1f)
        scale.animateTo(1f, animationSpec = tween(160, easing = EaseOutBack))
        val duration = if (kotlin.math.abs(event.delta) >= LARGE_DELTA_THRESHOLD) 1_150 else 800
        offsetY.animateTo(-38f, animationSpec = tween(duration, easing = EaseOutCubic))
        alpha.animateTo(0f, animationSpec = tween(220))
        onFinished()
    }

    val sign = if (event.delta > 0) "+" else ""
    val color = if (event.delta >= 0) SuccessGreen else CoralNegative
    val deltaText = event.displayText ?: "$sign${event.delta}"

    Box(
        modifier = modifier
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .alpha(alpha.value)
            .scale(scale.value)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SlateNavyElevated.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, color.copy(alpha = 0.45f)),
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = event.type.icon(),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = deltaText,
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (label.isNotEmpty()) {
                    Text(
                        text = label,
                        color = PaperWhite.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingStatChangeLayer(
    events: List<StatDeltaEvent>,
    onEventFinished: (Long) -> Unit,
    statLabel: @Composable (StatType) -> String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        events.forEachIndexed { index, event ->
            FloatingStatChange(
                event = event,
                label = statLabel(event.type),
                onFinished = { onEventFinished(event.id) },
                startDelayMs = index * STAGGER_DELAY_MS,
                modifier = Modifier.offset(y = (index * 26).dp)
            )
        }
    }
}
