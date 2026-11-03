package com.maisha.game.domain

import com.maisha.game.data.model.CareerState
import com.maisha.game.data.model.CrimeTarget
import com.maisha.game.data.model.CriminalRecord
import com.maisha.game.data.model.EducationState
import com.maisha.game.data.model.Job
import com.maisha.game.data.model.Person
import com.maisha.game.data.model.PrisonActivity
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.RelationshipStage
import com.maisha.game.data.model.SchoolStage
import com.maisha.game.data.model.Stats
import com.maisha.game.data.model.WeddingVenueTier
import com.maisha.game.data.model.WorkplaceAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Part3SystemsTest {

    private val financeEngine = FinanceEngine()
    private val healthEngine = HealthEngine()
    private val relocationEngine = RelocationEngine()
    private val relationshipEngine = RelationshipEngine(financeEngine)
    private val careerEngine = CareerEngine(healthEngine, relocationEngine)
    private val crimeEngine = CrimeEngine()

    @Test
    fun testMatchmakerCandidatesGeneration() {
        val character = TestFixtures.character(
            age = 28,
            stats = Stats(money = 10_000)
        )
        val (updatedChar, candidates) = relationshipEngine.findMatchmakerCandidates(
            character = character,
            minAge = 25,
            maxAge = 32,
            wealthTier = 2
        )

        assertTrue(updatedChar.stats.money < 10_000)
        assertEquals(4, candidates.size)
        candidates.forEach { candidate ->
            assertTrue(candidate.age in 25..32)
            assertTrue(candidate.crazy in 0..100)
            assertTrue(candidate.occupation.isNotBlank())
            assertEquals(RelationshipStage.CASUAL, candidate.stage)
        }
    }

    @Test
    fun testMakeRelationshipExclusive() {
        val partner = Person(
            id = "partner_1",
            name = "Taylor",
            relation = RelationType.SPOUSE,
            age = 26,
            relationshipLevel = 75,
            stage = RelationshipStage.CASUAL,
            isMarried = false
        )
        val character = TestFixtures.character(age = 26).copy(
            family = listOf(partner)
        )

        val (updated, accepted) = relationshipEngine.makeRelationshipExclusive(character, "partner_1")
        assertTrue(accepted)
        val updatedPartner = updated.family.find { it.id == "partner_1" }
        assertNotNull(updatedPartner)
        assertEquals(RelationshipStage.EXCLUSIVE, updatedPartner?.stage)
    }

    @Test
    fun testProposeMarriageWithVenueTiers() {
        val partner = Person(
            id = "partner_1",
            name = "Jamie",
            relation = RelationType.SPOUSE,
            age = 27,
            relationshipLevel = 90,
            crazy = 10,
            stage = RelationshipStage.EXCLUSIVE,
            isMarried = false
        )
        val character = TestFixtures.character(
            age = 27,
            stats = Stats(money = 500_000)
        ).copy(
            family = listOf(partner)
        )

        val (updatedChar, result) = relationshipEngine.proposeMarriage(
            character = character,
            personId = "partner_1",
            signPrenup = true,
            venueTier = WeddingVenueTier.LUXURY_DESTINATION
        )

        if (result is ProposalResult.Accepted) {
            val spouse = result.character.family.find { it.id == "partner_1" }
            assertNotNull(spouse)
            assertTrue(spouse?.isMarried == true)
            assertEquals(RelationshipStage.MARRIED, spouse?.stage)
            assertTrue(result.character.stats.money < 500_000)
        }
    }

    @Test
    fun testWorkplaceRosterGenerationAndSchmooze() {
        val activeJob = Job(
            id = "junior_dev",
            title = "Junior Software Developer",
            minEducation = SchoolStage.UNIVERSITY,
            baseSalary = 75_000
        )
        val characterWithJob = TestFixtures.character(age = 24).copy(
            career = CareerState(
                currentJob = activeJob,
                yearsAtCurrentJob = 1
            )
        )

        val withRoster = careerEngine.ensureWorkplaceRoster(characterWithJob)
        val roster = withRoster.career.workplaceRoster
        assertTrue(roster.isNotEmpty())

        val boss = roster.find { it.isSupervisor }
        assertNotNull(boss)

        val (_, result) = careerEngine.handleWorkplaceAction(
            character = withRoster,
            personId = boss!!.id,
            action = WorkplaceAction.SCHMOOZE
        )

        assertTrue(result is WorkplaceActionResult.Success || result is WorkplaceActionResult.Denied)
    }

    @Test
    fun testTargetedCrimeExecutionAndNotoriety() {
        val character = TestFixtures.character(
            age = 22,
            stats = Stats(money = 1_000)
        )

        val result = crimeEngine.attemptTargetedCrime(
            character = character,
            target = CrimeTarget.GTA_SEDAN
        )

        when (result) {
            is CrimeResult.Success -> {
                assertTrue(result.moneyGained > 0)
                assertTrue(result.character.criminalRecord.notoriety > 0)
            }
            is CrimeResult.Caught -> {
                assertTrue(result.character.criminalRecord.awaitingTrial)
            }
            else -> {}
        }
    }

    @Test
    fun testPrisonActivitiesGangAndParole() {
        val prisoner = TestFixtures.character(age = 30).copy(
            criminalRecord = CriminalRecord(
                currentlyIncarcerated = true,
                yearsRemaining = 5,
                yearsServed = 2,
                totalSentenceYears = 7,
                notoriety = 20
            )
        )

        val gangResult = crimeEngine.performPrisonActivity(
            character = prisoner,
            activity = PrisonActivity.PRISON_YARD_GANG
        )
        assertTrue(gangResult is PrisonActivityResult.Success)
        val updatedGangChar = (gangResult as PrisonActivityResult.Success).character
        assertTrue(updatedGangChar.criminalRecord.notoriety >= 20)

        val paroleResult = crimeEngine.performPrisonActivity(
            character = prisoner,
            activity = PrisonActivity.PAROLE_HEARING_APPEAL
        )
        assertTrue(paroleResult is PrisonActivityResult.Success)
    }

    @Test
    fun testLifeArchetypeRibbons() {
        val loadedChar = TestFixtures.character()
        val ribbonLoaded = LifeArchetypeEngine.evaluateRibbon(loadedChar, 2_500_000)
        assertEquals(LifeRibbon.LOADED, ribbonLoaded)

        val academicChar = TestFixtures.character(
            education = EducationState(stage = SchoolStage.GRADUATED),
            stats = Stats(smarts = 85)
        )
        val ribbonAcademic = LifeArchetypeEngine.evaluateRibbon(academicChar, 50_000)
        assertEquals(LifeRibbon.ACADEMIC, ribbonAcademic)

        val criminalChar = TestFixtures.character(
            criminalRecord = CriminalRecord(notoriety = 65)
        )
        val ribbonCunning = LifeArchetypeEngine.evaluateRibbon(criminalChar, 50_000)
        assertEquals(LifeRibbon.CUNNING, ribbonCunning)

        val saintChar = TestFixtures.character(
            stats = Stats(karma = 90, happiness = 75)
        )
        val ribbonSaint = LifeArchetypeEngine.evaluateRibbon(saintChar, 50_000)
        assertEquals(LifeRibbon.SAINT, ribbonSaint)
    }
}
