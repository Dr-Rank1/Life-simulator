package com.maisha.game.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import com.maisha.game.feedback.HapticType
import com.maisha.game.ui.feedback.LocalFeedbackManager
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.CreamMuted
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.Hairline
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.LifeGreenSoft
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.TealPrimary


enum class ActionCardAccent {
    DEFAULT,
    CARE,
    RISK,
    GOLD
}

/** Two-column activity tile: icon over title, like a life-sim Activities grid. */
@Composable
@Suppress("UNUSED_PARAMETER")
fun ActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    metaLabel: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: ActionCardAccent = ActionCardAccent.DEFAULT,
    questHint: String? = null
) {
    val view = LocalView.current
    val feedbackManager = LocalFeedbackManager.current
    val iconBg = when {
        !enabled -> CreamMuted
        accent == ActionCardAccent.RISK -> CoralNegative.copy(alpha = 0.12f)
        accent == ActionCardAccent.CARE -> LifeGreenSoft
        accent == ActionCardAccent.GOLD -> GoldAccent.copy(alpha = 0.15f)
        else -> TealPrimary.copy(alpha = 0.12f)
    }
    val iconTint = when {
        !enabled -> InkTertiary
        accent == ActionCardAccent.RISK -> CoralNegative
        accent == ActionCardAccent.CARE -> LifeGreen
        accent == ActionCardAccent.GOLD -> GoldAccent
        else -> TealPrimary
    }
    val shape = RoundedCornerShape(16.dp)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) {
                feedbackManager.triggerHaptic(view, HapticType.LIGHT_TAP)
                onClick()
            },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) PaperWhite else CreamMuted
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (enabled) 2.dp else 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                MaishaIcon(
                    icon = icon,
                    contentDescription = null,
                    size = 24.dp,
                    tint = iconTint
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) InkPrimary else InkTertiary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled) InkSecondary else InkTertiary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            val caption = questHint ?: metaLabel
            if (!caption.isNullOrBlank()) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (questHint != null) LifeGreen else GoldAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

