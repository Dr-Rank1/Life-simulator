package com.maisha.game.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AdviceQuote(
    val id: String,
    val text: String
)

@Serializable
data class TriviaQuestion(
    val prompt: String,
    val correctAnswer: String,
    val incorrectAnswers: List<String> = emptyList(),
    val category: String = "",
    val difficulty: String = ""
)
