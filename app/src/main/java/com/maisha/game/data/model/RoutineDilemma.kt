package com.maisha.game.data.model

import kotlinx.serialization.Serializable

/**
 * Category of everyday routine where snap dilemmas occur.
 */
@Serializable
enum class RoutineCategory {
    COMMUTE,
    WORK_STUDY,
    WEEKEND_SPLURGE,
    FAMILY_DINNER
}

/**
 * A choice within a routine snap dilemma.
 */
@Serializable
data class DilemmaChoice(
    val id: String,
    val titleResKey: String,
    val descResKey: String,
    val tagResKey: String,
    val healthDelta: Int = 0,
    val happinessDelta: Int = 0,
    val smartsDelta: Int = 0,
    val karmaDelta: Int = 0,
    val cashDelta: Int = 0,
    val heatDelta: Int = 0,
    val relationshipDelta: Int = 0,
    val successProbability: Float = 1.0f,
    val failureHealthDelta: Int = 0,
    val failureHappinessDelta: Int = 0,
    val failureCashDelta: Int = 0,
    val failureHeatDelta: Int = 0,
    val outcomeSuccessResKey: String,
    val outcomeFailureResKey: String = ""
)

/**
 * A bite-sized micro-dilemma representing everyday life routines.
 */
@Serializable
data class RoutineDilemma(
    val id: String,
    val category: RoutineCategory,
    val titleResKey: String,
    val promptTemplate: String,
    val minAge: Int = 4,
    val maxAge: Int = 120,
    val requiresEmployment: Boolean = false,
    val requiresSchool: Boolean = false,
    val requiresFamily: Boolean = false,
    val requiresVehicle: Boolean = false,
    val choices: List<DilemmaChoice>
)

/**
 * Result of resolving a routine dilemma.
 */
data class DilemmaResolution(
    val dilemmaId: String,
    val choiceId: String,
    val success: Boolean,
    val outcomeMessageResKey: String,
    val character: Character,
    val healthDelta: Int,
    val happinessDelta: Int,
    val smartsDelta: Int,
    val karmaDelta: Int,
    val cashDelta: Int,
    val heatDelta: Int,
    val relationshipDelta: Int
)
