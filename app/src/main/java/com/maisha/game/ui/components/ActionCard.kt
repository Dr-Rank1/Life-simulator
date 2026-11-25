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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Surface
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
import com.maisha.game.ui.theme.SlateNavyCard
import com.maisha.game.ui.theme.SlateNavyMuted
import com.maisha.game.ui.theme.SteelSecondary
import com.maisha.game.ui.theme.SteelTertiary
import com.maisha.game.ui.theme.TabularFigures
import com.maisha.game.ui.theme.TealPrimary

enum class ActionCardAccent {
    DEFAULT,
    CARE,
    RISK,
    GOLD
}

/**
 * Tactile drill-down row for activity options:
 * Standardized 64.dp-72.dp rounded card (12.dp radius, #131822 fill, 1.dp border)
 * containing a 40.dp tinted vector icon container on the left, a bold title + concise
 * 1-line subtitle in the middle, and a clean chevron (Icons.Rounded.ChevronRight) on the right.
 */
@Composable
fun ActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    metaLabel: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: ActionCardAccent = ActionCardAccent.DEFAULT,
    questHint: String? = null,
    riskLabel: String? = null,
    energyCost: Int? = null
) {
    val view = LocalView.current
    val feedbackManager = runCatching { LocalFeedbackManager.current }.getOrNull()

    val iconTint = when {
        !enabled -> SteelTertiary
        accent == ActionCardAccent.RISK -> CrimsonDanger
        accent == ActionCardAccent.CARE -> EmeraldCash
        accent == ActionCardAccent.GOLD -> AmberWarning
        else -> TealPrimary
    }
    val iconBg = when {
        !enabled -> SlateNavyMuted
        else -> iconTint.copy(alpha = 0.16f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp, max = 74.dp)
            .semantics { role = Role.Button }
            .clickable(enabled = enabled) {
                feedbackManager?.triggerHaptic(view, HapticType.LIGHT_TAP)
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SlateNavyCard,
            disabledContainerColor = SlateNavyMuted
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (enabled) 2.dp else 0.dp),
        border = BorderStroke(1.dp, HairlineBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left: 40dp Tinted Vector Icon Container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg)
                    .border(1.dp, iconTint.copy(alpha = 0.28f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
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
                    color = if (enabled) OffWhitePrimary else SteelTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 15.sp
                )

                val subtitleText = description.ifBlank { metaLabel ?: "" }
                if (subtitleText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitleText,
                        style = TabularFigures.copy(fontSize = 12.sp, color = if (enabled) SteelSecondary else SteelTertiary, fontWeight = FontWeight.Normal),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (energyCost != null || riskLabel != null || questHint != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        energyCost?.let { cost ->
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
                                        text = "$cost Energy",
                                        style = TabularFigures.copy(fontSize = 10.sp, color = AmberWarning, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }

                        riskLabel?.let { risk ->
                            val lower = risk.lowercase()
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
                                    text = risk,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = riskColor,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        questHint?.let { hint ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EmeraldCash.copy(alpha = 0.14f))
                                    .border(0.5.dp, EmeraldCash.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = hint,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldCash,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Right: Clean Chevron
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = if (enabled) SteelSecondary else SteelTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
