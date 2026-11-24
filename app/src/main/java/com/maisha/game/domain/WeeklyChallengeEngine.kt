package com.maisha.game.domain

import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.AssetType
import com.maisha.game.data.model.CareerTrack
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.GraduationHonors
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.SchoolStage
import com.maisha.game.data.model.UniversityMajor
import com.maisha.game.util.clampStat
import com.maisha.game.util.formatMoney
import javax.inject.Inject
import javax.inject.Singleton

enum class RequirementUnit {
    COUNT,
    CURRENCY,
    PERCENT,
    LEVEL
}

data class ChallengeRequirement(
    val id: String,
    val titleKey: String,
    val current: Int,
    val target: Int,
    val unit: RequirementUnit = RequirementUnit.COUNT,
    val isCompleted: Boolean = current >= target
) {
    val progressFraction: Float
        get() = if (target <= 0) 1f else (current.toFloat() / target).coerceIn(0f, 1f)
}

data class ChallengeRequirementDef(
    val id: String,
    val titleKey: String,
    val target: Int,
    val unit: RequirementUnit = RequirementUnit.COUNT,
    val evaluator: (Character) -> Int
)

data class ChallengeReward(
    val ribbonKey: String,
    val cashKenya: Int = 20_000,
    val karmaBonus: Int = 5,
    val happinessBonus: Int = 10,
    val titleKey: String = ""
)

/** Real-world weekly challenge template (BitLife-style ribbon goals). */
data class WeeklyChallenge(
    val id: String,
    val titleKey: String,
    val descriptionKey: String,
    val ribbonKey: String,
    val reward: ChallengeReward = ChallengeReward(ribbonKey = ribbonKey),
    val requirementDefs: List<ChallengeRequirementDef> = emptyList()
)

