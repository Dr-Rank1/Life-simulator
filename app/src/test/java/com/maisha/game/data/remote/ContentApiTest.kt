package com.maisha.game.data.remote

import com.maisha.game.data.content.AdviceRepository
import com.maisha.game.data.content.BookRepository
import com.maisha.game.data.content.CarMakeRepository
import com.maisha.game.data.content.FallbackAdviceCatalog
import com.maisha.game.data.content.FallbackBooks
import com.maisha.game.data.content.FallbackCarMakes
import com.maisha.game.data.content.FallbackInsults
import com.maisha.game.data.content.FallbackQuotes
import com.maisha.game.data.content.FallbackTriviaCatalog
import com.maisha.game.data.content.InsultRepository
import com.maisha.game.data.content.QuoteRepository
import com.maisha.game.data.content.TriviaRepository
import com.maisha.game.data.content.ArtRepository
import com.maisha.game.data.content.FallbackArtworks
import com.maisha.game.data.content.FallbackSpaceNews
import com.maisha.game.data.content.FallbackStreets
import com.maisha.game.data.content.FallbackWanted
import com.maisha.game.data.content.SpaceNewsRepository
import com.maisha.game.data.content.StreetRepository
import com.maisha.game.data.content.WantedRepository
import com.maisha.game.data.local.GameDatabaseAccess
import com.maisha.game.data.local.ProceduralCacheDao
import com.maisha.game.data.local.ProceduralCacheEntity
import com.maisha.game.data.model.AdviceQuote
import com.maisha.game.data.model.TriviaQuestion
import com.maisha.game.util.HtmlEntities
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlEntitiesTest {
    @Test
    fun `decodes trivia HTML entities`() {
        assertEquals(
            "Who wrote \"Romeo's\" play?",
            HtmlEntities.decode("Who wrote &quot;Romeo&#039;s&quot; play?")
        )
        assertEquals("Tom & Jerry", HtmlEntities.decode("Tom &amp; Jerry"))
    }
}

class AdviceHttpClientTest {
    @Test
    fun `parses slip id and advice`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse().setBody("""{"slip":{"id":42,"advice":"Don't panic."}}""")
            )
            val api = AdviceHttpClient(JsonHttpGateway(OkHttpClient()), server.url("/advice"))
            val quote = api.fetchRandomAdvice()
            assertEquals("42", quote.id)
            assertEquals("Don't panic.", quote.text)
            assertTrue(server.takeRequest().requestUrl?.queryParameterName(0) == "t")
        }
    }

    @Test
    fun `empty advice is InvalidResponse`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"slip":{"id":1,"advice":"  "}}"""))
            try {
                AdviceHttpClient(JsonHttpGateway(OkHttpClient()), server.url("/advice")).fetchRandomAdvice()
                org.junit.Assert.fail("expected InvalidResponse")
            } catch (_: RemoteApiException.InvalidResponse) {
            }
        }
    }
}

class TriviaHttpClientTest {
    @Test
    fun `parses a batch and decodes entities`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse().setBody(
                    """
                    {
                      "response_code": 0,
                      "results": [{
                        "category": "Science",
                        "type": "multiple",
                        "difficulty": "easy",
                        "question": "What is H&amp;2O?",
                        "correct_answer": "Water",
                        "incorrect_answers": ["Air", "Fire", "Earth"]
                      }]
                    }
                    """.trimIndent()
                )
            )
            val api = TriviaHttpClient(JsonHttpGateway(OkHttpClient()), server.url("/api.php"))
            val questions = api.fetchQuestions(10)
            val recorded = server.takeRequest().requestUrl!!
            assertEquals("10", recorded.queryParameter("amount"))
            assertEquals("multiple", recorded.queryParameter("type"))
            assertEquals("What is H&2O?", questions.single().prompt)
            assertEquals("Water", questions.single().correctAnswer)
        }
    }

    @Test
    fun `nonzero response_code is InvalidResponse`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"response_code":1,"results":[]}"""))
            try {
                TriviaHttpClient(JsonHttpGateway(OkHttpClient()), server.url("/api.php")).fetchQuestions(5)
                org.junit.Assert.fail("expected InvalidResponse")
            } catch (e: RemoteApiException.InvalidResponse) {
                assertTrue(e.message!!.contains("response_code"))
            }
        }
    }
}

