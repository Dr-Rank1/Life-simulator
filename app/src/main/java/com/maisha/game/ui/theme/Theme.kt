// app/src/main/java/com/maisha/game/ui/theme/Theme.kt
package com.maisha.game.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Consistent spacing scale — prefer these over ad-hoc dp in new UI. */
object MaishaSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
}

object MaishaRadius {
    val card = 14.dp
    val button = 14.dp
    val ageUp = 22.dp
    val sheet = 20.dp
    val avatar = 99.dp
    val cardShape = RoundedCornerShape(card)
    val buttonShape = RoundedCornerShape(button)
    val ageUpShape = RoundedCornerShape(ageUp)
}

object MaishaElevation {
    val card = 0.dp
    val raised = 0.dp
    val dialog = 0.dp
}

private val MaishaLightColorScheme = lightColorScheme(
    primary = LifeGreen,
    onPrimary = Color.White,
    primaryContainer = LifeGreenSoft,
    onPrimaryContainer = InkPrimary,
    secondary = GoldAccent,
    onSecondary = InkPrimary,
    secondaryContainer = Color(0x33FFC107),
    onSecondaryContainer = InkPrimary,
    tertiary = AccentPink,
    onTertiary = Color.White,
    background = CreamBg,
    onBackground = InkPrimary,
    surface = PaperWhite,
    onSurface = InkPrimary,
    surfaceVariant = CreamMuted,
    onSurfaceVariant = InkSecondary,
    error = CoralNegative,
    onError = Color.White,
    outline = OutlineInk,
    outlineVariant = OutlineInk
)

@Composable
fun MaishaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaishaLightColorScheme,
        typography = MaishaTypography,
        content = content
    )
}
