// app/src/main/java/com/maisha/game/data/model/PersonalFinance.kt
package com.maisha.game.data.model

import kotlinx.serialization.Serializable

/**
 * Breakdown of a character's annual cash flow computed during yearly progression.
 * All amounts are scaled in local currency units.
 */
@Serializable
data class AnnualCashFlow(
    val salaryIncome: Int = 0,
    val partTimeIncome: Int = 0,
    val sideHustleIncome: Int = 0,
    val rentalIncome: Int = 0,
    val investmentDividends: Int = 0,
    val pensionIncome: Int = 0,
    val businessProfits: Int = 0,
    val socialMediaIncome: Int = 0,
    val otherIncome: Int = 0,
    val grossIncome: Int = 0,
    val incomeTax: Int = 0,
    val livingCost: Int = 0,
    val mortgagePayments: Int = 0,
    val studentLoanPayment: Int = 0,
    val assetUpkeep: Int = 0,
    val childSupport: Int = 0,
    val petUpkeep: Int = 0,
    val otherExpenses: Int = 0,
    val totalDeductions: Int = 0,
    val netCashFlow: Int = 0
)

/**
 * Standard credit score tiers (300 to 850 range).
 * Governs mortgage qualification, loan approval, down-payment requirements, and interest rates.
 */
@Serializable
enum class CreditRatingTier(val minScore: Int, val maxScore: Int) {
    EXCELLENT(750, 850),
    GOOD(670, 749),
    FAIR(580, 669),
    POOR(300, 579);

    companion object {
        fun forScore(score: Int): CreditRatingTier = when {
            score >= 750 -> EXCELLENT
            score >= 670 -> GOOD
            score >= 580 -> FAIR
            else -> POOR
        }
    }
}

/**
 * Persisted state of a character's personal financial standing and history.
 */
@Serializable
data class PersonalFinanceState(
    val creditScore: Int = 650,
    val juniorTrustBalance: Int = 0,
    val onTimePaymentStreak: Int = 0,
    val missedPaymentsCount: Int = 0,
    val lastCashFlow: AnnualCashFlow = AnnualCashFlow()
)
