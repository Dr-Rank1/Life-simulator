package com.maisha.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.CorporateFare
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.R
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.Job
import com.maisha.game.data.model.WorkEffort
import com.maisha.game.ui.theme.BorderCardSlate
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.CreamBg
import com.maisha.game.ui.theme.CreamMuted
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.SuccessGreen
import com.maisha.game.ui.theme.TealPrimary
import com.maisha.game.util.formatMoney

@Composable
fun CareerHeroCard(
    character: Character,
    job: Job,
    promoReady: Boolean,
    modifier: Modifier = Modifier
) {
    val companyName = character.career.companyName ?: stringResource(R.string.career_company_unknown)
    val performance = character.career.performance.toInt().coerceIn(0, 100)
    val stress = character.career.stress.toInt().coerceIn(0, 100)

    // Role vector badge configuration
    val (roleIcon, roleColor) = resolveRoleBadge(job)

    // Corporate Tier (1 = Entry, 2 = Mid, 3 = Senior, 4 = Lead, 5 = Director, 6+ = Executive)
    val tierIndex = job.level.coerceIn(1, 6) - 1

    // Raise projection label
    val raiseProjectionLabel = when {
        promoReady -> stringResource(R.string.career_projected_raise_promo)
        character.career.plannedWorkEffort == WorkEffort.GRIND -> stringResource(R.string.career_projected_raise_grind)
        character.career.plannedWorkEffort == WorkEffort.COAST -> stringResource(R.string.career_projected_raise_coast)
        else -> stringResource(R.string.career_projected_raise_normal)
    }

    // Performance state
    val (perfStateRes, perfColor) = when {
        performance >= 85 -> R.string.performance_state_stellar to SuccessGreen
        performance >= 65 -> R.string.performance_state_strong to TealPrimary
        performance >= 35 -> R.string.performance_state_satisfactory to GoldAccent
        else -> R.string.performance_state_at_risk to CoralNegative
    }

    // Burnout & Stress state
    val isBurnoutCritical = stress >= 75
    val (burnoutRes, burnoutColor) = when {
        isBurnoutCritical -> R.string.burnout_warning_critical to CoralNegative
        stress >= 50 -> R.string.burnout_warning_elevated to GoldAccent
        else -> R.string.burnout_warning_calm to TealPrimary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, if (promoReady) SuccessGreen.copy(alpha = 0.5f) else BorderCardSlate),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Role Badge + Title + Company
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(roleColor.copy(alpha = 0.12f))
                        .border(1.5.dp, roleColor.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = roleIcon,
                        contentDescription = null,
                        tint = roleColor,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TealPrimary.copy(alpha = 0.10f)
                        ) {
                            Text(
                                text = companyName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TealPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = stringResource(R.string.format_tenure_years, character.career.yearsAtCurrentJob),
                            style = MaterialTheme.typography.labelSmall,
                            color = InkTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Corporate Tier Stepper
            CorporateTierStepper(activeTier = tierIndex)

            Spacer(modifier = Modifier.height(12.dp))

            // Salary & Annual Raise Projection Pill
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CreamMuted,
                border = BorderStroke(1.dp, BorderCardSlate)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${formatMoney(job.baseSalary, character.countryCode)} / yr",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (promoReady) SuccessGreen.copy(alpha = 0.14f) else GoldAccent.copy(alpha = 0.14f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = if (promoReady) SuccessGreen else GoldAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = raiseProjectionLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (promoReady) SuccessGreen else GoldAccent
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Performance Meter with Color States
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.stat_performance),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = InkSecondary
                    )
                    Text(
                        text = "${stringResource(perfStateRes)} ($performance%)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = perfColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CreamBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (performance / 100f).coerceIn(0f, 1f))
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(perfColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Burnout / Stress Warning Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isBurnoutCritical) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = CoralNegative,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.stat_workplace_stress),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isBurnoutCritical) CoralNegative else InkSecondary
                        )
                    }

                    Text(
                        text = "${stringResource(burnoutRes)} ($stress%)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = burnoutColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CreamBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (stress / 100f).coerceIn(0f, 1f))
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(burnoutColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun CorporateTierStepper(activeTier: Int) {
    val tiers = listOf(
        R.string.tier_entry,
        R.string.tier_mid,
        R.string.tier_senior,
        R.string.tier_lead,
        R.string.tier_director,
        R.string.tier_executive
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tiers.forEachIndexed { index, _ ->
                val isPast = index < activeTier
                val isCurrent = index == activeTier

                Box(
                    modifier = Modifier
                        .size(if (isCurrent) 14.dp else 10.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent -> GoldAccent
                                isPast -> TealPrimary
                                else -> CreamBg
                            }
                        )
                        .border(
                            1.dp,
                            if (isCurrent) GoldAccent else if (isPast) TealPrimary else BorderCardSlate,
                            CircleShape
                        )
                )

                if (index < tiers.lastIndex) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (index < activeTier) TealPrimary else BorderCardSlate.copy(alpha = 0.5f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            tiers.forEachIndexed { index, nameRes ->
                val isCurrent = index == activeTier
                Text(
                    text = stringResource(nameRes),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = if (isCurrent) 10.sp else 9.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isCurrent -> GoldAccent
                        index < activeTier -> TealPrimary
                        else -> InkTertiary
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun resolveRoleBadge(job: Job): Pair<ImageVector, Color> {
    return when {
        job.isMilitary -> Icons.Rounded.MilitaryTech to Color(0xFF556B2F)
        job.id.contains("developer", ignoreCase = true) || job.id.contains("engineer", ignoreCase = true) || job.id.contains("tech", ignoreCase = true) ->
            Icons.Rounded.Code to TealPrimary
        job.id.contains("doctor", ignoreCase = true) || job.id.contains("nurse", ignoreCase = true) || job.id.contains("medical", ignoreCase = true) ->
            Icons.Rounded.LocalHospital to Color(0xFF00897B)
        job.id.contains("lawyer", ignoreCase = true) || job.id.contains("legal", ignoreCase = true) ->
            Icons.Rounded.Gavel to Color(0xFF3949AB)
        job.id.contains("director", ignoreCase = true) || job.id.contains("executive", ignoreCase = true) || job.id.contains("manager", ignoreCase = true) ->
            Icons.Rounded.CorporateFare to GoldAccent
        job.id.contains("bank", ignoreCase = true) || job.id.contains("finance", ignoreCase = true) || job.id.contains("accountant", ignoreCase = true) ->
            Icons.Rounded.AccountBalance to Color(0xFF1E88E5)
        job.id.contains("actor", ignoreCase = true) || job.id.contains("singer", ignoreCase = true) || job.id.contains("artist", ignoreCase = true) || job.id.contains("media", ignoreCase = true) ->
            Icons.Rounded.Star to Color(0xFF8E24AA)
        else -> Icons.Rounded.Badge to TealPrimary
    }
}
