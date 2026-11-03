package com.maisha.game.domain

import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.Asset
import com.maisha.game.data.model.AssetType
import com.maisha.game.data.model.Character
import com.maisha.game.util.formatMoney
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

enum class LeisureActivity {
    /** Toddler: bedtime stories. */
    STORYTIME,
    /** Toddler: outing with a caregiver. */
    PARK_CAREGIVER,
    /** Toddler: nap / routine. */
    NAP_ROUTINE,
    /** Toddler: playdate. */
    PLAYDATE,
    /** Child/teen: outdoor play. */
    PLAYGROUND,
    /** School-age study session. */
    STUDY_BUDDY,
    /** Chores for a small cash tip. */
    CHORES,
    NIGHT_OUT,
    NATURE_DAY,
    CITY_SHOW,
    SPA_DAY,
    /** Mid-life: wellness retreat. */
    WEEKEND_RETREAT,
    /** Mid-life: dinner with old friends. */
    REUNION_DINNER,
    /** Senior: day with grandchildren. */
    GRANDKIDS_DAY,
    /** Senior: community club meetup. */
    COMMUNITY_CLUB,
    /** Senior: memoir / life writing. */
    MEMOIR_WRITING,
    /** Mind & body: library reading. */
    LIBRARY_VISIT,
    /** Mind & body: meditation / calm. */
    MEDITATION,
    /** High culture: museum visit. */
    VISIT_MUSEUM,
    /** Mind & body: annual health checkup. */
    ANNUAL_CHECKUP,
    /** Luxury: day on the yacht (requires owning a boat). */
    YACHT_DAY,
    /** Luxury: private jet trip (requires owning a private jet). */
    PRIVATE_JET_TRIP,
    /** Child (3-11): play with toys. */
    PLAY_WITH_TOYS,
    /** Child (3-8): throw a tantrum. */
    THROW_TANTRUM,
    /** Child/teen (6-15): ask parents for pocket money. */
    ASK_ALLOWANCE,
    /** Child/teen (6-15): youth hobby (art, music, sports). */
    CHILDHOOD_HOBBY,
    /** Teen (12-17): sneak out after hours. */
    SNEAK_OUT,
    /** Teen (12-17): mall hangout with friends. */
    MALL_HANGOUT,
    /** Child/teen (10-17): gaming marathon. */
    GAMING_MARATHON
}

sealed class LeisureResult {
    data class Success(
        val character: Character,
        val activity: LeisureActivity,
        val flavor: String? = null
    ) : LeisureResult()
    data object InsufficientFunds : LeisureResult()
    data object Ineligible : LeisureResult()
}

/**
 * Cash-only leisure / childhood activities — happiness/health/smarts tradeoffs, no gambling.
 * Costs are Kenya-baseline, scaled via [EconomyScaler].
 */
