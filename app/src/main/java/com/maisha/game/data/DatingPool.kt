// app/src/main/java/com/maisha/game/data/DatingPool.kt (modified — cross-country prospects)
package com.maisha.game.data

import com.maisha.game.data.model.Character
import com.maisha.game.data.model.Person
import com.maisha.game.domain.PersonGenerator

object DatingPool {

    private const val POOL_SIZE = 20
    private const val PROSPECT_COUNT = 3
    private const val MIN_DATING_AGE = 18

    fun generateProspects(character: Character): List<Person> {
        if (character.age < MIN_DATING_AGE) return emptyList()
        return (0 until PROSPECT_COUNT).map {
            PersonGenerator.buildDatingProspect(character, MIN_DATING_AGE)
        }
    }

    fun generateMatchmakerCandidates(
        character: Character,
        minAge: Int,
        maxAge: Int,
        wealthTier: Int
    ): List<Person> {
        if (character.age < MIN_DATING_AGE) return emptyList()
        return (0 until 4).map {
            PersonGenerator.buildDatingProspect(
                character = character,
                minDatingAge = MIN_DATING_AGE,
                targetMinAge = minAge,
                targetMaxAge = maxAge,
                targetWealthTier = wealthTier
            )
        }
    }
}
