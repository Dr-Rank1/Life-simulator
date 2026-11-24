package com.maisha.game.ui.components

import androidx.annotation.StringRes
import com.maisha.game.R
import com.maisha.game.data.model.RoutineCategory

/**
 * Maps pure domain routine dilemma keys to localized Android string resources.
 */
object RoutineDilemmaStrings {

    @StringRes
    fun categoryTitleRes(category: RoutineCategory): Int = when (category) {
        RoutineCategory.COMMUTE -> R.string.routine_category_commute
        RoutineCategory.WORK_STUDY -> R.string.routine_category_work_study
        RoutineCategory.WEEKEND_SPLURGE -> R.string.routine_category_weekend_splurge
        RoutineCategory.FAMILY_DINNER -> R.string.routine_category_family_dinner
    }

    @StringRes
    fun stringResFor(key: String): Int = when (key) {
        // Commute 1: Seat
        "dilemma_commute_seat_title" -> R.string.dilemma_commute_seat_title
        "dilemma_commute_seat_prompt" -> R.string.dilemma_commute_seat_prompt
        "dilemma_commute_seat_offer" -> R.string.dilemma_commute_seat_offer
        "dilemma_commute_seat_offer_desc" -> R.string.dilemma_commute_seat_offer_desc
        "dilemma_commute_seat_offer_tag" -> R.string.dilemma_commute_seat_offer_tag
        "dilemma_commute_seat_offer_outcome" -> R.string.dilemma_commute_seat_offer_outcome
        "dilemma_commute_seat_ignore" -> R.string.dilemma_commute_seat_ignore
        "dilemma_commute_seat_ignore_desc" -> R.string.dilemma_commute_seat_ignore_desc
        "dilemma_commute_seat_ignore_tag" -> R.string.dilemma_commute_seat_ignore_tag
        "dilemma_commute_seat_ignore_outcome" -> R.string.dilemma_commute_seat_ignore_outcome

        // Commute 2: Shortcut
        "dilemma_commute_shortcut_title" -> R.string.dilemma_commute_shortcut_title
        "dilemma_commute_shortcut_prompt" -> R.string.dilemma_commute_shortcut_prompt
        "dilemma_commute_shortcut_take" -> R.string.dilemma_commute_shortcut_take
        "dilemma_commute_shortcut_take_desc" -> R.string.dilemma_commute_shortcut_take_desc
        "dilemma_commute_shortcut_take_tag" -> R.string.dilemma_commute_shortcut_take_tag
        "dilemma_commute_shortcut_take_success" -> R.string.dilemma_commute_shortcut_take_success
        "dilemma_commute_shortcut_take_fail" -> R.string.dilemma_commute_shortcut_take_fail
        "dilemma_commute_shortcut_stay" -> R.string.dilemma_commute_shortcut_stay
        "dilemma_commute_shortcut_stay_desc" -> R.string.dilemma_commute_shortcut_stay_desc
        "dilemma_commute_shortcut_stay_tag" -> R.string.dilemma_commute_shortcut_stay_tag
        "dilemma_commute_shortcut_stay_outcome" -> R.string.dilemma_commute_shortcut_stay_outcome

        // Commute 3: Fare
        "dilemma_commute_fare_title" -> R.string.dilemma_commute_fare_title
        "dilemma_commute_fare_prompt" -> R.string.dilemma_commute_fare_prompt
        "dilemma_commute_fare_dispute" -> R.string.dilemma_commute_fare_dispute
        "dilemma_commute_fare_dispute_desc" -> R.string.dilemma_commute_fare_dispute_desc
        "dilemma_commute_fare_dispute_tag" -> R.string.dilemma_commute_fare_dispute_tag
        "dilemma_commute_fare_dispute_success" -> R.string.dilemma_commute_fare_dispute_success
        "dilemma_commute_fare_dispute_fail" -> R.string.dilemma_commute_fare_dispute_fail
        "dilemma_commute_fare_pay" -> R.string.dilemma_commute_fare_pay
        "dilemma_commute_fare_pay_desc" -> R.string.dilemma_commute_fare_pay_desc
        "dilemma_commute_fare_pay_tag" -> R.string.dilemma_commute_fare_pay_tag
        "dilemma_commute_fare_pay_outcome" -> R.string.dilemma_commute_fare_pay_outcome

        // Commute 4: Push
        "dilemma_commute_push_title" -> R.string.dilemma_commute_push_title
        "dilemma_commute_push_prompt" -> R.string.dilemma_commute_push_prompt
        "dilemma_commute_push_help" -> R.string.dilemma_commute_push_help
        "dilemma_commute_push_help_desc" -> R.string.dilemma_commute_push_help_desc
        "dilemma_commute_push_help_tag" -> R.string.dilemma_commute_push_help_tag
        "dilemma_commute_push_help_outcome" -> R.string.dilemma_commute_push_help_outcome
        "dilemma_commute_push_watch" -> R.string.dilemma_commute_push_watch
        "dilemma_commute_push_watch_desc" -> R.string.dilemma_commute_push_watch_desc
        "dilemma_commute_push_watch_tag" -> R.string.dilemma_commute_push_watch_tag
        "dilemma_commute_push_watch_outcome" -> R.string.dilemma_commute_push_watch_outcome

        // Work 5: Cover
        "dilemma_work_cover_title" -> R.string.dilemma_work_cover_title
        "dilemma_work_cover_prompt" -> R.string.dilemma_work_cover_prompt
        "dilemma_work_cover_accept" -> R.string.dilemma_work_cover_accept
        "dilemma_work_cover_accept_desc" -> R.string.dilemma_work_cover_accept_desc
        "dilemma_work_cover_accept_tag" -> R.string.dilemma_work_cover_accept_tag
        "dilemma_work_cover_accept_outcome" -> R.string.dilemma_work_cover_accept_outcome
        "dilemma_work_cover_decline" -> R.string.dilemma_work_cover_decline
        "dilemma_work_cover_decline_desc" -> R.string.dilemma_work_cover_decline_desc
        "dilemma_work_cover_decline_tag" -> R.string.dilemma_work_cover_decline_tag
        "dilemma_work_cover_decline_outcome" -> R.string.dilemma_work_cover_decline_outcome

        // Work 6: Midnight Oil
        "dilemma_work_midnight_title" -> R.string.dilemma_work_midnight_title
        "dilemma_work_midnight_prompt" -> R.string.dilemma_work_midnight_prompt
        "dilemma_work_midnight_push" -> R.string.dilemma_work_midnight_push
        "dilemma_work_midnight_push_desc" -> R.string.dilemma_work_midnight_push_desc
        "dilemma_work_midnight_push_tag" -> R.string.dilemma_work_midnight_push_tag
        "dilemma_work_midnight_push_outcome" -> R.string.dilemma_work_midnight_push_outcome
        "dilemma_work_midnight_sleep" -> R.string.dilemma_work_midnight_sleep
        "dilemma_work_midnight_sleep_desc" -> R.string.dilemma_work_midnight_sleep_desc
        "dilemma_work_midnight_sleep_tag" -> R.string.dilemma_work_midnight_sleep_tag
        "dilemma_work_midnight_sleep_outcome" -> R.string.dilemma_work_midnight_sleep_outcome

        // Work 7: Leak
        "dilemma_work_leak_title" -> R.string.dilemma_work_leak_title
        "dilemma_work_leak_prompt" -> R.string.dilemma_work_leak_prompt
        "dilemma_work_leak_share" -> R.string.dilemma_work_leak_share
        "dilemma_work_leak_share_desc" -> R.string.dilemma_work_leak_share_desc
        "dilemma_work_leak_share_tag" -> R.string.dilemma_work_leak_share_tag
        "dilemma_work_leak_share_outcome" -> R.string.dilemma_work_leak_share_outcome
        "dilemma_work_leak_ignore" -> R.string.dilemma_work_leak_ignore
        "dilemma_work_leak_ignore_desc" -> R.string.dilemma_work_leak_ignore_desc
        "dilemma_work_leak_ignore_tag" -> R.string.dilemma_work_leak_ignore_tag
        "dilemma_work_leak_ignore_outcome" -> R.string.dilemma_work_leak_ignore_outcome

        // Work 8: Shortcut
        "dilemma_work_shortcut_title" -> R.string.dilemma_work_shortcut_title
        "dilemma_work_shortcut_prompt" -> R.string.dilemma_work_shortcut_prompt
        "dilemma_work_shortcut_gamble" -> R.string.dilemma_work_shortcut_gamble
        "dilemma_work_shortcut_gamble_desc" -> R.string.dilemma_work_shortcut_gamble_desc
        "dilemma_work_shortcut_gamble_tag" -> R.string.dilemma_work_shortcut_gamble_tag
        "dilemma_work_shortcut_gamble_success" -> R.string.dilemma_work_shortcut_gamble_success
        "dilemma_work_shortcut_gamble_fail" -> R.string.dilemma_work_shortcut_gamble_fail
        "dilemma_work_shortcut_manual" -> R.string.dilemma_work_shortcut_manual
        "dilemma_work_shortcut_manual_desc" -> R.string.dilemma_work_shortcut_manual_desc
        "dilemma_work_shortcut_manual_tag" -> R.string.dilemma_work_shortcut_manual_tag
        "dilemma_work_shortcut_manual_outcome" -> R.string.dilemma_work_shortcut_manual_outcome

        // Weekend 9: Haggle
        "dilemma_splurge_haggle_title" -> R.string.dilemma_splurge_haggle_title
        "dilemma_splurge_haggle_prompt" -> R.string.dilemma_splurge_haggle_prompt
        "dilemma_splurge_haggle_push" -> R.string.dilemma_splurge_haggle_push
        "dilemma_splurge_haggle_push_desc" -> R.string.dilemma_splurge_haggle_push_desc
        "dilemma_splurge_haggle_push_tag" -> R.string.dilemma_splurge_haggle_push_tag
        "dilemma_splurge_haggle_push_success" -> R.string.dilemma_splurge_haggle_push_success
        "dilemma_splurge_haggle_push_fail" -> R.string.dilemma_splurge_haggle_push_fail
        "dilemma_splurge_haggle_pay" -> R.string.dilemma_splurge_haggle_pay
        "dilemma_splurge_haggle_pay_desc" -> R.string.dilemma_splurge_haggle_pay_desc
        "dilemma_splurge_haggle_pay_tag" -> R.string.dilemma_splurge_haggle_pay_tag
        "dilemma_splurge_haggle_pay_outcome" -> R.string.dilemma_splurge_haggle_pay_outcome

        // Weekend 10: Bill
        "dilemma_splurge_bill_title" -> R.string.dilemma_splurge_bill_title
        "dilemma_splurge_bill_prompt" -> R.string.dilemma_splurge_bill_prompt
        "dilemma_splurge_bill_item" -> R.string.dilemma_splurge_bill_item
        "dilemma_splurge_bill_item_desc" -> R.string.dilemma_splurge_bill_item_desc
        "dilemma_splurge_bill_item_tag" -> R.string.dilemma_splurge_bill_item_tag
        "dilemma_splurge_bill_item_outcome" -> R.string.dilemma_splurge_bill_item_outcome
        "dilemma_splurge_bill_even" -> R.string.dilemma_splurge_bill_even
        "dilemma_splurge_bill_even_desc" -> R.string.dilemma_splurge_bill_even_desc
        "dilemma_splurge_bill_even_tag" -> R.string.dilemma_splurge_bill_even_tag
        "dilemma_splurge_bill_even_outcome" -> R.string.dilemma_splurge_bill_even_outcome

        // Weekend 11: Busker
        "dilemma_splurge_busker_title" -> R.string.dilemma_splurge_busker_title
        "dilemma_splurge_busker_prompt" -> R.string.dilemma_splurge_busker_prompt
        "dilemma_splurge_busker_tip" -> R.string.dilemma_splurge_busker_tip
        "dilemma_splurge_busker_tip_desc" -> R.string.dilemma_splurge_busker_tip_desc
        "dilemma_splurge_busker_tip_tag" -> R.string.dilemma_splurge_busker_tip_tag
        "dilemma_splurge_busker_tip_outcome" -> R.string.dilemma_splurge_busker_tip_outcome
        "dilemma_splurge_busker_pass" -> R.string.dilemma_splurge_busker_pass
        "dilemma_splurge_busker_pass_desc" -> R.string.dilemma_splurge_busker_pass_desc
        "dilemma_splurge_busker_pass_tag" -> R.string.dilemma_splurge_busker_pass_tag
        "dilemma_splurge_busker_pass_outcome" -> R.string.dilemma_splurge_busker_pass_outcome

        // Weekend 12: Alley Game
        "dilemma_splurge_street_game_title" -> R.string.dilemma_splurge_street_game_title
        "dilemma_splurge_street_game_prompt" -> R.string.dilemma_splurge_street_game_prompt
        "dilemma_splurge_street_game_bet" -> R.string.dilemma_splurge_street_game_bet
        "dilemma_splurge_street_game_bet_desc" -> R.string.dilemma_splurge_street_game_bet_desc
        "dilemma_splurge_street_game_bet_tag" -> R.string.dilemma_splurge_street_game_bet_tag
        "dilemma_splurge_street_game_bet_success" -> R.string.dilemma_splurge_street_game_bet_success
        "dilemma_splurge_street_game_bet_fail" -> R.string.dilemma_splurge_street_game_bet_fail
        "dilemma_splurge_street_game_walk" -> R.string.dilemma_splurge_street_game_walk
        "dilemma_splurge_street_game_walk_desc" -> R.string.dilemma_splurge_street_game_walk_desc
        "dilemma_splurge_street_game_walk_tag" -> R.string.dilemma_splurge_street_game_walk_tag
        "dilemma_splurge_street_game_walk_outcome" -> R.string.dilemma_splurge_street_game_walk_outcome

        // Family 13: Advice
        "dilemma_family_advice_title" -> R.string.dilemma_family_advice_title
        "dilemma_family_advice_prompt" -> R.string.dilemma_family_advice_prompt
        "dilemma_family_advice_nod" -> R.string.dilemma_family_advice_nod
        "dilemma_family_advice_nod_desc" -> R.string.dilemma_family_advice_nod_desc
        "dilemma_family_advice_nod_tag" -> R.string.dilemma_family_advice_nod_tag
        "dilemma_family_advice_nod_outcome" -> R.string.dilemma_family_advice_nod_outcome
        "dilemma_family_advice_stand" -> R.string.dilemma_family_advice_stand
        "dilemma_family_advice_stand_desc" -> R.string.dilemma_family_advice_stand_desc
        "dilemma_family_advice_stand_tag" -> R.string.dilemma_family_advice_stand_tag
        "dilemma_family_advice_stand_outcome" -> R.string.dilemma_family_advice_stand_outcome

        // Family 14: Portion
        "dilemma_family_portion_title" -> R.string.dilemma_family_portion_title
        "dilemma_family_portion_prompt" -> R.string.dilemma_family_portion_prompt
        "dilemma_family_portion_yield" -> R.string.dilemma_family_portion_yield
        "dilemma_family_portion_yield_desc" -> R.string.dilemma_family_portion_yield_desc
        "dilemma_family_portion_yield_tag" -> R.string.dilemma_family_portion_yield_tag
        "dilemma_family_portion_yield_outcome" -> R.string.dilemma_family_portion_yield_outcome
        "dilemma_family_portion_take" -> R.string.dilemma_family_portion_take
        "dilemma_family_portion_take_desc" -> R.string.dilemma_family_portion_take_desc
        "dilemma_family_portion_take_tag" -> R.string.dilemma_family_portion_take_tag
        "dilemma_family_portion_take_outcome" -> R.string.dilemma_family_portion_take_outcome

        // Family 15: Loan
        "dilemma_family_loan_title" -> R.string.dilemma_family_loan_title
        "dilemma_family_loan_prompt" -> R.string.dilemma_family_loan_prompt
        "dilemma_family_loan_send" -> R.string.dilemma_family_loan_send
        "dilemma_family_loan_send_desc" -> R.string.dilemma_family_loan_send_desc
        "dilemma_family_loan_send_tag" -> R.string.dilemma_family_loan_send_tag
        "dilemma_family_loan_send_outcome" -> R.string.dilemma_family_loan_send_outcome
        "dilemma_family_loan_deny" -> R.string.dilemma_family_loan_deny
        "dilemma_family_loan_deny_desc" -> R.string.dilemma_family_loan_deny_desc
        "dilemma_family_loan_deny_tag" -> R.string.dilemma_family_loan_deny_tag
        "dilemma_family_loan_deny_outcome" -> R.string.dilemma_family_loan_deny_outcome

        // Family 16: Dishes
        "dilemma_family_dishes_title" -> R.string.dilemma_family_dishes_title
        "dilemma_family_dishes_prompt" -> R.string.dilemma_family_dishes_prompt
        "dilemma_family_dishes_wash" -> R.string.dilemma_family_dishes_wash
        "dilemma_family_dishes_wash_desc" -> R.string.dilemma_family_dishes_wash_desc
        "dilemma_family_dishes_wash_tag" -> R.string.dilemma_family_dishes_wash_tag
        "dilemma_family_dishes_wash_outcome" -> R.string.dilemma_family_dishes_wash_outcome
        "dilemma_family_dishes_leave" -> R.string.dilemma_family_dishes_leave
        "dilemma_family_dishes_leave_desc" -> R.string.dilemma_family_dishes_leave_desc
        "dilemma_family_dishes_leave_tag" -> R.string.dilemma_family_dishes_leave_tag
        "dilemma_family_dishes_leave_outcome" -> R.string.dilemma_family_dishes_leave_outcome

        // Fallback / Header
        else -> R.string.routine_dilemma_headline
    }
}
