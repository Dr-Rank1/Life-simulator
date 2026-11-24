// app/src/main/java/com/maisha/game/ui/components/AgeUpButton.kt
package com.maisha.game.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.R
import com.maisha.game.feedback.HapticType
import com.maisha.game.ui.feedback.LocalFeedbackManager
import com.maisha.game.ui.theme.ComicStyle
import com.maisha.game.ui.theme.CreamMuted
import com.maisha.game.ui.theme.Hairline
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.LifeGreenPressed
import com.maisha.game.ui.theme.MaishaRadius


import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush

@Composable
fun AgeUpButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val canPress = enabled && !isLoading
    val feedbackManager = LocalFeedbackManager.current
    val view = LocalView.current

    val scale by animateFloatAsState(
        targetValue = if (pressed && canPress) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ageUpScale"
    )

    val buttonBrush = when {
        !canPress -> Brush.verticalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
        pressed -> Brush.verticalGradient(listOf(LifeGreenPressed, Color(0xFF15803D)))
        else -> Brush.verticalGradient(listOf(LifeGreen, Color(0xFF16A34A)))
    }
    val lipColor = if (canPress) Color(0xFF15803D) else Color(0xFF94A3B8)
    val contentColor = if (canPress) Color.White else InkTertiary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .scale(scale)
            .shadow(
                elevation = if (canPress && !pressed) 4.dp else 0.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = LifeGreen.copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(lipColor)
            .semantics { role = Role.Button }
            .pointerInput(canPress, onClick) {
                if (!canPress) return@pointerInput
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    pressed = true
                    feedbackManager.triggerHaptic(view, HapticType.LIGHT_TAP)
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
        // 3D tactile button face that pushes down onto the bottom lip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .padding(top = if (pressed && canPress) 4.dp else 0.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(buttonBrush)
                .border(
                    width = 1.dp,
                    color = if (canPress) Color.White.copy(alpha = 0.25f) else Hairline,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when {
                    isLoading -> stringResource(R.string.btn_aging)
                    else -> stringResource(R.string.btn_live_another_year)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                fontSize = 17.sp,
                letterSpacing = 0.3.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

