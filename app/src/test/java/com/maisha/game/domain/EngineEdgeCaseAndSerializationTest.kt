package com.maisha.game.domain

import com.maisha.game.data.model.Asset
import com.maisha.game.data.model.AssetType
import com.maisha.game.data.model.CareerState
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.CrimeTarget
import com.maisha.game.data.model.CriminalRecord
import com.maisha.game.data.model.EducationState
import com.maisha.game.data.model.Job
import com.maisha.game.data.model.Person
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.SchoolStage
import com.maisha.game.data.model.Stats
import com.maisha.game.util.SerializationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineEdgeCaseAndSerializationTest {

    private val healthEngine = HealthEngine()
    private val financeEngine = FinanceEngine()
    private val careerEngine = CareerEngine(healthEngine, RelocationEngine())
    private val mortalityEngine = MortalityEngine()
    private val legacyEngine = LegacyEngine(mortalityEngine, financeEngine)

    // ── 1. Serialization & Backward-Compatibility Defaults ─────────────────────

    @Test
    fun serialization_safeDeserialize_handlesLegacyJsonMissingFields() {
        // Legacy Asset JSON missing mortgageBalance, annualMortgagePayment, servicedThisYear
        val legacyAssetJson = """
            {
                "id": "asset-legacy-1",
                "type": "HOUSE",
                "name": "Cozy Bungalow",
                "purchasePrice": 250000,
                "currentValue": 260000,
                "condition": 85,
                "monthlyUpkeep": 800
            }
        """.trimIndent()

        val asset = SerializationUtils.safeDeserialize(
            raw = legacyAssetJson,
            fieldName = "asset",
            default = TestFixtures.asset()
        )

        assertEquals("asset-legacy-1", asset.id)
        assertEquals(260000, asset.currentValue)
        assertEquals(0, asset.mortgageBalance)
        assertEquals(0, asset.annualMortgagePayment)
        assertFalse(asset.servicedThisYear)

        // Legacy CareerState JSON missing onPip, pipDeliverableCompletedThisYear, pipAppealedThisYear
        val legacyCareerJson = """
            {
                "performance": 65.0,
                "stress": 30.0,
                "yearsAtCurrentJob": 3
            }
        """.trimIndent()

        val career = SerializationUtils.safeDeserialize(
            raw = legacyCareerJson,
            fieldName = "career",
            default = CareerState()
        )

        assertEquals(65f, career.performance, 0.001f)
        assertFalse(career.onPip)
        assertFalse(career.pipDeliverableCompletedThisYear)
        assertFalse(career.pipAppealedThisYear)

        // Legacy CriminalRecord JSON missing heatLevel, casedTarget, heatActionDoneThisYear
        val legacyCrimeJson = """
            {
                "hasRecord": true,
                "timesArrested": 1
            }
        """.trimIndent()

        val crime = SerializationUtils.safeDeserialize(
            raw = legacyCrimeJson,
            fieldName = "criminalRecord",
            default = CriminalRecord()
        )

        assertTrue(crime.hasRecord)
        assertEquals(1, crime.timesArrested)
        assertEquals(0, crime.heatLevel)
        assertNull(crime.casedTarget)
        assertFalse(crime.heatActionDoneThisYear)
    }

    @Test
    fun serialization_safeDeserialize_coercesNullInputValuesToDefaults() {
        val jsonWithNulls = """
            {
                "id": "asset-null-coerced",
                "type": "CAR",
                "name": "Sedan",
                "purchasePrice": 20000,
                "currentValue": 18000,
                "monthlyUpkeep": 300,
                "servicedThisYear": null,
                "mortgageBalance": null
            }
        """.trimIndent()

        val asset = SerializationUtils.safeDeserialize(
            raw = jsonWithNulls,
            fieldName = "asset",
            default = TestFixtures.asset()
        )

        assertEquals("asset-null-coerced", asset.id)
        assertEquals(0, asset.mortgageBalance)
        assertFalse(asset.servicedThisYear)
    }

    // ── 2. Net Worth & Mortgage Liabilities ──────────────────────────────────

    @Test
    fun finance_calculateNetWorth_properlyDeductsMortgagesAndDebt() {
        val mortgagedHouse = Asset(
            id = "house-mortgaged",
            type = AssetType.HOUSE,
            name = "Suburban Home",
            purchasePrice = 500_000,
            currentValue = 500_000,
            monthlyUpkeep = 1_200,
            mortgageBalance = 350_000,
            annualMortgagePayment = 28_000
        )
        val character = TestFixtures.character(
            stats = Stats(money = 100_000),
            assets = listOf(mortgagedHouse),
            education = EducationState(studentLoanBalance = 20_000)
        )

        // Net worth = Cash (100k) + Asset (500k) - Student Loan (20k) - Mortgage (350k) = 230,000
        val netWorth = financeEngine.calculateNetWorth(character)
        assertEquals(230_000, netWorth)
    }

    @Test
    fun finance_prepayMortgagePrincipal_reducesBalanceAndClearsMortgageOnFullPayoff() {
        val mortgagedHouse = Asset(
            id = "house-1",
            type = AssetType.HOUSE,
            name = "Townhouse",
            purchasePrice = 400_000,
            currentValue = 420_000,
            monthlyUpkeep = 1_000,
            mortgageBalance = 200_000,
            annualMortgagePayment = 20_000
        )
        val character = TestFixtures.character(
            stats = Stats(money = 300_000),
            assets = listOf(mortgagedHouse)
        )

        // Partial prepayment of 80,000
        val partiallyPaid = financeEngine.prepayMortgagePrincipal(character, "house-1", 80_000)
        val updatedAsset = partiallyPaid.assets.first { it.id == "house-1" }
        assertEquals(120_000, updatedAsset.mortgageBalance)
        assertTrue(updatedAsset.annualMortgagePayment < 20_000)

        // Full remaining payoff of 120,000
        val fullyPaid = financeEngine.prepayMortgagePrincipal(partiallyPaid, "house-1", 120_000)
        val clearedAsset = fullyPaid.assets.first { it.id == "house-1" }
        assertEquals(0, clearedAsset.mortgageBalance)
        assertEquals(0, clearedAsset.annualMortgagePayment)
    }

    // ── 3. Lifecycle Extreme Ages & Minor Guards ─────────────────────────────

    @Test
    fun lifecycle_extremeAges_enforcesAgeGatesCorrectly() {
        val infant = TestFixtures.character(age = 0, stats = Stats(money = -5_000, health = 100, happiness = 100))
        assertNull("Infancy protection (age 0-2) blocks random illness onset", healthEngine.rollForIllness(infant))

        val agedInfant = healthEngine.processHealthProgression(infant)
        assertEquals("Infants do not suffer debt stress", 100, agedInfant.stats.health)
        assertEquals("Infants do not suffer debt stress", 100, agedInfant.stats.happiness)

        val teenStudent = TestFixtures.character(
            age = 16,
            education = EducationState(stage = SchoolStage.SECONDARY)
        )
        val youngStudent = TestFixtures.character(
            age = 13,
            education = EducationState(stage = SchoolStage.SECONDARY)
        )
        assertFalse("Under-16 cannot work part-time", careerEngine.canWorkPartTime(youngStudent))
        assertTrue("16+ enrolled student can work part-time", careerEngine.canWorkPartTime(teenStudent))

        val senior = TestFixtures.character(
            age = 60,
            career = CareerState(
                currentJob = Job(
                    id = "manager",
                    title = "Manager",
                    minEducation = SchoolStage.GRADUATED,
                    baseSalary = 100_000
                ),
                yearsAtCurrentJob = 20
            )
        )
        assertTrue("Age 60+ is eligible for retirement", careerEngine.canRetire(senior))

        val centenarian = TestFixtures.character(age = 105, stats = Stats(health = 60))
        val prob = mortalityEngine.estimatedDeathProbability(centenarian)
        assertTrue("Centenarian mortality probability must be bounded between 0 and 1", prob in 0f..1f)
    }

    // ── 4. Succession Zero vs Multiple Children & Estate Solvency ────────────

    @Test
    fun succession_zeroVsMultipleChildren_handlesInheritanceSafely() {
        val deceasedNoKids = TestFixtures.character(
            age = 75,
            family = listOf(TestFixtures.person(id = "spouse-1", relation = RelationType.SPOUSE, age = 72))
        )
        val heirsNoKids = legacyEngine.eligibleHeirs(deceasedNoKids)
        assertTrue("Zero children yields empty eligible heirs", heirsNoKids.isEmpty())
        val inheritanceNoKids = legacyEngine.calculateMoneyInheritance(deceasedNoKids, 500_000, deceasedNoKids.family.first())
        assertEquals(0, inheritanceNoKids)

        val youngChild = TestFixtures.child(id = "child-young", age = 10, relationship = 80)
        val deceasedYoungKid = TestFixtures.character(
            age = 40,
            family = listOf(youngChild)
        )
        val heirsYoung = legacyEngine.eligibleHeirs(deceasedYoungKid)
        assertTrue("Under-16 children cannot assume immediate dynasty succession", heirsYoung.isEmpty())

        val adultChild1 = TestFixtures.child(id = "child-1", age = 22, relationship = 85)
        val adultChild2 = TestFixtures.child(id = "child-2", age = 19, relationship = 90)
        val deceasedMultiKids = TestFixtures.character(
            age = 65,
            family = listOf(adultChild1, adultChild2)
        )
        val heirsMulti = legacyEngine.eligibleHeirs(deceasedMultiKids)
        assertEquals(2, heirsMulti.size)
        assertEquals("Oldest child is first", "child-1", heirsMulti.first().id)

        // Insolvent estate (liabilities exceed cash)
        val insolventDeceased = TestFixtures.character(
            age = 70,
            stats = Stats(money = -50_000),
            family = listOf(adultChild1)
        )
        val settlement = legacyEngine.calculateEstateSettlement(insolventDeceased)
        assertEquals("Distributable estate cash never drops below 0", 0, settlement.distributableCash)
        val inheritanceInsolvent = legacyEngine.calculateMoneyInheritance(insolventDeceased, settlement.distributableCash, adultChild1)
        assertEquals("Heir never inherits negative cash/debt", 0, inheritanceInsolvent)
    }
}
