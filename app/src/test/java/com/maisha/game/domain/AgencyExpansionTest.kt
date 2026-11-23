package com.maisha.game.domain

import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.Asset
import com.maisha.game.data.model.AssetType
import com.maisha.game.data.model.ClubRank
import com.maisha.game.data.model.CrimeTarget
import com.maisha.game.data.model.CrimeType
import com.maisha.game.data.model.CriminalRecord
import com.maisha.game.data.model.Job
import com.maisha.game.data.model.SchoolClub
import com.maisha.game.data.model.SchoolPerson
import com.maisha.game.data.model.SchoolRole
import com.maisha.game.data.model.SchoolStage
import com.maisha.game.data.model.WorkEffort
import com.maisha.game.data.model.WorkplaceAction
import com.maisha.game.data.model.WorkplacePerson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AgencyExpansionTest {

    private lateinit var careerEngine: CareerEngine
    private lateinit var educationEngine: EducationEngine
    private lateinit var crimeEngine: CrimeEngine
    private lateinit var financeEngine: FinanceEngine

    @Before
    fun setUp() {
        val healthEngine = HealthEngine()
        val relocationEngine = RelocationEngine()

        careerEngine = CareerEngine(
            healthEngine = healthEngine,
            relocationEngine = relocationEngine
        )
        educationEngine = EducationEngine(relocationEngine = relocationEngine)
        crimeEngine = CrimeEngine()
        financeEngine = FinanceEngine()
    }

    // -------------------------------------------------------------
    // 1. CAREER & WORKPLACE AGENCY
    // -------------------------------------------------------------

    @Test
    fun workplaceAntiFarming_coworkerCannotBeInteractedTwiceInSameYear() {
        val coworker = WorkplacePerson(
            id = "coworker-1",
            name = "Sarah Connor",
            role = "Analyst",
            relationshipLevel = 50,
            interactedThisYear = false
        )
        val character = TestFixtures.character(age = 25).copy(
            career = TestFixtures.character().career.copy(
                currentJob = Job(
                    id = "corporate_analyst",
                    title = "Analyst",
                    minEducation = SchoolStage.SECONDARY,
                    baseSalary = 50_000
                ),
                workplaceRoster = listOf(coworker)
            )
        )

        // First action succeeds and sets interactedThisYear = true
        val (afterFirst, result1) = careerEngine.handleWorkplaceAction(
            character,
            "coworker-1",
            WorkplaceAction.COLLABORATE
        )
        assertTrue(result1 is WorkplaceActionResult.Success)
        val updatedCoworker = afterFirst.career.workplaceRoster.first { it.id == "coworker-1" }
        assertTrue(updatedCoworker.interactedThisYear)

        // Second action within same year is denied
        val (afterSecond, result2) = careerEngine.handleWorkplaceAction(
            afterFirst,
            "coworker-1",
            WorkplaceAction.COLLABORATE
        )
        assertTrue(result2 is WorkplaceActionResult.Denied)
        assertEquals((result2 as WorkplaceActionResult.Denied).message, "You already interacted with Sarah Connor this year.")
    }

    @Test
    fun workplaceAntiFarming_resetsOnNewYearTick() {
        val coworker = WorkplacePerson(
            id = "coworker-1",
            name = "Sarah Connor",
            role = "Analyst",
            relationshipLevel = 50,
            interactedThisYear = true
        )
        val character = TestFixtures.character(age = 25).copy(
            career = TestFixtures.character().career.copy(
                currentJob = Job(
                    id = "corporate_analyst",
                    title = "Analyst",
                    minEducation = SchoolStage.SECONDARY,
                    baseSalary = 50_000
                ),
                workplaceRoster = listOf(coworker)
            )
        )

        // Advance year through workYear
        val aged = careerEngine.workYear(character, WorkEffort.NORMAL)
        val coworkerAged = aged.career.workplaceRoster.first { it.id == "coworker-1" }
        assertFalse(coworkerAged.interactedThisYear)
    }

    @Test
    fun workplaceMentorRequest_requiresCompetenceOrSupervisor() {
        val junior = WorkplacePerson(
            id = "coworker-junior",
            name = "Intern Bob",
            role = "Intern",
            competence = 30,
            isSupervisor = false
        )
        val character = TestFixtures.character(age = 25).copy(
            career = TestFixtures.character().career.copy(
                currentJob = Job("analyst", "Analyst", SchoolStage.SECONDARY, 50_000),
                workplaceRoster = listOf(junior)
            )
        )

        val (_, result) = careerEngine.handleWorkplaceAction(
            character,
            "coworker-junior",
            WorkplaceAction.MENTOR_REQUEST
        )
        assertTrue(result is WorkplaceActionResult.Denied)
        assertTrue((result as WorkplaceActionResult.Denied).message.contains("senior experience"))
    }

    @Test
    fun pipDeliverable_liftsPipAtAgeUp() {
        val character = TestFixtures.character(age = 30).copy(
            career = TestFixtures.character().career.copy(
                currentJob = Job("analyst", "Analyst", SchoolStage.SECONDARY, 50_000),
                onPip = true,
                performance = 40f
            )
        )

        // Work PIP deliverable
        val (withDeliverable, pipResult) = careerEngine.workPipDeliverable(character)
        assertTrue(pipResult is WorkplaceActionResult.Success)
        assertTrue(withDeliverable.career.pipDeliverableCompletedThisYear)
        assertTrue(withDeliverable.career.performance > 40f)

        // When working the year, PIP is cleared because milestone was completed
        val yearEnd = careerEngine.workYear(withDeliverable, WorkEffort.NORMAL)
        assertFalse(yearEnd.career.onPip)
        assertNotNull(yearEnd.career.currentJob)
    }

    @Test
    fun pipFailure_terminatesEmploymentWhenUnderperforming() {
        val character = TestFixtures.character(age = 30).copy(
            career = TestFixtures.character().career.copy(
                currentJob = Job("analyst", "Analyst", SchoolStage.SECONDARY, 50_000),
                onPip = true,
                performance = 30f,
                pipDeliverableCompletedThisYear = false
            )
        )

        // Underperforming on PIP without deliverable -> terminated
        val yearEnd = careerEngine.workYear(character, WorkEffort.COAST)
        assertFalse(yearEnd.career.onPip)
        assertNull(yearEnd.career.currentJob)
        assertTrue(yearEnd.eventLog.any { it.contains("terminated", ignoreCase = true) })
    }

    // -------------------------------------------------------------
    // 2. SCHOOL & CLUB LEADERSHIP AGENCY
    // -------------------------------------------------------------

    @Test
    fun clubLeadership_requiresOfficerOrCaptain() {
        val memberChar = TestFixtures.character(age = 15).copy(
            education = TestFixtures.character().education.copy(
                stage = SchoolStage.SECONDARY,
                schoolClub = SchoolClub.FOOTBALL,
                clubRank = ClubRank.MEMBER
            )
        )

        val strategyResult = educationEngine.leadClubStrategy(memberChar)
        assertTrue(strategyResult is ClubActivityResult.Ineligible)

        val mentorResult = educationEngine.mentorClubJunior(memberChar)
        assertTrue(mentorResult is ClubActivityResult.Ineligible)
    }

    @Test
    fun clubLeadership_officerCanRunStrategyAndMentoring() {
        val peer = SchoolPerson(
            id = "peer-1",
            name = "Kevin",
            role = SchoolRole.CLASSMATE,
            age = 15,
            relationshipLevel = 45
        )
        val officerChar = TestFixtures.character(age = 15).copy(
            education = TestFixtures.character().education.copy(
                stage = SchoolStage.SECONDARY,
                schoolClub = SchoolClub.DEBATE,
                clubRank = ClubRank.OFFICER,
                clubSkill = 50,
                clubPrestige = 40,
                schoolPeople = listOf(peer)
            )
        )

        // Strategy workshop
        val stratResult = educationEngine.leadClubStrategy(officerChar)
        assertTrue(stratResult is ClubActivityResult.Success)
        val afterStrat = (stratResult as ClubActivityResult.Success).character
        assertTrue(afterStrat.education.clubStrategyDoneThisYear)
        assertTrue(afterStrat.education.clubSkill > 50)
        assertTrue(afterStrat.education.clubPrestige > 40)

        // Junior mentoring
        val mentorResult = educationEngine.mentorClubJunior(afterStrat)
        assertTrue(mentorResult is ClubActivityResult.Success)
        val afterMentor = (mentorResult as ClubActivityResult.Success).character
        assertTrue(afterMentor.education.clubMentoringDoneThisYear)
        val updatedPeer = afterMentor.education.schoolPeople.first { it.id == "peer-1" }
        assertTrue(updatedPeer.relationshipLevel > 45)
        assertTrue(updatedPeer.interactedThisYear)
    }

    // -------------------------------------------------------------
    // 3. UNDERWORLD & CRIME HEAT AGENCY
    // -------------------------------------------------------------

    @Test
    fun targetedCrime_enforcesAgeGates() {
        // Age 14: allowed for petty crimes, but not Burglary/GTA (16+) or Heists (18+)
        val youngChar = TestFixtures.character(age = 14)
        val burglaryResult = crimeEngine.attemptTargetedCrime(youngChar, CrimeTarget.BURGLARY_APARTMENT)
        assertTrue(burglaryResult is CrimeResult.Caught)

        val teenChar = TestFixtures.character(age = 16)
        val heistResult = crimeEngine.attemptTargetedCrime(teenChar, CrimeTarget.HEIST_BANK_VAULT)
        assertTrue(heistResult is CrimeResult.Caught)
    }

    @Test
    fun crimeCasing_preparesTargetAndBoostsExecution() {
        val character = TestFixtures.character(age = 22).copy(
            stats = TestFixtures.character().stats.copy(smarts = 90),
            criminalRecord = CriminalRecord(heatLevel = 0)
        )

        val casedChar = crimeEngine.caseTarget(character, CrimeTarget.BURGLARY_APARTMENT)
        assertEquals(CrimeTarget.BURGLARY_APARTMENT, casedChar.criminalRecord.casedTarget)
        assertTrue(casedChar.criminalRecord.heatLevel > 0)
    }

    @Test
    fun crimeHeat_reducesSuccessChanceAndCanBeDissipated() {
        val coolChar = TestFixtures.character(age = 25).copy(
            criminalRecord = CriminalRecord(heatLevel = 80)
        )

        // Lay low reduces heat
        val laidLow = crimeEngine.layLow(coolChar)
        assertTrue(laidLow.criminalRecord.heatActionDoneThisYear)
        assertEquals(45, laidLow.criminalRecord.heatLevel)

        // Bribe informant reduces heat even further
        val richChar = TestFixtures.character(age = 25).copy(
            stats = TestFixtures.character().stats.copy(money = 100_000),
            criminalRecord = CriminalRecord(heatLevel = 90)
        )
        val bribed = crimeEngine.bribeInformant(richChar)
        assertTrue(bribed.criminalRecord.heatActionDoneThisYear)
        assertEquals(30, bribed.criminalRecord.heatLevel)
        assertTrue(bribed.stats.money < 100_000)
    }

    // -------------------------------------------------------------
    // 4. ASSETS & MORTGAGE AGENCY
    // -------------------------------------------------------------

    @Test
    fun prepayMortgagePrincipal_reducesBalanceAndRecalculatesPayment() {
        val mortgagedHouse = Asset(
            id = "house-1",
            type = AssetType.HOUSE,
            name = "Suburban Home",
            purchasePrice = 200_000,
            currentValue = 200_000,
            monthlyUpkeep = 100,
            mortgageBalance = 100_000,
            annualMortgagePayment = 10_000
        )
        val character = TestFixtures.character(age = 35).copy(
            stats = TestFixtures.character().stats.copy(money = 50_000),
            assets = listOf(mortgagedHouse)
        )

        // Prepay 40,000 principal
        val updated = financeEngine.prepayMortgagePrincipal(character, "house-1", 40_000)
        val updatedHouse = updated.assets.first { it.id == "house-1" }

        assertEquals(60_000, updatedHouse.mortgageBalance)
        assertEquals(6_000, updatedHouse.annualMortgagePayment)
        assertEquals(10_000, updated.stats.money)
    }

    @Test
    fun prepayMortgagePrincipal_fullyClearsWhenPaymentCoversBalance() {
        val mortgagedHouse = Asset(
            id = "house-1",
            type = AssetType.HOUSE,
            name = "Suburban Home",
            purchasePrice = 200_000,
            currentValue = 200_000,
            monthlyUpkeep = 100,
            mortgageBalance = 30_000,
            annualMortgagePayment = 3_000
        )
        val character = TestFixtures.character(age = 35).copy(
            stats = TestFixtures.character().stats.copy(money = 50_000),
            assets = listOf(mortgagedHouse)
        )

        // Prepay 35,000 (more than remaining 30,000)
        val updated = financeEngine.prepayMortgagePrincipal(character, "house-1", 35_000)
        val updatedHouse = updated.assets.first { it.id == "house-1" }

        assertEquals(0, updatedHouse.mortgageBalance)
        assertEquals(0, updatedHouse.annualMortgagePayment)
        assertEquals(20_000, updated.stats.money)
    }

    @Test
    fun serviceAsset_boostsConditionAndProtectsFromDegradation() {
        val car = Asset(
            id = "car-1",
            type = AssetType.CAR,
            name = "Family Sedan",
            purchasePrice = 20_000,
            currentValue = 15_000,
            condition = 70,
            monthlyUpkeep = 50
        )
        val character = TestFixtures.character(age = 28).copy(
            stats = TestFixtures.character().stats.copy(money = 10_000),
            assets = listOf(car)
        )

        val serviced = financeEngine.serviceAsset(character, "car-1")
        val servicedCar = serviced.assets.first { it.id == "car-1" }

        assertTrue(servicedCar.servicedThisYear)
        assertEquals(85, servicedCar.condition)
        assertTrue(serviced.stats.money < 10_000)
    }
}
