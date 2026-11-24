package com.maisha.game.domain

import com.maisha.game.data.model.CareerTrack
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.ChildhoodAptitude
import com.maisha.game.data.model.CommunityStandingTier
import com.maisha.game.data.model.FamilyHeritageTag
import com.maisha.game.data.model.Gender
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.SchoolClique
import com.maisha.game.data.model.SchoolStage
import com.maisha.game.data.model.Stats
import kotlinx.serialization.Serializable

@Serializable
enum class StartingStatArchetype(
    val titleKey: String,
    val descriptionKey: String,
    val health: Int,
    val happiness: Int,
    val smarts: Int,
    val looks: Int
) {
    BALANCED(
        titleKey = "archetype_preset_balanced",
        descriptionKey = "archetype_preset_balanced_desc",
        health = 75,
        happiness = 75,
        smarts = 75,
        looks = 75
    ),
    PRODIGY(
        titleKey = "archetype_preset_prodigy",
        descriptionKey = "archetype_preset_prodigy_desc",
        health = 70,
        happiness = 70,
        smarts = 95,
        looks = 65
    ),
    ATHLETE(
        titleKey = "archetype_preset_athlete",
        descriptionKey = "archetype_preset_athlete_desc",
        health = 95,
        happiness = 80,
        smarts = 60,
        looks = 80
    ),
    CHARMER(
        titleKey = "archetype_preset_charmer",
        descriptionKey = "archetype_preset_charmer_desc",
        health = 70,
        happiness = 85,
        smarts = 65,
        looks = 95
    ),
    HUSTLER(
        titleKey = "archetype_preset_hustler",
        descriptionKey = "archetype_preset_hustler_desc",
        health = 75,
        happiness = 75,
        smarts = 85,
        looks = 80
    );

    fun buildInitialStats(): Stats = Stats(
        health = health,
        happiness = happiness,
        smarts = smarts,
        looks = looks,
        money = 0,
        karma = 50
    )
}

@Serializable
enum class LifeRibbon(
    val titleKey: String,
    val descriptionKey: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long
) {
    LOADED("ribbon_loaded", "ribbon_loaded_desc", 0xFFF59E0B, 0xFFD97706),
    ACADEMIC("ribbon_academic", "ribbon_academic_desc", 0xFF6366F1, 0xFF4F46E5),
    CUNNING("ribbon_cunning", "ribbon_cunning_desc", 0xFF8B5CF6, 0xFF7C3AED),
    FAMILY_ANCHOR("ribbon_family", "ribbon_family_desc", 0xFFEC4899, 0xFFDB2777),
    FAME_ICON("ribbon_fame", "ribbon_fame_desc", 0xFFE11D48, 0xFFBE123C),
    SAINT("ribbon_saint", "ribbon_saint_desc", 0xFF10B981, 0xFF059669),
    SCANDALOUS("ribbon_scandalous", "ribbon_scandalous_desc", 0xFFEF4444, 0xFFDC2626),
    WORKAHOLIC("ribbon_workaholic", "ribbon_workaholic_desc", 0xFF0EA5E9, 0xFF0284C7),
    MEDIOCRE("ribbon_mediocre", "ribbon_mediocre_desc", 0xFF64748B, 0xFF475569)
}

/**
 * Ribbon-lite life label shown on the memorial screen — derived from how the character lived.
 */
object LifeArchetypeEngine {

    fun evaluateRibbon(character: Character, netWorth: Int): LifeRibbon {
        val livingChildren = character.family.count { it.relation == RelationType.CHILD && it.alive }
        val timesArrested = character.criminalRecord.timesArrested

        return when {
            netWorth >= 2_000_000 -> LifeRibbon.LOADED
            character.career.fame >= 60 || character.socialMedia.followers >= 100_000 -> LifeRibbon.FAME_ICON
            character.criminalRecord.notoriety >= 50 || (character.criminalRecord.hasRecord && character.stats.money >= 500_000) -> LifeRibbon.CUNNING
            timesArrested >= 3 || character.criminalRecord.currentlyIncarcerated -> LifeRibbon.SCANDALOUS
            character.stats.karma >= 80 && timesArrested == 0 && character.stats.happiness >= 60 -> LifeRibbon.SAINT
            character.education.stage == SchoolStage.GRADUATED && character.stats.smarts >= 80 -> LifeRibbon.ACADEMIC
            livingChildren >= 3 && character.stats.happiness >= 65 -> LifeRibbon.FAMILY_ANCHOR
            character.career.yearsAtCurrentJob >= 20 || character.career.promotionsEarned >= 3 -> LifeRibbon.WORKAHOLIC
            else -> LifeRibbon.MEDIOCRE
        }
    }

