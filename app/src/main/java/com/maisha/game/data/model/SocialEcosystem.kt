package com.maisha.game.data.model

import kotlinx.serialization.Serializable

/**
 * Generational reputation and public perception carried down through family lines.
 */
@Serializable
enum class FamilyHeritageTag {
    HUMBLE_ROOTS,
    HONORED_PHILANTHROPISTS,
    PROSPEROUS_DYNASTY,
    SCANDAL_TAINTED,
    RUTHLESS_MOGULS
}

/**
 * Public community reputation tiers within the player's neighborhood and social circle.
 */
@Serializable
enum class CommunityStandingTier {
    PILLAR_OF_SOCIETY,
    WELL_RESPECTED,
    RESPECTABLE,
    DISTRUSTED,
    NOTORIOUS_PARIAH;

    companion object {
        fun fromScore(score: Int): CommunityStandingTier = when {
            score >= 80 -> PILLAR_OF_SOCIETY
            score >= 65 -> WELL_RESPECTED
            score >= 45 -> RESPECTABLE
            score >= 25 -> DISTRUSTED
            else -> NOTORIOUS_PARIAH
        }
    }
}

/**
 * Specific relational bond connecting an NPC to the player's personal or family sphere.
 */
@Serializable
enum class SocialBondType {
    NONE,
    RECURRING_RIVAL,
    BENEFACTOR_MENTOR,
    FAMILY_FEUD,
    FAMILY_ALLY
}

/**
 * Tactical actions available when dealing with an active family feud.
 */
@Serializable
enum class FeudAction {
    OFFER_OLIVE_BRANCH,
    ESCALATE_FEUD,
    LEGAL_INJUNCTION
}

/**
 * Favors the player can request from a family benefactor or mentor.
 */
@Serializable
enum class BenefactorFavorType {
    CAREER_RECOMMENDATION,
    FINANCIAL_BACKING,
    COMMUNITY_DEFENSE
}

/**
 * Represents a persistent or ancestral feud between the player's household and a rival clan.
 *
 * @property intensity Volatility index [0–100]. Higher values cause hostility and events.
 * @property ancestral Whether this feud was inherited from previous generations.
 */
@Serializable
data class FamilyFeud(
    val id: String,
    val rivalFamilyName: String,
    val rivalPersonId: String? = null,
    val intensity: Int = 50,
    val originReason: String,
    val yearsActive: Int = 0,
    val ancestral: Boolean = false
)

/**
 * Represents a long-term bond of patronage or solidarity between families.
 *
 * @property trustLevel Loyalty and favor willingness [0–100].
 * @property ancestral Whether this alliance was established by previous generations.
 */
@Serializable
data class FamilyAlliance(
    val id: String,
    val allyFamilyName: String,
    val allyPersonId: String? = null,
    val trustLevel: Int = 70,
    val pactReason: String,
    val yearsActive: Int = 0,
    val ancestral: Boolean = false
)

/**
 * Complete social ecosystem state attached to a [Character].
 *
 * Persisted in Character saves. Defaults ensure backward compatibility with older saves.
 */
@Serializable
data class SocialEcosystemState(
    val communityStandingScore: Int = 50,
    val familyHeritage: FamilyHeritageTag = FamilyHeritageTag.HUMBLE_ROOTS,
    val activeFeuds: List<FamilyFeud> = emptyList(),
    val activeAlliances: List<FamilyAlliance> = emptyList(),
    val socialActionDoneThisYear: Boolean = false,
    val neighborhoodFavorsRemaining: Int = 1,
    val pastAncestralVictories: Int = 0
) {
    val currentStandingTier: CommunityStandingTier
        get() = CommunityStandingTier.fromScore(communityStandingScore)
}
