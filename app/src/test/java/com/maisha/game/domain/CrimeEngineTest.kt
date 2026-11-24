package com.maisha.game.domain

import com.maisha.game.data.model.CrimeTarget
import com.maisha.game.data.model.CrimeType
import com.maisha.game.data.model.CriminalRecord
import com.maisha.game.data.model.EducationState
import com.maisha.game.data.model.LawyerTier
import com.maisha.game.data.model.PrisonActivity
import com.maisha.game.data.model.PrisonGang
import com.maisha.game.data.model.SchoolStage
import com.maisha.game.data.model.Stats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrimeEngineTest {

    private val engine = CrimeEngine()
    private val careerEngine = CareerEngine(HealthEngine(), RelocationEngine())

    @Test
    fun crimeTypes_produceDifferentSentenceLengthsWhenCaught() {
        val pickpocketSentences = mutableListOf<Int>()
        val fraudSentences = mutableListOf<Int>()
        repeat(200) {
            val base = TestFixtures.character(age = 20, stats = Stats(money = 500_000))
            val pickArrested = engine.processArrest(base, CrimeType.PICKPOCKET)
            val fraudArrested = engine.processArrest(base, CrimeType.FRAUD)
            val pickTrial = engine.goToTrial(
                pickArrested,
                LawyerTier.PUBLIC_DEFENDER,
                netWorth = 500_000
            )
            val fraudTrial = engine.goToTrial(
                fraudArrested,
                LawyerTier.PUBLIC_DEFENDER,
                netWorth = 500_000
            )
            if (pickTrial is TrialResult.Sentenced) pickpocketSentences += pickTrial.sentenceYears
            if (fraudTrial is TrialResult.Sentenced) fraudSentences += fraudTrial.sentenceYears
        }
        assertTrue(pickpocketSentences.isNotEmpty())
        assertTrue(fraudSentences.isNotEmpty())
        val pickAvg = pickpocketSentences.average()
        val fraudAvg = fraudSentences.average()
        assertTrue("Fraud sentences should average longer than pickpocket", fraudAvg > pickAvg)
    }

    @Test
    fun processArrest_entersAwaitingTrialInsteadOfSentencing() {
        val character = TestFixtures.character(age = 20)
        val arrested = engine.processArrest(character, CrimeType.SHOPLIFT)
        assertTrue(arrested.criminalRecord.awaitingTrial)
        assertEquals(CrimeType.SHOPLIFT.name, arrested.criminalRecord.pendingCrimeType)
        assertFalse(arrested.criminalRecord.currentlyIncarcerated)
        assertEquals(0, arrested.criminalRecord.yearsRemaining)
    }

    @Test
    fun goToTrial_expensiveLawyerReducesSentenceProbability() {
        val base = TestFixtures.character(
            age = 25,
            stats = Stats(money = 2_000_000)
        )
        var publicSentences = 0
        var publicTotalYears = 0
        var expensiveLenientOutcomes = 0
        repeat(300) {
            val arrested = engine.processArrest(base, CrimeType.FRAUD)
            when (val publicResult = engine.goToTrial(
                arrested,
                LawyerTier.PUBLIC_DEFENDER,
                netWorth = 2_000_000
            )) {
                is TrialResult.Sentenced -> {
                    publicSentences++
                    publicTotalYears += publicResult.sentenceYears
                }
                is TrialResult.Acquitted -> Unit
                TrialResult.Ineligible -> Unit
            }
            val arrestedAgain = engine.processArrest(base, CrimeType.FRAUD)
            when (val expensiveResult = engine.goToTrial(
                arrestedAgain,
                LawyerTier.EXPENSIVE,
                netWorth = 2_000_000
            )) {
                is TrialResult.Acquitted -> expensiveLenientOutcomes++
                is TrialResult.Sentenced -> {
                    if (expensiveResult.sentenceYears <= 3) expensiveLenientOutcomes++
                }
                TrialResult.Ineligible -> Unit
            }
        }
        val publicAvg = publicTotalYears.toFloat() / publicSentences.coerceAtLeast(1)
        assertTrue(publicSentences > 100)
        assertTrue(expensiveLenientOutcomes > 150)
        assertTrue(expensiveLenientOutcomes > publicAvg)
    }

    @Test
    fun serveYear_triggersParoleReleaseOnGoodBehavior() {
        val imprisoned = TestFixtures.character(
            stats = Stats(happiness = 40),
            criminalRecord = CriminalRecord(
                currentlyIncarcerated = true,
                yearsRemaining = 4,
                totalSentenceYears = 6,
                yearsServed = 2,
                negativePrisonEvents = 0,
                paroleBonus = 6
            )
        )
        var paroles = 0
        repeat(400) {
            val after = engine.serveYear(imprisoned)
            if (!after.criminalRecord.currentlyIncarcerated &&
                after.eventLog.first().contains("parole", ignoreCase = true)
            ) {
                paroles++
            }
        }
        assertTrue("Good behavior should sometimes trigger parole", paroles > 0)
    }

    @Test
    fun cleanStreak_reducesHirePenaltyAfterFiveYears() {
        val fresh = TestFixtures.character(
            age = 25,
            stats = Stats(smarts = 65),
            education = EducationState(stage = SchoolStage.GRADUATED, gpa = 3.0f),
            criminalRecord = CriminalRecord(hasRecord = true, timesArrested = 1, lastArrestAge = 20)
        )
        // Keep ages well below / above the 5-year clean-streak threshold to reduce RNG flake.
        val beforeThreshold = fresh.copy(age = 22)
        val afterThreshold = fresh.copy(age = 30)
        var hiresBefore = 0
        var hiresAfter = 0
        repeat(1_200) {
            if (careerEngine.applyForJob(beforeThreshold, "teacher").second is CareerResult.Hired) hiresBefore++
            if (careerEngine.applyForJob(afterThreshold, "teacher").second is CareerResult.Hired) hiresAfter++
        }
        assertTrue("Clean streak should improve hiring after 5+ years", hiresAfter > hiresBefore)
    }

    @Test
    fun cleanStreak_doesNotReducePenaltyBeforeFiveYears() {
        val recent = TestFixtures.character(
            age = 22,
            stats = Stats(smarts = 65),
            education = EducationState(stage = SchoolStage.GRADUATED, gpa = 3.0f),
            criminalRecord = CriminalRecord(hasRecord = true, timesArrested = 1, lastArrestAge = 20)
        )
        val slightlyLater = recent.copy(age = 23)
        var recentHires = 0
        var laterHires = 0
        repeat(400) {
            if (careerEngine.applyForJob(recent, "teacher").second is CareerResult.Hired) recentHires++
            if (careerEngine.applyForJob(slightlyLater, "teacher").second is CareerResult.Hired) laterHires++
        }
        assertTrue(
            "Within 5-year window penalty should remain similarly harsh",
            kotlin.math.abs(recentHires - laterHires) < 80
        )
    }

    @Test
    fun attemptCrime_successRateScalesWithSmartsStat() {
        val lowSmarts = TestFixtures.character(age = 20, stats = Stats(smarts = 15))
        val highSmarts = TestFixtures.character(age = 20, stats = Stats(smarts = 95))
        var lowSuccesses = 0
        var highSuccesses = 0
        repeat(500) {
            if (engine.attemptCrime(lowSmarts, CrimeType.PICKPOCKET) is CrimeResult.Success) lowSuccesses++
            if (engine.attemptCrime(highSmarts, CrimeType.PICKPOCKET) is CrimeResult.Success) highSuccesses++
        }
        assertTrue(
            "Higher smarts should improve crime success rate",
            highSuccesses > lowSuccesses
        )
    }

    @Test
    fun performPrisonActivity_workDetailBoostsStandingAndCommissary() {
        val inmate = TestFixtures.character(
            age = 25,
            stats = Stats(money = 100),
            criminalRecord = CriminalRecord(
                currentlyIncarcerated = true,
                totalSentenceYears = 5,
                yearsRemaining = 5,
                cellblockStanding = 50,
                prisonJobDoneThisYear = false
            )
        )
        val result = engine.performPrisonActivity(inmate, PrisonActivity.WORK_DETAIL)
        assertTrue(result is PrisonActivityResult.Success)
        val updated = (result as PrisonActivityResult.Success).character
        assertTrue(updated.stats.money > 100)
        assertTrue(updated.criminalRecord.prisonJobDoneThisYear)
        assertTrue(updated.criminalRecord.cellblockStanding > 50)
    }

    @Test
    fun performPrisonActivity_gangAffiliationIncreasesStandingAndRivalryHeat() {
        val inmate = TestFixtures.character(
            age = 28,
            criminalRecord = CriminalRecord(
                currentlyIncarcerated = true,
                totalSentenceYears = 10,
                yearsRemaining = 10,
                cellblockStanding = 40,
                inmateRivalryHeat = 0,
                prisonGang = PrisonGang.NONE
            )
        )
        val result = engine.performPrisonActivity(inmate, PrisonActivity.PRISON_YARD_GANG)
        assertTrue(result is PrisonActivityResult.Success)
        val updated = (result as PrisonActivityResult.Success).character
        assertEquals(PrisonGang.YARD_SYNDICATE, updated.criminalRecord.prisonGang)
        assertTrue(updated.criminalRecord.cellblockStanding > 40)
        assertTrue(updated.criminalRecord.inmateRivalryHeat > 0)
    }

    @Test
    fun calculateEscapeChance_scalesWithSmartsStandingAndGangBonus() {
        val noviceInmate = TestFixtures.character(
            stats = Stats(smarts = 20),
            criminalRecord = CriminalRecord(
                cellblockStanding = 20,
                notoriety = 10,
                prisonGang = PrisonGang.NONE
            )
        )
        val veteranInmate = TestFixtures.character(
            stats = Stats(smarts = 85),
            criminalRecord = CriminalRecord(
                cellblockStanding = 85,
                notoriety = 80,
                prisonGang = PrisonGang.YARD_SYNDICATE
            )
        )
        val noviceOdds = engine.calculateEscapeChance(noviceInmate)
        val veteranOdds = engine.calculateEscapeChance(veteranInmate)
        assertTrue("Veteran crew member should have significantly higher escape odds", veteranOdds > noviceOdds)
    }

    @Test
    fun calculateProsecutionStrength_increasesWithHeatAndCrimeSeverity() {
        val pettyClean = TestFixtures.character(
            criminalRecord = CriminalRecord(
                pendingCrimeType = CrimeType.PICKPOCKET.name,
                heatLevel = 5,
                timesArrested = 1
            )
        )
        val majorHot = TestFixtures.character(
            criminalRecord = CriminalRecord(
                pendingCrimeType = CrimeType.FRAUD.name,
                heatLevel = 85,
                timesArrested = 4
            )
        )
        val lowPros = engine.calculateProsecutionStrength(pettyClean)
        val highPros = engine.calculateProsecutionStrength(majorHot)
        assertTrue("High heat and repeated felony should raise prosecution strength", highPros > lowPros)
    }

    @Test
    fun calculateAcquittalChance_scalesWithLawyerTier() {
        val defendant = TestFixtures.character(
            stats = Stats(smarts = 60, karma = 50),
            criminalRecord = CriminalRecord(
                pendingCrimeType = CrimeType.SHOPLIFT.name,
                heatLevel = 30
            )
        )
        val publicOdds = engine.calculateAcquittalChance(defendant, LawyerTier.PUBLIC_DEFENDER)
        val averageOdds = engine.calculateAcquittalChance(defendant, LawyerTier.AVERAGE)
        val expensiveOdds = engine.calculateAcquittalChance(defendant, LawyerTier.EXPENSIVE)
        assertTrue(expensiveOdds > averageOdds)
        assertTrue(averageOdds > publicOdds)
    }

    @Test
    fun calculateTargetCrimeSuccessChance_incorporatesCasingBonusAndPenalties() {
        val uncased = TestFixtures.character(
            stats = Stats(smarts = 70),
            criminalRecord = CriminalRecord(heatLevel = 10, casedTarget = null)
        )
        val cased = TestFixtures.character(
            stats = Stats(smarts = 70),
            criminalRecord = CriminalRecord(heatLevel = 10, casedTarget = CrimeTarget.BURGLARY_MANSION)
        )
        val uncasedChance = engine.calculateTargetCrimeSuccessChance(uncased, CrimeTarget.BURGLARY_MANSION)
        val casedChance = engine.calculateTargetCrimeSuccessChance(cased, CrimeTarget.BURGLARY_MANSION)
        assertTrue("Casing blueprint should improve target success chance", casedChance > uncasedChance)
    }

    @Test
    fun coolDownYear_resetsPrisonJobFlagAndCoolsRivalryHeat() {
        val character = TestFixtures.character(
            criminalRecord = CriminalRecord(
                heatLevel = 60,
                prisonJobDoneThisYear = true,
                inmateRivalryHeat = 40
            )
        )
        val cooled = engine.coolDownYear(character)
        assertFalse(cooled.criminalRecord.prisonJobDoneThisYear)
        assertTrue(cooled.criminalRecord.heatLevel < 60)
        assertTrue(cooled.criminalRecord.inmateRivalryHeat < 40)
    }
}
