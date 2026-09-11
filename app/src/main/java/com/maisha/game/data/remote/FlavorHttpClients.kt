package com.maisha.game.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

class CarMakeHttpClient(
    private val gateway: JsonHttpGateway,
    private val baseUrl: HttpUrl = DEFAULT_BASE_URL
) : CarMakeApi {

    override suspend fun fetchMakes(): List<String> {
        val dto = gateway.get(baseUrl, NhtsaMakesResponse.serializer())
        return dto.results.map { it.makeName.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(MAX_MAKES)
    }

    companion object {
        const val MAX_MAKES = 250
        val DEFAULT_BASE_URL: HttpUrl =
            "https://vpic.nhtsa.dot.gov/api/vehicles/getallmakes?format=json".toHttpUrl()
    }
}

class InsultHttpClient(
    private val gateway: JsonHttpGateway,
    private val baseUrl: HttpUrl = DEFAULT_BASE_URL
) : InsultApi {

    override suspend fun fetchInsult(): String {
        val dto = gateway.get(baseUrl, EvilInsultResponse.serializer())
        val text = dto.insult.trim().replace(Regex("\\s+"), " ")
        if (text.isEmpty()) throw RemoteApiException.InvalidResponse("empty insult")
        return text.take(MAX_LEN)
    }

    companion object {
        const val MAX_LEN = 160
        val DEFAULT_BASE_URL: HttpUrl =
            "https://evilinsult.com/generate_insult.php?lang=en&type=json".toHttpUrl()
    }
}

class BookHttpClient(
    private val gateway: JsonHttpGateway,
    private val baseUrl: HttpUrl = DEFAULT_BASE_URL
) : BookApi {

    override suspend fun fetchBooks(): List<String> {
        val dto = gateway.get(baseUrl, GutendexResponse.serializer())
        val titles = dto.results.mapNotNull { entry ->
            val title = entry.title.trim()
            if (title.isEmpty()) return@mapNotNull null
            val author = entry.authors.firstOrNull()?.name?.trim().orEmpty()
            if (author.isEmpty()) title else "$title by $author"
        }.distinct()
        if (titles.isEmpty()) throw RemoteApiException.InvalidResponse("no books")
        return titles
    }

    companion object {
        val DEFAULT_BASE_URL: HttpUrl =
            "https://gutendex.com/books/?languages=en".toHttpUrl()
    }
}

class QuoteHttpClient(
    private val gateway: JsonHttpGateway,
    private val baseUrl: HttpUrl = DEFAULT_BASE_URL
) : QuoteApi {

    override suspend fun fetchQuotes(): List<String> {
        val dto = gateway.get(baseUrl, ZenQuoteListSerializer)
        val quotes = dto.mapNotNull { entry ->
            val q = entry.quote.trim()
            if (q.isEmpty()) return@mapNotNull null
            val author = entry.author.trim()
            if (author.isEmpty()) q else "$q — $author"
        }.distinct()
        if (quotes.isEmpty()) throw RemoteApiException.InvalidResponse("no quotes")
        return quotes
    }

    companion object {
        /** Batch endpoint for the same service as `/api/random`. */
        val DEFAULT_BASE_URL: HttpUrl = "https://zenquotes.io/api/quotes".toHttpUrl()
    }
}

@Serializable
internal data class NhtsaMakesResponse(
    @SerialName("Results") val results: List<NhtsaMakeDto> = emptyList()
)

@Serializable
internal data class NhtsaMakeDto(
    @SerialName("Make_ID") val makeId: Int = 0,
    @SerialName("Make_Name") val makeName: String = ""
)

@Serializable
internal data class EvilInsultResponse(
    val insult: String = ""
)

@Serializable
internal data class GutendexResponse(
    val results: List<GutendexBookDto> = emptyList()
)

@Serializable
internal data class GutendexBookDto(
    val title: String = "",
    val authors: List<GutendexAuthorDto> = emptyList()
)

@Serializable
internal data class GutendexAuthorDto(
    val name: String = ""
)

@Serializable
internal data class ZenQuoteDto(
    @SerialName("q") val quote: String = "",
    @SerialName("a") val author: String = ""
)

private val ZenQuoteListSerializer =
    kotlinx.serialization.builtins.ListSerializer(ZenQuoteDto.serializer())
