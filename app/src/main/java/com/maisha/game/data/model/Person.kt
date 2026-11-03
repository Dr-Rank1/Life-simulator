// app/src/main/java/com/maisha/game/data/model/Person.kt (modified — milestones + country + interaction tracking)
package com.maisha.game.data.model

import com.maisha.game.util.clampRelationshipLevel
import kotlinx.serialization.Serializable

@Serializable
enum class RelationType {
    MOTHER,
    FATHER,
    SIBLING,
    SPOUSE,
    CHILD,
    FRIEND,
    BEST_FRIEND,
    ENEMY
}

@Serializable
enum class RelationshipStage {
    CASUAL,
    EXCLUSIVE,
    ENGAGED,
    MARRIED
}

@Serializable
enum class WeddingVenueTier {
    COURTHOUSE,
    LOCAL_VENUE,
    LUXURY_DESTINATION
}

/**
 * A family member, friend, or dating prospect attached to a [Character].
 *
 * Stored in [Character.family] (including friends and spouses). Relationship level is 0–100.
 *
 * @property secondaryCountryCode Set on children when parents have different [countryCode] values (mixed heritage).
 * @property interactedThisYear When false at year tick, relationship may decay toward neutral.
 */
@Serializable
data class Person(
    val id: String,
    val name: String,
    val relation: RelationType,
    val gender: Gender = Gender.MALE,
    val age: Int,
    val alive: Boolean = true,
    val relationshipLevel: Int = 50,
    val stats: Stats = Stats(),
    val dateOfPartnership: Int? = null,
    val isMarried: Boolean = false,
    val avatarConfig: AvatarConfig = AvatarConfig.DEFAULT,
    val complimentsThisYear: Int = 0,
    val countryCode: String = "XX",
    val secondaryCountryCode: String? = null,
    val milestones: List<RelationshipMilestone> = emptyList(),
    val interactedThisYear: Boolean = false,
    /** When true, divorce settlement is reduced for this spouse. */
    val prenupSigned: Boolean = false,
    /** True for adopted children (vs biological). */
    val isAdopted: Boolean = false,
    /** Current occupation or profession for dating profiles and adult relationships. */
    val occupation: String = "Unemployed",
    /** Drama / volatility index (0–100). Higher values reject prenups and cause relationship friction. */
    val crazy: Int = 20,
    /** Current romantic relationship progression stage. */
    val stage: RelationshipStage = RelationshipStage.CASUAL,
    /** Interaction types already performed with this person in the current year. Reset on age-up. */
    val interactedTypesThisYear: List<String> = emptyList()
) {
    fun coerceRelationship(): Person = copy(
        relationshipLevel = clampRelationshipLevel(relationshipLevel)
    )
}
