package com.maisha.game.data.remote

import com.maisha.game.data.NamePool
import com.maisha.game.data.model.Gender
import com.maisha.game.data.model.GeneratedName
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.concurrent.TimeUnit

class RandomUserHttpClientTest {

    @Test
    fun `request uses minimized query params and optional filters`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(successBody("Brad", "Gibson", "male"))
            val api = client(server)
            val name = api.fetchRandomUser(Gender.MALE, "US")
            assertEquals(GeneratedName("Brad", "Gibson", Gender.MALE), name)

            val recorded = server.takeRequest()
            assertEquals("GET", recorded.method)
            val url = recorded.requestUrl!!
            assertEquals(RandomUserHttpClient.INC, url.queryParameter("inc"))
            assertEquals(RandomUserHttpClient.RESULTS, url.queryParameter("results"))
            assertEquals("male", url.queryParameter("gender"))
            assertEquals("US", url.queryParameter("nat"))
            assertEquals(null, url.queryParameter("exc"))
        }
    }

    @Test
    fun `omits gender and nat when not requested`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(successBody("Jane", "Doe", "female"))
            client(server).fetchRandomUser()
            val url = server.takeRequest().requestUrl!!
            assertEquals(null, url.queryParameter("gender"))
            assertEquals(null, url.queryParameter("nat"))
            assertEquals("name,gender", url.queryParameter("inc"))
        }
    }

    @Test
    fun `maps female payload and ignores extra fields`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse().setBody(
                    """
                    {
                      "results": [{
                        "gender": "female",
                        "name": { "title": "Ms", "first": "marie", "last": "Dupont" },
                        "email": "hidden@example.com"
                      }],
                      "info": { "seed": "abc" }
                    }
                    """.trimIndent()
                )
            )
            val name = client(server).fetchRandomUser(Gender.FEMALE, "FR")
            assertEquals("Marie", name.first)
            assertEquals("Dupont", name.last)
            assertEquals(Gender.FEMALE, name.gender)
        }
    }

    @Test
    fun `non-200 responses wrap as HttpStatus`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(503))
            try {
                client(server).fetchRandomUser(Gender.MALE, "GB")
                fail("expected HttpStatus")
            } catch (e: RandomUserException.HttpStatus) {
                assertEquals(503, e.code)
            }
        }
    }

    @Test
    fun `empty results wrap as InvalidResponse`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"results":[]}"""))
            try {
                client(server).fetchRandomUser()
                fail("expected InvalidResponse")
            } catch (e: RandomUserException.InvalidResponse) {
                assertTrue(e.message!!.contains("no results"))
            }
        }
    }

    @Test
    fun `timeouts wrap as Timeout`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse()
                    .setBodyDelay(2, TimeUnit.SECONDS)
                    .setBody(successJson("Brad", "Gibson", "male"))
            )
            val slow = RandomUserHttpClient(
                httpClient = OkHttpClient.Builder()
                    .callTimeout(200, TimeUnit.MILLISECONDS)
                    .readTimeout(200, TimeUnit.MILLISECONDS)
                    .build(),
                baseUrl = server.url("/api/")
            )
            try {
                slow.fetchRandomUser(Gender.MALE, "US")
                fail("expected Timeout")
            } catch (e: RandomUserException.Timeout) {
                assertTrue(e.cause != null)
            }
        }
    }

    @Test
    fun `unreachable host wraps as Network`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
            try {
                client(server).fetchRandomUser()
                fail("expected Network")
            } catch (e: RandomUserException.Network) {
                assertTrue(e.cause != null)
            } catch (e: RandomUserException.Timeout) {
                // Some platforms surface a closed socket as a timeout.
                assertTrue(e.cause != null)
            }
        }
    }

    private fun client(server: MockWebServer) = RandomUserHttpClient(
        httpClient = OkHttpClient(),
        baseUrl = server.url("/api/")
    )

    private fun successBody(first: String, last: String, gender: String) =
        MockResponse().setBody(successJson(first, last, gender))

    private fun successJson(first: String, last: String, gender: String) =
        """{"results":[{"gender":"$gender","name":{"first":"$first","last":"$last"}}]}"""
}

class RandomNameServiceTest {

    @Test
    fun `uses API result when nationality is supported`() = runTest {
        val api = RecordingApi(GeneratedName("Brad", "Gibson", Gender.MALE))
        val service = RandomNameService(api)
        val name = service.fetchName(Gender.MALE, "US")
        assertEquals("Brad Gibson", name.fullName)
        assertEquals(Gender.MALE, api.lastGender)
        assertEquals("US", api.lastNationality)
        assertEquals(1, api.calls)
    }

    @Test
    fun `skips the network for countries the API does not support`() = runTest {
        val api = RecordingApi(error = IllegalStateException("should not be called"))
        val service = RandomNameService(api)
        val name = service.fetchName(Gender.FEMALE, "KE")
        assertEquals(0, api.calls)
        assertEquals(Gender.FEMALE, name.gender)
        assertTrue(name.first in NamePool.getNamePool("KE").femaleFirstNames)
        assertTrue(name.last in NamePool.getNamePool("KE").surnames)
    }

    @Test
    fun `falls back to NamePool on HTTP failure`() = runTest {
        val api = RecordingApi(error = RandomUserException.HttpStatus(500))
        val service = RandomNameService(api)
        val name = service.fetchName(Gender.MALE, "GB")
        assertEquals(1, api.calls)
        assertEquals(Gender.MALE, name.gender)
        assertTrue(name.first in NamePool.getNamePool("GB").maleFirstNames)
        assertTrue(name.last in NamePool.getNamePool("GB").surnames)
        assertFalse(name.first.isBlank())
    }

    @Test
    fun `falls back on timeout`() = runTest {
        val api = RecordingApi(error = RandomUserException.Timeout(RuntimeException("timeout")))
        val name = RandomNameService(api).fetchName(Gender.FEMALE, "FR")
        assertEquals(Gender.FEMALE, name.gender)
        assertTrue(name.first in NamePool.getNamePool("FR").femaleFirstNames)
    }

    @Test
    fun `nationality mapping covers roster countries the API supports`() {
        assertEquals("US", RandomUserNationality.fromCountryCode("us"))
        assertEquals("GB", RandomUserNationality.fromCountryCode("GB"))
        assertEquals("BR", RandomUserNationality.fromCountryCode("BR"))
        assertEquals("MX", RandomUserNationality.fromCountryCode("MX"))
        assertEquals("IN", RandomUserNationality.fromCountryCode("IN"))
        assertEquals(null, RandomUserNationality.fromCountryCode("KE"))
        assertEquals(null, RandomUserNationality.fromCountryCode("JP"))
        assertEquals(null, RandomUserNationality.fromCountryCode("ZZ"))
        assertEquals(null, RandomUserNationality.fromCountryCode(null))
    }

    private class RecordingApi(
        private val result: GeneratedName? = null,
        private val error: Exception? = null
    ) : RandomUserApi {
        var calls = 0
        var lastGender: Gender? = null
        var lastNationality: String? = null

        override suspend fun fetchRandomUser(gender: Gender?, nationality: String?): GeneratedName {
            calls++
            lastGender = gender
            lastNationality = nationality
            error?.let { throw it }
            return result ?: error("no result")
        }
    }
}
