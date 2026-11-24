package com.maisha.game.domain

import com.maisha.game.data.model.CareerState
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.CrimeType
import com.maisha.game.data.model.EducationState
import com.maisha.game.data.model.Job
import com.maisha.game.data.model.Person
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.Stats
import com.maisha.game.data.model.StudyEffort
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MiniChallengeEngineTest {

    private val engine = MiniChallengeEngine()

    private fun testCharacter(
        smarts: Int = 75,
        health: Int = 80,
        happiness: Int = 70,
        money: Int = 1_000,
        plannedStudyEffort: StudyEffort = StudyEffort.HARD,
        examStress: Int = 40,
        performance: Float = 85f,
        bossRel: Int = 60
    ): Character {
        val job = Job(
            id = "test_dev",
            title = "Software Engineer",
            minEducation = com.maisha.game.data.model.SchoolStage.UNIVERSITY,
            baseSalary = 50_000
        )
        return TestFixtures.character(
            name = "Amani Test",
            countryCode = "KE",
            age = 25,
            stats = Stats(
                health = health,
                happiness = happiness,
                smarts = smarts,
                looks = 70,
                money = money,
                karma = 60
            ),
            education = EducationState(
                plannedStudyEffort = plannedStudyEffort,
                examStress = examStress,
                gpa = 3.8f
            ),
            career = CareerState(
                currentJob = job,
                performance = performance,
                bossRelationship = bossRel
            ),
            family = listOf(
                Person(
                    id = "partner_1",
                    name = "Zawadi",
                    relation = RelationType.SPOUSE,
                    age = 24,
                    relationshipLevel = 75,
                    crazy = 30
                )
            )
        )
    }

    @Test
    fun evaluateExamFocus_analytical_reducesStressAndBoostsScore() {
        val character = testCharacter(smarts = 80, examStress = 50)
        val result = engine.evaluateExamFocus(
            character = character,
            tactic = ExamFocusTactic.ANALYTICAL_PRECISION,
            rapidResponseBonus = true
        )

        assertTrue(result.scoreDelta > 10f)
        assertEquals(-15, result.stressDelta)
        assertTrue(result.panicReduced)
        assertTrue(result.outcomeMessage.contains("Methodical"))
    }

    @Test
    fun evaluateExamFocus_instinctive_awardsSpeedBoost() {
        val character = testCharacter(smarts = 90)
        var awarded = false
        repeat(15) {
            val result = engine.evaluateExamFocus(
                character = character,
                tactic = ExamFocusTactic.INSTINCTIVE_SPEED,
                rapidResponseBonus = true
            )
            if (result.panicReduced) {
                assertTrue(result.scoreDelta >= 15f)
                awarded = true
                return
            }
        }
        assertTrue("Speed boost should be awarded on successful instinctive focus", awarded)
    }

    @Test
    fun startCrimePushLuck_initializesSessionWithAppropriateLootAndBustChance() {
        val character = testCharacter(smarts = 70)
        val session = engine.startCrimePushLuck(
            character = character,
            crimeType = CrimeType.PICKPOCKET
        )

        assertEquals(1, session.stage)
        assertEquals(3, session.maxStages)
        assertTrue(session.currentLoot > 0)
        assertTrue(session.nextStageLoot > session.currentLoot)
        assertTrue(session.currentBustChance in 0.10f..0.50f)
    }

    @Test
    fun resolvePushLuckStep_cashOut_safelyBanksLoot() {
        val character = testCharacter(money = 1_000)
        val session = engine.startCrimePushLuck(character, CrimeType.SHOPLIFT)

        val result = engine.resolvePushLuckStep(
            character = character,
            session = session,
            action = PushLuckAction.CASH_OUT
        )

        assertTrue(result is PushLuckStepResult.CashedOut)
        val cashed = result as PushLuckStepResult.CashedOut
        assertEquals(1_000 + session.currentLoot, cashed.character.stats.money)
        assertTrue(cashed.message.contains("Clean getaway"))
    }

    @Test
    fun evaluateNegotiation_salaryRaise_valueMetrics_scalesWithPerformance() {
        val topPerformer = testCharacter(performance = 92f, smarts = 85)
        val challenge = NegotiationChallenge(
            type = NegotiationType.SALARY_RAISE,
            party = NegotiationParty(
                name = "Director Kimani",
                roleTitle = "Supervisor",
                relationshipLevel = 60,
                resistanceLevel = 50
            ),
            subjectLabel = "Annual Salary Review",
            baselineRewardLabel = "+8%"
        )

        val outcome = engine.evaluateNegotiation(
            character = topPerformer,
            challenge = challenge,
            tactic = NegotiationTactics.VALUE_METRICS
        )

        assertTrue(outcome.success)
        assertTrue(outcome.bonusReward > 0)
        assertTrue(outcome.character.career.currentJob!!.baseSalary > 50_000)
    }

    @Test
    fun evaluateNegotiation_prenup_charismaEmpathy_calmsPartner() {
        val character = testCharacter()
        val challenge = NegotiationChallenge(
            type = NegotiationType.PRENUP,
            party = NegotiationParty(
                name = "Zawadi",
                roleTitle = "Fiancée",
                relationshipLevel = 80,
                resistanceLevel = 65 // moderate-high crazy
            ),
            subjectLabel = "Prenuptial Agreement",
            baselineRewardLabel = "Asset Protection"
        )

        val outcome = engine.evaluateNegotiation(
            character = character,
            challenge = challenge,
            tactic = NegotiationTactics.CHARISMA_EMPATHY
        )

        assertTrue(outcome.success)
        assertTrue(outcome.message.contains("reassured"))
    }

    @Test
    fun evaluateNegotiation_confrontBully_assertive_leveragesHealth() {
        val strongStudent = testCharacter(health = 95)
        val challenge = NegotiationChallenge(
            type = NegotiationType.CONFRONT_BULLY,
            party = NegotiationParty(
                name = "Marcus",
                roleTitle = "School Bully",
                relationshipLevel = 10,
                resistanceLevel = 60
            ),
            subjectLabel = "Hallway Showdown",
            baselineRewardLabel = "Stop Bullying"
        )

        val outcome = engine.evaluateNegotiation(
            character = strongStudent,
            challenge = challenge,
            tactic = NegotiationTactics.ASSERTIVE_LEVERAGE
        )

        assertTrue(outcome.success)
        assertTrue(outcome.character.hasConsequenceFlag("stoodUpToBully"))
    }
}
