package com.maisha.game.domain

import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.BenefactorFavorType
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.CommunityStandingTier
import com.maisha.game.data.model.FamilyAlliance
import com.maisha.game.data.model.FamilyFeud
import com.maisha.game.data.model.FamilyHeritageTag
import com.maisha.game.data.model.FeudAction
import com.maisha.game.data.model.Person
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.SocialBondType
import com.maisha.game.data.model.SocialEcosystemState
import com.maisha.game.util.clampStat
import com.maisha.game.util.formatMoney
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

sealed class FeudActionResult {
    data class Success(val character: Character, val message: String) : FeudActionResult()
    data class Failed(val character: Character, val reason: String) : FeudActionResult()
}

sealed class BenefactorFavorResult {
    data class Success(val character: Character, val message: String) : BenefactorFavorResult()
    data class Failed(val character: Character, val reason: String) : BenefactorFavorResult()
}

sealed class RivalryResolutionResult {
    data class Success(val character: Character, val message: String) : RivalryResolutionResult()
    data class Failed(val character: Character, val reason: String) : RivalryResolutionResult()
}

data class SocialEcosystemAgeUpResult(
    val character: Character,
    val notableIncident: String? = null
)

/**
 * Pure Kotlin domain engine simulating community reputation, family lineages, recurring rivals,
 * mentors, benefactors, and multi-generational feuds/alliances.
 */
