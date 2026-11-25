// app/src/main/java/com/maisha/game/ui/components/StatBar.kt (modified — AppIcons for stat glyphs)
package com.maisha.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset
import com.maisha.game.ui.theme.AmberWarning
import com.maisha.game.ui.theme.CrimsonDanger
import com.maisha.game.ui.theme.CrimsonDangerSoft
import com.maisha.game.ui.theme.EmeraldCash
import com.maisha.game.ui.theme.EmeraldCashSoft
import com.maisha.game.ui.theme.HairlineBorder
import com.maisha.game.ui.theme.SlateNavyCard
import com.maisha.game.ui.theme.SlateNavyElevated
import com.maisha.game.ui.theme.SlateNavyMuted
import com.maisha.game.ui.theme.SteelSecondary
import com.maisha.game.ui.theme.TabularFigures
import kotlin.math.roundToInt
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maisha.game.R
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.Hairline
import com.maisha.game.ui.theme.HairlineSoft
import com.maisha.game.ui.theme.StatCondition

import com.maisha.game.ui.theme.StatHappiness
import com.maisha.game.ui.theme.StatHealth
import com.maisha.game.ui.theme.StatKarma
import com.maisha.game.ui.theme.StatLooks
import com.maisha.game.ui.theme.StatMoney
import com.maisha.game.ui.theme.StatPerformance
import com.maisha.game.ui.theme.StatRelationship
import com.maisha.game.ui.theme.StatSmarts
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.SuccessGreen
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.unit.sp
import com.maisha.game.util.formatMoney
import kotlinx.coroutines.delay

enum class StatType {
    HEALTH,
    HAPPINESS,
    SMARTS,
    LOOKS,
    MONEY,
    NET_WORTH,
    RELATIONSHIP,
    CONDITION,
    PERFORMANCE,
    FOLLOWERS,
    SKILL,
    KARMA,
    ENERGY,
    STRESS
}

fun StatType.color(): Color = when (this) {
    StatType.HEALTH -> StatHealth
    StatType.HAPPINESS -> StatHappiness
    StatType.SMARTS -> StatSmarts
    StatType.LOOKS -> StatLooks
    StatType.MONEY -> StatMoney
    StatType.NET_WORTH -> StatMoney
    StatType.RELATIONSHIP -> StatRelationship
    StatType.CONDITION -> StatCondition
    StatType.PERFORMANCE -> StatPerformance
    StatType.FOLLOWERS -> StatLooks
    StatType.SKILL -> StatSmarts
    StatType.KARMA -> StatKarma
    StatType.ENERGY -> EmeraldCash
    StatType.STRESS -> AmberWarning
}

fun StatType.icon(): ImageVector = AppIcons.forStat(this)

@Composable
private fun StatType.defaultLabel(): String = when (this) {
    StatType.HEALTH -> stringResource(R.string.stat_health)
    StatType.HAPPINESS -> stringResource(R.string.stat_happiness)
    StatType.SMARTS -> stringResource(R.string.stat_smarts)
    StatType.LOOKS -> stringResource(R.string.stat_looks)
    StatType.MONEY -> stringResource(R.string.stat_money)
    StatType.NET_WORTH -> stringResource(R.string.label_net_worth)
    StatType.RELATIONSHIP -> stringResource(R.string.stat_relationship)
    StatType.CONDITION -> stringResource(R.string.stat_condition)
    StatType.PERFORMANCE -> stringResource(R.string.stat_performance)
    StatType.FOLLOWERS -> stringResource(R.string.stat_followers)
    StatType.SKILL -> stringResource(R.string.stat_skill)
    StatType.KARMA -> stringResource(R.string.stat_karma)
    StatType.ENERGY -> stringResource(R.string.stat_energy)
    StatType.STRESS -> stringResource(R.string.stat_stress)
}

