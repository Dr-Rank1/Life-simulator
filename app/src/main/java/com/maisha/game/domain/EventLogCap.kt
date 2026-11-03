package com.maisha.game.domain

/**
 * Bounds serialized [Character.eventLog] growth for long-lived saves.
 * Newest entries are kept at the front; death markers are always preserved.
 */
object EventLogCap {
    const val MAX_ENTRIES = 150
    const val DEATH_MARKER_PREFIX = "::DEATH:"

    fun prepend(existing: List<String>, entry: String): List<String> =
        trim(listOf(entry) + existing)

    /**
     * Appends [entry] in natural chronological order at the end of the current year's block
     * (directly before any older "── Age " divider, or at the end of the log).
     * If no divider exists for [currentAge], one is prepended with [entry] underneath.
     */
    fun prependToCurrentYear(existing: List<String>, entry: String, currentAge: Int): List<String> {
        val currentHeader = "── Age $currentAge ──"
        val headerIndex = existing.indexOfFirst { it == currentHeader }
        return if (headerIndex >= 0) {
            val updated = existing.toMutableList()
            val nextHeaderRelative = existing.subList(headerIndex + 1, existing.size)
                .indexOfFirst { it.startsWith("── Age ") }
            val insertIndex = if (nextHeaderRelative >= 0) {
                headerIndex + 1 + nextHeaderRelative
            } else {
                existing.size
            }
            updated.add(insertIndex, entry)
            trim(updated)
        } else {
            trim(listOf(entry) + existing)
        }
    }

    fun ensureAgeHeader(existing: List<String>, currentAge: Int): List<String> {
        val currentHeader = "── Age $currentAge ──"
        return if (existing.none { it == currentHeader }) {
            trim(listOf(currentHeader) + existing)
        } else {
            existing
        }
    }

    fun append(existing: List<String>, entry: String): List<String> =
        trim(existing + entry)

    fun trim(log: List<String>): List<String> {
        if (log.size <= MAX_ENTRIES) return log
        val deathMarkers = log.filter { it.startsWith(DEATH_MARKER_PREFIX) }
        val regular = log.filterNot { it.startsWith(DEATH_MARKER_PREFIX) }
        val allowedRegular = (MAX_ENTRIES - deathMarkers.size).coerceAtLeast(0)
        return deathMarkers + regular.take(allowedRegular)
    }
}
