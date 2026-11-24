// app/src/main/java/com/maisha/game/domain/BusinessEngine.kt
package com.maisha.game.domain

import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.Business
import com.maisha.game.data.model.BusinessIndustry
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.CompanyStage
import com.maisha.game.data.model.EconomicClimate
import com.maisha.game.util.clampStat
import com.maisha.game.util.formatMoney
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlin.random.Random

sealed class BusinessResult {
    data class Success(
        val character: Character,
        val business: Business? = null,
        val message: String? = null,
        val cashDelta: Int = 0
    ) : BusinessResult()
    data class Failed(val reason: BusinessFailure) : BusinessResult()
}

enum class BusinessFailure {
    INELIGIBLE,
    INSUFFICIENT_FUNDS,
    MAX_BUSINESSES,
    NOT_FOUND,
    INVALID_INVESTMENT,
    ACTION_ALREADY_DONE,
    CANNOT_RAISE_CAPITAL,
    CANNOT_PIVOT
}

@Singleton
class BusinessEngine @Inject constructor(
    private val financeEngine: FinanceEngine
) {

    fun startBusiness(
        character: Character,
        name: String,
        industry: BusinessIndustry,
        initialInvestment: Int
    ): BusinessResult {
        if (!canStartBusiness(character)) {
            return BusinessResult.Failed(BusinessFailure.INELIGIBLE)
        }
        if (character.businesses.size >= MAX_BUSINESSES) {
            return BusinessResult.Failed(BusinessFailure.MAX_BUSINESSES)
        }
        if (initialInvestment < minInvestment(character)) {
            return BusinessResult.Failed(BusinessFailure.INVALID_INVESTMENT)
        }
        if (character.stats.money < initialInvestment) {
            return BusinessResult.Failed(BusinessFailure.INSUFFICIENT_FUNDS)
        }

        val smartsFactor = 0.5f + (character.stats.smarts.coerceIn(0, 100) / 100f) * 0.5f
        val industryFactor = industryRevenueFactor(industry)
        val startingRevenue = (initialInvestment * 0.22f * smartsFactor * industryFactor)
            .roundToInt()
            .coerceAtLeast(1_000)
        val startingValuation = (initialInvestment * (0.95f + smartsFactor * 0.4f))
            .roundToInt()
            .coerceAtLeast(initialInvestment)
        val employees = (initialInvestment / employeeCapitalPerHead(character))
            .coerceIn(1, 50)
        val initialBurn = (startingRevenue * 0.75f).roundToInt()

        val business = Business(
            id = UUID.randomUUID().toString(),
            name = name.trim().ifEmpty { defaultName(industry) },
            industry = industry,
            valuation = startingValuation,
            revenue = startingRevenue,
            employeeCount = employees,
            lastYearProfit = 0,
            stage = CompanyStage.BOOTSTRAP,
            annualBurn = initialBurn,
            equityShare = 100,
            teamMorale = 80,
            productQuality = (50 + (character.stats.smarts / 4)).coerceIn(50, 90),
            actionDoneThisYear = false
        )

        val withCash = financeEngine.applyBusinessInvestment(character, initialInvestment)
        val updated = withCash.copy(
            businesses = withCash.businesses + business,
            eventLog = EventLogCap.prepend(
                withCash.eventLog,
                "Founded ${business.name} (${industryLabel(industry)}) with ${formatMoney(initialInvestment, character.countryCode)}."
            )
        )
        return BusinessResult.Success(
            character = updated,
            business = business,
            message = "Founded ${business.name}",
            cashDelta = -initialInvestment
        )
    }

    fun launchProductService(character: Character, businessId: String): BusinessResult {
        val business = character.businesses.find { it.id == businessId }
            ?: return BusinessResult.Failed(BusinessFailure.NOT_FOUND)
        if (!character.alive) return BusinessResult.Failed(BusinessFailure.INELIGIBLE)
        if (business.actionDoneThisYear) return BusinessResult.Failed(BusinessFailure.ACTION_ALREADY_DONE)

        val successScore = (business.productQuality * 0.45f) +
            (business.teamMorale * 0.35f) +
            (character.stats.smarts * 0.20f)
        val isHit = Random.nextFloat() < (successScore / 100f).coerceIn(0.20f, 0.90f)

        val updatedBusiness = if (isHit) {
            val revenueBoost = (business.revenue * (0.25f + Random.nextFloat() * 0.20f)).roundToInt()
            val valuationBoost = (business.valuation * (0.20f + Random.nextFloat() * 0.25f)).roundToInt()
            business.copy(
                revenue = business.revenue + revenueBoost,
                valuation = business.valuation + valuationBoost,
                productQuality = (business.productQuality + 6).coerceAtMost(100),
                teamMorale = (business.teamMorale + 8).coerceAtMost(100),
                actionDoneThisYear = true
            )
        } else {
            business.copy(
                productQuality = (business.productQuality + 2).coerceAtMost(100),
                teamMorale = (business.teamMorale - 10).coerceAtLeast(10),
                actionDoneThisYear = true
            )
        }

        val logText = if (isHit) {
            "${business.name} launched a breakthrough product! Customer adoption and revenue surged."
        } else {
            "${business.name} launched a new release to mixed reviews and operational bugs. Morale took a hit."
        }

        val updated = character.copy(
            businesses = character.businesses.map { if (it.id == businessId) updatedBusiness else it },
            eventLog = EventLogCap.prepend(character.eventLog, logText)
        )
        return BusinessResult.Success(
            character = updated,
            business = updatedBusiness,
            message = logText
        )
    }

    fun scaleExpand(character: Character, businessId: String): BusinessResult {
        val business = character.businesses.find { it.id == businessId }
            ?: return BusinessResult.Failed(BusinessFailure.NOT_FOUND)
        if (!character.alive) return BusinessResult.Failed(BusinessFailure.INELIGIBLE)
        if (business.actionDoneThisYear) return BusinessResult.Failed(BusinessFailure.ACTION_ALREADY_DONE)

        val cost = scaleCost(character, business.stage)
        if (character.stats.money < cost) {
            return BusinessResult.Failed(BusinessFailure.INSUFFICIENT_FUNDS)
        }

        val withCash = financeEngine.applyBusinessInvestment(character, cost)
        val newEmployees = business.employeeCount + Random.nextInt(4, 12)
        val newRevenue = (business.revenue * (1.25f + Random.nextFloat() * 0.15f)).roundToInt()
        val newValuation = (business.valuation * (1.20f + Random.nextFloat() * 0.15f)).roundToInt()
        val newBurn = calculateAnnualBurn(business.copy(employeeCount = newEmployees, revenue = newRevenue), character.countryCode)
        val newStage = computeNextStage(newValuation, newRevenue, business.stage, character.countryCode)

        val updatedBusiness = business.copy(
            employeeCount = newEmployees,
            revenue = newRevenue,
            valuation = newValuation,
            annualBurn = newBurn,
            stage = newStage,
            teamMorale = (business.teamMorale + 5).coerceAtMost(100),
            actionDoneThisYear = true
        )

        val logText = "${business.name} scaled operations and hired key talent for ${formatMoney(cost, character.countryCode)}."
        val updated = withCash.copy(
            businesses = withCash.businesses.map { if (it.id == businessId) updatedBusiness else it },
            eventLog = EventLogCap.prepend(withCash.eventLog, logText)
        )
        return BusinessResult.Success(
            character = updated,
            business = updatedBusiness,
            message = logText,
            cashDelta = -cost
        )
    }

    fun pivotBusiness(
        character: Character,
        businessId: String,
        newIndustry: BusinessIndustry
    ): BusinessResult {
        val business = character.businesses.find { it.id == businessId }
            ?: return BusinessResult.Failed(BusinessFailure.NOT_FOUND)
        if (!character.alive) return BusinessResult.Failed(BusinessFailure.INELIGIBLE)
        if (business.industry == newIndustry) return BusinessResult.Failed(BusinessFailure.CANNOT_PIVOT)
        if (business.actionDoneThisYear) return BusinessResult.Failed(BusinessFailure.ACTION_ALREADY_DONE)

        val cost = pivotCost(character)
        if (character.stats.money < cost) {
            return BusinessResult.Failed(BusinessFailure.INSUFFICIENT_FUNDS)
        }

        val withCash = financeEngine.applyBusinessInvestment(character, cost)
        val updatedBusiness = business.copy(
            industry = newIndustry,
            productQuality = 55,
            teamMorale = (business.teamMorale + 15).coerceIn(40, 95),
            actionDoneThisYear = true
        )

        val logText = "${business.name} pivoted into ${industryLabel(newIndustry)} for ${formatMoney(cost, character.countryCode)}."
        val updated = withCash.copy(
            businesses = withCash.businesses.map { if (it.id == businessId) updatedBusiness else it },
            eventLog = EventLogCap.prepend(withCash.eventLog, logText)
        )
        return BusinessResult.Success(
            character = updated,
            business = updatedBusiness,
            message = logText,
            cashDelta = -cost
        )
    }

    fun raiseCapital(character: Character, businessId: String): BusinessResult {
        val business = character.businesses.find { it.id == businessId }
            ?: return BusinessResult.Failed(BusinessFailure.NOT_FOUND)
        if (!character.alive) return BusinessResult.Failed(BusinessFailure.INELIGIBLE)
        if (business.actionDoneThisYear) return BusinessResult.Failed(BusinessFailure.ACTION_ALREADY_DONE)
        if (business.equityShare < 25 || business.stage == CompanyStage.ENTERPRISE) {
            return BusinessResult.Failed(BusinessFailure.CANNOT_RAISE_CAPITAL)
        }

        val dilution = 15
        val newEquity = business.equityShare - dilution
        val valuationBoost = (business.valuation * (1.50f + Random.nextFloat() * 0.40f)).roundToInt()
        val founderLiquidity = ((valuationBoost * 0.04f).roundToInt())
            .coerceAtLeast(EconomyScaler.scaleAmount(15_000, character.countryCode))
        val nextStage = when (business.stage) {
            CompanyStage.BOOTSTRAP -> CompanyStage.SEED
            CompanyStage.SEED -> CompanyStage.GROWTH
            CompanyStage.GROWTH -> CompanyStage.EXPANSION
            CompanyStage.EXPANSION -> CompanyStage.ENTERPRISE
            CompanyStage.ENTERPRISE -> CompanyStage.ENTERPRISE
        }

        val withCash = financeEngine.applyBusinessSale(character, founderLiquidity)
        val updatedBusiness = business.copy(
            valuation = valuationBoost,
            equityShare = newEquity,
            stage = nextStage,
            teamMorale = 95,
            actionDoneThisYear = true
        )

        val logText = "${business.name} closed a funding round! Valuation jumped to ${formatMoney(valuationBoost, character.countryCode)}. Sold $dilution% equity for ${formatMoney(founderLiquidity, character.countryCode)} in founder liquidity."
        val updated = withCash.copy(
            businesses = withCash.businesses.map { if (it.id == businessId) updatedBusiness else it },
            eventLog = EventLogCap.prepend(withCash.eventLog, logText)
        )
        return BusinessResult.Success(
            character = updated,
            business = updatedBusiness,
            message = logText,
            cashDelta = founderLiquidity
        )
    }

    fun sellBusiness(character: Character, businessId: String): BusinessResult {
        val business = character.businesses.find { it.id == businessId }
            ?: return BusinessResult.Failed(BusinessFailure.NOT_FOUND)
        if (!character.alive) {
            return BusinessResult.Failed(BusinessFailure.INELIGIBLE)
        }

        val netProceeds = ((business.valuation * (business.equityShare / 100f))).roundToInt()
            .coerceAtLeast(1_000)
        val withCash = financeEngine.applyBusinessSale(character, netProceeds)
        val updated = withCash.copy(
            businesses = withCash.businesses.filterNot { it.id == businessId },
            eventLog = EventLogCap.prepend(
                withCash.eventLog,
                "Sold ${business.name} (${business.equityShare}% stake) for ${formatMoney(netProceeds, character.countryCode)}."
            )
        )
        return BusinessResult.Success(
            character = updated,
            business = business,
            message = "Sold ${business.name}",
            cashDelta = netProceeds
        )
    }

    fun liquidateBusiness(character: Character, businessId: String): BusinessResult {
        val business = character.businesses.find { it.id == businessId }
            ?: return BusinessResult.Failed(BusinessFailure.NOT_FOUND)
        if (!character.alive) return BusinessResult.Failed(BusinessFailure.INELIGIBLE)

        val liquidationPayout = ((business.valuation * 0.20f) * (business.equityShare / 100f)).roundToInt()
            .coerceAtLeast(500)
        val withCash = financeEngine.applyBusinessSale(character, liquidationPayout)
        val updated = withCash.copy(
            businesses = withCash.businesses.filterNot { it.id == businessId },
            eventLog = EventLogCap.prepend(
                withCash.eventLog,
                "Liquidated ${business.name}. Shut down operations and recovered ${formatMoney(liquidationPayout, character.countryCode)} from residual assets."
            )
        )
        return BusinessResult.Success(
            character = updated,
            business = business,
            message = "Liquidated ${business.name}",
            cashDelta = liquidationPayout
        )
    }

    /**
     * Yearly P&L, burn rate, valuation drift from economic climate, stage updates, and employee churn.
     */
    fun processBusinessYear(character: Character): Character {
        if (character.businesses.isEmpty()) return character

        var money = character.stats.money
        var happiness = character.stats.happiness
        val climate = character.economicState.climate
        val marketModifier = character.economicState.marketModifier
        val updatedBusinesses = mutableListOf<Business>()
        var log = character.eventLog

        val policyMultiplier = EconomyScaler.policyBusinessRevenueMultiplier(
            character.politics.activeTaxPolicy
        )
        for (business in character.businesses) {
            val annualBurn = calculateAnnualBurn(business, character.countryCode)
            val profit = (calculateYearlyProfit(business, annualBurn, climate) * policyMultiplier)
                .roundToInt()
            val playerProfitShare = (profit * (business.equityShare / 100f)).roundToInt()
            money += playerProfitShare

            val valuationDrift = valuationMultiplier(climate, marketModifier)
            val newValuation = (business.valuation * valuationDrift)
                .roundToInt()
                .coerceAtLeast(1_000)
            val revenueDrift = 0.92f + Random.nextFloat() * 0.2f +
                if (profit >= 0) 0.05f else -0.08f
            val newRevenue = (business.revenue * revenueDrift * policyMultiplier)
                .roundToInt()
                .coerceAtLeast(500)
            val employeeDelta = Random.nextInt(-2, 4)
            val newEmployees = (business.employeeCount + employeeDelta).coerceIn(1, 200)

            val newMorale = if (profit >= 0) {
                (business.teamMorale + 4).coerceAtMost(100)
            } else {
                (business.teamMorale - 7).coerceAtLeast(15)
            }
            val newStage = computeNextStage(newValuation, newRevenue, business.stage, character.countryCode)

            val updatedBiz = business.copy(
                valuation = newValuation,
                revenue = newRevenue,
                employeeCount = newEmployees,
                lastYearProfit = profit,
                annualBurn = annualBurn,
                teamMorale = newMorale,
                stage = newStage,
                actionDoneThisYear = false
            )
            updatedBusinesses += updatedBiz

            val profitLabel = if (profit >= 0) {
                "profit of ${formatMoney(profit, character.countryCode)}"
            } else {
                "loss of ${formatMoney(-profit, character.countryCode)}"
            }
            log = EventLogCap.prepend(
                log,
                "${business.name} posted a $profitLabel."
            )
        }

        if (money < 0) {
            happiness = clampStat(happiness - BUSINESS_DEBT_HAPPINESS_PENALTY)
            money = 0
        }

        return character.copy(
            businesses = updatedBusinesses,
            stats = character.stats.copy(money = money, happiness = happiness),
            eventLog = log
        )
    }

    fun canStartBusiness(character: Character): Boolean =
        character.alive &&
            character.age >= MIN_BUSINESS_AGE &&
            !character.criminalRecord.currentlyIncarcerated &&
            !character.criminalRecord.awaitingTrial

    fun minInvestment(character: Character): Int =
        EconomyScaler.scaleAmount(MIN_INVESTMENT_KENYA, character.countryCode)

    fun investmentTiers(character: Character): List<Int> = listOf(
        EconomyScaler.scaleAmount(INVESTMENT_SMALL_KENYA, character.countryCode),
        EconomyScaler.scaleAmount(INVESTMENT_MEDIUM_KENYA, character.countryCode),
        EconomyScaler.scaleAmount(INVESTMENT_LARGE_KENYA, character.countryCode)
    )

    fun scaleCost(character: Character, stage: CompanyStage): Int =
        EconomyScaler.scaleAmount(25_000, character.countryCode) * (stage.ordinal + 1)

    fun pivotCost(character: Character): Int =
        EconomyScaler.scaleAmount(15_000, character.countryCode)

    fun applyBusinessEffects(
        character: Character,
        valuationDelta: Int,
        revenueDelta: Int
    ): Character {
        if (character.businesses.isEmpty()) return character
        if (valuationDelta == 0 && revenueDelta == 0) return character
        val updated = character.businesses.map { business ->
            business.copy(
                valuation = (business.valuation + valuationDelta).coerceAtLeast(1_000),
                revenue = (business.revenue + revenueDelta).coerceAtLeast(500)
            )
        }
        return character.copy(businesses = updated)
    }

    fun calculateAnnualBurn(business: Business, countryCode: String): Int {
        val employeeHeadCost = EconomyScaler.scaleAmount(25_000, countryCode)
        val payroll = business.employeeCount * employeeHeadCost
        val overhead = (business.revenue * 0.35f).roundToInt()
        return (payroll + overhead).coerceAtLeast(employeeHeadCost)
    }

    private fun computeNextStage(
        valuation: Int,
        revenue: Int,
        currentStage: CompanyStage,
        countryCode: String
    ): CompanyStage {
        val seedThreshold = EconomyScaler.scaleAmount(120_000, countryCode)
        val growthThreshold = EconomyScaler.scaleAmount(450_000, countryCode)
        val expansionThreshold = EconomyScaler.scaleAmount(1_800_000, countryCode)
        val enterpriseThreshold = EconomyScaler.scaleAmount(8_000_000, countryCode)

        return when {
            valuation >= enterpriseThreshold || revenue >= enterpriseThreshold / 5 -> CompanyStage.ENTERPRISE
            valuation >= expansionThreshold || revenue >= expansionThreshold / 5 -> CompanyStage.EXPANSION
            valuation >= growthThreshold || revenue >= growthThreshold / 5 -> CompanyStage.GROWTH
            valuation >= seedThreshold || revenue >= seedThreshold / 5 -> CompanyStage.SEED
            else -> currentStage
        }
    }

    private fun calculateYearlyProfit(
        business: Business,
        annualBurn: Int,
        climate: EconomicClimate
    ): Int {
        val baseMargin = when (climate) {
            EconomicClimate.BOOM -> 0.22f + Random.nextFloat() * 0.12f
            EconomicClimate.STEADY -> 0.10f + Random.nextFloat() * 0.12f
            EconomicClimate.INFLATION_SURGE -> 0.02f + Random.nextFloat() * 0.08f
            EconomicClimate.RECESSION -> -0.08f + Random.nextFloat() * 0.10f
        }
        val qualityBoost = ((business.productQuality - 50) / 100f) * 0.10f
        val moraleBoost = ((business.teamMorale - 50) / 100f) * 0.08f
        val netRevenue = (business.revenue * (1f + qualityBoost + moraleBoost)).roundToInt()
        val profit = netRevenue - annualBurn + (business.revenue * baseMargin).roundToInt()
        val noise = Random.nextInt(-business.revenue / 25, business.revenue / 20 + 1)
        return profit + noise
    }

    private fun valuationMultiplier(climate: EconomicClimate, marketModifier: Float): Float {
        val climateBoost = when (climate) {
            EconomicClimate.BOOM -> 1.08f
            EconomicClimate.STEADY -> 1.0f
            EconomicClimate.INFLATION_SURGE -> 0.95f
            EconomicClimate.RECESSION -> 0.88f
        }
        val noise = 0.94f + Random.nextFloat() * 0.14f
        return (climateBoost * marketModifier.coerceIn(0.8f, 1.2f) * noise).coerceIn(0.75f, 1.35f)
    }

    private fun industryRevenueFactor(industry: BusinessIndustry): Float = when (industry) {
        BusinessIndustry.TECH -> 1.15f
        BusinessIndustry.REAL_ESTATE -> 1.05f
        BusinessIndustry.ENTERTAINMENT -> 1.10f
        BusinessIndustry.RETAIL -> 0.95f
        BusinessIndustry.FOOD -> 0.90f
    }

    private fun employeeCapitalPerHead(character: Character): Int =
        EconomyScaler.scaleAmount(50_000, character.countryCode).coerceAtLeast(10_000)

    private fun defaultName(industry: BusinessIndustry): String = when (industry) {
        BusinessIndustry.TECH -> "Nova Tech"
        BusinessIndustry.RETAIL -> "Corner Market Co."
        BusinessIndustry.FOOD -> "Home Kitchen Ltd"
        BusinessIndustry.REAL_ESTATE -> "Horizon Properties"
        BusinessIndustry.ENTERTAINMENT -> "Spotlight Media"
    }

    fun industryLabel(industry: BusinessIndustry): String = when (industry) {
        BusinessIndustry.TECH -> "Tech"
        BusinessIndustry.RETAIL -> "Retail"
        BusinessIndustry.FOOD -> "Food"
        BusinessIndustry.REAL_ESTATE -> "Real Estate"
        BusinessIndustry.ENTERTAINMENT -> "Entertainment"
    }

    companion object {
        const val REQUIRES_BUSINESS_TAG = "requires_business"
        const val MAX_BUSINESSES = 3
        const val MIN_BUSINESS_AGE = 18
        const val MIN_INVESTMENT_KENYA = 50_000
        const val INVESTMENT_SMALL_KENYA = 50_000
        const val INVESTMENT_MEDIUM_KENYA = 150_000
        const val INVESTMENT_LARGE_KENYA = 500_000
        private const val BUSINESS_DEBT_HAPPINESS_PENALTY = 5
    }
}