data class WeeklyChallengeProgress(
    val challenge: WeeklyChallenge,
    val requirements: List<ChallengeRequirement>,
    val completedCount: Int,
    val totalCount: Int,
    val isAllCompleted: Boolean,
    val isClaimed: Boolean,
    val timeRemainingMillis: Long
) {
    val overallFraction: Float
        get() = if (totalCount <= 0) 1f else (completedCount.toFloat() / totalCount).coerceIn(0f, 1f)

    fun formattedTimeRemaining(): String {
        val totalSeconds = (timeRemainingMillis / 1000).coerceAtLeast(0)
        val days = totalSeconds / (24 * 3600)
        val hours = (totalSeconds % (24 * 3600)) / 3600
        val minutes = (totalSeconds % 3600) / 60
        return when {
            days > 0 -> "${days}d ${hours}h"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }
}

sealed class ClaimWeeklyChallengeResult {
    data class Success(
        val character: Character,
        val challenge: WeeklyChallenge,
        val scaledCashAwarded: Int,
        val ribbonKey: String
    ) : ClaimWeeklyChallengeResult()

    data object Ineligible : ClaimWeeklyChallengeResult()
    data object AlreadyClaimed : ClaimWeeklyChallengeResult()
}

/**
 * Rotating weekly multi-step goals stored app-wide via SettingsRepository.
 * Completion is evaluated against the active life in the current slot.
 */
@Singleton
class WeeklyChallengeEngine @Inject constructor() {

    fun currentChallenge(nowMillis: Long = System.currentTimeMillis()): WeeklyChallenge {
        val weekIndex = ((nowMillis / MILLIS_PER_WEEK).toInt() % CHALLENGES.size + CHALLENGES.size) % CHALLENGES.size
        return CHALLENGES[weekIndex]
    }

    fun timeRemainingMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        val weekStart = (nowMillis / MILLIS_PER_WEEK) * MILLIS_PER_WEEK
        val weekEnd = weekStart + MILLIS_PER_WEEK
        return (weekEnd - nowMillis).coerceAtLeast(0L)
    }

    fun isComplete(character: Character, challenge: WeeklyChallenge): Boolean {
        val defs = challenge.requirementDefs
        return if (defs.isNotEmpty()) {
            defs.all { def -> def.evaluator(character) >= def.target }
        } else {
            legacyIsComplete(character, challenge)
        }
    }

    fun evaluateProgress(
        character: Character,
        challenge: WeeklyChallenge,
        nowMillis: Long = System.currentTimeMillis(),
        isClaimed: Boolean = false
    ): WeeklyChallengeProgress {
        val reqs = challenge.requirementDefs.map { def ->
            val current = def.evaluator(character)
            ChallengeRequirement(
                id = def.id,
                titleKey = def.titleKey,
                current = current,
                target = def.target,
                unit = def.unit,
                isCompleted = current >= def.target
            )
        }
        val completedCount = reqs.count { it.isCompleted }
        val totalCount = reqs.size
        val isAllCompleted = totalCount > 0 && completedCount == totalCount
        val timeRemaining = timeRemainingMillis(nowMillis)

        return WeeklyChallengeProgress(
            challenge = challenge,
            requirements = reqs,
            completedCount = completedCount,
            totalCount = totalCount,
            isAllCompleted = isAllCompleted,
            isClaimed = isClaimed,
            timeRemainingMillis = timeRemaining
        )
    }

    fun claimReward(
        character: Character,
        challenge: WeeklyChallenge,
        isClaimed: Boolean
    ): ClaimWeeklyChallengeResult {
        if (!character.alive) return ClaimWeeklyChallengeResult.Ineligible
        if (isClaimed) return ClaimWeeklyChallengeResult.AlreadyClaimed
        if (!isComplete(character, challenge)) return ClaimWeeklyChallengeResult.Ineligible

        val scaledCash = EconomyScaler.scaleAmount(challenge.reward.cashKenya, character.countryCode)
        val updatedCharacter = character.copy(
            stats = character.stats.copy(
                money = character.stats.money + scaledCash,
                karma = clampStat(character.stats.karma + challenge.reward.karmaBonus),
                happiness = clampStat(character.stats.happiness + challenge.reward.happinessBonus)
            ),
            eventLog = EventLogCap.prepend(
                character.eventLog,
                "Claimed Weekly Challenge rewards for ${challenge.ribbonKey}! +${formatMoney(scaledCash, character.countryCode)}."
            )
        )

        return ClaimWeeklyChallengeResult.Success(
            character = updatedCharacter,
            challenge = challenge,
            scaledCashAwarded = scaledCash,
            ribbonKey = challenge.ribbonKey
        )
    }

    private fun legacyIsComplete(character: Character, challenge: WeeklyChallenge): Boolean =
        when (challenge.id) {
            "medical_residency" -> character.career.careerTrack == CareerTrack.MEDICAL &&
                character.career.trackLevel >= 2
            "legal_partner" -> character.career.careerTrack == CareerTrack.LEGAL &&
                character.career.trackLevel >= 2
            "adoptive_parent" -> character.family.any {
                it.relation == RelationType.CHILD && it.isAdopted
            }
            "home_improver" -> character.assets.any {
                it.type == AssetType.HOUSE && it.renovationLevel >= 1
            }
            "clean_slate" -> character.criminalRecord.recordExpunged
            "scholar" -> character.education.stage == SchoolStage.GRADUATED &&
                character.education.gpa >= 3.5f
            "teen_hustler" -> character.career.partTimeWorkedThisYear ||
                (character.age in 14..17 && character.stats.money >= 5_000)
            "portfolio_pro" -> character.investmentPortfolioValue >= 100_000
            else -> false
        }

    companion object {
        const val MILLIS_PER_WEEK = 7L * 24L * 60L * 60L * 1000L

        val CHALLENGES: List<WeeklyChallenge> = listOf(
            WeeklyChallenge(
                id = "medical_residency",
                titleKey = "challenge_medical_title",
                descriptionKey = "challenge_medical_desc",
                ribbonKey = "ribbon_healer",
                reward = ChallengeReward(
                    ribbonKey = "ribbon_healer",
                    cashKenya = 25_000,
                    karmaBonus = 6,
                    happinessBonus = 12,
                    titleKey = "title_master_healer"
                ),
                requirementDefs = listOf(
                    ChallengeRequirementDef(
                        id = "req_medical_degree",
                        titleKey = "req_medical_degree_title",
                        target = 1,
                        unit = RequirementUnit.COUNT,
                        evaluator = { c ->
                            if (c.education.stage == SchoolStage.GRADUATED &&
                                (c.education.universityMajor == UniversityMajor.MEDICINE || c.education.universityMajor == UniversityMajor.NURSING)
                            ) 1 else 0
                        }
                    ),
                    ChallengeRequirementDef(
                        id = "req_medical_track",
                        titleKey = "req_medical_track_title",
                        target = 2,
                        unit = RequirementUnit.LEVEL,
                        evaluator = { c ->
                            if (c.career.careerTrack == CareerTrack.MEDICAL) c.career.trackLevel else 0
                        }
                    ),
                    ChallengeRequirementDef(
                        id = "req_medical_health",
                        titleKey = "req_medical_health_title",
                        target = 80,
                        unit = RequirementUnit.PERCENT,
                        evaluator = { c -> c.stats.health }
                    )
                )
            ),
            WeeklyChallenge(
                id = "legal_partner",
                titleKey = "challenge_legal_title",
                descriptionKey = "challenge_legal_desc",
                ribbonKey = "ribbon_counsel",
                reward = ChallengeReward(
                    ribbonKey = "ribbon_counsel",
                    cashKenya = 30_000,
                    karmaBonus = 4,
                    happinessBonus = 10,
                    titleKey = "title_legal_ace"
                ),
                requirementDefs = listOf(
                    ChallengeRequirementDef(
                        id = "req_law_degree",
                        titleKey = "req_law_degree_title",
                        target = 1,
                        unit = RequirementUnit.COUNT,
                        evaluator = { c ->
                            if (c.education.stage == SchoolStage.GRADUATED &&
                                c.education.universityMajor == UniversityMajor.LAW
                            ) 1 else 0
                        }
                    ),
                    ChallengeRequirementDef(
                        id = "req_legal_track",
                        titleKey = "req_legal_track_title",
                        target = 2,
                        unit = RequirementUnit.LEVEL,
                        evaluator = { c ->
                            if (c.career.careerTrack == CareerTrack.LEGAL) c.career.trackLevel else 0
                        }
                    ),
                    ChallengeRequirementDef(
                        id = "req_legal_smarts",
                        titleKey = "req_legal_smarts_title",
                        target = 80,
                        unit = RequirementUnit.PERCENT,
                        evaluator = { c -> c.stats.smarts }
                    )
                )
            ),
            WeeklyChallenge(
                id = "adoptive_parent",
                titleKey = "challenge_adopt_title",
                descriptionKey = "challenge_adopt_desc",
                ribbonKey = "ribbon_guardian",
                reward = ChallengeReward(
                    ribbonKey = "ribbon_guardian",
                    cashKenya = 20_000,
                    karmaBonus = 10,
                    happinessBonus = 15,
                    titleKey = "title_devoted_guardian"
                ),
                requirementDefs = listOf(
                    ChallengeRequirementDef(
                        id = "req_adopt_child",
                        titleKey = "req_adopt_child_title",
                        target = 1,
                        unit = RequirementUnit.COUNT,
                        evaluator = { c ->
                            c.family.count { it.relation == RelationType.CHILD && it.isAdopted }
                        }
                    ),
                    ChallengeRequirementDef(
                        id = "req_adopt_bond",
                        titleKey = "req_adopt_bond_title",
                        target = 80,
                        unit = RequirementUnit.PERCENT,
                        evaluator = { c ->
                            c.family.filter { it.relation == RelationType.CHILD && it.isAdopted }
                                .maxOfOrNull { it.relationshipLevel } ?: 0
                        }
                    ),
                    ChallengeRequirementDef(
                        id = "req_own_home",
                        titleKey = "req_own_home_title",
                        target = 1,
                        unit = RequirementUnit.COUNT,
                        evaluator = { c -> c.assets.count { it.type == AssetType.HOUSE } }
                    )
                )
            ),
            WeeklyChallenge(
                id = "home_improver",
                titleKey = "challenge_renovate_title",
                descriptionKey = "challenge_renovate_desc",
                ribbonKey = "ribbon_architect",
                reward = ChallengeReward(
                    ribbonKey = "ribbon_architect",
                    cashKenya = 25_000,
                    karmaBonus = 4,
                    happinessBonus = 10,
                    titleKey = "title_property_mogul"
                ),
                requirementDefs = listOf(
                    ChallengeRequirementDef(
                        id = "req_own_home",
                        titleKey = "req_own_home_title",
                        target = 1,
                        unit = RequirementUnit.COUNT,
                        evaluator = { c -> c.assets.count { it.type == AssetType.HOUSE } }
                    ),
                    ChallengeRequirementDef(
                        id = "req_renovate_home",
                        titleKey = "req_renovate_home_title",
                        target = 1,
                        unit = RequirementUnit.COUNT,
                        evaluator = { c -> c.assets.count { it.type == AssetType.HOUSE && it.renovationLevel >= 1 } }
                    ),
                    ChallengeRequirementDef(
                        id = "req_accumulate_net_worth_100k",
                        titleKey = "req_net_worth_100k_title",
                        target = 100_000,
                        unit = RequirementUnit.CURRENCY,
                        evaluator = { c ->
                            val totalAssetVal = c.assets.sumOf { it.currentValue }
                            val totalMortgage = c.assets.sumOf { it.mortgageBalance }
                            (c.stats.money + totalAssetVal + c.investmentPortfolioValue + c.savingsBalance - totalMortgage).coerceAtLeast(0)
                        }
                    )
                )
            ),
            WeeklyChallenge(
                id = "clean_slate",
                titleKey = "challenge_expunge_title",
                descriptionKey = "challenge_expunge_desc",
                ribbonKey = "ribbon_redemption",
                reward = ChallengeReward(
                    ribbonKey = "ribbon_redemption",
                    cashKenya = 15_000,
                    karmaBonus = 15,
                    happinessBonus = 15,
                    titleKey = "title_redeemed_soul"
                ),
                requirementDefs = listOf(
                    ChallengeRequirementDef(
                        id = "req_expunged_record",
                        titleKey = "req_expunged_record_title",
                        target = 1,
                        unit = RequirementUnit.COUNT,
                        evaluator = { c -> if (c.criminalRecord.recordExpunged) 1 else 0 }
                    ),
                    ChallengeRequirementDef(
                        id = "req_high_karma",
                        titleKey = "req_high_karma_title",
                        target = 75,
                        unit = RequirementUnit.PERCENT,
                        evaluator = { c -> c.stats.karma }
                    ),
                    ChallengeRequirementDef(
                        id = "req_employed",
                        titleKey = "req_employed_title",
                        target = 1,
                        unit = RequirementUnit.COUNT,
                        evaluator = { c -> if (c.career.currentJob != null) 1 else 0 }
                    )
                )
            ),
            WeeklyChallenge(
                id = "scholar",
                titleKey = "challenge_scholar_title",
                descriptionKey = "challenge_scholar_desc",
                ribbonKey = "ribbon_valedictorian",
                reward = ChallengeReward(
                    ribbonKey = "ribbon_valedictorian",
                    cashKenya = 20_000,
                    karmaBonus = 5,
                    happinessBonus = 12,
                    titleKey = "title_valedictorian"
                ),
                requirementDefs = listOf(
                    ChallengeRequirementDef(
                        id = "req_high_gpa",
                        titleKey = "req_high_gpa_title",
                        target = 1,
                        unit = RequirementUnit.COUNT,
                        evaluator = { c ->
                            if (c.education.stage == SchoolStage.GRADUATED && c.education.gpa >= 3.5f) 1 else 0
                        }
                    ),
                    ChallengeRequirementDef(
                        id = "req_scholar_smarts",
                        titleKey = "req_scholar_smarts_title",
                        target = 85,
                        unit = RequirementUnit.PERCENT,
                        evaluator = { c -> c.stats.smarts }
                    ),
                    ChallengeRequirementDef(
                        id = "req_academic_honors",
                        titleKey = "req_academic_honors_title",
                        target = 1,
                        unit = RequirementUnit.COUNT,
                        evaluator = { c ->
                            if (c.education.graduationHonors != GraduationHonors.NONE ||
                                c.education.clubAwardsWon > 0 ||
                                c.education.clubLetterJacket
                            ) 1 else 0
                        }
                    )
                )
            ),
            WeeklyChallenge(
                id = "teen_hustler",
                titleKey = "challenge_teen_job_title",
                descriptionKey = "challenge_teen_job_desc",
                ribbonKey = "ribbon_hustler",
                reward = ChallengeReward(
                    ribbonKey = "ribbon_hustler",
                    cashKenya = 10_000,
                    karmaBonus = 4,
                    happinessBonus = 10,
                    titleKey = "title_young_entrepreneur"
                ),
                requirementDefs = listOf(
                    ChallengeRequirementDef(
                        id = "req_work_hustle",
                        titleKey = "req_work_hustle_title",
                        target = 1,
                        unit = RequirementUnit.COUNT,
                        evaluator = { c ->
                            if (c.career.partTimeWorkedThisYear ||
                                c.career.activePartTimeJob != null ||
                                c.career.sideHustleDoneThisYear
                            ) 1 else 0
                        }
                    ),
                    ChallengeRequirementDef(
                        id = "req_youth_savings",
                        titleKey = "req_youth_savings_title",
                        target = 5_000,
                        unit = RequirementUnit.CURRENCY,
                        evaluator = { c -> c.stats.money }
                    ),
                    ChallengeRequirementDef(
                        id = "req_pass_grades",
                        titleKey = "req_pass_grades_title",
                        target = 1,
                        unit = RequirementUnit.COUNT,
                        evaluator = { c -> if (c.education.gpa >= 2.5f) 1 else 0 }
                    )
                )
            ),
            WeeklyChallenge(
                id = "portfolio_pro",
                titleKey = "challenge_portfolio_title",
                descriptionKey = "challenge_portfolio_desc",
                ribbonKey = "ribbon_investor",
                reward = ChallengeReward(
                    ribbonKey = "ribbon_investor",
                    cashKenya = 50_000,
                    karmaBonus = 4,
                    happinessBonus = 10,
                    titleKey = "title_master_investor"
                ),
                requirementDefs = listOf(
                    ChallengeRequirementDef(
                        id = "req_portfolio_100k",
                        titleKey = "req_portfolio_100k_title",
                        target = 100_000,
                        unit = RequirementUnit.CURRENCY,
                        evaluator = { c -> c.investmentPortfolioValue }
                    ),
                    ChallengeRequirementDef(
                        id = "req_accumulate_net_worth_250k",
                        titleKey = "req_net_worth_250k_title",
                        target = 250_000,
                        unit = RequirementUnit.CURRENCY,
                        evaluator = { c ->
                            val totalAssetVal = c.assets.sumOf { it.currentValue }
                            val totalMortgage = c.assets.sumOf { it.mortgageBalance }
                            (c.stats.money + totalAssetVal + c.investmentPortfolioValue + c.savingsBalance - totalMortgage).coerceAtLeast(0)
                        }
                    ),
                    ChallengeRequirementDef(
                        id = "req_hold_savings",
                        titleKey = "req_hold_savings_title",
                        target = 20_000,
                        unit = RequirementUnit.CURRENCY,
                        evaluator = { c -> c.savingsBalance }
                    )
                )
            )
        )
    }
}
