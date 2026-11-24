// app/src/main/java/com/maisha/game/data/model/Politics.kt
package com.maisha.game.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class PoliticalOffice {
    MAYOR,
    GOVERNOR,
    PRESIDENT
}

@Serializable
enum class TaxPolicyType {
    TAX_CUTS,
    WEALTH_TAX
}

@Serializable
enum class CampaignAgenda {
    INFRASTRUCTURE,
    EDUCATION,
    HEALTHCARE,
    TAX_RELIEF,
    ANTI_CORRUPTION
}

/**
 * Political career state for a [Character].
 * Persisted as [com.maisha.game.data.local.CharacterEntity.politicsJson].
 */
@Serializable
data class PoliticalState(
    val currentOffice: PoliticalOffice? = null,
    val approvalRating: Int = 50,
    val campaignFunds: Int = 0,
    val activeTaxPolicy: TaxPolicyType? = null,
    val yearsInOffice: Int = 0,
    val campaignedThisYear: Boolean = false,
    /** Polling / voter support percentage (0..100) while actively running. */
    val voterSupport: Int = 35,
    /** Target office for an active election campaign. */
    val campaignActiveFor: PoliticalOffice? = null,
    /** Core platform agenda for the campaign. */
    val campaignAgenda: CampaignAgenda = CampaignAgenda.INFRASTRUCTURE,
    /** Remaining years in current term (defaults to 4-year term). */
    val termYearsLeft: Int = 4,
    /** Lifetime terms completed in office. */
    val termsServed: Int = 0,
    /** Controversy / scandal meter (0..100). */
    val scandalHeat: Int = 0,
    /** Whether an executive or campaign action was executed this year. */
    val politicalActionDoneThisYear: Boolean = false
)
