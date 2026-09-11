package com.maisha.game.data.content

import com.maisha.game.data.local.GameDatabaseAccess
import com.maisha.game.data.remote.CarMakeApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarMakeRepository(
    private val api: CarMakeApi,
    database: GameDatabaseAccess,
    fallback: List<String>
) : CarMakeCatalog {

    @Inject
    constructor(
        api: CarMakeApi,
        database: GameDatabaseAccess,
        assets: ContentAssetStore
    ) : this(
        api,
        database,
        assets.loadStrings(ASSET).ifEmpty { FallbackCarMakes.names }
    )

    private val cache = StringListCache(KIND, database, fallback)

    override fun makeFor(assetId: String): String =
        cache.forKey(assetId).ifBlank { FallbackCarMakes.names.first() }

    override fun randomMake(): String = cache.next().ifBlank { FallbackCarMakes.names.random() }

    suspend fun hydrateFromDisk() = cache.hydrateFromDisk()

    suspend fun refresh() {
        val fetched = runCatching { api.fetchMakes() }.getOrNull().orEmpty()
        if (fetched.isNotEmpty()) cache.replace(fetched)
    }

    companion object {
        const val KIND = "car_makes"
        const val ASSET = "data/content/car_makes.json"
    }
}