    fun resolveTitleKey(character: Character, netWorth: Int): String {
        val scores = mutableMapOf<String, Int>()

        fun bump(key: String, amount: Int) {
            scores[key] = (scores[key] ?: 0) + amount
        }

        when (character.education.stage) {
            SchoolStage.GRADUATED -> bump("archetype_scholar", 40)
            SchoolStage.UNIVERSITY -> bump("archetype_scholar", 25)
            else -> if (character.education.gpa >= 3.5f) bump("archetype_scholar", 15)
        }

        if (netWorth >= 2_000_000) bump("archetype_builder", 35)
        else if (netWorth >= 500_000) bump("archetype_builder", 20)

        if (character.politics.currentOffice != null) bump("archetype_public_servant", 45)
        if (character.businesses.isNotEmpty()) bump("archetype_builder", 15)

        if (character.isLivingAbroad() || character.ancestryHistory.size >= 2) {
            bump("archetype_globetrotter", 20)
        }

        val livingChildren = character.family.count { it.relation == RelationType.CHILD && it.alive }
        if (livingChildren >= 2 || character.generationNumber >= 2) {
            bump("archetype_patriarch", 25)
        }

        when (character.career.careerTrack) {
            CareerTrack.ENTERTAINMENT -> bump("archetype_artist", 30 + character.career.trackLevel * 10)
            CareerTrack.PRO_SPORTS -> bump("archetype_athlete", 30 + character.career.trackLevel * 10)
            CareerTrack.MEDICAL -> bump("archetype_healer", 35 + character.career.trackLevel * 10)
            CareerTrack.LEGAL -> bump("archetype_counsel", 35 + character.career.trackLevel * 10)
            CareerTrack.SOFTWARE -> bump("archetype_scholar", 30 + character.career.trackLevel * 10)
            CareerTrack.CORPORATE -> bump("archetype_tycoon", 30 + character.career.trackLevel * 10)
            CareerTrack.NONE -> Unit
        }

        if (character.criminalRecord.recordExpunged) bump("archetype_reformed", 25)

        if (character.socialMedia.followers >= 50_000) bump("archetype_artist", 20)
        if (character.criminalRecord.timesArrested >= 2) bump("archetype_reformed", 15)
        if (character.stats.karma >= 75) bump("archetype_model_citizen", 25)

        val top = scores.maxByOrNull { it.value }
        return top?.key ?: when {
            character.age >= 70 -> "archetype_elder"
            character.stats.karma >= 60 -> "archetype_model_citizen"
            else -> "archetype_everyday"
        }
    }

