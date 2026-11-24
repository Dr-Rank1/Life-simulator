package com.maisha.game.domain

import com.maisha.game.data.model.CriminalRecord
import com.maisha.game.data.model.Stats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LeisureEngineTest {
    private val engine = LeisureEngine()

    @Test
    fun perform_successSpendsCashAndBoostsHappiness() {
        val before = TestFixtures.character(
            age = 20,
            stats = Stats(money = 50_000, happiness = 40, health = 70, smarts = 50, looks = 50)
        )
        val cost = engine.cost(LeisureActivity.NATURE_DAY, before.countryCode)
        val result = engine.perform(before, LeisureActivity.NATURE_DAY)
        assertTrue(result is LeisureResult.Success)
        val after = (result as LeisureResult.Success).character
        assertEquals(before.stats.money - cost, after.stats.money)
        assertTrue(after.stats.happiness > before.stats.happiness)
        assertTrue(after.eventLog.first().contains("nature"))
    }

    @Test
    fun perform_insufficientFunds() {
        val broke = TestFixtures.character(
            age = 20,
            stats = Stats(money = 10, happiness = 40, health = 70)
        )
        val result = engine.perform(broke, LeisureActivity.SPA_DAY)
        assertEquals(LeisureResult.InsufficientFunds, result)
    }

    @Test
    fun perform_ineligibleWhenIncarcerated() {
        val locked = TestFixtures.character(
            age = 22,
            stats = Stats(money = 100_000),
            criminalRecord = CriminalRecord(currentlyIncarcerated = true, yearsRemaining = 2)
        )
        assertEquals(
            LeisureResult.Ineligible,
            engine.perform(locked, LeisureActivity.NIGHT_OUT)
        )
    }

    @Test
    fun activitiesFor_childGetsPlaygroundNotSpa() {
        val child = TestFixtures.character(
            age = 10,
            stats = Stats(money = 20_000, happiness = 50, health = 70)
        )
        val activities = engine.activitiesFor(child)
        assertTrue(activities.contains(LeisureActivity.PLAYGROUND))
        assertTrue(activities.contains(LeisureActivity.STUDY_BUDDY))
        assertTrue(activities.contains(LeisureActivity.CHORES))
        assertFalse(activities.contains(LeisureActivity.SPA_DAY))
    }

    @Test
    fun chores_paysCashAndIsEligibleForTween() {
        val tween = TestFixtures.character(
            age = 12,
            stats = Stats(money = 100, happiness = 50, health = 70)
        )
        val result = engine.perform(tween, LeisureActivity.CHORES)
        assertTrue(result is LeisureResult.Success)
        val after = (result as LeisureResult.Success).character
        assertTrue(after.stats.money > tween.stats.money)
    }

    @Test
    fun adultLeisure_ineligibleForYoungChild() {
        val toddler = TestFixtures.character(
            age = 4,
            stats = Stats(money = 50_000)
        )
        assertEquals(
            LeisureResult.Ineligible,
            engine.perform(toddler, LeisureActivity.PLAYGROUND)
        )
    }

    @Test
    fun toddlerGetsStorytimeAndParkActivities() {
        val toddler = TestFixtures.character(
            age = 3,
            stats = Stats(money = 50_000, happiness = 50, health = 70, smarts = 40)
        )
        val activities = engine.activitiesFor(toddler)
        assertTrue(activities.contains(LeisureActivity.STORYTIME))
        assertTrue(activities.contains(LeisureActivity.PARK_CAREGIVER))
        assertFalse(activities.contains(LeisureActivity.PLAYGROUND))
        val result = engine.perform(toddler, LeisureActivity.STORYTIME)
        assertTrue(result is LeisureResult.Success)
    }

    @Test
    fun midLifeGetsRetreatAndReunion() {
        val adult = TestFixtures.character(
            age = 35,
            stats = Stats(money = 200_000, happiness = 50, health = 70)
        )
        val activities = engine.activitiesFor(adult)
        assertTrue(activities.contains(LeisureActivity.WEEKEND_RETREAT))
        assertTrue(activities.contains(LeisureActivity.REUNION_DINNER))
    }

    @Test
    fun seniorGetsGrandkidsAndMemoir() {
        val senior = TestFixtures.character(
            age = 62,
            stats = Stats(money = 100_000, happiness = 50, health = 70, smarts = 60)
        )
        val activities = engine.activitiesFor(senior)
        assertTrue(activities.contains(LeisureActivity.GRANDKIDS_DAY))
        assertTrue(activities.contains(LeisureActivity.MEMOIR_WRITING))
    }

    @Test
    fun libraryVisit_usesBookCatalogTitle() {
        val books = object : com.maisha.game.data.content.BookCatalog {
            override fun nextBookTitle() = "Things Fall Apart by Chinua Achebe"
        }
        val quotes = object : com.maisha.game.data.content.QuoteCatalog {
            override fun nextQuote() = "unused"
        }
        val leisure = LeisureEngine(books, quotes)
        val before = TestFixtures.character(
            age = 20,
            stats = Stats(money = 50_000, happiness = 40, health = 70, smarts = 50)
        )
        val result = leisure.perform(before, LeisureActivity.LIBRARY_VISIT)
        assertTrue(result is LeisureResult.Success)
        val success = result as LeisureResult.Success
        assertEquals("Things Fall Apart by Chinua Achebe", success.flavor)
        assertTrue(success.character.eventLog.first().contains("Things Fall Apart"))
    }

    @Test
    fun meditation_usesQuoteCatalog() {
        val books = object : com.maisha.game.data.content.BookCatalog {
            override fun nextBookTitle() = "unused"
        }
        val quotes = object : com.maisha.game.data.content.QuoteCatalog {
            override fun nextQuote() = "Quiet the mind."
        }
        val leisure = LeisureEngine(books, quotes)
        val before = TestFixtures.character(
            age = 20,
            stats = Stats(money = 50_000, happiness = 40, health = 70, smarts = 50)
        )
        val result = leisure.perform(before, LeisureActivity.MEDITATION)
        assertTrue(result is LeisureResult.Success)
        val success = result as LeisureResult.Success
        assertEquals("Quiet the mind.", success.flavor)
        assertTrue(success.character.eventLog.first().contains("Quiet the mind."))
    }

    @Test
    fun museumVisit_usesArtCatalogTitle() {
        val books = object : com.maisha.game.data.content.BookCatalog {
            override fun nextBookTitle() = "unused"
        }
        val quotes = object : com.maisha.game.data.content.QuoteCatalog {
            override fun nextQuote() = "unused"
        }
        val art = object : com.maisha.game.data.content.ArtCatalog {
            override fun nextArtwork() = "The Dance Class by Edgar Degas"
        }
        val leisure = LeisureEngine(books, quotes, art)
        val before = TestFixtures.character(
            age = 22,
            stats = Stats(money = 50_000, happiness = 40, health = 70, smarts = 50)
        )
        val result = leisure.perform(before, LeisureActivity.VISIT_MUSEUM)
        assertTrue(result is LeisureResult.Success)
        val success = result as LeisureResult.Success
        assertEquals("The Dance Class by Edgar Degas", success.flavor)
        assertTrue(success.character.eventLog.any { it.contains("Dance Class") })
    }

    @Test
    fun luxuryActivities_requireCorrespondingAssets() {
        val adultWithoutAssets = TestFixtures.character(
            age = 35,
            stats = Stats(money = 10_000_000, happiness = 50, health = 80),
            assets = emptyList()
        )
        assertFalse(engine.isEligible(adultWithoutAssets, LeisureActivity.YACHT_DAY))
        assertFalse(engine.isEligible(adultWithoutAssets, LeisureActivity.PRIVATE_JET_TRIP))

        val yachtAsset = com.maisha.game.data.model.Asset(
            id = "boat_1",
            name = "Sunseeker Yacht",
            type = com.maisha.game.data.model.AssetType.BOAT,
            purchasePrice = 5_000_000,
            currentValue = 5_000_000,
            monthlyUpkeep = 10_000,
            condition = 100
        )
        val jetAsset = com.maisha.game.data.model.Asset(
            id = "jet_1",
            name = "Gulfstream Jet",
            type = com.maisha.game.data.model.AssetType.PRIVATE_JET,
            purchasePrice = 25_000_000,
            currentValue = 25_000_000,
            monthlyUpkeep = 50_000,
            condition = 100
        )

        val adultWithYacht = adultWithoutAssets.copy(assets = listOf(yachtAsset))
        assertTrue(engine.isEligible(adultWithYacht, LeisureActivity.YACHT_DAY))
        assertFalse(engine.isEligible(adultWithYacht, LeisureActivity.PRIVATE_JET_TRIP))

        val result = engine.perform(adultWithYacht, LeisureActivity.YACHT_DAY)
        assertTrue(result is LeisureResult.Success)
        val after = (result as LeisureResult.Success).character
        assertTrue("Yacht day should give a massive happiness boost", after.stats.happiness > 50)

        val adultWithJet = adultWithoutAssets.copy(assets = listOf(jetAsset))
        assertTrue(engine.isEligible(adultWithJet, LeisureActivity.PRIVATE_JET_TRIP))
    }
}

