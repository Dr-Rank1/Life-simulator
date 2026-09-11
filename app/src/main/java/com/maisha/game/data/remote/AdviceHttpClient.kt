package com.maisha.game.data.remote

import com.maisha.game.data.model.AdviceQuote
import kotlinx.serialization.Serializable
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

class AdviceHttpClient(
    private val gateway: JsonHttpGateway,
    private val baseUrl: HttpUrl = DEFAULT_BASE_URL
) : AdviceApi {

    override suspend fun fetchRandomAdvice(): AdviceQuote {
        val url = baseUrl.newBuilder()
            .addQueryParameter("t", System.currentTimeMillis().toString())
            .build()
        val dto = gateway.get(url, AdviceSlipResponse.serializer())
        val text = dto.slip?.advice?.trim().orEmpty()
        if (text.isEmpty()) {
            throw RemoteApiException.InvalidResponse("missing advice")
        }
        val id = dto.slip?.id?.toString()?.takeIf { it.isNotBlank() } ?: text.hashCode().toString()
        return AdviceQuote(id = id, text = text)
    }

    companion object {
        val DEFAULT_BASE_URL: HttpUrl = "https://api.adviceslip.com/advice".toHttpUrl()
    }
}

@Serializable
internal data class AdviceSlipResponse(
    val slip: AdviceSlipDto? = null
)

@Serializable
internal data class AdviceSlipDto(
    val id: Int = 0,
    val advice: String = ""
)
