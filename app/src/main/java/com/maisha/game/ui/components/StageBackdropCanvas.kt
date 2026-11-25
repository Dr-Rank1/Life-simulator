package com.maisha.game.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.maisha.game.data.model.Character

/**
 * Procedural life-stage and status backdrop types for the hero header.
 */
enum class StageBackdropType {
    INFANCY_CHILDHOOD,
    SCHOOL_CAMPUS,
    CAREER_SKYLINE,
    ELDERHOOD,
    PRISON,
    HOSPITAL
}

/**
 * Resolves the active backdrop type from the character's live state.
 */
fun resolveStageBackdrop(character: Character): StageBackdropType {
    return when {
        character.criminalRecord.currentlyIncarcerated -> StageBackdropType.PRISON
        character.stats.health < 25 -> StageBackdropType.HOSPITAL
        character.age < 13 -> StageBackdropType.INFANCY_CHILDHOOD
        character.education.stage.name != "NONE" && (character.age in 13..22 || character.education.stage.name == "UNIVERSITY") -> StageBackdropType.SCHOOL_CAMPUS
        character.age >= 60 -> StageBackdropType.ELDERHOOD
        else -> StageBackdropType.CAREER_SKYLINE
    }
}

/**
 * Lightweight, 100% offline procedural Canvas scene backdrop.
 * Renders behind the avatar and header card with stage-adaptive palettes and subtle geometric motifs.
 */
@Composable
fun StageBackdropCanvas(
    stageType: StageBackdropType,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        when (stageType) {
            StageBackdropType.INFANCY_CHILDHOOD -> drawChildhoodScene()
            StageBackdropType.SCHOOL_CAMPUS -> drawCampusScene()
            StageBackdropType.CAREER_SKYLINE -> drawSkylineScene()
            StageBackdropType.ELDERHOOD -> drawElderhoodScene()
            StageBackdropType.PRISON -> drawPrisonScene()
            StageBackdropType.HOSPITAL -> drawHospitalScene()
        }
    }
}

private fun DrawScope.drawChildhoodScene() {
    val w = size.width
    val h = size.height

    // Warm dark amber-tinted dawn gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1F1815), Color(0xFF171822), Color(0xFF131822))
        )
    )

    // Sun arc in top right corner
    drawCircle(
        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
        radius = h * 0.7f,
        center = Offset(w * 0.95f, 0f)
    )
    drawCircle(
        color = Color(0xFFFBBF24).copy(alpha = 0.22f),
        radius = h * 0.45f,
        center = Offset(w * 0.95f, 0f)
    )

    // Gentle rolling grassy hills at bottom
    val hillPath = Path().apply {
        moveTo(0f, h * 0.65f)
        cubicTo(w * 0.25f, h * 0.55f, w * 0.6f, h * 0.75f, w, h * 0.62f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = hillPath,
        color = Color(0xFF10B981).copy(alpha = 0.15f)
    )

    // Playful floating circles (bubbles/balloons motif)
    drawCircle(Color(0xFF60A5FA).copy(alpha = 0.22f), radius = 6f, center = Offset(w * 0.15f, h * 0.35f))
    drawCircle(Color(0xFFF472B6).copy(alpha = 0.22f), radius = 9f, center = Offset(w * 0.32f, h * 0.22f))
    drawCircle(Color(0xFFFBBF24).copy(alpha = 0.25f), radius = 5f, center = Offset(w * 0.78f, h * 0.42f))
}

private fun DrawScope.drawCampusScene() {
    val w = size.width
    val h = size.height

    // Collegiate dark indigo-navy sky
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF181B2E), Color(0xFF141824), Color(0xFF131822))
        )
    )

    // Academic column lines
    val colColor = Color(0xFF6366F1).copy(alpha = 0.10f)
    for (i in 0..5) {
        val x = w * (0.15f + i * 0.14f)
        drawRect(
            color = colColor,
            topLeft = Offset(x, h * 0.25f),
            size = Size(8f, h * 0.75f)
        )
    }

    // Neoclassical pediment triangle / collegiate arch
    val archPath = Path().apply {
        moveTo(w * 0.05f, h * 0.35f)
        lineTo(w * 0.5f, h * 0.08f)
        lineTo(w * 0.95f, h * 0.35f)
        lineTo(w * 0.95f, h * 0.40f)
        lineTo(w * 0.05f, h * 0.40f)
        close()
    }
    drawPath(path = archPath, color = Color(0xFF6366F1).copy(alpha = 0.12f))

    // Stylized graduation cap diamond geometry
    val capPath = Path().apply {
        moveTo(w * 0.85f, h * 0.22f)
        lineTo(w * 0.92f, h * 0.17f)
        lineTo(w * 0.99f, h * 0.22f)
        lineTo(w * 0.92f, h * 0.27f)
        close()
    }
    drawPath(path = capPath, color = Color(0xFF818CF8).copy(alpha = 0.22f))
}

