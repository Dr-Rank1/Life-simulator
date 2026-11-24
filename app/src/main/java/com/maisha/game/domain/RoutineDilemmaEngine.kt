package com.maisha.game.domain

import com.maisha.game.data.CountryCatalog
import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.FlavorInterpolator
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.DilemmaChoice
import com.maisha.game.data.model.DilemmaResolution
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.RoutineCategory
import com.maisha.game.data.model.RoutineDilemma
import com.maisha.game.data.model.SchoolStage
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.random.Random

/**
 * Pure domain engine that resolves fast, bite-sized micro-dilemmas and snap decisions
 * during everyday routines (transit, work/study, weekend outings, family dinners).
 */
@Singleton
class RoutineDilemmaEngine @Inject constructor() {

    private val catalog: List<RoutineDilemma> = listOf(
        // Commute 1: Crowded Transit Seat
        RoutineDilemma(
            id = "commute_rush_hour_seat",
            category = RoutineCategory.COMMUTE,
            titleResKey = "dilemma_commute_seat_title",
            promptTemplate = "An elderly passenger carrying heavy groceries steps aboard your packed {transportMode}. Every seat is occupied.",
            minAge = 6,
            choices = listOf(
                DilemmaChoice(
                    id = "seat_offer",
                    titleResKey = "dilemma_commute_seat_offer",
                    descResKey = "dilemma_commute_seat_offer_desc",
                    tagResKey = "dilemma_commute_seat_offer_tag",
                    healthDelta = -4,
                    happinessDelta = 3,
                    karmaDelta = 5,
                    outcomeSuccessResKey = "dilemma_commute_seat_offer_outcome"
                ),
                DilemmaChoice(
                    id = "seat_ignore",
                    titleResKey = "dilemma_commute_seat_ignore",
                    descResKey = "dilemma_commute_seat_ignore_desc",
                    tagResKey = "dilemma_commute_seat_ignore_tag",
                    healthDelta = 2,
                    karmaDelta = -4,
                    outcomeSuccessResKey = "dilemma_commute_seat_ignore_outcome"
                )
            )
        ),
        // Commute 2: Gridlock Shortcut
        RoutineDilemma(
            id = "commute_traffic_shortcut",
            category = RoutineCategory.COMMUTE,
            titleResKey = "dilemma_commute_shortcut_title",
            promptTemplate = "Traffic is deadlocked on your commute. The driver proposes weaving through narrow, unpaved alleyways to bypass the jam.",
            minAge = 12,
            choices = listOf(
                DilemmaChoice(
                    id = "shortcut_take",
                    titleResKey = "dilemma_commute_shortcut_take",
                    descResKey = "dilemma_commute_shortcut_take_desc",
                    tagResKey = "dilemma_commute_shortcut_take_tag",
                    successProbability = 0.65f,
                    smartsDelta = 5,
                    happinessDelta = 4,
                    failureHealthDelta = -6,
                    failureCashDelta = -50,
                    failureHappinessDelta = -3,
                    outcomeSuccessResKey = "dilemma_commute_shortcut_take_success",
                    outcomeFailureResKey = "dilemma_commute_shortcut_take_fail"
                ),
                DilemmaChoice(
                    id = "shortcut_stay",
                    titleResKey = "dilemma_commute_shortcut_stay",
                    descResKey = "dilemma_commute_shortcut_stay_desc",
                    tagResKey = "dilemma_commute_shortcut_stay_tag",
                    happinessDelta = -3,
                    smartsDelta = 2,
                    outcomeSuccessResKey = "dilemma_commute_shortcut_stay_outcome"
                )
            )
        ),
        // Commute 3: Conductor Fare Dispute
        RoutineDilemma(
            id = "commute_fare_dispute",
            category = RoutineCategory.COMMUTE,
            titleResKey = "dilemma_commute_fare_title",
            promptTemplate = "The {transportMode} conductor demands an extra fare charge, claiming peak-hour surge pricing.",
            minAge = 14,
            choices = listOf(
                DilemmaChoice(
                    id = "fare_dispute",
                    titleResKey = "dilemma_commute_fare_dispute",
                    descResKey = "dilemma_commute_fare_dispute_desc",
                    tagResKey = "dilemma_commute_fare_dispute_tag",
                    successProbability = 0.65f,
                    smartsDelta = 3,
                    cashDelta = 20,
                    failureHappinessDelta = -5,
                    failureHeatDelta = 5,
                    outcomeSuccessResKey = "dilemma_commute_fare_dispute_success",
                    outcomeFailureResKey = "dilemma_commute_fare_dispute_fail"
                ),
                DilemmaChoice(
                    id = "fare_pay",
                    titleResKey = "dilemma_commute_fare_pay",
                    descResKey = "dilemma_commute_fare_pay_desc",
                    tagResKey = "dilemma_commute_fare_pay_tag",
                    cashDelta = -20,
                    karmaDelta = 2,
                    outcomeSuccessResKey = "dilemma_commute_fare_pay_outcome"
                )
            )
        ),
        // Commute 4: Roadside Breakdown
        RoutineDilemma(
            id = "commute_breakdown_push",
            category = RoutineCategory.COMMUTE,
            titleResKey = "dilemma_commute_push_title",
            promptTemplate = "Your {transportMode} sputters and stalls in traffic. The driver asks passengers to help push it to start.",
            minAge = 12,
            choices = listOf(
                DilemmaChoice(
                    id = "push_help",
                    titleResKey = "dilemma_commute_push_help",
                    descResKey = "dilemma_commute_push_help_desc",
                    tagResKey = "dilemma_commute_push_help_tag",
                    healthDelta = -5,
                    karmaDelta = 5,
                    happinessDelta = 5,
                    outcomeSuccessResKey = "dilemma_commute_push_help_outcome"
                ),
                DilemmaChoice(
                    id = "push_watch",
                    titleResKey = "dilemma_commute_push_watch",
                    descResKey = "dilemma_commute_push_watch_desc",
                    tagResKey = "dilemma_commute_push_watch_tag",
                    karmaDelta = -2,
                    outcomeSuccessResKey = "dilemma_commute_push_watch_outcome"
                )
            )
        ),

        // Work/Study 5: Covering a Coworker
        RoutineDilemma(
            id = "work_cover_coworker",
            category = RoutineCategory.WORK_STUDY,
            titleResKey = "dilemma_work_cover_title",
            promptTemplate = "A visibly anxious coworker asks if you can finish their portion of tomorrow's major deliverable.",
            minAge = 16,
            requiresEmployment = true,
            choices = listOf(
                DilemmaChoice(
                    id = "cover_accept",
                    titleResKey = "dilemma_work_cover_accept",
                    descResKey = "dilemma_work_cover_accept_desc",
                    tagResKey = "dilemma_work_cover_accept_tag",
                    healthDelta = -6,
                    karmaDelta = 5,
                    relationshipDelta = 10,
                    outcomeSuccessResKey = "dilemma_work_cover_accept_outcome"
                ),
                DilemmaChoice(
                    id = "cover_decline",
                    titleResKey = "dilemma_work_cover_decline",
                    descResKey = "dilemma_work_cover_decline_desc",
                    tagResKey = "dilemma_work_cover_decline_tag",
                    healthDelta = 3,
                    relationshipDelta = -5,
                    outcomeSuccessResKey = "dilemma_work_cover_decline_outcome"
                )
            )
        ),
        // Work/Study 6: Midnight Oil
        RoutineDilemma(
            id = "work_midnight_oil",
            category = RoutineCategory.WORK_STUDY,
            titleResKey = "dilemma_work_midnight_title",
            promptTemplate = "It is 1:00 AM before a crucial presentation. Your eyelids are heavy, but several slides still look mediocre.",
            minAge = 14,
            choices = listOf(
                DilemmaChoice(
                    id = "midnight_push",
                    titleResKey = "dilemma_work_midnight_push",
                    descResKey = "dilemma_work_midnight_push_desc",
                    tagResKey = "dilemma_work_midnight_push_tag",
                    smartsDelta = 6,
                    healthDelta = -8,
                    happinessDelta = 4,
                    outcomeSuccessResKey = "dilemma_work_midnight_push_outcome"
                ),
                DilemmaChoice(
                    id = "midnight_sleep",
                    titleResKey = "dilemma_work_midnight_sleep",
                    descResKey = "dilemma_work_midnight_sleep_desc",
                    tagResKey = "dilemma_work_midnight_sleep_tag",
                    healthDelta = 6,
                    smartsDelta = -2,
                    outcomeSuccessResKey = "dilemma_work_midnight_sleep_outcome"
                )
            )
        ),
        // Work/Study 7: Watercooler Secret
        RoutineDilemma(
            id = "work_leak_memo",
            category = RoutineCategory.WORK_STUDY,
            titleResKey = "dilemma_work_leak_title",
            promptTemplate = "You inadvertently see an unredacted memo about upcoming restructuring plans left on a communal printer.",
            minAge = 18,
            requiresEmployment = true,
            choices = listOf(
                DilemmaChoice(
                    id = "leak_share",
                    titleResKey = "dilemma_work_leak_share",
                    descResKey = "dilemma_work_leak_share_desc",
                    tagResKey = "dilemma_work_leak_share_tag",
                    relationshipDelta = 10,
                    heatDelta = 8,
                    karmaDelta = -3,
                    outcomeSuccessResKey = "dilemma_work_leak_share_outcome"
                ),
                DilemmaChoice(
                    id = "leak_ignore",
                    titleResKey = "dilemma_work_leak_ignore",
                    descResKey = "dilemma_work_leak_ignore_desc",
                    tagResKey = "dilemma_work_leak_ignore_tag",
                    smartsDelta = 3,
                    karmaDelta = 2,
                    outcomeSuccessResKey = "dilemma_work_leak_ignore_outcome"
                )
            )
        ),
        // Work/Study 8: Report Shortcut
        RoutineDilemma(
            id = "work_report_shortcut",
            category = RoutineCategory.WORK_STUDY,
            titleResKey = "dilemma_work_shortcut_title",
            promptTemplate = "A repetitive audit report will take hours to verify manually. You could copy last term's baseline data.",
            minAge = 16,
            choices = listOf(
                DilemmaChoice(
                    id = "shortcut_gamble",
                    titleResKey = "dilemma_work_shortcut_gamble",
                    descResKey = "dilemma_work_shortcut_gamble_desc",
                    tagResKey = "dilemma_work_shortcut_gamble_tag",
                    successProbability = 0.70f,
                    happinessDelta = 5,
                    smartsDelta = 4,
                    failureHappinessDelta = -10,
                    failureHeatDelta = 10,
                    outcomeSuccessResKey = "dilemma_work_shortcut_gamble_success",
                    outcomeFailureResKey = "dilemma_work_shortcut_gamble_fail"
                ),
                DilemmaChoice(
                    id = "shortcut_manual",
                    titleResKey = "dilemma_work_shortcut_manual",
                    descResKey = "dilemma_work_shortcut_manual_desc",
                    tagResKey = "dilemma_work_shortcut_manual_tag",
                    healthDelta = -5,
                    smartsDelta = 6,
                    karmaDelta = 4,
                    outcomeSuccessResKey = "dilemma_work_shortcut_manual_outcome"
                )
            )
        ),

        // Weekend 9: Marketplace Bargain
        RoutineDilemma(
            id = "splurge_market_haggle",
            category = RoutineCategory.WEEKEND_SPLURGE,
            titleResKey = "dilemma_splurge_haggle_title",
            promptTemplate = "An open-air market vendor quotes a steep tourist price for fresh goods you want to purchase.",
            minAge = 12,
            choices = listOf(
                DilemmaChoice(
                    id = "haggle_push",
                    titleResKey = "dilemma_splurge_haggle_push",
                    descResKey = "dilemma_splurge_haggle_push_desc",
                    tagResKey = "dilemma_splurge_haggle_push_tag",
                    successProbability = 0.70f,
                    cashDelta = 40,
                    smartsDelta = 4,
                    happinessDelta = 4,
                    failureHappinessDelta = -5,
                    outcomeSuccessResKey = "dilemma_splurge_haggle_push_success",
                    outcomeFailureResKey = "dilemma_splurge_haggle_push_fail"
                ),
                DilemmaChoice(
                    id = "haggle_pay",
                    titleResKey = "dilemma_splurge_haggle_pay",
                    descResKey = "dilemma_splurge_haggle_pay_desc",
                    tagResKey = "dilemma_splurge_haggle_pay_tag",
                    cashDelta = -40,
                    karmaDelta = 4,
                    outcomeSuccessResKey = "dilemma_splurge_haggle_pay_outcome"
                )
            )
        ),
        // Weekend 10: Unequal Dinner Bill
        RoutineDilemma(
            id = "splurge_unequal_bill",
            category = RoutineCategory.WEEKEND_SPLURGE,
            titleResKey = "dilemma_splurge_bill_title",
            promptTemplate = "After dining with acquaintances, someone suggests an equal split, ignoring that two people ordered pricey cocktails.",
            minAge = 16,
            choices = listOf(
                DilemmaChoice(
                    id = "bill_item",
                    titleResKey = "dilemma_splurge_bill_item",
                    descResKey = "dilemma_splurge_bill_item_desc",
                    tagResKey = "dilemma_splurge_bill_item_tag",
                    cashDelta = 50,
                    relationshipDelta = -4,
                    outcomeSuccessResKey = "dilemma_splurge_bill_item_outcome"
                ),
                DilemmaChoice(
                    id = "bill_even",
                    titleResKey = "dilemma_splurge_bill_even",
                    descResKey = "dilemma_splurge_bill_even_desc",
                    tagResKey = "dilemma_splurge_bill_even_tag",
                    cashDelta = -50,
                    relationshipDelta = 6,
                    karmaDelta = 4,
                    outcomeSuccessResKey = "dilemma_splurge_bill_even_outcome"
                )
            )
        ),
        // Weekend 11: Street Musician
        RoutineDilemma(
            id = "splurge_street_musician",
            category = RoutineCategory.WEEKEND_SPLURGE,
            titleResKey = "dilemma_splurge_busker_title",
            promptTemplate = "A street musician plays a stirring acoustic melody near the walkway, drawing a small, appreciative crowd.",
            minAge = 8,
            choices = listOf(
                DilemmaChoice(
                    id = "busker_tip",
                    titleResKey = "dilemma_splurge_busker_tip",
                    descResKey = "dilemma_splurge_busker_tip_desc",
                    tagResKey = "dilemma_splurge_busker_tip_tag",
                    cashDelta = -15,
                    happinessDelta = 6,
                    karmaDelta = 4,
                    outcomeSuccessResKey = "dilemma_splurge_busker_tip_outcome"
                ),
                DilemmaChoice(
                    id = "busker_pass",
                    titleResKey = "dilemma_splurge_busker_pass",
                    descResKey = "dilemma_splurge_busker_pass_desc",
                    tagResKey = "dilemma_splurge_busker_pass_tag",
                    karmaDelta = -1,
                    outcomeSuccessResKey = "dilemma_splurge_busker_pass_outcome"
                )
            )
        ),
        // Weekend 12: Alley Shell Game
        RoutineDilemma(
            id = "splurge_alley_game",
            category = RoutineCategory.WEEKEND_SPLURGE,
            titleResKey = "dilemma_splurge_street_game_title",
            promptTemplate = "A quick-handed street operator on a wooden crate calls out to passersby to bet on a shell game.",
            minAge = 14,
            choices = listOf(
                DilemmaChoice(
                    id = "game_bet",
                    titleResKey = "dilemma_splurge_street_game_bet",
                    descResKey = "dilemma_splurge_street_game_bet_desc",
                    tagResKey = "dilemma_splurge_street_game_bet_tag",
                    successProbability = 0.40f,
                    cashDelta = 80,
                    happinessDelta = 6,
                    failureCashDelta = -40,
                    failureHappinessDelta = -5,
                    outcomeSuccessResKey = "dilemma_splurge_street_game_bet_success",
                    outcomeFailureResKey = "dilemma_splurge_street_game_bet_fail"
                ),
                DilemmaChoice(
                    id = "game_walk",
                    titleResKey = "dilemma_splurge_street_game_walk",
                    descResKey = "dilemma_splurge_street_game_walk_desc",
                    tagResKey = "dilemma_splurge_street_game_walk_tag",
                    smartsDelta = 3,
                    karmaDelta = 2,
                    outcomeSuccessResKey = "dilemma_splurge_street_game_walk_outcome"
                )
            )
        ),

        // Family 13: Unsolicited Advice
        RoutineDilemma(
            id = "family_unsolicited_advice",
            category = RoutineCategory.FAMILY_DINNER,
            titleResKey = "dilemma_family_advice_title",
            promptTemplate = "During dinner, a relative questions your recent financial and relationship decisions in front of everyone.",
            minAge = 12,
            requiresFamily = true,
            choices = listOf(
                DilemmaChoice(
                    id = "advice_nod",
                    titleResKey = "dilemma_family_advice_nod",
                    descResKey = "dilemma_family_advice_nod_desc",
                    tagResKey = "dilemma_family_advice_nod_tag",
                    relationshipDelta = 6,
                    happinessDelta = -3,
                    outcomeSuccessResKey = "dilemma_family_advice_nod_outcome"
                ),
                DilemmaChoice(
                    id = "advice_stand",
                    titleResKey = "dilemma_family_advice_stand",
                    descResKey = "dilemma_family_advice_stand_desc",
                    tagResKey = "dilemma_family_advice_stand_tag",
                    smartsDelta = 5,
                    relationshipDelta = -4,
                    outcomeSuccessResKey = "dilemma_family_advice_stand_outcome"
                )
            )
        ),
        // Family 14: The Last Savory Portion
        RoutineDilemma(
            id = "family_last_portion",
            category = RoutineCategory.FAMILY_DINNER,
            titleResKey = "dilemma_family_portion_title",
            promptTemplate = "One single delicious portion remains on the dinner platter, and your sibling is eyeing it hungrily.",
            minAge = 4,
            requiresFamily = true,
            choices = listOf(
                DilemmaChoice(
                    id = "portion_yield",
                    titleResKey = "dilemma_family_portion_yield",
                    descResKey = "dilemma_family_portion_yield_desc",
                    tagResKey = "dilemma_family_portion_yield_tag",
                    relationshipDelta = 8,
                    karmaDelta = 4,
                    outcomeSuccessResKey = "dilemma_family_portion_yield_outcome"
                ),
                DilemmaChoice(
                    id = "portion_take",
                    titleResKey = "dilemma_family_portion_take",
                    descResKey = "dilemma_family_portion_take_desc",
                    tagResKey = "dilemma_family_portion_take_tag",
                    healthDelta = 3,
                    relationshipDelta = -2,
                    outcomeSuccessResKey = "dilemma_family_portion_take_outcome"
                )
            )
        ),
        // Family 15: Relative Loan Request
        RoutineDilemma(
            id = "family_kin_loan",
            category = RoutineCategory.FAMILY_DINNER,
            titleResKey = "dilemma_family_loan_title",
            promptTemplate = "A family member sends a private message asking for an urgent {moneyApp} transfer to cover a surprise bill.",
            minAge = 16,
            requiresFamily = true,
            choices = listOf(
                DilemmaChoice(
                    id = "loan_send",
                    titleResKey = "dilemma_family_loan_send",
                    descResKey = "dilemma_family_loan_send_desc",
                    tagResKey = "dilemma_family_loan_send_tag",
                    cashDelta = -60,
                    relationshipDelta = 12,
                    karmaDelta = 6,
                    outcomeSuccessResKey = "dilemma_family_loan_send_outcome"
                ),
                DilemmaChoice(
                    id = "loan_deny",
                    titleResKey = "dilemma_family_loan_deny",
                    descResKey = "dilemma_family_loan_deny_desc",
                    tagResKey = "dilemma_family_loan_deny_tag",
                    relationshipDelta = -4,
                    outcomeSuccessResKey = "dilemma_family_loan_deny_outcome"
                )
            )
        ),
        // Family 16: Kitchen Clean-up
        RoutineDilemma(
            id = "family_kitchen_dishes",
            category = RoutineCategory.FAMILY_DINNER,
            titleResKey = "dilemma_family_dishes_title",
            promptTemplate = "After dinner, a tower of greasy cookware remains in the sink while everyone else drifts into the living room.",
            minAge = 8,
            requiresFamily = true,
            choices = listOf(
                DilemmaChoice(
                    id = "dishes_wash",
                    titleResKey = "dilemma_family_dishes_wash",
                    descResKey = "dilemma_family_dishes_wash_desc",
                    tagResKey = "dilemma_family_dishes_wash_tag",
                    healthDelta = -4,
                    relationshipDelta = 8,
                    karmaDelta = 5,
                    outcomeSuccessResKey = "dilemma_family_dishes_wash_outcome"
                ),
                DilemmaChoice(
                    id = "dishes_leave",
                    titleResKey = "dilemma_family_dishes_leave",
                    descResKey = "dilemma_family_dishes_leave_desc",
                    tagResKey = "dilemma_family_dishes_leave_tag",
                    healthDelta = 3,
                    relationshipDelta = -3,
                    outcomeSuccessResKey = "dilemma_family_dishes_leave_outcome"
                )
            )
        )
    )

