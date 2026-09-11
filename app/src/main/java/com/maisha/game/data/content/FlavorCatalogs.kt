package com.maisha.game.data.content

interface CarMakeCatalog {
    fun makeFor(assetId: String): String
    fun randomMake(): String
}

interface InsultCatalog {
    fun nextInsult(): String
}

interface BookCatalog {
    fun nextBookTitle(): String
}

interface QuoteCatalog {
    fun nextQuote(): String
}

interface StreetCatalog {
    fun listingFor(countryCode: String, key: String): String
}

interface ArtCatalog {
    fun nextArtwork(): String
}

interface WantedCatalog {
    fun nextBulletin(): String
}

interface SpaceNewsCatalog {
    fun nextHeadline(): String
}

object FallbackCarMakes {
    val names: List<String> = listOf(
        "Toyota", "Honda", "Ford", "Chevrolet", "Nissan", "Hyundai", "Volkswagen", "BMW"
    )
}

object FallbackInsults {
    val lines: List<String> = listOf(
        "You call that an effort?",
        "I've seen better from a first-year intern.",
        "That was painfully average.",
        "Do you practice being this late, or is it a gift?"
    )
}

object FallbackBooks {
    val titles: List<String> = listOf(
        "Pride and Prejudice by Jane Austen",
        "Things Fall Apart by Chinua Achebe",
        "The Great Gatsby by F. Scott Fitzgerald",
        "To Kill a Mockingbird by Harper Lee"
    )
}

object FallbackQuotes {
    val lines: List<String> = listOf(
        "Peace comes from within. Do not seek it without.",
        "You have power over your mind, not outside events.",
        "In the middle of difficulty lies opportunity.",
        "Quiet the mind, and the soul will speak."
    )
}

object FallbackCarMakeCatalog : CarMakeCatalog {
    override fun makeFor(assetId: String): String {
        val names = FallbackCarMakes.names
        return names[kotlin.math.abs(assetId.hashCode()) % names.size]
    }

    override fun randomMake(): String = FallbackCarMakes.names.random()
}

object FallbackInsultCatalog : InsultCatalog {
    override fun nextInsult(): String = FallbackInsults.lines.random()
}

object FallbackBookCatalog : BookCatalog {
    override fun nextBookTitle(): String = FallbackBooks.titles.random()
}

object FallbackQuoteCatalog : QuoteCatalog {
    override fun nextQuote(): String = FallbackQuotes.lines.random()
}

object FallbackStreets {
    val lines: List<String> = listOf(
        "KE|Westlands · Kenyatta Avenue",
        "KE|Kilimani · Argwings Kodhek Road",
        "NG|Victoria Island · Adeola Odeku",
        "NG|Ikeja · Allen Avenue",
        "ZA|Sandton · Rivonia Road",
        "ZA|Cape Town · Long Street",
        "EG|Zamalek · 26th of July Street",
        "EG|Maadi · Road 9",
        "US|Greenwich Village · Bleecker Street",
        "US|Brooklyn · Bedford Avenue",
        "CA|Downtown · Queen Street West",
        "GB|Camden · Camden High Street",
        "FR|Le Marais · Rue des Rosiers",
        "DE|Mitte · Unter den Linden",
        "IN|Bandra · Hill Road",
        "JP|Shibuya · Dogenzaka",
        "PH|Makati · Ayala Avenue",
        "ID|Menteng · Jalan Teuku Umar",
        "BR|Pinheiros · Rua dos Pinheiros",
        "MX|Roma Norte · Álvaro Obregón"
    )

    fun forCountry(countryCode: String): List<String> {
        val code = countryCode.uppercase()
        val matched = lines.mapNotNull { line ->
            val prefix = line.substringBefore("|")
            val rest = line.substringAfter("|")
            if (prefix == code) rest else null
        }
        return matched.ifEmpty {
            lines.filter { it.startsWith("US|") }.map { it.substringAfter("|") }
        }
    }
}

object FallbackArtworks {
    val titles: List<String> = listOf(
        "Self-Portrait with a Straw Hat by Vincent van Gogh",
        "Bridge over a Pond of Water Lilies by Claude Monet",
        "The Dance Class by Edgar Degas",
        "Washington Crossing the Delaware by Emanuel Leutze",
        "The Gulf Stream by Winslow Homer"
    )
}

object FallbackWanted {
    val lines: List<String> = listOf(
        "Jordan Hale · Financial fraud",
        "Riley Chen · Cyber fraud",
        "Morgan Diaz · Counterfeit currency",
        "Avery Okonkwo · Wire fraud",
        "Samira Patel · Securities fraud"
    )
}

object FallbackSpaceNews {
    val lines: List<String> = listOf(
        "New orbital telescope captures first calibration images — NASA",
        "Crewed capsule docks with the station after a two-day chase — ESA",
        "Smallsat swarm maps coastal flooding in near real time — SpaceNews",
        "Lab publishes a quieter ion thruster design — Nature"
    )
}

object FallbackStreetCatalog : StreetCatalog {
    override fun listingFor(countryCode: String, key: String): String {
        val streets = FallbackStreets.forCountry(countryCode)
        return streets[kotlin.math.abs(key.hashCode()) % streets.size]
    }
}

object FallbackArtCatalog : ArtCatalog {
    override fun nextArtwork(): String = FallbackArtworks.titles.random()
}

object FallbackWantedCatalog : WantedCatalog {
    override fun nextBulletin(): String = FallbackWanted.lines.random()
}

object FallbackSpaceNewsCatalog : SpaceNewsCatalog {
    override fun nextHeadline(): String = FallbackSpaceNews.lines.random()
}
