package com.maisha.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.maisha.game.R
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.ConfrontationChoice
import com.maisha.game.data.model.SecretConfrontation
import com.maisha.game.data.model.SecretIdentity
import com.maisha.game.data.model.SecretIdentityType
import com.maisha.game.ui.theme.BorderCardSlate
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.DeepSlate
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.Hairline
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.TealPrimary
import com.maisha.game.util.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoubleLifeSheet(
    character: Character,
    onAdoptSecret: (SecretIdentityType, String, String?) -> Unit,
    onPlantAlibi: () -> Unit,
    onScrubTracks: () -> Unit,
    onExploitSecret: () -> Unit,
    onComeClean: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val secret = character.doubleLife.activeSecret
    var adoptingType by remember { mutableStateOf<SecretIdentityType?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PaperWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.sheet_double_life_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    Text(
                        text = stringResource(R.string.sheet_double_life_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = InkSecondary
                    )
                }
            }

            if (secret != null) {
                ActiveSecretStatusCard(
                    secret = secret,
                    countryCode = character.countryCode,
                    onPlantAlibi = onPlantAlibi,
                    onScrubTracks = onScrubTracks,
                    onExploitSecret = onExploitSecret,
                    onComeClean = onComeClean
                )
            } else {
                Text(
                    text = stringResource(R.string.label_available_identities),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )

                SecretIdentityType.entries.forEach { type ->
                    val isAgeMet = character.age >= type.minAge
                    AvailableIdentityCard(
                        type = type,
                        isEligible = isAgeMet,
                        onSelect = { adoptingType = type }
                    )
                }
            }

            if (character.doubleLife.exposedHistory.isNotEmpty()) {
                PastExposuresCard(character = character)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    adoptingType?.let { type ->
        AdoptIdentityDialog(
            type = type,
            onConfirm = { codename, cover ->
                onAdoptSecret(type, codename, cover)
                adoptingType = null
            },
            onDismiss = { adoptingType = null }
        )
    }
}

@Composable
private fun ActiveSecretStatusCard(
    secret: SecretIdentity,
    countryCode: String,
    onPlantAlibi: () -> Unit,
    onScrubTracks: () -> Unit,
    onExploitSecret: () -> Unit,
    onComeClean: () -> Unit
) {
    val tensionColor = when {
        secret.exposureRisk >= 75 -> CoralNegative
        secret.exposureRisk >= 45 -> GoldAccent
        else -> LifeGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.5.dp, BorderCardSlate),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TealPrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = secret.type.title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = tensionColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "EXPOSURE ${secret.exposureRisk}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = tensionColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = secret.codename,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )

            Text(
                text = stringResource(R.string.label_cover_story, secret.coverStory),
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary
            )

            // Meters
            DoubleLifeMeterRow(
                label = stringResource(R.string.label_exposure_tension, secret.exposureRisk),
                value = secret.exposureRisk,
                meterColor = tensionColor
            )

            DoubleLifeMeterRow(
                label = stringResource(R.string.label_suspicion_level, secret.suspicionLevel),
                value = secret.suspicionLevel,
                meterColor = CoralNegative
            )

            DoubleLifeMeterRow(
                label = stringResource(R.string.label_alibi_strength, secret.alibiStrength),
                value = secret.alibiStrength,
                meterColor = LifeGreen
            )

            if (secret.annualPayoff > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.label_annual_payoff, formatMoney(secret.annualPayoff, countryCode)),
                        style = MaterialTheme.typography.labelSmall,
                        color = InkSecondary
                    )
                    Text(
                        text = stringResource(R.string.label_total_earnings, formatMoney(secret.totalEarningsCollected, countryCode)),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = LifeGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Actions Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPlantAlibi,
                    enabled = !secret.actionDoneThisYear,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_plant_alibi),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                OutlinedButton(
                    onClick = onScrubTracks,
                    enabled = !secret.actionDoneThisYear,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_scrub_tracks),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onExploitSecret,
                    enabled = !secret.actionDoneThisYear,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text(
                        text = stringResource(R.string.btn_exploit_secret),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                OutlinedButton(
                    onClick = onComeClean,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralNegative)
                ) {
                    Text(
                        text = stringResource(R.string.btn_come_clean),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DoubleLifeMeterRow(
    label: String,
    value: Int,
    meterColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = InkSecondary,
            fontSize = 11.sp
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(BorderCardSlate.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (value / 100f).coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(meterColor)
            )
        }
    }
}

@Composable
private fun AvailableIdentityCard(
    type: SecretIdentityType,
    isEligible: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = isEligible, onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        color = PaperWhite,
        border = BorderStroke(1.dp, if (isEligible) BorderCardSlate else Hairline)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = type.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isEligible) InkPrimary else InkTertiary
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isEligible) TealPrimary.copy(alpha = 0.1f) else BorderCardSlate
                ) {
                    Text(
                        text = "Age ${type.minAge}+",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isEligible) TealPrimary else InkTertiary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = type.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isEligible) InkSecondary else InkTertiary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun PastExposuresCard(character: Character) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BorderCardSlate.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.section_past_identities),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = InkSecondary
            )
            character.doubleLife.exposedHistory.forEach { record ->
                Text(
                    text = "Age ${record.yearExposed}: ${record.codename} (${record.type.title})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = InkPrimary
                )
                Text(
                    text = record.falloutSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun AdoptIdentityDialog(
    type: SecretIdentityType,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var codename by remember { mutableStateOf(type.title) }
    var coverStory by remember { mutableStateOf(type.defaultCoverStory) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PaperWhite),
            border = BorderStroke(1.dp, BorderCardSlate)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.dialog_adopt_secret_title, type.title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )

                Text(
                    text = stringResource(R.string.dialog_adopt_secret_prompt),
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )

                OutlinedTextField(
                    value = codename,
                    onValueChange = { codename = it },
                    label = { Text(stringResource(R.string.label_codename_field)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = coverStory,
                    onValueChange = { coverStory = it },
                    label = { Text(stringResource(R.string.label_cover_story_field)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onConfirm(codename, coverStory) },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text(stringResource(R.string.btn_initiate_double_life))
                    }
                }
            }
        }
    }
}

