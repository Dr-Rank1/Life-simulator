package com.maisha.game.data.content

import com.maisha.game.data.local.GameDatabaseAccess
import com.maisha.game.data.remote.ArtApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtRepository(
    private val api: ArtApi,
    database: GameDatabaseAccess,
    fallback: List<String>
) : ArtCatalog {

    @Inject
    constructor(
        api: ArtApi,
        database: GameDatabaseAccess,
        assets: ContentAssetStore
    ) : this(
        api,
        database,
        assets.loadStrings(ASSET).ifEmpty { FallbackArtworks.titles }
    )

    private val cache = StringListCache(KIND, database, fallback)

    override fun nextArtwork(): String = cache.next().ifBlank { FallbackArtworks.titles.random() }

    suspend fun hydrateFromDisk() = cache.hydrateFromDisk()

    suspend fun refresh() {
        val fetched = runCatching { api.fetchArtworks() }.getOrNull().orEmpty()
        if (fetched.isNotEmpty()) cache.replace(fetched)
    }

    companion object {
        const val KIND = "artworks"
        const val ASSET = "data/content/artworks.json"
    }
}
