package com.maisha.game.data

import com.maisha.game.data.model.Gender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class NamePoolTest {

    @Test
    fun `every roster country has a large de-duplicated pool`() {
        CountryCatalog.all().forEach { country ->
            val pool = NamePool.getNamePool(country.code)
            assertTrue(NamePool.hasPool(country.code))
            assertEquals(pool.maleFirstNames.size, pool.maleFirstNames.distinct().size)
            assertEquals(pool.femaleFirstNames.size, pool.femaleFirstNames.distinct().size)
            assertEquals(pool.surnames.size, pool.surnames.distinct().size)
            assertTrue("${country.code} male ${pool.maleFirstNames.size}", pool.maleFirstNames.size >= 48)
            assertTrue("${country.code} female ${pool.femaleFirstNames.size}", pool.femaleFirstNames.size >= 48)
            assertTrue("${country.code} surnames ${pool.surnames.size}", pool.surnames.size >= 40)
            pool.maleFirstNames.forEach { assertTrue(it.isNotBlank()) }
            pool.femaleFirstNames.forEach { assertTrue(it.isNotBlank()) }
            pool.surnames.forEach { assertTrue(it.isNotBlank()) }
        }
    }

    @Test
    fun `unknown country codes fall back to the US pool`() {
        assertFalse(NamePool.hasPool("ZZ"))
        assertEquals(
            NamePool.getNamePool("US"),
            NamePool.getNamePool("ZZ")
        )
        assertTrue(NamePool.randomMaleFirstName("ZZ") in NamePool.getNamePool("US").maleFirstNames)
    }

    @Test
    fun `Kenya and US given names stay distinct at the head of the list`() {
        assertNotEquals(
            NamePool.getNamePool("US").maleFirstNames.first(),
            NamePool.getNamePool("KE").maleFirstNames.first()
        )
        assertEquals("James", NamePool.getNamePool("US").maleFirstNames.first())
        assertEquals("Brian", NamePool.getNamePool("KE").maleFirstNames.first())
    }

    @Test
    fun `Kenya given names are not clan surnames`() {
        val ke = NamePool.getNamePool("KE")
        val leaked = listOf("Otieno", "Omondi", "Mwangi", "Kamau", "Odhiambo")
        leaked.forEach { name ->
            assertFalse("$name should not be a Kenyan given name", name in ke.maleFirstNames)
            assertTrue("$name should remain a surname", name in ke.surnames)
        }
        assertTrue("Wanjiku" in ke.femaleFirstNames)
        assertTrue("Wambui" in ke.femaleFirstNames)
        assertFalse("Wanjiku" in ke.surnames)
        assertFalse("Wambui" in ke.surnames)
    }

    @Test
    fun `compound surnames stay as a single token`() {
        assertTrue("Dela Cruz" in NamePool.getNamePool("PH").surnames)
        assertTrue("Van der Merwe" in NamePool.getNamePool("ZA").surnames)
        assertTrue("O'Brien" in NamePool.getNamePool("GB").surnames)
        assertTrue("D'Souza" in NamePool.getNamePool("IN").surnames)
        val ph = CountryNamePool(
            maleFirstNames = listOf("Jose"),
            femaleFirstNames = listOf("Maria"),
            surnames = listOf("Dela Cruz")
        )
        assertEquals("Jose Dela Cruz", ph.randomFullName(Gender.MALE, Random(1)))
        assertEquals("Maria Dela Cruz", ph.randomFullName(Gender.FEMALE, Random(1)))
        val sibling = ph.randomSiblingName(Random(0))
        assertTrue(sibling == "Jose Dela Cruz" || sibling == "Maria Dela Cruz")
    }

    @Test
    fun `full names and given names use the requested gender`() {
        val us = NamePool.getNamePool("US")
        repeat(40) {
            val male = NamePool.randomFullName(Gender.MALE, "US")
            val female = NamePool.randomFullName(Gender.FEMALE, "US")
            val given = male.substringBefore(" ")
            val femaleGiven = female.substringBefore(" ")
            assertTrue(given in us.maleFirstNames)
            assertTrue(femaleGiven in us.femaleFirstNames)
            assertTrue(male.substringAfter(" ") in us.surnames)
        }
        assertTrue(NamePool.randomGivenName("US") in us.givenNames)
        assertTrue(NamePool.randomSurname("PH") in NamePool.getNamePool("PH").surnames)
    }

    @Test
    fun `seeded draws are deterministic`() {
        val pool = NamePool.getNamePool("GB")
        assertEquals(pool.randomMaleFirstName(Random(42)), pool.randomMaleFirstName(Random(42)))
        assertEquals(pool.randomFullName(Gender.FEMALE, Random(7)), pool.randomFullName(Gender.FEMALE, Random(7)))
    }
}