@Singleton
class SocialEcosystemEngine @Inject constructor(
    private val financeEngine: FinanceEngine
) {

    constructor() : this(FinanceEngine())

    /**
     * Resolves annual shifts in community reputation, decays or escalates feuds, and refreshes favors.
     */
    fun ageUpSocialEcosystem(character: Character): SocialEcosystemAgeUpResult {
        if (!character.alive) {
            return SocialEcosystemAgeUpResult(character)
        }

        val state = character.socialEcosystem
        var standingScore = state.communityStandingScore
        val logLines = mutableListOf<String>()

        // 1. Natural drift based on behavior
        when {
            character.stats.karma >= 75 && !character.criminalRecord.hasRecord -> {
                standingScore = (standingScore + 3).coerceAtMost(100)
            }
            character.stats.karma <= 30 || character.criminalRecord.heatLevel >= 30 -> {
                standingScore = (standingScore - 5).coerceAtLeast(0)
            }
            character.criminalRecord.currentlyIncarcerated -> {
                standingScore = (standingScore - 8).coerceAtLeast(0)
            }
        }

        // Political bonus
        if (character.politics.currentOffice != null) {
            standingScore = (standingScore + 4).coerceAtMost(100)
        }

        // 2. Feuds progression
        val updatedFeuds = state.activeFeuds.map { feud ->
            val drift = if (standingScore < 40) 4 else -2
            val newIntensity = (feud.intensity + drift).coerceIn(10, 100)
            feud.copy(
                intensity = newIntensity,
                yearsActive = feud.yearsActive + 1
            )
        }.filter { it.intensity > 15 } // Dissolves if intensity drops below threshold

        // 3. Alliances progression
        val updatedAlliances = state.activeAlliances.map { alliance ->
            alliance.copy(yearsActive = alliance.yearsActive + 1)
        }

        // 4. Random dynamic spark (feud flare-up or benefactor dividend)
        var incidentText: String? = null
        if (updatedFeuds.isNotEmpty() && Random.nextFloat() < 0.25f) {
            val feud = updatedFeuds.random()
            incidentText = "The ${feud.rivalFamilyName} family circulated rumors challenging your household's standing."
            standingScore = (standingScore - 3).coerceAtLeast(0)
            logLines.add(incidentText)
        } else if (updatedAlliances.isNotEmpty() && Random.nextFloat() < 0.20f) {
            val ally = updatedAlliances.random()
            incidentText = "Patrons from the ${ally.allyFamilyName} family sent a generous seasonal tribute to your household."
            val giftAmount = EconomyScaler.scaleAmount(2_500, character.countryCode)
            val updatedMoney = character.stats.money + giftAmount
            logLines.add("$incidentText (+${formatMoney(giftAmount, character.countryCode)})")
            return SocialEcosystemAgeUpResult(
                character = character.copy(
                    stats = character.stats.copy(money = updatedMoney),
                    socialEcosystem = state.copy(
                        communityStandingScore = standingScore,
                        activeFeuds = updatedFeuds,
                        activeAlliances = updatedAlliances,
                        socialActionDoneThisYear = false,
                        neighborhoodFavorsRemaining = 1
                    ),
                    eventLog = EventLogCap.trim(logLines + character.eventLog)
                ),
                notableIncident = incidentText
            )
        }

        val updatedState = state.copy(
            communityStandingScore = standingScore,
            activeFeuds = updatedFeuds,
            activeAlliances = updatedAlliances,
            socialActionDoneThisYear = false,
            neighborhoodFavorsRemaining = 1
        )

        val updatedChar = character.copy(
            socialEcosystem = updatedState,
            eventLog = if (logLines.isNotEmpty()) EventLogCap.trim(logLines + character.eventLog) else character.eventLog
        )

        return SocialEcosystemAgeUpResult(updatedChar, incidentText)
    }

    /**
     * Resolves player actions toward an active family feud.
     */
    fun resolveFeudAction(character: Character, feudId: String, action: FeudAction): FeudActionResult {
        val state = character.socialEcosystem
        val feud = state.activeFeuds.find { it.id == feudId }
            ?: return FeudActionResult.Failed(character, "Feud not found.")

        if (state.socialActionDoneThisYear) {
            return FeudActionResult.Failed(character, "Social action already used this year.")
        }

        return when (action) {
            FeudAction.OFFER_OLIVE_BRANCH -> {
                val giftCost = EconomyScaler.scaleAmount(1_500, character.countryCode)
                if (character.stats.money < giftCost) {
                    return FeudActionResult.Failed(character, "Need ${formatMoney(giftCost, character.countryCode)} to host a reconciliation parley.")
                }
                val success = character.stats.karma >= 45 || Random.nextFloat() < 0.65f
                if (success) {
                    val remainingIntensity = feud.intensity - 35
                    val updatedFeuds = if (remainingIntensity <= 15) {
                        state.activeFeuds.filterNot { it.id == feudId }
                    } else {
                        state.activeFeuds.map { if (it.id == feudId) it.copy(intensity = remainingIntensity) else it }
                    }
                    val msg = if (remainingIntensity <= 15) {
                        "Olive branch accepted! The feud with the ${feud.rivalFamilyName} family has formally ended."
                    } else {
                        "Truce reached with the ${feud.rivalFamilyName} family. Feud intensity lowered significantly."
                    }
                    val updatedChar = character.copy(
                        stats = character.stats.copy(
                            money = character.stats.money - giftCost,
                            happiness = clampStat(character.stats.happiness + 8),
                            karma = clampStat(character.stats.karma + 6)
                        ),
                        socialEcosystem = state.copy(
                            communityStandingScore = (state.communityStandingScore + 5).coerceAtMost(100),
                            activeFeuds = updatedFeuds,
                            socialActionDoneThisYear = true
                        ),
                        eventLog = EventLogCap.prepend(character.eventLog, msg)
                    )
                    FeudActionResult.Success(updatedChar, msg)
                } else {
                    val msg = "The ${feud.rivalFamilyName} family rebuffed your peace offering and mocked the gesture."
                    val updatedChar = character.copy(
                        stats = character.stats.copy(
                            money = character.stats.money - giftCost,
                            happiness = clampStat(character.stats.happiness - 5)
                        ),
                        socialEcosystem = state.copy(socialActionDoneThisYear = true),
                        eventLog = EventLogCap.prepend(character.eventLog, msg)
                    )
                    FeudActionResult.Success(updatedChar, msg)
                }
            }
            FeudAction.ESCALATE_FEUD -> {
                val newIntensity = (feud.intensity + 25).coerceAtMost(100)
                val updatedFeuds = state.activeFeuds.map {
                    if (it.id == feudId) it.copy(intensity = newIntensity) else it
                }
                val msg = "You openly confronted the ${feud.rivalFamilyName} family in public. Hostilities have escalated!"
                val updatedChar = character.copy(
                    stats = character.stats.copy(
                        happiness = clampStat(character.stats.happiness - 4),
                        karma = clampStat(character.stats.karma - 8)
                    ),
                    socialEcosystem = state.copy(
                        communityStandingScore = (state.communityStandingScore - 6).coerceAtLeast(0),
                        activeFeuds = updatedFeuds,
                        socialActionDoneThisYear = true
                    ),
                    eventLog = EventLogCap.prepend(character.eventLog, msg)
                )
                FeudActionResult.Success(updatedChar, msg)
            }
            FeudAction.LEGAL_INJUNCTION -> {
                val legalFee = EconomyScaler.scaleAmount(3_000, character.countryCode)
                if (character.stats.money < legalFee) {
                    return FeudActionResult.Failed(character, "Need ${formatMoney(legalFee, character.countryCode)} for legal retainer fees.")
                }
                val newIntensity = (feud.intensity - 20).coerceAtLeast(10)
                val updatedFeuds = state.activeFeuds.map {
                    if (it.id == feudId) it.copy(intensity = newIntensity) else it
                }
                val msg = "Served legal cease-and-desist against the ${feud.rivalFamilyName} family. Provocations halted."
                val updatedChar = character.copy(
                    stats = character.stats.copy(money = character.stats.money - legalFee),
                    socialEcosystem = state.copy(
                        activeFeuds = updatedFeuds,
                        socialActionDoneThisYear = true
                    ),
                    eventLog = EventLogCap.prepend(character.eventLog, msg)
                )
                FeudActionResult.Success(updatedChar, msg)
            }
        }
    }

    /**
     * Calls in a favor from an allied house or benefactor.
     */
    fun callInBenefactorFavor(
        character: Character,
        allianceId: String,
        favorType: BenefactorFavorType
    ): BenefactorFavorResult {
        val state = character.socialEcosystem
        val alliance = state.activeAlliances.find { it.id == allianceId }
            ?: return BenefactorFavorResult.Failed(character, "Alliance not found.")

        if (state.neighborhoodFavorsRemaining <= 0) {
            return BenefactorFavorResult.Failed(character, "No family favors remaining this year.")
        }

        return when (favorType) {
            BenefactorFavorType.CAREER_RECOMMENDATION -> {
                if (character.career.currentJob == null) {
                    return BenefactorFavorResult.Failed(character, "Must be employed to leverage career endorsement.")
                }
                val updatedPerformance = (character.career.performance + 22f).coerceAtMost(100f)
                val msg = "The ${alliance.allyFamilyName} family endorsed your work! Performance boosted (+22)."
                val updatedChar = character.copy(
                    career = character.career.copy(performance = updatedPerformance),
                    socialEcosystem = state.copy(neighborhoodFavorsRemaining = state.neighborhoodFavorsRemaining - 1),
                    eventLog = EventLogCap.prepend(character.eventLog, msg)
                )
                BenefactorFavorResult.Success(updatedChar, msg)
            }
            BenefactorFavorType.FINANCIAL_BACKING -> {
                val grantAmount = EconomyScaler.scaleAmount(5_000, character.countryCode)
                val msg = "The ${alliance.allyFamilyName} family provided a patronage dividend (+${formatMoney(grantAmount, character.countryCode)})."
                val updatedChar = character.copy(
                    stats = character.stats.copy(money = character.stats.money + grantAmount),
                    socialEcosystem = state.copy(neighborhoodFavorsRemaining = state.neighborhoodFavorsRemaining - 1),
                    eventLog = EventLogCap.prepend(character.eventLog, msg)
                )
                BenefactorFavorResult.Success(updatedChar, msg)
            }
            BenefactorFavorType.COMMUNITY_DEFENSE -> {
                val newStanding = (state.communityStandingScore + 15).coerceAtMost(100)
                val msg = "The ${alliance.allyFamilyName} family issued a joint statement vouching for your character (+15 Standing)."
                val updatedChar = character.copy(
                    socialEcosystem = state.copy(
                        communityStandingScore = newStanding,
                        neighborhoodFavorsRemaining = state.neighborhoodFavorsRemaining - 1
                    ),
                    eventLog = EventLogCap.prepend(character.eventLog, msg)
                )
                BenefactorFavorResult.Success(updatedChar, msg)
            }
        }
    }

    /**
     * Resolves a rivalry encounter with a recurring rival person in the player's circle.
     */
    fun reconcileRivalry(character: Character, personId: String): RivalryResolutionResult {
        val person = character.family.find { it.id == personId }
            ?: return RivalryResolutionResult.Failed(character, "Person not found.")

        if (person.socialBond != SocialBondType.RECURRING_RIVAL && person.relation != RelationType.ENEMY) {
            return RivalryResolutionResult.Failed(character, "This person is not recognized as a rival.")
        }

        val success = character.stats.smarts >= 55 || character.stats.karma >= 50
        val updatedPerson = if (success) {
            person.copy(
                socialBond = SocialBondType.NONE,
                relation = RelationType.FRIEND,
                relationshipLevel = 55
            )
        } else {
            person.copy(
                relationshipLevel = (person.relationshipLevel - 8).coerceAtLeast(0)
            )
        }

        val updatedFamily = character.family.map { if (it.id == personId) updatedPerson else it }
        val msg = if (success) {
            "Shared a candid conversation with ${person.name}. Rivalry settled peacefully and mutual respect formed."
        } else {
            "Confrontation with ${person.name} backfired. Tensions linger."
        }

        val updatedChar = character.copy(
            family = updatedFamily,
            stats = character.stats.copy(happiness = clampStat(character.stats.happiness + if (success) 6 else -4)),
            eventLog = EventLogCap.prepend(character.eventLog, msg)
        )
        return RivalryResolutionResult.Success(updatedChar, msg)
    }

    /**
     * Calculates generational lineage heritage, carrying forward active feuds, alliances, and
     * establishing the heir's inherited community standing.
     */
    fun calculateLegacyHeritage(deceased: Character): SocialEcosystemState {
        val netWorth = financeEngine.calculateNetWorth(deceased)
        val karma = deceased.stats.karma
        val arrests = deceased.criminalRecord.timesArrested

        val heritage = when {
            arrests >= 2 || (deceased.criminalRecord.hasRecord && karma < 25) -> {
                FamilyHeritageTag.SCANDAL_TAINTED
            }
            netWorth >= 200_000 && karma >= 65 -> {
                FamilyHeritageTag.HONORED_PHILANTHROPISTS
            }
            netWorth >= 250_000 -> {
                FamilyHeritageTag.PROSPEROUS_DYNASTY
            }
            netWorth >= 100_000 && karma < 40 -> {
                FamilyHeritageTag.RUTHLESS_MOGULS
            }
            else -> {
                FamilyHeritageTag.HUMBLE_ROOTS
            }
        }

        val startingStanding = when (heritage) {
            FamilyHeritageTag.HONORED_PHILANTHROPISTS -> 75
            FamilyHeritageTag.PROSPEROUS_DYNASTY -> 68
            FamilyHeritageTag.HUMBLE_ROOTS -> 50
            FamilyHeritageTag.RUTHLESS_MOGULS -> 38
            FamilyHeritageTag.SCANDAL_TAINTED -> 26
        }

        // Active feuds with intensity >= 30 persist into heir's generation
        val inheritedFeuds = deceased.socialEcosystem.activeFeuds
            .filter { it.intensity >= 30 }
            .map { it.copy(ancestral = true, yearsActive = it.yearsActive + 1) }

        // Strong alliances with trust >= 50 persist into heir's generation
        val inheritedAlliances = deceased.socialEcosystem.activeAlliances
            .filter { it.trustLevel >= 50 }
            .map { it.copy(ancestral = true, yearsActive = it.yearsActive + 1) }

        return SocialEcosystemState(
            communityStandingScore = startingStanding,
            familyHeritage = heritage,
            activeFeuds = inheritedFeuds,
            activeAlliances = inheritedAlliances,
            socialActionDoneThisYear = false,
            neighborhoodFavorsRemaining = if (heritage == FamilyHeritageTag.HONORED_PHILANTHROPISTS) 2 else 1,
            pastAncestralVictories = deceased.socialEcosystem.pastAncestralVictories
        )
    }

    /**
     * Mortgage APR discount based on family standing (e.g., 0.5% discount for pillars of society).
     */
    fun getMortgageRateDiscount(character: Character): Double = when (character.socialEcosystem.currentStandingTier) {
        CommunityStandingTier.PILLAR_OF_SOCIETY -> 0.005
        CommunityStandingTier.WELL_RESPECTED -> 0.0025
        else -> 0.0
    }

    /**
     * Hiring success probability bonus or penalty based on community reputation.
     */
    fun getHireBonusPercentage(character: Character): Int = when (character.socialEcosystem.currentStandingTier) {
        CommunityStandingTier.PILLAR_OF_SOCIETY -> 15
        CommunityStandingTier.WELL_RESPECTED -> 8
        CommunityStandingTier.DISTRUSTED -> -10
        CommunityStandingTier.NOTORIOUS_PARIAH -> -20
        CommunityStandingTier.RESPECTABLE -> 0
    }
}
