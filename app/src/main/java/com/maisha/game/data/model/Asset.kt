// app/src/main/java/com/maisha/game/data/model/Asset.kt
package com.maisha.game.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class AssetType {
    HOUSE,
    CAR,
    MOTORBIKE,
    BOAT,
    PRIVATE_JET,
    HEIRLOOM
}

/**
 * Ownable property, vehicle, or generational heirloom.
 * [condition] affects [currentValue] via [com.maisha.game.domain.FinanceEngine.recalculateValue].
 * Heirlooms skip degradation and appreciate over time.
 * Real estate may be [isRentedOut] for passive income and tenant events.
 * Properties may have a mortgage tracked by [mortgageBalance] and [monthlyMortgagePayment].
 */
@Serializable
data class Asset(
    val id: String,
    /** [com.maisha.game.data.AssetCatalog] id when bought or granted; used for per-item art. */
    val catalogId: String? = null,
    val type: AssetType,
    val name: String,
    val purchasePrice: Int,
    val currentValue: Int,
    val condition: Int = 100,
    val monthlyUpkeep: Int,
    val isHeirloom: Boolean = false,
    /** [Character.generationNumber] when this heirloom entered the family. */
    val generationAcquired: Int = 1,
    /** True when this real-estate asset is leased to a tenant. */
    val isRentedOut: Boolean = false,
    /** Tenant satisfaction 0–100 when [isRentedOut]; null when vacant. */
    val tenantHappiness: Int? = null,
    /** Home upgrades (0–3): boosts value and tenant appeal. */
    val renovationLevel: Int = 0,
    /** Outstanding mortgage principal; 0 means fully owned. */
    val mortgageBalance: Int = 0,
    /** Fixed annual mortgage payment (principal + interest); 0 when no mortgage. */
    val annualMortgagePayment: Int = 0,
    /** Fixed annual mortgage interest rate (e.g. 0.08 for 8%). */
    val mortgageInterestRate: Double = 0.08,
    /** Preventative maintenance / servicing performed this year. */
    val servicedThisYear: Boolean = false
)
