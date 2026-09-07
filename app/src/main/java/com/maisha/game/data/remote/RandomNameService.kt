package com.maisha.game.data.remote

import com.maisha.game.data.NamePool
import com.maisha.game.data.model.Gender
import com.maisha.game.data.model.GeneratedName
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlin.random.Random

/**
 * Fetches a random name from Random User Generator, then falls back to [NamePool]
 * when the device is offline, the request fails, or the country is not an API `nat`.
 *
 * NPC generators stay on [NamePool] so age-up never waits on the network.
 */
@Singleton
class RandomNameService @Inject constructor(
    private val api: RandomUserApi
) {

    suspend fun fetchName(
        gender: Gender? = null,
        countryCode: String? = null
    ): GeneratedName {
        val nat = RandomUserNationality.fromCountryCode(countryCode)
        if (countryCode != null && nat == null) {
            return fallback(gender, countryCode)
        }
        return try {
            api.fetchRandomUser(gender, nat)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            fallback(gender, countryCode)
        }
    }

    private fun fallback(gender: Gender?, countryCode: String?): GeneratedName {
        val country = countryCode ?: "US"
        val resolvedGender = gender ?: if (Random.nextBoolean()) Gender.MALE else Gender.FEMALE
        val pool = NamePool.getNamePool(country)
        return GeneratedName(
            first = pool.randomFirstName(resolvedGender),
            last = pool.randomSurname(),
            gender = resolvedGender
        )
    }
}
