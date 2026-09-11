package com.maisha.game.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import kotlin.coroutines.coroutineContext

/**
 * Shared GET + JSON decode used by procedural-content APIs.
 * Callers never run this on the game loop — only from prefetch / refresh.
 */
class JsonHttpGateway(
    private val httpClient: OkHttpClient,
    private val json: Json = NETWORK_JSON
) {

    suspend fun <T> get(
        url: HttpUrl,
        deserializer: DeserializationStrategy<T>,
        extraHeaders: Map<String, String> = emptyMap()
    ): T =
        withContext(Dispatchers.IO) {
            val builder = Request.Builder()
                .url(url)
                .get()
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT)
            extraHeaders.forEach { (name, value) -> builder.header(name, value) }
            val request = builder.build()
            val call = httpClient.newCall(request)
            coroutineContext[Job]?.invokeOnCompletion { cause ->
                if (cause != null) call.cancel()
            }
            try {
                call.execute().use { response ->
                    coroutineContext.ensureActive()
                    if (!response.isSuccessful) {
                        throw RemoteApiException.HttpStatus(response.code)
                    }
                    val body = response.body?.string().orEmpty()
                    if (body.isBlank()) {
                        throw RemoteApiException.InvalidResponse("empty body")
                    }
                    try {
                        json.decodeFromString(deserializer, body)
                    } catch (e: SerializationException) {
                        throw RemoteApiException.InvalidResponse("parse", e)
                    } catch (e: IllegalArgumentException) {
                        throw RemoteApiException.InvalidResponse("parse", e)
                    }
                }
            } catch (e: RemoteApiException) {
                throw e
            } catch (e: SocketTimeoutException) {
                coroutineContext.ensureActive()
                throw RemoteApiException.Timeout(e)
            } catch (e: InterruptedIOException) {
                coroutineContext.ensureActive()
                throw RemoteApiException.Timeout(e)
            } catch (e: IOException) {
                coroutineContext.ensureActive()
                throw RemoteApiException.Network(e)
            }
        }

    companion object {
        val NETWORK_JSON: Json = Json { ignoreUnknownKeys = true }
        const val USER_AGENT = "Maisha/1.0 (https://github.com/stewiriffin/Life-simulator)"
    }
}
