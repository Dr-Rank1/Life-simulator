package com.maisha.game.domain

import com.maisha.game.data.model.BenefactorFavorType
import com.maisha.game.data.model.CareerState
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.CommunityStandingTier
import com.maisha.game.data.model.CriminalRecord
import com.maisha.game.data.model.FamilyAlliance
import com.maisha.game.data.model.FamilyFeud
import com.maisha.game.data.model.FamilyHeritageTag
import com.maisha.game.data.model.FeudAction
import com.maisha.game.data.model.Job
import com.maisha.game.data.model.Person
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.SchoolStage
import com.maisha.game.data.model.SocialBondType
import com.maisha.game.data.model.SocialEcosystemState
import com.maisha.game.data.model.Stats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SocialEcosystemEngineTest {

    private val financeEngine = FinanceEngine()
    private val engine = SocialEcosystemEngine(financeEngine)

    private fun testCharacter(
        standingScore: Int = 50,
        karma: Int = 50,
        money: Int = 20_000,
        feuds: List<FamilyFeud> = emptyList(),
        alliances: List<FamilyAlliance> = emptyList(),
        hasJob: Boolean = true,
        criminalRecord: CriminalRecord = CriminalRecord()
    ): Character {
        val career = if (hasJob) {
            CareerState(
                currentJob = Job(
                    id = "analyst_1",
                    title = "Data Analyst",
                    minEducation = SchoolStage.UNIVERSITY,
                    baseSalary = 50_000
                ),
                performance = 50f
            )
        } else {
            CareerState()
        }

        return TestFixtures.character(
            name = "Zawadi M",
            countryCode = "KE",
            age = 30,
            stats = Stats(
                health = 80,
                happiness = 75,
                smarts = 70,
                looks = 70,
                money = money,
                karma = karma
            ),
            career = career,
            criminalRecord = criminalRecord,
            socialEcosystem = SocialEcosystemState(
                communityStandingScore = standingScore,
                familyHeritage = FamilyHeritageTag.HUMBLE_ROOTS,
                activeFeuds = feuds,
                activeAlliances = alliances,
                socialActionDoneThisYear = false,
                neighborhoodFavorsRemaining = 1
            )
        )
    }

    @Test
    fun ageUp_improvesStanding_forHighKarmaAndCleanRecord() {
        val character = testCharacter(standingScore = 50, karma = 80)
        val result = engine.ageUpSocialEcosystem(character)

        assertTrue(result.character.socialEcosystem.communityStandingScore > 50)
        assertFalse(result.character.socialEcosystem.socialActionDoneThisYear)
        assertEquals(1, result.character.socialEcosystem.neighborhoodFavorsRemaining)
    }

    @Test
    fun ageUp_degradesStanding_forLowKarmaOrHighHeat() {
        val character = testCharacter(
            standingScore = 50,
            karma = 20,
            criminalRecord = CriminalRecord(heatLevel = 45)
        )
        val result = engine.ageUpSocialEcosystem(character)

        assertTrue(result.character.socialEcosystem.communityStandingScore < 50)
    }

    @Test
    fun ageUp_feudDecaysOrDissolves_whenIntensityDrops() {
        val weakFeud = FamilyFeud(
            id = "feud_1",
            rivalFamilyName = "Omondi",
            intensity = 16,
            originReason = "Disputed field boundary"
        )
        val character = testCharacter(standingScore = 70, feuds = listOf(weakFeud))
        val result = engine.ageUpSocialEcosystem(character)

        // At standing 70, drift is -2. 16 - 2 = 14 <= 15 threshold, so feud dissolves!
        assertTrue(result.character.socialEcosystem.activeFeuds.isEmpty())
    }

    @Test
    fun resolveFeudAction_oliveBranch_reducesIntensityOrSettlesFeud() {
        val feud = FamilyFeud(
            id = "feud_active",
            rivalFamilyName = "Kariuki",
            intensity = 40,
            originReason = "Ancestral trading squabble"
        )
        val character = testCharacter(money = 10_000, karma = 75, feuds = listOf(feud))

        val result = engine.resolveFeudAction(character, feud.id, FeudAction.OFFER_OLIVE_BRANCH)
        assertTrue(result is FeudActionResult.Success)
        val success = result as FeudActionResult.Success

        assertTrue(success.character.stats.money < 10_000)
        assertTrue(success.character.socialEcosystem.socialActionDoneThisYear)
    }

    @Test
    fun resolveFeudAction_escalateFeud_increasesIntensityAndDropsKarma() {
        val feud = FamilyFeud(
            id = "feud_active",
            rivalFamilyName = "Kariuki",
            intensity = 40,
            originReason = "Ancestral trading squabble"
        )
        val character = testCharacter(feuds = listOf(feud))

        val result = engine.resolveFeudAction(character, feud.id, FeudAction.ESCALATE_FEUD)
        assertTrue(result is FeudActionResult.Success)
        val updated = (result as FeudActionResult.Success).character

        val updatedFeud = updated.socialEcosystem.activeFeuds.first()
        assertEquals(65, updatedFeud.intensity)
        assertTrue(updated.stats.karma < 50)
    }

    @Test
    fun resolveFeudAction_legalInjunction_requiresFundsAndHaltsProvocations() {
        val feud = FamilyFeud(
            id = "feud_active",
            rivalFamilyName = "Kariuki",
            intensity = 50,
            originReason = "Ancestral trading squabble"
        )
        val character = testCharacter(money = 50_000, feuds = listOf(feud))

        val result = engine.resolveFeudAction(character, feud.id, FeudAction.LEGAL_INJUNCTION)
        assertTrue(result is FeudActionResult.Success)
        val updated = (result as FeudActionResult.Success).character

        val updatedFeud = updated.socialEcosystem.activeFeuds.first()
        assertEquals(30, updatedFeud.intensity)
        assertTrue(updated.stats.money < 50_000)
    }

    @Test
    fun callInBenefactorFavor_careerRecommendation_boostsPerformance() {
        val alliance = FamilyAlliance(
            id = "ally_1",
            allyFamilyName = "Barasa",
            trustLevel = 85,
            pactReason = "Longtime business partnerships"
        )
        val character = testCharacter(alliances = listOf(alliance))

        val result = engine.callInBenefactorFavor(character, alliance.id, BenefactorFavorType.CAREER_RECOMMENDATION)
        assertTrue(result is BenefactorFavorResult.Success)
        val updated = (result as BenefactorFavorResult.Success).character

        assertEquals(72f, updated.career.performance, 0.1f)
        assertEquals(0, updated.socialEcosystem.neighborhoodFavorsRemaining)
    }

    @Test
    fun callInBenefactorFavor_fails_whenNoFavorsRemaining() {
        val alliance = FamilyAlliance(
            id = "ally_1",
            allyFamilyName = "Barasa",
            trustLevel = 85,
            pactReason = "Longtime business partnerships"
        )
        val character = testCharacter(alliances = listOf(alliance)).let {
            it.copy(socialEcosystem = it.socialEcosystem.copy(neighborhoodFavorsRemaining = 0))
        }

        val result = engine.callInBenefactorFavor(character, alliance.id, BenefactorFavorType.FINANCIAL_BACKING)
        assertTrue(result is BenefactorFavorResult.Failed)
    }

    @Test
    fun reconcileRivalry_convertsRivalToFriend_onHighSmartsOrKarma() {
        val rival = Person(
            id = "rival_1",
            name = "Simba",
            relation = RelationType.ENEMY,
            age = 30,
            relationshipLevel = 20,
            socialBond = SocialBondType.RECURRING_RIVAL
        )
        val character = testCharacter(karma = 70).copy(family = listOf(rival))

        val result = engine.reconcileRivalry(character, rival.id)
        assertTrue(result is RivalryResolutionResult.Success)
        val updated = (result as RivalryResolutionResult.Success).character

        val reconciled = updated.family.first { it.id == rival.id }
        assertEquals(RelationType.FRIEND, reconciled.relation)
        assertEquals(SocialBondType.NONE, reconciled.socialBond)
        assertEquals(55, reconciled.relationshipLevel)
    }

    @Test
    fun calculateLegacyHeritage_assignsDynastyAndInheritsFeuds() {
        val feud = FamilyFeud(
            id = "feud_anc",
            rivalFamilyName = "Ndung'u",
            intensity = 60,
            originReason = "Stolen cattle generations ago"
        )
        val ally = FamilyAlliance(
            id = "ally_anc",
            allyFamilyName = "Ochieng",
            trustLevel = 80,
            pactReason = "Blood brotherhood"
        )
        val deceased = testCharacter(
            money = 300_000,
            karma = 70,
            feuds = listOf(feud),
            alliances = listOf(ally)
        )

        val heirState = engine.calculateLegacyHeritage(deceased)

        assertEquals(FamilyHeritageTag.HONORED_PHILANTHROPISTS, heirState.familyHeritage)
        assertEquals(75, heirState.communityStandingScore)
        assertEquals(1, heirState.activeFeuds.size)
        assertTrue(heirState.activeFeuds.first().ancestral)
        assertEquals(1, heirState.activeAlliances.size)
        assertTrue(heirState.activeAlliances.first().ancestral)
    }

    @Test
    fun standingTiers_and_mortgageDiscounts_areAccurate() {
        val pillarChar = testCharacter(standingScore = 90)
        assertEquals(CommunityStandingTier.PILLAR_OF_SOCIETY, pillarChar.socialEcosystem.currentStandingTier)
        assertEquals(0.005, engine.getMortgageRateDiscount(pillarChar), 0.0001)
        assertEquals(15, engine.getHireBonusPercentage(pillarChar))

        val pariahChar = testCharacter(standingScore = 15)
        assertEquals(CommunityStandingTier.NOTORIOUS_PARIAH, pariahChar.socialEcosystem.currentStandingTier)
        assertEquals(0.0, engine.getMortgageRateDiscount(pariahChar), 0.0001)
        assertEquals(-20, engine.getHireBonusPercentage(pariahChar))
    }
}
