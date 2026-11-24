package com.maisha.game.data.model

import kotlinx.serialization.Serializable

/**
 * Risk tier of a country-specific savings or investment instrument.
 */
@Serializable
enum class FinancialRiskLevel {
    GUARANTEED,
    LOW,
    MODERATE,
    HIGH
}

/**
 * Country-specific financial vehicle (e.g. Kenya SACCO/Chama, US 401(k),
 * Nigeria T-Bills, UK ISA, South Africa Stokvel, Japan NISA, etc.).
 */
@Serializable
data class CountryFinancialInstrument(
    val id: String,
    val name: String,
    val localName: String,
    val description: String,
    /** Base annual dividend or yield (e.g. 0.115 for 11.5%). */
    val annualYieldPercent: Double,
    /** Minimum initial deposit in base Kenya currency units before EconomyScaler scaling. */
    val minimumDepositKenya: Int,
    val riskLevel: FinancialRiskLevel = FinancialRiskLevel.LOW,
    val isRetirementTaxAdvantaged: Boolean = false,
    val allowsOverdraft: Boolean = false,
    val overdraftLimitKenya: Int = 0
)

/**
 * Country-level personal finance characteristics including primary localized
 * savings instrument, family obligation rate (e.g. "black tax" / filial support),
 * and mobile money overdraft rules.
 */
@Serializable
data class CountryFinanceProfile(
    val countryCode: String,
    val primaryInstrument: CountryFinancialInstrument,
    /** Fraction of surplus net income expected for family/kin obligation (0.01 to 0.12). */
    val familyObligationFraction: Double = 0.04,
    val mobileOverdraftFacility: Boolean = false,
    val mobileOverdraftLimitKenya: Int = 0
)
