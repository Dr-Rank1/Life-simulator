// app/src/main/java/com/maisha/game/data/model/Economic.kt
package com.maisha.game.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Custom serializer for [EconomicClimate] allowing legacy values "NEUTRAL" and "BUST"
 * to map cleanly to [EconomicClimate.STEADY] and [EconomicClimate.RECESSION].
 */
object EconomicClimateSerializer : KSerializer<EconomicClimate> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("EconomicClimate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: EconomicClimate) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): EconomicClimate {
        val raw = decoder.decodeString().uppercase()
        return when (raw) {
            "BOOM" -> EconomicClimate.BOOM
            "STEADY", "NEUTRAL" -> EconomicClimate.STEADY
            "INFLATION_SURGE", "INFLATION" -> EconomicClimate.INFLATION_SURGE
            "RECESSION", "BUST" -> EconomicClimate.RECESSION
            else -> EconomicClimate.STEADY
        }
    }
}

/** Macro-economic climate for the current in-game year. */
@Serializable(with = EconomicClimateSerializer::class)
enum class EconomicClimate {
    BOOM,
    STEADY,
    INFLATION_SURGE,
    RECESSION;

    companion object {
        val NEUTRAL: EconomicClimate get() = STEADY
        val BUST: EconomicClimate get() = RECESSION
    }
}

/**
 * Yearly market conditions affecting asset values and upkeep.
 * Portfolio fields are also stored on [Character] for Room column mapping.
 */
@Serializable
data class EconomicState(
    val climate: EconomicClimate = EconomicClimate.STEADY,
    val marketModifier: Float = 1.0f
)

/**
 * Fictional investment portfolio (not casino gambling).
 * Value fluctuates yearly via [com.maisha.game.domain.FinanceEngine.applyPortfolioMarketTick].
 */
@Serializable
data class InvestmentPortfolio(
    val value: Int = 0,
    /** Last applied return percentage, e.g. -30 to +40. */
    val lastReturnPercent: Int = 0
)

/** Risk profile for the fictional stock portfolio on [Character]. */
@Serializable
enum class PortfolioStrategy {
    /** Lower volatility, smaller upside. */
    CONSERVATIVE,
    /** Default balanced market exposure. */
    BALANCED,
    /** Higher volatility, larger upside. */
    AGGRESSIVE
}
