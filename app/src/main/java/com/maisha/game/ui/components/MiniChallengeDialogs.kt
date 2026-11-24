package com.maisha.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.maisha.game.domain.CrimePushLuckSession
import com.maisha.game.domain.ExamFocusChallenge
import com.maisha.game.domain.ExamFocusTactic
import com.maisha.game.domain.NegotiationChallenge
import com.maisha.game.domain.NegotiationTactics
import com.maisha.game.domain.PushLuckAction
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.BorderCardSlate
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.DeepSlate
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.Hairline
import com.maisha.game.ui.theme.HairlineSoft
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.StatHealth
import com.maisha.game.ui.theme.TealPrimary
import com.maisha.game.util.formatMoney
import kotlin.math.roundToInt

/**
 * 3-second interactive focus check for major exams and finals.
 */
@Composable
fun ExamFocusDialog(
    challenge: ExamFocusChallenge,
    onResolve: (ExamFocusTactic, Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timerProgress = remember { Animatable(1f) }
    var timerExpired by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        timerProgress.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 3500, easing = LinearEasing)
        )
        timerExpired = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PaperWhite),
            border = BorderStroke(1.5.dp, BorderCardSlate),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = challenge.examTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = stringResource(R.string.dialog_exam_focus_title),
                            style = MaterialTheme.typography.labelSmall,
                            color = TealPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = InkTertiary)
                    }
                }

                // 3-second focus timer bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.label_focus_timer),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (timerProgress.value > 0.3f) TealPrimary else CoralNegative,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (timerProgress.value > 0f) "+Bonus Active" else "Standard",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (timerProgress.value > 0f) LifeGreen else InkTertiary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                    LinearProgressIndicator(
                        progress = { timerProgress.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (timerProgress.value > 0.3f) TealPrimary else CoralNegative,
                        trackColor = HairlineSoft
                    )
                }

                // Status Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(0.5.dp, Hairline)
                    ) {
                        Text(
                            text = stringResource(R.string.label_exam_focus_preparedness, challenge.basePreparedness),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (challenge.baseStress > 50) CoralNegative.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(0.5.dp, Hairline)
                    ) {
                        Text(
                            text = stringResource(R.string.label_exam_focus_stress, challenge.baseStress),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (challenge.baseStress > 50) CoralNegative else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.dialog_exam_focus_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                    lineHeight = 16.sp
                )

                // Tactical Choice 1: Analytical
                FocusTacticCard(
                    title = stringResource(R.string.btn_tactic_analytical),
                    description = stringResource(R.string.desc_tactic_analytical),
                    accentColor = TealPrimary,
                    onClick = { onResolve(ExamFocusTactic.ANALYTICAL_PRECISION, !timerExpired) }
                )

                // Tactical Choice 2: Instinctive
                FocusTacticCard(
                    title = stringResource(R.string.btn_tactic_instinctive),
                    description = stringResource(R.string.desc_tactic_instinctive),
                    accentColor = LifeGreen,
                    onClick = { onResolve(ExamFocusTactic.INSTINCTIVE_SPEED, !timerExpired) }
                )

                // Tactical Choice 3: High-Stakes Shortcut
                FocusTacticCard(
                    title = stringResource(R.string.btn_tactic_shortcut),
                    description = stringResource(R.string.desc_tactic_shortcut),
                    accentColor = GoldAccent,
                    onClick = { onResolve(ExamFocusTactic.HIGH_STAKES_SHORTCUT, !timerExpired) }
                )
            }
        }
    }
}

@Composable
private fun FocusTacticCard(
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
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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

/**
 * Interactive Push-Your-Luck street crime and heist sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrimePushLuckSheet(
    session: CrimePushLuckSession,
    countryCode: String,
    onAction: (PushLuckAction) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PaperWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
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
                        color = CoralNegative.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = AppIcons.HeistMask,
                                contentDescription = null,
                                tint = CoralNegative,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.dialog_push_luck_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = stringResource(R.string.label_push_luck_stage, session.stage, session.maxStages),
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CoralNegative.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, CoralNegative.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = stringResource(R.string.label_bust_risk, (session.currentBustChance * 100).roundToInt()),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CoralNegative,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = stringResource(R.string.dialog_push_luck_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = InkSecondary,
                fontSize = 13.sp
            )

            // Current Loot Display
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = DeepSlate,
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.label_current_loot,
                            formatMoney(session.currentLoot, countryCode)
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        fontSize = 20.sp
                    )
                    if (session.stage < session.maxStages) {
                        Text(
                            text = stringResource(
                                R.string.label_next_stage_loot,
                                formatMoney(session.nextStageLoot, countryCode)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = PaperWhite.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Action Buttons: Cash Out vs Push Luck
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onAction(PushLuckAction.CASH_OUT) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, LifeGreen)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_cash_out),
                            fontWeight = FontWeight.Bold,
                            color = LifeGreen,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "100% Safe",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                if (session.stage < session.maxStages) {
                    Button(
                        onClick = { onAction(PushLuckAction.PUSH_LUCK) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralNegative)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.btn_push_luck),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "High Risk",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Interactive negotiation & persuasion mini-check for raises, promotions, prenups, and confrontations.
 */
@Composable
fun NegotiationDialog(
    challenge: NegotiationChallenge,
    onResolve: (NegotiationTactics) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PaperWhite),
            border = BorderStroke(1.5.dp, BorderCardSlate),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.dialog_negotiation_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = challenge.subjectLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = TealPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = InkTertiary)
                    }
                }

                // Counterpart Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    border = BorderStroke(0.5.dp, Hairline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PersonAvatar(
                            avatarConfig = com.maisha.game.data.model.AvatarConfig.DEFAULT,
                            size = 38,
                            age = 35,
                            seed = challenge.party.avatarSeed.ifBlank { challenge.party.name }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = challenge.party.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary
                            )
                            Text(
                                text = challenge.party.roleTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stringResource(R.string.label_counterpart_rapport, challenge.party.relationshipLevel),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LifeGreen,
                                fontSize = 10.sp
                            )
                            Text(
                                text = stringResource(R.string.label_counterpart_resistance, challenge.party.resistanceLevel),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (challenge.party.resistanceLevel > 60) CoralNegative else InkSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.dialog_negotiation_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                    lineHeight = 16.sp
                )

                // Tactic 1: Value / Metrics
                FocusTacticCard(
                    title = stringResource(R.string.btn_tactic_metrics),
                    description = stringResource(R.string.desc_tactic_metrics),
                    accentColor = TealPrimary,
                    onClick = { onResolve(NegotiationTactics.VALUE_METRICS) }
                )

                // Tactic 2: Charisma / Empathy
                FocusTacticCard(
                    title = stringResource(R.string.btn_tactic_empathy),
                    description = stringResource(R.string.desc_tactic_empathy),
                    accentColor = LifeGreen,
                    onClick = { onResolve(NegotiationTactics.CHARISMA_EMPATHY) }
                )

                // Tactic 3: Assertive Leverage
                FocusTacticCard(
                    title = stringResource(R.string.btn_tactic_leverage),
                    description = stringResource(R.string.desc_tactic_leverage),
                    accentColor = GoldAccent,
                    onClick = { onResolve(NegotiationTactics.ASSERTIVE_LEVERAGE) }
                )
            }
        }
    }
}
