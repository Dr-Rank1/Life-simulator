package com.maisha.game.domain

import com.maisha.game.data.model.LifestyleOption
import com.maisha.game.data.model.Person
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.Stats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EarlyGameAgeGateTest {

    private val healthEngine = HealthEngine()
    private val financeEngine = FinanceEngine()
    private val relationshipEngine = RelationshipEngine(financeEngine)

    @Test
    fun infantCannotPurchaseHealthInsurance() {
        val baby = TestFixtures.character(age = 1, stats = Stats(money = 100_000))
        val updated = healthEngine.setLifestyleOption(baby, LifestyleOption.HEALTH_INSURANCE, true)

        assertFalse("Infant should not be able to purchase independent health insurance", updated.lifestyle.hasHealthInsurance)
    }

    @Test
    fun infantDoesNotSufferDebtStress() {
        val newborn = TestFixtures.character(
            age = 0,
            stats = Stats(health = 100, happiness = 100, money = 0)
        )
        val agedUp = healthEngine.processHealthProgression(newborn)

        assertEquals("Newborn health should not be drained by debt stress", 100, agedUp.stats.health)
        assertEquals("Newborn happiness should not be drained by debt stress", 100, agedUp.stats.happiness)
    }

    @Test
    fun macroEconomicShiftsDoNotSpamMinorWithoutAssets() {
        val toddler = TestFixtures.character(age = 2, assets = emptyList())
        val resultBoom = financeEngine.applyEconomicShift(toddler, forced = "boom")
        val resultBust = financeEngine.applyEconomicShift(toddler, forced = "bust")

        assertEquals("Toddler log should remain empty during economic shifts", 0, resultBoom.character.eventLog.size)
        assertEquals("Toddler log should remain empty during economic shifts", 0, resultBust.character.eventLog.size)
    }

    @Test
    fun adultWithAssetsReceivesEconomicShiftNotice() {
        val house = TestFixtures.asset(currentValue = 200_000)
        val adult = TestFixtures.character(age = 30, assets = listOf(house))
        val resultBoom = financeEngine.applyEconomicShift(adult, forced = "boom")

        assertTrue("Adult with assets should log economic shift", resultBoom.character.eventLog.isNotEmpty())
    }

    @Test
    fun cannotRepeatSameRelationshipInteractionInSameYear() {
        val parent = Person(id = "parent-1", name = "Mom", age = 30, relation = RelationType.MOTHER, relationshipLevel = 80)
        val infant = TestFixtures.character(age = 1, family = listOf(parent))

        val result1 = relationshipEngine.progressRelationship(infant, parent.id, InteractionType.SPEND_TIME)
        val updatedParent1 = result1.character.family.first { it.id == parent.id }
        assertTrue(updatedParent1.interactedTypesThisYear.contains(InteractionType.SPEND_TIME.name))

        val result2 = relationshipEngine.progressRelationship(result1.character, parent.id, InteractionType.SPEND_TIME)
        assertEquals("msg_interaction_already_done", result2.messageKey)

        // But a different interaction type should be permitted
        val result3 = relationshipEngine.progressRelationship(result1.character, parent.id, InteractionType.COMPLIMENT)
        val updatedParent3 = result3.character.family.first { it.id == parent.id }
        assertTrue(updatedParent3.interactedTypesThisYear.contains(InteractionType.COMPLIMENT.name))

        // After yearly tick, interactedTypesThisYear resets
        val ticked = relationshipEngine.tickFamilyYear(result3.character)
        val tickedParent = ticked.family.first { it.id == parent.id }
        assertTrue("Interactions list should reset on year tick", tickedParent.interactedTypesThisYear.isEmpty())
    }

    @Test
    fun minorCannotThrowParty() {
        val minor = TestFixtures.character(age = 14, stats = Stats(money = 10_000))
        val partyResult = relationshipEngine.throwParty(minor, budget = 5_000)

        assertTrue("Minor under 16 cannot throw a party", partyResult is PartyResult.InvalidBudget)
    }
}
