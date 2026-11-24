// app/src/main/java/com/maisha/game/domain/CrimeEngine.kt
package com.maisha.game.domain

import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.CrimeTarget
import com.maisha.game.data.model.CrimeType
import com.maisha.game.data.model.CriminalRecord
import com.maisha.game.data.model.LawyerTier
import com.maisha.game.data.model.PrisonActivity
import com.maisha.game.util.clampStat
import com.maisha.game.util.formatMoney
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

sealed class CrimeResult {
    data class Success(val character: Character, val moneyGained: Int) : CrimeResult()
    data class Caught(val character: Character) : CrimeResult()
}

sealed class TrialResult {
    data class Sentenced(val character: Character, val sentenceYears: Int) : TrialResult()
    data class Acquitted(val character: Character) : TrialResult()
    data object Ineligible : TrialResult()
}

sealed class PrisonActivityResult {
    data class Success(val character: Character) : PrisonActivityResult()
    data object Ineligible : PrisonActivityResult()
}

sealed class ExpungementResult {
    data class Success(val character: Character) : ExpungementResult()
    data class Denied(val character: Character) : ExpungementResult()
    data object Ineligible : ExpungementResult()
    data object InsufficientFunds : ExpungementResult()
}

@Singleton
class CrimeEngine @Inject constructor() {

    /**
     * Attempts a crime if the character is at least [MIN_CRIME_AGE], not incarcerated, and not awaiting trial.
     *
     * Success chance scales with smarts; failure routes through [processArrest].
     */
    fun attemptCrime(character: Character, crimeType: CrimeType): CrimeResult {
        val record = character.criminalRecord
        if (record.currentlyIncarcerated || record.awaitingTrial) {
            return CrimeResult.Caught(character)
        }
        if (character.age < MIN_CRIME_AGE) {
            return CrimeResult.Caught(character)
        }

        val attemptsSoFar = record.crimeAttemptsThisYear
        val chance = successChance(character, crimeType, attemptsSoFar)
        val result = if (Random.nextFloat() < chance) {
            val moneyGained = moneyReward(crimeType, character.countryCode)
            val updatedHeat = (record.heatLevel + 8).coerceIn(0, 100)
            val updated = character.copy(
                stats = character.stats.copy(
                    money = character.stats.money + moneyGained
                ),
                criminalRecord = record.copy(heatLevel = updatedHeat)
            )
            CrimeResult.Success(updated, moneyGained)
        } else {
            val arrested = processArrest(character, crimeType)
            val updatedHeat = (arrested.criminalRecord.heatLevel + 30).coerceIn(0, 100)
            CrimeResult.Caught(arrested.copy(criminalRecord = arrested.criminalRecord.copy(heatLevel = updatedHeat)))
        }
        return recordCrimeAttempt(result)
    }

