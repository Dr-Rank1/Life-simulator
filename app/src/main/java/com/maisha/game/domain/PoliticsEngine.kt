// app/src/main/java/com/maisha/game/domain/PoliticsEngine.kt
package com.maisha.game.domain

import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.CampaignAgenda
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.EventChoice
import com.maisha.game.data.model.LifeEvent
import com.maisha.game.data.model.PoliticalOffice
import com.maisha.game.data.model.PoliticalState
import com.maisha.game.util.clampStat
import com.maisha.game.util.formatMoney
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlin.random.Random

sealed class CampaignResult {
    data class Won(val character: Character, val message: String? = null) : CampaignResult()
    data class Lost(val character: Character, val message: String? = null) : CampaignResult()
    data class InProgress(val character: Character, val message: String? = null) : CampaignResult()
    data class Failed(val reason: CampaignFailure) : CampaignResult()
}

enum class CampaignFailure {
    INELIGIBLE,
    ALREADY_IN_OFFICE,
    INSUFFICIENT_FUNDS,
    ALREADY_CAMPAIGNED,
    INVALID_INVESTMENT,
    NO_ACTIVE_CAMPAIGN,
    ACTION_ALREADY_DONE,
    NOT_IN_OFFICE
}

data class GovernanceTickResult(
    val character: Character,
    val impeachmentEvent: LifeEvent? = null,
    val termCompleted: Boolean = false
)

