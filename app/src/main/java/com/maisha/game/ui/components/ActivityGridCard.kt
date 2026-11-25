package com.maisha.game.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.ui.theme.BorderCardSlate
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.CreamMuted
import com.maisha.game.ui.theme.DeepSlate
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.Hairline
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.RoyalIndigo
import com.maisha.game.ui.theme.SlateNavyMuted
import com.maisha.game.ui.theme.TealPrimary

/**
 * Risk tier for arcade activity tiles.
 */
enum class ActivityRiskLevel {
    SAFE,
    LOW,
    MODERATE,
    HIGH,
    UNDERWORLD
}

/**
 * Arcade-style 2-column activity tile featuring a bold icon medallion,
 * energy cost pips, risk rating chip, and spring squash physics on tap.
 */
@Composable
fun ActivityGridCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    energyCost: Int = 1,
    riskLevel: ActivityRiskLevel = ActivityRiskLevel.SAFE,
    trailingBadge: String? = null,
    questHint: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "activityCardScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) PaperWhite else SlateNavyMuted
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPressed) 1.dp else 2.dp),
        border = BorderStroke(1.dp, if (questHint != null) GoldAccent.copy(alpha = 0.5f) else BorderCardSlate)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Medallion & Energy Pips / Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(iconTint.copy(alpha = 0.22f), iconTint.copy(alpha = 0.08f))
                            )
                        )
                        .border(1.dp, iconTint.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) iconTint else Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (trailingBadge != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CoralNegative.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = trailingBadge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CoralNegative,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Energy Pips (1 to 3 dots)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..3) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i <= energyCost) GoldAccent else Hairline
                                    )
                            )
                        }
                    }
                }
            }

            // Title & Subtitle
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) InkPrimary else Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom Row: Risk Chip & Quest Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RiskBadgeChip(riskLevel = riskLevel)

                if (questHint != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = GoldAccent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "QUEST",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldAccent,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RiskBadgeChip(riskLevel: ActivityRiskLevel) {
    val (label, bg, fg) = when (riskLevel) {
        ActivityRiskLevel.SAFE -> Triple("Safe", LifeGreen.copy(alpha = 0.12f), LifeGreen)
        ActivityRiskLevel.LOW -> Triple("Low Risk", TealPrimary.copy(alpha = 0.12f), TealPrimary)
        ActivityRiskLevel.MODERATE -> Triple("Moderate", GoldAccent.copy(alpha = 0.14f), GoldAccent)
        ActivityRiskLevel.HIGH -> Triple("High Stakes", CoralNegative.copy(alpha = 0.14f), CoralNegative)
        ActivityRiskLevel.UNDERWORLD -> Triple("Underworld", RoyalIndigo.copy(alpha = 0.16f), RoyalIndigo)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
