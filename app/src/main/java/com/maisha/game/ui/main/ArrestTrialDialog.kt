// app/src/main/java/com/maisha/game/ui/main/ArrestTrialDialog.kt
package com.maisha.game.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.CrimeType
import com.maisha.game.data.model.LawyerTier
import com.maisha.game.domain.CrimeEngine
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

@Composable
fun ArrestTrialDialog(
    character: Character,
    publicDefenderAffordable: Boolean,
    averageFee: Int,
    averageAffordable: Boolean,
    expensiveFee: Int,
    expensiveAffordable: Boolean,
    onSelectLawyer: (LawyerTier) -> Unit
) {
    val crimeEngine = remember { CrimeEngine() }
    val record = character.criminalRecord
    val crimeType = runCatching {
        record.pendingCrimeType?.let { CrimeType.valueOf(it) }
    }.getOrNull() ?: CrimeType.PICKPOCKET

    val maxSentence = crimeEngine.maxSentenceYears(crimeType, record.timesArrested)
    val prosecutionStrength = (crimeEngine.calculateProsecutionStrength(character) * 100).toInt()
    val publicOdds = (crimeEngine.calculateAcquittalChance(character, LawyerTier.PUBLIC_DEFENDER) * 100).toInt()
    val averageOdds = (crimeEngine.calculateAcquittalChance(character, LawyerTier.AVERAGE) * 100).toInt()
    val expensiveOdds = (crimeEngine.calculateAcquittalChance(character, LawyerTier.EXPENSIVE) * 100).toInt()

    var selectedTier by remember { mutableStateOf(LawyerTier.PUBLIC_DEFENDER) }

    AlertDialog(
        onDismissRequest = { },
        containerColor = PaperWhite,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CoralNegative.copy(alpha = 0.12f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = AppIcons.CourtGavel,
                            contentDescription = null,
                            tint = CoralNegative,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = stringResource(R.string.court_proceedings_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = InkPrimary
                    )
                    Text(
                        text = stringResource(R.string.court_case_heading, character.name),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Charge & Sentence Exposure Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CreamBg,
                    border = BorderStroke(1.dp, BorderCardSlate),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val crimeName = when (crimeType) {
                                CrimeType.PICKPOCKET -> stringResource(R.string.crime_type_pickpocket)
                                CrimeType.SHOPLIFT -> stringResource(R.string.crime_type_shoplift)
                                CrimeType.FRAUD -> stringResource(R.string.crime_type_fraud)
                            }
                            Text(
                                text = stringResource(R.string.court_charge_dossier, crimeName),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CoralNegative
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CoralNegative.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "Arrest #${record.timesArrested}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralNegative,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.court_max_sentence_warning, maxSentence),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }

                // Prosecution Evidence Strength Meter
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.court_prosecution_meter, prosecutionStrength),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = InkSecondary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { prosecutionStrength / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CoralNegative,
                        trackColor = BorderCardSlate
                    )
                }

                // Defense Options Stepper / Cards
                Text(
                    text = "Select Defense Strategy & Legal Counsel",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )

                // 1. Public Defender
                DefenseCounselCard(
                    title = stringResource(R.string.lawyer_public_defender),
                    description = stringResource(R.string.lawyer_public_defender_desc),
                    feeLabel = stringResource(R.string.lawyer_fee_free),
                    acquittalOdds = publicOdds,
                    selected = selectedTier == LawyerTier.PUBLIC_DEFENDER,
                    enabled = publicDefenderAffordable,
                    onClick = { selectedTier = LawyerTier.PUBLIC_DEFENDER }
                )

                // 2. Retained Attorney
                DefenseCounselCard(
                    title = stringResource(R.string.lawyer_average),
                    description = stringResource(R.string.lawyer_average_desc),
                    feeLabel = stringResource(
                        R.string.lawyer_fee_amount,
                        formatMoney(averageFee, character.countryCode)
                    ),
                    acquittalOdds = averageOdds,
                    selected = selectedTier == LawyerTier.AVERAGE,
                    enabled = averageAffordable,
                    onClick = { selectedTier = LawyerTier.AVERAGE }
                )

                // 3. Elite Defense Firm
                DefenseCounselCard(
                    title = stringResource(R.string.lawyer_expensive),
                    description = stringResource(R.string.lawyer_expensive_desc),
                    feeLabel = stringResource(
                        R.string.lawyer_fee_amount,
                        formatMoney(expensiveFee, character.countryCode)
                    ),
                    acquittalOdds = expensiveOdds,
                    selected = selectedTier == LawyerTier.EXPENSIVE,
                    enabled = expensiveAffordable,
                    onClick = { selectedTier = LawyerTier.EXPENSIVE }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSelectLawyer(selectedTier) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTier == LawyerTier.EXPENSIVE) GoldAccent else TealPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Icon(AppIcons.CourtGavel, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.btn_enter_court_defense),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = { }
    )
}

@Composable
private fun DefenseCounselCard(
    title: String,
    description: String,
    feeLabel: String,
    acquittalOdds: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) CreamBg else PaperWhite,
            disabledContainerColor = BorderCardSlate.copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) TealPrimary else BorderCardSlate
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (enabled) InkPrimary else InkTertiary
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (acquittalOdds >= 50) SuccessGreen.copy(alpha = 0.15f) else GoldAccent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "$acquittalOdds% Odds",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (acquittalOdds >= 50) SuccessGreen else GoldAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) InkSecondary else InkTertiary
                )
                Text(
                    text = feeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) GoldAccent else InkTertiary
                )
            }
        }
    }
}
