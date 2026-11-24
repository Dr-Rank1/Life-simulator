package com.maisha.game.domain

/**
 * Classifies event-log lines so the Life tab can tint milestone entries and organize story timeline cards.
 * Heuristic on English log text (domain engines still write English narrative).
 */
enum class EventLogTone {
    MILESTONE,
    POSITIVE,
    NEGATIVE,
    NEUTRAL
}

enum class EventLogCategory {
    MILESTONE,
    CRIME_DRAMA,
    CAREER_SCHOOL,
    FAMILY_ROMANCE,
    HEALTH_WELLNESS,
    GENERAL
}

object EventLogClassifier {
    private val milestoneKeywords = listOf(
        "married", "marriage", "born", "graduated", "graduation", "promoted",
        "promotion", "achievement", "centenarian", "legacy", "heir", "quest complete",
        "citizenship", "elected", "won"
    )
    private val positiveKeywords = listOf(
        "hired", "friend", "gift", "donated", "volunteer", "recovered", "bought",
        "profit", "followers", "salary increases", "passed"
    )
    private val negativeKeywords = listOf(
        "arrest", "fired", "died", "death", "ill", "sick", "divorce", "broke up",
        "downsiz", "failed", "jailed", "incarcerat", "stolen", "crash", "debt"
    )

    private val dramaKeywords = listOf(
        "arrest", "crime", "robbery", "jail", "prison", "incarcerat", "caught",
        "police", "bribe", "stolen", "burglary", "detention", "suspended", "fight"
    )

    private val careerSchoolKeywords = listOf(
        "graduat", "school", "university", "promot", "hired", "fired", "exam",
        "interview", "salary", "job", "career", "clique", "aptitude", "hobby",
        "masterclass", "practice", "degree", "internship", "business"
    )

    private val familyRomanceKeywords = listOf(
        "married", "marriage", "wedding", "divorce", "spouse", "dating", "broke up",
        "child", "baby", "sibling", "parent", "mother", "father", "son", "daughter",
        "adopted", "family"
    )

    private val healthWellnessKeywords = listOf(
        "doctor", "hospital", "clinic", "ill", "sick", "disease", "treatment",
        "wellness", "gym", "diet", "mental", "stress", "exhaustion", "recovered"
    )

    fun classify(entry: String): EventLogTone {
        val lower = entry.lowercase()
        if (milestoneKeywords.any { it in lower }) return EventLogTone.MILESTONE
        if (negativeKeywords.any { it in lower }) return EventLogTone.NEGATIVE
        if (positiveKeywords.any { it in lower }) return EventLogTone.POSITIVE
        return EventLogTone.NEUTRAL
    }

    fun categorize(entry: String): EventLogCategory {
        val lower = entry.lowercase()
        if (milestoneKeywords.any { it in lower }) return EventLogCategory.MILESTONE
        if (dramaKeywords.any { it in lower }) return EventLogCategory.CRIME_DRAMA
        if (careerSchoolKeywords.any { it in lower }) return EventLogCategory.CAREER_SCHOOL
        if (familyRomanceKeywords.any { it in lower }) return EventLogCategory.FAMILY_ROMANCE
        if (healthWellnessKeywords.any { it in lower }) return EventLogCategory.HEALTH_WELLNESS
        return EventLogCategory.GENERAL
    }
}
