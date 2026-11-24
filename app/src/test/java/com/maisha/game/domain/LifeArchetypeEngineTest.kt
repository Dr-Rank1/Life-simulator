package com.maisha.game.domain

import com.maisha.game.data.model.CareerState
import com.maisha.game.data.model.CareerTrack
import com.maisha.game.data.model.ChildhoodAptitude
import com.maisha.game.data.model.CommunityStandingTier
import com.maisha.game.data.model.CriminalRecord
import com.maisha.game.data.model.DoubleLifeState
import com.maisha.game.data.model.EducationState
import com.maisha.game.data.model.Gender
import com.maisha.game.data.model.Job
import com.maisha.game.data.model.Person
import com.maisha.game.data.model.PoliticalOffice
import com.maisha.game.data.model.PoliticalState
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.SchoolClique
import com.maisha.game.data.model.SchoolStage
import com.maisha.game.data.model.SecretIdentity
import com.maisha.game.data.model.SecretIdentityType
import com.maisha.game.data.model.SocialEcosystemState
import com.maisha.game.data.model.Stats
import org.junit.Assert.assertEquals
import org.junit.Test

class LifeArchetypeEngineTest {

    @Test
    fun resolveChapterKey_infancyEras() {
        val fragile = TestFixtures.character(age = 1, stats = Stats(health = 30, happiness = 50))
        assertEquals("chapter_fragile_beginnings", LifeArchetypeEngine.resolveChapterKey(fragile))

        val sunny = TestFixtures.character(age = 2, stats = Stats(health = 80, happiness = 85))
        assertEquals("chapter_sunlit_nursery", LifeArchetypeEngine.resolveChapterKey(sunny))

        val normal = TestFixtures.character(age = 2, stats = Stats(health = 60, happiness = 50))
        assertEquals("chapter_first_steps", LifeArchetypeEngine.resolveChapterKey(normal))
    }

    @Test
    fun resolveChapterKey_earlyChildhoodEras() {
        val wonder = TestFixtures.character(age = 4, stats = Stats(smarts = 85, happiness = 60))
        assertEquals("chapter_little_wonder", LifeArchetypeEngine.resolveChapterKey(wonder))

        val carefree = TestFixtures.character(age = 5, stats = Stats(smarts = 50, happiness = 80))
        assertEquals("chapter_carefree_days", LifeArchetypeEngine.resolveChapterKey(carefree))

        val curious = TestFixtures.character(age = 3, stats = Stats(smarts = 50, happiness = 50))
        assertEquals("chapter_curious_explorer", LifeArchetypeEngine.resolveChapterKey(curious))
    }

    @Test
    fun resolveChapterKey_primarySchoolAptitudes() {
        val athlete = TestFixtures.character(
            age = 8,
            education = EducationState(childhoodAptitude = ChildhoodAptitude.ATHLETICS)
        )
        assertEquals("chapter_playground_champion", LifeArchetypeEngine.resolveChapterKey(athlete))

        val artist = TestFixtures.character(
            age = 9,
            education = EducationState(childhoodAptitude = ChildhoodAptitude.ARTS_MUSIC)
        )
        assertEquals("chapter_budding_artist", LifeArchetypeEngine.resolveChapterKey(artist))

        val tinkerer = TestFixtures.character(
            age = 7,
            education = EducationState(childhoodAptitude = ChildhoodAptitude.TECH_TINKERING)
        )
        assertEquals("chapter_young_tinkerer", LifeArchetypeEngine.resolveChapterKey(tinkerer))

        val hustler = TestFixtures.character(
            age = 10,
            education = EducationState(childhoodAptitude = ChildhoodAptitude.STREET_HUSTLE)
        )
        assertEquals("chapter_streetwise_kid", LifeArchetypeEngine.resolveChapterKey(hustler))
    }

    @Test
    fun resolveChapterKey_middleSchoolCliques() {
        val rebel = TestFixtures.character(
            age = 13,
            education = EducationState(schoolClique = SchoolClique.REBELS)
        )
        assertEquals("chapter_reckless_rebel", LifeArchetypeEngine.resolveChapterKey(rebel))

        val jock = TestFixtures.character(
            age = 12,
            education = EducationState(schoolClique = SchoolClique.JOCKS)
        )
        assertEquals("chapter_varsity_aspirant", LifeArchetypeEngine.resolveChapterKey(jock))

        val troubled = TestFixtures.character(
            age = 14,
            criminalRecord = CriminalRecord(heatLevel = 30)
        )
        assertEquals("chapter_troubled_youth", LifeArchetypeEngine.resolveChapterKey(troubled))
    }

    @Test
    fun resolveChapterKey_highSchoolEras() {
        val honor = TestFixtures.character(
            age = 17,
            education = EducationState(gpa = 3.9f)
        )
        assertEquals("chapter_honor_roll_bound", LifeArchetypeEngine.resolveChapterKey(honor))

        val incarcerated = TestFixtures.character(
            age = 18,
            criminalRecord = CriminalRecord(currentlyIncarcerated = true)
        )
        assertEquals("chapter_juvenile_outlaw", LifeArchetypeEngine.resolveChapterKey(incarcerated))
    }

    @Test
    fun resolveChapterKey_youngAdulthood() {
        val uni = TestFixtures.character(
            age = 21,
            education = EducationState(stage = SchoolStage.UNIVERSITY)
        )
        assertEquals("chapter_campus_grind", LifeArchetypeEngine.resolveChapterKey(uni))

        val ladder = TestFixtures.character(
            age = 23,
            career = CareerState(
                currentJob = TestFixtures.job(
                    id = "junior_dev",
                    title = "Junior Dev"
                )
            )
        )
        assertEquals("chapter_climbing_the_ladder", LifeArchetypeEngine.resolveChapterKey(ladder))
    }

