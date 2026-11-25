package com.maisha.game.ui.components

import com.maisha.game.data.model.Person
import com.maisha.game.data.model.RelationType
import com.maisha.game.domain.TestFixtures
import com.maisha.game.ui.theme.AmberWarning
import com.maisha.game.ui.theme.CrimsonDanger
import com.maisha.game.ui.theme.EmeraldCash
import com.maisha.game.ui.theme.RoyalIndigo
import com.maisha.game.ui.theme.TealPrimary
import org.junit.Assert.assertEquals
import org.junit.Test

class PersonCardHelpersTest {

    @Test
    fun getPersonInitials_formatsCorrectly() {
        assertEquals("JD", getPersonInitials("John Doe"))
        assertEquals("AL", getPersonInitials("Alice Little"))
        assertEquals("MA", getPersonInitials("Mary"))
        assertEquals("?", getPersonInitials(""))
        assertEquals("?", getPersonInitials("   "))
        assertEquals("TS", getPersonInitials("  Triple   Space  "))
    }

    @Test
    fun relationColor_mapsExpectedSemanticAccents() {
        assertEquals(RoyalIndigo, relationColor(RelationType.SPOUSE))
        assertEquals(TealPrimary, relationColor(RelationType.MOTHER))
        assertEquals(TealPrimary, relationColor(RelationType.FATHER))
        assertEquals(AmberWarning, relationColor(RelationType.SIBLING))
        assertEquals(CrimsonDanger, relationColor(RelationType.ENEMY))
    }

    @Test
    fun personTrait_resolvesAccurateTraitAndColor() {
        val volatilePerson = TestFixtures.person(id = "1", name = "Test").copy(crazy = 85)
        val (volatileLabel, volatileColor) = personTrait(volatilePerson)
        assertEquals("VOLATILE", volatileLabel)
        assertEquals(CrimsonDanger, volatileColor)

        val calmPerson = TestFixtures.person(id = "2", name = "Test").copy(crazy = 20)
        val (calmLabel, calmColor) = personTrait(calmPerson)
        assertEquals("CALM", calmLabel)
        assertEquals(EmeraldCash, calmColor)

        val basePerson = TestFixtures.person(id = "3", name = "Test")
        val geniusPerson = basePerson.copy(crazy = 50, stats = basePerson.stats.copy(smarts = 90))
        val (geniusLabel, _) = personTrait(geniusPerson)
        assertEquals("GENIUS", geniusLabel)
    }
}
