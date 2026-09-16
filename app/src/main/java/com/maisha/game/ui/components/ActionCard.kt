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
import com.maisha.game.feedback.HapticType
import com.maisha.game.ui.feedback.LocalFeedbackManager
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.OutlineInk

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
    val fill = when {
        !enabled -> Color(0xFFE0E0E0)
        accent == ActionCardAccent.RISK -> Color(0xFFE53935)
        accent == ActionCardAccent.CARE -> LifeGreen
        accent == ActionCardAccent.GOLD -> GoldAccent
        else -> Color(0xFF4FC3F7)
    }
    val onFill = when {
        !enabled -> Color(0xFF111111).copy(alpha = 0.45f)
        accent == ActionCardAccent.GOLD -> Color(0xFF111111)
        else -> Color.White
    }
    val shape = RoundedCornerShape(12.dp)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.15f)
            .border(2.5.dp, OutlineInk, shape)
            .clickable(enabled = enabled) {
                feedbackManager.triggerHaptic(view, HapticType.LIGHT_TAP)
                onClick()
            },
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = fill),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            MaishaIcon(
                icon = icon,
                contentDescription = null,
                size = 36.dp,
                tint = onFill
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = onFill,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            val caption = questHint ?: metaLabel
            if (!caption.isNullOrBlank()) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.labelSmall,
                    color = onFill.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
