package com.maisha.game.domain

import com.maisha.game.data.model.CareerState
import com.maisha.game.data.model.CriminalRecord
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.RoutineCategory
import com.maisha.game.data.model.Stats
import com.maisha.game.data.model.WorkplacePerson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class RoutineDilemmaEngineTest {

    private val engine = RoutineDilemmaEngine()

    @Test
    fun catalog_containsExpectedCategories() {
        val all = engine.getAllDilemmas()
        assertTrue(all.isNotEmpty())
        assertTrue(all.any { it.category == RoutineCategory.COMMUTE })
        assertTrue(all.any { it.category == RoutineCategory.WORK_STUDY })
        assertTrue(all.any { it.category == RoutineCategory.WEEKEND_SPLURGE })
        assertTrue(all.any { it.category == RoutineCategory.FAMILY_DINNER })
    }

    @Test
    fun findEligibleDilemma_respectsAgeConstraints() {
        val baby = TestFixtures.character(age = 2)
        val babyDilemma = engine.findEligibleDilemma(baby)
        assertNull(babyDilemma)

        val child = TestFixtures.character(
            age = 8,
            family = listOf(TestFixtures.person(relation = RelationType.MOTHER))
        )
        val childDilemma = engine.findEligibleDilemma(child)
        assertNotNull(childDilemma)
        assertTrue(childDilemma!!.minAge <= 8)
    }

    @Test
    fun findEligibleDilemma_respectsEmploymentRequirement() {
        val unemployed = TestFixtures.character(
            age = 25,
            career = CareerState(currentJob = null)
        )
        val dilemma = engine.findEligibleDilemma(unemployed, preferredCategory = RoutineCategory.WORK_STUDY)
        // If no work/study dilemmas can be found that don't require employment (e.g. all available require work)
        // it shouldn't pick one requiring employment.
        if (dilemma != null && dilemma.category == RoutineCategory.WORK_STUDY) {
            assertFalse(dilemma.requiresEmployment)
        }

        val employed = TestFixtures.character(
            age = 25,
            career = CareerState(currentJob = TestFixtures.job(id = "accountant"))
        )
        val workDilemma = engine.findEligibleDilemma(employed, preferredCategory = RoutineCategory.WORK_STUDY)
        assertNotNull(workDilemma)
        assertEquals(RoutineCategory.WORK_STUDY, workDilemma!!.category)
    }

    @Test
    fun interpolatePrompt_replacesCountryFlavorPlaceholders() {
        val prompt = "You ride the {transportMode} and send funds via {moneyApp}."

        val keInterpolated = engine.interpolatePrompt(prompt, "KE")
        assertTrue(keInterpolated.contains("matatu") || keInterpolated.contains("boda"))
        assertTrue(keInterpolated.contains("M-Pesa"))

        val usInterpolated = engine.interpolatePrompt(prompt, "US")
        assertTrue(usInterpolated.contains("bus") || usInterpolated.contains("subway"))
        assertTrue(usInterpolated.contains("Venmo"))
    }

    @Test
    fun resolveChoice_deterministicConsequencesApplied() {
        val character = TestFixtures.character(
            stats = Stats(health = 70, happiness = 60, karma = 50, money = 1_000)
        )
        val seatDilemma = engine.getAllDilemmas().first { it.id == "commute_rush_hour_seat" }
        val offerChoice = seatDilemma.choices.first { it.id == "seat_offer" }

        val result = engine.resolveChoice(character, seatDilemma, offerChoice)

        assertTrue(result.success)
        assertEquals("dilemma_commute_seat_offer_outcome", result.outcomeMessageResKey)
        assertEquals(66, result.character.stats.health) // 70 - 4
        assertEquals(63, result.character.stats.happiness) // 60 + 3
        assertEquals(55, result.character.stats.karma) // 50 + 5
    }

    @Test
    fun resolveChoice_gambleProbabilisticOutcome() {
        val character = TestFixtures.character(
            stats = Stats(health = 80, happiness = 50, smarts = 60, money = 1_000)
        )
        val shortcutDilemma = engine.getAllDilemmas().first { it.id == "commute_traffic_shortcut" }
        val shortcutChoice = shortcutDilemma.choices.first { it.id == "shortcut_take" }

        // Test forced success (Random that returns 0.0f)
        val successRandom = object : Random() {
            override fun nextBits(bitCount: Int): Int = 0
            override fun nextFloat(): Float = 0.0f
        }
        val successResult = engine.resolveChoice(character, shortcutDilemma, shortcutChoice, successRandom)
        assertTrue(successResult.success)
        assertEquals("dilemma_commute_shortcut_take_success", successResult.outcomeMessageResKey)
        assertEquals(65, successResult.character.stats.smarts) // 60 + 5
        assertEquals(54, successResult.character.stats.happiness) // 50 + 4

        // Test forced failure (Random that returns 1.0f)
        val failureRandom = object : Random() {
            override fun nextBits(bitCount: Int): Int = 0
            override fun nextFloat(): Float = 1.0f
        }
        val failureResult = engine.resolveChoice(character, shortcutDilemma, shortcutChoice, failureRandom)
        assertFalse(failureResult.success)
        assertEquals("dilemma_commute_shortcut_take_fail", failureResult.outcomeMessageResKey)
        assertEquals(74, failureResult.character.stats.health) // 80 - 6
        assertEquals(47, failureResult.character.stats.happiness) // 50 - 3
    }

    @Test
    fun resolveChoice_familyDinnerAffectsFamilyRelationships() {
        val character = TestFixtures.character(
            stats = Stats(karma = 50),
            family = listOf(
                TestFixtures.person(id = "mom", relation = RelationType.MOTHER, relationshipLevel = 60),
                TestFixtures.person(id = "bro", relation = RelationType.SIBLING, relationshipLevel = 50)
            )
        )
        val portionDilemma = engine.getAllDilemmas().first { it.id == "family_last_portion" }
        val yieldChoice = portionDilemma.choices.first { it.id == "portion_yield" }

        val result = engine.resolveChoice(character, portionDilemma, yieldChoice)

        assertTrue(result.success)
        assertEquals(68, result.character.family.first { it.id == "mom" }.relationshipLevel) // 60 + 8
        assertEquals(58, result.character.family.first { it.id == "bro" }.relationshipLevel) // 50 + 8
        assertEquals(54, result.character.stats.karma) // 50 + 4
    }

    @Test
    fun resolveChoice_workDilemmaAffectsWorkplaceRoster() {
        val character = TestFixtures.character(
            career = CareerState(
                currentJob = TestFixtures.job(id = "engineer"),
                bossRelationship = 50,
                workplaceRoster = listOf(
                    WorkplacePerson(id = "colleague1", name = "Sam", role = "Peer", relationshipLevel = 45)
                )
            )
        )
        val coverDilemma = engine.getAllDilemmas().first { it.id == "work_cover_coworker" }
        val acceptChoice = coverDilemma.choices.first { it.id == "cover_accept" }

        val result = engine.resolveChoice(character, coverDilemma, acceptChoice)

        assertTrue(result.success)
        assertEquals(55, result.character.career.workplaceRoster.first().relationshipLevel) // 45 + 10
        assertEquals(60, result.character.career.bossRelationship) // 50 + 10
    }

    @Test
    fun resolveChoice_heatAppliedForRiskyChoices() {
        val character = TestFixtures.character(
            criminalRecord = CriminalRecord(heatLevel = 10)
        )
        val leakDilemma = engine.getAllDilemmas().first { it.id == "work_leak_memo" }
        val shareChoice = leakDilemma.choices.first { it.id == "leak_share" }

        val result = engine.resolveChoice(character, leakDilemma, shareChoice)

        assertTrue(result.success)
        assertEquals(18, result.character.criminalRecord.heatLevel) // 10 + 8
    }
}
