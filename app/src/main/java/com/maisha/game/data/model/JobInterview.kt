package com.maisha.game.data.model

import kotlinx.serialization.Serializable

/**
 * An individual choice within a job interview question.
 */
@Serializable
data class InterviewChoice(
    val id: String,
    val textResKey: String,
    val scoreDelta: Int,
    val feedbackResKey: String
)

/**
 * An interview question presented to a job applicant.
 */
@Serializable
data class InterviewQuestion(
    val id: String,
    val promptResKey: String,
    val choices: List<InterviewChoice>
)

/**
 * Represents an active interactive job interview session before hiring.
 */
@Serializable
data class JobInterviewSession(
    val jobId: String,
    val jobTitle: String,
    val companyName: String,
    val countryCode: String,
    val questions: List<InterviewQuestion>,
    val currentQuestionIndex: Int = 0,
    val accumulatedScore: Int = 0,
    val previousFeedbackKey: String? = null,
    val isConcluded: Boolean = false,
    val isHired: Boolean = false,
    val finalFeedbackKey: String? = null
) {
    val currentQuestion: InterviewQuestion? get() = questions.getOrNull(currentQuestionIndex)
}
