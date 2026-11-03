package com.maisha.game.domain

import com.maisha.game.data.model.CareerTrack
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.SchoolStage
import kotlinx.serialization.Serializable

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
}