    /**
     * Executes a specialized crime target (Burglary, GTA, Heist).
     * Success scales with smarts, notoriety, and prior casing reconnaissance.
     */
    fun attemptTargetedCrime(character: Character, target: CrimeTarget): CrimeResult {
        val record = character.criminalRecord
        val minAge = when (target.category) {
            "Armed Heist" -> 18
            else -> 16
        }
        if (record.currentlyIncarcerated || record.awaitingTrial || character.age < minAge) {
            return CrimeResult.Caught(character)
        }

        val smartsFactor = character.stats.smarts / 100f * 0.35f
        val notorietyFactor = (record.notoriety / 100f) * 0.15f
        val attemptPenalty = (record.crimeAttemptsThisYear * 0.15f)
        val heatPenalty = (record.heatLevel / 100f * 0.25f)
        val casedBonus = if (record.casedTarget == target) 0.20f else 0.0f
        val chance = (1.0f - target.difficulty + smartsFactor + notorietyFactor + casedBonus - attemptPenalty - heatPenalty).coerceIn(0.08f, 0.90f)

        val heatGain = when (target.category) {
            "Armed Heist" -> 30
            else -> 15
        }

        val result = if (Random.nextFloat() < chance) {
            val reward = EconomyScaler.scaleAmount(target.baseReward, character.countryCode)
            val updatedNotoriety = (record.notoriety + target.notorietyGain).coerceIn(0, 100)
            val updatedHeat = (record.heatLevel + heatGain).coerceIn(0, 100)
            val updated = character.copy(
                stats = character.stats.copy(
                    money = character.stats.money + reward,
                    happiness = clampStat(character.stats.happiness + 8)
                ),
                criminalRecord = record.copy(
                    notoriety = updatedNotoriety,
                    heatLevel = updatedHeat,
                    casedTarget = null
                ),
                eventLog = EventLogCap.prepend(
                    character.eventLog,
                    "Pulled off the ${target.category} target (${target.name.lowercase().replace('_', ' ')})! Scored ${formatMoney(reward, character.countryCode)}."
                )
            )
            CrimeResult.Success(updated, reward)
        } else {
            val arrested = processArrest(character, CrimeType.FRAUD)
            val updatedHeat = (arrested.criminalRecord.heatLevel + 40).coerceIn(0, 100)
            CrimeResult.Caught(
                arrested.copy(
                    criminalRecord = arrested.criminalRecord.copy(
                        heatLevel = updatedHeat,
                        casedTarget = null
                    ),
                    eventLog = EventLogCap.prepend(
                        arrested.eventLog,
                        "Caught during attempted ${target.category}! The police surrounded the scene."
                    )
                )
            )
        }
        return recordCrimeAttempt(result)
    }

    /**
     * Cases out a specialized target in advance, mapping security to increase execution odds.
     */
    fun caseTarget(character: Character, target: CrimeTarget): Character {
        val record = character.criminalRecord
        val minAge = when (target.category) {
            "Armed Heist" -> 18
            else -> 16
        }
        if (record.currentlyIncarcerated || record.awaitingTrial || character.age < minAge) {
            return character
        }
        val updatedHeat = (record.heatLevel + 5).coerceIn(0, 100)
        return character.copy(
            criminalRecord = record.copy(
                casedTarget = target,
                heatLevel = updatedHeat
            ),
            eventLog = EventLogCap.prepend(
                character.eventLog,
                "Cased out the ${target.category} target (${target.name.lowercase().replace('_', ' ')}). Security rhythms mapped (+Planning bonus, +5 Heat)."
            )
        )
    }

    /**
     * Burns off police heat by staying off the streets in a safehouse.
     */
    fun layLow(character: Character): Character {
        val record = character.criminalRecord
        if (record.currentlyIncarcerated || record.awaitingTrial || record.heatActionDoneThisYear) {
            return character
        }
        val cooledHeat = (record.heatLevel - 35).coerceAtLeast(0)
        return character.copy(
            criminalRecord = record.copy(
                heatLevel = cooledHeat,
                heatActionDoneThisYear = true
            ),
            eventLog = EventLogCap.prepend(
                character.eventLog,
                "Laid low in a safehouse off the radar. Police heat cooled down (-35 Heat)."
            )
        )
    }

    /**
     * Bribes an underworld informant to scrub tips and misdirect police inquiries.
     */
    fun bribeInformant(character: Character): Character {
        val record = character.criminalRecord
        if (record.currentlyIncarcerated || record.awaitingTrial || record.heatActionDoneThisYear) {
            return character
        }
        val bribeCost = EconomyScaler.scaleAmount(12_000, character.countryCode)
        if (character.stats.money < bribeCost) return character

        val cooledHeat = (record.heatLevel - 60).coerceAtLeast(0)
        return character.copy(
            stats = character.stats.copy(money = character.stats.money - bribeCost),
            criminalRecord = record.copy(
                heatLevel = cooledHeat,
                heatActionDoneThisYear = true
            ),
            eventLog = EventLogCap.prepend(
                character.eventLog,
                "Bribed an underworld informant with ${formatMoney(bribeCost, character.countryCode)} to misdirect police investigations (-60 Heat)."
            )
        )
    }

