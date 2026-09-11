package com.maisha.game.data.remote

import com.maisha.game.data.model.AdviceQuote

interface AdviceApi {
    suspend fun fetchRandomAdvice(): AdviceQuote
}

interface TriviaApi {
    suspend fun fetchQuestions(amount: Int = DEFAULT_BATCH): List<com.maisha.game.data.model.TriviaQuestion>

    companion object {
        const val DEFAULT_BATCH = 20
    }
}

interface CarMakeApi {
    suspend fun fetchMakes(): List<String>
}

interface InsultApi {
    suspend fun fetchInsult(): String
}

interface BookApi {
    suspend fun fetchBooks(): List<String>
}

interface QuoteApi {
    suspend fun fetchQuotes(): List<String>
}

interface StreetApi {
    suspend fun fetchStreets(city: String): List<String>
}

interface ArtApi {
    suspend fun fetchArtworks(): List<String>
}

interface WantedApi {
    suspend fun fetchBulletins(): List<String>
}

interface SpaceNewsApi {
    suspend fun fetchHeadlines(): List<String>
}
