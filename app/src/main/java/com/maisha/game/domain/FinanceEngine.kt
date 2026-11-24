// app/src/main/java/com/maisha/game/domain/FinanceEngine.kt
package com.maisha.game.domain

import com.maisha.game.data.AssetCatalog
import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.PetCatalog
import com.maisha.game.data.content.CarMakeCatalog
import com.maisha.game.data.content.FallbackCarMakeCatalog
import com.maisha.game.data.content.FallbackStreetCatalog
import com.maisha.game.data.content.StreetCatalog
import com.maisha.game.data.model.AnnualCashFlow
import com.maisha.game.data.model.Asset
import com.maisha.game.data.model.AssetType
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.CreditRatingTier
import com.maisha.game.data.model.EconomicClimate
import com.maisha.game.data.model.EconomicState
import com.maisha.game.data.model.PersonalFinanceState
import com.maisha.game.data.model.PortfolioStrategy
import com.maisha.game.data.model.PoliticalOffice
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.StudentFinance
import com.maisha.game.data.model.StudentLiabilitySnapshot
import com.maisha.game.data.model.TaxPolicyType
import com.maisha.game.util.formatMoney
import java.util.UUID
import com.maisha.game.util.clampCondition
import com.maisha.game.util.clampStat
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlin.random.Random

sealed class PurchaseResult {
    data class Success(val character: Character) : PurchaseResult()
    data object InsufficientFunds : PurchaseResult()
}

sealed class RepairResult {
    data class Success(val character: Character) : RepairResult()
    data object InsufficientFunds : RepairResult()
    data object AssetNotFound : RepairResult()
}

sealed class RenovateResult {
    data class Success(val character: Character) : RenovateResult()
    data object InsufficientFunds : RenovateResult()
    data object AssetNotFound : RenovateResult()
    data object NotEligible : RenovateResult()
}

data class EconomicShiftResult(
    val character: Character,
    val climate: EconomicClimate
)

