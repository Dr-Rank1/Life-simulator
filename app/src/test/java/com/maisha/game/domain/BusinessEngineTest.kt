package com.maisha.game.domain

import com.maisha.game.data.model.Business
import com.maisha.game.data.model.BusinessIndustry
import com.maisha.game.data.model.CompanyStage
import com.maisha.game.data.model.Stats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BusinessEngineTest {

    private val financeEngine = FinanceEngine()
    private val engine = BusinessEngine(financeEngine)

    @Test
    fun startBusiness_deductsCashAndCreatesBusinessEntity() {
        val investment = engine.minInvestment(
            TestFixtures.character(stats = Stats(money = 1_000_000))
        )
        val character = TestFixtures.character(
            age = 30,
            stats = Stats(money = 1_000_000, smarts = 70)
        )
        when (val result = engine.startBusiness(
            character,
            name = "Acme Labs",
            industry = BusinessIndustry.TECH,
            initialInvestment = investment
        )) {
            is BusinessResult.Success -> {
                assertEquals(1, result.character.businesses.size)
                val business = result.character.businesses.first()
                assertEquals("Acme Labs", business.name)
                assertEquals(BusinessIndustry.TECH, business.industry)
                assertEquals(CompanyStage.BOOTSTRAP, business.stage)
                assertEquals(100, business.equityShare)
                assertTrue(business.valuation > 0)
                assertTrue(business.revenue > 0)
                assertTrue(business.annualBurn > 0)
                assertTrue(business.employeeCount >= 1)
                assertEquals(1_000_000 - investment, result.character.stats.money)
            }
            is BusinessResult.Failed -> error("Expected start success: ${result.reason}")
        }
    }

    @Test
    fun launchProductService_updatesQualityAndMorale() {
        val business = Business(
            id = "b1",
            name = "App Factory",
            industry = BusinessIndustry.TECH,
            valuation = 200_000,
            revenue = 100_000,
            employeeCount = 4,
            stage = CompanyStage.BOOTSTRAP,
            productQuality = 85,
            teamMorale = 90
        )
        val character = TestFixtures.character(
            age = 32,
            stats = Stats(money = 50_000, smarts = 80),
            businesses = listOf(business)
        )
        when (val result = engine.launchProductService(character, "b1")) {
            is BusinessResult.Success -> {
                val updated = result.character.businesses.first()
                assertTrue(updated.actionDoneThisYear)
                assertTrue(updated.productQuality >= 85)
            }
            is BusinessResult.Failed -> error("Expected launch success: ${result.reason}")
        }
    }

    @Test
    fun scaleExpand_hiresEmployeesAndIncreasesBurn() {
        val business = Business(
            id = "b1",
            name = "Cafe Empire",
            industry = BusinessIndustry.FOOD,
            valuation = 300_000,
            revenue = 120_000,
            employeeCount = 5,
            stage = CompanyStage.BOOTSTRAP
        )
        val character = TestFixtures.character(
            age = 35,
            stats = Stats(money = 500_000),
            businesses = listOf(business)
        )
        when (val result = engine.scaleExpand(character, "b1")) {
            is BusinessResult.Success -> {
                val updated = result.character.businesses.first()
                assertTrue(updated.employeeCount > 5)
                assertTrue(updated.revenue > 120_000)
                assertTrue(result.character.stats.money < 500_000)
                assertTrue(updated.actionDoneThisYear)
            }
            is BusinessResult.Failed -> error("Expected scale success: ${result.reason}")
        }
    }

    @Test
    fun pivotBusiness_changesIndustryAndRetainsEntity() {
        val business = Business(
            id = "b1",
            name = "Pivot Co",
            industry = BusinessIndustry.RETAIL,
            valuation = 150_000,
            revenue = 60_000,
            employeeCount = 3
        )
        val character = TestFixtures.character(
            age = 29,
            stats = Stats(money = 100_000),
            businesses = listOf(business)
        )
        when (val result = engine.pivotBusiness(character, "b1", BusinessIndustry.TECH)) {
            is BusinessResult.Success -> {
                val updated = result.character.businesses.first()
                assertEquals(BusinessIndustry.TECH, updated.industry)
                assertTrue(updated.actionDoneThisYear)
                assertTrue(result.character.stats.money < 100_000)
            }
            is BusinessResult.Failed -> error("Expected pivot success: ${result.reason}")
        }
    }

    @Test
    fun raiseCapital_dilutesEquityAndGrantsFounderLiquidity() {
        val business = Business(
            id = "b1",
            name = "Unicorn AI",
            industry = BusinessIndustry.TECH,
            valuation = 500_000,
            revenue = 150_000,
            employeeCount = 6,
            stage = CompanyStage.BOOTSTRAP,
            equityShare = 100
        )
        val character = TestFixtures.character(
            age = 33,
            stats = Stats(money = 10_000),
            businesses = listOf(business)
        )
        when (val result = engine.raiseCapital(character, "b1")) {
            is BusinessResult.Success -> {
                val updated = result.character.businesses.first()
                assertEquals(85, updated.equityShare)
                assertTrue(updated.valuation > 500_000)
                assertEquals(CompanyStage.SEED, updated.stage)
                assertTrue(result.character.stats.money > 10_000)
            }
            is BusinessResult.Failed -> error("Expected raise capital success: ${result.reason}")
        }
    }

    @Test
    fun liquidateBusiness_removesEntityAndGrantsLiquidationPayout() {
        val business = Business(
            id = "b1",
            name = "Struggling Co",
            industry = BusinessIndustry.RETAIL,
            valuation = 100_000,
            revenue = 10_000,
            employeeCount = 2,
            equityShare = 100
        )
        val character = TestFixtures.character(
            age = 38,
            stats = Stats(money = 5_000),
            businesses = listOf(business)
        )
        when (val result = engine.liquidateBusiness(character, "b1")) {
            is BusinessResult.Success -> {
                assertTrue(result.character.businesses.isEmpty())
                assertTrue(result.character.stats.money > 5_000)
            }
            is BusinessResult.Failed -> error("Expected liquidate success: ${result.reason}")
        }
    }

    @Test
    fun processBusinessYear_calculatesProfitAndAddsToCharacterCash() {
        val business = Business(
            id = "b1",
            name = "Corner Shop",
            industry = BusinessIndustry.RETAIL,
            valuation = 200_000,
            revenue = 100_000,
            employeeCount = 3,
            lastYearProfit = 0,
            annualBurn = 50_000,
            equityShare = 100
        )
        val character = TestFixtures.character(
            age = 35,
            stats = Stats(money = 50_000),
            businesses = listOf(business)
        )
        val after = engine.processBusinessYear(character)
        assertEquals(1, after.businesses.size)
        val updated = after.businesses.first()
        assertFalse(updated.actionDoneThisYear)
        assertTrue(updated.valuation >= 1_000)
        assertTrue(updated.annualBurn > 0)
    }

    @Test
    fun sellBusiness_removesBusinessAndAddsValuationToCash() {
        val business = Business(
            id = "b1",
            name = "Sold Co",
            industry = BusinessIndustry.FOOD,
            valuation = 250_000,
            revenue = 40_000,
            employeeCount = 5,
            equityShare = 100
        )
        val character = TestFixtures.character(
            age = 40,
            stats = Stats(money = 10_000),
            businesses = listOf(business)
        )
        when (val result = engine.sellBusiness(character, "b1")) {
            is BusinessResult.Success -> {
                assertTrue(result.character.businesses.isEmpty())
                assertEquals(260_000, result.character.stats.money)
            }
            is BusinessResult.Failed -> error("Expected sell success: ${result.reason}")
        }
    }
}