    /**
     * Cools down heat and resets yearly crime limits at age-up.
     */
    fun coolDownYear(character: Character): Character {
        val record = character.criminalRecord
        val newHeat = (record.heatLevel - 20).coerceAtLeast(0)
        return character.copy(
            criminalRecord = record.copy(
                heatLevel = newHeat,
                heatActionDoneThisYear = false,
                crimeAttemptsThisYear = 0
            )
        )
    }

    private fun recordCrimeAttempt(result: CrimeResult): CrimeResult {
        val bumped = { character: Character ->
            character.copy(
                criminalRecord = character.criminalRecord.copy(
                    crimeAttemptsThisYear = character.criminalRecord.crimeAttemptsThisYear + 1
                )
            )
        }
        return when (result) {
            is CrimeResult.Success -> CrimeResult.Success(bumped(result.character), result.moneyGained)
            is CrimeResult.Caught -> CrimeResult.Caught(bumped(result.character))
        }
    }

    /**
     * Records arrest and enters [CriminalRecord.awaitingTrial]; sentencing is deferred to [goToTrial].
     */
    fun processArrest(character: Character, crimeType: CrimeType): Character {
        val record = character.criminalRecord
        val newTimesArrested = record.timesArrested + 1
        val updatedRecord = record.copy(
            hasRecord = true,
            timesArrested = newTimesArrested,
            awaitingTrial = true,
            pendingCrimeType = crimeType.name,
            currentlyIncarcerated = false,
            yearsRemaining = 0,
            totalSentenceYears = 0,
            yearsServed = 0,
            negativePrisonEvents = 0,
            paroleBonus = 0,
            lastArrestAge = character.age
        )
        val logEntry = when (crimeType) {
            CrimeType.PICKPOCKET -> "Caught pickpocketing. Awaiting trial."
            CrimeType.SHOPLIFT -> "Caught shoplifting. Awaiting trial."
            CrimeType.FRAUD -> "Caught committing fraud. Awaiting trial."
        }
        return character.copy(
            criminalRecord = updatedRecord,
            career = character.career.currentJob?.let { job ->
                character.career.copy(
                    currentJob = null,
                    yearsAtCurrentJob = 0,
                    jobHistory = character.career.jobHistory + job.title
                )
            } ?: character.career,
            eventLog = EventLogCap.prepend(character.eventLog, logEntry),
            stats = character.stats.copy(
                happiness = clampStat(character.stats.happiness - 12)
            )
        )
    }

    fun lawyerFee(lawyerTier: LawyerTier, netWorth: Int): Int = when (lawyerTier) {
        LawyerTier.PUBLIC_DEFENDER -> 0
        LawyerTier.AVERAGE -> (netWorth * AVERAGE_LAWYER_NET_WORTH_RATE).roundToInt().coerceAtLeast(AVERAGE_LAWYER_MIN_FEE)
        LawyerTier.EXPENSIVE -> (netWorth * EXPENSIVE_LAWYER_NET_WORTH_RATE).roundToInt().coerceAtLeast(EXPENSIVE_LAWYER_MIN_FEE)
    }

    fun canAffordLawyer(character: Character, lawyerTier: LawyerTier, netWorth: Int): Boolean =
        character.stats.money >= lawyerFee(lawyerTier, netWorth)