@Composable
fun StatBar(
    type: StatType,
    value: Int,
    modifier: Modifier = Modifier,
    label: String? = null,
    maxValue: Int = 100,
    showIcon: Boolean = true,
    showBar: Boolean = true,
    displayValue: String? = null,
    barColorOverride: Color? = null
) {
    val progress = (value.toFloat() / maxValue).coerceIn(0f, 1f)
    val statAnimationSpec = tween<Float>(durationMillis = 600, easing = EaseOutCubic)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = statAnimationSpec,
        label = "statProgress"
    )

    val normalBarColor = barColorOverride ?: type.color()
    val criticalBarColor = if (barColorOverride == null && type in CRITICAL_STAT_TYPES && value < CRITICAL_THRESHOLD) {
        CoralNegative
    } else {
        normalBarColor
    }
    val barColor by animateColorAsState(
        targetValue = criticalBarColor,
        animationSpec = tween(durationMillis = 600, easing = EaseOutCubic),
        label = "statBarColor"
    )
    val resolvedLabel = label ?: type.defaultLabel()
    val resolvedValue = displayValue ?: value.toString()
    val stateDescriptionText = if (showBar && type !in VALUE_STAT_TYPES) {
        stringResource(R.string.a11y_stat_percent, resolvedLabel, value.coerceIn(0, maxValue))
    } else {
        stringResource(R.string.a11y_stat_value, resolvedLabel, resolvedValue)
    }

    var previousValue by remember { mutableIntStateOf(value) }
    var flashAlpha by remember { mutableStateOf(0f) }
    var flashTint by remember { mutableStateOf<Color?>(null) }

    LaunchedEffect(value) {
        if (value != previousValue) {
            flashTint = if (value > previousValue) SuccessGreen else CoralNegative
            flashAlpha = 0.55f
            previousValue = value
            delay(350)
            flashAlpha = 0f
            delay(100)
            flashTint = null
        }
    }

    val animatedFlashAlpha by animateFloatAsState(
        targetValue = flashAlpha,
        animationSpec = tween(durationMillis = 300, easing = EaseOutCubic),
        label = "statFlash"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                stateDescription = stateDescriptionText
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (showIcon) {
                    MaishaStatIcon(
                        type = type,
                        size = 18.dp
                    )
                }
                Text(
                    text = resolvedLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Text(
                text = resolvedValue,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (type == StatType.MONEY) StatMoney else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
        if (showBar) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(HairlineSoft)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(barColor)
                )
                flashTint?.let { tint ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(tint.copy(alpha = animatedFlashAlpha))
                    )
                }
            }
        }
    }
}

private val VALUE_STAT_TYPES = setOf(StatType.MONEY, StatType.NET_WORTH)

private val CRITICAL_STAT_TYPES = setOf(
    StatType.HEALTH,
    StatType.HAPPINESS,
    StatType.SMARTS,
    StatType.LOOKS,
    StatType.CONDITION,
    StatType.PERFORMANCE,
    StatType.RELATIONSHIP,
    StatType.KARMA
)

private const val CRITICAL_THRESHOLD = 30

@Composable
fun MoneyStatRow(
    amount: Int,
    modifier: Modifier = Modifier,
    label: String? = null,
    countryCode: String = "XX"
) {
    StatBar(
        type = StatType.MONEY,
        value = 0,
        label = label ?: stringResource(R.string.stat_money),
        displayValue = formatMoney(amount, countryCode),
        showBar = false,
        modifier = modifier
    )
}

/**
 * Dynamic color shift based on threshold:
 * - Emerald (#10B981) above 60%
 * - Amber (#F59E0B) between 30–60%
 * - Crimson (#EF4444) below 30%
 * (For inverted stats like stress, the scale is reversed).
 */
fun statThresholdColor(value: Int, inverted: Boolean = false): Color {
    val clamped = value.coerceIn(0, 100)
    return if (!inverted) {
        when {
            clamped > 60 -> EmeraldCash
            clamped >= 30 -> AmberWarning
            else -> CrimsonDanger
        }
    } else {
        when {
            clamped > 60 -> CrimsonDanger
            clamped >= 30 -> AmberWarning
            else -> EmeraldCash
        }
    }
}

/**
 * Compact elevated docked status panel positioned just above the bottom navigation dock.
 * Inspired by BitLife's scannable rhythm with a sleek dark-mode finish (#131822).
 * Displays Happiness, Health, Smarts, Looks (and Energy/Stress if active) with dynamic threshold colors,
 * right-aligned tabular percentages, animated progress tracks, and crisp floating delta pills.
 */
