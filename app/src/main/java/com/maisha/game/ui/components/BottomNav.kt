// app/src/main/java/com/maisha/game/ui/components/BottomNav.kt
package com.maisha.game.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.R
import com.maisha.game.feedback.HapticType
import com.maisha.game.ui.feedback.LocalFeedbackManager
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.HairlineBorder
import com.maisha.game.ui.theme.RoyalIndigo
import com.maisha.game.ui.theme.RoyalIndigoLip
import com.maisha.game.ui.theme.RoyalIndigoPressed
import com.maisha.game.ui.theme.RoyalIndigoSoft
import com.maisha.game.ui.theme.SlateNavyCard
import com.maisha.game.ui.theme.SlateNavyMuted
import com.maisha.game.ui.theme.SteelSecondary
import com.maisha.game.ui.theme.SteelTertiary

enum class MainTab {
    LIFE,
    CAREER,
    ASSETS,
    FAMILY,
    ACTIONS
}

/**
 * Modern AltLife/BitLife 4-tab docked action dock with a prominent center + Age button.
 * Flanking tabs: Occupation (Career), Assets, Relationships (Family), Activities (Actions).
 */
@Composable
fun ActionDockWithCenterAge(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onAgeUp: () -> Unit,
    ageUpEnabled: Boolean,
    isAgingUp: Boolean,
    currentAge: Int,
    disabledTabs: Set<MainTab> = emptySet(),
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SlateNavyCard,
        border = BorderStroke(1.dp, HairlineBorder),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Occupation (CAREER)
            DockActionTab(
                tab = MainTab.CAREER,
                label = stringResource(R.string.nav_occupation),
                icon = AppIcons.Career,
                selected = selectedTab == MainTab.CAREER,
                disabled = MainTab.CAREER in disabledTabs,
                onSelect = {
                    if (selectedTab == MainTab.CAREER) onTabSelected(MainTab.LIFE)
                    else onTabSelected(MainTab.CAREER)
                },
                modifier = Modifier.weight(1f)
            )

            // Tab 2: Assets (ASSETS)
            DockActionTab(
                tab = MainTab.ASSETS,
                label = stringResource(R.string.nav_assets),
                icon = AppIcons.NavAssets,
                selected = selectedTab == MainTab.ASSETS,
                disabled = MainTab.ASSETS in disabledTabs,
                onSelect = {
                    if (selectedTab == MainTab.ASSETS) onTabSelected(MainTab.LIFE)
                    else onTabSelected(MainTab.ASSETS)
                },
                modifier = Modifier.weight(1f)
            )

            // Center tactile + Age trigger
            CenterAgeUpTrigger(
                onClick = onAgeUp,
                enabled = ageUpEnabled,
                isLoading = isAgingUp,
                currentAge = currentAge,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Tab 3: Relationships (FAMILY)
            DockActionTab(
                tab = MainTab.FAMILY,
                label = stringResource(R.string.nav_relationships),
                icon = AppIcons.NavFamily,
                selected = selectedTab == MainTab.FAMILY,
                disabled = MainTab.FAMILY in disabledTabs,
                onSelect = {
                    if (selectedTab == MainTab.FAMILY) onTabSelected(MainTab.LIFE)
                    else onTabSelected(MainTab.FAMILY)
                },
                modifier = Modifier.weight(1f)
            )

            // Tab 4: Activities (ACTIONS)
            DockActionTab(
                tab = MainTab.ACTIONS,
                label = stringResource(R.string.nav_activities),
                icon = AppIcons.NavActions,
                selected = selectedTab == MainTab.ACTIONS,
                disabled = MainTab.ACTIONS in disabledTabs,
                onSelect = {
                    if (selectedTab == MainTab.ACTIONS) onTabSelected(MainTab.LIFE)
                    else onTabSelected(MainTab.ACTIONS)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DockActionTab(
    tab: MainTab,
    label: String,
    icon: ImageVector,
    selected: Boolean,
    disabled: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !disabled,
                onClick = onSelect
            )
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (selected) RoyalIndigoSoft
                    else Color.Transparent
                )
                .border(
                    width = 1.dp,
                    color = if (selected) RoyalIndigo.copy(alpha = 0.35f) else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = when {
                    disabled -> SteelTertiary
                    selected -> RoyalIndigo
                    else -> SteelSecondary
                },
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = when {
                disabled -> SteelTertiary
                selected -> RoyalIndigo
                else -> SteelSecondary
            },
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Tactile 3D physical center + Age button with press scale physics and haptic feedback.
 */
@Composable
fun CenterAgeUpTrigger(
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean,
    currentAge: Int,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val canPress = enabled && !isLoading
    val feedbackManager = LocalFeedbackManager.current
    val view = LocalView.current

    val scale by animateFloatAsState(
        targetValue = if (pressed && canPress) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "centerAgeScale"
    )

    val buttonBrush = when {
        !canPress -> Brush.verticalGradient(listOf(Color(0xFF1E2736), SlateNavyMuted))
        pressed -> Brush.verticalGradient(listOf(RoyalIndigoPressed, RoyalIndigoLip))
        else -> Brush.verticalGradient(listOf(RoyalIndigo, RoyalIndigoPressed))
    }
    val lipColor = if (canPress) RoyalIndigoLip else SlateNavyMuted
    val contentColor = if (canPress) Color.White else SteelTertiary

    Box(
        modifier = modifier
            .widthIn(min = 78.dp, max = 92.dp)
            .height(54.dp)
            .scale(scale)
            .shadow(
                elevation = if (canPress && !pressed) 4.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = RoyalIndigo.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(lipColor)
            .semantics { role = Role.Button }
            .pointerInput(canPress, onClick) {
                if (!canPress) return@pointerInput
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    pressed = true
                    feedbackManager.triggerHaptic(view, HapticType.SUCCESS)
                    try {
                        val up = waitForUpOrCancellation()
                        if (up != null) {
                            onClick()
                        }
                    } finally {
                        pressed = false
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(51.dp)
                .padding(top = if (pressed && canPress) 3.dp else 0.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(buttonBrush)
                .border(
                    width = 1.dp,
                    color = if (canPress) Color.White.copy(alpha = 0.22f) else HairlineBorder,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.btn_age_center),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = contentColor,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                    Text(
                        text = "Yr ${currentAge + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.Bold,
                        color = if (canPress) Color.White.copy(alpha = 0.82f) else SteelTertiary,
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Backward-compatible bottom nav fallback delegating to ActionDockWithCenterAge.
 */
@Composable
fun MaishaBottomNav(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    disabledTabs: Set<MainTab> = emptySet(),
    modifier: Modifier = Modifier
) {
    ActionDockWithCenterAge(
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        onAgeUp = {},
        ageUpEnabled = false,
        isAgingUp = false,
        currentAge = 0,
        disabledTabs = disabledTabs,
        modifier = modifier
    )
}


