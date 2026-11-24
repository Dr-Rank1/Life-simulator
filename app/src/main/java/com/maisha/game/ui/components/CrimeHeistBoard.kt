package com.maisha.game.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.maisha.game.data.CountryCatalog
import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.CrimeTarget
import com.maisha.game.data.model.CrimeType
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

private enum class CrimeHeistTier {
    STREET_HUSTLES,
    BURGLARY_GTA,
    ARMED_HEISTS
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CrimeHeistBoard(
    character: Character,
    crimeEngine: CrimeEngine,
    onAttemptStreetCrime: (CrimeType) -> Unit,
    onAttemptTargetedCrime: (CrimeTarget) -> Unit,
    onCaseTarget: (CrimeTarget) -> Unit,
    onLayLow: () -> Unit,
    onBribeInformant: () -> Unit,
    onRequestExpungement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val record = character.criminalRecord
    var selectedTier by remember { mutableIntStateOf(0) }
    val currentTier = CrimeHeistTier.entries[selectedTier]
    val bribeCost = EconomyScaler.scaleAmount(12_000, character.countryCode)
    val canBribe = character.stats.money >= bribeCost && !record.heatActionDoneThisYear

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Underworld Status & Heat Meter Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PaperWhite),
            border = BorderStroke(1.dp, BorderCardSlate),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CoralNegative.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = AppIcons.UnderworldHeat,
                                    contentDescription = null,
                                    tint = CoralNegative,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.underworld_board_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary
                            )
                            val notorietyRank = when {
                                record.notoriety >= 75 -> stringResource(R.string.notoriety_kingpin)
                                record.notoriety >= 40 -> stringResource(R.string.notoriety_infamous)
                                record.notoriety >= 15 -> stringResource(R.string.notoriety_hustler)
                                else -> stringResource(R.string.notoriety_amateur)
                            }
                            Text(
                                text = "$notorietyRank (Notoriety: ${record.notoriety}%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary
                            )
                        }
                    }

                    // Notoriety Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CoralNegative.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${record.notoriety}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CoralNegative,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Heat Level Gauge
                val heatFraction = (record.heatLevel / 100f).coerceIn(0f, 1f)
                val heatColor by animateColorAsState(
                    targetValue = when {
                        record.heatLevel >= 90 -> CoralNegative
                        record.heatLevel >= 70 -> Color(0xFFE65100)
                        record.heatLevel >= 40 -> GoldAccent
                        else -> SuccessGreen
                    },
                    label = "heatColor"
                )

                val heatStatusText = when {
                    record.heatLevel >= 90 -> stringResource(R.string.heat_status_most_wanted)
                    record.heatLevel >= 70 -> stringResource(R.string.heat_status_critical)
                    record.heatLevel >= 45 -> stringResource(R.string.heat_status_hot)
                    record.heatLevel >= 20 -> stringResource(R.string.heat_status_monitored)
                    else -> stringResource(R.string.heat_status_cold)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.heat_meter_title),
                            style = MaterialTheme.typography.labelMedium,
                            color = InkSecondary
                        )
                        Text(
                            text = "${record.heatLevel}/100 — $heatStatusText",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = heatColor
                        )
                    }
                    LinearProgressIndicator(
                        progress = { heatFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = heatColor,
                        trackColor = BorderCardSlate
                    )
                }

                // Heat Countermeasure Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onLayLow,
                        enabled = !record.heatActionDoneThisYear && record.heatLevel > 0,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_lay_low_safehouse),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Button(
                        onClick = onBribeInformant,
                        enabled = canBribe && record.heatLevel > 0,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralNegative),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.btn_bribe_police_informant,
                                formatMoney(bribeCost, character.countryCode)
                            ),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // Tier Filter Chips (Street Crimes, Burglary & GTA, Major Heists)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CrimeHeistTier.entries.forEachIndexed { index, tier ->
                val label = when (tier) {
                    CrimeHeistTier.STREET_HUSTLES -> stringResource(R.string.tab_street_crimes)
                    CrimeHeistTier.BURGLARY_GTA -> stringResource(R.string.tab_property_gta)
                    CrimeHeistTier.ARMED_HEISTS -> stringResource(R.string.tab_major_heists)
                }
                FilterChip(
                    selected = selectedTier == index,
                    onClick = { selectedTier = index },
                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CoralNegative,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Job & Heist Opportunity Cards based on Selected Tier
        when (currentTier) {
            CrimeHeistTier.STREET_HUSTLES -> {
                StreetHustleCard(
                    title = stringResource(R.string.crime_pickpocket_title),
                    description = stringResource(
                        R.string.crime_pickpocket_desc,
                        CountryCatalog.flavorFor(character.countryCode).commonTransportMode
                    ),
                    estPayout = formatMoney(EconomyScaler.scaleAmount(450, character.countryCode), character.countryCode),
                    heatGain = 8,
                    oddsPercent = (crimeEngine.successChance(character, CrimeType.PICKPOCKET) * 100).toInt(),
                    icon = AppIcons.CrimePickpocket,
                    onExecute = { onAttemptStreetCrime(CrimeType.PICKPOCKET) }
                )

                StreetHustleCard(
                    title = stringResource(R.string.crime_shoplift_title),
                    description = stringResource(R.string.crime_shoplift_desc),
                    estPayout = formatMoney(EconomyScaler.scaleAmount(1_200, character.countryCode), character.countryCode),
                    heatGain = 12,
                    oddsPercent = (crimeEngine.successChance(character, CrimeType.SHOPLIFT) * 100).toInt(),
                    icon = AppIcons.CrimeShoplift,
                    onExecute = { onAttemptStreetCrime(CrimeType.SHOPLIFT) }
                )

                StreetHustleCard(
                    title = stringResource(R.string.crime_fraud_title),
                    description = stringResource(R.string.crime_fraud_desc),
                    estPayout = formatMoney(EconomyScaler.scaleAmount(6_500, character.countryCode), character.countryCode),
                    heatGain = 18,
                    oddsPercent = (crimeEngine.successChance(character, CrimeType.FRAUD) * 100).toInt(),
                    icon = AppIcons.CrimeFraud,
                    onExecute = { onAttemptStreetCrime(CrimeType.FRAUD) }
                )
            }

            CrimeHeistTier.BURGLARY_GTA -> {
                val targets = listOf(
                    CrimeTarget.BURGLARY_APARTMENT,
                    CrimeTarget.BURGLARY_SUBURB,
                    CrimeTarget.BURGLARY_MANSION,
                    CrimeTarget.GTA_SEDAN,
                    CrimeTarget.GTA_LUXURY_SUV,
                    CrimeTarget.GTA_SUPERCAR
                )
                targets.forEach { target ->
                    TargetedHeistCard(
                        target = target,
                        character = character,
                        crimeEngine = crimeEngine,
                        onExecute = { onAttemptTargetedCrime(target) },
                        onCase = { onCaseTarget(target) }
                    )
                }
            }

            CrimeHeistTier.ARMED_HEISTS -> {
                val heists = listOf(
                    CrimeTarget.HEIST_CONVENIENCE,
                    CrimeTarget.HEIST_JEWELRY,
                    CrimeTarget.HEIST_BANK_VAULT
                )
                heists.forEach { target ->
                    TargetedHeistCard(
                        target = target,
                        character = character,
                        crimeEngine = crimeEngine,
                        onExecute = { onAttemptTargetedCrime(target) },
                        onCase = { onCaseTarget(target) }
                    )
                }
            }
        }

        // Expungement Action Card if record exists
        if (record.hasRecord && !record.recordExpunged) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CreamBg),
                border = BorderStroke(1.dp, BorderCardSlate),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.btn_request_expungement),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = stringResource(
                                R.string.dialog_expunge_body,
                                formatMoney(crimeEngine.expungementFee(character), character.countryCode)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                    Button(
                        onClick = onRequestExpungement,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text(stringResource(R.string.btn_request_expungement))
                    }
                }
            }
        }
    }
}

