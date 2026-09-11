package com.maisha.game.data.content

import com.maisha.game.data.local.GameDatabaseAccess
import com.maisha.game.data.remote.BookApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository(
    private val api: BookApi,
    database: GameDatabaseAccess,
    fallback: List<String>
) : BookCatalog {

    @Inject
    constructor(
        api: BookApi,
        database: GameDatabaseAccess,
        assets: ContentAssetStore
    ) : this(
        api,
        database,
        assets.loadStrings(ASSET).ifEmpty { FallbackBooks.titles }
    )

    private val cache = StringListCache(KIND, database, fallback)

    override fun nextBookTitle(): String = cache.next().ifBlank { FallbackBooks.titles.random() }

    suspend fun hydrateFromDisk() = cache.hydrateFromDisk()

    suspend fun refresh() {
        val fetched = runCatching { api.fetchBooks() }.getOrNull().orEmpty()
        if (fetched.isNotEmpty()) cache.replace(fetched)
    }

    companion object {
        const val KIND = "books"
        const val ASSET = "data/content/books.json"
    }
}
