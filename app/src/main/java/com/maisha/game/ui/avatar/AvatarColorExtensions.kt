// app/src/main/java/com/maisha/game/ui/avatar/AvatarColorExtensions.kt
package com.maisha.game.ui.avatar

import androidx.compose.ui.graphics.Color

/**
 * Darkens a [Color] by [factor] in range [0.0..1.0].
 * factor = 0.0 leaves the color unchanged; factor = 1.0 produces black.
 */
fun Color.darken(factor: Float): Color {
    val f = (1f - factor.coerceIn(0f, 1f))
    return Color(
        red = (red * f).coerceIn(0f, 1f),
        green = (green * f).coerceIn(0f, 1f),
        blue = (blue * f).coerceIn(0f, 1f),
        alpha = alpha
    )
}

/**
 * Lightens a [Color] by [factor] in range [0.0..1.0].
 * factor = 0.0 leaves the color unchanged; factor = 1.0 produces white.
 */
fun Color.lighten(factor: Float): Color {
    val f = factor.coerceIn(0f, 1f)
    return Color(
        red = (red + (1f - red) * f).coerceIn(0f, 1f),
        green = (green + (1f - green) * f).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * f).coerceIn(0f, 1f),
        alpha = alpha
    )
}

/**
 * Linearly interpolates between [start] and [stop] colors by [fraction].
 */
fun lerpColor(start: Color, stop: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (stop.red - start.red) * f,
        green = start.green + (stop.green - start.green) * f,
        blue = start.blue + (stop.blue - start.blue) * f,
        alpha = start.alpha + (stop.alpha - start.alpha) * f
    )
}