    /**
     * Resolves a dynamic era/chapter title representing the character's dominant life trajectory.
     */
    fun resolveChapterKey(character: Character, netWorth: Int = 0): String {
        val age = character.age
        return when {
            // Era 1: Infancy (0–2)
            age <= 2 -> when {
                character.stats.health < 40 -> "chapter_fragile_beginnings"
                character.stats.happiness >= 70 -> "chapter_sunlit_nursery"
                else -> "chapter_first_steps"
            }
            // Era 2: Early Childhood (3–5)
            age <= 5 -> when {
                character.stats.smarts >= 70 -> "chapter_little_wonder"
                character.stats.happiness >= 70 -> "chapter_carefree_days"
                else -> "chapter_curious_explorer"
            }
            // Era 3: Primary School & Aptitudes (6–10)
            age <= 10 -> when (character.education.childhoodAptitude) {
                ChildhoodAptitude.ATHLETICS -> "chapter_playground_champion"
                ChildhoodAptitude.ARTS_MUSIC -> "chapter_budding_artist"
                ChildhoodAptitude.TECH_TINKERING -> "chapter_young_tinkerer"
                ChildhoodAptitude.ACADEMICS -> "chapter_classroom_prodigy"
                ChildhoodAptitude.STREET_HUSTLE -> "chapter_streetwise_kid"
                ChildhoodAptitude.NONE -> if (character.stats.smarts >= 70) "chapter_classroom_prodigy" else "chapter_schoolyard_days"
            }
            // Era 4: Middle School & Cliques (11–14)
            age <= 14 -> when {
                character.criminalRecord.heatLevel >= 25 || character.criminalRecord.timesArrested > 0 -> "chapter_troubled_youth"
                character.education.schoolClique == SchoolClique.REBELS -> "chapter_reckless_rebel"
                character.education.schoolClique == SchoolClique.JOCKS -> "chapter_varsity_aspirant"
                character.education.schoolClique == SchoolClique.NERDS -> "chapter_academic_focus"
                character.education.schoolClique == SchoolClique.POPULARS -> "chapter_social_star"
                character.education.schoolClique == SchoolClique.OUTCASTS -> "chapter_lone_wolf"
                else -> "chapter_crossing_thresholds"
            }
            // Era 5: High School & Identity (15–18)
            age <= 18 -> when {
                character.criminalRecord.currentlyIncarcerated -> "chapter_juvenile_outlaw"
                character.career.careerTrack == CareerTrack.PRO_SPORTS || character.career.careerTrack == CareerTrack.ENTERTAINMENT -> "chapter_rising_contender"
                character.education.gpa >= 3.6f -> "chapter_honor_roll_bound"
                character.stats.money >= 3_000 -> "chapter_teenage_breadwinner"
                character.criminalRecord.hasRecord -> "chapter_troubled_youth"
                else -> "chapter_crossroads_of_youth"
            }
            // Era 6: Young Adulthood (19–25)
            age <= 25 -> when {
                character.criminalRecord.currentlyIncarcerated -> "chapter_behind_bars"
                character.doubleLife.activeSecret != null -> "chapter_covert_double_life"
                character.education.stage == SchoolStage.UNIVERSITY -> "chapter_campus_grind"
                character.businesses.isNotEmpty() -> "chapter_bootstrap_hustle"
                character.career.currentJob != null -> "chapter_climbing_the_ladder"
                character.career.careerTrack != CareerTrack.NONE -> "chapter_chasing_the_dream"
                else -> "chapter_charting_the_path"
            }
            // Era 7: Prime Career & Family (26–45)
            age <= 45 -> when {
                character.criminalRecord.currentlyIncarcerated -> "chapter_serving_time"
                character.politics.currentOffice != null -> "chapter_hall_of_power"
                character.doubleLife.activeSecret != null -> "chapter_master_of_disguise"
                character.criminalRecord.notoriety >= 40 -> "chapter_underworld_syndicate"
                netWorth >= 1_000_000 -> "chapter_wealth_and_influence"
                character.businesses.isNotEmpty() -> "chapter_venture_builder"
                character.socialEcosystem.currentStandingTier == CommunityStandingTier.PILLAR_OF_SOCIETY -> "chapter_pillar_of_society"
                character.family.count { it.relation == RelationType.CHILD && it.alive } >= 2 -> "chapter_family_anchor"
                character.career.currentJob != null && character.career.performance >= 70f -> "chapter_corporate_commander"
                else -> "chapter_the_working_years"
            }
            // Era 8: Mature Adulthood & Midlife (46–65)
            age <= 65 -> when {
                character.criminalRecord.currentlyIncarcerated -> "chapter_hardened_convict"
                character.politics.currentOffice != null -> "chapter_statesperson"
                netWorth >= 2_000_000 -> "chapter_empire_builder"
                character.stats.karma >= 75 && character.socialEcosystem.familyHeritage == FamilyHeritageTag.HONORED_PHILANTHROPISTS -> "chapter_honored_patron"
                character.criminalRecord.recordExpunged -> "chapter_repentant_soul"
                character.family.count { it.relation == RelationType.CHILD && it.alive } >= 2 -> {
                    if (character.gender == Gender.FEMALE) "chapter_beloved_matriarch" else "chapter_beloved_patriarch"
                }
                character.career.currentJob != null -> "chapter_seasoned_veteran"
                else -> "chapter_autumn_wisdom"
            }
            // Era 9: Senior & Golden Twilight (66+)
            else -> when {
                character.criminalRecord.currentlyIncarcerated -> "chapter_end_of_the_line"
                character.socialEcosystem.familyHeritage == FamilyHeritageTag.HONORED_PHILANTHROPISTS || character.stats.karma >= 80 -> "chapter_venerable_philanthropist"
                netWorth >= 1_500_000 -> "chapter_dynasty_elder"
                character.socialEcosystem.currentStandingTier == CommunityStandingTier.NOTORIOUS_PARIAH || character.criminalRecord.notoriety >= 50 -> "chapter_unforgiven_outlaw"
                character.family.any { it.relation == RelationType.CHILD } -> "chapter_honored_grandparent"
                character.career.currentJob == null -> "chapter_quiet_retirement"
                else -> "chapter_the_golden_twilight"
            }
        }
    }

