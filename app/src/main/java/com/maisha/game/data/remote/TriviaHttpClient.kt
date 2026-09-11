package com.maisha.game.data.remote

import com.maisha.game.data.model.TriviaQuestion
import com.maisha.game.util.HtmlEntities
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

class TriviaHttpClient(
    private val gateway: JsonHttpGateway,
    private val baseUrl: HttpUrl = DEFAULT_BASE_URL
) : TriviaApi {

    override suspend fun fetchQuestions(amount: Int): List<TriviaQuestion> {
        val url = baseUrl.newBuilder()
            .setQueryParameter("amount", amount.coerceIn(1, 50).toString())
            .setQueryParameter("type", "multiple")
            .build()
        val dto = gateway.get(url, OpenTriviaResponse.serializer())
        if (dto.responseCode != 0) {
            throw RemoteApiException.InvalidResponse("response_code ${dto.responseCode}")
        }
        val questions = dto.results.mapNotNull { it.toDomain() }
        if (questions.isEmpty()) {
            throw RemoteApiException.InvalidResponse("no questions")
        }
        return questions
    }

    companion object {
        val DEFAULT_BASE_URL: HttpUrl = "https://opentdb.com/api.php".toHttpUrl()
    }
}

@Serializable
internal data class OpenTriviaResponse(
    @SerialName("response_code") val responseCode: Int = 0,
    val results: List<OpenTriviaResult> = emptyList()
)

@Serializable
internal data class OpenTriviaResult(
    val category: String = "",
    val type: String = "",
    val difficulty: String = "",
    val question: String = "",
    @SerialName("correct_answer") val correctAnswer: String = "",
    @SerialName("incorrect_answers") val incorrectAnswers: List<String> = emptyList()
)

private fun OpenTriviaResult.toDomain(): TriviaQuestion? {
    val prompt = HtmlEntities.decode(question).trim()
    val correct = HtmlEntities.decode(correctAnswer).trim()
    if (prompt.isEmpty() || correct.isEmpty()) return null
    return TriviaQuestion(
        prompt = prompt,
        correctAnswer = correct,
        incorrectAnswers = incorrectAnswers.map { HtmlEntities.decode(it).trim() }.filter { it.isNotEmpty() },
        category = HtmlEntities.decode(category).trim(),
        difficulty = difficulty.trim()
    )
}
