package com.maisha.game.ui.components

import androidx.annotation.StringRes
import com.maisha.game.R

object LifeArchetypeStrings {

    @StringRes
    fun chapterStringRes(key: String): Int = when (key) {
        "chapter_first_steps" -> R.string.chapter_first_steps
        "chapter_fragile_beginnings" -> R.string.chapter_fragile_beginnings
        "chapter_sunlit_nursery" -> R.string.chapter_sunlit_nursery
        "chapter_curious_explorer" -> R.string.chapter_curious_explorer
        "chapter_little_wonder" -> R.string.chapter_little_wonder
        "chapter_carefree_days" -> R.string.chapter_carefree_days
        "chapter_playground_champion" -> R.string.chapter_playground_champion
        "chapter_budding_artist" -> R.string.chapter_budding_artist
        "chapter_young_tinkerer" -> R.string.chapter_young_tinkerer
        "chapter_classroom_prodigy" -> R.string.chapter_classroom_prodigy
        "chapter_streetwise_kid" -> R.string.chapter_streetwise_kid
        "chapter_schoolyard_days" -> R.string.chapter_schoolyard_days
        "chapter_troubled_youth" -> R.string.chapter_troubled_youth
        "chapter_reckless_rebel" -> R.string.chapter_reckless_rebel
        "chapter_varsity_aspirant" -> R.string.chapter_varsity_aspirant
        "chapter_academic_focus" -> R.string.chapter_academic_focus
        "chapter_creative_spark" -> R.string.chapter_creative_spark
        "chapter_lone_wolf" -> R.string.chapter_lone_wolf
        "chapter_social_star" -> R.string.chapter_social_star
        "chapter_crossing_thresholds" -> R.string.chapter_crossing_thresholds
        "chapter_juvenile_outlaw" -> R.string.chapter_juvenile_outlaw
        "chapter_rising_contender" -> R.string.chapter_rising_contender
        "chapter_honor_roll_bound" -> R.string.chapter_honor_roll_bound
        "chapter_teenage_breadwinner" -> R.string.chapter_teenage_breadwinner
        "chapter_crossroads_of_youth" -> R.string.chapter_crossroads_of_youth
        "chapter_behind_bars" -> R.string.chapter_behind_bars
        "chapter_covert_double_life" -> R.string.chapter_covert_double_life
        "chapter_campus_grind" -> R.string.chapter_campus_grind
        "chapter_bootstrap_hustle" -> R.string.chapter_bootstrap_hustle
        "chapter_climbing_the_ladder" -> R.string.chapter_climbing_the_ladder
        "chapter_chasing_the_dream" -> R.string.chapter_chasing_the_dream
        "chapter_charting_the_path" -> R.string.chapter_charting_the_path
        "chapter_serving_time" -> R.string.chapter_serving_time
        "chapter_hall_of_power" -> R.string.chapter_hall_of_power
        "chapter_master_of_disguise" -> R.string.chapter_master_of_disguise
        "chapter_underworld_syndicate" -> R.string.chapter_underworld_syndicate
        "chapter_wealth_and_influence" -> R.string.chapter_wealth_and_influence
        "chapter_venture_builder" -> R.string.chapter_venture_builder
        "chapter_pillar_of_society" -> R.string.chapter_pillar_of_society
        "chapter_family_anchor" -> R.string.chapter_family_anchor
        "chapter_corporate_commander" -> R.string.chapter_corporate_commander
        "chapter_the_working_years" -> R.string.chapter_the_working_years
        "chapter_hardened_convict" -> R.string.chapter_hardened_convict
        "chapter_statesperson" -> R.string.chapter_statesperson
        "chapter_empire_builder" -> R.string.chapter_empire_builder
        "chapter_honored_patron" -> R.string.chapter_honored_patron
        "chapter_repentant_soul" -> R.string.chapter_repentant_soul
        "chapter_beloved_matriarch" -> R.string.chapter_beloved_matriarch
        "chapter_beloved_patriarch" -> R.string.chapter_beloved_patriarch
        "chapter_seasoned_veteran" -> R.string.chapter_seasoned_veteran
        "chapter_autumn_wisdom" -> R.string.chapter_autumn_wisdom
        "chapter_end_of_the_line" -> R.string.chapter_end_of_the_line
        "chapter_venerable_philanthropist" -> R.string.chapter_venerable_philanthropist
        "chapter_dynasty_elder" -> R.string.chapter_dynasty_elder
        "chapter_unforgiven_outlaw" -> R.string.chapter_unforgiven_outlaw
        "chapter_honored_grandparent" -> R.string.chapter_honored_grandparent
        "chapter_quiet_retirement" -> R.string.chapter_quiet_retirement
        else -> R.string.chapter_the_golden_twilight
    }

    @StringRes
    fun epitaphStringRes(key: String): Int = when (key) {
        "epitaph_saintly_benefactor" -> R.string.epitaph_saintly_benefactor
        "epitaph_tycoon_builder" -> R.string.epitaph_tycoon_builder
        "epitaph_family_pillar" -> R.string.epitaph_family_pillar
        "epitaph_shadow_operator" -> R.string.epitaph_shadow_operator
        "epitaph_unforgiven_pariah" -> R.string.epitaph_unforgiven_pariah
        "epitaph_scholar_polymath" -> R.string.epitaph_scholar_polymath
        "epitaph_public_champion" -> R.string.epitaph_public_champion
        "epitaph_solitary_wanderer" -> R.string.epitaph_solitary_wanderer
        else -> R.string.epitaph_everyday_endurance
    }

    @StringRes
    fun legacyStampStringRes(key: String): Int = when (key) {
        "stamp_dynasty_founder" -> R.string.stamp_dynasty_founder
        "stamp_civic_luminary" -> R.string.stamp_civic_luminary
        "stamp_honored_ancestor" -> R.string.stamp_honored_ancestor
        "stamp_renegade_legend" -> R.string.stamp_renegade_legend
        "stamp_solitary_pioneer" -> R.string.stamp_solitary_pioneer
        "stamp_steadfast_torchbearer" -> R.string.stamp_steadfast_torchbearer
        else -> R.string.stamp_humble_origin
    }
}