    @Test
    fun resolveChapterKey_primeAndMatureCareer() {
        val politician = TestFixtures.character(
            age = 35,
            politics = PoliticalState(currentOffice = PoliticalOffice.MAYOR)
        )
        assertEquals("chapter_hall_of_power", LifeArchetypeEngine.resolveChapterKey(politician))

        val matriarch = TestFixtures.character(
            age = 52,
            gender = Gender.FEMALE,
            family = listOf(
                TestFixtures.person(id = "c1", name = "Child 1", relation = RelationType.CHILD, age = 20),
                TestFixtures.person(id = "c2", name = "Child 2", relation = RelationType.CHILD, age = 18)
            )
        )
        assertEquals("chapter_beloved_matriarch", LifeArchetypeEngine.resolveChapterKey(matriarch))

        val patriarch = TestFixtures.character(
            age = 52,
            gender = Gender.MALE,
            family = listOf(
                TestFixtures.person(id = "c1", name = "Child 1", relation = RelationType.CHILD, age = 20),
                TestFixtures.person(id = "c2", name = "Child 2", relation = RelationType.CHILD, age = 18)
            )
        )
        assertEquals("chapter_beloved_patriarch", LifeArchetypeEngine.resolveChapterKey(patriarch))

        val tycoonElder = TestFixtures.character(age = 70)
        assertEquals("chapter_dynasty_elder", LifeArchetypeEngine.resolveChapterKey(tycoonElder, netWorth = 2_000_000))
    }

    @Test
    fun resolveEpitaphKey_evaluatesCorrectArchetypes() {
        val saint = TestFixtures.character(
            stats = Stats(karma = 90, happiness = 70),
            criminalRecord = CriminalRecord(timesArrested = 0)
        )
        assertEquals("epitaph_saintly_benefactor", LifeArchetypeEngine.resolveEpitaphKey(saint))

        val tycoon = TestFixtures.character(stats = Stats(karma = 40, happiness = 40))
        assertEquals("epitaph_tycoon_builder", LifeArchetypeEngine.resolveEpitaphKey(tycoon, netWorth = 1_500_000))

        val politician = TestFixtures.character(
            stats = Stats(karma = 40),
            politics = PoliticalState(currentOffice = PoliticalOffice.GOVERNOR)
        )
        assertEquals("epitaph_public_champion", LifeArchetypeEngine.resolveEpitaphKey(politician))

        val shadow = TestFixtures.character(
            stats = Stats(karma = 40),
            doubleLife = DoubleLifeState(
                activeSecret = SecretIdentity(
                    type = SecretIdentityType.SHADOW_OPERATOR,
                    codename = "Specter",
                    coverStory = "Importer"
                )
            )
        )
        assertEquals("epitaph_shadow_operator", LifeArchetypeEngine.resolveEpitaphKey(shadow))

        val pariah = TestFixtures.character(
            stats = Stats(karma = 20),
            socialEcosystem = SocialEcosystemState(communityStandingScore = 15)
        )
        assertEquals("epitaph_unforgiven_pariah", LifeArchetypeEngine.resolveEpitaphKey(pariah))

        val familyAnchor = TestFixtures.character(
            stats = Stats(karma = 50, happiness = 70),
            family = listOf(
                TestFixtures.person(id = "s1", name = "Son", relation = RelationType.CHILD, age = 20),
                TestFixtures.person(id = "d1", name = "Daughter", relation = RelationType.CHILD, age = 18)
            )
        )
        assertEquals("epitaph_family_pillar", LifeArchetypeEngine.resolveEpitaphKey(familyAnchor))
    }

    @Test
    fun resolveLegacyStampKey_evaluatesCorrectStamps() {
        val founder = TestFixtures.character(
            family = listOf(TestFixtures.person(id = "s1", name = "Son", relation = RelationType.CHILD, age = 10))
        )
        assertEquals("stamp_dynasty_founder", LifeArchetypeEngine.resolveLegacyStampKey(founder, netWorth = 800_000))

        val luminary = TestFixtures.character(
            socialEcosystem = SocialEcosystemState(communityStandingScore = 85)
        )
        assertEquals("stamp_civic_luminary", LifeArchetypeEngine.resolveLegacyStampKey(luminary))

        val ancestor = TestFixtures.character(
            stats = Stats(karma = 80),
            family = listOf(TestFixtures.person(id = "c1", name = "Child", relation = RelationType.CHILD, age = 15))
        )
        assertEquals("stamp_honored_ancestor", LifeArchetypeEngine.resolveLegacyStampKey(ancestor))

        val renegade = TestFixtures.character(
            criminalRecord = CriminalRecord(notoriety = 60)
        )
        assertEquals("stamp_renegade_legend", LifeArchetypeEngine.resolveLegacyStampKey(renegade))

        val pioneer = TestFixtures.character(
            countryCode = "CA",
            relocationHistory = listOf("KE", "CA")
        ).copy(citizenships = listOf("KE"))
        assertEquals("stamp_solitary_pioneer", LifeArchetypeEngine.resolveLegacyStampKey(pioneer))

        val torchbearer = TestFixtures.character(
            generationNumber = 3
        )
        assertEquals("stamp_steadfast_torchbearer", LifeArchetypeEngine.resolveLegacyStampKey(torchbearer))

        val humble = TestFixtures.character(
            generationNumber = 1
        )
        assertEquals("stamp_humble_origin", LifeArchetypeEngine.resolveLegacyStampKey(humble))
    }
}
