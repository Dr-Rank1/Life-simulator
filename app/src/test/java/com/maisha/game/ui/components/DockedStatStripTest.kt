package com.maisha.game.ui.components

import com.maisha.game.ui.theme.AmberWarning
import com.maisha.game.ui.theme.CrimsonDanger
import com.maisha.game.ui.theme.EmeraldCash
import org.junit.Assert.assertEquals
import org.junit.Test

class DockedStatStripTest {

    @Test
    fun testStatThresholdColorStandard() {
        // High values (> 60) should return Emerald
        assertEquals(EmeraldCash, statThresholdColor(100))
        assertEquals(EmeraldCash, statThresholdColor(85))
        assertEquals(EmeraldCash, statThresholdColor(61))

        // Medium values (30..60) should return Amber
        assertEquals(AmberWarning, statThresholdColor(60))
        assertEquals(AmberWarning, statThresholdColor(45))
        assertEquals(AmberWarning, statThresholdColor(30))

        // Low values (< 30) should return Crimson
        assertEquals(CrimsonDanger, statThresholdColor(29))
        assertEquals(CrimsonDanger, statThresholdColor(15))
        assertEquals(CrimsonDanger, statThresholdColor(0))
    }

    @Test
    fun testStatThresholdColorClamping() {
        // Out-of-bounds inputs should clamp properly
        assertEquals(EmeraldCash, statThresholdColor(150))
        assertEquals(CrimsonDanger, statThresholdColor(-20))
    }

    @Test
    fun testStatThresholdColorInverted() {
        // Inverted (e.g. stress): low is good (Emerald), high is dangerous (Crimson)
        assertEquals(EmeraldCash, statThresholdColor(10, inverted = true))
        assertEquals(EmeraldCash, statThresholdColor(29, inverted = true))

        assertEquals(AmberWarning, statThresholdColor(30, inverted = true))
        assertEquals(AmberWarning, statThresholdColor(50, inverted = true))
        assertEquals(AmberWarning, statThresholdColor(60, inverted = true))

        assertEquals(CrimsonDanger, statThresholdColor(61, inverted = true))
        assertEquals(CrimsonDanger, statThresholdColor(95, inverted = true))
    }

    @Test
    fun testStatTypeEnergyAndStress() {
        assertEquals(EmeraldCash, StatType.ENERGY.color())
        assertEquals(AmberWarning, StatType.STRESS.color())
    }
}
