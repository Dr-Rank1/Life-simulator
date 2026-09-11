package com.maisha.game.data.content

import com.maisha.game.data.model.TriviaQuestion

/**
 * Sync reader for exam-paper flavor. Age-up only reads this cache.
 */
interface TriviaCatalog {
    fun nextQuestion(): TriviaQuestion?
}

object FallbackTriviaCatalog : TriviaCatalog {
    val questions: List<TriviaQuestion> = listOf(
        TriviaQuestion(
            prompt = "Which planet is known as the Red Planet?",
            correctAnswer = "Mars",
            incorrectAnswers = listOf("Venus", "Jupiter", "Mercury"),
            category = "Science",
            difficulty = "easy"
        ),
        TriviaQuestion(
            prompt = "What is the capital of Japan?",
            correctAnswer = "Tokyo",
            incorrectAnswers = listOf("Kyoto", "Osaka", "Nagoya"),
            category = "Geography",
            difficulty = "easy"
        ),
        TriviaQuestion(
            prompt = "How many minutes are in a day?",
            correctAnswer = "1440",
            incorrectAnswers = listOf("1240", "1600", "1000"),
            category = "Mathematics",
            difficulty = "medium"
        ),
        TriviaQuestion(
            prompt = "Who wrote Romeo and Juliet?",
            correctAnswer = "William Shakespeare",
            incorrectAnswers = listOf("Charles Dickens", "Jane Austen", "Mark Twain"),
            category = "Literature",
            difficulty = "easy"
        ),
        TriviaQuestion(
            prompt = "What gas do plants absorb from the air?",
            correctAnswer = "Carbon dioxide",
            incorrectAnswers = listOf("Oxygen", "Nitrogen", "Helium"),
            category = "Science",
            difficulty = "easy"
        ),
        TriviaQuestion(
            prompt = "Which ocean is the largest?",
            correctAnswer = "Pacific Ocean",
            incorrectAnswers = listOf("Atlantic Ocean", "Indian Ocean", "Arctic Ocean"),
            category = "Geography",
            difficulty = "easy"
        ),
        TriviaQuestion(
            prompt = "What is 12 multiplied by 12?",
            correctAnswer = "144",
            incorrectAnswers = listOf("124", "121", "132"),
            category = "Mathematics",
            difficulty = "easy"
        ),
        TriviaQuestion(
            prompt = "In what year did the first human land on the Moon?",
            correctAnswer = "1969",
            incorrectAnswers = listOf("1959", "1972", "1965"),
            category = "History",
            difficulty = "medium"
        )
    )

    override fun nextQuestion(): TriviaQuestion = questions.random()
}