    fun getAllDilemmas(): List<RoutineDilemma> = catalog

    /**
     * Finds an eligible routine dilemma matching the character's life state and preferred category.
     */
    fun findEligibleDilemma(
        character: Character,
        preferredCategory: RoutineCategory? = null,
        excludedIds: Set<String> = emptySet(),
        random: Random = Random.Default
    ): RoutineDilemma? {
        val eligible = catalog.filter { dilemma ->
            dilemma.id !in excludedIds &&
                character.age in dilemma.minAge..dilemma.maxAge &&
                (!dilemma.requiresEmployment || character.career.currentJob != null) &&
                (!dilemma.requiresSchool || character.education.stage != SchoolStage.NONE && character.education.stage != SchoolStage.GRADUATED) &&
                (!dilemma.requiresFamily || character.family.any { it.alive })
        }

        if (eligible.isEmpty()) return null

        val prioritized = if (preferredCategory != null) {
            eligible.filter { it.category == preferredCategory }.ifEmpty { eligible }
        } else {
            eligible
        }

        return prioritized.randomOrNull(random)
    }

    /**
     * Interpolates placeholders ({transportMode}, {moneyApp}, {greeting}) in the dilemma prompt.
     */
    fun interpolatePrompt(promptTemplate: String, countryCode: String): String {
        val flavor = CountryCatalog.flavorFor(countryCode)
        return FlavorInterpolator.interpolate(promptTemplate, flavor)
    }

