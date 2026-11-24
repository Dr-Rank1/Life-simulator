// app/src/main/java/com/maisha/game/ui/components/PoliticalCampaignBoard.kt
package com.maisha.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maisha.game.R
import com.maisha.game.data.model.CampaignAgenda
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.PoliticalOffice
import com.maisha.game.data.model.TaxPolicyType
import com.maisha.game.domain.PoliticsEngine
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.BorderCardSlate
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.CreamBg
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.SuccessGreen
import com.maisha.game.ui.theme.TealPrimary
import com.maisha.game.util.formatMoney

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PoliticalCampaignBoard(
    character: Character,
    politicsEngine: PoliticsEngine,
    onStartCampaign: (PoliticalOffice, CampaignAgenda, Int) -> Unit,
    onHoldRally: () -> Unit,
    onFundraise: () -> Unit,
    onCastBallots: () -> Unit,
    onStateAddress: () -> Unit,
    onSignatureReform: (CampaignAgenda) -> Unit,
    onHandleScandal: (Boolean) -> Unit,
    onResignOffice: () -> Unit,
    onPassTaxPolicy: (TaxPolicyType) -> Unit,
    modifier: Modifier = Modifier
) {
    val politics = character.politics
    val countryCode = character.countryCode

    when {
        // State 1: Currently Holding Public Office
        politics.currentOffice != null -> {
            OfficeholderDashboard(
                character = character,
                office = politics.currentOffice,
                onStateAddress = onStateAddress,
                onSignatureReform = onSignatureReform,
                onHandleScandal = onHandleScandal,
                onResignOffice = onResignOffice,
                onPassTaxPolicy = onPassTaxPolicy,
                modifier = modifier
            )
        }

        // State 2: Actively Running an Election Campaign
        politics.campaignActiveFor != null -> {
            ActiveCampaignDashboard(
                character = character,
                targetOffice = politics.campaignActiveFor,
                onHoldRally = onHoldRally,
                onFundraise = onFundraise,
                onCastBallots = onCastBallots,
                modifier = modifier
            )
        }

        // State 3: Launch Candidacy
        else -> {
            RunForOfficeSetupCard(
                character = character,
                politicsEngine = politicsEngine,
                onStartCampaign = onStartCampaign,
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OfficeholderDashboard(
    character: Character,
    office: PoliticalOffice,
    onStateAddress: () -> Unit,
    onSignatureReform: (CampaignAgenda) -> Unit,
    onHandleScandal: (Boolean) -> Unit,
    onResignOffice: () -> Unit,
    onPassTaxPolicy: (TaxPolicyType) -> Unit,
    modifier: Modifier = Modifier
) {
    val politics = character.politics
    var showScandalDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, BorderCardSlate)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Official Seal & Office Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppIcons.GovernmentSeal,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = politicalOfficeLabel(office),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = "Year ${politics.yearsInOffice + 1} (Term ${politics.termsServed + 1})",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkTertiary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CreamBg,
                    border = BorderStroke(1.dp, BorderCardSlate)
                ) {
                    Text(
                        text = "${politics.termYearsLeft} yrs left",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = InkSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Public Approval Metric Strip
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Public Approval: ${politics.approvalRating}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (politics.approvalRating >= 50) SuccessGreen else CoralNegative
                    )
                    Text(
                        text = when {
                            politics.approvalRating >= 70 -> "High Standing"
                            politics.approvalRating >= 40 -> "Fragile Majority"
                            else -> "Impeachment Hazard"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = InkTertiary
                    )
                }
                LinearProgressIndicator(
                    progress = { politics.approvalRating / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (politics.approvalRating >= 50) SuccessGreen else CoralNegative,
                    trackColor = BorderCardSlate
                )
            }

            // Scandal Meter (if controversy exists)
            if (politics.scandalHeat > 0) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CoralNegative.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, CoralNegative.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${stringResource(R.string.label_scandal_risk)}: ${politics.scandalHeat}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CoralNegative
                            )
                            Text(
                                text = "High scandal heat drags public approval.",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkTertiary
                            )
                        }
                        OutlinedButton(
                            onClick = { showScandalDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CoralNegative)
                        ) {
                            Text(stringResource(R.string.btn_handle_scandal), color = CoralNegative)
                        }
                    }
                }
            }

            // Status notice if action completed
            if (politics.politicalActionDoneThisYear) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CreamBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.msg_campaign_action_done),
                        style = MaterialTheme.typography.labelSmall,
                        color = InkTertiary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Executive Actions
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // State Address
                Button(
                    onClick = onStateAddress,
                    enabled = !politics.politicalActionDoneThisYear,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Icon(AppIcons.Megaphone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.btn_state_address))
                }

                // Pass Signature Reform
                OutlinedButton(
                    onClick = { onSignatureReform(politics.campaignAgenda) },
                    enabled = !politics.politicalActionDoneThisYear,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, TealPrimary)
                ) {
                    Icon(AppIcons.Smarts, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.btn_signature_reform), color = TealPrimary)
                }

                // Tax Relief Policy
                OutlinedButton(
                    onClick = { onPassTaxPolicy(TaxPolicyType.TAX_CUTS) },
                    enabled = politics.activeTaxPolicy != TaxPolicyType.TAX_CUTS,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GoldAccent)
                ) {
                    Icon(AppIcons.Money, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Tax Relief", color = GoldAccent)
                }

                // Resign Office
                OutlinedButton(
                    onClick = onResignOffice,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CoralNegative)
                ) {
                    Text(stringResource(R.string.btn_resign_office), color = CoralNegative)
                }
            }
        }
    }

    if (showScandalDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showScandalDialog = false },
            title = { Text(stringResource(R.string.btn_handle_scandal)) },
            text = { Text("Controversy is mounting. Choose how to handle the political fallout:") },
            confirmButton = {
                Button(
                    onClick = {
                        onHandleScandal(false)
                        showScandalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Independent Inquiry")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        onHandleScandal(true)
                        showScandalDialog = false
                    }
                ) {
                    Text("Cover Up")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveCampaignDashboard(
    character: Character,
    targetOffice: PoliticalOffice,
    onHoldRally: () -> Unit,
    onFundraise: () -> Unit,
    onCastBallots: () -> Unit,
    modifier: Modifier = Modifier
) {
    val politics = character.politics
    val countryCode = character.countryCode

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, BorderCardSlate)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Campaign Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(TealPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppIcons.Megaphone,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Campaign for ${politicalOfficeLabel(targetOffice)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = agendaLabelText(politics.campaignAgenda),
                            style = MaterialTheme.typography.labelSmall,
                            color = TealPrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CreamBg
                ) {
                    Text(
                        text = "War Chest: ${formatMoney(politics.campaignFunds, countryCode)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Polling Support Meter
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stringResource(R.string.label_voter_support)}: ${politics.voterSupport}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (politics.voterSupport >= 50) SuccessGreen else InkPrimary
                    )
                    Text(
                        text = if (politics.voterSupport >= 50) "Victory Territory (50%+)" else "Trailing in Polls",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkTertiary
                    )
                }
                LinearProgressIndicator(
                    progress = { politics.voterSupport / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (politics.voterSupport >= 50) SuccessGreen else TealPrimary,
                    trackColor = BorderCardSlate
                )
            }

            // Campaign Action Buttons
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Host Public Rally
                Button(
                    onClick = onHoldRally,
                    enabled = !politics.politicalActionDoneThisYear,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Icon(AppIcons.Megaphone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.btn_campaign_rally))
                }

                // Fundraise Donors
                OutlinedButton(
                    onClick = onFundraise,
                    enabled = !politics.politicalActionDoneThisYear,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GoldAccent)
                ) {
                    Icon(AppIcons.Money, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.btn_fundraise_donors), color = GoldAccent)
                }

                // Election Day
                Button(
                    onClick = onCastBallots,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(AppIcons.VoteBallot, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.btn_cast_ballots))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RunForOfficeSetupCard(
    character: Character,
    politicsEngine: PoliticsEngine,
    onStartCampaign: (PoliticalOffice, CampaignAgenda, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOffice by remember { mutableStateOf(PoliticalOffice.MAYOR) }
    var selectedAgenda by remember { mutableStateOf(CampaignAgenda.INFRASTRUCTURE) }

    val minInvest = politicsEngine.minCampaignInvestment(selectedOffice, character.countryCode)
    val eligible = politicsEngine.canRunForOffice(character, selectedOffice)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, BorderCardSlate)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Run for Public Office",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
            Text(
                text = "Launch an election campaign. Mobilize voter enthusiasm, build a war chest, and enact signature policy reforms.",
                style = MaterialTheme.typography.bodySmall,
                color = InkTertiary
            )

            // Select Office
            Text(
                text = "Target Office:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = InkSecondary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PoliticalOffice.entries.forEach { office ->
                    FilterChip(
                        selected = selectedOffice == office,
                        onClick = { selectedOffice = office },
                        label = { Text(politicalOfficeLabel(office)) }
                    )
                }
            }

            // Select Agenda Platform
            Text(
                text = "Platform Agenda:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = InkSecondary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CampaignAgenda.entries.forEach { agenda ->
                    FilterChip(
                        selected = selectedAgenda == agenda,
                        onClick = { selectedAgenda = agenda },
                        label = { Text(agendaLabelText(agenda)) }
                    )
                }
            }

            // Investment & Launch Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Campaign Fee:",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkTertiary
                    )
                    Text(
                        text = formatMoney(minInvest, character.countryCode),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                }

                Button(
                    onClick = { onStartCampaign(selectedOffice, selectedAgenda, minInvest) },
                    enabled = eligible && character.stats.money >= minInvest,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Icon(AppIcons.Megaphone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.btn_file_candidacy))
                }
            }

            if (!eligible) {
                Text(
                    text = "Requires age 30+, citizenship, clean record, and executive smarts.",
                    style = MaterialTheme.typography.labelSmall,
                    color = CoralNegative
                )
            }
        }
    }
}

@Composable
fun politicalOfficeLabel(office: PoliticalOffice): String = when (office) {
    PoliticalOffice.MAYOR -> stringResource(R.string.office_mayor)
    PoliticalOffice.GOVERNOR -> stringResource(R.string.office_governor)
    PoliticalOffice.PRESIDENT -> stringResource(R.string.office_president)
}

@Composable
fun agendaLabelText(agenda: CampaignAgenda): String = when (agenda) {
    CampaignAgenda.INFRASTRUCTURE -> stringResource(R.string.agenda_infrastructure)
    CampaignAgenda.EDUCATION -> stringResource(R.string.agenda_education)
    CampaignAgenda.HEALTHCARE -> stringResource(R.string.agenda_healthcare)
    CampaignAgenda.TAX_RELIEF -> stringResource(R.string.agenda_tax_relief)
    CampaignAgenda.ANTI_CORRUPTION -> stringResource(R.string.agenda_anti_corruption)
}