class AdviceRepositoryTest {
    @Test
    fun `serves fallback when the API fails`() = runTest {
        val api = FakeAdviceApi(error = RemoteApiException.HttpStatus(500))
        val repo = AdviceRepository(api, MemoryCacheDb())
        repo.refresh()
        assertEquals(5, api.calls)
        assertTrue(repo.nextAdvice() in FallbackAdviceCatalog.quotes)
    }

    @Test
    fun `replaces memory with fetched slips`() = runTest {
        val api = FakeAdviceApi(quote = AdviceQuote("7", "Stay curious."))
        val repo = AdviceRepository(api, MemoryCacheDb())
        repo.refresh()
        assertEquals("Stay curious.", repo.nextAdvice())
    }
}

class TriviaRepositoryTest {
    @Test
    fun `keeps bundled questions when refresh fails`() = runTest {
        val api = FakeTriviaApi(error = RemoteApiException.Timeout(RuntimeException("t")))
        val repo = TriviaRepository(api, MemoryCacheDb())
        repo.refresh()
        assertTrue(repo.nextQuestion().prompt in FallbackTriviaCatalog.questions.map { it.prompt })
    }

    @Test
    fun `uses fetched questions after a successful refresh`() = runTest {
        val question = TriviaQuestion("What is 2+2?", "4", listOf("3", "5", "22"))
        val repo = TriviaRepository(FakeTriviaApi(questions = listOf(question)), MemoryCacheDb())
        repo.refresh()
        assertEquals("What is 2+2?", repo.nextQuestion().prompt)
    }
}

private class FakeAdviceApi(
    private val quote: AdviceQuote? = null,
    private val error: Exception? = null
) : AdviceApi {
    var calls = 0
    override suspend fun fetchRandomAdvice(): AdviceQuote {
        calls++
        error?.let { throw it }
        return quote ?: error("no quote")
    }
}

private class FakeTriviaApi(
    private val questions: List<TriviaQuestion> = emptyList(),
    private val error: Exception? = null
) : TriviaApi {
    override suspend fun fetchQuestions(amount: Int): List<TriviaQuestion> {
        error?.let { throw it }
        return questions
    }
}

private class MemoryCacheDb : GameDatabaseAccess {
    override val isAvailable: Boolean = true
    override val characterDao = null
    override val achievementDao = null
    override val proceduralCacheDao = MemoryProceduralCacheDao()
}

class CarMakeHttpClientTest {
    @Test
    fun `parses NHTSA make names and caps the list`() = runTest {
        MockWebServer().use { server ->
            val results = (1..260).joinToString(",") { i ->
                """{"Make_ID":$i,"Make_Name":"Make$i"}"""
            }
            server.enqueue(MockResponse().setBody("""{"Count":260,"Results":[$results]}"""))
            val api = CarMakeHttpClient(JsonHttpGateway(OkHttpClient()), server.url("/makes"))
            val makes = api.fetchMakes()
            assertEquals(CarMakeHttpClient.MAX_MAKES, makes.size)
            assertEquals("Make1", makes.first())
        }
    }
}

class InsultHttpClientTest {
    @Test
    fun `trims and truncates insult text`() = runTest {
        MockWebServer().use { server ->
            val long = "x".repeat(200)
            server.enqueue(MockResponse().setBody("""{"insult":"  $long  "}"""))
            val api = InsultHttpClient(JsonHttpGateway(OkHttpClient()), server.url("/insult"))
            val insult = api.fetchInsult()
            assertEquals(InsultHttpClient.MAX_LEN, insult.length)
        }
    }
}