    /**
     * Resolves a pending trial with the chosen [lawyerTier].
     * Public defenders are free but usually yield the maximum sentence; expensive counsel can acquit or sharply reduce time.
     */
    fun goToTrial(character: Character, lawyerTier: LawyerTier, netWorth: Int): TrialResult {
        val record = character.criminalRecord
        if (!record.awaitingTrial || record.pendingCrimeType == null) return TrialResult.Ineligible

        val crimeType = runCatching {
            CrimeType.valueOf(record.pendingCrimeType)
        }.getOrNull() ?: return TrialResult.Ineligible

        val fee = lawyerFee(lawyerTier, netWorth)
        if (fee > character.stats.money) return TrialResult.Ineligible

        val maxSentence = maxSentenceYears(crimeType, record.timesArrested)
        val roll = Random.nextFloat()
        val outcome = resolveTrialOutcome(lawyerTier, roll, maxSentence)

        val afterFee = character.copy(
            stats = character.stats.copy(money = character.stats.money - fee)
        )

        return when (outcome) {
            TrialOutcome.Acquitted -> TrialResult.Acquitted(
                afterFee.copy(
                    criminalRecord = clearedTrialState(record),
                    eventLog = EventLogCap.prepend(
                        afterFee.eventLog,
                        "Acquitted at trial with ${lawyerLabel(lawyerTier)}."
                    ),
                    stats = afterFee.stats.copy(
                        happiness = clampStat(afterFee.stats.happiness + 6)
                    )
                )
            )
            is TrialOutcome.Sentenced -> {
                val sentence = outcome.years
                val incarcerated = sentence > 0
                TrialResult.Sentenced(
                    afterFee.copy(
                        criminalRecord = record.copy(
                            awaitingTrial = false,
                            pendingCrimeType = null,
                            currentlyIncarcerated = incarcerated,
                            yearsRemaining = sentence,
                            totalSentenceYears = sentence,
                            yearsServed = 0,
                            negativePrisonEvents = 0,
                            paroleBonus = 0
                        ),
                        eventLog = EventLogCap.prepend(
                            afterFee.eventLog,
                            trialSentenceLog(crimeType, sentence, lawyerTier)
                        ),
                        stats = afterFee.stats.copy(
                            happiness = clampStat(afterFee.stats.happiness - if (incarcerated) 10 else 4)
                        )
                    ),
                    sentenceYears = sentence
                )
            }
        }
    }

    /** Applies prison-event choice effects to parole eligibility and roll bonus. */
    fun applyPrisonChoiceEffect(character: Character, paroleEffect: Int): Character {
        if (!character.criminalRecord.currentlyIncarcerated || paroleEffect == 0) return character
        val record = character.criminalRecord
        return character.copy(
            criminalRecord = record.copy(
                negativePrisonEvents = if (paroleEffect < 0) {
                    record.negativePrisonEvents + 1
                } else {
                    record.negativePrisonEvents
                },
                paroleBonus = (record.paroleBonus + paroleEffect).coerceAtLeast(0)
            )
        )
    }

    /**
     * Advances incarceration by one year: parole roll, sentence decrement, happiness penalty, release log.
     */
    fun serveYear(character: Character): Character {
        val record = character.criminalRecord
        if (!record.currentlyIncarcerated) return character

        val yearsServed = record.yearsServed + 1
        val minServedForParole = (record.totalSentenceYears * PAROLE_MIN_SERVED_FRACTION)
            .roundToInt()
            .coerceAtLeast(1)
        val paroleEligible = yearsServed >= minServedForParole &&
            record.negativePrisonEvents == 0 &&
            record.totalSentenceYears > 0

        if (paroleEligible && Random.nextFloat() < paroleChance(record)) {
            return releaseFromPrison(
                character = character.copy(
                    criminalRecord = record.copy(yearsServed = yearsServed)
                ),
                logMessage = "Released on parole after $yearsServed year(s) served."
            )
        }

        val remaining = (record.yearsRemaining - 1).coerceAtLeast(0)
        val stillInside = remaining > 0
        val updatedRecord = record.copy(
            yearsServed = yearsServed,
            yearsRemaining = remaining,
            currentlyIncarcerated = stillInside
        )

        if (!stillInside) {
            return releaseFromPrison(
                character.copy(criminalRecord = updatedRecord),
                logMessage = "Released from prison."
            )
        }

        return character.copy(
            criminalRecord = updatedRecord,
            stats = character.stats.copy(
                happiness = clampStat(character.stats.happiness - INCARCERATION_HAPPINESS_PENALTY)
            )
        )
    }

