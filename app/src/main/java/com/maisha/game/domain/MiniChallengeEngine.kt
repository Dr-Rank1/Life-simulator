package com.maisha.game.domain

import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.CrimeTarget
import com.maisha.game.data.model.CrimeType
import com.maisha.game.data.model.Person
import com.maisha.game.data.model.StudyEffort
import com.maisha.game.util.clampRelationshipLevel
import com.maisha.game.util.clampStat
import com.maisha.game.util.formatMoney
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlin.random.Random

// 1. Exam & Academic Focus Check
enum class ExamFocusTactic {
    ANALYTICAL_PRECISION,
    INSTINCTIVE_SPEED,
    HIGH_STAKES_SHORTCUT
}

data class ExamFocusChallenge(
    val examTitle: String,
    val subjectSummary: String,
    val basePreparedness: Int,
    val baseSmarts: Int,
    val baseStress: Int
)

data class ExamFocusResult(
    val scoreDelta: Float,
    val stressDelta: Int,
    val panicReduced: Boolean,
    val consequenceFlag: String? = null,
    val outcomeMessage: String
)

// 2. Push-Your-Luck Crime & Risk Escalation
enum class PushLuckAction {
    PUSH_LUCK,
    CASH_OUT
}

data class CrimePushLuckSession(
    val crimeType: CrimeType? = null,
    val target: CrimeTarget? = null,
    val stage: Int = 1,
    val maxStages: Int = 3,
    val currentLoot: Int,
    val nextStageLoot: Int,
    val currentBustChance: Float,
    val accumulatedHeat: Int
)

sealed class PushLuckStepResult {
    data class PushedSuccess(val updatedSession: CrimePushLuckSession, val message: String) : PushLuckStepResult()
    data class CashedOut(val character: Character, val moneyGained: Int, val message: String) : PushLuckStepResult()
    data class Busted(val character: Character, val message: String) : PushLuckStepResult()
}

// 3. Negotiation & Persuasion Mini-Check
enum class NegotiationType {
    SALARY_RAISE,
    PROMOTION,
    PRENUP,
    CONFRONT_BULLY,
    EXPULSION_APPEAL
}

enum class NegotiationTactics {
    VALUE_METRICS,
    CHARISMA_EMPATHY,
    ASSERTIVE_LEVERAGE
}

data class NegotiationParty(
    val name: String,
    val roleTitle: String,
    val relationshipLevel: Int,
    val resistanceLevel: Int,
    val avatarSeed: String = ""
)

data class NegotiationChallenge(
    val type: NegotiationType,
    val party: NegotiationParty,
    val subjectLabel: String,
    val baselineRewardLabel: String
)

data class NegotiationOutcome(
    val success: Boolean,
    val character: Character,
    val bonusReward: Int = 0,
    val relationshipDelta: Int = 0,
    val message: String
)

