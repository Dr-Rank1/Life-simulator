package com.maisha.game.data.content

import com.maisha.game.data.local.GameDatabaseAccess
import com.maisha.game.data.local.ProceduralCacheEntity
import com.maisha.game.data.model.TriviaQuestion
import com.maisha.game.data.remote.JsonHttpGateway
import com.maisha.game.data.remote.TriviaApi
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first trivia. [nextQuestion] is lock-step and never hits the network.
 */
@Singleton
class TriviaRepository @Inject constructor(
    private val api: TriviaApi,
    private val database: GameDatabaseAccess
) : TriviaCatalog {

    @Volatile
    private var cache: List<TriviaQuestion> = FallbackTriviaCatalog.questions

    private val cursor = AtomicInteger(0)

    override fun nextQuestion(): TriviaQuestion {
        val items = cache
        if (items.isEmpty()) return FallbackTriviaCatalog.nextQuestion()
        val index = cursor.getAndIncrement().floorMod(items.size)
        return items[index]
    }

    suspend fun hydrateFromDisk() {
        val stored = loadFromRoom()
        if (stored.isNotEmpty()) cache = stored
    }

    suspend fun refresh() {
        val fetched = runCatching { api.fetchQuestions(TriviaApi.DEFAULT_BATCH) }.getOrNull().orEmpty()
        if (fetched.isEmpty()) return
        persist(fetched)
        cache = fetched
    }

    private suspend fun loadFromRoom(): List<TriviaQuestion> {
        val dao = database.proceduralCacheDao ?: return emptyList()
        return dao.getAll(KIND).mapNotNull { entity ->
            runCatching {
                JsonHttpGateway.NETWORK_JSON.decodeFromString(TriviaQuestion.serializer(), entity.payloadJson)
            }.getOrNull()?.takeIf { it.prompt.isNotBlank() }
        }
    }

    private suspend fun persist(items: List<TriviaQuestion>) {
        val dao = database.proceduralCacheDao ?: return
        val now = System.currentTimeMillis()
        dao.replaceKind(
            KIND,
            items.map { question ->
                ProceduralCacheEntity(
                    kind = KIND,
                    id = question.prompt.hashCode().toString(),
                    payloadJson = JsonHttpGateway.NETWORK_JSON.encodeToString(
                        TriviaQuestion.serializer(),
                        question
                    ),
                    fetchedAt = now
                )
            }
        )
    }

    private fun Int.floorMod(size: Int): Int {
        if (size <= 0) return 0
        val r = this % size
        return if (r < 0) r + size else r
    }

    companion object {
        const val KIND = "trivia"
    }
}