private fun DrawScope.drawSkylineScene() {
    val w = size.width
    val h = size.height

    // Modern corporate dark emerald/slate gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF101E1D), Color(0xFF121B21), Color(0xFF131822))
        )
    )

    // Geometric city towers
    val towers = listOf(
        Triple(0.02f, 0.22f, 0.48f),
        Triple(0.18f, 0.16f, 0.32f),
        Triple(0.32f, 0.20f, 0.55f),
        Triple(0.50f, 0.18f, 0.38f),
        Triple(0.66f, 0.22f, 0.60f),
        Triple(0.85f, 0.14f, 0.42f)
    )

    val towerColor = Color(0xFF10B981).copy(alpha = 0.10f)
    val spireColor = Color(0xFF10B981).copy(alpha = 0.20f)

    towers.forEach { (xRatio, wRatio, topRatio) ->
        val x = w * xRatio
        val tw = w * wRatio
        val y = h * topRatio
        drawRect(
            color = towerColor,
            topLeft = Offset(x, y),
            size = Size(tw, h - y)
        )
        // Spire on top of tall tower
        if (topRatio < 0.45f) {
            drawLine(
                color = spireColor,
                start = Offset(x + tw / 2, y),
                end = Offset(x + tw / 2, y - 14f),
                strokeWidth = 2.5f
            )
        }
    }

    // Upward financial trend line accent
    val trendPath = Path().apply {
        moveTo(0f, h * 0.85f)
        lineTo(w * 0.35f, h * 0.72f)
        lineTo(w * 0.65f, h * 0.58f)
        lineTo(w, h * 0.40f)
    }
    drawPath(
        path = trendPath,
        color = Color(0xFF10B981).copy(alpha = 0.30f),
        style = Stroke(width = 2f)
    )
}

private fun DrawScope.drawElderhoodScene() {
    val w = size.width
    val h = size.height

    // Warm golden hour dark sunset
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF221A12), Color(0xFF1A171D), Color(0xFF131822))
        )
    )

    // Gentle radiating sunset rays
    val rayColor = Color(0xFFF59E0B).copy(alpha = 0.08f)
    val sunCenter = Offset(w * 0.5f, h * 1.05f)
    drawCircle(
        color = Color(0xFFF59E0B).copy(alpha = 0.20f),
        radius = h * 0.75f,
        center = sunCenter
    )
    for (angle in -60..60 step 20) {
        val rad = Math.toRadians(angle.toDouble() - 90.0)
        val endX = sunCenter.x + (w * 0.8f * Math.cos(rad)).toFloat()
        val endY = sunCenter.y + (w * 0.8f * Math.sin(rad)).toFloat()
        drawLine(
            color = rayColor,
            start = sunCenter,
            end = Offset(endX, endY),
            strokeWidth = 3f
        )
    }

    // Horizon line
    drawLine(
        color = Color(0xFFD97706).copy(alpha = 0.22f),
        start = Offset(0f, h * 0.82f),
        end = Offset(w, h * 0.82f),
        strokeWidth = 2f
    )
}

private fun DrawScope.drawPrisonScene() {
    val w = size.width
    val h = size.height

    // Cold slate industrial yard
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0B0E14), Color(0xFF131822), Color(0xFF1A2130))
        )
    )

    // Steel vertical bars
    val barColor = Color(0xFF8E9BAE).copy(alpha = 0.20f)
    val barHighlight = Color(0xFFF1F5F9).copy(alpha = 0.12f)
    val barCount = 10
    val step = w / barCount
    for (i in 0..barCount) {
        val bx = i * step
        drawRect(
            color = barColor,
            topLeft = Offset(bx, 0f),
            size = Size(6f, h)
        )
        drawLine(
            color = barHighlight,
            start = Offset(bx + 1.5f, 0f),
            end = Offset(bx + 1.5f, h),
            strokeWidth = 1.5f
        )
    }

    // Horizontal reinforced cross-brackets
    drawRect(
        color = Color(0xFF1F2736),
        topLeft = Offset(0f, h * 0.28f),
        size = Size(w, 8f)
    )
    drawRect(
        color = Color(0xFF1F2736),
        topLeft = Offset(0f, h * 0.72f),
        size = Size(w, 8f)
    )
}

private fun DrawScope.drawHospitalScene() {
    val w = size.width
    val h = size.height

    // Clean clinical dark cyan-teal
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0E1E22), Color(0xFF111A20), Color(0xFF131822))
        )
    )

    // Subtle medical cross watermark in background
    val crossColor = Color(0xFF14B8A6).copy(alpha = 0.10f)
    val cx = w * 0.82f
    val cy = h * 0.38f
    val arm = 22f
    val th = 7f
    drawRect(crossColor, topLeft = Offset(cx - th / 2, cy - arm), size = Size(th, arm * 2))
    drawRect(crossColor, topLeft = Offset(cx - arm, cy - th / 2), size = Size(arm * 2, th))

    // Procedural cardiac heartbeat pulse polyline
    val pulsePath = Path().apply {
        moveTo(0f, h * 0.75f)
        lineTo(w * 0.30f, h * 0.75f)
        lineTo(w * 0.36f, h * 0.65f)
        lineTo(w * 0.42f, h * 0.88f)
        lineTo(w * 0.48f, h * 0.52f)
        lineTo(w * 0.54f, h * 0.79f)
        lineTo(w * 0.60f, h * 0.75f)
        lineTo(w, h * 0.75f)
    }
    drawPath(
        path = pulsePath,
        color = Color(0xFF14B8A6).copy(alpha = 0.38f),
        style = Stroke(width = 2.5f)
    )
}

/**
 * Screen edge-glow feedback overlay for critical state changes or major positive windfalls.
 */
@Composable
fun EdgeGlowOverlay(
    isPositiveWindfall: Boolean,
    isCriticalWarning: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isPositiveWindfall && !isCriticalWarning) return

    val infiniteTransition = rememberInfiniteTransition(label = "edgeGlowPulse")
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val glowColor = when {
        isCriticalWarning -> Color(0xFFEF4444).copy(alpha = 0.35f * alphaPulse)
        isPositiveWindfall -> Color(0xFFF59E0B).copy(alpha = 0.35f * alphaPulse)
        else -> Color.Transparent
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Vignette rim drawing
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, glowColor.copy(alpha = 0.05f), glowColor),
                center = Offset(w / 2f, h / 2f),
                radius = Math.max(w, h) * 0.72f
            )
        )
    }
}