@Singleton
class PoliticsEngine @Inject constructor(
    private val financeEngine: FinanceEngine
) {

    fun holdsOffice(character: Character): Boolean =
        character.politics.currentOffice != null

    fun isCampaignActive(character: Character): Boolean =
        character.politics.campaignActiveFor != null

    fun canRunForOffice(character: Character, office: PoliticalOffice): Boolean {
        if (!character.alive) return false
        if (character.age < MIN_OFFICE_AGE) return false
        if (character.criminalRecord.hasRecord) return false
        if (character.criminalRecord.currentlyIncarcerated) return false
        if (!character.holdsCitizenship(character.countryCode)) return false
        if (character.politics.currentOffice != null) return false
        if (character.politics.campaignedThisYear && character.politics.campaignActiveFor == null) return false
        if (character.stats.smarts < minSmartsFor(office)) return false
        val netWorth = financeEngine.calculateNetWorth(character)
        if (netWorth < minNetWorthFor(office, character.countryCode)) return false
        return true
    }

    fun minCampaignInvestment(office: PoliticalOffice, countryCode: String): Int =
        EconomyScaler.scaleAmount(baseInvestmentFor(office), countryCode)

    fun campaignWinChance(
        character: Character,
        office: PoliticalOffice,
        investment: Int
    ): Float {
        val minInvest = minCampaignInvestment(office, character.countryCode).coerceAtLeast(1)
        val investmentFactor = (investment.toFloat() / minInvest).coerceIn(0.25f, 3f) * 0.22f
        val smartsFactor = character.stats.smarts / 100f * 0.28f
        val looksFactor = character.stats.looks / 100f * 0.18f
        val followers = character.socialMedia.followers.coerceAtLeast(0)
        val socialFactor = (followers / 50_000f).coerceIn(0f, 1f) * 0.18f
        val officeDifficulty = when (office) {
            PoliticalOffice.MAYOR -> 0.12f
            PoliticalOffice.GOVERNOR -> 0.06f
            PoliticalOffice.PRESIDENT -> 0f
        }
        return (0.12f + investmentFactor + smartsFactor + looksFactor + socialFactor + officeDifficulty)
            .coerceIn(0.05f, 0.92f)
    }

    /**
     * Start a formal election campaign for [office] with an initial [agenda] and [investment].
     */
    fun startCampaignRun(
        character: Character,
        office: PoliticalOffice,
        agenda: CampaignAgenda,
        investment: Int
    ): CampaignResult {
        if (character.politics.currentOffice != null) {
            return CampaignResult.Failed(CampaignFailure.ALREADY_IN_OFFICE)
        }
        if (!canRunForOffice(character, office)) {
            return CampaignResult.Failed(CampaignFailure.INELIGIBLE)
        }
        val minInvest = minCampaignInvestment(office, character.countryCode)
        if (investment < minInvest) {
            return CampaignResult.Failed(CampaignFailure.INVALID_INVESTMENT)
        }
        if (character.stats.money < investment) {
            return CampaignResult.Failed(CampaignFailure.INSUFFICIENT_FUNDS)
        }

        val minInvestFloat = minInvest.toFloat().coerceAtLeast(1f)
        val initialPolling = (30 + ((investment.toFloat() / minInvestFloat) * 10f).toInt())
            .coerceIn(25, 55)

        val updated = character.copy(
            stats = character.stats.copy(money = character.stats.money - investment),
            politics = character.politics.copy(
                campaignActiveFor = office,
                campaignAgenda = agenda,
                campaignFunds = investment,
                voterSupport = initialPolling,
                campaignedThisYear = true,
                politicalActionDoneThisYear = false
            ),
            eventLog = EventLogCap.prepend(
                character.eventLog,
                "Announced candidacy for ${officeLabel(office)} on a platform of ${agendaLabel(agenda)}. Invested ${formatMoney(investment, character.countryCode)} in initial campaign infrastructure."
            )
        )
        return CampaignResult.InProgress(
            character = updated,
            message = "Filed candidacy for ${officeLabel(office)}."
        )
    }

    /**
     * Holds a high-energy campaign rally to mobilize voters and lift polling numbers.
     */
    fun holdCampaignRally(character: Character): CampaignResult {
        val office = character.politics.campaignActiveFor
            ?: return CampaignResult.Failed(CampaignFailure.NO_ACTIVE_CAMPAIGN)
        if (character.politics.politicalActionDoneThisYear) {
            return CampaignResult.Failed(CampaignFailure.ACTION_ALREADY_DONE)
        }

        val charisma = (character.stats.looks * 0.5f + character.stats.smarts * 0.5f) / 100f
        val boost = Random.nextInt(8, 16) + (charisma * 6).toInt()
        val newPolling = (character.politics.voterSupport + boost).coerceAtMost(98)
        val hecklerRisk = Random.nextFloat() < 0.20f
        val newScandal = if (hecklerRisk) (character.politics.scandalHeat + 12).coerceAtMost(100) else character.politics.scandalHeat

        val logText = if (hecklerRisk) {
            "Hosted a fiery rally for ${officeLabel(office)}. Crowds swelled polling to $newPolling%, but vocal counter-protesters drew controversy."
        } else {
            "Hosted an electric public rally for ${officeLabel(office)}. Grassroots enthusiasm surged to $newPolling% voter support."
        }

        val updated = character.copy(
            politics = character.politics.copy(
                voterSupport = newPolling,
                scandalHeat = newScandal,
                politicalActionDoneThisYear = true
            ),
            stats = character.stats.copy(happiness = clampStat(character.stats.happiness + 4)),
            eventLog = EventLogCap.prepend(character.eventLog, logText)
        )
        return CampaignResult.InProgress(character = updated, message = logText)
    }

    /**
     * Fundraises from political donors and PACs, swelling the campaign war chest.
     */
    fun fundraiseCampaign(character: Character): CampaignResult {
        val office = character.politics.campaignActiveFor
            ?: return CampaignResult.Failed(CampaignFailure.NO_ACTIVE_CAMPAIGN)
        if (character.politics.politicalActionDoneThisYear) {
            return CampaignResult.Failed(CampaignFailure.ACTION_ALREADY_DONE)
        }

        val baseDonation = minCampaignInvestment(office, character.countryCode) / 3
        val supportFactor = (character.politics.voterSupport / 50f).coerceIn(0.5f, 2.0f)
        val fundsRaised = (baseDonation * supportFactor * (0.8f + Random.nextFloat() * 0.5f)).roundToInt()

        val logText = "Hosted a campaign donor dinner. Raised ${formatMoney(fundsRaised, character.countryCode)} for the ${officeLabel(office)} campaign."
        val updated = character.copy(
            politics = character.politics.copy(
                campaignFunds = character.politics.campaignFunds + fundsRaised,
                politicalActionDoneThisYear = true
            ),
            eventLog = EventLogCap.prepend(character.eventLog, logText)
        )
        return CampaignResult.InProgress(character = updated, message = logText)
    }

    /**
     * Election Day: tallies votes from voter support, war chest, and candidate stature.
     */
    fun resolveElection(character: Character): CampaignResult {
        val office = character.politics.campaignActiveFor
            ?: return CampaignResult.Failed(CampaignFailure.NO_ACTIVE_CAMPAIGN)

        val polling = character.politics.voterSupport
        val funds = character.politics.campaignFunds
        val minInvest = minCampaignInvestment(office, character.countryCode).coerceAtLeast(1)
        val fundBonus = (funds.toFloat() / minInvest).coerceIn(0.2f, 2.5f) * 10f
        val finalVoteShare = (polling * 0.70f + fundBonus + (character.stats.smarts / 10f) + Random.nextInt(-6, 7))
            .roundToInt()
            .coerceIn(10, 95)

        val won = finalVoteShare >= 50
        val officeLabel = officeLabel(office)

        return if (won) {
            val elected = character.copy(
                politics = PoliticalState(
                    currentOffice = office,
                    approvalRating = STARTING_APPROVAL + (finalVoteShare - 50) / 2,
                    campaignFunds = 0,
                    activeTaxPolicy = null,
                    yearsInOffice = 0,
                    termYearsLeft = 4,
                    termsServed = character.politics.termsServed,
                    campaignedThisYear = true,
                    politicalActionDoneThisYear = false
                ),
                stats = character.stats.copy(happiness = clampStat(character.stats.happiness + 12)),
                eventLog = EventLogCap.prepend(
                    character.eventLog,
                    "ELECTION VICTORY: You won the race for $officeLabel with $finalVoteShare% of the popular vote!"
                )
            )
            CampaignResult.Won(elected, "Elected $officeLabel!")
        } else {
            val lost = character.copy(
                politics = character.politics.copy(
                    campaignActiveFor = null,
                    campaignFunds = 0,
                    voterSupport = 35,
                    politicalActionDoneThisYear = false
                ),
                stats = character.stats.copy(happiness = clampStat(character.stats.happiness - 8)),
                eventLog = EventLogCap.prepend(
                    character.eventLog,
                    "ELECTION DEFEAT: Conceded the race for $officeLabel after securing $finalVoteShare% of the vote."
                )
            )
            CampaignResult.Lost(lost, "Defeated in $officeLabel election.")
        }
    }

    /**
     * Backward-compatible direct campaign launcher: starts and immediately resolves race.
     */
    fun launchCampaign(
        character: Character,
        office: PoliticalOffice,
        investment: Int
    ): CampaignResult {
        val runResult = startCampaignRun(character, office, CampaignAgenda.INFRASTRUCTURE, investment)
        if (runResult !is CampaignResult.InProgress) return runResult
        return resolveElection(runResult.character)
    }

    /**
     * Delivers an address to constituents or press, boosting public approval.
     */
    fun deliverStateAddress(character: Character): CampaignResult {
        val office = character.politics.currentOffice
            ?: return CampaignResult.Failed(CampaignFailure.NOT_IN_OFFICE)
        if (character.politics.politicalActionDoneThisYear) {
            return CampaignResult.Failed(CampaignFailure.ACTION_ALREADY_DONE)
        }

        val speechQuality = (character.stats.smarts * 0.6f + character.stats.looks * 0.4f) / 100f
        val approvalDelta = Random.nextInt(5, 12) + (speechQuality * 4).roundToInt()
        val newApproval = (character.politics.approvalRating + approvalDelta).coerceAtMost(100)
        val label = officeLabel(office)

        val logText = "Delivered a televised address as $label. Clear policy direction lifted public approval to $newApproval%."
        val updated = character.copy(
            politics = character.politics.copy(
                approvalRating = newApproval,
                politicalActionDoneThisYear = true
            ),
            stats = character.stats.copy(happiness = clampStat(character.stats.happiness + 3)),
            eventLog = EventLogCap.prepend(character.eventLog, logText)
        )
        return CampaignResult.InProgress(character = updated, message = logText)
    }

    /**
     * Enacts a signature policy reform in office.
     */
    fun enactSignatureReform(character: Character, agenda: CampaignAgenda): CampaignResult {
        val office = character.politics.currentOffice
            ?: return CampaignResult.Failed(CampaignFailure.NOT_IN_OFFICE)
        if (character.politics.politicalActionDoneThisYear) {
            return CampaignResult.Failed(CampaignFailure.ACTION_ALREADY_DONE)
        }

        val label = officeLabel(office)
        val agendaText = agendaLabel(agenda)
        val approvalBoost = Random.nextInt(8, 16)
        val newApproval = (character.politics.approvalRating + approvalBoost).coerceAtMost(100)
        val newScandal = (character.politics.scandalHeat - 15).coerceAtLeast(0)

        val logText = "Signed landmark $agendaText reform legislation into law as $label! Approval climbed to $newApproval%."
        val updated = character.copy(
            politics = character.politics.copy(
                approvalRating = newApproval,
                scandalHeat = newScandal,
                campaignAgenda = agenda,
                politicalActionDoneThisYear = true
            ),
            stats = character.stats.copy(
                happiness = clampStat(character.stats.happiness + 6),
                karma = clampStat(character.stats.karma + 5)
            ),
            eventLog = EventLogCap.prepend(character.eventLog, logText)
        )
        return CampaignResult.InProgress(character = updated, message = logText)
    }

    /**
     * Handle political controversy: transparent inquiry vs aggressive cover-up.
     */
    fun handlePoliticalScandal(character: Character, coverUp: Boolean): CampaignResult {
        val office = character.politics.currentOffice
            ?: return CampaignResult.Failed(CampaignFailure.NOT_IN_OFFICE)
        val label = officeLabel(office)

        return if (coverUp) {
            val leaked = Random.nextFloat() < 0.60f
            if (leaked) {
                val newApproval = (character.politics.approvalRating - 24).coerceAtLeast(5)
                val newScandal = (character.politics.scandalHeat + 35).coerceAtMost(100)
                val logText = "The cover-up leaked to investigative reporters! Approval for $label plummeted to $newApproval%."
                val updated = character.copy(
                    politics = character.politics.copy(
                        approvalRating = newApproval,
                        scandalHeat = newScandal
                    ),
                    stats = character.stats.copy(
                        happiness = clampStat(character.stats.happiness - 12),
                        karma = clampStat(character.stats.karma - 15)
                    ),
                    eventLog = EventLogCap.prepend(character.eventLog, logText)
                )
                CampaignResult.InProgress(character = updated, message = logText)
            } else {
                val logText = "Managed political fallout behind closed doors. The news cycle moved on without public leaks."
                val updated = character.copy(
                    politics = character.politics.copy(scandalHeat = 0),
                    eventLog = EventLogCap.prepend(character.eventLog, logText)
                )
                CampaignResult.InProgress(character = updated, message = logText)
            }
        } else {
            val newApproval = (character.politics.approvalRating - 8).coerceAtLeast(10)
            val logText = "Ordered an independent public inquiry as $label. Transparency took an initial political toll (-8 approval), but preserved integrity."
            val updated = character.copy(
                politics = character.politics.copy(
                    approvalRating = newApproval,
                    scandalHeat = 0
                ),
                stats = character.stats.copy(karma = clampStat(character.stats.karma + 8)),
                eventLog = EventLogCap.prepend(character.eventLog, logText)
            )
            CampaignResult.InProgress(character = updated, message = logText)
        }
    }

    /**
     * Yearly governance tick: handles term countdown, approval drift, and impeachment.
     */
    fun tickGovernance(character: Character): GovernanceTickResult {
        val office = character.politics.currentOffice
            ?: return GovernanceTickResult(
                character.copy(
                    politics = character.politics.copy(
                        campaignedThisYear = false,
                        politicalActionDoneThisYear = false
                    )
                )
            )

        val drift = Random.nextInt(APPROVAL_DRIFT_MIN, APPROVAL_DRIFT_MAX + 1)
        val scandalPenalty = (character.politics.scandalHeat / 10)
        val newApproval = (character.politics.approvalRating + drift - scandalPenalty).coerceIn(0, 100)
        val newYearsInOffice = character.politics.yearsInOffice + 1
        val newTermYearsLeft = (character.politics.termYearsLeft - 1).coerceAtLeast(0)

        var updated = character.copy(
            politics = character.politics.copy(
                approvalRating = newApproval,
                yearsInOffice = newYearsInOffice,
                termYearsLeft = newTermYearsLeft,
                campaignedThisYear = false,
                politicalActionDoneThisYear = false,
                scandalHeat = (character.politics.scandalHeat - 5).coerceAtLeast(0)
            )
        )

        // Impeachment check if public approval collapses
        if (newApproval < IMPEACHMENT_THRESHOLD) {
            val event = buildImpeachmentEvent(updated, office)
            updated = resignFromOffice(
                updated,
                reason = "Public approval collapsed to $newApproval%. You resigned from $office under intense legislative pressure."
            )
            return GovernanceTickResult(updated, impeachmentEvent = event)
        }

        // Term expiration check
        if (newTermYearsLeft <= 0) {
            val label = officeLabel(office)
            val termsDone = updated.politics.termsServed + 1
            val legacyMessage = if (newApproval >= 50) {
                "COMPLETED TERM: Successfully concluded your 4-year term as $label with a high approval rating ($newApproval%). Citizens praise your leadership."
            } else {
                "COMPLETED TERM: Stepped down after completing your 4-year term as $label. Public opinion remains polarized."
            }

            updated = updated.copy(
                politics = PoliticalState(
                    termsServed = termsDone,
                    voterSupport = 40,
                    campaignedThisYear = false,
                    politicalActionDoneThisYear = false
                ),
                stats = updated.stats.copy(
                    happiness = clampStat(updated.stats.happiness + if (newApproval >= 50) 8 else 0)
                ),
                eventLog = EventLogCap.prepend(updated.eventLog, legacyMessage)
            )
            return GovernanceTickResult(updated, termCompleted = true)
        }

        return GovernanceTickResult(updated)
    }

    fun resignFromOffice(character: Character, reason: String): Character {
        if (character.politics.currentOffice == null) return character
        return character.copy(
            politics = PoliticalState(
                campaignedThisYear = character.politics.campaignedThisYear,
                termsServed = character.politics.termsServed
            ),
            stats = character.stats.copy(
                happiness = clampStat(character.stats.happiness - 10)
            ),
            eventLog = EventLogCap.prepend(character.eventLog, reason)
        )
    }

    fun buildImpeachmentEvent(character: Character, office: PoliticalOffice): LifeEvent {
        val label = officeLabel(office)
        return LifeEvent(
            id = "politics_impeachment_${character.age}",
            minAge = character.age,
            maxAge = character.age,
            text = "Your approval as $label has collapsed. Allies abandon you; " +
                "impeachment hearings dominate national headlines.",
            choices = listOf(
                EventChoice(
                    label = "Resign with dignity",
                    statEffects = mapOf("happiness" to -4),
                    resultText = "You step down. History will debate your legacy."
                ),
                EventChoice(
                    label = "Fight to the bitter end",
                    statEffects = mapOf("happiness" to -8, "smarts" to 1),
                    resultText = "You lose anyway — and burn remaining public goodwill."
                )
            ),
            tags = listOf(POLITICS_SYSTEM_TAG, REQUIRES_OFFICE_TAG),
            weight = 1
        )
    }

    fun officeLabel(office: PoliticalOffice): String = when (office) {
        PoliticalOffice.MAYOR -> "Mayor"
        PoliticalOffice.GOVERNOR -> "Governor"
        PoliticalOffice.PRESIDENT -> "President"
    }

    fun agendaLabel(agenda: CampaignAgenda): String = when (agenda) {
        CampaignAgenda.INFRASTRUCTURE -> "Infrastructure & Transit"
        CampaignAgenda.EDUCATION -> "School Funding & Education"
        CampaignAgenda.HEALTHCARE -> "Public Healthcare Access"
        CampaignAgenda.TAX_RELIEF -> "Tax Relief & Growth"
        CampaignAgenda.ANTI_CORRUPTION -> "Anti-Corruption & Integrity"
    }

    private fun minSmartsFor(office: PoliticalOffice): Int = when (office) {
        PoliticalOffice.MAYOR -> 55
        PoliticalOffice.GOVERNOR -> 65
        PoliticalOffice.PRESIDENT -> 75
    }

    private fun minNetWorthFor(office: PoliticalOffice, countryCode: String): Int =
        EconomyScaler.scaleAmount(
            when (office) {
                PoliticalOffice.MAYOR -> 200_000
                PoliticalOffice.GOVERNOR -> 800_000
                PoliticalOffice.PRESIDENT -> 2_000_000
            },
            countryCode
        )

    private fun baseInvestmentFor(office: PoliticalOffice): Int = when (office) {
        PoliticalOffice.MAYOR -> 50_000
        PoliticalOffice.GOVERNOR -> 200_000
        PoliticalOffice.PRESIDENT -> 500_000
    }

    companion object {
        const val REQUIRES_OFFICE_TAG = "requires_office"
        const val POLITICS_SYSTEM_TAG = "politics_system"
        const val POLITICS_TAG = "politics"
        const val MIN_OFFICE_AGE = 30
        const val IMPEACHMENT_THRESHOLD = 20
        const val STARTING_APPROVAL = 55
        private const val APPROVAL_DRIFT_MIN = -6
        private const val APPROVAL_DRIFT_MAX = 7
    }
}
