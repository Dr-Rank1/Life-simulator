package com.maisha.game.domain

import com.maisha.game.data.model.CareerState
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.ConfrontationChoice
import com.maisha.game.data.model.Job
import com.maisha.game.data.model.Person
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.SchoolStage
import com.maisha.game.data.model.SecretConfrontation
import com.maisha.game.data.model.SecretIdentityType
import com.maisha.game.data.model.Stats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DoubleLifeEngineTest {

    private val engine = DoubleLifeEngine()

    private fun testCharacter(
        age: Int = 26,
        money: Int = 20_000,
        smarts: Int = 85,
        karma: Int = 70,
        hasSpouse: Boolean = true,
        hasJob: Boolean = true
    ): Character {
        val family = if (hasSpouse) {
            listOf(
                Person(
                    id = "spouse_1",
                    name = "Zawadi",
                    relation = RelationType.SPOUSE,
                    age = 25,
                    relationshipLevel = 80,
                    isMarried = true
                )
            )
        } else {
            emptyList()
        }

        val career = if (hasJob) {
            CareerState(
                currentJob = Job(
                    id = "analyst_1",
                    title = "Data Analyst",
                    minEducation = SchoolStage.UNIVERSITY,
                    baseSalary = 50_000
                )
            )
        } else {
            CareerState()
        }

        return TestFixtures.character(
            name = "Amani Test",
            countryCode = "US",
            age = age,
            stats = Stats(
                health = 80,
                happiness = 75,
                smarts = smarts,
                looks = 70,
                money = money,
                karma = karma
            ),
            family = family,
            career = career
        )
    }

    @Test
    fun canAdoptSecret_validatesAgeAndContextRequirements() {
        val minor = testCharacter(age = 15)
        val (canSyndicateMinor, _) = engine.canAdoptSecret(minor, SecretIdentityType.UNDERCOVER_SYNDICATE)
        assertFalse("Syndicate requires age 16+", canSyndicateMinor)

        val adultNoPartner = testCharacter(age = 22, hasSpouse = false)
        val (canRomance, reason) = engine.canAdoptSecret(adultNoPartner, SecretIdentityType.SECRET_ROMANCE)
        assertFalse("Secret romance requires existing partner", canRomance)
        assertNotNull(reason)

        val adultWithPartner = testCharacter(age = 22, hasSpouse = true)
        val (canRomanceValid, _) = engine.canAdoptSecret(adultWithPartner, SecretIdentityType.SECRET_ROMANCE)
        assertTrue("Eligible with partner", canRomanceValid)
    }

    @Test
    fun adoptSecret_initializesIdentityWithScaledPayoff() {
        val character = testCharacter()
        val adopted = engine.adoptSecret(
            character = character,
            type = SecretIdentityType.UNDERCOVER_SYNDICATE,
            codename = "The Night Viper",
            coverStory = "Night Shift Security Auditing"
        )

        val secret = adopted.doubleLife.activeSecret
        assertNotNull(secret)
        assertEquals(SecretIdentityType.UNDERCOVER_SYNDICATE, secret?.type)
        assertEquals("The Night Viper", secret?.codename)
        assertEquals("Night Shift Security Auditing", secret?.coverStory)
        assertTrue((secret?.annualPayoff ?: 0) > 4_000)
        assertFalse(secret?.actionDoneThisYear ?: true)
    }

    @Test
    fun plantAlibi_reducesSuspicionAndIncreasesAlibiStrength() {
        val character = testCharacter(money = 10_000)
        val adopted = engine.adoptSecret(character, SecretIdentityType.SHADOW_OPERATOR, "Specter")
        val result = engine.plantAlibi(adopted)

        assertTrue(result is DoubleLifeActionResult.Success)
        val updated = (result as DoubleLifeActionResult.Success).character
        val secret = updated.doubleLife.activeSecret

        assertNotNull(secret)
        assertTrue((secret?.alibiStrength ?: 0) >= 100)
        assertEquals(0, secret?.suspicionLevel)
        assertTrue(secret?.actionDoneThisYear == true)
        assertEquals(1, updated.doubleLife.alibisPlantedTotal)
    }

    @Test
    fun scrubTracks_reducesExposureRisk() {
        val character = testCharacter()
        val adopted = engine.adoptSecret(character, SecretIdentityType.BLACKMAIL_VAULT, "Archivist")
        val result = engine.scrubTracks(adopted)

        assertTrue(result is DoubleLifeActionResult.Success)
        val updated = (result as DoubleLifeActionResult.Success).character
        val secret = updated.doubleLife.activeSecret

        assertEquals(0, secret?.exposureRisk)
        assertTrue(secret?.actionDoneThisYear == true)
    }

    @Test
    fun exploitSecret_yieldsImmediateWindfallWithElevatedScrutiny() {
        val character = testCharacter(money = 5_000)
        val adopted = engine.adoptSecret(character, SecretIdentityType.UNDERCOVER_SYNDICATE, "Viper")
        val result = engine.exploitSecret(adopted)

        assertTrue(result is DoubleLifeActionResult.Success)
        val updated = (result as DoubleLifeActionResult.Success).character
        val secret = updated.doubleLife.activeSecret

        assertTrue("Money should increase", updated.stats.money > 5_000)
        assertTrue("Suspicion should rise", (secret?.suspicionLevel ?: 0) > 20)
        assertTrue("Exposure risk should rise", (secret?.exposureRisk ?: 0) > 15)
    }

    @Test
    fun comeClean_dissolvesIdentitySafelyAndArchivesHistory() {
        val character = testCharacter(hasSpouse = true)
        val adopted = engine.adoptSecret(character, SecretIdentityType.SECRET_ROMANCE, "The Affair")
        val clean = engine.comeClean(adopted)

        assertNull("Active secret should be cleared", clean.doubleLife.activeSecret)
        assertEquals(1, clean.doubleLife.exposedHistory.size)
        assertEquals("The Affair", clean.doubleLife.exposedHistory.first().codename)
        val partner = clean.family.first()
        assertTrue("Partner relationship should drop", partner.relationshipLevel < 80)
    }

    @Test
    fun ageUpDoubleLife_collectsPayoffAndDecaysAlibi() {
        val character = testCharacter(money = 1_000)
        val adopted = engine.adoptSecret(character, SecretIdentityType.HIDDEN_CREATOR, "GhostWriter")
        val initialPayoff = adopted.doubleLife.activeSecret?.annualPayoff ?: 0

        val (aged, confrontation) = engine.ageUpDoubleLife(adopted)
        assertEquals(1_000 + initialPayoff, aged.stats.money)
        assertEquals(1, aged.doubleLife.activeSecret?.yearsActive)
        assertTrue("Alibi should decay", (aged.doubleLife.activeSecret?.alibiStrength ?: 100) < 80)
        assertNull("No confrontation on low initial risk", confrontation)
    }

    @Test
    fun resolveConfrontation_doubleDownSuccess_restoresCover() {
        val character = testCharacter(smarts = 98, karma = 90)
        val adopted = engine.adoptSecret(character, SecretIdentityType.SHADOW_OPERATOR, "Specter")
        val activeSecret = adopted.doubleLife.activeSecret!!.copy(
            exposureRisk = 90,
            pendingConfrontation = SecretConfrontation(
                triggerReason = "Suspicious network traffic logged",
                confrontedBy = "Internal Affairs",
                severity = 40,
                silenceBribeCost = 10_000
            )
        )
        val withConfrontation = adopted.copy(doubleLife = adopted.doubleLife.copy(activeSecret = activeSecret))

        val resolution = engine.resolveConfrontation(withConfrontation, ConfrontationChoice.DOUBLE_DOWN)
        assertTrue(resolution.success)
        assertNotNull(resolution.character.doubleLife.activeSecret)
        assertNull(resolution.character.doubleLife.activeSecret?.pendingConfrontation)
        assertEquals(1, resolution.character.doubleLife.successfulBluffs)
    }

    @Test
    fun resolveConfrontation_bribeSilence_drainsCashAndClearsConfrontation() {
        val character = testCharacter(money = 50_000)
        val adopted = engine.adoptSecret(character, SecretIdentityType.UNDERCOVER_SYNDICATE, "Viper")
        val activeSecret = adopted.doubleLife.activeSecret!!.copy(
            pendingConfrontation = SecretConfrontation(
                triggerReason = "Informant flagged warehouse address",
                confrontedBy = "Rival Crew Leader",
                severity = 60,
                silenceBribeCost = 15_000
            )
        )
        val withConfrontation = adopted.copy(doubleLife = adopted.doubleLife.copy(activeSecret = activeSecret))

        val resolution = engine.resolveConfrontation(withConfrontation, ConfrontationChoice.BRIBE_SILENCE)
        assertTrue(resolution.success)
        assertEquals(35_000, resolution.character.stats.money)
        assertNull(resolution.character.doubleLife.activeSecret?.pendingConfrontation)
    }

    @Test
    fun resolveConfrontation_cutTiesVanish_burnsIdentityWithoutArrest() {
        val character = testCharacter(money = 40_000)
        val adopted = engine.adoptSecret(character, SecretIdentityType.UNDERCOVER_SYNDICATE, "Viper")
        val activeSecret = adopted.doubleLife.activeSecret!!.copy(
            pendingConfrontation = SecretConfrontation(
                triggerReason = "Federal raid impending",
                confrontedBy = "Taskforce",
                severity = 90,
                silenceBribeCost = 100_000
            )
        )
        val withConfrontation = adopted.copy(doubleLife = adopted.doubleLife.copy(activeSecret = activeSecret))

        val resolution = engine.resolveConfrontation(withConfrontation, ConfrontationChoice.CUT_TIES_VANISH)
        assertTrue(resolution.success)
        assertNull(resolution.character.doubleLife.activeSecret)
        assertFalse(resolution.character.criminalRecord.currentlyIncarcerated)
        assertEquals(1, resolution.character.doubleLife.exposedHistory.size)
    }
}
