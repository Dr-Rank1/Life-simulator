package com.maisha.game.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maisha.game.R
import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.PrisonActivity
import com.maisha.game.data.model.PrisonGang
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
fun PrisonYardBoard(
    character: Character,
    crimeEngine: CrimeEngine,
    onPerformActivity: (PrisonActivity) -> Unit,
    modifier: Modifier = Modifier
) {
    val record = character.criminalRecord
    val facilityName = crimeEngine.prisonFacilityName(record.totalSentenceYears)
    val gangLabel = crimeEngine.gangLabel(record.prisonGang)
    val totalSentence = record.totalSentenceYears.coerceAtLeast(1)
    val sentenceProgress = (record.yearsServed.toFloat() / totalSentence).coerceIn(0f, 1f)
    val isParoleEligible = crimeEngine.paroleEligible(record)
    val paroleOdds = (crimeEngine.paroleChance(record) * 100).toInt()
    val escapeOdds = (crimeEngine.calculateEscapeChance(character) * 100).toInt()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Facility Header & Sentence Tracker
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CoralNegative.copy(alpha = 0.12f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = AppIcons.PrisonBars,
                                    contentDescription = null,
                                    tint = CoralNegative,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.prison_yard_dashboard_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary
                            )
                            Text(
                                text = stringResource(R.string.prison_facility_label, facilityName),
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary
                            )
                        }
                    }

                    // Security Tier Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = when {
                            record.totalSentenceYears >= 15 -> CoralNegative.copy(alpha = 0.15f)
                            record.totalSentenceYears >= 5 -> GoldAccent.copy(alpha = 0.15f)
                            else -> TealPrimary.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = when {
                                record.totalSentenceYears >= 15 -> "Max Security"
                                record.totalSentenceYears >= 5 -> "Med Security"
                                else -> "Detention"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                record.totalSentenceYears >= 15 -> CoralNegative
                                record.totalSentenceYears >= 5 -> GoldAccent
                                else -> TealPrimary
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Sentence Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(
                                R.string.prison_sentence_tracker,
                                record.yearsServed,
                                record.totalSentenceYears,
                                record.yearsRemaining
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = InkPrimary
                        )
                        Text(
                            text = "${(sentenceProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = InkSecondary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { sentenceProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = TealPrimary,
                        trackColor = BorderCardSlate
                    )
                }

                // Cellblock Dynamics Meters: Standing, Gang, Rivalry Heat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DynamicsMiniPill(
                        label = stringResource(R.string.prison_cellblock_respect, record.cellblockStanding),
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    DynamicsMiniPill(
                        label = stringResource(R.string.prison_crew_affiliation, gangLabel),
                        color = if (record.prisonGang != PrisonGang.NONE) GoldAccent else InkSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    DynamicsMiniPill(
                        label = stringResource(R.string.prison_inmate_rivalry, record.inmateRivalryHeat),
                        color = if (record.inmateRivalryHeat >= 50) CoralNegative else InkSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Daily Routine & Growth Cards
        Text(
            text = "Daily Routine & Rehabilitation",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = InkPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val dailyWorkPay = EconomyScaler.scaleAmount(500, character.countryCode)
            PrisonActionTile(
                title = stringResource(R.string.btn_prison_labor_action),
                subtitle = "Earn ${formatMoney(dailyWorkPay, character.countryCode)} (+4 Respect)",
                icon = AppIcons.Money,
                enabled = !record.prisonJobDoneThisYear,
                onClick = { onPerformActivity(PrisonActivity.WORK_DETAIL) },
                modifier = Modifier.weight(1f)
            )

            PrisonActionTile(
                title = stringResource(R.string.btn_prison_law_library),
                subtitle = "Study Legal Case Law (+Smarts, +Parole)",
                icon = AppIcons.Smarts,
                enabled = true,
                onClick = { onPerformActivity(PrisonActivity.LIBRARY) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PrisonActionTile(
                title = stringResource(R.string.btn_yard_iron_workout),
                subtitle = "Pump Iron in Yard (+Health, +Respect)",
                icon = AppIcons.Health,
                enabled = true,
                onClick = { onPerformActivity(PrisonActivity.EXERCISE) },
                modifier = Modifier.weight(1f)
            )

            PrisonActionTile(
                title = stringResource(R.string.btn_quiet_good_behavior),
                subtitle = "Stay Clear of Drama (+2 Parole Credit)",
                icon = AppIcons.Condition,
                enabled = true,
                onClick = { onPerformActivity(PrisonActivity.GOOD_BEHAVIOR) },
                modifier = Modifier.weight(1f)
            )
        }

        // Yard Crew Alliance Card
        Card(
            shape = RoundedCornerShape(14.dp),
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
                        text = stringResource(R.string.prison_gang_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    Text(
                        text = if (record.prisonGang == PrisonGang.NONE) {
                            "Join a yard crew for defense against shiv attacks (+18 Respect, +12 Notoriety)."
                        } else {
                            "Current Crew: $gangLabel. Deepen rank in the cellblock."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                }

                Button(
                    onClick = { onPerformActivity(PrisonActivity.PRISON_YARD_GANG) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralNegative)
                ) {
                    Text(stringResource(R.string.btn_align_yard_crew))
                }
            }
        }

        // High-Stakes Gambits Section: Parole Board & Prison Break
        Text(
            text = "High-Stakes Gambits",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = InkPrimary
        )

        // Parole Petition Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PaperWhite),
            border = BorderStroke(1.dp, if (isParoleEligible) SuccessGreen else BorderCardSlate),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        Icon(AppIcons.CourtGavel, contentDescription = null, tint = if (isParoleEligible) SuccessGreen else InkSecondary, modifier = Modifier.size(20.dp))
                        Text(
                            text = stringResource(R.string.prison_parole_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isParoleEligible) SuccessGreen.copy(alpha = 0.15f) else BorderCardSlate.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = if (isParoleEligible) stringResource(R.string.parole_status_eligible) else "Ineligible",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isParoleEligible) SuccessGreen else InkTertiary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (isParoleEligible) {
                    Text(
                        text = "Parole Board Approval Probability: $paroleOdds% (Parole Bonus: +${record.paroleBonus})",
                        style = MaterialTheme.typography.bodySmall,
                        color = SuccessGreen
                    )
                } else {
                    val minRequired = (record.totalSentenceYears * 0.33f).toInt().coerceAtLeast(1)
                    val remainingYears = (minRequired - record.yearsServed).coerceAtLeast(0)
                    Text(
                        text = stringResource(R.string.parole_status_ineligible, remainingYears),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                }

                Button(
                    onClick = { onPerformActivity(PrisonActivity.PAROLE_HEARING_APPEAL) },
                    enabled = isParoleEligible,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_petition_parole_board), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Prison Breakout Planner Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PaperWhite),
            border = BorderStroke(1.dp, CoralNegative.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        Icon(AppIcons.PrisonEscape, contentDescription = null, tint = CoralNegative, modifier = Modifier.size(20.dp))
                        Text(
                            text = stringResource(R.string.prison_escape_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CoralNegative
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CoralNegative.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = stringResource(R.string.escape_odds_meter, escapeOdds),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CoralNegative,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.escape_warning_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )

                Button(
                    onClick = { onPerformActivity(PrisonActivity.PRISON_ESCAPE_ATTEMPT) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralNegative),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(AppIcons.PrisonEscape, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.btn_attempt_breakout), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DynamicsMiniPill(label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.10f),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PrisonActionTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderCardSlate),
        modifier = modifier.height(72.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(icon, contentDescription = null, tint = if (enabled) TealPrimary else InkTertiary, modifier = Modifier.size(20.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) InkPrimary else InkTertiary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                    maxLines = 1
                )
            }
        }
    }
}
