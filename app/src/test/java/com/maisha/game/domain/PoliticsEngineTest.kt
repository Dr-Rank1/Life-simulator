package com.maisha.game.domain

import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.Business
import com.maisha.game.data.model.BusinessIndustry
import com.maisha.game.data.model.CampaignAgenda
import com.maisha.game.data.model.PoliticalOffice
import com.maisha.game.data.model.PoliticalState
import com.maisha.game.data.model.SocialMediaState
import com.maisha.game.data.model.Stats
import com.maisha.game.data.model.TaxPolicyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PoliticsEngineTest {

    private val financeEngine = FinanceEngine()
    private val politicsEngine = PoliticsEngine(financeEngine)
    private val businessEngine = BusinessEngine(financeEngine)

    @Test
    fun startCampaignRun_setsUpActiveCampaignFlow() {
        val candidate = eligiblePolitician(
            smarts = 80,
            looks = 75,
            money = 5_000_000,
            followers = 10_000
        )
        val minInvest = politicsEngine.minCampaignInvestment(PoliticalOffice.MAYOR, "KE")
        val result = politicsEngine.startCampaignRun(
            candidate,
            PoliticalOffice.MAYOR,
            CampaignAgenda.INFRASTRUCTURE,
            minInvest
        )
        assertTrue(result is CampaignResult.InProgress)
        val updated = (result as CampaignResult.InProgress).character
        assertEquals(PoliticalOffice.MAYOR, updated.politics.campaignActiveFor)
        assertEquals(CampaignAgenda.INFRASTRUCTURE, updated.politics.campaignAgenda)
        assertTrue(updated.politics.voterSupport >= 30)
        assertEquals(minInvest, updated.politics.campaignFunds)
        assertEquals(5_000_000 - minInvest, updated.stats.money)
    }

    @Test
    fun campaignRallyAndFundraising_boostsPollingAndFunds() {
        val candidate = eligiblePolitician(
            smarts = 85,
            looks = 80,
            money = 5_000_000,
            followers = 20_000
        ).copy(
            politics = PoliticalState(
                campaignActiveFor = PoliticalOffice.MAYOR,
                campaignAgenda = CampaignAgenda.EDUCATION,
                voterSupport = 40,
                campaignFunds = 100_000
            )
        )

        val rallyResult = politicsEngine.holdCampaignRally(candidate)
        assertTrue(rallyResult is CampaignResult.InProgress)
        val afterRally = (rallyResult as CampaignResult.InProgress).character
        assertTrue(afterRally.politics.voterSupport > 40)
        assertTrue(afterRally.politics.politicalActionDoneThisYear)

        val fundraiseCandidate = afterRally.copy(
            politics = afterRally.politics.copy(politicalActionDoneThisYear = false)
        )
        val fundraiseResult = politicsEngine.fundraiseCampaign(fundraiseCandidate)
        assertTrue(fundraiseResult is CampaignResult.InProgress)
        val afterFundraise = (fundraiseResult as CampaignResult.InProgress).character
        assertTrue(afterFundraise.politics.campaignFunds > 100_000)
    }

    @Test
    fun resolveElection_declaresWinnerWhenPollingHigh() {
        val candidate = eligiblePolitician(
            smarts = 90,
            looks = 85,
            money = 5_000_000,
            followers = 50_000
        ).copy(
            politics = PoliticalState(
                campaignActiveFor = PoliticalOffice.MAYOR,
                campaignAgenda = CampaignAgenda.HEALTHCARE,
                voterSupport = 85,
                campaignFunds = 500_000
            )
        )

        val electionResult = politicsEngine.resolveElection(candidate)
        assertTrue(electionResult is CampaignResult.Won)
        val elected = (electionResult as CampaignResult.Won).character
        assertEquals(PoliticalOffice.MAYOR, elected.politics.currentOffice)
        assertNull(elected.politics.campaignActiveFor)
        assertEquals(4, elected.politics.termYearsLeft)
        assertTrue(elected.politics.approvalRating >= 50)
    }

    @Test
    fun governanceActions_boostApprovalAndTermCountdownTicks() {
        val mayor = eligiblePolitician(
            smarts = 85,
            looks = 80,
            money = 1_000_000,
            followers = 15_000
        ).copy(
            politics = PoliticalState(
                currentOffice = PoliticalOffice.MAYOR,
                approvalRating = 55,
                termYearsLeft = 4,
                yearsInOffice = 0
            )
        )

        val addressResult = politicsEngine.deliverStateAddress(mayor)
        assertTrue(addressResult is CampaignResult.InProgress)
        val afterAddress = (addressResult as CampaignResult.InProgress).character
        assertTrue(afterAddress.politics.approvalRating >= 55)

        val reformCandidate = afterAddress.copy(
            politics = afterAddress.politics.copy(politicalActionDoneThisYear = false)
        )
        val reformResult = politicsEngine.enactSignatureReform(reformCandidate, CampaignAgenda.ANTI_CORRUPTION)
        assertTrue(reformResult is CampaignResult.InProgress)
        val afterReform = (reformResult as CampaignResult.InProgress).character
        assertTrue(afterReform.politics.approvalRating > 55)

        // Tick governance
        val tickResult = politicsEngine.tickGovernance(afterReform)
        val ticked = tickResult.character
        assertEquals(3, ticked.politics.termYearsLeft)
        assertEquals(1, ticked.politics.yearsInOffice)
        assertFalse(ticked.politics.politicalActionDoneThisYear)
    }

    @Test
    fun launchCampaign_calculatesWinBasedOnInvestmentAndStats() {
        val weak = eligiblePolitician(
            smarts = 55,
            looks = 40,
            money = 5_000_000,
            followers = 0
        )
        val strong = eligiblePolitician(
            smarts = 95,
            looks = 90,
            money = 5_000_000,
            followers = 200_000
        )
        val minInvest = politicsEngine.minCampaignInvestment(PoliticalOffice.MAYOR, "KE")
        val weakChance = politicsEngine.campaignWinChance(weak, PoliticalOffice.MAYOR, minInvest)
        val strongChance = politicsEngine.campaignWinChance(
            strong,
            PoliticalOffice.MAYOR,
            minInvest * 3
        )
        assertTrue(
            "Strong campaign ($strongChance) should beat weak ($weakChance)",
            strongChance > weakChance + 0.15f
        )
    }

    @Test
    fun passTaxPolicy_modifiesEconomyScalerAndApprovalRating() {
        val president = eligiblePolitician(
            smarts = 90,
            looks = 80,
            money = 1_000_000,
            followers = 50_000
        ).copy(
            politics = PoliticalState(
                currentOffice = PoliticalOffice.PRESIDENT,
                approvalRating = 60,
                campaignFunds = 0
            ),
            businesses = listOf(
                Business(
                    id = "b1",
                    name = "Test Co",
                    industry = BusinessIndustry.TECH,
                    valuation = 500_000,
                    revenue = 100_000,
                    employeeCount = 5
                )
            )
        )

        assertEquals(1.0f, EconomyScaler.policyBusinessRevenueMultiplier(null))
        assertTrue(EconomyScaler.policyBusinessRevenueMultiplier(TaxPolicyType.TAX_CUTS) > 1f)
        assertTrue(EconomyScaler.policyBusinessRevenueMultiplier(TaxPolicyType.WEALTH_TAX) < 1f)

        val taxCuts = financeEngine.passTaxPolicy(president, TaxPolicyType.TAX_CUTS)
        assertTrue(taxCuts is FinanceEngine.TaxPolicyResult.Success)
        val afterCuts = (taxCuts as FinanceEngine.TaxPolicyResult.Success).character
        assertEquals(TaxPolicyType.TAX_CUTS, afterCuts.politics.activeTaxPolicy)
        assertEquals(52, afterCuts.politics.approvalRating)
        assertEquals(1_000_000, afterCuts.stats.money)

        val wealthTax = financeEngine.passTaxPolicy(
            president.copy(politics = president.politics.copy(activeTaxPolicy = null)),
            TaxPolicyType.WEALTH_TAX
        )
        assertTrue(wealthTax is FinanceEngine.TaxPolicyResult.Success)
        val afterWealth = (wealthTax as FinanceEngine.TaxPolicyResult.Success).character
        assertEquals(TaxPolicyType.WEALTH_TAX, afterWealth.politics.activeTaxPolicy)
        assertEquals(70, afterWealth.politics.approvalRating)
        assertTrue(afterWealth.stats.money < president.stats.money)

        val boosted = businessEngine.processBusinessYear(afterCuts)
        val taxed = businessEngine.processBusinessYear(
            president.copy(
                politics = president.politics.copy(activeTaxPolicy = TaxPolicyType.WEALTH_TAX),
                businesses = president.businesses
            )
        )
        assertTrue(boosted.businesses.first().revenue >= taxed.businesses.first().revenue * 0.85f)
    }

    private fun eligiblePolitician(
        smarts: Int,
        looks: Int,
        money: Int,
        followers: Int
    ) = TestFixtures.character(
        age = 40,
        countryCode = "KE",
        stats = Stats(
            health = 80,
            happiness = 70,
            smarts = smarts,
            looks = looks,
            money = money
        ),
        socialMedia = SocialMediaState(hasAccount = followers > 0, followers = followers)
    ).copy(citizenships = listOf("KE"))
}
