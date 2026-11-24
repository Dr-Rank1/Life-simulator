// app/src/test/java/com/maisha/game/ui/avatar/ExpressionResolverTest.kt (new)
package com.maisha.game.ui.avatar

import com.maisha.game.data.model.Character
import com.maisha.game.data.model.Expression
import com.maisha.game.data.model.Gender
import com.maisha.game.data.model.Stats
import org.junit.Assert.assertEquals
import org.junit.Test

class ExpressionResolverTest {

    @Test
    fun lowHappiness_returnsSad() {
        val character = baseCharacter().copy(stats = Stats(happiness = 20))
        assertEquals(Expression.SAD, ExpressionResolver.resolveExpression(character, null))
    }

    @Test
    fun highHappiness_returnsHappy() {
        val character = baseCharacter().copy(stats = Stats(happiness = 85))
        assertEquals(Expression.HAPPY, ExpressionResolver.resolveExpression(character, null))
    }

    @Test
    fun positiveEventOutcome_returnsHappy() {
        val character = baseCharacter().copy(stats = Stats(happiness = 50))
        val expression = ExpressionResolver.resolveExpression(
            character,
            EventOutcome.Positive(magnitude = 2)
        )
        assertEquals(Expression.HAPPY, expression)
    }

    @Test
    fun conflictOutcome_returnsAngry() {
        val character = baseCharacter()
        val expression = ExpressionResolver.resolveExpression(
            character,
            EventOutcome.Negative(isConflict = true)
        )
        assertEquals(Expression.ANGRY, expression)
    }

    @Test
    fun personExpression_reflectsRelationshipLevel() {
        val happyPerson = com.maisha.game.domain.TestFixtures.person(relationshipLevel = 80)
        assertEquals(Expression.HAPPY, ExpressionResolver.resolvePersonExpression(happyPerson))

        val sadPerson = com.maisha.game.domain.TestFixtures.person(relationshipLevel = 25)
        assertEquals(Expression.SAD, ExpressionResolver.resolvePersonExpression(sadPerson))

        val neutralPerson = com.maisha.game.domain.TestFixtures.person(relationshipLevel = 50)
        assertEquals(Expression.NEUTRAL, ExpressionResolver.resolvePersonExpression(neutralPerson))
    }

    private fun baseCharacter() = Character(
        name = "Test",
        gender = Gender.MALE,
        birthYear = 2000,
        age = 20,
        stats = Stats()
    )
}
