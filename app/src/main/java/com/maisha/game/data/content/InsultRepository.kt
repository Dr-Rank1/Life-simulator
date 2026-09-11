package com.maisha.game.data.content

import com.maisha.game.data.local.GameDatabaseAccess
import com.maisha.game.data.remote.InsultApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsultRepository(
    private val api: InsultApi,
    database: GameDatabaseAccess,
    fallback: List<String>
) : InsultCatalog {

    @Inject
    constructor(
        api: InsultApi,
        database: GameDatabaseAccess,
        assets: ContentAssetStore
    ) : this(
        api,
        database,
        assets.loadStrings(ASSET).ifEmpty { FallbackInsults.lines }
    )

    private val cache = StringListCache(KIND, database, fallback)

    override fun nextInsult(): String = cache.next().ifBlank { FallbackInsults.lines.random() }

    suspend fun hydrateFromDisk() = cache.hydrateFromDisk()

    suspend fun refresh() {
        val fetched = LinkedHashSet<String>()
        repeat(BATCH) {
            runCatching { api.fetchInsult() }.getOrNull()?.let { fetched += it }
        }
        if (fetched.isNotEmpty()) cache.replace(fetched.toList())
    }

    companion object {
        const val KIND = "insults"
        const val ASSET = "data/content/insults.json"
        const val BATCH = 8
    }
}