@Composable
private fun StreetHustleCard(
    title: String,
    description: String,
    estPayout: String,
    heatGain: Int,
    oddsPercent: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onExecute: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, BorderCardSlate),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CoralNegative.copy(alpha = 0.10f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = CoralNegative, modifier = Modifier.size(20.dp))
                        }
                    }
                    Column {
                        Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = InkPrimary)
                        Text(text = description, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                    }
                }
            }

            // Metrics Pill Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    label = stringResource(R.string.label_payout_range, estPayout),
                    color = SuccessGreen
                )
                MetricChip(
                    label = stringResource(R.string.label_heat_risk, heatGain),
                    color = CoralNegative
                )
                MetricChip(
                    label = stringResource(R.string.label_success_chance, oddsPercent),
                    color = if (oddsPercent >= 60) SuccessGreen else GoldAccent
                )
            }

            Button(
                onClick = onExecute,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralNegative),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(AppIcons.HeistMask, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.btn_execute_crime), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TargetedHeistCard(
    target: CrimeTarget,
    character: Character,
    crimeEngine: CrimeEngine,
    onExecute: () -> Unit,
    onCase: () -> Unit
) {
    val isCased = character.criminalRecord.casedTarget == target
    val scaledPayout = EconomyScaler.scaleAmount(target.baseReward, character.countryCode)
    val odds = (crimeEngine.calculateTargetCrimeSuccessChance(character, target) * 100).toInt()
    val minAge = if (target.category == "Armed Heist") 18 else 16
    val lockedByAge = character.age < minAge

    val heatCost = when (target.category) {
        "Armed Heist" -> 30
        else -> 15
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, if (isCased) GoldAccent else BorderCardSlate),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCased) GoldAccent.copy(alpha = 0.15f) else CoralNegative.copy(alpha = 0.10f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isCased) AppIcons.Smarts else AppIcons.HeistMask,
                                contentDescription = null,
                                tint = if (isCased) GoldAccent else CoralNegative,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "${target.category}: ${target.name.lowercase().replace('_', ' ').replaceFirstChar { it.titlecase() }}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = "Notoriety: +${target.notorietyGain}% on clean score",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }

                if (isCased) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldAccent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = stringResource(R.string.label_target_cased_bonus),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    label = stringResource(R.string.label_payout_range, formatMoney(scaledPayout, character.countryCode)),
                    color = SuccessGreen
                )
                MetricChip(
                    label = stringResource(R.string.label_heat_risk, heatCost),
                    color = CoralNegative
                )
                MetricChip(
                    label = stringResource(R.string.label_success_chance, odds),
                    color = if (odds >= 50) SuccessGreen else GoldAccent
                )
            }

            if (lockedByAge) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BorderCardSlate.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.label_age_gate_heist),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkTertiary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isCased && character.criminalRecord.heatLevel < 85) {
                        OutlinedButton(
                            onClick = onCase,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(AppIcons.Smarts, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.btn_case_blueprint), style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Button(
                        onClick = onExecute,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralNegative),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(AppIcons.HeistMask, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.btn_execute_crime), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}
