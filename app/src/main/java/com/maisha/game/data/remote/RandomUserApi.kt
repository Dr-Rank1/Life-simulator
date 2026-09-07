package com.maisha.game.data.remote

import com.maisha.game.data.model.Gender
import com.maisha.game.data.model.GeneratedName
import java.util.Locale

/**
 * Random User Generator ([https://randomuser.me/](https://randomuser.me/)).
 *
 * Supported `nat` values are a subset of ISO codes; see [RandomUserNationality].
 */
interface RandomUserApi {
    suspend fun fetchRandomUser(gender: Gender? = null, nationality: String? = null): GeneratedName
}

/**
 * Nationalities the public API accepts. Roster countries not in this set
 * (Kenya, Nigeria, Japan, …) keep using [com.maisha.game.data.NamePool].
 */
object RandomUserNationality {
    val supported: Set<String> = setOf(
        "AU", "BR", "CA", "CH", "DE", "DK", "ES", "FI", "FR", "GB",
        "IE", "IN", "IR", "MX", "NL", "NO", "NZ", "RS", "TR", "UA", "US"
    )

    fun fromCountryCode(countryCode: String?): String? {
        val nat = countryCode?.trim()?.uppercase(Locale.US).orEmpty()
        if (nat.isEmpty()) return null
        return nat.takeIf { it in supported }
    }
}
