package com.maisha.game.data.content

/**
 * Sync reader for parent/mentor advice. Gameplay never waits on the network —
 * implementations serve memory, Room, or [FallbackAdviceCatalog].
 */
interface AdviceCatalog {
    fun nextAdvice(): String
}

object FallbackAdviceCatalog : AdviceCatalog {
    val quotes: List<String> = listOf(
        "Measure twice, cut once.",
        "Sleep is a strategy, not a luxury.",
        "Ask for help before the problem grows teeth.",
        "Your name is worth more than a quick win.",
        "Show up even on the days you do not feel ready.",
        "Save a little whenever you can; future you is listening.",
        "Kindness is cheaper than regret.",
        "Learn one useful thing every year and you will never be stuck.",
        "Walk away from rooms that make you smaller.",
        "The boring choice is often the rich one later."
    )

    override fun nextAdvice(): String = quotes.random()
}
