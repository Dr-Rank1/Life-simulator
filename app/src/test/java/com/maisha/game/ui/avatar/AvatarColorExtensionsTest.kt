package com.maisha.game.ui.avatar

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AvatarColorExtensionsTest {

    @Test
    fun darken_reducesRgbChannelsProportionally() {
        val original = Color(0xFF808080)
        val darkened = original.darken(0.20f)
        assertTrue(darkened.red < original.red)
        assertTrue(darkened.green < original.green)
        assertTrue(darkened.blue < original.blue)
        assertEquals(original.alpha, darkened.alpha, 0.001f)
    }

    @Test
    fun lighten_increasesRgbChannelsProportionally() {
        val original = Color(0xFF404040)
        val lightened = original.lighten(0.20f)
        assertTrue(lightened.red > original.red)
        assertTrue(lightened.green > original.green)
        assertTrue(lightened.blue > original.blue)
        assertEquals(original.alpha, lightened.alpha, 0.001f)
    }

    @Test
    fun lerpColor_interpolatesCorrectly() {
        val black = Color(0xFF000000)
        val white = Color(0xFFFFFFFF)
        val mid = lerpColor(black, white, 0.5f)
        assertEquals(0.5f, mid.red, 0.01f)
        assertEquals(0.5f, mid.green, 0.01f)
        assertEquals(0.5f, mid.blue, 0.01f)
    }

    @Test
    fun darkenAndLighten_handleZeroAndFullBoundsSafely() {
        val base = Color(0xFF5588AA)
        assertEquals(base, base.darken(0.0f))
        assertEquals(base, base.lighten(0.0f))

        val fullyDark = base.darken(1.0f)
        assertEquals(0f, fullyDark.red, 0.001f)
        assertEquals(0f, fullyDark.green, 0.001f)
        assertEquals(0f, fullyDark.blue, 0.001f)

        val fullyLight = base.lighten(1.0f)
        assertEquals(1f, fullyLight.red, 0.001f)
        assertEquals(1f, fullyLight.green, 0.001f)
        assertEquals(1f, fullyLight.blue, 0.001f)
    }
}
