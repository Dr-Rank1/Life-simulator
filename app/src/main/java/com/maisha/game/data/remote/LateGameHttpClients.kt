package com.maisha.game.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

class StreetHttpClient(
    private val gateway: JsonHttpGateway,
    private val baseUrl: HttpUrl = DEFAULT_BASE_URL
) : StreetApi {

    override suspend fun fetchStreets(city: String): List<String> {
        val url = baseUrl.newBuilder()
            .addQueryParameter("city", city)
            .addQueryParameter("format", "json")
            .addQueryParameter("addressdetails", "1")
            .addQueryParameter("limit", LIMIT.toString())
            .build()
        val places = gateway.get(url, ListSerializer(NominatimPlaceDto.serializer()))
        return places.mapNotNull { NominatimPlaceDto.toListing(it) }
            .distinct()
            .take(MAX_STREETS)
    }

    companion object {
        const val LIMIT = 20
        const val MAX_STREETS = 12
        val DEFAULT_BASE_URL: HttpUrl = "https://nominatim.openstreetmap.org/search".toHttpUrl()
    }
}

class ArtHttpClient(
    private val gateway: JsonHttpGateway,
    private val searchUrl: HttpUrl = DEFAULT_SEARCH_URL,
    private val objectBaseUrl: HttpUrl = DEFAULT_OBJECT_BASE
) : ArtApi {

    override suspend fun fetchArtworks(): List<String> {
        val search = gateway.get(searchUrl, MetSearchResponse.serializer())
        val ids = search.objectIds.filter { it > 0 }.distinct().take(OBJECT_BATCH)
        if (ids.isEmpty()) throw RemoteApiException.InvalidResponse("no art ids")
        val titles = LinkedHashSet<String>()
        for (id in ids) {
            val url = objectBaseUrl.newBuilder().addPathSegment(id.toString()).build()
            val dto = runCatching { gateway.get(url, MetObjectDto.serializer()) }.getOrNull()
                ?: continue
            MetObjectDto.toListing(dto)?.let { titles += it }
        }
        if (titles.isEmpty()) throw RemoteApiException.InvalidResponse("no artworks")
        return titles.toList()
    }

    companion object {
        const val OBJECT_BATCH = 8
        val DEFAULT_SEARCH_URL: HttpUrl =
            "https://collectionapi.metmuseum.org/public/collection/v1/search?q=painting&hasImages=true".toHttpUrl()
        val DEFAULT_OBJECT_BASE: HttpUrl =
            "https://collectionapi.metmuseum.org/public/collection/v1/objects".toHttpUrl()
    }
}

class WantedHttpClient(
    private val gateway: JsonHttpGateway,
    private val baseUrl: HttpUrl = DEFAULT_BASE_URL
) : WantedApi {

    override suspend fun fetchBulletins(): List<String> {
        val dto = gateway.get(baseUrl, FbiWantedResponse.serializer())
        val bulletins = dto.items.mapNotNull { FbiWantedSanitizer.sanitize(it) }
            .distinct()
            .take(MAX_ITEMS)
        if (bulletins.isEmpty()) throw RemoteApiException.InvalidResponse("no safe bulletins")
        return bulletins
    }

    companion object {
        const val MAX_ITEMS = 20
        val DEFAULT_BASE_URL: HttpUrl =
            "https://api.fbi.gov/wanted/v1/list?pageSize=20".toHttpUrl()
    }
}

class SpaceNewsHttpClient(
    private val gateway: JsonHttpGateway,
    private val baseUrl: HttpUrl = DEFAULT_BASE_URL
) : SpaceNewsApi {

    override suspend fun fetchHeadlines(): List<String> {
        val dto = gateway.get(baseUrl, SpaceflightListResponse.serializer())
        val headlines = dto.results.mapNotNull { entry ->
            val title = entry.title.trim().replace(Regex("\\s+"), " ")
            if (title.isEmpty()) return@mapNotNull null
            val site = entry.newsSite.trim()
            val line = if (site.isEmpty()) title else "$title — $site"
            line.take(MAX_LEN)
        }.distinct()
        if (headlines.isEmpty()) throw RemoteApiException.InvalidResponse("no headlines")
        return headlines
    }

    companion object {
        const val MAX_LEN = 160
        val DEFAULT_BASE_URL: HttpUrl =
            "https://api.spaceflightnewsapi.net/v4/articles/?limit=20".toHttpUrl()
    }
}

