package com.maisha.game.data.remote

import com.maisha.game.data.model.Gender
import com.maisha.game.data.model.GeneratedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.util.Locale
import kotlin.coroutines.coroutineContext

/**
 * OkHttp client for `https://randomuser.me/api/` with a minimized `inc=name,gender` payload.
 */
class RandomUserHttpClient(
    private val httpClient: OkHttpClient,
    private val baseUrl: HttpUrl = DEFAULT_BASE_URL,
    private val json: Json = NETWORK_JSON
) : RandomUserApi {

    override suspend fun fetchRandomUser(
        gender: Gender?,
        nationality: String?
    ): GeneratedName = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(buildUrl(gender, nationality))
            .get()
            .header("Accept", "application/json")
            .build()
        val call = httpClient.newCall(request)
        coroutineContext[Job]?.invokeOnCompletion { cause ->
            if (cause != null) call.cancel()
        }
        try {
            call.execute().use { response ->
                coroutineContext.ensureActive()
                if (!response.isSuccessful) {
                    throw RandomUserException.HttpStatus(response.code)
                }
                val body = response.body?.string().orEmpty()
                parseBody(body, gender)
            }
        } catch (e: RandomUserException) {
            throw e
        } catch (e: SocketTimeoutException) {
            coroutineContext.ensureActive()
            throw RandomUserException.Timeout(e)
        } catch (e: InterruptedIOException) {
            coroutineContext.ensureActive()
            throw RandomUserException.Timeout(e)
        } catch (e: IOException) {
            coroutineContext.ensureActive()
            throw RandomUserException.Network(e)
        }
    }

    private fun buildUrl(gender: Gender?, nationality: String?): HttpUrl {
        val builder = baseUrl.newBuilder()
            .setQueryParameter("inc", INC)
            .setQueryParameter("results", RESULTS)
        if (gender != null) {
            builder.setQueryParameter("gender", gender.name.lowercase(Locale.US))
        }
        val nat = nationality?.trim()?.uppercase(Locale.US)
        if (!nat.isNullOrEmpty()) {
            builder.setQueryParameter("nat", nat)
        }
        return builder.build()
    }

    private fun parseBody(body: String, requestedGender: Gender?): GeneratedName {
        if (body.isBlank()) {
            throw RandomUserException.InvalidResponse("empty body")
        }
        val parsed = try {
            json.decodeFromString(RandomUserResponse.serializer(), body)
        } catch (e: SerializationException) {
            throw RandomUserException.InvalidResponse("parse", e)
        } catch (e: IllegalArgumentException) {
            throw RandomUserException.InvalidResponse("parse", e)
        }
        val result = parsed.results.firstOrNull()
            ?: throw RandomUserException.InvalidResponse("no results")
        val first = result.name?.first.asNamePart()
            ?: throw RandomUserException.InvalidResponse("missing first name")
        val last = result.name?.last.asNamePart()
            ?: throw RandomUserException.InvalidResponse("missing last name")
        val gender = parseGender(result.gender) ?: requestedGender
            ?: throw RandomUserException.InvalidResponse("missing gender")
        return GeneratedName(first = first, last = last, gender = gender)
    }

    companion object {
        const val INC = "name,gender"
        const val RESULTS = "1"
        val DEFAULT_BASE_URL: HttpUrl = "https://randomuser.me/api/".toHttpUrl()
        val NETWORK_JSON: Json = Json { ignoreUnknownKeys = true }

        private fun parseGender(raw: String?): Gender? = when (raw?.trim()?.lowercase(Locale.US)) {
            "male" -> Gender.MALE
            "female" -> Gender.FEMALE
            else -> null
        }

        private fun String?.asNamePart(): String? {
            val trimmed = this?.trim().orEmpty()
            if (trimmed.isEmpty()) return null
            return trimmed.replaceFirstChar { ch ->
                if (ch.isLowerCase()) ch.titlecase(Locale.US) else ch.toString()
            }
        }
    }
}

@Serializable
internal data class RandomUserResponse(
    val results: List<RandomUserResult> = emptyList()
)

@Serializable
internal data class RandomUserResult(
    val gender: String? = null,
    val name: RandomUserName? = null
)

@Serializable
internal data class RandomUserName(
    val first: String? = null,
    val last: String? = null
)
