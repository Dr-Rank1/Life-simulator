package com.maisha.game.data.content

import com.maisha.game.data.local.GameDatabaseAccess
import com.maisha.game.data.local.ProceduralCacheEntity
import com.maisha.game.data.remote.JsonHttpGateway
import kotlinx.serialization.builtins.serializer
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs

/**
 * In-memory + Room string list. Gameplay calls [next] / [forKey]; network never runs here.
 */
class StringListCache(
    private val kind: String,
    private val database: GameDatabaseAccess,
    fallback: List<String>
) {
    @Volatile
    private var cache: List<String> = fallback

    private val cursor = AtomicInteger(0)
    private val json = JsonHttpGateway.NETWORK_JSON

    fun next(): String {
        val items = cache
        if (items.isEmpty()) return ""
        val index = cursor.getAndIncrement().floorMod(items.size)
        return items[index]
    }

    fun forKey(key: String): String {
        val items = cache
        if (items.isEmpty()) return ""
        return items[abs(key.hashCode()) % items.size]
    }

    fun snapshot(): List<String> = cache

    suspend fun hydrateFromDisk() {
        val stored = loadFromRoom()
        if (stored.isNotEmpty()) cache = stored
    }

    suspend fun replace(items: List<String>) {
        if (items.isEmpty()) return
        persist(items)
        cache = items
    }

    private suspend fun loadFromRoom(): List<String> {
        val dao = database.proceduralCacheDao ?: return emptyList()
        return dao.getAll(kind).mapNotNull { entity ->
            runCatching {
                json.decodeFromString(String.serializer(), entity.payloadJson)
            }.getOrNull()?.trim()?.takeIf { it.isNotEmpty() }
        }
    }

    private suspend fun persist(items: List<String>) {
        val dao = database.proceduralCacheDao ?: return
        val now = System.currentTimeMillis()
        dao.replaceKind(
            kind,
            items.mapIndexed { index, value ->
                ProceduralCacheEntity(
                    kind = kind,
                    id = "${kind}_$index",
                    payloadJson = json.encodeToString(String.serializer(), value),
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
}
