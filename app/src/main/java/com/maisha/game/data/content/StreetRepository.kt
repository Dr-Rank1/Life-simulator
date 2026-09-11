package com.maisha.game.data.content

import com.maisha.game.data.local.GameDatabaseAccess
import com.maisha.game.data.remote.StreetApi
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class StreetRepository(
    private val api: StreetApi,
    private val database: GameDatabaseAccess,
    fallback: List<String>,
    private val requestGapMs: Long = REQUEST_GAP_MS
) : StreetCatalog {

    @Inject
    constructor(
        api: StreetApi,
        database: GameDatabaseAccess,
        assets: ContentAssetStore
    ) : this(
        api,
        database,
        assets.loadStrings(ASSET).ifEmpty { FallbackStreets.lines }
    )

    private val cache = StringListCache(KIND, database, fallback)

    override fun listingFor(countryCode: String, key: String): String {
        val code = countryCode.uppercase()
        val streets = cache.snapshot().mapNotNull { line ->
            val prefix = line.substringBefore("|", missingDelimiterValue = "")
            val rest = line.substringAfter("|", missingDelimiterValue = "")
            if (prefix == code && rest.isNotBlank()) rest else null
        }.ifEmpty { FallbackStreets.forCountry(code) }
        if (streets.isEmpty()) return FallbackStreets.forCountry("US").first()
        return streets[abs(key.hashCode()) % streets.size]
    }

    suspend fun hydrateFromDisk() = cache.hydrateFromDisk()

    suspend fun refresh() {
        val stored = database.proceduralCacheDao?.getAll(KIND).orEmpty()
        if (stored.size >= MIN_CACHED) return
        val merged = cache.snapshot().toMutableList()
        var fetchedAny = false
        for ((code, city) in CITIES) {
            if (fetchedAny && requestGapMs > 0L) delay(requestGapMs)
            val streets = runCatching { api.fetchStreets(city) }.getOrNull().orEmpty()
            if (streets.isEmpty()) continue
            fetchedAny = true
            merged.removeAll { it.startsWith("$code|") }
            merged += streets.map { "$code|$it" }
        }
        if (merged.isNotEmpty()) cache.replace(merged.distinct())
    }

    companion object {
        const val KIND = "streets"
        const val ASSET = "data/content/streets.json"
        const val MIN_CACHED = 20
        const val REQUEST_GAP_MS = 1_100L
        val CITIES: Map<String, String> = linkedMapOf(
            "KE" to "Nairobi",
            "NG" to "Lagos",
            "ZA" to "Johannesburg",
            "EG" to "Cairo",
            "US" to "New York",
            "CA" to "Toronto",
            "GB" to "London",
            "FR" to "Paris",
            "DE" to "Berlin",
            "IN" to "Mumbai",
            "JP" to "Tokyo",
            "PH" to "Manila",
            "ID" to "Jakarta",
            "BR" to "Sao Paulo",
            "MX" to "Mexico City"
        )
    }
}
