package com.maisha.game.domain

import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.ConfrontationChoice
import com.maisha.game.data.model.CrimeType
import com.maisha.game.data.model.DoubleLifeState
import com.maisha.game.data.model.ExposedSecretRecord
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.SchoolStage
import com.maisha.game.data.model.SecretConfrontation
import com.maisha.game.data.model.SecretIdentity
import com.maisha.game.data.model.SecretIdentityType
import com.maisha.game.util.clampRelationshipLevel
import com.maisha.game.util.clampStat
import com.maisha.game.util.formatMoney
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlin.random.Random

sealed class DoubleLifeActionResult {
    data class Success(val character: Character, val message: String) : DoubleLifeActionResult()
    data class Failed(val character: Character, val reason: String) : DoubleLifeActionResult()
}

data class ConfrontationResolution(
    val character: Character,
    val success: Boolean,
    val headline: String,
    val narrativeOutcome: String
)

@Singleton
class DoubleLifeEngine @Inject constructor(
    private val crimeEngine: CrimeEngine
) {
    constructor() : this(CrimeEngine())

    /**
     * Checks whether the character meets the prerequisites to adopt the requested secret identity.
     */
    fun canAdoptSecret(character: Character, type: SecretIdentityType): Pair<Boolean, String?> {
        if (!character.alive) return false to "Character is not alive."
        if (character.age < type.minAge) {
            return false to "Must be at least age ${type.minAge} to initiate this double life."
        }
        if (character.doubleLife.activeSecret != null) {
            return false to "Already leading an active secret identity (${character.doubleLife.activeSecret.codename})."
        }

        return when (type) {
            SecretIdentityType.UNDERCOVER_SYNDICATE -> {
                if (character.criminalRecord.currentlyIncarcerated) {
                    false to "Cannot run a street syndicate from prison."
                } else {
                    true to null
                }
            }
            SecretIdentityType.SECRET_ROMANCE -> {
                val hasCommittedPartner = character.family.any {
                    (it.relation == RelationType.SPOUSE || it.isMarried) && it.alive
                }
                if (!hasCommittedPartner) {
                    false to "Requires an existing marriage or committed partner to conceal from."
                } else {
                    true to null
                }
            }
            SecretIdentityType.SHADOW_OPERATOR -> {
                val hasPosition = character.career.currentJob != null ||
                    character.education.stage == SchoolStage.UNIVERSITY
                if (!hasPosition) {
                    false to "Requires employment or university enrollment to conduct espionage."
                } else {
                    true to null
                }
            }
            SecretIdentityType.HIDDEN_CREATOR,
            SecretIdentityType.BLACKMAIL_VAULT -> true to null
        }
    }

    /**
     * Initiates a new secret double life for the character.
     */
    fun adoptSecret(
        character: Character,
        type: SecretIdentityType,
        codename: String,
        coverStory: String? = null
    ): Character {
        val (eligible, _) = canAdoptSecret(character, type)
        if (!eligible) return character

        val basePayoff = when (type) {
            SecretIdentityType.UNDERCOVER_SYNDICATE -> 22_000
            SecretIdentityType.SECRET_ROMANCE -> 0
            SecretIdentityType.HIDDEN_CREATOR -> 9_500
            SecretIdentityType.SHADOW_OPERATOR -> 28_000
            SecretIdentityType.BLACKMAIL_VAULT -> 16_000
        }
        val scaledPayoff = EconomyScaler.scaleAmount(basePayoff, character.countryCode)

        val cleanCodename = codename.trim().ifEmpty { type.title }
        val cleanCover = coverStory?.trim()?.ifEmpty { type.defaultCoverStory } ?: type.defaultCoverStory

        val identity = SecretIdentity(
            type = type,
            codename = cleanCodename,
            coverStory = cleanCover,
            exposureRisk = 15,
            suspicionLevel = 20,
            alibiStrength = 80,
            annualPayoff = scaledPayoff,
            totalEarningsCollected = 0,
            yearsActive = 0,
            actionDoneThisYear = false
        )

        val updated = character.copy(
            doubleLife = character.doubleLife.copy(activeSecret = identity),
            eventLog = EventLogCap.prepend(
                character.eventLog,
                "Double Life initiated: Established covert identity '$cleanCodename' under cover of '$cleanCover'."
            )
        )
        return updated
    }

    /**
     * Plants alibis, decoys, and false schedules to buffer against exposure and drop suspicion.
     */
    fun plantAlibi(character: Character): DoubleLifeActionResult {
        val secret = character.doubleLife.activeSecret
            ?: return DoubleLifeActionResult.Failed(character, "No active secret identity to defend.")
        if (secret.actionDoneThisYear) {
            return DoubleLifeActionResult.Failed(character, "Cover action already used this year.")
        }

        val alibiCost = EconomyScaler.scaleAmount(1_200, character.countryCode)
        if (character.stats.money < alibiCost) {
            return DoubleLifeActionResult.Failed(
                character,
                "Need ${formatMoney(alibiCost, character.countryCode)} to arrange credible alibis."
            )
        }

        val updatedSecret = secret.copy(
            alibiStrength = (secret.alibiStrength + 35).coerceIn(0, 100),
            suspicionLevel = (secret.suspicionLevel - 20).coerceIn(0, 100),
            actionDoneThisYear = true
        )

        val updatedChar = character.copy(
            stats = character.stats.copy(money = character.stats.money - alibiCost),
            doubleLife = character.doubleLife.copy(
                activeSecret = updatedSecret,
                alibisPlantedTotal = character.doubleLife.alibisPlantedTotal + 1
            ),
            eventLog = EventLogCap.prepend(
                character.eventLog,
                "Planted alibis for '${secret.codename}': Fabricated receipts, decoy itineraries, and corroborating witnesses (-${formatMoney(alibiCost, character.countryCode)})."
            )
        )

        val msg = "Alibis planted successfully! Cover reinforced (+35 Alibi, -20 Suspicion)."
        return DoubleLifeActionResult.Success(updatedChar, msg)
    }

    /**
     * Destroys burner devices, cleans digital logs, and moves secret stashes to reduce exposure risk.
     */
    fun scrubTracks(character: Character): DoubleLifeActionResult {
        val secret = character.doubleLife.activeSecret
            ?: return DoubleLifeActionResult.Failed(character, "No active secret identity to scrub.")
        if (secret.actionDoneThisYear) {
            return DoubleLifeActionResult.Failed(character, "Cover action already used this year.")
        }

        val updatedSecret = secret.copy(
            exposureRisk = (secret.exposureRisk - 25).coerceIn(0, 100),
            alibiStrength = (secret.alibiStrength - 5).coerceIn(0, 100),
            actionDoneThisYear = true
        )

        val updatedChar = character.copy(
            stats = character.stats.copy(happiness = clampStat(character.stats.happiness - 2)),
            doubleLife = character.doubleLife.copy(activeSecret = updatedSecret),
            eventLog = EventLogCap.prepend(
                character.eventLog,
                "Scrubbed traces for '${secret.codename}': Destroyed burner logs, sanitized storage, and wiped surveillance trails."
            )
        )

        val msg = "Tracks scrubbed clean! Exposure risk dropped by 25%."
        return DoubleLifeActionResult.Success(updatedChar, msg)
    }

    /**
     * Exploits the secret identity for rapid cash, clout, or intimacy at the cost of elevated scrutiny.
     */
    fun exploitSecret(character: Character): DoubleLifeActionResult {
        val secret = character.doubleLife.activeSecret
            ?: return DoubleLifeActionResult.Failed(character, "No active secret identity to exploit.")
        if (secret.actionDoneThisYear) {
            return DoubleLifeActionResult.Failed(character, "Cover action already used this year.")
        }

        val isRomance = secret.type == SecretIdentityType.SECRET_ROMANCE
        val windfall = if (isRomance) 0 else (secret.annualPayoff * 1.4f).roundToInt().coerceAtLeast(2_000)

        val updatedSecret = secret.copy(
            suspicionLevel = (secret.suspicionLevel + 22).coerceIn(0, 100),
            exposureRisk = (secret.exposureRisk + 15).coerceIn(0, 100),
            alibiStrength = (secret.alibiStrength - 12).coerceIn(0, 100),
            totalEarningsCollected = secret.totalEarningsCollected + windfall,
            actionDoneThisYear = true
        )

        val updatedChar = character.copy(
            stats = character.stats.copy(
                money = character.stats.money + windfall,
                happiness = clampStat(character.stats.happiness + (if (isRomance) 12 else 6))
            ),
            doubleLife = character.doubleLife.copy(activeSecret = updatedSecret),
            eventLog = EventLogCap.prepend(
                character.eventLog,
                if (isRomance) {
                    "Secret rendezvous with '${secret.codename}': Thrilling intimacy, but alibis were stretched thin."
                } else {
                    "Exploited '${secret.codename}' operation: Collected +${formatMoney(windfall, character.countryCode)} in shadow returns."
                }
            )
        )

        val msg = if (isRomance) {
            "Secret romance rendezvous enjoyed (+12 Happiness, +22 Suspicion)."
        } else {
            "Exploited double life successfully: +${formatMoney(windfall, character.countryCode)} (+15 Exposure Risk)."
        }
        return DoubleLifeActionResult.Success(updatedChar, msg)
    }

    /**
     * Voluntarily ends the double life and comes clean before being forcefully busted.
     */
    fun comeClean(character: Character): Character {
        val secret = character.doubleLife.activeSecret ?: return character

        var working = character
        val summary: String

        when (secret.type) {
            SecretIdentityType.SECRET_ROMANCE -> {
                val partner = working.family.find {
                    (it.relation == RelationType.SPOUSE || it.isMarried) && it.alive
                }
                if (partner != null) {
                    val newRel = (partner.relationshipLevel - 28).coerceIn(0, 100)
                    val updatedFamily = working.family.map {
                        if (it.id == partner.id) it.copy(relationshipLevel = newRel) else it
                    }
                    working = working.copy(family = updatedFamily)
                }
                summary = "Came clean to partner about secret romance '${secret.codename}'. Avoided scandalous exposure, but trust took a heavy hit."
            }
            SecretIdentityType.UNDERCOVER_SYNDICATE -> {
                val cooledHeat = (working.criminalRecord.heatLevel - 25).coerceAtLeast(0)
                working = working.copy(criminalRecord = working.criminalRecord.copy(heatLevel = cooledHeat))
                summary = "Quietly dismantled street syndicate '${secret.codename}' and severed all underworld ties."
            }
            SecretIdentityType.SHADOW_OPERATOR -> {
                working = working.copy(career = working.career.copy(stress = (working.career.stress - 15f).coerceAtLeast(0f)))
                summary = "Ceased corporate espionage leaks as '${secret.codename}' and destroyed transmission keys."
            }
            SecretIdentityType.HIDDEN_CREATOR -> {
                summary = "Retired anonymous persona '${secret.codename}' peacefully before identity unmasking."
            }
            SecretIdentityType.BLACKMAIL_VAULT -> {
                summary = "Incinerated compromising blackmail dossier '${secret.codename}' to eliminate retaliation risk."
            }
        }

        val record = ExposedSecretRecord(
            type = secret.type,
            codename = secret.codename,
            yearExposed = working.age,
            falloutSummary = summary
        )

        return working.copy(
            doubleLife = working.doubleLife.copy(
                activeSecret = null,
                exposedHistory = working.doubleLife.exposedHistory + record
            ),
            eventLog = EventLogCap.prepend(working.eventLog, summary)
        )
    }

    /**
     * Advances the double life on annual age-up: checks scrutiny, decays alibis, collects passive proceeds,
     * and triggers confrontation when exposure cracks.
     */
    fun ageUpDoubleLife(character: Character): Pair<Character, SecretConfrontation?> {
        val secret = character.doubleLife.activeSecret ?: return character to null

        var working = character
        val isRomance = secret.type == SecretIdentityType.SECRET_ROMANCE
        val annualPayout = if (isRomance) 0 else secret.annualPayoff

        // 1. Natural Alibi Decay
        val decayedAlibi = (secret.alibiStrength - 18).coerceIn(0, 100)

        // 2. Cross-domain Scrutiny Spikes
        var addedExposure = 0
        var addedSuspicion = 0

        when (secret.type) {
            SecretIdentityType.UNDERCOVER_SYNDICATE -> {
                if (working.criminalRecord.heatLevel >= 30) {
                    addedExposure += (working.criminalRecord.heatLevel / 4)
                }
            }
            SecretIdentityType.SECRET_ROMANCE -> {
                val partner = working.family.find {
                    (it.relation == RelationType.SPOUSE || it.isMarried) && it.alive
                }
                if (partner != null && partner.crazy >= 50) {
                    addedSuspicion += 16
                }
            }
            SecretIdentityType.SHADOW_OPERATOR -> {
                if (working.career.stress >= 60 || working.career.performance < 45) {
                    addedSuspicion += 14
                }
            }
            SecretIdentityType.HIDDEN_CREATOR -> {
                if (working.career.fame >= 35) {
                    addedExposure += 12
                }
            }
            SecretIdentityType.BLACKMAIL_VAULT -> {
                if (working.stats.karma < 40) {
                    addedSuspicion += 15
                }
            }
        }

        // Low alibis cause exposure creep
        if (decayedAlibi < 30) {
            addedExposure += (30 - decayedAlibi) / 2
        }

        val newExposure = (secret.exposureRisk + addedExposure).coerceIn(0, 100)
        val newSuspicion = (secret.suspicionLevel + addedSuspicion).coerceIn(0, 100)

        val updatedSecret = secret.copy(
            exposureRisk = newExposure,
            suspicionLevel = newSuspicion,
            alibiStrength = decayedAlibi,
            totalEarningsCollected = secret.totalEarningsCollected + annualPayout,
            yearsActive = secret.yearsActive + 1,
            actionDoneThisYear = false
        )

        working = working.copy(
            stats = working.stats.copy(money = working.stats.money + annualPayout),
            doubleLife = working.doubleLife.copy(activeSecret = updatedSecret)
        )

        // 3. Crack & Confrontation Check
        val cracks = newExposure >= 82 || (newSuspicion >= 65 && Random.nextFloat() < 0.38f)
        if (cracks) {
            val (confrontedBy, reason) = when (secret.type) {
                SecretIdentityType.UNDERCOVER_SYNDICATE -> "Undercover Taskforce" to "Financial paper trail linked back to your accounts."
                SecretIdentityType.SECRET_ROMANCE -> "Suspicious Partner" to "Overheard phone call and mismatched schedule alibis."
                SecretIdentityType.SHADOW_OPERATOR -> "Corporate Security" to "Unusual data exports logged during off-hours."
                SecretIdentityType.HIDDEN_CREATOR -> "Investigative Journalist" to "Metadata leakage unmasked your anonymous alter-ego."
                SecretIdentityType.BLACKMAIL_VAULT -> "Target's Private Investigator" to "Confronted outside home with surveillance photos."
            }

            val bribeCost = EconomyScaler.scaleAmount(12_000, working.countryCode)
            val confrontation = SecretConfrontation(
                triggerReason = reason,
                confrontedBy = confrontedBy,
                severity = (newExposure * 0.7f + newSuspicion * 0.3f).roundToInt(),
                silenceBribeCost = bribeCost
            )

            val charWithPending = working.copy(
                doubleLife = working.doubleLife.copy(
                    activeSecret = updatedSecret.copy(pendingConfrontation = confrontation)
                ),
                eventLog = EventLogCap.prepend(
                    working.eventLog,
                    "CONFRONTATION: $confrontedBy confronted you regarding '${secret.codename}' ($reason)!"
                )
            )
            return charWithPending to confrontation
        }

        return working to null
    }

    /**
     * Resolves an active confrontation when a double life's cover cracks.
     */
    fun resolveConfrontation(
        character: Character,
        choice: ConfrontationChoice
    ): ConfrontationResolution {
        val secret = character.doubleLife.activeSecret
            ?: return ConfrontationResolution(character, false, "No Active Secret", "No active secret identity found.")
        val confrontation = secret.pendingConfrontation
            ?: return ConfrontationResolution(character, false, "No Confrontation", "No pending confrontation to resolve.")

        return when (choice) {
            ConfrontationChoice.DOUBLE_DOWN -> {
                val smartsScore = character.stats.smarts * 0.65f + character.stats.karma * 0.35f
                val difficulty = confrontation.severity * 0.55f + 25f
                val win = smartsScore >= difficulty || Random.nextFloat() < 0.30f

                if (win) {
                    val updatedSecret = secret.copy(
                        exposureRisk = 25,
                        suspicionLevel = 20,
                        alibiStrength = 65,
                        pendingConfrontation = null
                    )
                    val updated = character.copy(
                        doubleLife = character.doubleLife.copy(
                            activeSecret = updatedSecret,
                            successfulBluffs = character.doubleLife.successfulBluffs + 1
                        ),
                        eventLog = EventLogCap.prepend(
                            character.eventLog,
                            "Masterful Bluff: You turned the tables on ${confrontation.confrontedBy}, deflecting suspicion and cementing your cover story."
                        )
                    )
                    ConfrontationResolution(
                        character = updated,
                        success = true,
                        headline = "Cover Restored",
                        narrativeOutcome = "Your cool composure and counter-evidence convinced ${confrontation.confrontedBy}. Suspicion has collapsed back to safe levels."
                    )
                } else {
                    // Catastrophic failure
                    val falloutChar = applyExposureFallout(character, secret, confrontation)
                    ConfrontationResolution(
                        character = falloutChar,
                        success = false,
                        headline = "Bluff Shattered",
                        narrativeOutcome = "Your story collapsed under hard proof! ${confrontation.confrontedBy} blew your cover wide open."
                    )
                }
            }

            ConfrontationChoice.BRIBE_SILENCE -> {
                val cost = confrontation.silenceBribeCost
                if (character.stats.money >= cost) {
                    val updatedSecret = secret.copy(
                        exposureRisk = 20,
                        suspicionLevel = 15,
                        alibiStrength = 55,
                        pendingConfrontation = null
                    )
                    val updated = character.copy(
                        stats = character.stats.copy(money = character.stats.money - cost),
                        doubleLife = character.doubleLife.copy(activeSecret = updatedSecret),
                        eventLog = EventLogCap.prepend(
                            character.eventLog,
                            "Bought Silence: Paid ${formatMoney(cost, character.countryCode)} to bury evidence of '${secret.codename}'."
                        )
                    )
                    ConfrontationResolution(
                        character = updated,
                        success = true,
                        headline = "Silence Purchased",
                        narrativeOutcome = "The hush money was accepted. Key evidence was quietly handed over and burned."
                    )
                } else {
                    // Cannot afford bribe, falls back to failed exposure
                    val falloutChar = applyExposureFallout(character, secret, confrontation)
                    ConfrontationResolution(
                        character = falloutChar,
                        success = false,
                        headline = "Bribe Failed",
                        narrativeOutcome = "Insufficient funds to satisfy the silence demand. Evidence was leaked immediately!"
                    )
                }
            }

            ConfrontationChoice.CUT_TIES_VANISH -> {
                val escapeCost = (character.stats.money * 0.35f).roundToInt()
                val summary = "Vanished into the night: Abandoned '${secret.codename}' and fled the scene (-${formatMoney(escapeCost, character.countryCode)})."
                val record = ExposedSecretRecord(
                    type = secret.type,
                    codename = secret.codename,
                    yearExposed = character.age,
                    falloutSummary = summary
                )
                val updated = character.copy(
                    stats = character.stats.copy(
                        money = (character.stats.money - escapeCost).coerceAtLeast(0),
                        happiness = clampStat(character.stats.happiness - 15)
                    ),
                    doubleLife = character.doubleLife.copy(
                        activeSecret = null,
                        exposedHistory = character.doubleLife.exposedHistory + record
                    ),
                    eventLog = EventLogCap.prepend(character.eventLog, summary)
                )
                ConfrontationResolution(
                    character = updated,
                    success = true,
                    headline = "Identity Burned",
                    narrativeOutcome = "You torched all equipment, abandoned shadow assets, and disappeared before authorities or partners could corner you."
                )
            }

            ConfrontationChoice.CONFESS_FALLOUT -> {
                val falloutChar = applyExposureFallout(character, secret, confrontation, confessed = true)
                ConfrontationResolution(
                    character = falloutChar,
                    success = false,
                    headline = "Confession Handed Over",
                    narrativeOutcome = "You laid all facts bare. You face the consequences honestly without evasion."
                )
            }
        }
    }

    private fun applyExposureFallout(
        character: Character,
        secret: SecretIdentity,
        confrontation: SecretConfrontation,
        confessed: Boolean = false
    ): Character {
        var working = character
        val prefix = if (confessed) "Confessed" else "Exposed"

        when (secret.type) {
            SecretIdentityType.UNDERCOVER_SYNDICATE -> {
                working = crimeEngine.processArrest(working, CrimeType.FRAUD)
            }
            SecretIdentityType.SECRET_ROMANCE -> {
                val partner = working.family.find {
                    (it.relation == RelationType.SPOUSE || it.isMarried) && it.alive
                }
                if (partner != null) {
                    val updatedFamily = working.family.map {
                        if (it.id == partner.id) it.copy(relationshipLevel = 0, isMarried = false) else it
                    }
                    working = working.copy(
                        family = updatedFamily,
                        stats = working.stats.copy(happiness = clampStat(working.stats.happiness - 35))
                    )
                }
            }
            SecretIdentityType.SHADOW_OPERATOR -> {
                working = working.copy(
                    career = working.career.copy(
                        currentJob = null,
                        stress = (working.career.stress + 20f).coerceIn(0f, 100f)
                    ),
                    stats = working.stats.copy(happiness = clampStat(working.stats.happiness - 25))
                )
            }
            SecretIdentityType.HIDDEN_CREATOR,
            SecretIdentityType.BLACKMAIL_VAULT -> {
                working = working.copy(
                    stats = working.stats.copy(
                        karma = clampStat(working.stats.karma - 25),
                        happiness = clampStat(working.stats.happiness - 20)
                    )
                )
            }
        }

        val summary = "$prefix as '${secret.codename}' by ${confrontation.confrontedBy}: ${confrontation.triggerReason}"
        val record = ExposedSecretRecord(
            type = secret.type,
            codename = secret.codename,
            yearExposed = working.age,
            falloutSummary = summary
        )

        return working.copy(
            doubleLife = working.doubleLife.copy(
                activeSecret = null,
                exposedHistory = working.doubleLife.exposedHistory + record
            ),
            eventLog = EventLogCap.prepend(working.eventLog, "EXPOSED: $summary")
        )
    }
}