    fun paroleEligible(record: CriminalRecord): Boolean {
        if (!record.currentlyIncarcerated || record.totalSentenceYears <= 0) return false
        val minServed = (record.totalSentenceYears * PAROLE_MIN_SERVED_FRACTION)
            .roundToInt()
            .coerceAtLeast(1)
        return record.yearsServed >= minServed && record.negativePrisonEvents == 0
    }

    fun paroleChance(record: CriminalRecord): Float =
        (PAROLE_BASE_CHANCE + record.paroleBonus * PAROLE_BONUS_PER_POINT).coerceAtMost(PAROLE_MAX_CHANCE)

    fun performPrisonActivity(character: Character, activity: PrisonActivity): PrisonActivityResult {
        if (!character.alive || !character.criminalRecord.currentlyIncarcerated) {
            return PrisonActivityResult.Ineligible
        }
        val updated = when (activity) {
            PrisonActivity.WORK_DETAIL -> {
                val pay = EconomyScaler.scaleAmount(PRISON_WORK_PAY_KENYA, character.countryCode)
                character.copy(
                    stats = character.stats.copy(
                        money = character.stats.money + pay,
                        happiness = clampStat(character.stats.happiness + 2)
                    ),
                    eventLog = EventLogCap.prepend(
                        character.eventLog,
                        "You worked prison detail and earned ${formatMoney(pay, character.countryCode)}."
                    )
                )
            }
            PrisonActivity.LIBRARY -> character.copy(
                stats = character.stats.copy(
                    smarts = clampStat(character.stats.smarts + 2),
                    happiness = clampStat(character.stats.happiness + 1)
                ),
                eventLog = EventLogCap.prepend(character.eventLog, "You read in the prison library.")
            )
            PrisonActivity.EXERCISE -> character.copy(
                stats = character.stats.copy(
                    health = clampStat(character.stats.health + 3),
                    happiness = clampStat(character.stats.happiness + 1)
                ),
                eventLog = EventLogCap.prepend(character.eventLog, "You exercised in the yard.")
            )
            PrisonActivity.GOOD_BEHAVIOR -> character.copy(
                criminalRecord = character.criminalRecord.copy(
                    paroleBonus = character.criminalRecord.paroleBonus + 1
                ),
                stats = character.stats.copy(
                    happiness = clampStat(character.stats.happiness - 1)
                ),
                eventLog = EventLogCap.prepend(
                    character.eventLog,
                    "You kept your head down and earned good behavior credit."
                )
            )
            PrisonActivity.PRISON_YARD_GANG -> {
                val newNotoriety = (character.criminalRecord.notoriety + 10).coerceIn(0, 100)
                character.copy(
                    criminalRecord = character.criminalRecord.copy(
                        notoriety = newNotoriety,
                        paroleBonus = (character.criminalRecord.paroleBonus - 1).coerceAtLeast(0)
                    ),
                    stats = character.stats.copy(
                        happiness = clampStat(character.stats.happiness + 4)
                    ),
                    eventLog = EventLogCap.prepend(
                        character.eventLog,
                        "Joined the prison yard clique for protection and gained respect (+10 Notoriety)."
                    )
                )
            }
            PrisonActivity.PRISON_ESCAPE_ATTEMPT -> {
                val escapeChance = ((character.stats.smarts * 0.4f + character.criminalRecord.notoriety * 0.3f) / 100f).coerceIn(0.12f, 0.65f)
                if (Random.nextFloat() < escapeChance) {
                    val released = releaseFromPrison(
                        character,
                        "Escaped from maximum security prison through the utility duct! You are now a fugitive on the run."
                    )
                    released.copy(
                        criminalRecord = released.criminalRecord.copy(
                            notoriety = (released.criminalRecord.notoriety + 30).coerceIn(0, 100)
                        ),
                        stats = released.stats.copy(happiness = clampStat(released.stats.happiness + 25))
                    )
                } else {
                    val currentRecord = character.criminalRecord
                    val penaltyYears = 4
                    character.copy(
                        criminalRecord = currentRecord.copy(
                            yearsRemaining = currentRecord.yearsRemaining + penaltyYears,
                            totalSentenceYears = currentRecord.totalSentenceYears + penaltyYears,
                            negativePrisonEvents = currentRecord.negativePrisonEvents + 1
                        ),
                        stats = character.stats.copy(
                            happiness = clampStat(character.stats.happiness - 20),
                            health = clampStat(character.stats.health - 10)
                        ),
                        eventLog = EventLogCap.prepend(
                            character.eventLog,
                            "Escape attempt failed! Guards caught you at the perimeter wire (+4 years added to sentence)."
                        )
                    )
                }
            }
            PrisonActivity.PAROLE_HEARING_APPEAL -> {
                if (paroleEligible(character.criminalRecord)) {
                    val chance = paroleChance(character.criminalRecord)
                    if (Random.nextFloat() < chance) {
                        releaseFromPrison(character, "Granted early parole by the board! You walked free.")
                    } else {
                        character.copy(
                            stats = character.stats.copy(happiness = clampStat(character.stats.happiness - 8)),
                            eventLog = EventLogCap.prepend(
                                character.eventLog,
                                "The parole board reviewed your case and denied early release. Try again next year."
                            )
                        )
                    }
                } else {
                    character.copy(
                        eventLog = EventLogCap.prepend(
                            character.eventLog,
                            "The parole board rejected your petition: you have not served the minimum sentence yet."
                        )
                    )
                }
            }
        }
        return PrisonActivityResult.Success(updated)
    }

