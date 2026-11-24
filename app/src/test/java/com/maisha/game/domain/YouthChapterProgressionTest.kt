package com.maisha.game.domain

import com.maisha.game.data.events.EventRepository
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.ChildhoodAptitude
import com.maisha.game.data.model.EducationState
import com.maisha.game.data.model.Gender
import com.maisha.game.data.model.LifeEvent
import com.maisha.game.data.model.Person
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.SchoolActivity
import com.maisha.game.data.model.SchoolClique
import com.maisha.game.data.model.SchoolClub
import com.maisha.game.data.model.SchoolPerson
import com.maisha.game.data.model.SchoolPersonAction
import com.maisha.game.data.model.SchoolRole
import com.maisha.game.data.model.SchoolStage
import com.maisha.game.data.model.Stats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class YouthChapterProgressionTest {

    private lateinit var educationEngine: EducationEngine
    private lateinit var relationshipEngine: RelationshipEngine
    private lateinit var eventRepository: EventRepository

    @Before
    fun setUp() {
        val relocationEngine = RelocationEngine()
        educationEngine = EducationEngine(relocationEngine)
        val financeEngine = FinanceEngine()
        relationshipEngine = RelationshipEngine(financeEngine)
        eventRepository = EventRepository.forTesting(financeEngine)
    }

    private fun createYouthCharacter(
        age: Int,
        aptitude: ChildhoodAptitude = ChildhoodAptitude.NONE,
        clique: SchoolClique = SchoolClique.UNAFFILIATED,
        parentalTrust: Int = 75,
        curfewHour: Int = 21,
        flags: Set<String> = emptySet(),
        stats: Stats = Stats(smarts = 60, health = 70, happiness = 70, looks = 50, money = 100)
    ): Character {
        return TestFixtures.character(
            age = age,
            stats = stats,
            education = EducationState(
                stage = if (age >= 6) SchoolStage.PRIMARY else SchoolStage.NONE,
                childhoodAptitude = aptitude,
                schoolClique = clique,
                parentalTrust = parentalTrust,
                curfewHour = curfewHour,
                consequenceFlags = flags
            )
        )
    }

    // ==========================================
    // 1. Ages 4-10: Childhood Aptitude Seeds
    // ==========================================

    @Test
    fun testExploreChildhoodAptitude_InfancyProtected_ReturnsTooYoung() {
        val baby = createYouthCharacter(age = 2)
        val result = educationEngine.exploreChildhoodAptitude(baby, ChildhoodAptitude.ATHLETICS)
        assertTrue(result is ChildhoodAptitudeResult.TooYoung)

        val toddler = createYouthCharacter(age = 3)
        val toddlerResult = educationEngine.exploreChildhoodAptitude(toddler, ChildhoodAptitude.ACADEMICS)
        assertTrue(toddlerResult is ChildhoodAptitudeResult.TooYoung)
    }

    @Test
    fun testExploreChildhoodAptitude_AgeFourToTen_DiscoversObsessionAndBoostsStats() {
        val child = createYouthCharacter(age = 6, stats = Stats(smarts = 50, health = 50, happiness = 50))
        val result = educationEngine.exploreChildhoodAptitude(child, ChildhoodAptitude.TECH_TINKERING)

        assertTrue(result is ChildhoodAptitudeResult.Success)
        val updated = (result as ChildhoodAptitudeResult.Success).character
        assertEquals(ChildhoodAptitude.TECH_TINKERING, updated.education.childhoodAptitude)
        assertTrue(updated.education.aptitudeScore >= 18)
        assertTrue(updated.education.aptitudeExploredThisYear)
        assertTrue(updated.stats.smarts > 50)
        assertTrue(updated.stats.happiness > 50)
    }

    @Test
    fun testExploreChildhoodAptitude_AlreadyDoneThisYear_Guarded() {
        val child = createYouthCharacter(age = 8).let {
            it.copy(education = it.education.copy(aptitudeExploredThisYear = true))
        }
        val result = educationEngine.exploreChildhoodAptitude(child, ChildhoodAptitude.ATHLETICS)
        assertTrue(result is ChildhoodAptitudeResult.AlreadyDoneThisYear)
    }

    @Test
    fun testAptitudeBonus_ScholarshipChanceBoosted() {
        val baseStudent = createYouthCharacter(age = 18).copy(
            education = EducationState(
                stage = SchoolStage.SECONDARY,
                gpa = 3.6f,
                childhoodAptitude = ChildhoodAptitude.NONE
            )
        )
        val giftedStudent = baseStudent.copy(
            education = baseStudent.education.copy(
                childhoodAptitude = ChildhoodAptitude.ACADEMICS,
                aptitudeScore = 80
            )
        )

        val baseChance = educationEngine.scholarshipSuccessChance(baseStudent)
        val giftedChance = educationEngine.scholarshipSuccessChance(giftedStudent)
        assertTrue("Academics aptitude should boost scholarship chance", giftedChance > baseChance)
    }

    @Test
    fun testAptitudeBonus_ClubStartingSkillBoosted() {
        val athlete = createYouthCharacter(age = 15).copy(
            education = EducationState(
                stage = SchoolStage.SECONDARY,
                childhoodAptitude = ChildhoodAptitude.ATHLETICS,
                aptitudeScore = 70
            )
        )
        val regular = createYouthCharacter(age = 15).copy(
            education = EducationState(
                stage = SchoolStage.SECONDARY,
                childhoodAptitude = ChildhoodAptitude.NONE
            )
        )

        val athleteJoined = educationEngine.joinSchoolClub(athlete, SchoolClub.FOOTBALL)
        val regularJoined = educationEngine.joinSchoolClub(regular, SchoolClub.FOOTBALL)

        val athleteSkill = athleteJoined.education.clubSkill
        val regularSkill = regularJoined.education.clubSkill
        assertTrue("Athletics aptitude gives bonus starting football skill", athleteSkill > regularSkill)
    }

    // ==========================================
    // 2. Sibling Dynamics & Allowance Gating
    // ==========================================

    @Test
    fun testSiblingInteraction_TeamUp_BoostsBondAndHappiness() {
        val sibling = Person(
            id = "sib_1",
            name = "Kofi Mensah",
            relation = RelationType.SIBLING,
            relationshipLevel = 60,
            age = 10,
            gender = Gender.MALE
        )
        val me = createYouthCharacter(age = 9).copy(family = listOf(sibling))

        val result = relationshipEngine.progressRelationship(me, "sib_1", InteractionType.SIBLING_TEAM_UP)
        val updatedSib = result.character.family.first()
        assertTrue("Sibling bond should increase on team up", updatedSib.relationshipLevel > 60)
        assertTrue("Happiness should increase on team up", result.character.stats.happiness >= me.stats.happiness)
        assertEquals("msg_sibling_team_up_success", result.messageKey)
    }

    @Test
    fun testSiblingInteraction_Compete_SetsSiblingRivalryFlag() {
        val sibling = Person(
            id = "sib_2",
            name = "Amara Mensah",
            relation = RelationType.SIBLING,
            relationshipLevel = 70,
            age = 12,
            gender = Gender.FEMALE
        )
        val me = createYouthCharacter(age = 11).copy(family = listOf(sibling))

        val result = relationshipEngine.progressRelationship(me, "sib_2", InteractionType.SIBLING_COMPETE)
        assertTrue("Sibling compete sets siblingRivalry flag", result.character.hasConsequenceFlag("siblingRivalry"))
    }

    @Test
    fun testParentalAllowance_GatedByParentalTrust() {
        val parent = Person(
            id = "parent_1",
            name = "Baba Mensah",
            relation = RelationType.FATHER,
            relationshipLevel = 80,
            age = 42,
            gender = Gender.MALE
        )

        // Low trust: refused
        val untrustedChild = createYouthCharacter(age = 14, parentalTrust = 35).copy(family = listOf(parent))
        val lowTrustResult = relationshipEngine.progressRelationship(untrustedChild, "parent_1", InteractionType.ASK_FOR_MONEY)
        assertEquals("msg_ask_money_low_trust", lowTrustResult.messageKey)

        // High trust: not blocked by trust
        val trustedChild = createYouthCharacter(age = 14, parentalTrust = 85).copy(family = listOf(parent))
        val highTrustResult = relationshipEngine.progressRelationship(trustedChild, "parent_1", InteractionType.ASK_FOR_MONEY)
        assertTrue(highTrustResult.messageKey == "msg_ask_money_success" || highTrustResult.messageKey == "msg_ask_money_refused")
    }

    // ==========================================
    // 3. Middle School Cliques, Drama & Reputation
    // ==========================================

    @Test
    fun testSchoolClique_NerdsQualificationBySmartsOrAptitude() {
        val smartKid = createYouthCharacter(age = 12, stats = Stats(smarts = 75))
            .copy(education = EducationState(stage = SchoolStage.PRIMARY, childhoodAptitude = ChildhoodAptitude.NONE))
        val joinResult = educationEngine.joinClique(smartKid, SchoolClique.NERDS)
        assertTrue(joinResult is SchoolActionResult.Success)
        val joined = (joinResult as SchoolActionResult.Success).character
        assertEquals(SchoolClique.NERDS, joined.education.schoolClique)
        assertEquals(60, joined.education.cliqueStanding)

        // Kid with low smarts and no academic aptitude gets rejected
        val goofyKid = createYouthCharacter(age = 12, stats = Stats(smarts = 30))
            .copy(education = EducationState(stage = SchoolStage.PRIMARY, childhoodAptitude = ChildhoodAptitude.NONE))
        val rejectedResult = educationEngine.joinClique(goofyKid, SchoolClique.NERDS)
        assertTrue(rejectedResult is SchoolActionResult.Success)
        assertEquals(SchoolClique.UNAFFILIATED, (rejectedResult as SchoolActionResult.Success).character.education.schoolClique)
    }

    @Test
    fun testSchoolClique_MingleBoostsStanding() {
        val nerdKid = createYouthCharacter(age = 13, clique = SchoolClique.NERDS)
            .copy(education = EducationState(stage = SchoolStage.PRIMARY, schoolClique = SchoolClique.NERDS, cliqueStanding = 50))
        val result = educationEngine.mingleWithClique(nerdKid)

        assertTrue(result is SchoolActionResult.Success)
        val updated = (result as SchoolActionResult.Success).character
        assertTrue("Clique standing should increase after mingling", updated.education.cliqueStanding > 50)
        assertTrue(updated.education.socialActionDoneThisYear)
    }

    @Test
    fun testPlaygroundHustle_AvailableAtAgeTenToFourteen() {
        val student = createYouthCharacter(age = 12).copy(
            education = EducationState(stage = SchoolStage.PRIMARY)
        )
        val available = educationEngine.availableSchoolActivities(student)
        assertTrue("Playground hustle should be available at age 12", available.contains(SchoolActivity.PLAYGROUND_HUSTLE))

        val result = educationEngine.performSchoolActivity(student, SchoolActivity.PLAYGROUND_HUSTLE)
        assertTrue(result is SchoolActionResult.Success)
        val updated = (result as SchoolActionResult.Success).character
        assertTrue("Playground hustle gives consequence flag playgroundHustler", updated.hasConsequenceFlag("playgroundHustler"))
    }

    @Test
    fun testSchoolPersonInteractions_PassNoteAndBefriendBully() {
        val bully = SchoolPerson(
            id = "bully_1",
            name = "Biff",
            role = SchoolRole.BULLY,
            age = 13,
            relationshipLevel = 25,
            clique = SchoolClique.REBELS
        )
        val classmate = SchoolPerson(
            id = "classmate_1",
            name = "Grace",
            role = SchoolRole.CLASSMATE,
            age = 13,
            relationshipLevel = 55,
            clique = SchoolClique.NERDS
        )
        val student = createYouthCharacter(age = 13).copy(
            education = EducationState(
                stage = SchoolStage.PRIMARY,
                schoolPeople = listOf(bully, classmate)
            )
        )

        // Pass note to classmate
        val noteResult = educationEngine.handleSchoolPersonInteraction(student, "classmate_1", SchoolPersonAction.PASS_NOTE)
        assertTrue(noteResult is SchoolInteractionResult.Success)

        // Befriend bully
        val befriendResult = educationEngine.handleSchoolPersonInteraction(student, "bully_1", SchoolPersonAction.BEFRIEND_BULLY)
        assertTrue(befriendResult is SchoolInteractionResult.Success)
        val convertedBully = (befriendResult as SchoolInteractionResult.Success).character.education.schoolPeople.first { it.id == "bully_1" }
        assertEquals(SchoolRole.CLASSMATE, convertedBully.role)
        assertTrue(convertedBully.relationshipLevel >= 50)
    }

    // ==========================================
    // 4. Ages 15-18: High School, Curfew & Rebellion
    // ==========================================

    @Test
    fun testParentalCurfew_RequestExtension_HighVsLowTrust() {
        // High trust: granted
        val goodTeen = createYouthCharacter(age = 16, parentalTrust = 80, curfewHour = 21)
        val (extended, extMsg) = educationEngine.requestCurfewExtension(goodTeen)
        assertEquals(22, extended.education.curfewHour)
        assertTrue(extMsg.contains("extended", ignoreCase = true))

        // Low trust: denied
        val rebelTeen = createYouthCharacter(age = 16, parentalTrust = 50, curfewHour = 21)
        val (denied, denMsg) = educationEngine.requestCurfewExtension(rebelTeen)
        assertEquals(21, denied.education.curfewHour)
        assertTrue(denMsg.contains("denied", ignoreCase = true))
    }

    @Test
    fun testTickSchoolYear_LiftsCurfewAtAgeEighteen() {
        val teenAt17 = createYouthCharacter(age = 17, curfewHour = 22, parentalTrust = 70).copy(
            education = EducationState(stage = SchoolStage.SECONDARY, curfewHour = 22, parentalTrust = 70)
        )
        val adultAt18 = educationEngine.tickSchoolYear(teenAt17.copy(age = 18))
        assertEquals("Curfew should be lifted at age 18", 24, adultAt18.education.curfewHour)
    }

    // ==========================================
    // 5. Age 18 Crossroads Milestone
    // ==========================================

    @Test
    fun testEvaluateAge18Crossroads_GeneratesCommemorativeMilestone() {
        val candidate = createYouthCharacter(
            age = 18,
            aptitude = ChildhoodAptitude.ACADEMICS,
            parentalTrust = 90
        ).copy(
            education = EducationState(
                stage = SchoolStage.SECONDARY,
                gpa = 3.9f,
                childhoodAptitude = ChildhoodAptitude.ACADEMICS,
                aptitudeScore = 85,
                parentalTrust = 90
            )
        )

        val evaluated = educationEngine.evaluateAge18Crossroads(candidate)
        assertEquals(24, evaluated.education.curfewHour)
        assertTrue(evaluated.hasConsequenceFlag("completedYouthChapter"))
        val logEntry = evaluated.eventLog.firstOrNull { it.contains("Age 18 Crossroads") }
        assertNotNull("Milestone event log entry must be present", logEntry)
        assertTrue(logEntry!!.contains("honors", ignoreCase = true))
        assertTrue(logEntry.contains("Academic Prodigy"))
    }

    // ==========================================
    // 6. Consequence Flags & Event Gating
    // ==========================================

    @Test
    fun testEventRepositoryGating_RequiresFlagAndConflictsWithFlag() {
        val flagEvent = LifeEvent(
            id = "flag_test_event",
            minAge = 0,
            maxAge = 100,
            text = "You cross paths again.",
            choices = emptyList(),
            tags = listOf("requires_flag_stoodUpToBully", "conflicts_with_flag_inJail")
        )

        val characterWithoutFlag = createYouthCharacter(age = 15)
        assertFalse(eventRepository.passesPrerequisiteEventGate(flagEvent, emptySet(), characterWithoutFlag))

        val characterWithFlag = characterWithoutFlag.addConsequenceFlag("stoodUpToBully")
        assertTrue(eventRepository.passesPrerequisiteEventGate(flagEvent, emptySet(), characterWithFlag))

        val characterInJail = characterWithFlag.addConsequenceFlag("inJail")
        assertFalse(eventRepository.passesPrerequisiteEventGate(flagEvent, emptySet(), characterInJail))
    }

    @Test
    fun testEventRepositoryGating_RequiresAptitudeAndClique() {
        val aptitudeEvent = LifeEvent(
            id = "aptitude_test_event",
            minAge = 0,
            maxAge = 100,
            text = "Hackathon opportunity.",
            choices = emptyList(),
            tags = listOf("requires_aptitude_TECH_TINKERING")
        )

        val nonTech = createYouthCharacter(age = 14, aptitude = ChildhoodAptitude.ATHLETICS)
        assertFalse(eventRepository.passesPrerequisiteEventGate(aptitudeEvent, emptySet(), nonTech))

        val techWhiz = createYouthCharacter(age = 14, aptitude = ChildhoodAptitude.TECH_TINKERING)
        assertTrue(eventRepository.passesPrerequisiteEventGate(aptitudeEvent, emptySet(), techWhiz))
    }
}
