package com.maisha.game.domain

import com.maisha.game.data.model.AnnualCashFlow
import com.maisha.game.data.model.Asset
import com.maisha.game.data.model.AssetType
import com.maisha.game.data.model.CareerState
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.CreditRatingTier
import com.maisha.game.data.model.CriminalRecord
import com.maisha.game.data.model.EconomicClimate
import com.maisha.game.data.model.EconomicState
import com.maisha.game.data.model.EducationState
import com.maisha.game.data.model.Job
import com.maisha.game.data.model.Person
import com.maisha.game.data.model.PersonalFinanceState
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.Stats
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FinanceEngineTest {

    private lateinit var financeEngine: FinanceEngine
    private lateinit var careerEngine: CareerEngine

    @Before
    fun setUp() {
        financeEngine = FinanceEngine()
        careerEngine = CareerEngine(HealthEngine(), RelocationEngine())
    }

    @Test
    fun creditRatingTier_resolvesCorrectlyAcrossRanges() {
        assertEquals(CreditRatingTier.EXCELLENT, CreditRatingTier.forScore(850))
        assertEquals(CreditRatingTier.EXCELLENT, CreditRatingTier.forScore(750))
        assertEquals(CreditRatingTier.GOOD, CreditRatingTier.forScore(749))
        assertEquals(CreditRatingTier.GOOD, CreditRatingTier.forScore(670))
        assertEquals(CreditRatingTier.FAIR, CreditRatingTier.forScore(669))
        assertEquals(CreditRatingTier.FAIR, CreditRatingTier.forScore(580))
        assertEquals(CreditRatingTier.POOR, CreditRatingTier.forScore(579))
        assertEquals(CreditRatingTier.POOR, CreditRatingTier.forScore(300))
    }

    @Test
    fun creditScore_increasesWithOnTimeDebtPayments() {
        val mortgagedHouse = TestFixtures.asset(
            id = "house-1",
            type = AssetType.HOUSE,
            purchasePrice = 200_000,
            currentValue = 200_000,
            mortgageBalance = 150_000,
            annualMortgagePayment = 12_000
        )
        val character = TestFixtures.character(
            age = 28,
            stats = Stats(money = 100_000),
            assets = listOf(mortgagedHouse),
            career = CareerState(currentJob = TestFixtures.job(id = "job-1", title = "Architect", baseSalary = 80_000)),
            finance = PersonalFinanceState(creditScore = 650, onTimePaymentStreak = 2)
        )

        val updated = financeEngine.tickCreditScoreAndCashFlow(character)

        assertTrue("Credit score should increase on-time", updated.finance.creditScore > 650)
        assertEquals(3, updated.finance.onTimePaymentStreak)
        assertEquals(0, updated.finance.missedPaymentsCount)
    }

    @Test
    fun creditScore_dropsOnPaymentShortfall() {
        val mortgagedHouse = TestFixtures.asset(
            id = "house-1",
            type = AssetType.HOUSE,
            purchasePrice = 200_000,
            currentValue = 200_000,
            mortgageBalance = 150_000,
            annualMortgagePayment = 12_000
        )
        val brokeCharacter = TestFixtures.character(
            age = 28,
            stats = Stats(money = 0),
            assets = listOf(mortgagedHouse),
            finance = PersonalFinanceState(creditScore = 680, onTimePaymentStreak = 5)
        )

        val updated = financeEngine.tickCreditScoreAndCashFlow(brokeCharacter)

        assertEquals("Payment streak should reset to 0 on shortfall", 0, updated.finance.onTimePaymentStreak)
        assertEquals("Missed payments counter should increment", 1, updated.finance.missedPaymentsCount)
        assertTrue("Credit score should drop significantly", updated.finance.creditScore <= 645)
    }

    @Test
    fun creditScore_draggedByCriminalRecord() {
        val character = TestFixtures.character(
            age = 30,
            stats = Stats(money = 50_000),
            criminalRecord = CriminalRecord(hasRecord = true, timesArrested = 2),
            finance = PersonalFinanceState(creditScore = 700)
        )

        val updated = financeEngine.tickCreditScoreAndCashFlow(character)

        assertTrue("Criminal record should drag down credit score", updated.finance.creditScore < 700)
    }

    @Test
    fun prepayMortgage_boostsCreditScore() {
        val house = TestFixtures.asset(
            id = "house-1",
            type = AssetType.HOUSE,
            purchasePrice = 300_000,
            currentValue = 300_000,
            mortgageBalance = 200_000,
            annualMortgagePayment = 18_000
        )
        val character = TestFixtures.character(
            age = 32,
            stats = Stats(money = 50_000),
            assets = listOf(house),
            finance = PersonalFinanceState(creditScore = 650)
        )

        val updated = financeEngine.prepayMortgagePrincipal(character, "house-1", 20_000)

        assertEquals(665, updated.finance.creditScore)
        assertEquals(180_000, updated.assets.first().mortgageBalance)
    }

    @Test
    fun payOffMortgage_boostsCreditScore() {
        val house = TestFixtures.asset(
            id = "house-1",
            type = AssetType.HOUSE,
            purchasePrice = 100_000,
            currentValue = 100_000,
            mortgageBalance = 30_000,
            annualMortgagePayment = 4_000
        )
        val character = TestFixtures.character(
            age = 35,
            stats = Stats(money = 40_000),
            assets = listOf(house),
            finance = PersonalFinanceState(creditScore = 670)
        )

        val updated = financeEngine.payOffMortgage(character, "house-1")

        assertEquals(695, updated.finance.creditScore)
        assertEquals(0, updated.assets.first().mortgageBalance)
    }

    @Test
    fun mortgageGating_rejectsSub500CreditScore() {
        val poorCreditCharacter = TestFixtures.character(
            age = 25,
            stats = Stats(money = 500_000),
            countryCode = "US",
            finance = PersonalFinanceState(creditScore = 450)
        )

        val result = financeEngine.purchaseWithMortgage(
            poorCreditCharacter,
            "house_suburban",
            downPaymentPercent = 20
        )

        assertTrue(result is FinanceEngine.MortgagePurchaseResult.RejectedCredit)
        assertEquals(450, (result as FinanceEngine.MortgagePurchaseResult.RejectedCredit).creditScore)
    }

    @Test
    fun mortgageGating_tierDownPaymentAndAprScaling() {
        assertEquals(10, financeEngine.minDownPaymentPercentForCredit(780))
        assertEquals(20, financeEngine.minDownPaymentPercentForCredit(700))
        assertEquals(25, financeEngine.minDownPaymentPercentForCredit(620))
        assertEquals(40, financeEngine.minDownPaymentPercentForCredit(520))

        assertEquals(0.055, financeEngine.baseMortgageAprForCredit(800), 0.001)
        assertEquals(0.075, financeEngine.baseMortgageAprForCredit(700), 0.001)
        assertEquals(0.095, financeEngine.baseMortgageAprForCredit(600), 0.001)
        assertEquals(0.120, financeEngine.baseMortgageAprForCredit(500), 0.001)
    }

    @Test
    fun computeAnnualCashFlow_calculatesAccurateTotals() {
        val job = TestFixtures.job(id = "job-1", title = "Doctor", baseSalary = 150_000)
        val rentedHouse = TestFixtures.asset(
            id = "house-rented",
            type = AssetType.HOUSE,
            purchasePrice = 400_000,
            currentValue = 400_000,
            monthlyUpkeep = 500,
            isRentedOut = true,
            tenantHappiness = 85
        )
        val child = Person(
            id = "child-1",
            name = "Baby",
            age = 4,
            relation = RelationType.CHILD,
            alive = true
        )
        val character = TestFixtures.character(
            age = 34,
            stats = Stats(money = 50_000),
            countryCode = "US",
            career = CareerState(currentJob = job),
            assets = listOf(rentedHouse),
            family = listOf(child),
            finance = PersonalFinanceState(creditScore = 720)
        )

        val cashFlow = financeEngine.computeAnnualCashFlow(character)

        assertEquals(150_000, cashFlow.salaryIncome)
        assertTrue("Rental income should be positive", cashFlow.rentalIncome > 0)
        assertEquals(cashFlow.salaryIncome + cashFlow.rentalIncome, cashFlow.grossIncome)
        assertTrue("Income tax should be calculated", cashFlow.incomeTax > 0)
        assertTrue("Living cost should be calculated", cashFlow.livingCost > 0)
        assertTrue("Asset upkeep should be calculated", cashFlow.assetUpkeep > 0)
        assertTrue("Child support should be calculated", cashFlow.childSupport > 0)
        assertEquals(
            cashFlow.incomeTax + cashFlow.livingCost + cashFlow.assetUpkeep + cashFlow.childSupport,
            cashFlow.totalDeductions
        )
        assertEquals(cashFlow.grossIncome - cashFlow.totalDeductions, cashFlow.netCashFlow)
    }

    @Test
    fun economicCycle_resolvesAndAppliesAllFourStates() {
        val adult = TestFixtures.character(age = 30)

        val boomResult = financeEngine.applyEconomicShift(adult, forced = "boom")
        assertEquals(EconomicClimate.BOOM, boomResult.climate)
        assertEquals(FinanceEngine.BOOM_MARKET_MODIFIER, boomResult.character.economicState.marketModifier, 0.01f)

        val steadyResult = financeEngine.applyEconomicShift(adult, forced = "steady")
        assertEquals(EconomicClimate.STEADY, steadyResult.climate)
        assertEquals(FinanceEngine.STEADY_MARKET_MODIFIER, steadyResult.character.economicState.marketModifier, 0.01f)

        val inflationResult = financeEngine.applyEconomicShift(adult, forced = "inflation_surge")
        assertEquals(EconomicClimate.INFLATION_SURGE, inflationResult.climate)
        assertEquals(FinanceEngine.INFLATION_MARKET_MODIFIER, inflationResult.character.economicState.marketModifier, 0.01f)

        val recessionResult = financeEngine.applyEconomicShift(adult, forced = "recession")
        assertEquals(EconomicClimate.RECESSION, recessionResult.climate)
        assertEquals(FinanceEngine.RECESSION_MARKET_MODIFIER, recessionResult.character.economicState.marketModifier, 0.01f)
    }

    @Test
    fun economicCycle_backwardCompatibilityAliases() {
        val adult = TestFixtures.character(age = 30)

        val legacyNeutral = financeEngine.applyEconomicShift(adult, forced = "neutral")
        assertEquals(EconomicClimate.STEADY, legacyNeutral.climate)

        val legacyBust = financeEngine.applyEconomicShift(adult, forced = "bust")
        assertEquals(EconomicClimate.RECESSION, legacyBust.climate)
    }

    @Test
    fun economicCycle_layoffOddsScaleWithMacroConditions() {
        val employedAdult = TestFixtures.character(
            age = 30,
            career = CareerState(currentJob = TestFixtures.job(id = "job-1", title = "Analyst", baseSalary = 50_000)),
            economicState = EconomicState(climate = EconomicClimate.RECESSION)
        )

        // Multiple sampling to verify downsizing trigger responds
        var layoffsInRecession = 0
        for (i in 0 until 100) {
            if (careerEngine.shouldTriggerDownsizing(employedAdult)) {
                layoffsInRecession++
            }
        }

        val employedBoom = employedAdult.copy(economicState = EconomicState(climate = EconomicClimate.BOOM))
        var layoffsInBoom = 0
        for (i in 0 until 100) {
            if (careerEngine.shouldTriggerDownsizing(employedBoom)) {
                layoffsInBoom++
            }
        }

        assertTrue("Recession layoffs should be significantly higher than Boom layoffs", layoffsInRecession >= layoffsInBoom)
    }

    @Test
    fun juniorTrust_depositLockedForMinorAndUnlocksAtAge18() {
        val minor = TestFixtures.character(
            age = 10,
            stats = Stats(money = 15_000),
            finance = PersonalFinanceState(juniorTrustBalance = 5_000)
        )

        val depositResult = financeEngine.depositJuniorTrust(minor, 5_000)
        assertTrue(depositResult is FinanceEngine.InvestmentResult.Success)
        val afterDeposit = (depositResult as FinanceEngine.InvestmentResult.Success).character
        assertEquals(10_000, afterDeposit.stats.money)
        assertEquals(10_000, afterDeposit.finance.juniorTrustBalance)

        // Reaching age 18 unlocks the trust
        val adultCharacter = afterDeposit.copy(age = 18)
        val afterUnlock = financeEngine.unlockJuniorTrustAtAge18(adultCharacter)

        assertEquals("Trust balance should be cleared", 0, afterUnlock.finance.juniorTrustBalance)
        assertEquals("Liquid cash should receive all trust funds", 20_000, afterUnlock.stats.money)
        assertTrue("Event log should mention Junior Trust unlock", afterUnlock.eventLog.any { it.contains("Junior Trust Piggy Bank") })
    }

    @Test
    fun infancyProtection_noLivingCostOrDebtStressAtAgeZeroToTwo() {
        val infant = TestFixtures.character(
            age = 1,
            stats = Stats(happiness = 100, health = 100, money = 0)
        )

        val afterCol = financeEngine.applyCostOfLiving(infant)
        assertEquals(100, afterCol.stats.happiness)
        assertEquals(100, afterCol.stats.health)
        assertEquals(0, afterCol.stats.money)

        val afterCredit = financeEngine.tickCreditScoreAndCashFlow(infant)
        assertEquals(650, afterCredit.finance.creditScore)
    }

    @Test
    fun economicClimateSerializer_deserializesLegacyNeutralAndBust() {
        val json = Json { ignoreUnknownKeys = true }

        val neutralJson = """{"climate":"NEUTRAL","marketModifier":1.0}"""
        val parsedNeutral = json.decodeFromString<EconomicState>(neutralJson)
        assertEquals(EconomicClimate.STEADY, parsedNeutral.climate)

        val bustJson = """{"climate":"BUST","marketModifier":0.88}"""
        val parsedBust = json.decodeFromString<EconomicState>(bustJson)
        assertEquals(EconomicClimate.RECESSION, parsedBust.climate)

        val inflationJson = """{"climate":"INFLATION_SURGE","marketModifier":1.05}"""
        val parsedInflation = json.decodeFromString<EconomicState>(inflationJson)
        assertEquals(EconomicClimate.INFLATION_SURGE, parsedInflation.climate)
    }

    @Test
    fun assetSerialization_defaultsMortgageInterestRate() {
        val json = Json { ignoreUnknownKeys = true }
        val legacyAssetJson = """{
            "id": "asset-1",
            "type": "HOUSE",
            "name": "Cozy Bungalow",
            "purchasePrice": 200000,
            "currentValue": 200000,
            "monthlyUpkeep": 300,
            "mortgageBalance": 160000,
            "annualMortgagePayment": 14000
        }"""

        val asset = json.decodeFromString<Asset>(legacyAssetJson)
        assertEquals(0.08, asset.mortgageInterestRate, 0.001)
    }
}
