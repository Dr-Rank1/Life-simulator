package com.maisha.game.data.content

import com.maisha.game.data.local.GameDatabaseAccess
import com.maisha.game.data.remote.QuoteApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRepository(
    private val api: QuoteApi,
    database: GameDatabaseAccess,
    fallback: List<String>
) : QuoteCatalog {

    @Inject
    constructor(
        api: QuoteApi,
        database: GameDatabaseAccess,
        assets: ContentAssetStore
    ) : this(
        api,
        database,
        assets.loadStrings(ASSET).ifEmpty { FallbackQuotes.lines }
    )

    private val cache = StringListCache(KIND, database, fallback)

    override fun nextQuote(): String = cache.next().ifBlank { FallbackQuotes.lines.random() }

    suspend fun hydrateFromDisk() = cache.hydrateFromDisk()

    suspend fun refresh() {
        val fetched = runCatching { api.fetchQuotes() }.getOrNull().orEmpty()
        if (fetched.isNotEmpty()) cache.replace(fetched)
    }

    companion object {
        const val KIND = "quotes"
        const val ASSET = "data/content/quotes.json"
    }
}
