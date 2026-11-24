package com.maisha.game.domain

import com.maisha.game.data.model.CareerState
import com.maisha.game.data.model.LifestyleState
import com.maisha.game.data.model.Stats
import com.maisha.game.data.model.WorkEffort
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthEngineTest {

    private val engine = HealthEngine()

    @Test
    fun illnessRoll_lowHealthProducesMoreIllnessesThanHighHealth() {
        val healthy = TestFixtures.character(stats = Stats(health = 95))
        val fragile = TestFixtures.character(stats = Stats(health = 15))
        var healthyIllnesses = 0
        var fragileIllnesses = 0
        repeat(500) {
            if (engine.rollForIllness(healthy) != null) healthyIllnesses++
            if (engine.rollForIllness(fragile) != null) fragileIllnesses++
        }
        assertTrue("Low health should roll illness more often", fragileIllnesses > healthyIllnesses)
    }

    @Test
    fun addCondition_prependsEventLogEntry() {
        val character = TestFixtures.character()
        val condition = com.maisha.game.data.model.HealthCondition(
            id = "c1",
            name = "Malaria",
            severity = 2
        )
        val updated = engine.addCondition(character, condition)
        assertTrue(updated.eventLog.first().contains("Malaria"))
        assertEquals(1, updated.activeConditions.size)
    }

    @Test
    fun processHealthProgression_appliesGymBonusAndStressPenalty() {
        val baseStats = Stats(
            health = 80,
            happiness = 80,
            looks = 70,
            money = 500_000
        )
        val baseline = TestFixtures.character(stats = baseStats)
        val withGym = baseline.copy(
            lifestyle = LifestyleState(hasGymMembership = true)
        )

        val baselineAfter = engine.processHealthProgression(baseline)
        val gymAfter = engine.processHealthProgression(withGym)

        assertTrue(gymAfter.stats.looks >= baselineAfter.stats.looks)

        val employed = TestFixtures.character(
            stats = baseStats,
            career = CareerState(
                currentJob = TestFixtures.job(id = "engineer", title = "Engineer", level = 3)
            )
        )
        val afterNormal = engine.applyWorkEffortStress(employed, WorkEffort.NORMAL)
        val afterGrind = engine.applyWorkEffortStress(employed, WorkEffort.GRIND)

        assertTrue(afterGrind.stats.health < afterNormal.stats.health)
        assertTrue(afterGrind.stats.happiness < afterNormal.stats.happiness)
    }

    @Test
    fun infancy_protectedFromIllnessAndHealthcareCosts() {
        val infant = TestFixtures.character(age = 1, stats = Stats(health = 10, money = 0))
        org.junit.Assert.assertNull("Infants under age 3 should not roll random illnesses", engine.rollForIllness(infant))

        val condition = com.maisha.game.data.model.HealthCondition(id = "c0", name = "Pediatric Cough", severity = 2)
        val withCond = engine.addCondition(infant, condition)

        val cost = engine.estimateTreatmentCost(withCond, condition.severity, com.maisha.game.data.model.CareTier.PUBLIC_CLINIC)
        assertEquals(0, cost)

        val privateCost = engine.estimateTreatmentCost(withCond, condition.severity, com.maisha.game.data.model.CareTier.PRIVATE_HOSPITAL)
        assertEquals(0, privateCost)

        val odds = engine.calculateRecoveryOdds(withCond, condition, com.maisha.game.data.model.CareTier.PUBLIC_CLINIC)
        assertEquals(95, odds)
    }

    @Test
    fun visitDoctor_alternativeMedicineAwardsKarmaAndHappiness() {
        val character = TestFixtures.character(
            age = 25,
            stats = Stats(money = 100_000, happiness = 50, karma = 50, health = 70)
        )
        val condition = com.maisha.game.data.model.HealthCondition(id = "c1", name = "Minor Flu", severity = 1)
        val withCond = engine.addCondition(character, condition)

        val result = engine.visitDoctor(withCond, "c1", com.maisha.game.data.model.CareTier.ALTERNATIVE_MEDICINE)
        val updated = when (result) {
            is DoctorResult.Treated -> result.character
            is DoctorResult.Failed -> result.character
        }
        assertTrue("Alternative medicine should increase happiness", updated.stats.happiness > 50)
        assertTrue("Alternative medicine should increase karma", updated.stats.karma > 50)
    }

    @Test
    fun visitDoctor_mentalWellnessRelievesStressForPtsd() {
        val character = TestFixtures.character(
            age = 30,
            stats = Stats(money = 100_000, happiness = 35, health = 75)
        )
        val condition = com.maisha.game.data.model.HealthCondition(id = "c2", name = "PTSD", severity = 2)
        val withCond = engine.addCondition(character, condition)

        val odds = engine.calculateRecoveryOdds(withCond, condition, com.maisha.game.data.model.CareTier.MENTAL_WELLNESS)
        assertTrue("Mental wellness therapy should have high recovery odds for PTSD", odds >= 75)

        val result = engine.visitDoctor(withCond, "c2", com.maisha.game.data.model.CareTier.MENTAL_WELLNESS)
        val updated = when (result) {
            is DoctorResult.Treated -> result.character
            is DoctorResult.Failed -> result.character
        }
        assertTrue("Therapy session should lift happiness", updated.stats.happiness > 35)
    }

    @Test
    fun calculateRecoveryOdds_returnsValidPercentages() {
        val character = TestFixtures.character(age = 22, stats = Stats(health = 80, smarts = 70, money = 10_000))
        val condition = com.maisha.game.data.model.HealthCondition(id = "c3", name = "Infection", severity = 2)

        com.maisha.game.data.model.CareTier.entries.forEach { tier ->
            val odds = engine.calculateRecoveryOdds(character, condition, tier)
            assertTrue("Odds for $tier should be in range 1..99", odds in 1..99)
        }
    }
}