    private fun releaseFromPrison(character: Character, logMessage: String): Character {
        val record = character.criminalRecord
        return character.copy(
            criminalRecord = record.copy(
                currentlyIncarcerated = false,
                yearsRemaining = 0,
                totalSentenceYears = 0,
                yearsServed = 0,
                negativePrisonEvents = 0,
                paroleBonus = 0
            ),
            eventLog = EventLogCap.prepend(character.eventLog, logMessage),
            stats = character.stats.copy(
                happiness = clampStat(character.stats.happiness + 4)
            )
        )
    }

    private fun clearedTrialState(record: CriminalRecord): CriminalRecord =
        record.copy(
            awaitingTrial = false,
            pendingCrimeType = null
        )

    private sealed class TrialOutcome {
        data object Acquitted : TrialOutcome()
        data class Sentenced(val years: Int) : TrialOutcome()
    }

    private fun resolveTrialOutcome(
        lawyerTier: LawyerTier,
        roll: Float,
        maxSentence: Int
    ): TrialOutcome {
        if (maxSentence <= 0) {
            return when (lawyerTier) {
                LawyerTier.PUBLIC_DEFENDER -> TrialOutcome.Sentenced(0)
                LawyerTier.AVERAGE -> if (roll < 0.35f) TrialOutcome.Acquitted else TrialOutcome.Sentenced(0)
                LawyerTier.EXPENSIVE -> if (roll < 0.65f) TrialOutcome.Acquitted else TrialOutcome.Sentenced(0)
            }
        }

        return when (lawyerTier) {
            LawyerTier.PUBLIC_DEFENDER -> when {
                roll < 0.05f -> TrialOutcome.Acquitted
                roll < 0.15f -> TrialOutcome.Sentenced((maxSentence * 0.75f).roundToInt().coerceAtLeast(1))
                else -> TrialOutcome.Sentenced(maxSentence)
            }
            LawyerTier.AVERAGE -> when {
                roll < 0.25f -> TrialOutcome.Acquitted
                roll < 0.55f -> TrialOutcome.Sentenced((maxSentence * 0.5f).roundToInt().coerceAtLeast(1))
                roll < 0.80f -> TrialOutcome.Sentenced((maxSentence * 0.75f).roundToInt().coerceAtLeast(1))
                else -> TrialOutcome.Sentenced(maxSentence)
            }
            LawyerTier.EXPENSIVE -> when {
                roll < 0.45f -> TrialOutcome.Acquitted
                roll < 0.75f -> TrialOutcome.Sentenced((maxSentence * 0.35f).roundToInt().coerceAtLeast(1))
                roll < 0.90f -> TrialOutcome.Sentenced((maxSentence * 0.6f).roundToInt().coerceAtLeast(1))
                else -> TrialOutcome.Sentenced(maxSentence)
            }
        }
    }