object FbiWantedSanitizer {
    private val blocked = listOf(
        "murder", "homicide", "terror", "sexual", "rape", "assault", "violent",
        "kidnap", "child", "exploitation", "weapon", "shooting", "bomb",
        "massacre", "torture", "armed", "homicide", "cyberstalk"
    )

    fun sanitize(item: FbiWantedItemDto): String? {
        val name = item.title.trim().replace(Regex("\\s+"), " ")
        if (name.isEmpty()) return null
        val subjects = item.subjects.map { it.trim() }.filter { it.isNotEmpty() }
        val blob = (subjects + item.description.orEmpty() + item.rewardText.orEmpty())
            .joinToString(" ")
            .lowercase()
        if (blocked.any { it in blob }) return null
        val mild = subjects.firstOrNull { subject ->
            val lower = subject.lowercase()
            blocked.none { it in lower }
        }?.take(40)
        val flavor = mild ?: "person of interest"
        return "$name · $flavor".take(120)
    }
}

@Serializable
internal data class NominatimPlaceDto(
    @SerialName("display_name") val displayName: String = "",
    val address: NominatimAddressDto? = null
) {
    companion object {
        fun toListing(place: NominatimPlaceDto): String? {
            val addr = place.address
            val road = listOf(addr?.road, addr?.pedestrian).firstOrNull { !it.isNullOrBlank() }?.trim()
            val area = listOf(
                addr?.neighbourhood,
                addr?.suburb,
                addr?.quarter,
                addr?.cityDistrict
            ).firstOrNull { !it.isNullOrBlank() }?.trim()
            val combined = when {
                !road.isNullOrBlank() && !area.isNullOrBlank() &&
                    !road.equals(area, ignoreCase = true) -> "$area · $road"
                !road.isNullOrBlank() -> road
                !area.isNullOrBlank() -> area
                else -> {
                    val parts = place.displayName.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    if (parts.size < 2) null else parts.take(2).joinToString(" · ")
                }
            }?.take(80)
            return combined?.takeIf { it.length >= 4 }
        }
    }
}

@Serializable
internal data class NominatimAddressDto(
    val road: String? = null,
    val pedestrian: String? = null,
    val neighbourhood: String? = null,
    val suburb: String? = null,
    val quarter: String? = null,
    @SerialName("city_district") val cityDistrict: String? = null
)

@Serializable
internal data class MetSearchResponse(
    val total: Int = 0,
    @SerialName("objectIDs") val objectIds: List<Int> = emptyList()
)

@Serializable
internal data class MetObjectDto(
    val title: String = "",
    val artistDisplayName: String = "",
    val objectName: String = ""
) {
    companion object {
        fun toListing(dto: MetObjectDto): String? {
            val title = dto.title.trim().ifBlank { dto.objectName.trim() }
            if (title.isEmpty()) return null
            val artist = dto.artistDisplayName.trim()
            val line = if (artist.isEmpty()) title else "$title by $artist"
            return line.take(120)
        }
    }
}

@Serializable
data class FbiWantedItemDto(
    val title: String = "",
    val description: String? = null,
    val subjects: List<String> = emptyList(),
    @SerialName("reward_text") val rewardText: String? = null
)

@Serializable
internal data class FbiWantedResponse(
    val items: List<FbiWantedItemDto> = emptyList()
)

@Serializable
internal data class SpaceflightListResponse(
    val results: List<SpaceflightArticleDto> = emptyList()
)

@Serializable
internal data class SpaceflightArticleDto(
    val title: String = "",
    @SerialName("news_site") val newsSite: String = ""
)
