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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import com.maisha.game.R
import com.maisha.game.data.CountryCatalog
import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.CareTier
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.HealthCondition
import com.maisha.game.data.model.LifestyleOption
import com.maisha.game.domain.HealthEngine
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.BorderCardSlate
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.CreamBg
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.SuccessGreen
import com.maisha.game.ui.theme.TealPrimary
import com.maisha.game.util.formatMoney

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HealthWellnessHub(
    character: Character,
    onVisitDoctor: (String, CareTier) -> Unit,
    onSetLifestyleOption: (LifestyleOption, Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val healthEngine = remember { HealthEngine() }
    val untreated = character.activeConditions.filter { !it.treated }
    var selectedTreatment by remember { mutableStateOf<Triple<HealthCondition, CareTier, Int>?>(null) }
    var lifestyleToConfirm by remember { mutableStateOf<Pair<LifestyleOption, Boolean>?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Vitality Header Card
        item {
            VitalitySummaryCard(
                character = character,
                untreatedCount = untreated.size
            )
        }

        // Infancy Protection Banner
        if (character.age < 3) {
            item {
                InfancyProtectionBanner()
            }
        }

        // Active Conditions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.label_active_conditions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )
                if (untreated.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CoralNegative.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, CoralNegative.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "${untreated.size} Unresolved",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CoralNegative,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Active Conditions or Clean Bill of Health
        if (untreated.isEmpty()) {
            item {
                CleanBillOfHealthCard()
            }
        } else {
            items(untreated, key = { it.id }) { condition ->
                ConditionTreatmentCard(
                    character = character,
                    condition = condition,
                    healthEngine = healthEngine,
                    onSelectTier = { tier, cost ->
                        selectedTreatment = Triple(condition, tier, cost)
                    }
                )
            }
        }

        // Preventative Wellness & Subscriptions Section
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.label_wellness_subscriptions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
        }

        item {
            WellnessSubscriptionsSection(
                character = character,
                onToggleLifestyle = { option, enable ->
                    lifestyleToConfirm = Pair(option, enable)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Treatment Confirmation Dialog
    selectedTreatment?.let { (condition, tier, cost) ->
        val canAfford = character.stats.money >= cost
        val tierTitle = when (tier) {
            CareTier.PUBLIC_CLINIC -> stringResource(R.string.care_public_clinic)
            CareTier.PRIVATE_HOSPITAL -> CountryCatalog.flavorFor(character.countryCode).privateHospitalName
            CareTier.ALTERNATIVE_MEDICINE -> stringResource(R.string.care_alternative_medicine)
            CareTier.MENTAL_WELLNESS -> stringResource(R.string.care_mental_wellness)
        }
        val odds = healthEngine.calculateRecoveryOdds(character, condition, tier)

        Dialog(onDismissRequest = { selectedTreatment = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PaperWhite,
                border = BorderStroke(1.dp, BorderCardSlate),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.dialog_seek_treatment_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    Text(
                        text = "Undergo treatment for ${condition.name} at $tierTitle. Projected cure rate: $odds%." +
                            (if (cost > 0) " Cost: ${formatMoney(cost, character.countryCode)}." else " Free pediatric care."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedTreatment = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.btn_cancel))
                        }
                        Button(
                            onClick = {
                                selectedTreatment = null
                                onVisitDoctor(condition.id, tier)
                            },
                            enabled = canAfford,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (tier == CareTier.PRIVATE_HOSPITAL) TealPrimary else LifeGreen
                            )
                        ) {
                            Text(
                                text = if (canAfford) "Confirm" else "Insufficient Funds",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Lifestyle Subscription Toggle Confirmation Dialog
    lifestyleToConfirm?.let { (option, enable) ->
        val cost = when (option) {
            LifestyleOption.GYM -> EconomyScaler.scaleAmount(HealthEngine.GYM_YEARLY_COST, character.countryCode)
            LifestyleOption.DIET -> EconomyScaler.scaleAmount(HealthEngine.DIET_YEARLY_COST, character.countryCode)
            LifestyleOption.THERAPIST -> EconomyScaler.scaleAmount(HealthEngine.THERAPIST_YEARLY_COST, character.countryCode)
            LifestyleOption.HEALTH_INSURANCE -> EconomyScaler.scaleAmount(HealthEngine.HEALTH_INSURANCE_YEARLY_COST, character.countryCode)
        }
        val optionName = when (option) {
            LifestyleOption.GYM -> "Gym Membership"
            LifestyleOption.DIET -> "Premium Diet"
            LifestyleOption.THERAPIST -> "Personal Therapy"
            LifestyleOption.HEALTH_INSURANCE -> "Health Insurance"
        }

        Dialog(onDismissRequest = { lifestyleToConfirm = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PaperWhite,
                border = BorderStroke(1.dp, BorderCardSlate),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (enable) stringResource(R.string.dialog_lifestyle_enable_title) else stringResource(R.string.dialog_lifestyle_disable_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    Text(
                        text = if (enable) {
                            stringResource(
                                R.string.dialog_lifestyle_enable_desc,
                                optionName,
                                formatMoney(cost, character.countryCode)
                            )
                        } else {
                            stringResource(R.string.dialog_lifestyle_disable_desc, optionName)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { lifestyleToConfirm = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.btn_cancel))
                        }
                        Button(
                            onClick = {
                                lifestyleToConfirm = null
                                onSetLifestyleOption(option, enable)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (enable) TealPrimary else CoralNegative
                            )
                        ) {
                            Text(
                                text = if (enable) "Activate" else "Cancel Plan",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VitalitySummaryCard(
    character: Character,
    untreatedCount: Int
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, BorderCardSlate),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(TealPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppIcons.Health,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.label_vitality),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = if (untreatedCount > 0) "$untreatedCount active health issues" else "Optimal physiological condition",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (untreatedCount > 0) CoralNegative else InkSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (character.lifestyle.hasHealthInsurance) SuccessGreen.copy(alpha = 0.12f) else CreamBg,
                    border = BorderStroke(
                        1.dp,
                        if (character.lifestyle.hasHealthInsurance) SuccessGreen.copy(alpha = 0.35f) else BorderCardSlate
                    )
                ) {
                    Text(
                        text = if (character.lifestyle.hasHealthInsurance) "Insured (35% Copay)" else "Uninsured",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (character.lifestyle.hasHealthInsurance) SuccessGreen else InkTertiary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Health Meter
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Physical Health",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkSecondary
                    )
                    Text(
                        text = "${character.stats.health}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (character.stats.health >= 50) SuccessGreen else CoralNegative
                    )
                }
                LinearProgressIndicator(
                    progress = { character.stats.health / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (character.stats.health >= 50) SuccessGreen else CoralNegative,
                    trackColor = CreamBg
                )
            }

            // Happiness / Mental Balance Meter
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Mental Equilibrium",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkSecondary
                    )
                    Text(
                        text = "${character.stats.happiness}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (character.stats.happiness >= 50) GoldAccent else CoralNegative
                    )
                }
                LinearProgressIndicator(
                    progress = { character.stats.happiness / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (character.stats.happiness >= 50) GoldAccent else CoralNegative,
                    trackColor = CreamBg
                )
            }
        }
    }
}

@Composable
private fun InfancyProtectionBanner() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SuccessGreen.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.Peaceful,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.label_infant_care_free),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
                Text(
                    text = "Toddlers and infants receive 100% free medical care with maximum cure rates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )
            }
        }
    }
}

@Composable
private fun CleanBillOfHealthCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, BorderCardSlate),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.Peaceful,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(26.dp)
                )
            }
            Text(
                text = stringResource(R.string.label_no_conditions),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = InkPrimary
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConditionTreatmentCard(
    character: Character,
    condition: HealthCondition,
    healthEngine: HealthEngine,
    onSelectTier: (CareTier, Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, BorderCardSlate),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Condition Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = condition.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    if (condition.yearsUntreated > 0) {
                        Text(
                            text = if (condition.yearsUntreated == 1) {
                                stringResource(R.string.label_years_untreated, condition.yearsUntreated)
                            } else {
                                stringResource(R.string.label_years_untreated_plural, condition.yearsUntreated)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = CoralNegative
                        )
                    }
                }

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val severityLabel = when (condition.severity) {
                        1 -> stringResource(R.string.label_severity_mild)
                        2 -> stringResource(R.string.label_severity_moderate)
                        else -> stringResource(R.string.label_severity_severe)
                    }
                    val severityColor = when (condition.severity) {
                        1 -> SuccessGreen
                        2 -> GoldAccent
                        else -> CoralNegative
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = severityColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, severityColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = severityLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = severityColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (condition.isChronic) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TealPrimary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = stringResource(R.string.label_chronic_condition),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // 4 Treatment Tiers Comparison
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CareTierRow(
                    character = character,
                    condition = condition,
                    tier = CareTier.PUBLIC_CLINIC,
                    healthEngine = healthEngine,
                    icon = AppIcons.HealthClinic,
                    name = stringResource(R.string.care_public_clinic),
                    description = stringResource(R.string.care_tier_public_desc),
                    onSelect = onSelectTier
                )

                CareTierRow(
                    character = character,
                    condition = condition,
                    tier = CareTier.PRIVATE_HOSPITAL,
                    healthEngine = healthEngine,
                    icon = AppIcons.HealthHospital,
                    name = CountryCatalog.flavorFor(character.countryCode).privateHospitalName,
                    description = stringResource(R.string.care_tier_private_desc),
                    onSelect = onSelectTier
                )

                CareTierRow(
                    character = character,
                    condition = condition,
                    tier = CareTier.ALTERNATIVE_MEDICINE,
                    healthEngine = healthEngine,
                    icon = AppIcons.Peaceful,
                    name = stringResource(R.string.care_alternative_medicine),
                    description = stringResource(R.string.care_tier_alternative_desc),
                    onSelect = onSelectTier
                )

                CareTierRow(
                    character = character,
                    condition = condition,
                    tier = CareTier.MENTAL_WELLNESS,
                    healthEngine = healthEngine,
                    icon = AppIcons.Happiness,
                    name = stringResource(R.string.care_mental_wellness),
                    description = stringResource(R.string.care_tier_therapy_desc),
                    onSelect = onSelectTier
                )
            }
        }
    }
}

@Composable
private fun CareTierRow(
    character: Character,
    condition: HealthCondition,
    tier: CareTier,
    healthEngine: HealthEngine,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    name: String,
    description: String,
    onSelect: (CareTier, Int) -> Unit
) {
    val cost = healthEngine.estimateTreatmentCost(character, condition.severity, tier)
    val odds = healthEngine.calculateRecoveryOdds(character, condition, tier)
    val canAfford = character.stats.money >= cost

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CreamBg.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, BorderCardSlate),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(TealPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (odds >= 75) SuccessGreen.copy(alpha = 0.12f) else GoldAccent.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "$odds% Odds",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (odds >= 75) SuccessGreen else GoldAccent,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                    maxLines = 1
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (cost > 0) formatMoney(cost, character.countryCode) else "Free",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (canAfford) TealPrimary else CoralNegative
                )
                Button(
                    onClick = { onSelect(tier, cost) },
                    enabled = canAfford,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Treat",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun WellnessSubscriptionsSection(
    character: Character,
    onToggleLifestyle: (LifestyleOption, Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SubscriptionRow(
            title = "Gym Membership",
            description = stringResource(R.string.lifestyle_gym_desc),
            cost = EconomyScaler.scaleAmount(HealthEngine.GYM_YEARLY_COST, character.countryCode),
            active = character.lifestyle.hasGymMembership,
            countryCode = character.countryCode,
            onToggle = { onToggleLifestyle(LifestyleOption.GYM, !character.lifestyle.hasGymMembership) }
        )

        SubscriptionRow(
            title = "Premium Nutrition & Diet",
            description = stringResource(R.string.lifestyle_diet_desc),
            cost = EconomyScaler.scaleAmount(HealthEngine.DIET_YEARLY_COST, character.countryCode),
            active = character.lifestyle.isVegan,
            countryCode = character.countryCode,
            onToggle = { onToggleLifestyle(LifestyleOption.DIET, !character.lifestyle.isVegan) }
        )

        SubscriptionRow(
            title = "Personal Therapy & Counseling",
            description = stringResource(R.string.lifestyle_therapist_desc),
            cost = EconomyScaler.scaleAmount(HealthEngine.THERAPIST_YEARLY_COST, character.countryCode),
            active = character.lifestyle.hasTherapist,
            countryCode = character.countryCode,
            onToggle = { onToggleLifestyle(LifestyleOption.THERAPIST, !character.lifestyle.hasTherapist) }
        )

        SubscriptionRow(
            title = "Health Insurance Coverage",
            description = stringResource(R.string.lifestyle_insurance_desc),
            cost = EconomyScaler.scaleAmount(HealthEngine.HEALTH_INSURANCE_YEARLY_COST, character.countryCode),
            active = character.lifestyle.hasHealthInsurance,
            countryCode = character.countryCode,
            ageLocked = character.age < 18,
            onToggle = { onToggleLifestyle(LifestyleOption.HEALTH_INSURANCE, !character.lifestyle.hasHealthInsurance) }
        )
    }
}

@Composable
private fun SubscriptionRow(
    title: String,
    description: String,
    cost: Int,
    active: Boolean,
    countryCode: String,
    ageLocked: Boolean = false,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PaperWhite,
        border = BorderStroke(1.dp, if (active) TealPrimary.copy(alpha = 0.5f) else BorderCardSlate),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    if (active) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SuccessGreen.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = SuccessGreen,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${formatMoney(cost, countryCode)} / year",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TealPrimary
                )
            }

            if (ageLocked) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CreamBg
                ) {
                    Text(
                        text = "Age 18+",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkTertiary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onToggle,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (active) CoralNegative else TealPrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (active) "Cancel" else "Enroll",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (active) CoralNegative else TealPrimary
                    )
                }
            }
        }
    }
}
