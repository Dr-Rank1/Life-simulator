package com.maisha.game.data.content

import com.maisha.game.data.local.GameDatabaseAccess
import com.maisha.game.data.remote.WantedApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WantedRepository(
    private val api: WantedApi,
    database: GameDatabaseAccess,
    fallback: List<String>
) : WantedCatalog {

    @Inject
    constructor(
        api: WantedApi,
        database: GameDatabaseAccess,
        assets: ContentAssetStore
    ) : this(
        api,
        database,
        assets.loadStrings(ASSET).ifEmpty { FallbackWanted.lines }
    )

    private val cache = StringListCache(KIND, database, fallback)

    override fun nextBulletin(): String = cache.next().ifBlank { FallbackWanted.lines.random() }

    suspend fun hydrateFromDisk() = cache.hydrateFromDisk()

    suspend fun refresh() {
        val fetched = runCatching { api.fetchBulletins() }.getOrNull().orEmpty()
        if (fetched.isNotEmpty()) cache.replace(fetched)
    }

    companion object {
        const val KIND = "wanted"
        const val ASSET = "data/content/wanted.json"
    }
}