class CrimeStatusMapperTest {
    @Test
    fun map_awaitingTrialAndPrison() {
        assertEquals(
            CrimeStatusKind.AWAITING_TRIAL,
            CrimeStatusMapper.map(CriminalRecord(awaitingTrial = true)).kind
        )
        val prison = CrimeStatusMapper.map(
            CriminalRecord(
                currentlyIncarcerated = true,
                yearsRemaining = 3,
                yearsServed = 1,
                totalSentenceYears = 4
            )
        )
        assertEquals(CrimeStatusKind.INCARCERATED, prison.kind)
        assertEquals(3, prison.yearsRemaining)
        assertTrue(prison.showParoleHint)
    }
}

class ActionQuestHintsTest {
    @Test
    fun matches_skillAndSocialFamilies() {
        assertTrue(
            ActionQuestHints.helpsQuest(YearQuestKind.RAISE_SKILL, ActionFamily.SKILL_PRACTICE)
        )
        assertTrue(
            ActionQuestHints.helpsQuest(YearQuestKind.GROW_FOLLOWERS, ActionFamily.SOCIAL_POST)
        )
        assertTrue(
            ActionQuestHints.anyMatch(
                listOf(YearQuest(YearQuestKind.EARN_MONEY, 1_000, "x")),
                ActionFamily.SIDE_HUSTLE
            )
        )
    }
}
