// app/src/main/java/com/maisha/game/data/model/Business.kt
package com.maisha.game.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class BusinessIndustry {
    TECH,
    RETAIL,
    FOOD,
    REAL_ESTATE,
    ENTERTAINMENT
}

@Serializable
enum class CompanyStage {
    BOOTSTRAP,
    SEED,
    GROWTH,
    EXPANSION,
    ENTERPRISE
}

/**
 * Player-owned company. Yearly profit/loss, expenses, stage, and valuation are updated by
 * [com.maisha.game.domain.BusinessEngine.processBusinessYear].
 */
@Serializable
data class Business(
    val id: String,
    val name: String,
    val industry: BusinessIndustry,
    val valuation: Int,
    val revenue: Int,
    val employeeCount: Int,
    /** Most recent yearly profit (positive) or loss (negative). */
    val lastYearProfit: Int = 0,
    val stage: CompanyStage = CompanyStage.BOOTSTRAP,
    /** Annual burn rate / operating expenses. */
    val annualBurn: Int = 0,
    /** Player equity percentage (0..100). */
    val equityShare: Int = 100,
    /** Team morale and culture (0..100). */
    val teamMorale: Int = 75,
    /** Product/service quality rating (0..100). */
    val productQuality: Int = 60,
    /** Whether an executive action has been taken this in-game year. */
    val actionDoneThisYear: Boolean = false
)
