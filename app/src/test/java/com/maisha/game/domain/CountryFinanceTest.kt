package com.maisha.game.domain

import com.maisha.game.billing.BillingProducts
import com.maisha.game.data.CountryCatalog
import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.Country
import com.maisha.game.data.model.EconomicClimate
import com.maisha.game.data.model.EconomicState
import com.maisha.game.data.model.FinancialRiskLevel
import com.maisha.game.data.model.Person
import com.maisha.game.data.model.PersonalFinanceState
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.Stats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CountryFinanceTest {

    private lateinit var financeEngine: FinanceEngine
    private lateinit var relocationEngine: RelocationEngine

    @Before
    fun setUp() {
        financeEngine = FinanceEngine()
        relocationEngine = RelocationEngine()
    }

    @Test
    fun countryCatalog_all15CountriesHaveDistinctFinanceProfiles() {
        val supportedCodes = listOf(
            "BR", "CA", "EG", "FR", "DE", "IN", "ID", "JP", "KE", "MX", "NG", "PH", "ZA", "GB", "US"
        )
        for (code in supportedCodes) {
            val profile = CountryCatalog.financeProfileFor(code)
            assertEquals(code, profile.countryCode)
            assertNotNull(profile.primaryInstrument)
            assertTrue(profile.primaryInstrument.name.isNotBlank())
            assertTrue(profile.primaryInstrument.localName.isNotBlank())
            assertTrue(profile.primaryInstrument.annualYieldPercent > 0.0)
            assertTrue(profile.familyObligationFraction > 0.0)
        }
    }

    @Test
    fun countryCatalog_unknownCountryFallsBackToGenericProfile() {
        val fallback = CountryCatalog.financeProfileFor("ZZ")
        assertEquals("XX", fallback.countryCode)
        assertEquals("index_savings_xx", fallback.primaryInstrument.id)
    }

    @Test
    fun countryCatalog_kenyaHasAuthenticSaccoAndMobileOverdraft() {
        val keProfile = CountryCatalog.financeProfileFor("KE")
        assertEquals("sacco_chama_ke", keProfile.primaryInstrument.id)
        assertEquals("SACCO / Chama", keProfile.primaryInstrument.localName)
        assertTrue("Kenya must support mobile overdraft buffer", keProfile.mobileOverdraftFacility)
        assertTrue(keProfile.mobileOverdraftLimitKenya > 0)
    }

    @Test
    fun countryCatalog_nigeriaHasHighestFamilyObligationFraction() {
        val ngProfile = CountryCatalog.financeProfileFor("NG")
        val usProfile = CountryCatalog.financeProfileFor("US")
        assertTrue(
            "Nigeria cultural black tax/family support should be higher than US",
            ngProfile.familyObligationFraction > usProfile.familyObligationFraction
        )
    }

    @Test
    fun economyScaler_convertCurrency_computesAccurateFxRatios() {
        // Same country identity
        assertEquals(50_000, EconomyScaler.convertCurrency(50_000, "KE", "KE"))

        // KE (mult 1.0) to US (mult 0.25): 100,000 KES -> 25,000 USD
        val usdFromKes = EconomyScaler.convertCurrency(100_000, "KE", "US")
        assertEquals(25_000, usdFromKes)

        // US (mult 0.25) to KE (mult 1.0): 25,000 USD -> 100,000 KES
        val kesFromUsd = EconomyScaler.convertCurrency(25_000, "US", "KE")
        assertEquals(100_000, kesFromUsd)

        // Zero amount stays zero
        assertEquals(0, EconomyScaler.convertCurrency(0, "KE", "US"))
    }

    @Test
    fun relocation_convertsCurrencyAndResetsLocalCreditScore() {
        val keCharacter = TestFixtures.character(
            age = 28,
            countryCode = "KE",
            stats = Stats(money = 200_000, happiness = 80),
            finance = PersonalFinanceState(
                creditScore = 780,
                onTimePaymentStreak = 10,
                countryInstrumentBalance = 50_000
            )
        )

        val usCountry = CountryCatalog.getCountry("US")
        val relocated = relocationEngine.relocate(keCharacter, usCountry)

        assertEquals("US", relocated.countryCode)
        // Converted money: 200k KES -> 50k USD, plus 50k KES liquidated SACCO -> 12.5k USD = 62.5k USD
        assertEquals(62_500, relocated.stats.money)
        // Credit score resets to 600 in the new country
        assertEquals(600, relocated.finance.creditScore)
        assertEquals(0, relocated.finance.onTimePaymentStreak)
        assertEquals(0, relocated.finance.countryInstrumentBalance)
        assertTrue(relocated.eventLog.any { it.contains("Converted funds to USD") })
    }

    @Test
    fun countryInstrument_depositAndWithdrawWithMinimumThreshold() {
        val character = TestFixtures.character(
            age = 25,
            countryCode = "KE",
            stats = Stats(money = 100_000),
            finance = PersonalFinanceState(countryInstrumentBalance = 0)
        )

        // Try depositing below minimum (5,000 for KE SACCO)
        val belowMinResult = financeEngine.depositCountryInstrument(character, 1_000)
        assertTrue(belowMinResult is FinanceEngine.InvestmentResult.BelowMinimum)

        // Successful deposit
        val successDeposit = financeEngine.depositCountryInstrument(character, 20_000)
        assertTrue(successDeposit is FinanceEngine.InvestmentResult.Success)
        val afterDeposit = (successDeposit as FinanceEngine.InvestmentResult.Success).character
        assertEquals(80_000, afterDeposit.stats.money)
        assertEquals(20_000, afterDeposit.finance.countryInstrumentBalance)

        // Withdraw half
        val successWithdraw = financeEngine.withdrawCountryInstrument(afterDeposit, 10_000)
        assertTrue(successWithdraw is FinanceEngine.InvestmentResult.Success)
        val afterWithdraw = (successWithdraw as FinanceEngine.InvestmentResult.Success).character
        assertEquals(90_000, afterWithdraw.stats.money)
        assertEquals(10_000, afterWithdraw.finance.countryInstrumentBalance)
    }

    @Test
    fun countryInstrument_annualDividendsPayOutAccurately() {
        val character = TestFixtures.character(
            age = 30,
            countryCode = "KE",
            stats = Stats(money = 10_000),
            finance = PersonalFinanceState(countryInstrumentBalance = 100_000),
            economicState = EconomicState(climate = EconomicClimate.STEADY)
        )

        val updated = financeEngine.tickCountryInstrument(character)
        // KE SACCO yields 11.5% in steady climate -> 11,500 payout
        assertEquals(21_500, updated.stats.money)
        assertTrue(updated.eventLog.any { it.contains("annual dividend yields from SACCO / Chama") })
    }

    @Test
    fun mobileOverdraft_buffersLivingCostShortfallInKenya() {
        val brokeKenyan = TestFixtures.character(
            age = 22,
            countryCode = "KE",
            stats = Stats(money = 0, health = 100, happiness = 80),
            finance = PersonalFinanceState(mobileOverdraftUsed = 0)
        )

        val updated = financeEngine.applyCostOfLiving(brokeKenyan)
        // Living costs should be buffered by overdraft rather than inflicting maximum penalty
        assertTrue("Mobile overdraft should be drawn", updated.finance.mobileOverdraftUsed > 0)
        assertTrue("Health should be protected by buffer", updated.stats.health >= 90)
        assertTrue(updated.eventLog.any { it.contains("mobile overdraft") })
    }

    @Test
    fun internationalRemittance_requiresLivingAbroadAndBoostsFamily() {
        val parent = Person(
            id = "parent-1",
            name = "Amina",
            age = 55,
            relation = RelationType.MOTHER,
            alive = true,
            relationshipLevel = 60,
            countryCode = "KE"
        )
        val diasporaCharacter = TestFixtures.character(
            age = 29,
            countryCode = "US",
            stats = Stats(money = 5_000, happiness = 70, karma = 50),
            family = listOf(parent)
        ).copy(birthCountryCode = "KE")

        // Remit 1,000 USD to family in KE
        val result = financeEngine.remitMoneyHome(diasporaCharacter, 1_000)
        assertTrue(result is FinanceEngine.RemittanceResult.Success)
        val success = result as FinanceEngine.RemittanceResult.Success
        assertEquals(1_000, success.sentAmount)
        // In KE (1.0 vs 0.25), 1,000 USD -> 4,000 KES
        assertEquals(4_000, success.receivedAmount)

        val updated = success.character
        assertEquals(4_000, updated.stats.money)
        assertEquals(1_000, updated.finance.lifetimeRemittancesSent)
        assertTrue("Family relation should increase", updated.family.first().relationshipLevel > 60)
        assertTrue("Happiness should increase", updated.stats.happiness > 70)
        assertTrue(updated.eventLog.any { it.contains("foreign remittances to family in Kenya") })
    }

    @Test
    fun internationalRemittance_rejectedWhenNotLivingAbroad() {
        val localCharacter = TestFixtures.character(
            age = 25,
            countryCode = "KE",
            stats = Stats(money = 50_000)
        ).copy(birthCountryCode = "KE")

        val result = financeEngine.remitMoneyHome(localCharacter, 5_000)
        assertTrue(result is FinanceEngine.RemittanceResult.NotLivingAbroad)
    }

    @Test
    fun billingProducts_scaleConsumableCashVouchersAcrossCountries() {
        val baseStarter = BillingProducts.getBaseCashForProduct(BillingProducts.PRODUCT_CASH_STARTER)
        assertEquals(BillingProducts.BASE_CASH_STARTER_KENYA, baseStarter)

        val starterInKenya = EconomyScaler.scaleAmount(baseStarter, "KE")
        val starterInUs = EconomyScaler.scaleAmount(baseStarter, "US")
        val starterInNigeria = EconomyScaler.scaleAmount(baseStarter, "NG")

        assertEquals(100_000, starterInKenya)
        assertEquals(25_000, starterInUs)
        assertEquals(95_000, starterInNigeria)
    }
}
