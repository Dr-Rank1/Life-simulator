package com.maisha.game.data.content

import com.maisha.game.data.local.GameDatabaseAccess
import com.maisha.game.data.remote.SpaceNewsApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpaceNewsRepository(
    private val api: SpaceNewsApi,
    database: GameDatabaseAccess,
    fallback: List<String>
) : SpaceNewsCatalog {

    @Inject
    constructor(
        api: SpaceNewsApi,
        database: GameDatabaseAccess,
        assets: ContentAssetStore
    ) : this(
        api,
        database,
        assets.loadStrings(ASSET).ifEmpty { FallbackSpaceNews.lines }
    )

    private val cache = StringListCache(KIND, database, fallback)

    override fun nextHeadline(): String = cache.next().ifBlank { FallbackSpaceNews.lines.random() }

    suspend fun hydrateFromDisk() = cache.hydrateFromDisk()

    suspend fun refresh() {
        val fetched = runCatching { api.fetchHeadlines() }.getOrNull().orEmpty()
        if (fetched.isNotEmpty()) cache.replace(fetched)
    }

    companion object {
        const val KIND = "space_news"
        const val ASSET = "data/content/space_news.json"
    }
}