@Composable
fun DockedStatStrip(
    happiness: Int,
    health: Int,
    smarts: Int,
    looks: Int,
    energy: Int? = null,
    stress: Int? = null,
    pendingDeltas: List<StatDeltaEvent> = emptyList(),
    onDeltaFinished: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SlateNavyCard,
        border = BorderStroke(1.dp, HairlineBorder),
        tonalElevation = 4.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Floating cash delta pill anchored in the center above the panel
            val cashDelta = remember(pendingDeltas) {
                pendingDeltas.find { it.type == StatType.MONEY || it.type == StatType.NET_WORTH }
            }
            if (cashDelta != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    FloatingStatDeltaPill(
                        event = cashDelta,
                        onFinished = { onDeltaFinished(cashDelta.id) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Happiness
                val happyDelta = remember(pendingDeltas) {
                    pendingDeltas.find { it.type == StatType.HAPPINESS }
                }
                DockedStatBarItem(
                    label = "HAPPY",
                    value = happiness,
                    icon = AppIcons.Happiness,
                    activeDelta = happyDelta,
                    onDeltaFinished = onDeltaFinished,
                    modifier = Modifier.weight(1f)
                )

                // 2. Health
                val healthDelta = remember(pendingDeltas) {
                    pendingDeltas.find { it.type == StatType.HEALTH }
                }
                DockedStatBarItem(
                    label = "HEALTH",
                    value = health,
                    icon = AppIcons.Health,
                    activeDelta = healthDelta,
                    onDeltaFinished = onDeltaFinished,
                    modifier = Modifier.weight(1f)
                )

                // 3. Smarts
                val smartsDelta = remember(pendingDeltas) {
                    pendingDeltas.find { it.type == StatType.SMARTS }
                }
                DockedStatBarItem(
                    label = "SMARTS",
                    value = smarts,
                    icon = AppIcons.Smarts,
                    activeDelta = smartsDelta,
                    onDeltaFinished = onDeltaFinished,
                    modifier = Modifier.weight(1f)
                )

                // 4. Looks
                val looksDelta = remember(pendingDeltas) {
                    pendingDeltas.find { it.type == StatType.LOOKS }
                }
                DockedStatBarItem(
                    label = "LOOKS",
                    value = looks,
                    icon = AppIcons.Looks,
                    activeDelta = looksDelta,
                    onDeltaFinished = onDeltaFinished,
                    modifier = Modifier.weight(1f)
                )

                // 5. Energy or Stress (if active)
                if (energy != null) {
                    val energyDelta = remember(pendingDeltas) {
                        pendingDeltas.find { it.type == StatType.ENERGY }
                    }
                    DockedStatBarItem(
                        label = "ENERGY",
                        value = energy,
                        icon = AppIcons.NavActions,
                        activeDelta = energyDelta,
                        onDeltaFinished = onDeltaFinished,
                        modifier = Modifier.weight(1f)
                    )
                } else if (stress != null) {
                    val stressDelta = remember(pendingDeltas) {
                        pendingDeltas.find { it.type == StatType.STRESS }
                    }
                    DockedStatBarItem(
                        label = "STRESS",
                        value = stress,
                        icon = AppIcons.UnderworldHeat,
                        isInverted = true,
                        activeDelta = stressDelta,
                        onDeltaFinished = onDeltaFinished,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DockedStatBarItem(
    label: String,
    value: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isInverted: Boolean = false,
    activeDelta: StatDeltaEvent? = null,
    onDeltaFinished: (Long) -> Unit = {}
) {
    val clampedValue = value.coerceIn(0, 100)
    val targetProgress = (clampedValue.toFloat() / 100f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 500, easing = EaseOutCubic),
        label = "statTrackProgress"
    )

    val targetColor = statThresholdColor(clampedValue, inverted = isInverted)
    val barColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 400),
        label = "statTrackColor"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        // Floating delta pill anchored above the bar
        if (activeDelta != null) {
            FloatingStatDeltaPill(
                event = activeDelta,
                onFinished = { onDeltaFinished(activeDelta.id) }
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = barColor,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SteelSecondary,
                        maxLines = 1,
                        letterSpacing = 0.3.sp
                    )
                }

                Text(
                    text = "$clampedValue%",
                    style = TabularFigures,
                    fontSize = 10.sp,
                    color = barColor
                )
            }

            // Smooth animated progress track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(SlateNavyMuted)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(barColor)
                )
            }
        }
    }
}

@Composable
fun FloatingStatDeltaPill(
    event: StatDeltaEvent,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val offsetY = remember(event.id) { Animatable(0f) }
    val alpha = remember(event.id) { Animatable(0f) }
    val scale = remember(event.id) { Animatable(0.82f) }

    LaunchedEffect(event.id) {
        alpha.snapTo(0f)
        offsetY.snapTo(0f)
        scale.snapTo(0.82f)

        alpha.snapTo(1f)
        scale.animateTo(1f, animationSpec = tween(140, easing = EaseOutBack))
        offsetY.animateTo(-30f, animationSpec = tween(750, easing = EaseOutCubic))
        alpha.animateTo(0f, animationSpec = tween(200))
        onFinished()
    }

    val isPositive = event.delta >= 0
    val deltaColor = if (isPositive) EmeraldCash else CrimsonDanger
    val sign = if (isPositive) "+" else ""
    val text = event.displayText ?: "$sign${event.delta}"

    Box(
        modifier = modifier
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .alpha(alpha.value)
            .scale(scale.value)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SlateNavyElevated.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, deltaColor.copy(alpha = 0.6f)),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = text,
                    style = TabularFigures,
                    fontSize = 10.sp,
                    color = deltaColor
                )
            }
        }
    }
}
