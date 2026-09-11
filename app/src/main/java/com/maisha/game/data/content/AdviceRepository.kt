package com.maisha.game.data.content

import com.maisha.game.data.local.GameDatabaseAccess
import com.maisha.game.data.local.ProceduralCacheEntity
import com.maisha.game.data.model.AdviceQuote
import com.maisha.game.data.remote.AdviceApi
import com.maisha.game.data.remote.JsonHttpGateway
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first advice. [nextAdvice] is lock-step and never hits the network.
 */
@Singleton
class AdviceRepository @Inject constructor(
    private val api: AdviceApi,
    private val database: GameDatabaseAccess
) : AdviceCatalog {

    @Volatile
    private var cache: List<AdviceQuote> = FallbackAdviceCatalog.quotes.mapIndexed { index, text ->
        AdviceQuote(id = "fallback-$index", text = text)
    }

    private val cursor = AtomicInteger(0)

    override fun nextAdvice(): String {
        val items = cache
        if (items.isEmpty()) return FallbackAdviceCatalog.nextAdvice()
        val index = cursor.getAndIncrement().floorMod(items.size)
        return items[index].text
    }

    suspend fun hydrateFromDisk() {
        val stored = loadFromRoom()
        if (stored.isNotEmpty()) cache = stored
    }

    suspend fun refresh() {
        val fetched = LinkedHashMap<String, AdviceQuote>()
        repeat(REFRESH_COUNT) {
            runCatching { api.fetchRandomAdvice() }.getOrNull()?.let { quote ->
                fetched[quote.text] = quote
            }
        }
        if (fetched.isEmpty()) return
        val items = fetched.values.toList()
        persist(items)
        cache = items
    }

    private suspend fun loadFromRoom(): List<AdviceQuote> {
        val dao = database.proceduralCacheDao ?: return emptyList()
        return dao.getAll(KIND).mapNotNull { entity ->
            runCatching {
                JsonHttpGateway.NETWORK_JSON.decodeFromString(AdviceQuote.serializer(), entity.payloadJson)
            }.getOrNull()?.takeIf { it.text.isNotBlank() }
        }
    }

    private suspend fun persist(items: List<AdviceQuote>) {
        val dao = database.proceduralCacheDao ?: return
        val now = System.currentTimeMillis()
        dao.replaceKind(
            KIND,
            items.map { quote ->
                ProceduralCacheEntity(
                    kind = KIND,
                    id = quote.id,
                    payloadJson = JsonHttpGateway.NETWORK_JSON.encodeToString(
                        AdviceQuote.serializer(),
                        quote
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
        const val KIND = "advice"
        const val REFRESH_COUNT = 5
    }
}
