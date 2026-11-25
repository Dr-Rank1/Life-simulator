package com.maisha.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.feedback.HapticType
import com.maisha.game.ui.feedback.LocalFeedbackManager
import com.maisha.game.ui.theme.AmberWarning
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.CrimsonDanger
import com.maisha.game.ui.theme.EmeraldCash
import com.maisha.game.ui.theme.HairlineBorder
import com.maisha.game.ui.theme.OffWhitePrimary
import com.maisha.game.ui.theme.RoyalIndigo
import com.maisha.game.ui.theme.SlateNavyCard
import com.maisha.game.ui.theme.SlateNavyElevated
import com.maisha.game.ui.theme.SlateNavyMuted
import com.maisha.game.ui.theme.SteelSecondary
import com.maisha.game.ui.theme.SteelTertiary
import com.maisha.game.ui.theme.TabularFigures

/**
 * Tactile drill-down row inspired by BitLife and Another Life:
 * Standardized 64.dp-72.dp rounded card (12.dp radius, #131822 fill, 1.dp border)
 * containing a 40.dp tinted vector icon container on the left, a bold title + concise
 * 1-line subtitle (salary, cost, energy cost, or risk badge) in the middle, and a
 * clean chevron (Icons.Rounded.ChevronRight) or status pill on the right.
 */
@Composable
fun DrillDownRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = EmeraldCash,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingBadge: String? = null,
    trailingBadgeColor: Color? = null,
    pricePill: String? = null,
    riskPill: String? = null,
    energyCost: Int? = null,
    isAgeLocked: Boolean = false,
    lockBadgeText: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    insideContainer: Boolean = false
) {
    val isRowEnabled = enabled && !isAgeLocked
    val view = LocalView.current
    val feedbackManager = runCatching { LocalFeedbackManager.current }.getOrNull()

    val rowContent = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left: 40dp Tinted Vector Icon Container or Custom Leading Content
            if (leadingContent != null) {
                leadingContent()
            } else if (icon != null) {
                val badgeColor = when {
                    isAgeLocked -> SteelTertiary
                    iconTint != Color.Unspecified && iconTint != Color.Transparent -> iconTint
                    else -> EmeraldCash
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeColor.copy(alpha = 0.16f))
                        .border(1.dp, badgeColor.copy(alpha = 0.28f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAgeLocked) AppIcons.Lock else icon,
                        contentDescription = null,
                        tint = if (isAgeLocked) SteelTertiary else badgeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Middle: Bold Title + Concise 1-Line Subtitle & Subtle Badges
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isRowEnabled) OffWhitePrimary else SteelTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 15.sp
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = TabularFigures.copy(fontSize = 12.sp, color = if (isRowEnabled) SteelSecondary else SteelTertiary, fontWeight = FontWeight.Normal),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!pricePill.isNullOrBlank() || !riskPill.isNullOrBlank() || energyCost != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!pricePill.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EmeraldCash.copy(alpha = 0.14f))
                                    .border(0.5.dp, EmeraldCash.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = pricePill,
                                    style = TabularFigures.copy(fontSize = 10.sp, color = if (isRowEnabled) EmeraldCash else SteelTertiary, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        if (energyCost != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AmberWarning.copy(alpha = 0.14f))
                                    .border(0.5.dp, AmberWarning.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = AppIcons.NavActions,
                                        contentDescription = null,
                                        tint = AmberWarning,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "$energyCost Energy",
                                        style = TabularFigures.copy(fontSize = 10.sp, color = AmberWarning, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                        if (!riskPill.isNullOrBlank()) {
                            val lower = riskPill.lowercase()
                            val isHigh = "high" in lower || "extreme" in lower
                            val isMed = "med" in lower || "mod" in lower
                            val riskColor = if (isHigh) CrimsonDanger else if (isMed) AmberWarning else EmeraldCash
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(riskColor.copy(alpha = 0.14f))
                                    .border(0.5.dp, riskColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = riskPill,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = riskColor,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Right: Trailing Badge / Status Pill or Clean Chevron
            if (isAgeLocked && lockBadgeText != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateNavyMuted)
                        .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Lock,
                            contentDescription = null,
                            tint = SteelSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = lockBadgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SteelSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            } else if (trailingBadge != null) {
                val badgeBg = trailingBadgeColor ?: iconTint.copy(alpha = 0.16f)
                val badgeText = if (trailingBadgeColor != null) Color.White else iconTint
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .border(0.5.dp, (trailingBadgeColor ?: iconTint).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = trailingBadge,
                        style = TabularFigures.copy(fontSize = 11.sp, color = badgeText, fontWeight = FontWeight.Bold)
                    )
                }
            }

            if (trailingContent != null) {
                trailingContent()
            } else {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = if (isRowEnabled) SteelSecondary else SteelTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp, max = 72.dp)
            .semantics { role = Role.Button }
            .clickable(
                enabled = isRowEnabled,
                onClick = {
                    feedbackManager?.triggerHaptic(view, HapticType.LIGHT_TAP)
                    onClick()
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SlateNavyCard,
            disabledContainerColor = SlateNavyMuted
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, HairlineBorder)
    ) {
        rowContent()
    }
}