    private fun trialSentenceLog(
        crimeType: CrimeType,
        sentence: Int,
        lawyerTier: LawyerTier
    ): String {
        val crimeLabel = when (crimeType) {
            CrimeType.PICKPOCKET -> "pickpocketing"
            CrimeType.SHOPLIFT -> "shoplifting"
            CrimeType.FRAUD -> "fraud"
        }
        return if (sentence > 0) {
            "Found guilty of $crimeLabel. Sentenced to $sentence year(s) with ${lawyerLabel(lawyerTier)}."
        } else {
            "Found guilty of $crimeLabel but received no prison time with ${lawyerLabel(lawyerTier)}."
        }
    }

    private fun lawyerLabel(tier: LawyerTier): String = when (tier) {
        LawyerTier.PUBLIC_DEFENDER -> "a public defender"
        LawyerTier.AVERAGE -> "average legal counsel"
        LawyerTier.EXPENSIVE -> "expensive legal counsel"
    }

    internal fun successChance(
        character: Character,
        crimeType: CrimeType,
        priorAttemptsThisYear: Int = character.criminalRecord.crimeAttemptsThisYear
    ): Float {
        val smartsFactor = character.stats.smarts / 100f
        val base = when (crimeType) {
            CrimeType.PICKPOCKET -> 0.25f + smartsFactor * 0.45f
            CrimeType.SHOPLIFT -> 0.18f + smartsFactor * 0.50f
            CrimeType.FRAUD -> 0.12f + smartsFactor * 0.55f
        }
        val karmaPenalty = when {
            character.stats.karma < LOW_KARMA_THRESHOLD -> LOW_KARMA_CATCH_PENALTY
            character.stats.karma < MID_KARMA_THRESHOLD -> MID_KARMA_CATCH_PENALTY
            else -> 0f
        }
        val repeatRisk = REPEAT_ATTEMPT_RISK_BASE.pow(priorAttemptsThisYear)
        val heatPenalty = (character.criminalRecord.heatLevel / 100f * 0.20f)
        return (base / repeatRisk - karmaPenalty - heatPenalty).coerceIn(MIN_SUCCESS_CHANCE, MAX_SUCCESS_CHANCE)
    }

    internal fun moneyReward(crimeType: CrimeType, countryCode: String): Int {
        val base = when (crimeType) {
            CrimeType.PICKPOCKET -> Random.nextInt(500, 2_501)
            CrimeType.SHOPLIFT -> Random.nextInt(2_000, 8_001)
            CrimeType.FRAUD -> Random.nextInt(15_000, 45_001)
        }
        return com.maisha.game.data.EconomyScaler.scaleAmount(base, countryCode)
    }

    /** Maximum sentence before lawyer mitigation (used at trial). */
    fun maxSentenceYears(crimeType: CrimeType, timesArrested: Int): Int {
        val base = when (crimeType) {
            CrimeType.PICKPOCKET -> Random.nextInt(0, 2)
            CrimeType.SHOPLIFT -> Random.nextInt(1, 4)
            CrimeType.FRAUD -> Random.nextInt(3, 7)
        }
        val repeatBonus = (timesArrested - 1).coerceAtLeast(0)
        return (base + repeatBonus).coerceAtMost(MAX_SENTENCE_YEARS)
    }

    fun canRequestExpungement(character: Character): Boolean {
        val record = character.criminalRecord
        if (!record.hasRecord || record.recordExpunged) return false
        if (record.currentlyIncarcerated || record.awaitingTrial) return false
        val cleanYears = record.lastArrestAge?.let { character.age - it } ?: 0
        return cleanYears >= EXPUNGEMENT_MIN_CLEAN_YEARS
    }

