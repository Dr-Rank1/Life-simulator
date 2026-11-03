// app/src/main/java/com/maisha/game/data/model/Crime.kt
package com.maisha.game.data.model

import kotlinx.serialization.Serializable

/**
 * Criminal history, trial, and incarceration state.
 *
 * Defaults to empty record on new characters. [lastArrestAge] drives clean-streak hire penalty reduction in [com.maisha.game.domain.CareerEngine].
 */
@Serializable
data class CriminalRecord(
    val hasRecord: Boolean = false,
    val timesArrested: Int = 0,
    val currentlyIncarcerated: Boolean = false,
    val yearsRemaining: Int = 0,
    /** Age when last arrested; used for clean-streak hire penalty reduction. */
    val lastArrestAge: Int? = null,
    /** Failed/successful crime tries in the current in-game year; resets on age-up. */
    val crimeAttemptsThisYear: Int = 0,
    /** True after arrest until the player picks legal representation at trial. */
    val awaitingTrial: Boolean = false,
    /** [CrimeType.name] pending resolution at trial. */
    val pendingCrimeType: String? = null,
    /** Full sentence handed down at trial; used for parole eligibility. */
    val totalSentenceYears: Int = 0,
    /** Years already served in the current sentence. */
    val yearsServed: Int = 0,
    /** Bad prison-event choices that disqualify parole for this sentence. */
    val negativePrisonEvents: Int = 0,
    /** Good prison-event choices that boost yearly parole roll chance. */
    val paroleBonus: Int = 0,
    /** Record cleared via legal expungement — hire penalty waived. */
    val recordExpunged: Boolean = false,
    /** Criminal street reputation / infamy (0–100). Unlocks high-tier heists and prison crew standing. */
    val notoriety: Int = 0
)

@Serializable
enum class CrimeType {
    PICKPOCKET,
    SHOPLIFT,
    FRAUD
}

@Serializable
enum class CrimeTarget(
    val category: String,
    val difficulty: Float,
    val baseReward: Int,
    val notorietyGain: Int
) {
    BURGLARY_APARTMENT("Burglary", 0.35f, 2_000, 6),
    BURGLARY_SUBURB("Burglary", 0.55f, 10_000, 14),
    BURGLARY_MANSION("Burglary", 0.80f, 60_000, 28),

    GTA_SEDAN("Grand Theft Auto", 0.30f, 3_500, 8),
    GTA_LUXURY_SUV("Grand Theft Auto", 0.60f, 22_000, 18),
    GTA_SUPERCAR("Grand Theft Auto", 0.85f, 120_000, 35),

    HEIST_CONVENIENCE("Armed Heist", 0.40f, 4_000, 12),
    HEIST_JEWELRY("Armed Heist", 0.70f, 65_000, 32),
    HEIST_BANK_VAULT("Armed Heist", 0.90f, 350_000, 50)
}

@Serializable
enum class LawyerTier {
    PUBLIC_DEFENDER,
    AVERAGE,
    EXPENSIVE
}

/** Voluntary activities while [CriminalRecord.currentlyIncarcerated]. */
@Serializable
enum class PrisonActivity {
    WORK_DETAIL,
    LIBRARY,
    EXERCISE,
    GOOD_BEHAVIOR,
    PRISON_YARD_GANG,
    PRISON_ESCAPE_ATTEMPT,
    PAROLE_HEARING_APPEAL
}
