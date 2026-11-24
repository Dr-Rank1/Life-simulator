package com.maisha.game.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class FameTier {
    UNKNOWN,
    LOCAL,
    REGIONAL,
    NATIONAL,
    GLOBAL
}

/**
 * Online presence for a [Character]: account, followers, reputation, burnout, and sponsorships.
 * Persisted as [com.maisha.game.data.local.CharacterEntity.socialMediaJson].
 */
@Serializable
data class SocialMediaState(
    val hasAccount: Boolean = false,
    val followers: Int = 0,
    val isVerified: Boolean = false,
    val monetizedThisYear: Boolean = false,
    /** Highest fame tier reached (for tier-up detection / UI). */
    val fameTier: FameTier = FameTier.UNKNOWN,
    /** Brand reputation and audience goodwill (0..100). */
    val reputation: Int = 75,
    /** Creator fatigue and mental strain (0..100). */
    val creatorBurnout: Int = 10,
    /** Current active brand partner or sponsorship. */
    val activeBrandDeal: String? = null,
    /** Whether an endorsement deal was claimed this year. */
    val brandDealDoneThisYear: Boolean = false,
    /** Whether a creator collaboration was produced this year. */
    val collabDoneThisYear: Boolean = false,
    /** Whether a digital detox retreat was taken this year. */
    val detoxDoneThisYear: Boolean = false,
    /** Whether a regular post was published this year. */
    val postDoneThisYear: Boolean = false,
    /** Active public drama or controversy status. */
    val dramaActive: Boolean = false,
    /** Summary of recent online controversy. */
    val scandalMessage: String? = null
)