    fun expungementFee(character: Character): Int =
        com.maisha.game.data.EconomyScaler.scaleAmount(EXPUNGEMENT_FEE_KENYA, character.countryCode)

    /** Clears criminal record for hiring after years clean and legal fees. */
    fun requestExpungement(character: Character): ExpungementResult {
        if (!canRequestExpungement(character)) return ExpungementResult.Ineligible
        val fee = expungementFee(character)
        if (character.stats.money < fee) return ExpungementResult.InsufficientFunds

        val approved = character.stats.karma >= EXPUNGEMENT_MIN_KARMA &&
            Random.nextFloat() < expungementApprovalChance(character)
        if (!approved) {
            return ExpungementResult.Denied(
                character.copy(
                    stats = character.stats.copy(money = character.stats.money - fee),
                    eventLog = EventLogCap.prepend(
                        character.eventLog,
                        "Your expungement petition was denied " +
                            "(${com.maisha.game.util.formatMoney(fee, character.countryCode)} in legal fees)."
                    )
                )
            )
        }

        return ExpungementResult.Success(
            character.copy(
                stats = character.stats.copy(money = character.stats.money - fee),
                criminalRecord = character.criminalRecord.copy(
                    hasRecord = false,
                    recordExpunged = true
                ),
                eventLog = EventLogCap.prepend(
                    character.eventLog,
                    "Your criminal record was expunged. A fresh start at last."
                )
            )
        )
    }

    private fun expungementApprovalChance(character: Character): Float {
        val cleanYears = character.criminalRecord.lastArrestAge?.let { character.age - it } ?: 0
        val yearsBonus = (cleanYears - EXPUNGEMENT_MIN_CLEAN_YEARS).coerceAtLeast(0) * 0.04f
        val karmaBonus = (character.stats.karma - 50).coerceAtLeast(0) / 100f * 0.2f
        return (EXPUNGEMENT_BASE_CHANCE + yearsBonus + karmaBonus).coerceIn(0.35f, 0.92f)
    }

    companion object {
        const val MIN_CRIME_AGE = 14
        const val REQUIRES_INCARCERATED_TAG = "requires_incarcerated"
        const val PRISON_TAG = "prison"

        private const val MAX_SENTENCE_YEARS = 6
        private const val INCARCERATION_HAPPINESS_PENALTY = 6
        private const val REPEAT_ATTEMPT_RISK_BASE = 1.4f
        private const val MIN_SUCCESS_CHANCE = 0.08f
        private const val MAX_SUCCESS_CHANCE = 0.80f
        private const val LOW_KARMA_THRESHOLD = 20
        private const val MID_KARMA_THRESHOLD = 40
        private const val LOW_KARMA_CATCH_PENALTY = 0.15f
        private const val MID_KARMA_CATCH_PENALTY = 0.08f
        private const val AVERAGE_LAWYER_NET_WORTH_RATE = 0.08
        private const val EXPENSIVE_LAWYER_NET_WORTH_RATE = 0.20
        private const val AVERAGE_LAWYER_MIN_FEE = 5_000
        private const val EXPENSIVE_LAWYER_MIN_FEE = 25_000
        private const val PAROLE_MIN_SERVED_FRACTION = 0.30
        private const val PAROLE_BASE_CHANCE = 0.12f
        private const val PAROLE_BONUS_PER_POINT = 0.04f
        private const val PAROLE_MAX_CHANCE = 0.50f
        private const val PRISON_WORK_PAY_KENYA = 1_500
        private const val EXPUNGEMENT_FEE_KENYA = 55_000
        private const val EXPUNGEMENT_MIN_CLEAN_YEARS = 7
        private const val EXPUNGEMENT_MIN_KARMA = 40
        private const val EXPUNGEMENT_BASE_CHANCE = 0.55f
    }
}