class BookHttpClientTest {
    @Test
    fun `formats title with author`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse().setBody(
                    """
                    {
                      "results": [
                        {"title":"Pride and Prejudice","authors":[{"name":"Austen, Jane"}]},
                        {"title":"Untitled","authors":[]}
                      ]
                    }
                    """.trimIndent()
                )
            )
            val api = BookHttpClient(JsonHttpGateway(OkHttpClient()), server.url("/books"))
            val books = api.fetchBooks()
            assertEquals("Pride and Prejudice by Austen, Jane", books[0])
            assertEquals("Untitled", books[1])
        }
    }
}

class QuoteHttpClientTest {
    @Test
    fun `formats quote with author`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse().setBody("""[{"q":"Be still.","a":"Unknown"}]""")
            )
            val api = QuoteHttpClient(JsonHttpGateway(OkHttpClient()), server.url("/quotes"))
            assertEquals("Be still. — Unknown", api.fetchQuotes().single())
        }
    }
}

class CarMakeRepositoryTest {
    @Test
    fun `keeps fallback makes when the API fails`() = runTest {
        val repo = CarMakeRepository(
            FakeCarMakeApi(error = RemoteApiException.HttpStatus(500)),
            MemoryCacheDb(),
            FallbackCarMakes.names
        )
        repo.refresh()
        assertTrue(repo.randomMake() in FallbackCarMakes.names)
        assertTrue(repo.makeFor("car_new") in FallbackCarMakes.names)
    }

    @Test
    fun `uses fetched makes after a successful refresh`() = runTest {
        val repo = CarMakeRepository(
            FakeCarMakeApi(makes = listOf("Saab")),
            MemoryCacheDb(),
            FallbackCarMakes.names
        )
        repo.refresh()
        assertEquals("Saab", repo.randomMake())
        assertEquals("Saab", repo.makeFor("car_new"))
    }
}

class InsultRepositoryTest {
    @Test
    fun `keeps fallback insults when refresh fails`() = runTest {
        val repo = InsultRepository(
            FakeInsultApi(error = RemoteApiException.Timeout(RuntimeException("t"))),
            MemoryCacheDb(),
            FallbackInsults.lines
        )
        repo.refresh()
        assertTrue(repo.nextInsult() in FallbackInsults.lines)
    }

    @Test
    fun `uses fetched insults after a successful refresh`() = runTest {
        val repo = InsultRepository(
            FakeInsultApi(insult = "That was a choice."),
            MemoryCacheDb(),
            FallbackInsults.lines
        )
        repo.refresh()
        assertEquals("That was a choice.", repo.nextInsult())
    }
}

class BookRepositoryTest {
    @Test
    fun `keeps fallback titles when refresh fails`() = runTest {
        val repo = BookRepository(
            FakeBookApi(error = RemoteApiException.Network(RuntimeException("n"))),
            MemoryCacheDb(),
            FallbackBooks.titles
        )
        repo.refresh()
        assertTrue(repo.nextBookTitle() in FallbackBooks.titles)
    }

    @Test
    fun `uses fetched titles after a successful refresh`() = runTest {
        val repo = BookRepository(
            FakeBookApi(titles = listOf("Things Fall Apart by Chinua Achebe")),
            MemoryCacheDb(),
            FallbackBooks.titles
        )
        repo.refresh()
        assertEquals("Things Fall Apart by Chinua Achebe", repo.nextBookTitle())
    }
}

class QuoteRepositoryTest {
    @Test
    fun `keeps fallback quotes when refresh fails`() = runTest {
        val repo = QuoteRepository(
            FakeQuoteApi(error = RemoteApiException.HttpStatus(503)),
            MemoryCacheDb(),
            FallbackQuotes.lines
        )
        repo.refresh()
        assertTrue(repo.nextQuote() in FallbackQuotes.lines)
    }

    @Test
    fun `uses fetched quotes after a successful refresh`() = runTest {
        val repo = QuoteRepository(
            FakeQuoteApi(quotes = listOf("Quiet the mind.")),
            MemoryCacheDb(),
            FallbackQuotes.lines
        )
        repo.refresh()
        assertEquals("Quiet the mind.", repo.nextQuote())
    }
}