@Singleton
class FinanceEngine @Inject constructor(
    private val carMakeCatalog: CarMakeCatalog,
    private val streetCatalog: StreetCatalog
) {
    constructor(carMakeCatalog: CarMakeCatalog) : this(carMakeCatalog, FallbackStreetCatalog)
    constructor() : this(FallbackCarMakeCatalog, FallbackStreetCatalog)

    /**
     * Buys a catalog asset scaled to [Character.countryCode]; deducts scaled price from money.
     *
     * @return [PurchaseResult.InsufficientFunds] if unknown id or not enough cash.
     */
    fun purchaseAsset(character: Character, assetCatalogId: String): PurchaseResult {
        val catalogItem = AssetCatalog.findById(assetCatalogId)
            ?: return PurchaseResult.InsufficientFunds
        if (!catalogItem.isPurchasable || catalogItem.isHeirloom) {
            return PurchaseResult.InsufficientFunds
        }

        val purchasePrice = EconomyScaler.scaleAmount(catalogItem.purchasePrice, character.countryCode)
        val monthlyUpkeep = EconomyScaler.scaleAmount(catalogItem.monthlyUpkeep, character.countryCode)

        if (character.stats.money < purchasePrice) {
            return PurchaseResult.InsufficientFunds
        }

        val displayName = AssetCatalog.withPropertyListings(
            character.countryCode,
            carMakeCatalog,
            streetCatalog
        ).find { it.id == catalogItem.id }?.name ?: catalogItem.name
        val asset = Asset(
            id = UUID.randomUUID().toString(),
            catalogId = catalogItem.id,
            type = catalogItem.type,
            name = displayName,
            purchasePrice = purchasePrice,
            currentValue = purchasePrice,
            condition = 100,
            monthlyUpkeep = monthlyUpkeep
        )

        val updatedCharacter = character.copy(
            stats = character.stats.copy(
                money = character.stats.money - purchasePrice
            ),
            assets = character.assets + asset
        )
        return PurchaseResult.Success(updatedCharacter)
    }

    /** Sells [assetId] at [Asset.currentValue] and removes it from [Character.assets]. Heirlooms cannot be sold. */
    fun sellAsset(character: Character, assetId: String): Character {
        val asset = character.assets.find { it.id == assetId } ?: return character
        if (asset.isHeirloom) return character
        // If asset has a mortgage, pay it off from the sale proceeds
        val saleProceeds = (asset.currentValue - asset.mortgageBalance).coerceAtLeast(0)
        return character.copy(
            stats = character.stats.copy(
                money = character.stats.money + saleProceeds
            ),
            assets = character.assets.filterNot { it.id == assetId },
            eventLog = if (asset.mortgageBalance > 0) EventLogCap.prepend(
                character.eventLog,
                "Sold ${asset.name} for ${formatMoney(asset.currentValue, character.countryCode)}, " +
                    "settling ${formatMoney(asset.mortgageBalance, character.countryCode)} mortgage."
            ) else character.eventLog
        )
    }

    sealed class MortgagePurchaseResult {
        data class Success(val character: Character) : MortgagePurchaseResult()
        data object InsufficientFunds : MortgagePurchaseResult()
        data object NotEligible : MortgagePurchaseResult()
        data class RejectedCredit(val creditScore: Int) : MortgagePurchaseResult()
    }

    /** Minimum down-payment percentage gated by credit rating tier. */
    fun minDownPaymentPercentForCredit(creditScore: Int): Int = when (CreditRatingTier.forScore(creditScore)) {
        CreditRatingTier.EXCELLENT -> 10
        CreditRatingTier.GOOD -> 20
        CreditRatingTier.FAIR -> 25
        CreditRatingTier.POOR -> 40
    }

    /** Base mortgage APR gated by credit rating tier. */
    fun baseMortgageAprForCredit(creditScore: Int): Double = when (CreditRatingTier.forScore(creditScore)) {
        CreditRatingTier.EXCELLENT -> 0.055
        CreditRatingTier.GOOD -> 0.075
        CreditRatingTier.FAIR -> 0.095
        CreditRatingTier.POOR -> 0.120
    }

    /**
     * Purchases a house with a credit-gated down payment and interest rate.
     * Requires credit score >= 500. Lower credit scores require higher down payments and APRs.
     * Monthly payment = P * r / (1 - (1+r)^-n), where r = annual_rate/12, n = 360.
     */
    fun purchaseWithMortgage(
        character: Character,
        assetCatalogId: String,
        downPaymentPercent: Int = 20
    ): MortgagePurchaseResult {
        val catalogItem = AssetCatalog.findById(assetCatalogId)
            ?: return MortgagePurchaseResult.NotEligible
        if (catalogItem.type != AssetType.HOUSE || !catalogItem.isPurchasable || catalogItem.isHeirloom) {
            return MortgagePurchaseResult.NotEligible
        }
        if (character.finance.creditScore < 500) {
            return MortgagePurchaseResult.RejectedCredit(character.finance.creditScore)
        }
        val minDown = minDownPaymentPercentForCredit(character.finance.creditScore)
        val pct = maxOf(downPaymentPercent, minDown).coerceIn(minDown, 100)
        val purchasePrice = EconomyScaler.scaleAmount(catalogItem.purchasePrice, character.countryCode)
        val downPayment = (purchasePrice * pct / 100.0).toInt()

        if (character.stats.money < downPayment) return MortgagePurchaseResult.InsufficientFunds

        val principal = purchasePrice - downPayment
        val standingTier = character.socialEcosystem.currentStandingTier
        val standingDiscount = when (standingTier) {
            com.maisha.game.data.model.CommunityStandingTier.PILLAR_OF_SOCIETY -> 0.005
            com.maisha.game.data.model.CommunityStandingTier.WELL_RESPECTED -> 0.0025
            else -> 0.0
        }
        val baseRate = baseMortgageAprForCredit(character.finance.creditScore)
        val effectiveRate = (baseRate - standingDiscount).coerceAtLeast(0.035)
        val monthlyRate = effectiveRate / 12.0
        val annualPayment = if (principal <= 0) 0 else {
            val monthly = principal * monthlyRate /
                (1.0 - Math.pow(1.0 + monthlyRate, -MORTGAGE_TERM_MONTHS.toDouble()))
            (monthly * 12).toInt()
        }

        val displayName = AssetCatalog.withPropertyListings(
            character.countryCode, carMakeCatalog, streetCatalog
        ).find { it.id == catalogItem.id }?.name ?: catalogItem.name

        val asset = Asset(
            id = UUID.randomUUID().toString(),
            catalogId = catalogItem.id,
            type = catalogItem.type,
            name = displayName,
            purchasePrice = purchasePrice,
            currentValue = purchasePrice,
            condition = 100,
            monthlyUpkeep = EconomyScaler.scaleAmount(catalogItem.monthlyUpkeep, character.countryCode),
            mortgageBalance = principal,
            annualMortgagePayment = annualPayment,
            mortgageInterestRate = effectiveRate
        )

        val tierLabel = CreditRatingTier.forScore(character.finance.creditScore).name.lowercase()
        val logEntry = if (principal > 0) {
            "Bought ${asset.name} with a ${pct}% down payment " +
                "(${formatMoney(downPayment, character.countryCode)}). " +
                "Mortgage: ${formatMoney(principal, character.countryCode)} over 30 years at ${(effectiveRate * 1000).roundToInt() / 10.0}% APR ($tierLabel credit rating)."
        } else {
            "Bought ${asset.name} outright for ${formatMoney(purchasePrice, character.countryCode)}."
        }

        return MortgagePurchaseResult.Success(
            character.copy(
                stats = character.stats.copy(money = character.stats.money - downPayment),
                assets = character.assets + asset,
                eventLog = EventLogCap.prepend(character.eventLog, logEntry)
            )
        )
    }

    /**
     * Pays annual mortgage installments on all mortgaged properties.
     * Deducts [Asset.annualMortgagePayment] from cash, reduces [Asset.mortgageBalance].
     * When balance hits 0 the mortgage is cleared.
     */
    fun payMortgages(character: Character): Character {
        val mortgaged = character.assets.filter { it.mortgageBalance > 0 && it.annualMortgagePayment > 0 }
        if (mortgaged.isEmpty()) return character

        var cash = character.stats.money
        val updatedAssets = character.assets.map { asset ->
            if (asset.mortgageBalance <= 0 || asset.annualMortgagePayment <= 0) return@map asset
            val payment = minOf(asset.annualMortgagePayment, asset.mortgageBalance)
            cash -= payment
            val newBalance = (asset.mortgageBalance - payment).coerceAtLeast(0)
            asset.copy(mortgageBalance = newBalance, annualMortgagePayment = if (newBalance == 0) 0 else asset.annualMortgagePayment)
        }

        val totalPaid = character.stats.money - cash.coerceAtLeast(0)
        val paidOffNames = character.assets.zip(updatedAssets)
            .filter { (old, new) -> old.mortgageBalance > 0 && new.mortgageBalance == 0 }
            .map { it.second.name }

        var eventLog = character.eventLog
        if (totalPaid > 0) {
            eventLog = EventLogCap.prepend(eventLog,
                "Paid ${formatMoney(totalPaid, character.countryCode)} in mortgage installments.")
        }
        paidOffNames.forEach { name ->
            eventLog = EventLogCap.prepend(eventLog, "Mortgage fully paid off on $name!")
        }

        val updatedCredit = if (paidOffNames.isNotEmpty()) {
            (character.finance.creditScore + 20 * paidOffNames.size).coerceAtMost(850)
        } else {
            character.finance.creditScore
        }

        return character.copy(
            stats = character.stats.copy(money = cash.coerceAtLeast(0)),
            assets = updatedAssets,
            finance = character.finance.copy(creditScore = updatedCredit),
            eventLog = eventLog
        )
    }

    /**
     * Pays off the entire remaining mortgage on a property if player has sufficient funds.
     */
    fun payOffMortgage(character: Character, assetId: String): Character {
        val asset = character.assets.find { it.id == assetId } ?: return character
        if (asset.mortgageBalance <= 0) return character
        val payoff = asset.mortgageBalance
        if (character.stats.money < payoff) return character

        val updatedAssets = character.assets.map {
            if (it.id == assetId) it.copy(mortgageBalance = 0, annualMortgagePayment = 0)
            else it
        }
        val message = "Paid off the mortgage on ${asset.name} in full (${formatMoney(payoff, character.countryCode)})."
        val boostedCredit = (character.finance.creditScore + 25).coerceAtMost(850)
        return character.copy(
            stats = character.stats.copy(money = character.stats.money - payoff),
            assets = updatedAssets,
            finance = character.finance.copy(creditScore = boostedCredit),
            eventLog = EventLogCap.prependToCurrentYear(character.eventLog, message, character.age)
        )
    }

    /**
     * Prepays an extra principal lump sum against an asset's mortgage balance.
     * Recalculates the annual mortgage payment downwards proportionally.
     */
    fun prepayMortgagePrincipal(character: Character, assetId: String, amount: Int): Character {
        if (amount <= 0) return character
        val asset = character.assets.find { it.id == assetId } ?: return character
        if (asset.mortgageBalance <= 0) return character
        val actualPayment = minOf(amount, asset.mortgageBalance)
        if (character.stats.money < actualPayment) return character

        val remainingBalance = asset.mortgageBalance - actualPayment
        val newAnnualPayment = if (remainingBalance <= 0) {
            0
        } else {
            (asset.annualMortgagePayment * (remainingBalance.toDouble() / asset.mortgageBalance.toDouble()))
                .roundToInt().coerceAtLeast(1)
        }

        val updatedAssets = character.assets.map {
            if (it.id == assetId) it.copy(
                mortgageBalance = remainingBalance,
                annualMortgagePayment = newAnnualPayment
            ) else it
        }
        val message = if (remainingBalance <= 0) {
            "Prepaid remaining principal on ${asset.name} (${formatMoney(actualPayment, character.countryCode)}). Mortgage fully cleared!"
        } else {
            "Prepaid ${formatMoney(actualPayment, character.countryCode)} toward ${asset.name}'s mortgage principal (Remaining: ${formatMoney(remainingBalance, character.countryCode)})."
        }
        val boostedCredit = (character.finance.creditScore + (if (remainingBalance <= 0) 25 else 15)).coerceAtMost(850)
        return character.copy(
            stats = character.stats.copy(money = character.stats.money - actualPayment),
            assets = updatedAssets,
            finance = character.finance.copy(creditScore = boostedCredit),
            eventLog = EventLogCap.prependToCurrentYear(character.eventLog, message, character.age)
        )
    }

    /**
     * Preventative maintenance / professional servicing for a vehicle or property.
     * Restores condition and marks [Asset.servicedThisYear] to cut degradation and prevent critical failures.
     */
    fun serviceAsset(character: Character, assetId: String): Character {
        val asset = character.assets.find { it.id == assetId } ?: return character
        if (asset.isHeirloom || asset.servicedThisYear) return character
        val serviceCost = (calculateRepairCost(asset, character.countryCode) * 0.4f).roundToInt().coerceAtLeast(1_500)
        if (character.stats.money < serviceCost) return character

        val updatedCondition = (asset.condition + 15).coerceIn(0, 100)
        val serviced = recalculateValue(
            asset.copy(condition = updatedCondition, servicedThisYear = true),
            character.economicState.marketModifier
        )
        val updatedAssets = character.assets.map { if (it.id == assetId) serviced else it }
        val message = "Performed scheduled preventative servicing on ${asset.name} (${formatMoney(serviceCost, character.countryCode)}). Condition is now $updatedCondition%."
        return character.copy(
            stats = character.stats.copy(money = character.stats.money - serviceCost),
            assets = updatedAssets,
            eventLog = EventLogCap.prependToCurrentYear(character.eventLog, message, character.age)
        )
    }

    /**
     * Deducts annual upkeep (12× monthly, with multi-asset discount and economic climate).
     * If money goes negative, floors at 0 and applies a happiness penalty.
     */
    fun applyUpkeep(character: Character): Character {
        if (character.assets.isEmpty()) return character

        val rawAnnual = character.assets.sumOf { it.monthlyUpkeep * 12 }
        val stackDiscount = when {
            character.assets.size <= 1 -> 1.0
            character.assets.size == 2 -> 0.95
            character.assets.size == 3 -> 0.90
            else -> 0.85
        }
        val climateMultiplier = upkeepMultiplier(character.economicState.climate)
        val annualUpkeep = (rawAnnual * stackDiscount * climateMultiplier).toInt()
        val newMoney = character.stats.money - annualUpkeep

        return if (newMoney < 0) {
            appreciateHeirlooms(
                character.copy(
                    stats = character.stats.copy(
                        money = 0,
                        happiness = clampStat(character.stats.happiness - DEBT_HAPPINESS_PENALTY)
                    )
                )
            )
        } else {
            appreciateHeirlooms(
                character.copy(
                    stats = character.stats.copy(money = newMoney)
                )
            )
        }
    }

    /**
     * Ad-revenue payout for monetized social accounts, scaled by followers and country economy.
     */
    fun calculateSocialMediaPayout(character: Character): Int {
        val followers = character.socialMedia.followers.coerceAtLeast(0)
        val tiers = followers / 100_000
        val baseKenya = SOCIAL_MEDIA_BASE_PAYOUT_KENYA + tiers * SOCIAL_MEDIA_PAYOUT_PER_100K_KENYA
        return EconomyScaler.scaleAmount(baseKenya, character.countryCode)
    }

    /** Adds [payout] cash from social media monetization. */
    fun applySocialMediaRevenue(character: Character, payout: Int): Character {
        if (payout <= 0) return character
        return character.copy(
            stats = character.stats.copy(money = character.stats.money + payout)
        )
    }

    /** Deducts startup capital when founding a business. */
    fun applyBusinessInvestment(character: Character, investment: Int): Character {
        if (investment <= 0) return character
        return character.copy(
            stats = character.stats.copy(
                money = (character.stats.money - investment).coerceAtLeast(0)
            )
        )
    }

    /** Adds sale proceeds when liquidating a business. */
    fun applyBusinessSale(character: Character, valuation: Int): Character {
        if (valuation <= 0) return character
        return character.copy(
            stats = character.stats.copy(money = character.stats.money + valuation)
        )
    }

    /**
     * Passive yearly living expenses for each living [RelationType.CHILD] under 18.
     * Scaled to [Character.countryCode]; floors money at 0 with a happiness penalty if broke.
     */
    fun applyChildSupport(character: Character): Character {
        val minorChildren = character.family.count {
            it.relation == RelationType.CHILD && it.alive && it.age < MINOR_CHILD_MAX_AGE
        }
        if (minorChildren == 0) return character

        val perChild = EconomyScaler.scaleAmount(CHILD_SUPPORT_BASE_KENYA, character.countryCode)
        val total = perChild * minorChildren
        val newMoney = character.stats.money - total
        return if (newMoney < 0) {
            character.copy(
                stats = character.stats.copy(
                    money = 0,
                    happiness = clampStat(character.stats.happiness - DEBT_HAPPINESS_PENALTY)
                )
            )
        } else {
            character.copy(stats = character.stats.copy(money = newMoney))
        }
    }

    /**
     * Deducts annual pet care costs scaled to [Character.countryCode].
     * If money goes negative, floors at 0 and applies a happiness penalty.
     */
    fun applyPetUpkeep(character: Character): Character {
        if (character.pets.isEmpty()) return character

        val climateMultiplier = upkeepMultiplier(character.economicState.climate)
        val rawAnnual = character.pets.sumOf { pet ->
            val entry = PetCatalog.findBySpecies(pet.species) ?: return@sumOf 0
            EconomyScaler.scaleAmount(entry.yearlyUpkeep, character.countryCode)
        }
        val annualUpkeep = (rawAnnual * climateMultiplier).toInt()
        val newMoney = character.stats.money - annualUpkeep

        return if (newMoney < 0) {
            character.copy(
                stats = character.stats.copy(
                    money = 0,
                    happiness = clampStat(character.stats.happiness - DEBT_HAPPINESS_PENALTY)
                )
            )
        } else {
            character.copy(
                stats = character.stats.copy(money = newMoney)
            )
        }
    }

    /**
     * Grants a rare heirloom from [AssetCatalog] heirdom entries.
     * Skips if the character already owns that catalog item by name.
     */
    fun grantHeirloom(character: Character, catalogId: String): Character {
        val catalogItem = AssetCatalog.findHeirloomById(catalogId) ?: return character
        if (character.assets.any { it.isHeirloom && it.name == catalogItem.name }) return character

        val baseValue = EconomyScaler.scaleAmount(catalogItem.purchasePrice, character.countryCode)
        val heirloom = Asset(
            id = UUID.randomUUID().toString(),
            catalogId = catalogItem.id,
            type = AssetType.HEIRLOOM,
            name = catalogItem.name,
            purchasePrice = baseValue,
            currentValue = baseValue,
            condition = 100,
            monthlyUpkeep = 0,
            isHeirloom = true,
            generationAcquired = character.generationNumber
        )

        return character.copy(
            assets = character.assets + heirloom,
            eventLog = EventLogCap.prepend(
                character.eventLog,
                "A family heirloom joined your estate: ${catalogItem.name}."
            )
        )
    }

    private fun appreciateHeirlooms(character: Character): Character {
        if (character.assets.none { it.isHeirloom }) return character
        val appreciated = character.assets.map { asset ->
            if (!asset.isHeirloom) asset else {
                val boosted = (asset.currentValue * (1f + HEIRLOOM_ANNUAL_APPRECIATION_RATE)).toInt()
                    .coerceAtLeast(asset.currentValue + 1)
                asset.copy(currentValue = boosted, condition = 100)
            }
        }
        return character.copy(assets = appreciated)
    }

    /** Random yearly wear; rented properties degrade faster. May trigger critical failure below 40%. */
    fun degradeAssets(character: Character): Character {
        if (character.assets.isEmpty()) return character

        val modifier = character.economicState.marketModifier
        var updatedLog = character.eventLog
        val degraded = character.assets.map { asset ->
            if (asset.isHeirloom) asset else {
                val worn = applyConditionDegradation(asset, modifier)
                val afterFailure = rollCriticalFailure(worn, modifier)
                if (afterFailure.condition == 0 && worn.condition > 0) {
                    updatedLog = EventLogCap.prepend(updatedLog, criticalFailureLog(afterFailure))
                }
                afterFailure
            }
        }
        return character.copy(assets = degraded, eventLog = updatedLog)
    }

    /** Houses that can be leased (non-heirloom real estate). */
    fun isRentable(asset: Asset): Boolean =
        asset.type == AssetType.HOUSE && !asset.isHeirloom

    /** Yearly rent for a leased property (5–8% of current valuation). */
    fun calculateYearlyRent(asset: Asset): Int {
        if (!asset.isRentedOut || !isRentable(asset)) return 0
        val rate = RENT_YIELD_MIN + Random.nextFloat() * (RENT_YIELD_MAX - RENT_YIELD_MIN)
        return (asset.currentValue * rate).toInt().coerceAtLeast(1)
    }

    fun estimateYearlyRent(asset: Asset): Int {
        if (!isRentable(asset)) return 0
        val midRate = (RENT_YIELD_MIN + RENT_YIELD_MAX) / 2f
        return (asset.currentValue * midRate).toInt().coerceAtLeast(1)
    }

    fun evictionFee(character: Character): Int =
        EconomyScaler.scaleAmount(EVICTION_LEGAL_FEE_KENYA, character.countryCode)

    /**
     * Collects rent from all leased properties and drifts tenant happiness.
     */
    fun collectRent(character: Character): Character {
        val rented = character.assets.filter { it.isRentedOut && isRentable(it) }
        if (rented.isEmpty()) return character

        var totalRent = 0
        val updatedAssets = character.assets.map { asset ->
            if (!asset.isRentedOut || !isRentable(asset)) {
                asset
            } else {
                val rent = calculateYearlyRent(asset)
                totalRent += rent
                val happiness = (asset.tenantHappiness ?: STARTING_TENANT_HAPPINESS) +
                    Random.nextInt(TENANT_HAPPINESS_DRIFT_MIN, TENANT_HAPPINESS_DRIFT_MAX + 1)
                asset.copy(tenantHappiness = happiness.coerceIn(0, 100))
            }
        }
        if (totalRent <= 0) return character.copy(assets = updatedAssets)

        return character.copy(
            stats = character.stats.copy(money = character.stats.money + totalRent),
            assets = updatedAssets,
            eventLog = EventLogCap.prepend(
                character.eventLog,
                "Collected ${formatMoney(totalRent, character.countryCode)} in rental income."
            )
        )
    }

    sealed class RentalResult {
        data class Success(val character: Character) : RentalResult()
        data object NotFound : RentalResult()
        data object NotRentable : RentalResult()
        data object AlreadyRented : RentalResult()
        data object NotRented : RentalResult()
        data object InsufficientFunds : RentalResult()
    }

    /** Lists a vacant house for rent with a new tenant. */
    fun rentOutProperty(character: Character, assetId: String): RentalResult {
        val index = character.assets.indexOfFirst { it.id == assetId }
        if (index == -1) return RentalResult.NotFound
        val asset = character.assets[index]
        if (!isRentable(asset)) return RentalResult.NotRentable
        if (asset.isRentedOut) return RentalResult.AlreadyRented

        val rented = asset.copy(
            isRentedOut = true,
            tenantHappiness = STARTING_TENANT_HAPPINESS
        )
        val updatedAssets = character.assets.toMutableList().apply { this[index] = rented }
        return RentalResult.Success(
            character.copy(
                assets = updatedAssets,
                eventLog = EventLogCap.prepend(
                    character.eventLog,
                    "You rented out ${asset.name}. Tenants move in this week."
                )
            )
        )
    }

    /**
     * Ends a lease: legal fees, slight happiness hit, clears rental status.
     */
    fun evictTenant(character: Character, assetId: String): RentalResult {
        val index = character.assets.indexOfFirst { it.id == assetId }
        if (index == -1) return RentalResult.NotFound
        val asset = character.assets[index]
        if (!asset.isRentedOut) return RentalResult.NotRented

        val fee = evictionFee(character)
        if (character.stats.money < fee) return RentalResult.InsufficientFunds

        val vacant = asset.copy(isRentedOut = false, tenantHappiness = null)
        val updatedAssets = character.assets.toMutableList().apply { this[index] = vacant }
        return RentalResult.Success(
            character.copy(
                stats = character.stats.copy(
                    money = character.stats.money - fee,
                    happiness = clampStat(character.stats.happiness - EVICTION_HAPPINESS_HIT)
                ),
                assets = updatedAssets,
                eventLog = EventLogCap.prepend(
                    character.eventLog,
                    "You evicted the tenant from ${asset.name} for " +
                        "${formatMoney(fee, character.countryCode)} in legal fees."
                )
            )
        )
    }

    fun ownsRentedProperty(character: Character): Boolean =
        character.assets.any { it.isRentedOut && isRentable(it) }

    /**
     * Pays to restore [assetId] to 100% condition. Cost scales with [Asset.purchasePrice] and wear.
     */
    fun repairAsset(character: Character, assetId: String): RepairResult {
        val index = character.assets.indexOfFirst { it.id == assetId }
        if (index == -1) return RepairResult.AssetNotFound

        val asset = character.assets[index]
        if (asset.isHeirloom || asset.condition >= 100) {
            return RepairResult.Success(character)
        }

        val cost = calculateRepairCost(asset, character.countryCode)
        if (character.stats.money < cost) {
            return RepairResult.InsufficientFunds
        }

        val repaired = recalculateValue(
            asset.copy(condition = 100),
            character.economicState.marketModifier
        )
        val updatedAssets = character.assets.toMutableList().apply { this[index] = repaired }
        return RepairResult.Success(
            character.copy(
                stats = character.stats.copy(money = character.stats.money - cost),
                assets = updatedAssets,
                eventLog = EventLogCap.prepend(
                    character.eventLog,
                    "Repaired ${asset.name} for ${formatRepairCost(cost, character.countryCode)}."
                )
            )
        )
    }

    /** Repair quote for UI confirmation dialogs. */
    fun calculateRepairCost(asset: Asset, countryCode: String): Int {
        val wearFraction = (100 - asset.condition.coerceIn(0, 100)) / 100f
        val rawCost = (asset.purchasePrice * REPAIR_BASE_RATE * wearFraction).toInt()
            .coerceAtLeast(MIN_REPAIR_COST)
        return EconomyScaler.scaleAmount(rawCost, countryCode)
    }

    /** Cash, assets, businesses, savings, junior trust, and investment portfolio, minus student loans and mortgages. */
    fun calculateNetWorth(character: Character): Int {
        val assetValue = character.assets.sumOf { it.currentValue }
        val businessValue = character.businesses.sumOf { it.valuation }
        val totalMortgages = character.assets.sumOf { it.mortgageBalance.coerceAtLeast(0) }
        return character.stats.money + assetValue + businessValue +
            character.investmentPortfolioValue.coerceAtLeast(0) +
            character.savingsBalance.coerceAtLeast(0) +
            character.finance.juniorTrustBalance.coerceAtLeast(0) -
            character.education.studentLoanBalance.coerceAtLeast(0) -
            totalMortgages
    }

    fun studentLiabilitySnapshot(character: Character) =
        StudentLiabilitySnapshot.from(character.education)

    /**
     * Accrues interest on student loans and auto-repays a slice of salary once employed.
     */
    fun tickStudentLoan(character: Character): Character {
        var balance = character.education.studentLoanBalance
        if (balance <= 0) return character

        val interestPercent = Random.nextInt(
            StudentFinance.LOAN_INTEREST_PERCENT_MIN,
            StudentFinance.LOAN_INTEREST_PERCENT_MAX + 1
        )
        val interest = (balance * interestPercent / 100f).roundToInt().coerceAtLeast(1)
        balance += interest

        var money = character.stats.money
        var log = "Student loan interest +$interestPercent% " +
            "(${formatMoney(interest, character.countryCode)})."
        val salary = character.career.currentJob?.baseSalary ?: 0
        if (salary > 0 && character.alive) {
            val payment = (salary * StudentFinance.LOAN_REPAY_SALARY_FRACTION)
                .roundToInt()
                .coerceAtMost(balance)
                .coerceAtMost(money)
            if (payment > 0) {
                balance -= payment
                money -= payment
                log += " Auto-repaid ${formatMoney(payment, character.countryCode)} from wages."
            }
        }

        return character.copy(
            stats = character.stats.copy(money = money),
            education = character.education.copy(studentLoanBalance = balance.coerceAtLeast(0)),
            eventLog = EventLogCap.prepend(character.eventLog, log)
        )
    }

    sealed class StudentLoanRepayResult {
        data class Success(val character: Character, val paid: Int) : StudentLoanRepayResult()
        data object NoBalance : StudentLoanRepayResult()
        data object InsufficientFunds : StudentLoanRepayResult()
        data object InvalidAmount : StudentLoanRepayResult()
    }

    /** Manual lump-sum student loan repayment from liquid cash. */
    fun repayStudentLoan(character: Character, amount: Int): StudentLoanRepayResult {
        if (amount <= 0) return StudentLoanRepayResult.InvalidAmount
        val balance = character.education.studentLoanBalance
        if (balance <= 0) return StudentLoanRepayResult.NoBalance
        if (character.stats.money < amount) return StudentLoanRepayResult.InsufficientFunds
        val paid = amount.coerceAtMost(balance)
        return StudentLoanRepayResult.Success(
            character.copy(
                stats = character.stats.copy(money = character.stats.money - paid),
                education = character.education.copy(
                    studentLoanBalance = (balance - paid).coerceAtLeast(0)
                ),
                eventLog = EventLogCap.prepend(
                    character.eventLog,
                    "You paid ${formatMoney(paid, character.countryCode)} toward your student loan."
                )
            ),
            paid
        )
    }

    /** Moves liquid cash into safe savings (bank-style). */
    fun depositSavings(character: Character, amount: Int): InvestmentResult {
        if (amount <= 0) return InvestmentResult.InvalidAmount
        if (character.stats.money < amount) return InvestmentResult.InsufficientFunds
        return InvestmentResult.Success(
            character.copy(
                stats = character.stats.copy(money = character.stats.money - amount),
                savingsBalance = character.savingsBalance + amount,
                eventLog = EventLogCap.prepend(
                    character.eventLog,
                    "Deposited ${formatMoney(amount, character.countryCode)} into savings."
                )
            )
        )
    }

    /** Moves savings back to liquid cash. */
    fun withdrawSavings(character: Character, amount: Int): InvestmentResult {
        if (amount <= 0) return InvestmentResult.InvalidAmount
        if (character.savingsBalance < amount) return InvestmentResult.InsufficientFunds
        return InvestmentResult.Success(
            character.copy(
                stats = character.stats.copy(money = character.stats.money + amount),
                savingsBalance = character.savingsBalance - amount,
                eventLog = EventLogCap.prepend(
                    character.eventLog,
                    "Withdrew ${formatMoney(amount, character.countryCode)} from savings."
                )
            )
        )
    }

    /**
     * Credits modest interest on [Character.savingsBalance] (climate-aware, far safer than the portfolio).
     */
    fun applySavingsInterest(character: Character): Character {
        if (character.savingsBalance <= 0) {
            return character.copy(lastSavingsInterestPercent = 0)
        }
        val ratePercent = when (character.economicState.climate) {
            EconomicClimate.BOOM -> Random.nextInt(SAVINGS_INTEREST_BOOM_MIN, SAVINGS_INTEREST_BOOM_MAX + 1)
            EconomicClimate.RECESSION -> Random.nextInt(SAVINGS_INTEREST_BUST_MIN, SAVINGS_INTEREST_BUST_MAX + 1)
            EconomicClimate.INFLATION_SURGE -> Random.nextInt(6, 11)
            EconomicClimate.STEADY -> Random.nextInt(SAVINGS_INTEREST_MIN, SAVINGS_INTEREST_MAX + 1)
        }
        val interest = ((character.savingsBalance * ratePercent) / 100.0).toInt().coerceAtLeast(0)
        if (interest <= 0) {
            return character.copy(lastSavingsInterestPercent = ratePercent)
        }
        return character.copy(
            savingsBalance = character.savingsBalance + interest,
            lastSavingsInterestPercent = ratePercent,
            eventLog = EventLogCap.prepend(
                character.eventLog,
                "Savings earned $ratePercent% interest " +
                    "(${formatMoney(interest, character.countryCode)})."
            )
        )
    }

    /** Saves gift money into childhood trust piggy bank (ages 0-17). */
    fun depositJuniorTrust(character: Character, amount: Int): InvestmentResult {
        if (amount <= 0) return InvestmentResult.InvalidAmount
        if (character.age >= 18) return InvestmentResult.InvalidAmount
        if (character.stats.money < amount) return InvestmentResult.InsufficientFunds
        return InvestmentResult.Success(
            character.copy(
                stats = character.stats.copy(money = character.stats.money - amount),
                finance = character.finance.copy(
                    juniorTrustBalance = character.finance.juniorTrustBalance + amount
                ),
                eventLog = EventLogCap.prepend(
                    character.eventLog,
                    "Saved ${formatMoney(amount, character.countryCode)} into your Junior Trust Piggy Bank."
                )
            )
        )
    }

    /** Unlocks the childhood trust piggy bank into liquid cash when reaching adulthood at 18. */
    fun unlockJuniorTrustAtAge18(character: Character): Character {
        if (character.age < 18 || character.finance.juniorTrustBalance <= 0) return character
        val trustAmount = character.finance.juniorTrustBalance
        val updatedMoney = character.stats.money + trustAmount
        return character.copy(
            stats = character.stats.copy(money = updatedMoney),
            finance = character.finance.copy(juniorTrustBalance = 0),
            eventLog = EventLogCap.prepend(
                character.eventLog,
                "Unlocked your Junior Trust Piggy Bank! Transferred ${formatMoney(trustAmount, character.countryCode)} into your liquid funds."
            )
        )
    }

    fun setLivingStandard(
        character: Character,
        standard: com.maisha.game.data.model.LivingStandard
    ): Character {
        if (character.lifestyle.livingStandard == standard) return character
        return character.copy(
            lifestyle = character.lifestyle.copy(livingStandard = standard),
            eventLog = EventLogCap.prepend(
                character.eventLog,
                "You set your living standard to ${standard.name.lowercase()}."
            )
        )
    }

    /**
     * Kenya-baseline annual living costs scaled to country: age band, living standard,
     * housing ownership, and dependent children.
     */
    fun estimateAnnualCostOfLiving(character: Character): Int {
        val ageBase = when {
            character.age < 6 -> 6_000
            character.age < 13 -> 12_000
            character.age < 18 -> 20_000
            character.age < 25 -> 48_000
            character.age < 65 -> 56_000
            else -> 42_000
        }
        val standardMult = when (character.lifestyle.livingStandard) {
            com.maisha.game.data.model.LivingStandard.FRUGAL -> 0.62
            com.maisha.game.data.model.LivingStandard.MODEST -> 1.0
            com.maisha.game.data.model.LivingStandard.COMFORTABLE -> 1.55
            com.maisha.game.data.model.LivingStandard.LUXURY -> 2.35
        }
        val ownsHome = character.assets.any { it.type == AssetType.HOUSE }
        val housingMult = when {
            character.age < 18 -> 0.35
            ownsHome -> 0.55
            else -> 1.15
        }
        val dependentChildren = character.family.count {
            it.relation == RelationType.CHILD && it.alive && it.age < 18
        }
        val dependentCost = dependentChildren * DEPENDENT_CHILD_COST_BASE
        val climateCostMult = when (character.economicState.climate) {
            EconomicClimate.BOOM -> 1.08
            EconomicClimate.STEADY -> 1.0
            EconomicClimate.INFLATION_SURGE -> 1.30
            EconomicClimate.RECESSION -> 0.95
        }
        val raw = ((ageBase * standardMult * housingMult * climateCostMult) + dependentCost).toInt()
        return EconomyScaler.scaleAmount(raw.coerceAtLeast(1), character.countryCode)
    }

    /**
     * Deducts yearly cost of living. Shortfall floors cash at 0 and hits happiness/health;
     * luxury unpaid is especially painful. Frugal paid lifestyle drains a little joy.
     */
    fun applyCostOfLiving(character: Character): Character {
        if (character.age < MIN_COST_OF_LIVING_AGE) return character
        val cost = estimateAnnualCostOfLiving(character)
        if (cost <= 0) return character

        val paid = minOf(cost, character.stats.money)
        val shortfall = cost - paid
        var happinessDelta = 0
        var healthDelta = 0
        when (character.lifestyle.livingStandard) {
            com.maisha.game.data.model.LivingStandard.FRUGAL ->
                if (shortfall == 0) happinessDelta -= FRUGAL_HAPPINESS_DRAIN
            com.maisha.game.data.model.LivingStandard.MODEST -> Unit
            com.maisha.game.data.model.LivingStandard.COMFORTABLE ->
                if (shortfall == 0) happinessDelta += 1
            com.maisha.game.data.model.LivingStandard.LUXURY ->
                if (shortfall == 0) happinessDelta += 3
        }
        if (shortfall > 0) {
            happinessDelta -= COL_SHORTFALL_HAPPINESS
            healthDelta -= COL_SHORTFALL_HEALTH
            if (character.lifestyle.livingStandard ==
                com.maisha.game.data.model.LivingStandard.LUXURY
            ) {
                happinessDelta -= LUXURY_SHORTFALL_EXTRA_HAPPINESS
            }
        }

        var updated = character.copy(
            stats = character.stats.copy(
                money = (character.stats.money - paid).coerceAtLeast(0),
                happiness = clampStat(character.stats.happiness + happinessDelta),
                health = clampStat(character.stats.health + healthDelta)
            )
        )
        val log = if (shortfall > 0) {
            "Living costs ${formatMoney(cost, character.countryCode)} — " +
                "short by ${formatMoney(shortfall, character.countryCode)}. Stress mounted."
        } else {
            "Paid ${formatMoney(cost, character.countryCode)} in living costs " +
                "(${character.lifestyle.livingStandard.name.lowercase()})."
        }
        return updated.copy(eventLog = EventLogCap.prepend(updated.eventLog, log))
    }

    /**
     * Progressive income tax on a Kenya-baseline gross pay amount (already country-scaled salary).
     * Brackets are expressed in Kenya units then scaled so tax burden tracks local salaries.
     */
    fun calculateIncomeTax(grossPay: Int, countryCode: String): Int =
        Companion.calculateIncomeTax(grossPay, countryCode)

    sealed class InvestmentResult {
        data class Success(val character: Character) : InvestmentResult()
        data object InsufficientFunds : InvestmentResult()
        data object InvalidAmount : InvestmentResult()
    }

    /** Moves liquid cash into the investment portfolio. */
    fun investFunds(character: Character, amount: Int): InvestmentResult {
        if (amount <= 0) return InvestmentResult.InvalidAmount
        if (character.stats.money < amount) return InvestmentResult.InsufficientFunds
        return InvestmentResult.Success(
            character.copy(
                stats = character.stats.copy(money = character.stats.money - amount),
                investmentPortfolioValue = character.investmentPortfolioValue + amount,
                eventLog = EventLogCap.prepend(
                    character.eventLog,
                    "Invested ${formatMoney(amount, character.countryCode)} in the market."
                )
            )
        )
    }

    /** Moves portfolio value back to liquid cash. */
    fun withdrawFunds(character: Character, amount: Int): InvestmentResult {
        if (amount <= 0) return InvestmentResult.InvalidAmount
        if (character.investmentPortfolioValue < amount) return InvestmentResult.InsufficientFunds
        return InvestmentResult.Success(
            character.copy(
                stats = character.stats.copy(money = character.stats.money + amount),
                investmentPortfolioValue = character.investmentPortfolioValue - amount,
                eventLog = EventLogCap.prepend(
                    character.eventLog,
                    "Withdrew ${formatMoney(amount, character.countryCode)} from investments."
                )
            )
        )
    }

    /**
     * Yearly portfolio tick: random return from [PORTFOLIO_RETURN_MIN_PERCENT] to
     * [PORTFOLIO_RETURN_MAX_PERCENT] when portfolio value is positive, shifted by macroeconomic climate.
     */
    fun applyPortfolioMarketTick(character: Character): Character {
        if (character.investmentPortfolioValue <= 0) {
            return character.copy(lastPortfolioReturnPercent = 0)
        }
        val (baseMin, baseMax) = portfolioReturnRange(character.lifestyle.portfolioStrategy)
        val climateShift = when (character.economicState.climate) {
            EconomicClimate.BOOM -> 10
            EconomicClimate.STEADY -> 0
            EconomicClimate.INFLATION_SURGE -> -5
            EconomicClimate.RECESSION -> -15
        }
        val returnPercent = Random.nextInt(baseMin + climateShift, baseMax + climateShift + 1)
        return applyPortfolioReturn(character, returnPercent, logMarketMove = true)
    }

    fun setPortfolioStrategy(character: Character, strategy: PortfolioStrategy): Character {
        if (character.lifestyle.portfolioStrategy == strategy) return character
        val label = when (strategy) {
            PortfolioStrategy.CONSERVATIVE -> "conservative"
            PortfolioStrategy.BALANCED -> "balanced"
            PortfolioStrategy.AGGRESSIVE -> "aggressive"
        }
        return character.copy(
            lifestyle = character.lifestyle.copy(portfolioStrategy = strategy),
            eventLog = EventLogCap.prepend(
                character.eventLog,
                "You switched your portfolio to a $label strategy."
            )
        )
    }

    private fun portfolioReturnRange(strategy: PortfolioStrategy): Pair<Int, Int> = when (strategy) {
        PortfolioStrategy.CONSERVATIVE -> -15 to 20
        PortfolioStrategy.BALANCED -> PORTFOLIO_RETURN_MIN_PERCENT to PORTFOLIO_RETURN_MAX_PERCENT
        PortfolioStrategy.AGGRESSIVE -> -45 to 55
    }

    /** Upgrades a house beyond repair — adds [Asset.renovationLevel] and boosts value. */
    fun renovateAsset(character: Character, assetId: String): RenovateResult {
        val index = character.assets.indexOfFirst { it.id == assetId }
        if (index == -1) return RenovateResult.AssetNotFound

        val asset = character.assets[index]
        if (asset.type != AssetType.HOUSE || asset.isHeirloom) return RenovateResult.NotEligible
        if (asset.condition < RENOVATE_MIN_CONDITION || asset.renovationLevel >= MAX_RENOVATION_LEVEL) {
            return RenovateResult.NotEligible
        }

        val cost = calculateRenovationCost(asset, character.countryCode)
        if (character.stats.money < cost) return RenovateResult.InsufficientFunds

        val newLevel = asset.renovationLevel + 1
        val valueBoost = (asset.currentValue * RENOVATION_VALUE_BOOST).roundToInt()
        val renovated = recalculateValue(
            asset.copy(
                renovationLevel = newLevel,
                condition = 100,
                currentValue = asset.currentValue + valueBoost,
                purchasePrice = asset.purchasePrice + (cost / 2)
            ),
            character.economicState.marketModifier
        )
        val updatedAssets = character.assets.toMutableList().apply { this[index] = renovated }
        return RenovateResult.Success(
            character.copy(
                stats = character.stats.copy(money = character.stats.money - cost),
                assets = updatedAssets,
                eventLog = EventLogCap.prepend(
                    character.eventLog,
                    "Renovated ${asset.name} (level $newLevel) for ${formatMoney(cost, character.countryCode)}."
                )
            )
        )
    }

    fun calculateRenovationCost(asset: Asset, countryCode: String): Int {
        val tierMultiplier = 1f + asset.renovationLevel * 0.35f
        val raw = (asset.purchasePrice * RENOVATION_BASE_RATE * tierMultiplier).toInt()
            .coerceAtLeast(MIN_RENOVATION_COST)
        return EconomyScaler.scaleAmount(raw, countryCode)
    }

    /**
     * Applies an immediate portfolio return percentage (e.g. event-driven crash or rally).
     */
    fun applyPortfolioReturn(
        character: Character,
        returnPercent: Int,
        logMarketMove: Boolean = true
    ): Character {
        if (character.investmentPortfolioValue <= 0) {
            return character.copy(lastPortfolioReturnPercent = 0)
        }
        val before = character.investmentPortfolioValue
        val after = ((before.toLong() * (100L + returnPercent)) / 100L)
            .toInt()
            .coerceAtLeast(0)
        val sign = if (returnPercent >= 0) "+" else ""
        val logLine = if (logMarketMove) {
            "Portfolio returned $sign$returnPercent% " +
                "(${formatMoney(before, character.countryCode)} → " +
                "${formatMoney(after, character.countryCode)})."
        } else {
            null
        }
        return character.copy(
            investmentPortfolioValue = after,
            lastPortfolioReturnPercent = returnPercent,
            eventLog = if (logLine != null) {
                EventLogCap.prepend(character.eventLog, logLine)
            } else {
                character.eventLog
            }
        )
    }

    fun hasPortfolio(character: Character): Boolean =
        character.investmentPortfolioValue > 0

    /** Pays annual pension to retired characters during the yearly finance tick. */
    fun applyPension(character: Character): Character {
        if (!character.career.isRetired || character.career.pensionAmount <= 0) return character
        return character.copy(
            stats = character.stats.copy(
                money = character.stats.money + character.career.pensionAmount
            )
        )
    }

    /**
     * Rolls or forces a yearly macro-economic shift (boom / bust / neutral) and refreshes asset values.
     *
     * @param forced `boom`, `bust`, or `random` from event choices; null rolls naturally.
     */
    fun applyEconomicShift(character: Character, forced: String? = null): EconomicShiftResult {
        val climate = resolveClimate(forced)
        val modifier = marketModifierFor(climate)
        val logEntry = economicLogFor(climate)
        val updatedAssets = character.assets.map { recalculateValue(it, modifier) }
        val updated = character.copy(
            economicState = EconomicState(climate = climate, marketModifier = modifier),
            assets = updatedAssets,
            eventLog = if (climate == EconomicClimate.STEADY || (character.age < 18 && character.assets.isEmpty())) {
                character.eventLog
            } else {
                EventLogCap.prepend(character.eventLog, logEntry)
            }
        )
        return EconomicShiftResult(updated, climate)
    }

    /** Adjusts condition on a specific owned asset by id. */
    fun applyConditionToAsset(
        character: Character,
        assetId: String,
        conditionDelta: Int
    ): Character {
        val index = character.assets.indexOfFirst { it.id == assetId }
        if (index == -1) return character

        val asset = character.assets[index]
        val newCondition = clampCondition(asset.condition + conditionDelta)
        val vacateTenant = asset.isRentedOut && conditionDelta <= TENANT_LEAVE_CONDITION_DELTA
        val updatedAsset = recalculateValue(
            asset.copy(
                condition = newCondition,
                isRentedOut = if (vacateTenant) false else asset.isRentedOut,
                tenantHappiness = if (vacateTenant) null else asset.tenantHappiness
            ),
            character.economicState.marketModifier
        )
        return character.copy(
            assets = character.assets.toMutableList().apply { this[index] = updatedAsset }
        )
    }

    /** Sets condition on the first asset of [type] to an absolute value (0–100). */
    fun setAssetConditionByType(
        character: Character,
        type: AssetType,
        condition: Int
    ): Character {
        val index = indexOfPreferredAsset(character, type)
        if (index == -1) return character

        val asset = character.assets[index]
        val updatedAsset = recalculateValue(
            asset.copy(condition = clampCondition(condition)),
            character.economicState.marketModifier
        )
        return character.copy(
            assets = character.assets.toMutableList().apply { this[index] = updatedAsset }
        )
    }

    /** Adjusts condition on the first asset of [type]; prefers rented houses for landlord events. */
    fun applyConditionToAssetType(
        character: Character,
        type: AssetType,
        conditionDelta: Int
    ): Character {
        val index = indexOfPreferredAsset(character, type)
        if (index == -1) return character
        return applyConditionToAsset(character, character.assets[index].id, conditionDelta)
    }

    private fun indexOfPreferredAsset(character: Character, type: AssetType): Int {
        val rented = character.assets.indexOfFirst { it.type == type && it.isRentedOut }
        if (rented >= 0) return rented
        return character.assets.indexOfFirst { it.type == type }
    }

    /** Adjusts condition on the first owned asset when event has no [EventChoice.targetAssetType]. */
    fun applyConditionToFirstAsset(
        character: Character,
        conditionDelta: Int
    ): Character {
        if (character.assets.isEmpty()) return character
        return applyConditionToAsset(character, character.assets.first().id, conditionDelta)
    }

    /** Gate for finance-tagged random events: owns assets or has at least FINANCE_EVENT_MONEY_THRESHOLD cash. */
    fun meetsFinanceEventThreshold(character: Character): Boolean {
        return character.assets.isNotEmpty() || character.stats.money >= FINANCE_EVENT_MONEY_THRESHOLD
    }

    /**
     * Sets [Asset.currentValue] from purchase price, condition, and [marketModifier].
     * Real estate may appreciate; vehicles only depreciate via condition.
     * Boats and jets depreciate slowly (70% of vehicle rate).
     */
    fun recalculateValue(asset: Asset, marketModifier: Float = 1.0f): Asset {
        val conditionFactor = asset.condition / 100f
        val value = when (asset.type) {
            AssetType.HOUSE -> {
                val renovationBonus = 1f + asset.renovationLevel * RENOVATION_VALUE_MULTIPLIER_PER_LEVEL
                val marketAdjusted = asset.purchasePrice * conditionFactor * marketModifier * renovationBonus
                val appreciation = if (Random.nextFloat() < REAL_ESTATE_APPRECIATION_CHANCE) {
                    asset.purchasePrice * Random.nextFloat() * MAX_ANNUAL_APPRECIATION_RATE
                } else {
                    0f
                }
                (marketAdjusted + appreciation).toInt()
            }
            AssetType.CAR, AssetType.MOTORBIKE -> {
                (asset.purchasePrice * conditionFactor).toInt()
            }
            AssetType.BOAT, AssetType.PRIVATE_JET -> {
                // Luxury vehicles depreciate slower — retain 70% value at 100% condition
                (asset.purchasePrice * (0.70f + conditionFactor * 0.30f)).toInt()
            }
            AssetType.HEIRLOOM -> {
                val appreciation = asset.currentValue * HEIRLOOM_ANNUAL_APPRECIATION_RATE
                (asset.currentValue + appreciation).toInt()
            }
        }
        return asset.copy(currentValue = value.coerceAtLeast(0))
    }

    private fun resolveClimate(forced: String?): EconomicClimate {
        return when (forced?.lowercase()) {
            "boom" -> EconomicClimate.BOOM
            "bust", "recession" -> EconomicClimate.RECESSION
            "inflation", "inflation_surge", "inflationsurge" -> EconomicClimate.INFLATION_SURGE
            "steady", "neutral" -> EconomicClimate.STEADY
            "random" -> rollClimate()
            null -> rollClimate()
            else -> rollClimate()
        }
    }

    private fun rollClimate(): EconomicClimate {
        val roll = Random.nextFloat()
        return when {
            roll < BOOM_CHANCE -> EconomicClimate.BOOM
            roll < BOOM_CHANCE + RECESSION_CHANCE -> EconomicClimate.RECESSION
            roll < BOOM_CHANCE + RECESSION_CHANCE + INFLATION_CHANCE -> EconomicClimate.INFLATION_SURGE
            else -> EconomicClimate.STEADY
        }
    }

    private fun marketModifierFor(climate: EconomicClimate): Float = when (climate) {
        EconomicClimate.BOOM -> BOOM_MARKET_MODIFIER
        EconomicClimate.RECESSION -> RECESSION_MARKET_MODIFIER
        EconomicClimate.INFLATION_SURGE -> INFLATION_MARKET_MODIFIER
        EconomicClimate.STEADY -> STEADY_MARKET_MODIFIER
    }

    private fun upkeepMultiplier(climate: EconomicClimate): Double = when (climate) {
        EconomicClimate.BOOM -> BOOM_UPKEEP_MULTIPLIER
        EconomicClimate.RECESSION -> RECESSION_UPKEEP_MULTIPLIER
        EconomicClimate.INFLATION_SURGE -> INFLATION_UPKEEP_MULTIPLIER
        EconomicClimate.STEADY -> 1.0
    }

    private fun applyConditionDegradation(asset: Asset, marketModifier: Float): Asset {
        val base = if (asset.servicedThisYear) {
            Random.nextInt(1, 4)
        } else {
            Random.nextInt(MIN_DEGRADATION, MAX_DEGRADATION + 1)
        }
        val rentalWear = if (asset.isRentedOut) RENTED_EXTRA_DEGRADATION else 0
        val newCondition = clampCondition(asset.condition - base - rentalWear)
        return recalculateValue(asset.copy(condition = newCondition, servicedThisYear = false), marketModifier)
    }

    private fun rollCriticalFailure(asset: Asset, marketModifier: Float): Asset {
        if (asset.isHeirloom || asset.servicedThisYear) return asset
        if (asset.condition >= CRITICAL_CONDITION_THRESHOLD) return asset
        if (Random.nextFloat() >= CRITICAL_FAILURE_CHANCE) return asset
        return recalculateValue(asset.copy(condition = 0), marketModifier)
    }

    private fun criticalFailureLog(asset: Asset): String = when (asset.type) {
        AssetType.HOUSE -> "Critical failure: ${asset.name} suffered major damage — the roof collapsed."
        AssetType.CAR -> "Critical failure: ${asset.name}'s engine blew out completely."
        AssetType.MOTORBIKE -> "Critical failure: ${asset.name}'s engine seized on the road."
        AssetType.BOAT -> "Critical failure: ${asset.name}'s hull developed a serious leak."
        AssetType.PRIVATE_JET -> "Critical failure: ${asset.name} suffered major avionics damage — grounded for major repairs."
        AssetType.HEIRLOOM -> ""
    }

    private fun formatRepairCost(amount: Int, countryCode: String): String =
        com.maisha.game.util.formatMoney(amount, countryCode)

    private fun economicLogFor(climate: EconomicClimate): String = when (climate) {
        EconomicClimate.BOOM -> "A strong economic boom lifted property values and investment returns this year."
        EconomicClimate.RECESSION -> "An economic recession pulled asset prices down and tightened job markets."
        EconomicClimate.INFLATION_SURGE -> "An inflation surge drove up consumer prices, living costs, and interest rates."
        EconomicClimate.STEADY -> ""
    }

    sealed class TaxPolicyResult {
        data class Success(val character: Character) : TaxPolicyResult()
        data object Ineligible : TaxPolicyResult()
        data object AlreadyActive : TaxPolicyResult()
    }

    /**
     * Governors and presidents may enact tax policy.
     * Tax cuts boost business revenue (via [EconomyScaler]) but lower approval.
     * Wealth taxes raise approval but seize a slice of liquid cash.
     */
    fun passTaxPolicy(character: Character, type: TaxPolicyType): TaxPolicyResult {
        val office = character.politics.currentOffice
        if (office != PoliticalOffice.GOVERNOR && office != PoliticalOffice.PRESIDENT) {
            return TaxPolicyResult.Ineligible
        }
        if (character.politics.activeTaxPolicy == type) {
            return TaxPolicyResult.AlreadyActive
        }

        return when (type) {
            TaxPolicyType.TAX_CUTS -> {
                val approval = (character.politics.approvalRating - TAX_CUTS_APPROVAL_HIT)
                    .coerceIn(0, 100)
                TaxPolicyResult.Success(
                    character.copy(
                        politics = character.politics.copy(
                            activeTaxPolicy = TaxPolicyType.TAX_CUTS,
                            approvalRating = approval
                        ),
                        eventLog = EventLogCap.prepend(
                            character.eventLog,
                            "You passed tax cuts. Businesses cheer; the public is less impressed."
                        )
                    )
                )
            }
            TaxPolicyType.WEALTH_TAX -> {
                val fraction = EconomyScaler.policyWealthTaxCashFraction(TaxPolicyType.WEALTH_TAX)
                val cashHit = (character.stats.money * fraction).toInt().coerceAtLeast(0)
                val approval = (character.politics.approvalRating + WEALTH_TAX_APPROVAL_GAIN)
                    .coerceIn(0, 100)
                TaxPolicyResult.Success(
                    character.copy(
                        stats = character.stats.copy(
                            money = (character.stats.money - cashHit).coerceAtLeast(0)
                        ),
                        politics = character.politics.copy(
                            activeTaxPolicy = TaxPolicyType.WEALTH_TAX,
                            approvalRating = approval
                        ),
                        eventLog = EventLogCap.prepend(
                            character.eventLog,
                            "You passed a wealth tax. Public approval rose; your accounts lost " +
                                "${formatMoney(cashHit, character.countryCode)}."
                        )
                    )
                )
            }
        }
    }

    /**
     * Calculates transparent annual cash flow breakdown for [character].
     * Computes Gross Income (salary, part-time, side hustles, rentals, dividends, pension, business, social media)
     * minus Deductions (progressive tax, living cost, mortgages, student loans, asset upkeep, child support, pets).
     */
    fun computeAnnualCashFlow(character: Character): AnnualCashFlow {
        val salary = if (!character.career.isRetired && character.career.currentJob != null) {
            character.career.currentJob!!.baseSalary
        } else 0
        val partTime = 0
        val sideHustle = 0
        val rental = character.assets.filter { it.isRentedOut && isRentable(it) }.sumOf { calculateYearlyRent(it) }
        val dividends = if (character.savingsBalance > 0 && character.lastSavingsInterestPercent > 0) {
            ((character.savingsBalance * character.lastSavingsInterestPercent) / 100.0).toInt()
        } else 0
        val pension = if (character.career.isRetired) character.career.pensionAmount else 0
        val businessProfits = character.businesses.sumOf { b ->
            (b.revenue * 0.10f).roundToInt().coerceAtLeast(0)
        }
        val socialMedia = if (character.socialMedia.hasAccount && character.socialMedia.followers >= 100_000) {
            calculateSocialMediaPayout(character)
        } else 0
        val other = 0

        val gross = salary + partTime + sideHustle + rental + dividends + pension + businessProfits + socialMedia + other

        val taxableIncome = (salary + partTime + sideHustle + rental + businessProfits + socialMedia).coerceAtLeast(0)
        val tax = calculateIncomeTax(taxableIncome, character.countryCode)
        val living = if (character.age >= MIN_COST_OF_LIVING_AGE) estimateAnnualCostOfLiving(character) else 0
        val mortgage = character.assets.sumOf { if (it.mortgageBalance > 0) minOf(it.annualMortgagePayment, it.mortgageBalance) else 0 }
        val studentLoan = if (character.education.studentLoanBalance > 0 && character.alive && salary > 0) {
            (salary * StudentFinance.LOAN_REPAY_SALARY_FRACTION).roundToInt().coerceAtMost(character.education.studentLoanBalance)
        } else 0
        val assetUpkeep = if (character.assets.isNotEmpty()) {
            val raw = character.assets.sumOf { it.monthlyUpkeep * 12 }
            val stackDiscount = when {
                character.assets.size <= 1 -> 1.0
                character.assets.size == 2 -> 0.95
                character.assets.size == 3 -> 0.90
                else -> 0.85
            }
            (raw * stackDiscount * upkeepMultiplier(character.economicState.climate)).toInt()
        } else 0
        val minorChildren = character.family.count {
            it.relation == RelationType.CHILD && it.alive && it.age < MINOR_CHILD_MAX_AGE
        }
        val childSupport = minorChildren * EconomyScaler.scaleAmount(CHILD_SUPPORT_BASE_KENYA, character.countryCode)
        val petUpkeep = character.pets.sumOf { pet ->
            val entry = PetCatalog.findBySpecies(pet.species) ?: return@sumOf 0
            EconomyScaler.scaleAmount(entry.yearlyUpkeep, character.countryCode)
        }
        val otherExpenses = 0

        val totalDeductions = tax + living + mortgage + studentLoan + assetUpkeep + childSupport + petUpkeep + otherExpenses
        val net = gross - totalDeductions

        return AnnualCashFlow(
            salaryIncome = salary,
            partTimeIncome = partTime,
            sideHustleIncome = sideHustle,
            rentalIncome = rental,
            investmentDividends = dividends,
            pensionIncome = pension,
            businessProfits = businessProfits,
            socialMediaIncome = socialMedia,
            otherIncome = other,
            grossIncome = gross,
            incomeTax = tax,
            livingCost = living,
            mortgagePayments = mortgage,
            studentLoanPayment = studentLoan,
            assetUpkeep = assetUpkeep,
            childSupport = childSupport,
            petUpkeep = petUpkeep,
            otherExpenses = otherExpenses,
            totalDeductions = totalDeductions,
            netCashFlow = net
        )
    }

    /**
     * Ticks credit score and stores the latest [AnnualCashFlow] snapshot on [Character.finance].
     */
    fun tickCreditScoreAndCashFlow(character: Character): Character {
        val cashFlow = computeAnnualCashFlow(character)
        if (character.age < MIN_COST_OF_LIVING_AGE) {
            return character.copy(finance = character.finance.copy(lastCashFlow = cashFlow))
        }

        var score = character.finance.creditScore
        var streak = character.finance.onTimePaymentStreak
        var missed = character.finance.missedPaymentsCount

        val hasDebt = character.assets.any { it.mortgageBalance > 0 } || character.education.studentLoanBalance > 0
        val hadShortfall = character.stats.money <= 0 && cashFlow.netCashFlow < 0

        if (hadShortfall) {
            missed += 1
            streak = 0
            score = (score - 35).coerceAtLeast(300)
        } else if (hasDebt) {
            streak += 1
            val boost = when {
                score < 600 -> 12
                score < 700 -> 8
                score < 750 -> 6
                else -> 4
            }
            score = (score + boost).coerceAtMost(850)
        } else {
            if (score < 680) {
                score = (score + 3).coerceAtMost(680)
            }
        }

        if (character.criminalRecord.hasRecord && !character.criminalRecord.recordExpunged) {
            val arrestPenalty = (character.criminalRecord.timesArrested * 15).coerceAtLeast(15)
            score = (score - arrestPenalty).coerceAtLeast(300)
        }

        val totalAnnualDebtService = cashFlow.mortgagePayments + cashFlow.studentLoanPayment
        if (cashFlow.grossIncome > 0 && (totalAnnualDebtService.toDouble() / cashFlow.grossIncome.toDouble()) > 0.40) {
            score = (score - 5).coerceAtLeast(300)
        }

        return character.copy(
            finance = character.finance.copy(
                creditScore = score,
                onTimePaymentStreak = streak,
                missedPaymentsCount = missed,
                lastCashFlow = cashFlow
            )
        )
    }

    companion object {
        private const val TAX_CUTS_APPROVAL_HIT = 8
        private const val WEALTH_TAX_APPROVAL_GAIN = 10
        const val FINANCE_TAG = "finance"
        const val REQUIRES_RENTAL_TAG = "requires_rental"
        const val REQUIRES_PORTFOLIO_TAG = "requires_portfolio"
        /** Severe crash floor for yearly portfolio tick. */
        const val PORTFOLIO_RETURN_MIN_PERCENT = -30
        /** Bull-run ceiling for yearly portfolio tick. */
        const val PORTFOLIO_RETURN_MAX_PERCENT = 40
        private const val RENT_YIELD_MIN = 0.05f
        private const val RENT_YIELD_MAX = 0.08f
        /** 30-year amortisation term in months. */
        private const val MORTGAGE_TERM_MONTHS = 360
        /** Fixed annual interest rate for mortgages (8%). */
        private const val MORTGAGE_ANNUAL_RATE = 0.08
        private const val RENTED_EXTRA_DEGRADATION = 3
        private const val STARTING_TENANT_HAPPINESS = 72
        private const val TENANT_HAPPINESS_DRIFT_MIN = -5
        private const val TENANT_HAPPINESS_DRIFT_MAX = 4
        private const val EVICTION_LEGAL_FEE_KENYA = 25_000
        private const val EVICTION_HAPPINESS_HIT = 4
        /** Severe neglect on a rental causes the tenant to leave. */
        private const val TENANT_LEAVE_CONDITION_DELTA = -25
        private const val FINANCE_EVENT_MONEY_THRESHOLD = 50_000
        private const val DEBT_HAPPINESS_PENALTY = 5
        private const val MIN_DEGRADATION = 2
        private const val MAX_DEGRADATION = 8
        private const val CRITICAL_CONDITION_THRESHOLD = 40
        private const val CRITICAL_FAILURE_CHANCE = 0.25f
        const val REPAIR_UI_THRESHOLD = 70
        private const val REPAIR_BASE_RATE = 0.18f
        private const val MIN_REPAIR_COST = 3_000
        const val RENOVATE_MIN_CONDITION = 80
        private const val MAX_RENOVATION_LEVEL = 3
        private const val RENOVATION_BASE_RATE = 0.22f
        private const val RENOVATION_VALUE_BOOST = 0.15f
        private const val RENOVATION_VALUE_MULTIPLIER_PER_LEVEL = 0.08f
        private const val MIN_RENOVATION_COST = 8_000
        private const val BOOM_CHANCE = 0.20f
        private const val RECESSION_CHANCE = 0.20f
        private const val INFLATION_CHANCE = 0.20f
        private const val BUST_CHANCE = 0.20f
        const val BOOM_MARKET_MODIFIER = 1.15f
        const val RECESSION_MARKET_MODIFIER = 0.88f
        const val BUST_MARKET_MODIFIER = 0.88f
        const val INFLATION_MARKET_MODIFIER = 1.05f
        const val STEADY_MARKET_MODIFIER = 1.0f
        const val NEUTRAL_MARKET_MODIFIER = 1.0f
        private const val BOOM_UPKEEP_MULTIPLIER = 1.12
        private const val RECESSION_UPKEEP_MULTIPLIER = 0.94
        private const val BUST_UPKEEP_MULTIPLIER = 0.94
        private const val INFLATION_UPKEEP_MULTIPLIER = 1.35
        private const val REAL_ESTATE_APPRECIATION_CHANCE = 0.35f
        private const val MAX_ANNUAL_APPRECIATION_RATE = 0.06f
        private const val HEIRLOOM_ANNUAL_APPRECIATION_RATE = 0.03f
        private const val CHILD_SUPPORT_BASE_KENYA = 12_000
        private const val MINOR_CHILD_MAX_AGE = 18
        private const val SOCIAL_MEDIA_BASE_PAYOUT_KENYA = 80_000
        private const val SOCIAL_MEDIA_PAYOUT_PER_100K_KENYA = 40_000
        private const val SAVINGS_INTEREST_MIN = 3
        private const val SAVINGS_INTEREST_MAX = 5
        private const val SAVINGS_INTEREST_BOOM_MIN = 4
        private const val SAVINGS_INTEREST_BOOM_MAX = 7
        private const val SAVINGS_INTEREST_BUST_MIN = 1
        private const val SAVINGS_INTEREST_BUST_MAX = 3
        private const val DEPENDENT_CHILD_COST_BASE = 14_000
        private const val MIN_COST_OF_LIVING_AGE = 6
        private const val FRUGAL_HAPPINESS_DRAIN = 2
        private const val COL_SHORTFALL_HAPPINESS = 6
        private const val COL_SHORTFALL_HEALTH = 3
        private const val LUXURY_SHORTFALL_EXTRA_HAPPINESS = 5
        private const val TAX_BRACKET_1 = 50_000
        private const val TAX_BRACKET_2 = 200_000
        private const val TAX_BRACKET_3 = 500_000
        private const val TAX_RATE_2 = 10
        private const val TAX_RATE_3 = 20
        private const val TAX_RATE_4 = 30

        fun calculateIncomeTax(grossPay: Int, countryCode: String): Int {
            if (grossPay <= 0) return 0
            val b1 = EconomyScaler.scaleAmount(TAX_BRACKET_1, countryCode)
            val b2 = EconomyScaler.scaleAmount(TAX_BRACKET_2, countryCode)
            val b3 = EconomyScaler.scaleAmount(TAX_BRACKET_3, countryCode)
            var remaining = grossPay
            var tax = 0
            val slice1 = minOf(remaining, b1)
            remaining -= slice1
            if (remaining <= 0) return 0
            val slice2 = minOf(remaining, b2 - b1)
            tax += (slice2 * TAX_RATE_2 / 100.0).toInt()
            remaining -= slice2
            if (remaining <= 0) return tax
            val slice3 = minOf(remaining, b3 - b2)
            tax += (slice3 * TAX_RATE_3 / 100.0).toInt()
            remaining -= slice3
            if (remaining <= 0) return tax
            tax += (remaining * TAX_RATE_4 / 100.0).toInt()
            return tax.coerceAtLeast(0)
        }
    }
}