/**
 * Dramatic confrontation dialog triggered when a double life's cover cracks.
 */
@Composable
fun SecretConfrontationDialog(
    confrontation: SecretConfrontation,
    countryCode: String,
    onChoice: (ConfrontationChoice) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PaperWhite),
            border = BorderStroke(1.5.dp, CoralNegative.copy(alpha = 0.6f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CoralNegative.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = CoralNegative,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.dialog_confrontation_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CoralNegative
                        )
                        Text(
                            text = stringResource(R.string.label_confronted_by, confrontation.confrontedBy),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = InkPrimary
                        )
                    }
                }

                Text(
                    text = confrontation.triggerReason,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                    lineHeight = 16.sp
                )

                // Choice 1: Double Down & Bluff
                ConfrontationChoiceCard(
                    title = stringResource(R.string.btn_choice_double_down),
                    description = stringResource(R.string.desc_choice_double_down),
                    accentColor = TealPrimary,
                    onClick = { onChoice(ConfrontationChoice.DOUBLE_DOWN) }
                )

                // Choice 2: Bribe Silence
                ConfrontationChoiceCard(
                    title = stringResource(R.string.btn_choice_bribe_silence, formatMoney(confrontation.silenceBribeCost, countryCode)),
                    description = stringResource(R.string.desc_choice_bribe_silence),
                    accentColor = GoldAccent,
                    onClick = { onChoice(ConfrontationChoice.BRIBE_SILENCE) }
                )

                // Choice 3: Burn Identity & Vanish
                ConfrontationChoiceCard(
                    title = stringResource(R.string.btn_choice_cut_ties),
                    description = stringResource(R.string.desc_choice_cut_ties),
                    accentColor = DeepSlate,
                    onClick = { onChoice(ConfrontationChoice.CUT_TIES_VANISH) }
                )

                // Choice 4: Face Fallout
                ConfrontationChoiceCard(
                    title = stringResource(R.string.btn_choice_confess),
                    description = stringResource(R.string.desc_choice_confess),
                    accentColor = CoralNegative,
                    onClick = { onChoice(ConfrontationChoice.CONFESS_FALLOUT) }
                )
            }
        }
    }
}

@Composable
private fun ConfrontationChoiceCard(
    title: String,
    description: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = PaperWhite,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontSize = 13.sp
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