private class FakeCarMakeApi(
    private val makes: List<String> = emptyList(),
    private val error: Exception? = null
) : CarMakeApi {
    override suspend fun fetchMakes(): List<String> {
        error?.let { throw it }
        return makes
    }
}

private class FakeInsultApi(
    private val insult: String? = null,
    private val error: Exception? = null
) : InsultApi {
    override suspend fun fetchInsult(): String {
        error?.let { throw it }
        return insult ?: error("no insult")
    }
}

private class FakeBookApi(
    private val titles: List<String> = emptyList(),
    private val error: Exception? = null
) : BookApi {
    override suspend fun fetchBooks(): List<String> {
        error?.let { throw it }
        return titles
    }
}

private class FakeQuoteApi(
    private val quotes: List<String> = emptyList(),
    private val error: Exception? = null
) : QuoteApi {
    override suspend fun fetchQuotes(): List<String> {
        error?.let { throw it }
        return quotes
    }
}

class StreetHttpClientTest {
    @Test
    fun `formats suburb and road from Nominatim`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse().setBody(
                    """[{"display_name":"Kenyatta Avenue, Westlands, Nairobi",
                    "address":{"road":"Kenyatta Avenue","suburb":"Westlands"}}]"""
                )
            )
            val api = StreetHttpClient(JsonHttpGateway(OkHttpClient()), server.url("/search"))
            assertEquals("Westlands · Kenyatta Avenue", api.fetchStreets("Nairobi").single())
        }
    }
}