@Singleton
class LeisureEngine @Inject constructor(
    private val bookCatalog: com.maisha.game.data.content.BookCatalog,
    private val quoteCatalog: com.maisha.game.data.content.QuoteCatalog,
    private val artCatalog: com.maisha.game.data.content.ArtCatalog
) {
    constructor() : this(
        com.maisha.game.data.content.FallbackBookCatalog,
        com.maisha.game.data.content.FallbackQuoteCatalog,
        com.maisha.game.data.content.FallbackArtCatalog
    )
    constructor(
        bookCatalog: com.maisha.game.data.content.BookCatalog,
        quoteCatalog: com.maisha.game.data.content.QuoteCatalog
    ) : this(
        bookCatalog,
        quoteCatalog,
        com.maisha.game.data.content.FallbackArtCatalog
    )

    fun cost(activity: LeisureActivity, countryCode: String): Int =
        EconomyScaler.scaleAmount(baseCostKenya(activity), countryCode)

    fun isEligible(character: Character, activity: LeisureActivity): Boolean {
        if (!character.alive ||
            character.criminalRecord.currentlyIncarcerated ||
            character.criminalRecord.awaitingTrial
        ) {
            return false
        }
        val age = character.age
        return when (activity) {
            LeisureActivity.STORYTIME -> age in STORYTIME_MIN_AGE..STORYTIME_MAX_AGE
            LeisureActivity.PARK_CAREGIVER -> age in PARK_MIN_AGE..TODDLER_MAX_AGE
            LeisureActivity.NAP_ROUTINE -> age in NAP_MIN_AGE..NAP_MAX_AGE
            LeisureActivity.PLAYDATE -> age in PLAYDATE_MIN_AGE..TODDLER_MAX_AGE
            LeisureActivity.PLAYGROUND -> age in PLAYGROUND_MIN_AGE..PLAYGROUND_MAX_AGE
            LeisureActivity.STUDY_BUDDY -> age in STUDY_MIN_AGE..STUDY_MAX_AGE
            LeisureActivity.CHORES -> age in CHORES_MIN_AGE..CHORES_MAX_AGE
            LeisureActivity.NIGHT_OUT,
            LeisureActivity.NATURE_DAY,
            LeisureActivity.CITY_SHOW,
            LeisureActivity.SPA_DAY -> age >= ADULT_LEISURE_MIN_AGE
            LeisureActivity.WEEKEND_RETREAT,
            LeisureActivity.REUNION_DINNER -> age in MID_LIFE_LEISURE_MIN_AGE..MID_LIFE_LEISURE_MAX_AGE
            LeisureActivity.GRANDKIDS_DAY,
            LeisureActivity.COMMUNITY_CLUB,
            LeisureActivity.MEMOIR_WRITING -> age >= SENIOR_LEISURE_MIN_AGE
            LeisureActivity.LIBRARY_VISIT -> age >= MIND_BODY_MIN_AGE
            LeisureActivity.MEDITATION -> age >= MIND_BODY_MIN_AGE
            LeisureActivity.ANNUAL_CHECKUP -> age >= CHECKUP_MIN_AGE
            LeisureActivity.VISIT_MUSEUM -> age >= ADULT_LEISURE_MIN_AGE
            LeisureActivity.YACHT_DAY -> age >= ADULT_LEISURE_MIN_AGE &&
                character.assets.any { it.type == com.maisha.game.data.model.AssetType.BOAT }
            LeisureActivity.PRIVATE_JET_TRIP -> age >= ADULT_LEISURE_MIN_AGE &&
                character.assets.any { it.type == com.maisha.game.data.model.AssetType.PRIVATE_JET }
            LeisureActivity.PLAY_WITH_TOYS -> age in 3..11
            LeisureActivity.THROW_TANTRUM -> age in 3..8
            LeisureActivity.ASK_ALLOWANCE -> age in 6..15
            LeisureActivity.CHILDHOOD_HOBBY -> age in 6..15
            LeisureActivity.SNEAK_OUT -> age in 12..17
            LeisureActivity.MALL_HANGOUT -> age in 12..17
            LeisureActivity.GAMING_MARATHON -> age in 10..17
        }
    }

    fun activitiesFor(character: Character): List<LeisureActivity> =
        LeisureActivity.entries.filter { isEligible(character, it) }

    fun perform(character: Character, activity: LeisureActivity): LeisureResult {
        if (!isEligible(character, activity)) return LeisureResult.Ineligible
        val fee = cost(activity, character.countryCode)
        if (activity != LeisureActivity.CHORES && fee > 0 && character.stats.money < fee) {
            return LeisureResult.InsufficientFunds
        }

        val (happinessDelta, healthDelta, smartsDelta, moneyDelta) =
            deltas(activity, fee, character.countryCode)
        val flavor = when (activity) {
            LeisureActivity.LIBRARY_VISIT -> bookCatalog.nextBookTitle()
            LeisureActivity.MEDITATION -> quoteCatalog.nextQuote()
            LeisureActivity.VISIT_MUSEUM -> artCatalog.nextArtwork()
            else -> null
        }
        var updated = character.copy(
            stats = character.stats.copy(
                money = (character.stats.money + moneyDelta).coerceAtLeast(0),
                happiness = (character.stats.happiness + happinessDelta).coerceIn(0, 100),
                health = (character.stats.health + healthDelta).coerceIn(0, 100),
                smarts = (character.stats.smarts + smartsDelta).coerceIn(0, 100)
            ),
            eventLog = EventLogCap.prependToCurrentYear(
                character.eventLog,
                logLine(activity, fee, character.countryCode, flavor),
                character.age
            )
        )
        if (activity == LeisureActivity.VISIT_MUSEUM) {
            updated = maybeCollectPrint(updated, flavor)
        }
        return LeisureResult.Success(updated, activity, flavor)
    }

    /** @deprecated Prefer [activitiesFor]. */
    fun allActivities(): List<LeisureActivity> = LeisureActivity.entries.toList()

    private fun baseCostKenya(activity: LeisureActivity): Int = when (activity) {
        LeisureActivity.STORYTIME -> STORYTIME_COST_KENYA
        LeisureActivity.PARK_CAREGIVER -> PARK_COST_KENYA
        LeisureActivity.NAP_ROUTINE -> 0
        LeisureActivity.PLAYDATE -> PLAYDATE_COST_KENYA
        LeisureActivity.PLAYGROUND -> PLAYGROUND_COST_KENYA
        LeisureActivity.STUDY_BUDDY -> STUDY_COST_KENYA
        LeisureActivity.CHORES -> 0
        LeisureActivity.NIGHT_OUT -> NIGHT_OUT_COST_KENYA
        LeisureActivity.NATURE_DAY -> NATURE_DAY_COST_KENYA
        LeisureActivity.CITY_SHOW -> CITY_SHOW_COST_KENYA
        LeisureActivity.SPA_DAY -> SPA_DAY_COST_KENYA
        LeisureActivity.WEEKEND_RETREAT -> WEEKEND_RETREAT_COST_KENYA
        LeisureActivity.REUNION_DINNER -> REUNION_DINNER_COST_KENYA
        LeisureActivity.GRANDKIDS_DAY -> GRANDKIDS_COST_KENYA
        LeisureActivity.COMMUNITY_CLUB -> COMMUNITY_CLUB_COST_KENYA
        LeisureActivity.MEMOIR_WRITING -> MEMOIR_COST_KENYA
        LeisureActivity.LIBRARY_VISIT -> LIBRARY_COST_KENYA
        LeisureActivity.VISIT_MUSEUM -> VISIT_MUSEUM_COST_KENYA
        LeisureActivity.MEDITATION -> 0
        LeisureActivity.ANNUAL_CHECKUP -> CHECKUP_COST_KENYA
        LeisureActivity.YACHT_DAY -> YACHT_DAY_COST_KENYA
        LeisureActivity.PRIVATE_JET_TRIP -> PRIVATE_JET_TRIP_COST_KENYA
        LeisureActivity.PLAY_WITH_TOYS -> 0
        LeisureActivity.THROW_TANTRUM -> 0
        LeisureActivity.ASK_ALLOWANCE -> 0
        LeisureActivity.CHILDHOOD_HOBBY -> CHILDHOOD_HOBBY_COST_KENYA
        LeisureActivity.SNEAK_OUT -> 0
        LeisureActivity.MALL_HANGOUT -> MALL_HANGOUT_COST_KENYA
        LeisureActivity.GAMING_MARATHON -> 0
    }

    private fun deltas(
        activity: LeisureActivity,
        fee: Int,
        countryCode: String
    ): Quad = when (activity) {
        LeisureActivity.STORYTIME -> Quad(6, 1, 2, -fee)
        LeisureActivity.PARK_CAREGIVER -> Quad(7, 3, 0, -fee)
        LeisureActivity.NAP_ROUTINE -> Quad(2, 4, 0, 0)
        LeisureActivity.PLAYDATE -> Quad(8, 2, 1, -fee)
        LeisureActivity.PLAYGROUND -> Quad(8, 3, 0, -fee)
        LeisureActivity.STUDY_BUDDY -> Quad(2, 0, 5, -fee)
        LeisureActivity.CHORES -> Quad(
            -2,
            0,
            0,
            EconomyScaler.scaleAmount(CHORES_PAY_KENYA, countryCode)
        )
        LeisureActivity.NIGHT_OUT -> Quad(10, -3, 0, -fee)
        LeisureActivity.NATURE_DAY -> Quad(8, 4, 0, -fee)
        LeisureActivity.CITY_SHOW -> Quad(9, 0, 0, -fee)
        LeisureActivity.SPA_DAY -> Quad(7, 6, 0, -fee)
        LeisureActivity.WEEKEND_RETREAT -> Quad(12, 5, 0, -fee)
        LeisureActivity.REUNION_DINNER -> Quad(10, 0, 1, -fee)
        LeisureActivity.GRANDKIDS_DAY -> Quad(11, 2, 0, -fee)
        LeisureActivity.COMMUNITY_CLUB -> Quad(6, 3, 1, -fee)
        LeisureActivity.MEMOIR_WRITING -> Quad(5, 0, 4, -fee)
        LeisureActivity.LIBRARY_VISIT -> Quad(3, 0, 4, -fee)
        LeisureActivity.VISIT_MUSEUM -> Quad(6, 0, 3, -fee)
        LeisureActivity.MEDITATION -> Quad(4, 2, 0, 0)
        LeisureActivity.ANNUAL_CHECKUP -> Quad(0, 6, 0, -fee)
        LeisureActivity.YACHT_DAY -> Quad(15, 3, 0, -fee)
        LeisureActivity.PRIVATE_JET_TRIP -> Quad(18, 2, 0, -fee)
        LeisureActivity.PLAY_WITH_TOYS -> Quad(6, 1, 2, 0)
        LeisureActivity.THROW_TANTRUM -> Quad(3, -1, -2, 0)
        LeisureActivity.ASK_ALLOWANCE -> Quad(
            4,
            0,
            0,
            EconomyScaler.scaleAmount(ALLOWANCE_PAY_KENYA, countryCode)
        )
        LeisureActivity.CHILDHOOD_HOBBY -> Quad(7, 2, 3, -fee)
        LeisureActivity.SNEAK_OUT -> Quad(8, -1, 0, 0)
        LeisureActivity.MALL_HANGOUT -> Quad(7, 0, 1, -fee)
        LeisureActivity.GAMING_MARATHON -> Quad(8, -2, 1, 0)
    }

    private fun logLine(
        activity: LeisureActivity,
        fee: Int,
        countryCode: String,
        flavor: String?
    ): String {
        val money = formatMoney(fee, countryCode)
        val chorePay = formatMoney(
            EconomyScaler.scaleAmount(CHORES_PAY_KENYA, countryCode),
            countryCode
        )
        return when (activity) {
            LeisureActivity.STORYTIME -> "You listened to a bedtime story."
            LeisureActivity.PARK_CAREGIVER -> "You went to the park with your caregiver."
            LeisureActivity.NAP_ROUTINE -> "You rested and recharged with a nap."
            LeisureActivity.PLAYDATE -> "You had a playdate with a friend."
            LeisureActivity.PLAYGROUND -> "You played outside with friends."
            LeisureActivity.STUDY_BUDDY -> "You spent $money on a study session."
            LeisureActivity.CHORES -> "You did chores and earned $chorePay."
            LeisureActivity.NIGHT_OUT -> "You spent $money on a night out."
            LeisureActivity.NATURE_DAY -> "You spent $money on a day in nature."
            LeisureActivity.CITY_SHOW -> "You spent $money on tickets to a city show."
            LeisureActivity.SPA_DAY -> "You spent $money on a spa day."
            LeisureActivity.WEEKEND_RETREAT -> "You spent $money on a weekend retreat."
            LeisureActivity.REUNION_DINNER -> "You spent $money on a reunion dinner."
            LeisureActivity.GRANDKIDS_DAY -> "You spent the day with grandchildren."
            LeisureActivity.COMMUNITY_CLUB -> "You joined a community club meetup ($money)."
            LeisureActivity.MEMOIR_WRITING -> "You worked on your life story ($money)."
            LeisureActivity.LIBRARY_VISIT -> {
                val book = flavor?.takeIf { it.isNotBlank() } ?: "a book"
                "You read $book at the library ($money)."
            }
            LeisureActivity.VISIT_MUSEUM -> {
                val art = flavor?.takeIf { it.isNotBlank() } ?: "some art"
                "You visited a museum and saw $art ($money)."
            }
            LeisureActivity.MEDITATION -> {
                val quote = flavor?.takeIf { it.isNotBlank() }
                if (quote == null) "You meditated and cleared your mind."
                else "You meditated on \"$quote\""
            }
            LeisureActivity.ANNUAL_CHECKUP -> "You got an annual checkup ($money)."
            LeisureActivity.YACHT_DAY -> "You spent the day on your yacht ($money). Absolute luxury."
            LeisureActivity.PRIVATE_JET_TRIP -> "You took a trip on your private jet ($money). The world is yours."
            LeisureActivity.PLAY_WITH_TOYS -> "You spent hours building worlds and playing with your favorite toys."
            LeisureActivity.THROW_TANTRUM -> "You threw a fierce tantrum on the living room floor."
            LeisureActivity.ASK_ALLOWANCE -> {
                val allowance = formatMoney(
                    EconomyScaler.scaleAmount(ALLOWANCE_PAY_KENYA, countryCode),
                    countryCode
                )
                "You asked your parents for pocket money and received $allowance."
            }
            LeisureActivity.CHILDHOOD_HOBBY -> "You practiced your childhood hobby with passion ($money)."
            LeisureActivity.SNEAK_OUT -> "You snuck out of the house late at night for an exhilarating adventure."
            LeisureActivity.MALL_HANGOUT -> "You met up with friends to walk around the mall ($money)."
            LeisureActivity.GAMING_MARATHON -> "You stayed glued to the screen for an epic gaming session."
        }
    }

    private fun maybeCollectPrint(character: Character, artwork: String?): Character {
        val title = artwork?.takeIf { it.isNotBlank() } ?: return character
        if (Random.nextFloat() > PRINT_COLLECT_CHANCE) return character
        val owned = character.assets.count { it.catalogId == ART_PRINT_CATALOG_ID }
        if (owned >= MAX_ART_PRINTS) return character
        val printCost = EconomyScaler.scaleAmount(ART_PRINT_COST_KENYA, character.countryCode)
        if (character.stats.money < printCost) return character
        val print = Asset(
            id = UUID.randomUUID().toString(),
            catalogId = ART_PRINT_CATALOG_ID,
            type = AssetType.HEIRLOOM,
            name = "Print: $title",
            purchasePrice = printCost,
            currentValue = printCost,
            monthlyUpkeep = 0,
            isHeirloom = true
        )
        return character.copy(
            stats = character.stats.copy(money = character.stats.money - printCost),
            assets = character.assets + print,
            eventLog = EventLogCap.prepend(
                character.eventLog,
                "You added a print of $title to your collection (${formatMoney(printCost, character.countryCode)})."
            )
        )
    }

    private data class Quad(
        val happiness: Int,
        val health: Int,
        val smarts: Int,
        val money: Int
    )

    companion object {
        const val STORYTIME_MIN_AGE = 0
        const val STORYTIME_MAX_AGE = 4
        const val PARK_MIN_AGE = 1
        const val TODDLER_MAX_AGE = 4
        const val NAP_MIN_AGE = 0
        const val NAP_MAX_AGE = 3
        const val PLAYDATE_MIN_AGE = 2
        const val PLAYGROUND_MIN_AGE = 5
        const val PLAYGROUND_MAX_AGE = 13
        const val STUDY_MIN_AGE = 6
        const val STUDY_MAX_AGE = 17
        const val CHORES_MIN_AGE = 8
        const val CHORES_MAX_AGE = 16
        const val ADULT_LEISURE_MIN_AGE = 14
        const val MID_LIFE_LEISURE_MIN_AGE = 26
        const val MID_LIFE_LEISURE_MAX_AGE = 59
        const val SENIOR_LEISURE_MIN_AGE = 60
        /** @deprecated Use age-gated [activitiesFor]. */
        const val MIN_LEISURE_AGE = 0

        const val STORYTIME_COST_KENYA = 0
        const val PARK_COST_KENYA = 300
        const val PLAYDATE_COST_KENYA = 500
        const val PLAYGROUND_COST_KENYA = 500
        const val STUDY_COST_KENYA = 1_500
        const val CHORES_PAY_KENYA = 2_000
        const val NIGHT_OUT_COST_KENYA = 8_000
        const val NATURE_DAY_COST_KENYA = 3_500
        const val CITY_SHOW_COST_KENYA = 6_000
        const val SPA_DAY_COST_KENYA = 15_000
        const val WEEKEND_RETREAT_COST_KENYA = 25_000
        const val REUNION_DINNER_COST_KENYA = 12_000
        const val GRANDKIDS_COST_KENYA = 2_000
        const val COMMUNITY_CLUB_COST_KENYA = 1_500
        const val MEMOIR_COST_KENYA = 3_000
        const val LIBRARY_COST_KENYA = 500
        const val VISIT_MUSEUM_COST_KENYA = 2_500
        const val ART_PRINT_COST_KENYA = 15_000
        const val ART_PRINT_CATALOG_ID = "art_print"
        const val MAX_ART_PRINTS = 12
        const val PRINT_COLLECT_CHANCE = 0.35f
        const val CHECKUP_COST_KENYA = 4_000
        const val MIND_BODY_MIN_AGE = 8
        const val CHECKUP_MIN_AGE = 14
        /** Fuel, crew, and dockage for a full day trip. */
        const val YACHT_DAY_COST_KENYA = 120_000
        /** Fuel, handling fees, and incidentals for one jet trip. */
        const val PRIVATE_JET_TRIP_COST_KENYA = 500_000
        const val ALLOWANCE_PAY_KENYA = 800
        const val CHILDHOOD_HOBBY_COST_KENYA = 400
        const val MALL_HANGOUT_COST_KENYA = 600
    }
}
