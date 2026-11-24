// app/src/main/java/com/maisha/game/domain/SocialMediaEngine.kt
package com.maisha.game.domain

import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.FameTier
import com.maisha.game.data.model.SocialMediaState
import com.maisha.game.util.clampStat
import com.maisha.game.util.formatMoney
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlin.random.Random

sealed class SocialMediaResult {
    data class Success(
        val character: Character,
        val followersGained: Int = 0,
        val payout: Int = 0,
        val wentViral: Boolean = false,
        val message: String? = null
    ) : SocialMediaResult()

    data class Failed(val reason: SocialMediaFailure) : SocialMediaResult()
}

enum class SocialMediaFailure {
    ALREADY_HAS_ACCOUNT,
    NO_ACCOUNT,
    INELIGIBLE,
    BELOW_MONETIZATION_THRESHOLD,
    ALREADY_MONETIZED_THIS_YEAR,
    BELOW_SPONSORSHIP_THRESHOLD,
    ALREADY_SPONSORED_THIS_YEAR,
    ACTION_ALREADY_DONE
}

@Singleton
class SocialMediaEngine @Inject constructor(
    private val financeEngine: FinanceEngine,
    private val quoteCatalog: com.maisha.game.data.content.QuoteCatalog =
        com.maisha.game.data.content.FallbackQuoteCatalog
) {

    fun createAccount(character: Character): SocialMediaResult {
        if (!character.alive ||
            character.age < MIN_ACCOUNT_AGE ||
            character.criminalRecord.currentlyIncarcerated ||
            character.criminalRecord.awaitingTrial
        ) {
            return SocialMediaResult.Failed(SocialMediaFailure.INELIGIBLE)
        }
        if (character.socialMedia.hasAccount) {
            return SocialMediaResult.Failed(SocialMediaFailure.ALREADY_HAS_ACCOUNT)
        }
        val updated = character.copy(
            socialMedia = SocialMediaState(hasAccount = true),
            eventLog = EventLogCap.prepend(
                character.eventLog,
                "Created a verified-ready social media account."
            )
        )
        return SocialMediaResult.Success(updated, message = "Created social media account.")
    }

    fun deleteAccount(character: Character): SocialMediaResult {
        if (!character.socialMedia.hasAccount) {
            return SocialMediaResult.Failed(SocialMediaFailure.NO_ACCOUNT)
        }
        val updated = character.copy(
            socialMedia = SocialMediaState(),
            eventLog = EventLogCap.prepend(
                character.eventLog,
                "Deleted your social media account."
            )
        )
        return SocialMediaResult.Success(updated, message = "Deleted social media account.")
    }

    /**
     * Posts content and grows followers from looks/smarts, with viral chance and burnout tracking.
     */
    fun postContent(character: Character): SocialMediaResult {
        if (!canUseSocialMedia(character)) {
            return SocialMediaResult.Failed(SocialMediaFailure.INELIGIBLE)
        }
        if (!character.socialMedia.hasAccount) {
            return SocialMediaResult.Failed(SocialMediaFailure.NO_ACCOUNT)
        }

        val wentViral = Random.nextFloat() < VIRAL_CHANCE
        val growth = calculateFollowerGrowth(character, wentViral)
        val newFollowers = (character.socialMedia.followers + growth).coerceAtLeast(0)
        val isVerified = character.socialMedia.isVerified || newFollowers >= VERIFIED_FOLLOWER_THRESHOLD
        val caption = quoteCatalog.nextQuote()
        val newBurnout = (character.socialMedia.creatorBurnout + if (wentViral) 12 else 6).coerceAtMost(100)

        val logLine = if (wentViral) {
            "A post went viral! Gained $growth followers. \"$caption\""
        } else {
            "Posted an update. Gained $growth followers. \"$caption\""
        }
        val withFollowers = character.copy(
            socialMedia = character.socialMedia.copy(
                followers = newFollowers,
                isVerified = isVerified,
                creatorBurnout = newBurnout,
                postDoneThisYear = true
            ),
            eventLog = EventLogCap.prepend(character.eventLog, logLine)
        )
        val updated = syncFameTier(withFollowers)
        return SocialMediaResult.Success(
            character = updated,
            followersGained = growth,
            wentViral = wentViral,
            message = logLine
        )
    }

    /**
     * Sign a brand sponsorship deal for immediate creator payout.
     */
    fun acceptBrandSponsorship(character: Character): SocialMediaResult {
        if (!canUseSocialMedia(character)) {
            return SocialMediaResult.Failed(SocialMediaFailure.INELIGIBLE)
        }
        if (!character.socialMedia.hasAccount) {
            return SocialMediaResult.Failed(SocialMediaFailure.NO_ACCOUNT)
        }
        if (character.socialMedia.followers < SPONSORSHIP_MIN_FOLLOWERS) {
            return SocialMediaResult.Failed(SocialMediaFailure.BELOW_SPONSORSHIP_THRESHOLD)
        }
        if (character.socialMedia.brandDealDoneThisYear) {
            return SocialMediaResult.Failed(SocialMediaFailure.ALREADY_SPONSORED_THIS_YEAR)
        }

        val payout = calculateBrandDealPayout(character)
        val withCash = financeEngine.applySocialMediaRevenue(character, payout)
        val partnerName = brandPartnerName(character.socialMedia.fameTier)
        val newBurnout = (character.socialMedia.creatorBurnout + 14).coerceAtMost(100)

        val logText = "Signed a sponsored campaign with $partnerName, earning ${formatMoney(payout, character.countryCode)}."
        val updated = withCash.copy(
            socialMedia = withCash.socialMedia.copy(
                activeBrandDeal = partnerName,
                brandDealDoneThisYear = true,
                creatorBurnout = newBurnout
            ),
            stats = withCash.stats.copy(happiness = clampStat(withCash.stats.happiness + 5)),
            eventLog = EventLogCap.prepend(withCash.eventLog, logText)
        )
        return SocialMediaResult.Success(
            character = updated,
            payout = payout,
            message = logText
        )
    }

    /**
     * Collaborates with another prominent content creator.
     */
    fun collaborateWithCreator(character: Character): SocialMediaResult {
        if (!canUseSocialMedia(character)) {
            return SocialMediaResult.Failed(SocialMediaFailure.INELIGIBLE)
        }
        if (!character.socialMedia.hasAccount) {
            return SocialMediaResult.Failed(SocialMediaFailure.NO_ACCOUNT)
        }
        if (character.socialMedia.collabDoneThisYear) {
            return SocialMediaResult.Failed(SocialMediaFailure.ACTION_ALREADY_DONE)
        }

        val baseGrowth = calculateFollowerGrowth(character, wentViral = false) * 3
        val tierBonus = (character.socialMedia.followers * 0.08f).roundToInt()
        val totalGrowth = (baseGrowth + tierBonus).coerceAtLeast(150)
        val newFollowers = character.socialMedia.followers + totalGrowth
        val newBurnout = (character.socialMedia.creatorBurnout + 8).coerceAtMost(100)
        val newReputation = (character.socialMedia.reputation + 4).coerceAtMost(100)
        val isVerified = character.socialMedia.isVerified || newFollowers >= VERIFIED_FOLLOWER_THRESHOLD

        val logText = "Produced a high-profile collaborative video project! Gained $totalGrowth new followers and strengthened creator ties."
        val updated = character.copy(
            socialMedia = character.socialMedia.copy(
                followers = newFollowers,
                isVerified = isVerified,
                collabDoneThisYear = true,
                creatorBurnout = newBurnout,
                reputation = newReputation
            ),
            eventLog = EventLogCap.prepend(character.eventLog, logText)
        )
        val synced = syncFameTier(updated)
        return SocialMediaResult.Success(
            character = synced,
            followersGained = totalGrowth,
            message = logText
        )
    }

    /**
     * Digital detox: disengages from notifications, drastically reducing creator burnout.
     */
    fun takeDigitalDetox(character: Character): SocialMediaResult {
        if (!character.socialMedia.hasAccount) {
            return SocialMediaResult.Failed(SocialMediaFailure.NO_ACCOUNT)
        }
        if (character.socialMedia.detoxDoneThisYear) {
            return SocialMediaResult.Failed(SocialMediaFailure.ACTION_ALREADY_DONE)
        }

        val newBurnout = (character.socialMedia.creatorBurnout - 35).coerceAtLeast(0)
        val logText = "Unplugged for a 2-week digital detox retreat. Reset screen habits and relieved mental fatigue."
        val updated = character.copy(
            socialMedia = character.socialMedia.copy(
                creatorBurnout = newBurnout,
                detoxDoneThisYear = true
            ),
            stats = character.stats.copy(
                happiness = clampStat(character.stats.happiness + 8),
                health = clampStat(character.stats.health + 4)
            ),
            eventLog = EventLogCap.prepend(character.eventLog, logText)
        )
        return SocialMediaResult.Success(character = updated, message = logText)
    }

    /**
     * Handles public drama: apologize with humility vs double down with hot takes.
     */
    fun handleDrama(character: Character, apologize: Boolean): SocialMediaResult {
        if (!character.socialMedia.hasAccount) {
            return SocialMediaResult.Failed(SocialMediaFailure.NO_ACCOUNT)
        }

        return if (apologize) {
            val newReputation = (character.socialMedia.reputation + 15).coerceAtMost(100)
            val logText = "Posted a sincere apology addressing recent online friction. Preserved audience trust and cleared controversy."
            val updated = character.copy(
                socialMedia = character.socialMedia.copy(
                    reputation = newReputation,
                    dramaActive = false,
                    scandalMessage = null
                ),
                stats = character.stats.copy(karma = clampStat(character.stats.karma + 5)),
                eventLog = EventLogCap.prepend(character.eventLog, logText)
            )
            SocialMediaResult.Success(character = updated, message = logText)
        } else {
            val followerJump = (character.socialMedia.followers * 0.08f).roundToInt().coerceAtLeast(200)
            val newReputation = (character.socialMedia.reputation - 22).coerceAtLeast(5)
            val newBurnout = (character.socialMedia.creatorBurnout + 20).coerceAtMost(100)
            val logText = "Doubled down on the drama with spicy commentary! Gained $followerJump rage-followers, but reputation took a hit."
            val updated = character.copy(
                socialMedia = character.socialMedia.copy(
                    followers = character.socialMedia.followers + followerJump,
                    reputation = newReputation,
                    creatorBurnout = newBurnout,
                    dramaActive = false,
                    scandalMessage = null
                ),
                stats = character.stats.copy(
                    happiness = clampStat(character.stats.happiness - 4),
                    karma = clampStat(character.stats.karma - 8)
                ),
                eventLog = EventLogCap.prepend(character.eventLog, logText)
            )
            SocialMediaResult.Success(character = updated, followersGained = followerJump, message = logText)
        }
    }

    /**
     * Yearly ad-revenue payout when followers exceed [MONETIZATION_FOLLOWER_THRESHOLD].
     */
    fun monetizeAccount(character: Character): SocialMediaResult {
        if (!canUseSocialMedia(character)) {
            return SocialMediaResult.Failed(SocialMediaFailure.INELIGIBLE)
        }
        if (!character.socialMedia.hasAccount) {
            return SocialMediaResult.Failed(SocialMediaFailure.NO_ACCOUNT)
        }
        if (character.socialMedia.followers < MONETIZATION_FOLLOWER_THRESHOLD) {
            return SocialMediaResult.Failed(SocialMediaFailure.BELOW_MONETIZATION_THRESHOLD)
        }
        if (character.socialMedia.monetizedThisYear) {
            return SocialMediaResult.Failed(SocialMediaFailure.ALREADY_MONETIZED_THIS_YEAR)
        }

        val payout = financeEngine.calculateSocialMediaPayout(character)
        val withCash = financeEngine.applySocialMediaRevenue(character, payout)
        val updated = withCash.copy(
            socialMedia = withCash.socialMedia.copy(monetizedThisYear = true),
            eventLog = EventLogCap.prepend(
                withCash.eventLog,
                "Monetized your account and collected ${formatMoney(payout, character.countryCode)} in ad revenue."
            )
        )
        return SocialMediaResult.Success(character = updated, payout = payout)
    }

    fun canUseSocialMedia(character: Character): Boolean =
        character.alive &&
            character.age >= MIN_ACCOUNT_AGE &&
            !character.criminalRecord.currentlyIncarcerated &&
            !character.criminalRecord.awaitingTrial

    /**
     * Resets yearly action flags on age-up and applies follower churn / burnout mechanics.
     */
    fun resetYearlyFlags(character: Character): Character {
        if (!character.socialMedia.hasAccount) return character

        var followers = character.socialMedia.followers
        var burnout = (character.socialMedia.creatorBurnout - 12).coerceAtLeast(0)
        var happiness = character.stats.happiness
        var log = character.eventLog

        // Burnout penalty
        if (character.socialMedia.creatorBurnout >= 80) {
            happiness = clampStat(happiness - 8)
            val lost = (followers * 0.05f).roundToInt()
            followers = (followers - lost).coerceAtLeast(0)
            log = EventLogCap.prepend(
                log,
                "Creator burnout took a heavy toll. Audience disengaged and lost $lost followers."
            )
        }

        // Reputation word-of-mouth momentum
        if (character.socialMedia.reputation >= 85 && followers >= 1_000) {
            val organicGrowth = (followers * 0.05f).roundToInt()
            followers += organicGrowth
            log = EventLogCap.prepend(
                log,
                "High creator reputation spurred word-of-mouth recommendations (+${organicGrowth} followers)."
            )
        }

        return character.copy(
            socialMedia = character.socialMedia.copy(
                followers = followers,
                creatorBurnout = burnout,
                monetizedThisYear = false,
                brandDealDoneThisYear = false,
                collabDoneThisYear = false,
                detoxDoneThisYear = false,
                postDoneThisYear = false
            ),
            stats = character.stats.copy(happiness = happiness),
            eventLog = log
        )
    }

    /** Recomputes [SocialMediaState.fameTier]; on tier-up grants happiness and a log line. */
    fun syncFameTier(character: Character): Character {
        if (!character.socialMedia.hasAccount) return character
        val tier = fameTierFor(character.socialMedia.followers)
        val previous = character.socialMedia.fameTier
        if (tier == previous) return character
        val rankedUp = tier.ordinal > previous.ordinal
        var updated = character.copy(
            socialMedia = character.socialMedia.copy(fameTier = tier)
        )
        if (rankedUp) {
            updated = updated.copy(
                stats = updated.stats.copy(
                    happiness = clampStat(updated.stats.happiness + FAME_TIER_HAPPINESS)
                ),
                eventLog = EventLogCap.prepend(
                    updated.eventLog,
                    "Fame rose to ${fameLabel(tier)} tier!"
                )
            )
        }
        return updated
    }

    fun fameTierFor(followers: Int): FameTier = when {
        followers >= GLOBAL_FOLLOWERS -> FameTier.GLOBAL
        followers >= NATIONAL_FOLLOWERS -> FameTier.NATIONAL
        followers >= REGIONAL_FOLLOWERS -> FameTier.REGIONAL
        followers >= LOCAL_FOLLOWERS -> FameTier.LOCAL
        else -> FameTier.UNKNOWN
    }

    fun fameLabel(tier: FameTier): String = when (tier) {
        FameTier.UNKNOWN -> "Unknown"
        FameTier.LOCAL -> "Local Star"
        FameTier.REGIONAL -> "Regional Influencer"
        FameTier.NATIONAL -> "National Celebrity"
        FameTier.GLOBAL -> "Global Icon"
    }

    fun calculateFollowerGrowth(character: Character, wentViral: Boolean = false): Int {
        val looks = character.stats.looks.coerceIn(0, 100)
        val smarts = character.stats.smarts.coerceIn(0, 100)
        val statFactor = (looks * LOOKS_WEIGHT + smarts * SMARTS_WEIGHT) / 100f
        val baseRoll = Random.nextInt(BASE_GROWTH_MIN, BASE_GROWTH_MAX + 1)
        var growth = (baseRoll * (0.35f + statFactor * 0.9f)).roundToInt().coerceAtLeast(1)
        if (wentViral) {
            growth = (growth * Random.nextInt(VIRAL_MULTIPLIER_MIN, VIRAL_MULTIPLIER_MAX + 1))
                .coerceAtMost(MAX_VIRAL_GROWTH)
        }
        return growth
    }

    fun calculateBrandDealPayout(character: Character): Int {
        val followers = character.socialMedia.followers.coerceAtLeast(SPONSORSHIP_MIN_FOLLOWERS)
        val reputationMultiplier = (character.socialMedia.reputation / 75f).coerceIn(0.6f, 1.5f)
        val basePayout = (followers * 0.15f * reputationMultiplier).roundToInt()
        return EconomyScaler.scaleAmount(basePayout.coerceIn(15_000, 5_000_000), character.countryCode)
    }

    private fun brandPartnerName(tier: FameTier): String = when (tier) {
        FameTier.UNKNOWN, FameTier.LOCAL -> "Local Streetwear Boutique"
        FameTier.REGIONAL -> "Pulse Energy Drinks"
        FameTier.NATIONAL -> "Apex Audio Tech"
        FameTier.GLOBAL -> "Luxe Global Haute Couture"
    }

    companion object {
        const val REQUIRES_SOCIAL_MEDIA_TAG = "requires_social_media"
        const val MONETIZATION_FOLLOWER_THRESHOLD = 100_000
        const val SPONSORSHIP_MIN_FOLLOWERS = 5_000
        const val VERIFIED_FOLLOWER_THRESHOLD = 50_000
        const val MIN_ACCOUNT_AGE = 13
        const val VIRAL_CHANCE = 0.03f
        private const val LOOKS_WEIGHT = 0.6f
        private const val SMARTS_WEIGHT = 0.4f
        private const val BASE_GROWTH_MIN = 45
        private const val BASE_GROWTH_MAX = 200
        private const val VIRAL_MULTIPLIER_MIN = 20
        private const val VIRAL_MULTIPLIER_MAX = 50
        private const val MAX_VIRAL_GROWTH = 250_000
        const val LOCAL_FOLLOWERS = 1_000
        const val REGIONAL_FOLLOWERS = 10_000
        const val NATIONAL_FOLLOWERS = 100_000
        const val GLOBAL_FOLLOWERS = 1_000_000
        private const val FAME_TIER_HAPPINESS = 4
    }
}