    /**
     * Resolves a chosen dilemma option, applying stat, cash, relationship, and heat deltas.
     */
    fun resolveChoice(
        character: Character,
        dilemma: RoutineDilemma,
        choice: DilemmaChoice,
        random: Random = Random.Default
    ): DilemmaResolution {
        val success = if (choice.successProbability < 1.0f) {
            random.nextFloat() <= choice.successProbability
        } else {
            true
        }

        val healthDelta = if (success) choice.healthDelta else choice.failureHealthDelta
        val happinessDelta = if (success) choice.happinessDelta else choice.failureHappinessDelta
        val smartsDelta = choice.smartsDelta
        val karmaDelta = choice.karmaDelta
        val rawCashDelta = if (success) choice.cashDelta else choice.failureCashDelta
        val heatDelta = if (success) choice.heatDelta else choice.failureHeatDelta
        val relationshipDelta = choice.relationshipDelta
        val outcomeMessageResKey = if (success) choice.outcomeSuccessResKey else choice.outcomeFailureResKey.ifEmpty { choice.outcomeSuccessResKey }

        // Scale cash delta based on national economy
        val scaledCashDelta = if (rawCashDelta != 0) {
            val sign = if (rawCashDelta > 0) 1 else -1
            sign * EconomyScaler.scaleAmount(abs(rawCashDelta), character.countryCode)
        } else {
            0
        }

        val newMoney = (character.stats.money + scaledCashDelta).coerceAtLeast(0)
        val updatedStats = character.stats.copy(
            health = (character.stats.health + healthDelta).coerceIn(0, 100),
            happiness = (character.stats.happiness + happinessDelta).coerceIn(0, 100),
            smarts = (character.stats.smarts + smartsDelta).coerceIn(0, 100),
            karma = (character.stats.karma + karmaDelta).coerceIn(0, 100),
            money = newMoney
        )

        val updatedRecord = if (heatDelta != 0) {
            character.criminalRecord.copy(
                heatLevel = (character.criminalRecord.heatLevel + heatDelta).coerceIn(0, 100)
            )
        } else {
            character.criminalRecord
        }

        val updatedFamily = if (dilemma.category == RoutineCategory.FAMILY_DINNER && relationshipDelta != 0) {
            character.family.map { person ->
                if (person.alive) {
                    person.copy(relationshipLevel = (person.relationshipLevel + relationshipDelta).coerceIn(0, 100))
                } else {
                    person
                }
            }
        } else {
            character.family
        }

        val updatedCareer = if (dilemma.category == RoutineCategory.WORK_STUDY && relationshipDelta != 0) {
            val updatedRoster = character.career.workplaceRoster.map {
                it.copy(relationshipLevel = (it.relationshipLevel + relationshipDelta).coerceIn(0, 100))
            }
            character.career.copy(
                workplaceRoster = updatedRoster,
                bossRelationship = (character.career.bossRelationship + relationshipDelta).coerceIn(0, 100)
            )
        } else {
            character.career
        }

        val updatedCharacter = character.copy(
            stats = updatedStats,
            criminalRecord = updatedRecord,
            family = updatedFamily,
            career = updatedCareer
        )

        return DilemmaResolution(
            dilemmaId = dilemma.id,
            choiceId = choice.id,
            success = success,
            outcomeMessageResKey = outcomeMessageResKey,
            character = updatedCharacter,
            healthDelta = healthDelta,
            happinessDelta = happinessDelta,
            smartsDelta = smartsDelta,
            karmaDelta = karmaDelta,
            cashDelta = scaledCashDelta,
            heatDelta = heatDelta,
            relationshipDelta = relationshipDelta
        )
    }
}
