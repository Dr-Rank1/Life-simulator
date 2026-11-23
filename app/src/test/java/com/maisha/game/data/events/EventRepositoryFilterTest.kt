package com.maisha.game.data.events

import com.maisha.game.data.model.EventChoice
import com.maisha.game.data.model.LifeEvent
import com.maisha.game.domain.FinanceEngine
import com.maisha.game.domain.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EventRepositoryFilterTest {

    private val repository = EventRepository.forTesting(FinanceEngine())

    @Test
    fun getEligibleEvents_excludesCountryRestrictedEventForWrongCountry() {
        val events = listOf(
            event("ke_only", restrictedToCountry = "KE"),
            event("global_event")
        )
        val repo = EventRepository.forTesting(FinanceEngine(), events)
        val ngCharacter = TestFixtures.character(age = 25, countryCode = "NG")

        val eligible = repo.getEligibleEvents(25, emptySet(), ngCharacter).map { it.id }

        assertFalse(eligible.contains("ke_only"))
        assertTrue(eligible.contains("global_event"))
    }

    @Test
    fun getEligibleEvents_includesCountryRestrictedEventForMatchingCountry() {
        val events = listOf(event("ke_only", restrictedToCountry = "KE"))
        val repo = EventRepository.forTesting(FinanceEngine(), events)
        val keCharacter = TestFixtures.character(age = 25, countryCode = "KE")

        val eligible = repo.getEligibleEvents(25, emptySet(), keCharacter).map { it.id }

        assertEquals(listOf("ke_only"), eligible)
    }

    @Test
    fun getEligibleEvents_excludesOneTimeEventsAlreadyTriggered() {
        val events = listOf(
            event("once", tags = listOf(EventRepository.ONE_TIME_TAG))
        )
        val repo = EventRepository.forTesting(FinanceEngine(), events)
        val character = TestFixtures.character(age = 30)

        val first = repo.getEligibleEvents(30, emptySet(), character)
        val second = repo.getEligibleEvents(30, setOf("once"), character)

        assertEquals(1, first.size)
        assertTrue(second.isEmpty())
    }

    @Test
    fun getEligibleEvents_excludesCareerSystemTaggedEvents() {
        val events = listOf(
            event("career_system_evt", tags = listOf(com.maisha.game.domain.CareerEngine.CAREER_SYSTEM_TAG)),
            event("player_career_evt", tags = listOf("career"))
        )
        val repo = EventRepository.forTesting(FinanceEngine(), events)
        val character = TestFixtures.character(age = 30)

        val eligible = repo.getEligibleEvents(30, emptySet(), character).map { it.id }

        assertFalse(eligible.contains("career_system_evt"))
        assertTrue(eligible.contains("player_career_evt"))
    }

    @Test
    fun getEligibleEvents_handlesMultiStepNarrativeArcs() {
        val step1 = event("arc_step_1", tags = listOf(EventRepository.ONE_TIME_TAG))
        val step2 = event("arc_step_2", tags = listOf(EventRepository.ONE_TIME_TAG, "requires_event_arc_step_1"))
        val step3 = event("arc_step_3", tags = listOf(EventRepository.ONE_TIME_TAG, "requires_event_arc_step_2"))
        val repo = EventRepository.forTesting(FinanceEngine(), listOf(step1, step2, step3))
        val character = TestFixtures.character(age = 25)

        // Initially only step 1 is eligible
        val initial = repo.getEligibleEvents(25, emptySet(), character).map { it.id }
        assertEquals(listOf("arc_step_1"), initial)

        // After step 1 is triggered (present in usedIds), step 1 is excluded and step 2 becomes eligible
        val afterStep1 = repo.getEligibleEvents(25, setOf("arc_step_1"), character).map { it.id }
        assertEquals(listOf("arc_step_2"), afterStep1)

        // After step 2 is triggered, step 3 becomes eligible
        val afterStep2 = repo.getEligibleEvents(25, setOf("arc_step_1", "arc_step_2"), character).map { it.id }
        assertEquals(listOf("arc_step_3"), afterStep2)

        // When all are completed, none are eligible
        val allDone = repo.getEligibleEvents(25, setOf("arc_step_1", "arc_step_2", "arc_step_3"), character).map { it.id }
        assertTrue(allDone.isEmpty())
    }

    @Test
    fun getEligibleEvents_excludesConflictingBranchEvents() {
        val eventA = event("branch_a", tags = listOf(EventRepository.ONE_TIME_TAG, "conflicts_with_event_branch_b"))
        val repo = EventRepository.forTesting(FinanceEngine(), listOf(eventA))
        val character = TestFixtures.character(age = 25)

        // If branch_b was triggered, eventA should not be eligible
        val blocked = repo.getEligibleEvents(25, setOf("branch_b"), character).map { it.id }
        assertTrue(blocked.isEmpty())

        // If branch_b was not triggered, eventA is eligible
        val allowed = repo.getEligibleEvents(25, emptySet(), character).map { it.id }
        assertEquals(listOf("branch_a"), allowed)
    }

    private fun event(
        id: String,
        restrictedToCountry: String? = null,
        tags: List<String> = emptyList()
    ): LifeEvent = LifeEvent(
        id = id,
        minAge = 18,
        maxAge = 99,
        text = id,
        choices = listOf(EventChoice(label = "OK", resultText = "Done")),
        tags = tags,
        weight = 1,
        restrictedToCountry = restrictedToCountry
    )
}
