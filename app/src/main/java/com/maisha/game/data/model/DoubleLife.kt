package com.maisha.game.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class SecretIdentityType(
    val title: String,
    val description: String,
    val minAge: Int,
    val defaultCoverStory: String
) {
    UNDERCOVER_SYNDICATE(
        title = "Underworld Syndicate",
        description = "Run a hidden shadow hustle or street crew beneath a respectable citizen facade.",
        minAge = 16,
        defaultCoverStory = "Late-Night Consulting & Logistics"
    ),
    SECRET_ROMANCE(
        title = "Parallel Romance",
        description = "Maintain a passionate, illicit romance hidden from your primary partner or coworkers.",
        minAge = 18,
        defaultCoverStory = "Extended Gym Routine & Hobbies"
    ),
    HIDDEN_CREATOR(
        title = "Masked Creator Persona",
        description = "Operate an anonymous viral internet identity or underground artist persona.",
        minAge = 14,
        defaultCoverStory = "Private Coding & Audio Editing"
    ),
    SHADOW_OPERATOR(
        title = "Corporate / Campus Spy",
        description = "Funnel confidential intel or trade secrets to rival organizations for lucrative kickbacks.",
        minAge = 18,
        defaultCoverStory = "Overtime Archive Auditing"
    ),
    BLACKMAIL_VAULT(
        title = "Compromising Dossier",
        description = "Hold devastating dirt on powerful figures to extract annual hush money and immunity.",
        minAge = 18,
        defaultCoverStory = "Legal Research & Private Filing"
    )
}

@Serializable
data class SecretIdentity(
    val type: SecretIdentityType,
    val codename: String,
    val coverStory: String,
    /** Exposure risk meter (0–100). High risk forces scrutiny confrontations. */
    val exposureRisk: Int = 15,
    /** Suspicion level of peers, authorities, or partner (0–100). */
    val suspicionLevel: Int = 20,
    /** Quality of alibis and cover tracks (0–100). Buffers against exposure rolls. */
    val alibiStrength: Int = 75,
    /** Scaled cash or resource yield generated per year. */
    val annualPayoff: Int = 5_000,
    /** Total accumulated earnings or proceeds from this identity. */
    val totalEarningsCollected: Int = 0,
    /** In-game years this secret identity has been active. */
    val yearsActive: Int = 0,
    /** Action (alibi, scrub, exploit) already performed this year. */
    val actionDoneThisYear: Boolean = false,
    /** Active confrontation awaiting player decision when cover cracks. */
    val pendingConfrontation: SecretConfrontation? = null
)

@Serializable
data class SecretConfrontation(
    val triggerReason: String,
    val confrontedBy: String,
    val severity: Int = 50,
    val silenceBribeCost: Int = 8_000
)

@Serializable
enum class ConfrontationChoice {
    DOUBLE_DOWN,
    BRIBE_SILENCE,
    CUT_TIES_VANISH,
    CONFESS_FALLOUT
}

@Serializable
data class ExposedSecretRecord(
    val type: SecretIdentityType,
    val codename: String,
    val yearExposed: Int,
    val falloutSummary: String
)

@Serializable
data class DoubleLifeState(
    val activeSecret: SecretIdentity? = null,
    val exposedHistory: List<ExposedSecretRecord> = emptyList(),
    val alibisPlantedTotal: Int = 0,
    val successfulBluffs: Int = 0
)