class ArtHttpClientTest {
    @Test
    fun `loads object titles after search ids`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"total":1,"objectIDs":[11]}"""))
            server.enqueue(
                MockResponse().setBody(
                    """{"title":"The Dance Class","artistDisplayName":"Edgar Degas"}"""
                )
            )
            val api = ArtHttpClient(
                JsonHttpGateway(OkHttpClient()),
                server.url("/search"),
                server.url("/objects/")
            )
            assertEquals("The Dance Class by Edgar Degas", api.fetchArtworks().single())
        }
    }
}

class FbiWantedSanitizerTest {
    @Test
    fun `drops violent subjects and missing names`() {
        assertEquals(
            null,
            FbiWantedSanitizer.sanitize(
                FbiWantedItemDto(title = "BAD ACTOR", subjects = listOf("Homicide"))
            )
        )
        assertEquals(
            null,
            FbiWantedSanitizer.sanitize(FbiWantedItemDto(title = "  ", subjects = listOf("Fraud")))
        )
        assertEquals(
            "Jordan Hale · White Collar Crime",
            FbiWantedSanitizer.sanitize(
                FbiWantedItemDto(title = "Jordan Hale", subjects = listOf("White Collar Crime"))
            )
        )
    }
}

class WantedHttpClientTest {
    @Test
    fun `keeps only sanitized bulletins`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse().setBody(
                    """
                    {"items":[
                      {"title":"Jordan Hale","subjects":["White Collar Crime"],"description":"alleged fraud"},
                      {"title":"Violent Case","subjects":["Homicide"],"description":"murder warrant"}
                    ]}
                    """.trimIndent()
                )
            )
            val api = WantedHttpClient(JsonHttpGateway(OkHttpClient()), server.url("/wanted"))
            assertEquals(listOf("Jordan Hale · White Collar Crime"), api.fetchBulletins())
        }
    }
}

class SpaceNewsHttpClientTest {
    @Test
    fun `formats title and news site`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse().setBody(
                    """{"results":[{"title":"New telescope online","news_site":"NASA"}]}"""
                )
            )
            val api = SpaceNewsHttpClient(JsonHttpGateway(OkHttpClient()), server.url("/articles"))
            assertEquals("New telescope online — NASA", api.fetchHeadlines().single())
        }
    }
}

class StreetRepositoryTest {
    @Test
    fun `keeps fallback streets when the API fails`() = runTest {
        val repo = StreetRepository(
            FakeStreetApi(error = RemoteApiException.HttpStatus(503)),
            MemoryCacheDb(),
            FallbackStreets.lines,
            requestGapMs = 0L
        )
        repo.refresh()
        assertTrue(repo.listingFor("KE", "house_suburban") in FallbackStreets.forCountry("KE"))
    }

    @Test
    fun `uses fetched streets after a successful refresh`() = runTest {
        val repo = StreetRepository(
            FakeStreetApi(streets = listOf("Hill Road")),
            MemoryCacheDb(),
            FallbackStreets.lines,
            requestGapMs = 0L
        )
        repo.refresh()
        assertEquals("Hill Road", repo.listingFor("KE", "house_suburban"))
    }
}

class ArtRepositoryTest {
    @Test
    fun `keeps fallback art when refresh fails`() = runTest {
        val repo = ArtRepository(
            FakeArtApi(error = RemoteApiException.Timeout(RuntimeException("t"))),
            MemoryCacheDb(),
            FallbackArtworks.titles
        )
        repo.refresh()
        assertTrue(repo.nextArtwork() in FallbackArtworks.titles)
    }

    @Test
    fun `uses fetched art after a successful refresh`() = runTest {
        val repo = ArtRepository(
            FakeArtApi(titles = listOf("Water Lilies by Claude Monet")),
            MemoryCacheDb(),
            FallbackArtworks.titles
        )
        repo.refresh()
        assertEquals("Water Lilies by Claude Monet", repo.nextArtwork())
    }
}

class WantedRepositoryTest {
    @Test
    fun `keeps fallback bulletins when refresh fails`() = runTest {
        val repo = WantedRepository(
            FakeWantedApi(error = RemoteApiException.Network(RuntimeException("n"))),
            MemoryCacheDb(),
            FallbackWanted.lines
        )
        repo.refresh()
        assertTrue(repo.nextBulletin() in FallbackWanted.lines)
    }
}

class SpaceNewsRepositoryTest {
    @Test
    fun `uses fetched headlines after a successful refresh`() = runTest {
        val repo = SpaceNewsRepository(
            FakeSpaceNewsApi(headlines = listOf("Docking complete — ESA")),
            MemoryCacheDb(),
            FallbackSpaceNews.lines
        )
        repo.refresh()
        assertEquals("Docking complete — ESA", repo.nextHeadline())
    }
}

private class FakeStreetApi(
    private val streets: List<String> = emptyList(),
    private val error: Exception? = null
) : StreetApi {
    override suspend fun fetchStreets(city: String): List<String> {
        error?.let { throw it }
        return streets
    }
}

private class FakeArtApi(
    private val titles: List<String> = emptyList(),
    private val error: Exception? = null
) : ArtApi {
    override suspend fun fetchArtworks(): List<String> {
        error?.let { throw it }
        return titles
    }
}

private class FakeWantedApi(
    private val bulletins: List<String> = emptyList(),
    private val error: Exception? = null
) : WantedApi {
    override suspend fun fetchBulletins(): List<String> {
        error?.let { throw it }
        return bulletins
    }
}

private class FakeSpaceNewsApi(
    private val headlines: List<String> = emptyList(),
    private val error: Exception? = null
) : SpaceNewsApi {
    override suspend fun fetchHeadlines(): List<String> {
        error?.let { throw it }
        return headlines
    }
}

private class MemoryProceduralCacheDao : ProceduralCacheDao {
    private val rows = mutableListOf<ProceduralCacheEntity>()

    override suspend fun getAll(kind: String): List<ProceduralCacheEntity> =
        rows.filter { it.kind == kind }

    override suspend fun upsertAll(entities: List<ProceduralCacheEntity>) {
        rows.removeAll { entity -> entities.any { it.kind == entity.kind && it.id == entity.id } }
        rows += entities
    }

    override suspend fun deleteKind(kind: String) {
        rows.removeAll { it.kind == kind }
    }
}
