package com.maisha.game.ui.theme

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/** Consistent spacing scale — prefer these over ad-hoc dp in new UI. */
object MaishaSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
}

object MaishaRadius {
    val card = 16.dp
    val button = 14.dp
    val ageUp = 22.dp
    val sheet = 24.dp
    val avatar = 20.dp
    val cardShape = RoundedCornerShape(card)
    val buttonShape = RoundedCornerShape(button)
    val ageUpShape = RoundedCornerShape(ageUp)
}

object MaishaElevation {
    val none = 0.dp
    val card = 1.dp
    val raised = 2.dp
    val dialog = 8.dp
}

object MaishaBorder {
    val hairline = BorderStroke(1.dp, HairlineBorder)
    val elevated = BorderStroke(1.dp, SlateNavyElevated)
}

private val MaishaDarkColorScheme = darkColorScheme(
    primary = RoyalIndigo,
    onPrimary = Color.White,
    primaryContainer = RoyalIndigoSoft,
    onPrimaryContainer = OffWhitePrimary,
    secondary = EmeraldCash,
    onSecondary = Color.White,
    secondaryContainer = EmeraldCashSoft,
    onSecondaryContainer = OffWhitePrimary,
    tertiary = AmberWarning,
    onTertiary = ObsidianBg,
    background = ObsidianBg,
    onBackground = OffWhitePrimary,
    surface = SlateNavyCard,
    onSurface = OffWhitePrimary,
    surfaceVariant = SlateNavyMuted,
    onSurfaceVariant = SteelSecondary,
    error = CrimsonDanger,
    onError = Color.White,
    outline = HairlineBorder,
    outlineVariant = SlateNavyMuted
)

@Composable
fun MaishaTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = MaishaDarkColorScheme,
        typography = MaishaTypography,
        content = content
    )
}