    /**
     * Resolves an evocative narrative epitaph honoring how the character lived and affected others.
     */
    fun resolveEpitaphKey(character: Character, netWorth: Int = 0): String {
        val livingChildren = character.family.count { it.relation == RelationType.CHILD && it.alive }
        val timesArrested = character.criminalRecord.timesArrested

        return when {
            character.stats.karma >= 75 && timesArrested == 0 && character.stats.happiness >= 50 -> {
                "epitaph_saintly_benefactor"
            }
            netWorth >= 1_000_000 -> {
                "epitaph_tycoon_builder"
            }
            character.politics.currentOffice != null -> {
                "epitaph_public_champion"
            }
            character.doubleLife.activeSecret != null || character.criminalRecord.notoriety >= 45 || (character.criminalRecord.hasRecord && character.stats.money >= 300_000) -> {
                "epitaph_shadow_operator"
            }
            timesArrested >= 3 || character.criminalRecord.currentlyIncarcerated || character.socialEcosystem.currentStandingTier == CommunityStandingTier.NOTORIOUS_PARIAH -> {
                "epitaph_unforgiven_pariah"
            }
            livingChildren >= 2 && character.stats.happiness >= 55 -> {
                "epitaph_family_pillar"
            }
            character.education.stage == SchoolStage.GRADUATED && character.stats.smarts >= 80 -> {
                "epitaph_scholar_polymath"
            }
            character.family.none { it.relation == RelationType.SPOUSE || it.relation == RelationType.CHILD } && (character.isLivingAbroad() || character.ancestryHistory.isNotEmpty()) -> {
                "epitaph_solitary_wanderer"
            }
            else -> {
                "epitaph_everyday_endurance"
            }
        }
    }

    /**
     * Resolves a multi-generational legacy stamp summarizing the character's lasting family footprint.
     */
    fun resolveLegacyStampKey(character: Character, netWorth: Int = 0): String {
        val hasChildren = character.family.any { it.relation == RelationType.CHILD }
        return when {
            netWorth >= 500_000 && hasChildren -> "stamp_dynasty_founder"
            character.politics.currentOffice != null || character.socialEcosystem.currentStandingTier == CommunityStandingTier.PILLAR_OF_SOCIETY -> "stamp_civic_luminary"
            hasChildren && character.stats.karma >= 65 -> "stamp_honored_ancestor"
            character.criminalRecord.notoriety >= 40 || character.doubleLife.activeSecret != null -> "stamp_renegade_legend"
            character.isLivingAbroad() && !hasChildren -> "stamp_solitary_pioneer"
            character.generationNumber >= 2 -> "stamp_steadfast_torchbearer"
            else -> "stamp_humble_origin"
        }
    }
}