@Singleton
class MiniChallengeEngine @Inject constructor(
    private val crimeEngine: CrimeEngine
) {
    constructor() : this(CrimeEngine())

    /**
     * Evaluates the player's 3-second exam mental tactic against their character stats,
     * study effort, and preparedness to grant score boosts, stress relief, or shortcut stakes.
     */
    fun evaluateExamFocus(
        character: Character,
        tactic: ExamFocusTactic,
        rapidResponseBonus: Boolean = false
    ): ExamFocusResult {
        val smartsFactor = character.stats.smarts / 100f
        val effortBoost = when (character.education.plannedStudyEffort) {
            StudyEffort.HARD -> 4f
            StudyEffort.NORMAL -> 2f
            StudyEffort.SLACK -> -2f
        }

        return when (tactic) {
            ExamFocusTactic.ANALYTICAL_PRECISION -> {
                val scoreBoost = 8f + smartsFactor * 6f + effortBoost + (if (rapidResponseBonus) 3f else 0f)
                ExamFocusResult(
                    scoreDelta = scoreBoost,
                    stressDelta = -15,
                    panicReduced = true,
                    outcomeMessage = "Analytical focus applied: Methodical problem-solving yielded steady marks and calmed exam panic."
                )
            }
            ExamFocusTactic.INSTINCTIVE_SPEED -> {
                val misreadChance = if (character.stats.smarts < 40) 0.25f else 0.05f
                val misread = Random.nextFloat() < misreadChance
                if (misread) {
                    ExamFocusResult(
                        scoreDelta = -4f,
                        stressDelta = 5,
                        panicReduced = false,
                        outcomeMessage = "Instinctive rush caused a misread on tricky logic questions (-4% score)."
                    )
                } else {
                    val scoreBoost = 12f + smartsFactor * 6f + effortBoost + (if (rapidResponseBonus) 5f else 0f)
                    ExamFocusResult(
                        scoreDelta = scoreBoost,
                        stressDelta = -6,
                        panicReduced = true,
                        outcomeMessage = "Rapid-fire instincts paid off! Finished with time to spare and high accuracy (+${scoreBoost.roundToInt()}% score)."
                    )
                }
            }
            ExamFocusTactic.HIGH_STAKES_SHORTCUT -> {
                val successChance = (smartsFactor * 0.70f + 0.20f).coerceIn(0.25f, 0.85f)
                val success = Random.nextFloat() < successChance
                if (success) {
                    val scoreBoost = 22f + (if (rapidResponseBonus) 4f else 0f)
                    ExamFocusResult(
                        scoreDelta = scoreBoost,
                        stressDelta = -20,
                        panicReduced = true,
                        outcomeMessage = "High-stakes gamble hit! You recalled a shortcut formula that cracked the hardest problem (+${scoreBoost.roundToInt()}% score)."
                    )
                } else {
                    ExamFocusResult(
                        scoreDelta = -14f,
                        stressDelta = 20,
                        panicReduced = false,
                        consequenceFlag = "examShortcutScrutiny",
                        outcomeMessage = "Shortcut backfired under proctor scrutiny! You panicked and lost precious time (-14% score)."
                    )
                }
            }
        }
    }

    /**
     * Initializes a multi-step Push-Your-Luck crime session.
     */
    fun startCrimePushLuck(
        character: Character,
        crimeType: CrimeType?,
        target: CrimeTarget? = null
    ): CrimePushLuckSession {
        val baseReward = when {
            target != null -> EconomyScaler.scaleAmount(target.baseReward, character.countryCode)
            crimeType != null -> crimeEngine.moneyReward(crimeType, character.countryCode)
            else -> 500
        }

        val smartsFactor = character.stats.smarts / 100f * 0.20f
        val heatPenalty = character.criminalRecord.heatLevel / 100f * 0.20f
        val attemptsPenalty = character.criminalRecord.crimeAttemptsThisYear * 0.10f

        val baseBustChance = when {
            target != null -> (target.difficulty * 0.45f - smartsFactor + heatPenalty + attemptsPenalty).coerceIn(0.12f, 0.40f)
            crimeType != null -> (1.0f - crimeEngine.successChance(character, crimeType, 0) + 0.05f).coerceIn(0.10f, 0.35f)
            else -> 0.20f
        }

        val initialLoot = (baseReward * 0.75f).roundToInt().coerceAtLeast(100)
        val nextLoot = (initialLoot * 1.8f).roundToInt()

        return CrimePushLuckSession(
            crimeType = crimeType,
            target = target,
            stage = 1,
            maxStages = 3,
            currentLoot = initialLoot,
            nextStageLoot = nextLoot,
            currentBustChance = baseBustChance,
            accumulatedHeat = 8
        )
    }

    /**
     * Resolves a step in the push-your-luck crime session:
     * - CASH_OUT: Pockets current accumulated loot and cleanly escapes with moderate heat.
     * - PUSH_LUCK: Rolls risk against current bust chance for higher loot multiplier.
     */
    fun resolvePushLuckStep(
        character: Character,
        session: CrimePushLuckSession,
        action: PushLuckAction
    ): PushLuckStepResult {
        if (action == PushLuckAction.CASH_OUT) {
            val updatedHeat = (character.criminalRecord.heatLevel + session.accumulatedHeat).coerceIn(0, 100)
            val updated = character.copy(
                stats = character.stats.copy(
                    money = character.stats.money + session.currentLoot,
                    happiness = clampStat(character.stats.happiness + 5)
                ),
                criminalRecord = character.criminalRecord.copy(
                    heatLevel = updatedHeat,
                    casedTarget = null
                ),
                eventLog = EventLogCap.prepend(
                    character.eventLog,
                    "Clean getaway! Slipped away with ${formatMoney(session.currentLoot, character.countryCode)}."
                )
            )
            val message = "Clean getaway! You pocketed ${formatMoney(session.currentLoot, character.countryCode)}."
            return PushLuckStepResult.CashedOut(updated, session.currentLoot, message)
        }

        // PUSH_LUCK: Check detection roll
        val busted = Random.nextFloat() < session.currentBustChance
        if (busted) {
            val crimeToArrest = session.crimeType ?: CrimeType.FRAUD
            val arrested = crimeEngine.processArrest(character, crimeToArrest)
            val finalHeat = (arrested.criminalRecord.heatLevel + 35).coerceIn(0, 100)
            val updated = arrested.copy(
                criminalRecord = arrested.criminalRecord.copy(
                    heatLevel = finalHeat,
                    casedTarget = null
                ),
                eventLog = EventLogCap.prepend(
                    arrested.eventLog,
                    "Greed was your downfall! Cornered by police while pushing for extra loot."
                )
            )
            return PushLuckStepResult.Busted(updated, "Busted by police while pushing for the extra score!")
        }

        // Successfully pushed to next stage!
        val nextStage = session.stage + 1
        val newCurrentLoot = session.nextStageLoot
        val multiplier = if (nextStage >= session.maxStages) 1.5f else 1.8f
        val newNextLoot = (newCurrentLoot * multiplier).roundToInt()
        val newBustChance = (session.currentBustChance + 0.22f).coerceAtMost(0.85f)
        val newHeat = session.accumulatedHeat + 14

        val updatedSession = session.copy(
            stage = nextStage,
            currentLoot = newCurrentLoot,
            nextStageLoot = newNextLoot,
            currentBustChance = newBustChance,
            accumulatedHeat = newHeat
        )

        val stageName = if (nextStage >= session.maxStages) "Final Jackpot" else "Round $nextStage"
        return PushLuckStepResult.PushedSuccess(
            updatedSession,
            "Success! Secured the $stageName stash: ${formatMoney(newCurrentLoot, character.countryCode)}."
        )
    }

    /**
     * Evaluates a player's interactive pitch in a high-stakes negotiation
     * (salary raise, promotion, prenup, or bully confrontation).
     */
    fun evaluateNegotiation(
        character: Character,
        challenge: NegotiationChallenge,
        tactic: NegotiationTactics
    ): NegotiationOutcome {
        val party = challenge.party

        return when (challenge.type) {
            NegotiationType.SALARY_RAISE -> {
                val currentJob = character.career.currentJob
                val baseSalary = currentJob?.baseSalary ?: 10_000
                val perf = character.career.performance
                val bossRel = party.relationshipLevel
                val strictness = party.resistanceLevel

                when (tactic) {
                    NegotiationTactics.VALUE_METRICS -> {
                        val score = perf * 0.65f + character.stats.smarts * 0.25f - strictness * 0.20f
                        val success = score >= 50f
                        if (success) {
                            val bump = (baseSalary * 0.08f).roundToInt()
                            val updatedJob = currentJob?.copy(baseSalary = baseSalary + bump)
                            val updated = character.copy(
                                career = character.career.copy(
                                    currentJob = updatedJob,
                                    askPromotionDoneThisYear = true,
                                    bossRelationship = clampBoss(bossRel + 3)
                                ),
                                stats = character.stats.copy(happiness = clampStat(character.stats.happiness + 6)),
                                eventLog = EventLogCap.prepend(
                                    character.eventLog,
                                    "Metrics-driven raise approved: +${formatMoney(bump, character.countryCode)}/year based on deliverables."
                                )
                            )
                            NegotiationOutcome(
                                success = true,
                                character = updated,
                                bonusReward = bump,
                                relationshipDelta = 3,
                                message = "Metrics pitch succeeded! Raise approved: +${formatMoney(bump, character.countryCode)}/year."
                            )
                        } else {
                            val updated = character.copy(
                                career = character.career.copy(
                                    askPromotionDoneThisYear = true,
                                    stress = clampStress(character.career.stress + 4f)
                                ),
                                stats = character.stats.copy(happiness = clampStat(character.stats.happiness - 2))
                            )
                            NegotiationOutcome(
                                success = false,
                                character = updated,
                                message = "${party.name} reviewed the metrics but found targets unmet this quarter."
                            )
                        }
                    }
                    NegotiationTactics.CHARISMA_EMPATHY -> {
                        val score = bossRel * 0.70f + character.stats.looks * 0.20f + character.stats.karma * 0.15f - strictness * 0.25f
                        val success = score >= 48f
                        if (success) {
                            val bump = (baseSalary * 0.06f).roundToInt()
                            val updatedJob = currentJob?.copy(baseSalary = baseSalary + bump)
                            val updated = character.copy(
                                career = character.career.copy(
                                    currentJob = updatedJob,
                                    askPromotionDoneThisYear = true,
                                    bossRelationship = clampBoss(bossRel + 8)
                                ),
                                stats = character.stats.copy(happiness = clampStat(character.stats.happiness + 8)),
                                eventLog = EventLogCap.prepend(
                                    character.eventLog,
                                    "${party.name} appreciated your dedication and granted a raise (+${formatMoney(bump, character.countryCode)}/year)."
                                )
                            )
                            NegotiationOutcome(
                                success = true,
                                character = updated,
                                bonusReward = bump,
                                relationshipDelta = 8,
                                message = "Charisma appeal resonated! +${formatMoney(bump, character.countryCode)}/year and warmer rapport."
                            )
                        } else {
                            val updated = character.copy(career = character.career.copy(askPromotionDoneThisYear = true))
                            NegotiationOutcome(
                                success = false,
                                character = updated,
                                message = "${party.name} smiled sympathetically but cited corporate budget constraints."
                            )
                        }
                    }
                    NegotiationTactics.ASSERTIVE_LEVERAGE -> {
                        val score = perf * 0.85f - strictness * 0.35f
                        val success = score >= 55f
                        if (success) {
                            val bump = (baseSalary * 0.14f).roundToInt()
                            val updatedJob = currentJob?.copy(baseSalary = baseSalary + bump)
                            val updated = character.copy(
                                career = character.career.copy(
                                    currentJob = updatedJob,
                                    askPromotionDoneThisYear = true,
                                    bossRelationship = clampBoss(bossRel - 2)
                                ),
                                stats = character.stats.copy(happiness = clampStat(character.stats.happiness + 10)),
                                eventLog = EventLogCap.prepend(
                                    character.eventLog,
                                    "Hard bargaining paid off! Won a top-tier raise of +${formatMoney(bump, character.countryCode)}/year."
                                )
                            )
                            NegotiationOutcome(
                                success = true,
                                character = updated,
                                bonusReward = bump,
                                relationshipDelta = -2,
                                message = "Bold leverage won! Top-tier raise: +${formatMoney(bump, character.countryCode)}/year."
                            )
                        } else {
                            val updated = character.copy(
                                career = character.career.copy(
                                    askPromotionDoneThisYear = true,
                                    bossRelationship = clampBoss(bossRel - 10),
                                    stress = clampStress(character.career.stress + 8f)
                                ),
                                stats = character.stats.copy(happiness = clampStat(character.stats.happiness - 5))
                            )
                            NegotiationOutcome(
                                success = false,
                                character = updated,
                                relationshipDelta = -10,
                                message = "Bold stance backfired! ${party.name} resented the ultimatum (-10 Boss Rapport)."
                            )
                        }
                    }
                }
            }

            NegotiationType.PRENUP -> {
                val crazy = party.resistanceLevel
                val love = party.relationshipLevel

                when (tactic) {
                    NegotiationTactics.CHARISMA_EMPATHY -> {
                        // Gentle romantic framing: protects both partners equally
                        val success = love >= 45 || crazy <= 70
                        if (success) {
                            NegotiationOutcome(
                                success = true,
                                character = character,
                                relationshipDelta = 5,
                                message = "${party.name} felt reassured by your loving framing and agreed to sign the prenuptial agreement."
                            )
                        } else {
                            val targetKey = party.avatarSeed.ifEmpty { party.name }
                            val updatedPartner = updateFamilyMemberRelationship(character, targetKey, -15)
                            NegotiationOutcome(
                                success = false,
                                character = updatedPartner,
                                relationshipDelta = -15,
                                message = "${party.name} was still uneasy and refused to sign."
                            )
                        }
                    }
                    NegotiationTactics.VALUE_METRICS -> {
                        // Objective legal & financial transparency
                        val success = crazy <= 55 && character.stats.smarts >= 50
                        if (success) {
                            NegotiationOutcome(
                                success = true,
                                character = character,
                                relationshipDelta = 0,
                                message = "${party.name} recognized the practical business wisdom and signed the prenuptial agreement."
                            )
                        } else {
                            val targetKey = party.avatarSeed.ifEmpty { party.name }
                            val updatedPartner = updateFamilyMemberRelationship(character, targetKey, -12)
                            NegotiationOutcome(
                                success = false,
                                character = updatedPartner,
                                relationshipDelta = -12,
                                message = "${party.name} found the cold legal metrics unromantic and refused to sign."
                            )
                        }
                    }
                    NegotiationTactics.ASSERTIVE_LEVERAGE -> {
                        // Non-negotiable condition
                        val success = crazy <= 30 && love >= 65
                        if (success) {
                            NegotiationOutcome(
                                success = true,
                                character = character,
                                relationshipDelta = -5,
                                message = "${party.name} reluctantly conceded and signed the prenuptial agreement."
                            )
                        } else {
                            val targetKey = party.avatarSeed.ifEmpty { party.name }
                            val updatedPartner = updateFamilyMemberRelationship(character, targetKey, -25)
                            NegotiationOutcome(
                                success = false,
                                character = updatedPartner,
                                relationshipDelta = -25,
                                message = "${party.name} was deeply offended by the ultimatum and called off marriage plans!"
                            )
                        }
                    }
                }
            }

            NegotiationType.CONFRONT_BULLY -> {
                val health = character.stats.health
                val smarts = character.stats.smarts

                when (tactic) {
                    NegotiationTactics.CHARISMA_EMPATHY -> {
                        val win = smarts >= 50 || character.stats.karma >= 55 || Random.nextFloat() < 0.50f
                        if (win) {
                            val updated = character.addConsequenceFlag("befriendedBully")
                            NegotiationOutcome(
                                success = true,
                                character = updated.copy(
                                    stats = updated.stats.copy(karma = clampStat(updated.stats.karma + 8))
                                ),
                                relationshipDelta = 25,
                                message = "You disarmed ${party.name} with empathy and shared snacks. The bullying has completely stopped."
                            )
                        } else {
                            NegotiationOutcome(
                                success = false,
                                character = character,
                                relationshipDelta = -5,
                                message = "${party.name} laughed off your peace offering: \"Save the tears for someone who cares!\""
                            )
                        }
                    }
                    NegotiationTactics.ASSERTIVE_LEVERAGE -> {
                        val win = health >= 55 || Random.nextFloat() < 0.55f
                        if (win) {
                            val updated = character.addConsequenceFlag("stoodUpToBully")
                            NegotiationOutcome(
                                success = true,
                                character = updated.copy(
                                    education = updated.education.copy(
                                        schoolReputation = clampStat(updated.education.schoolReputation + 10)
                                    )
                                ),
                                relationshipDelta = -10,
                                message = "You stood firm and stared down ${party.name}. They backed off and word spread of your courage (+10 Reputation)."
                            )
                        } else {
                            NegotiationOutcome(
                                success = false,
                                character = character.copy(
                                    stats = character.stats.copy(health = clampStat(character.stats.health - 4))
                                ),
                                relationshipDelta = -8,
                                message = "The confrontation turned physical and ${party.name} shoved you into lockers (-4 Health)."
                            )
                        }
                    }
                    NegotiationTactics.VALUE_METRICS -> {
                        // Documented incident report to staff
                        NegotiationOutcome(
                            success = true,
                            character = character.copy(
                                education = character.education.copy(
                                    schoolReputation = clampStat(character.education.schoolReputation + 2)
                                )
                            ),
                            relationshipDelta = -15,
                            message = "You presented documented incident times to the guidance counselor. Staff issued ${party.name} a formal warning."
                        )
                    }
                }
            }

            else -> {
                // Default fallback outcome
                NegotiationOutcome(
                    success = true,
                    character = character,
                    message = "Resolution concluded."
                )
            }
        }
    }

    private fun updateFamilyMemberRelationship(character: Character, targetKey: String, delta: Int): Character {
        val memberIndex = character.family.indexOfFirst { it.id == targetKey || it.name == targetKey }
        if (memberIndex == -1) return character
        val member = character.family[memberIndex]
        val updatedMember = member.copy(
            relationshipLevel = clampRelationshipLevel(member.relationshipLevel + delta)
        ).coerceRelationship()
        return character.copy(family = character.family.replaceAt(memberIndex, updatedMember))
    }

    private fun <T> List<T>.replaceAt(index: Int, item: T): List<T> =
        toMutableList().apply { set(index, item) }.toList()

    private fun clampBoss(value: Int): Int = value.coerceIn(-100, 100)
    private fun clampStress(value: Float): Float = value.coerceIn(0f, 100f)
}
