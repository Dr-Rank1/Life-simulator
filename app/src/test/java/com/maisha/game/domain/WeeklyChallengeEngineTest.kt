package com.maisha.game.domain

import com.maisha.game.data.model.Asset
import com.maisha.game.data.model.AssetType
import com.maisha.game.data.model.CareerState
import com.maisha.game.data.model.CareerTrack
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.CriminalRecord
import com.maisha.game.data.model.EducationState
import com.maisha.game.data.model.Gender
import com.maisha.game.data.model.Person
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.SchoolStage
import com.maisha.game.data.model.Stats
import com.maisha.game.data.model.UniversityMajor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WeeklyChallengeEngineTest {

    private lateinit var engine: WeeklyChallengeEngine

    @Before
    fun setup() {
        engine = WeeklyChallengeEngine()
    }

    private fun testCharacter(): Character = TestFixtures.character(
        name = "Test Player",
        age = 25,
        gender = Gender.FEMALE,
        countryCode = "KE",
        alive = true,
        stats = Stats(happiness = 50, health = 85, smarts = 70, looks = 60, money = 10_000, karma = 50)
    )

    @Test
    fun currentChallenge_deterministicRotation() {
        val week0 = 0L
        val week1 = WeeklyChallengeEngine.MILLIS_PER_WEEK
        val week2 = WeeklyChallengeEngine.MILLIS_PER_WEEK * 2

        val challenge0 = engine.currentChallenge(week0)
        val challenge1 = engine.currentChallenge(week1)
        val challenge2 = engine.currentChallenge(week2)

        assertNotNull(challenge0)
        assertNotNull(challenge1)
        assertNotNull(challenge2)
        assertEquals(WeeklyChallengeEngine.CHALLENGES[0], challenge0)
        assertEquals(WeeklyChallengeEngine.CHALLENGES[1], challenge1)
        assertEquals(WeeklyChallengeEngine.CHALLENGES[2], challenge2)
    }

    @Test
    fun timeRemainingMillis_accurateCountdown() {
        val weekStart = 100 * WeeklyChallengeEngine.MILLIS_PER_WEEK
        val threeDaysIn = weekStart + (3L * 24 * 3600 * 1000)
        val remaining = engine.timeRemainingMillis(threeDaysIn)

        val expected = 4L * 24 * 3600 * 1000
        assertEquals(expected, remaining)
    }

    @Test
    fun evaluateProgress_medicalResidency_partialAndComplete() {
        val challenge = WeeklyChallengeEngine.CHALLENGES.first { it.id == "medical_residency" }
        var char = testCharacter().copy(
            stats = Stats(health = 90, happiness = 50, smarts = 85, looks = 50, money = 5000, karma = 50),
            education = EducationState(stage = SchoolStage.PRIMARY)
        )

        var progress = engine.evaluateProgress(char, challenge)
        assertEquals(3, progress.totalCount)
        assertEquals(1, progress.completedCount) // health >= 80 is satisfied
        assertFalse(progress.isAllCompleted)

        // Graduate with medicine degree
        char = char.copy(
            education = char.education.copy(
                stage = SchoolStage.GRADUATED,
                universityMajor = UniversityMajor.MEDICINE
            )
        )
        progress = engine.evaluateProgress(char, challenge)
        assertEquals(2, progress.completedCount)
        assertFalse(progress.isAllCompleted)

        // Reach residency track level 2
        char = char.copy(
            career = CareerState(
                careerTrack = CareerTrack.MEDICAL,
                trackLevel = 2
            )
        )
        progress = engine.evaluateProgress(char, challenge)
        assertEquals(3, progress.completedCount)
        assertTrue(progress.isAllCompleted)
        assertEquals(1.0f, progress.overallFraction, 0.001f)
    }

    @Test
    fun evaluateProgress_homeImprover_requirements() {
        val challenge = WeeklyChallengeEngine.CHALLENGES.first { it.id == "home_improver" }
        var char = testCharacter().copy(
            stats = Stats(money = 120_000, health = 70, happiness = 70, smarts = 70, looks = 70, karma = 50),
            assets = emptyList()
        )

        var progress = engine.evaluateProgress(char, challenge)
        assertEquals(1, progress.completedCount) // Net worth >= 100k satisfied
        assertFalse(progress.isAllCompleted)

        // Buy house
        val house = Asset(
            id = "house-1",
            type = AssetType.HOUSE,
            name = "Suburban Home",
            purchasePrice = 80_000,
            currentValue = 80_000,
            monthlyUpkeep = 100,
            renovationLevel = 0
        )
        char = char.copy(assets = listOf(house))
        progress = engine.evaluateProgress(char, challenge)
        assertEquals(2, progress.completedCount) // house owned + net worth

        // Renovate
        char = char.copy(assets = listOf(house.copy(renovationLevel = 1)))
        progress = engine.evaluateProgress(char, challenge)
        assertEquals(3, progress.completedCount)
        assertTrue(progress.isAllCompleted)
    }

    @Test
    fun claimReward_success_appliesCashAndStatsAndEventLog() {
        val challenge = WeeklyChallengeEngine.CHALLENGES.first { it.id == "teen_hustler" }
        val char = testCharacter().copy(
            age = 16,
            stats = Stats(money = 6000, happiness = 50, health = 80, smarts = 70, looks = 60, karma = 50),
            career = CareerState(partTimeWorkedThisYear = true),
            education = EducationState(gpa = 3.2f)
        )

        assertTrue(engine.isComplete(char, challenge))

        val result = engine.claimReward(char, challenge, isClaimed = false)
        assertTrue(result is ClaimWeeklyChallengeResult.Success)
        val success = result as ClaimWeeklyChallengeResult.Success

        assertEquals("ribbon_hustler", success.ribbonKey)
        assertTrue(success.scaledCashAwarded > 0)
        assertEquals(char.stats.money + success.scaledCashAwarded, success.character.stats.money)
        assertEquals(char.stats.karma + challenge.reward.karmaBonus, success.character.stats.karma)
        assertEquals(char.stats.happiness + challenge.reward.happinessBonus, success.character.stats.happiness)
        assertTrue(success.character.eventLog.any { it.contains("Weekly Challenge") })
    }

    @Test
    fun claimReward_failsWhenIneligibleOrAlreadyClaimed() {
        val challenge = WeeklyChallengeEngine.CHALLENGES.first { it.id == "teen_hustler" }
        val ineligibleChar = testCharacter().copy(
            stats = Stats(money = 100, happiness = 50, health = 80, smarts = 70, looks = 60, karma = 50),
            career = CareerState(partTimeWorkedThisYear = false),
            education = EducationState(gpa = 1.0f)
        )

        val resultIneligible = engine.claimReward(ineligibleChar, challenge, isClaimed = false)
        assertTrue(resultIneligible is ClaimWeeklyChallengeResult.Ineligible)

        val eligibleChar = testCharacter().copy(
            age = 16,
            stats = Stats(money = 6000, happiness = 50, health = 80, smarts = 70, looks = 60, karma = 50),
            career = CareerState(partTimeWorkedThisYear = true),
            education = EducationState(gpa = 3.2f)
        )
        val resultClaimed = engine.claimReward(eligibleChar, challenge, isClaimed = true)
        assertTrue(resultClaimed is ClaimWeeklyChallengeResult.AlreadyClaimed)
    }

    @Test
    fun formattedTimeRemaining_rendersCorrectUnits() {
        val progressDays = WeeklyChallengeProgress(
            challenge = WeeklyChallengeEngine.CHALLENGES[0],
            requirements = emptyList(),
            completedCount = 0,
            totalCount = 0,
            isAllCompleted = false,
            isClaimed = false,
            timeRemainingMillis = (3L * 24 * 3600 + 5 * 3600) * 1000
        )
        assertEquals("3d 5h", progressDays.formattedTimeRemaining())

        val progressHours = progressDays.copy(timeRemainingMillis = (5L * 3600 + 42 * 60) * 1000)
        assertEquals("5h 42m", progressHours.formattedTimeRemaining())

        val progressMins = progressDays.copy(timeRemainingMillis = 25L * 60 * 1000)
        assertEquals("25m", progressMins.formattedTimeRemaining())
    }
}
