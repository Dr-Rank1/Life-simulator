// app/src/main/java/com/maisha/game/ui/components/WeeklyChallengeSheet.kt
package com.maisha.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.R
import com.maisha.game.data.EconomyScaler
import com.maisha.game.domain.ChallengeRequirement
import com.maisha.game.domain.RequirementUnit
import com.maisha.game.domain.WeeklyChallengeProgress
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.BorderCardSlate
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.LifeGreenPressed
import com.maisha.game.ui.theme.LifeGreenSoft
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.TealPrimary
import com.maisha.game.util.formatMoney

/**
 * Rich ModalBottomSheet showcasing the active Weekly Challenge:
 * - Time remaining countdown
 * - Active challenge header and description
 * - Requirement checklist with individual live progress bars
 * - Scaled reward preview (cash, karma, happiness, ribbon, title)
 * - Tactile claim action button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyChallengeSheet(
    progress: WeeklyChallengeProgress,
    countryCode: String,
    onClaim: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val challenge = progress.challenge
    val reward = challenge.reward
    val scaledCash = EconomyScaler.scaleAmount(reward.cashKenya, countryCode)
    val formattedCash = formatMoney(scaledCash, countryCode)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PaperWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Category Pill & Countdown Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(GoldAccent.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = stringResource(R.string.section_weekly_challenge).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = InkSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = progress.formattedTimeRemaining(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = InkSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Challenge Title & Description
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = resolveChallengeTitleString(challenge.titleKey),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )
                Text(
                    text = resolveChallengeDescString(challenge.descriptionKey),
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSecondary,
                    lineHeight = 20.sp
                )
            }

            // Overall Progress Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, BorderCardSlate)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${progress.completedCount} of ${progress.totalCount} objectives completed",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = InkPrimary
                        )
                        Text(
                            text = "${(progress.overallFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (progress.isAllCompleted) LifeGreenPressed else GoldAccent
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progress.overallFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (progress.isAllCompleted) LifeGreen else GoldAccent,
                        trackColor = Color(0xFFE2E8F0)
                    )
                }
            }

            // Section: Checklist Objectives
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.challenge_checklist_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )

                progress.requirements.forEachIndexed { index, req ->
                    RequirementChecklistRow(
                        index = index + 1,
                        req = req
                    )
                }
            }

            // Section: Completion Rewards
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.challenge_rewards_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFBF6)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8C0))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Cash bonus
                        RewardRow(
                            icon = AppIcons.Money,
                            iconTint = LifeGreen,
                            text = stringResource(R.string.challenge_reward_cash, formattedCash),
                            highlight = true
                        )

                        // Silk Ribbon
                        RewardRow(
                            icon = AppIcons.Achievements,
                            iconTint = GoldAccent,
                            text = stringResource(R.string.challenge_reward_ribbon, resolveRibbonName(reward.ribbonKey)),
                            highlight = false
                        )

                        // Karma & Happiness
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            RewardRow(
                                icon = AppIcons.Looks,
                                iconTint = GoldAccent,
                                text = stringResource(R.string.challenge_reward_karma, reward.karmaBonus),
                                highlight = false,
                                modifier = Modifier.weight(1f)
                            )
                            RewardRow(
                                icon = AppIcons.Happiness,
                                iconTint = GoldAccent,
                                text = stringResource(R.string.challenge_reward_happiness, reward.happinessBonus),
                                highlight = false,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Unlockable Title (if defined)
                        if (reward.titleKey.isNotBlank()) {
                            RewardRow(
                                icon = AppIcons.HeirCrown,
                                iconTint = GoldAccent,
                                text = stringResource(R.string.challenge_reward_title_label, resolveTitleName(reward.titleKey)),
                                highlight = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action: Claim or Status CTA Button
            when {
                progress.isClaimed -> {
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Color(0xFFF1F5F9),
                            disabledContentColor = InkSecondary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.challenge_claimed_button),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
                progress.isAllCompleted -> {
                    Button(
                        onClick = onClaim,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LifeGreen,
                            contentColor = PaperWhite
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.challenge_claim_button),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                else -> {
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Color(0xFFE2E8F0),
                            disabledContentColor = InkSecondary
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Complete All Objectives (${progress.completedCount}/${progress.totalCount})",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequirementChecklistRow(
    index: Int,
    req: ChallengeRequirement,
    modifier: Modifier = Modifier
) {
    val isDone = req.isCompleted
    val title = resolveRequirementTitleString(req.titleKey)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDone) Color(0xFFF0FDF4) else PaperWhite),
        border = BorderStroke(1.dp, if (isDone) Color(0xFFBBF7D0) else BorderCardSlate)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Checkmark or Number Badge
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (isDone) LifeGreen else Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = PaperWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "$index",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = InkSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Requirement Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isDone) FontWeight.Bold else FontWeight.Medium,
                    color = InkPrimary,
                    modifier = Modifier.weight(1f)
                )

                // Current / Target readout
                Text(
                    text = formatRequirementProgress(req),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) LifeGreenPressed else InkSecondary,
                    fontSize = 11.sp
                )
            }

            // Requirement Sub-progress bar
            LinearProgressIndicator(
                progress = { req.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (isDone) LifeGreen else TealPrimary,
                trackColor = Color(0xFFF1F5F9)
            )
        }
    }
}

private fun formatRequirementProgress(req: ChallengeRequirement): String = when (req.unit) {
    RequirementUnit.CURRENCY -> "${req.current} / ${req.target}"
    RequirementUnit.PERCENT -> "${req.current}% / ${req.target}%"
    RequirementUnit.LEVEL -> "Lvl ${req.current} / ${req.target}"
    RequirementUnit.COUNT -> "${req.current} / ${req.target}"
}

@Composable
private fun RewardRow(
    icon: ImageVector,
    iconTint: Color,
    text: String,
    highlight: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) InkPrimary else InkSecondary,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun resolveChallengeDescString(key: String): String = when (key) {
    "challenge_medical_desc" -> stringResource(R.string.challenge_medical_desc)
    "challenge_legal_desc" -> stringResource(R.string.challenge_legal_desc)
    "challenge_adopt_desc" -> stringResource(R.string.challenge_adopt_desc)
    "challenge_renovate_desc" -> stringResource(R.string.challenge_renovate_desc)
    "challenge_expunge_desc" -> stringResource(R.string.challenge_expunge_desc)
    "challenge_scholar_desc" -> stringResource(R.string.challenge_scholar_desc)
    "challenge_teen_job_desc" -> stringResource(R.string.challenge_teen_job_desc)
    "challenge_portfolio_desc" -> stringResource(R.string.challenge_portfolio_desc)
    else -> key
}

@Composable
fun resolveRequirementTitleString(key: String): String = when (key) {
    "req_medical_degree_title" -> stringResource(R.string.req_medical_degree_title)
    "req_medical_track_title" -> stringResource(R.string.req_medical_track_title)
    "req_medical_health_title" -> stringResource(R.string.req_medical_health_title)
    "req_law_degree_title" -> stringResource(R.string.req_law_degree_title)
    "req_legal_track_title" -> stringResource(R.string.req_legal_track_title)
    "req_legal_smarts_title" -> stringResource(R.string.req_legal_smarts_title)
    "req_adopt_child_title" -> stringResource(R.string.req_adopt_child_title)
    "req_adopt_bond_title" -> stringResource(R.string.req_adopt_bond_title)
    "req_own_home_title" -> stringResource(R.string.req_own_home_title)
    "req_renovate_home_title" -> stringResource(R.string.req_renovate_home_title)
    "req_net_worth_100k_title" -> stringResource(R.string.req_net_worth_100k_title)
    "req_expunged_record_title" -> stringResource(R.string.req_expunged_record_title)
    "req_high_karma_title" -> stringResource(R.string.req_high_karma_title)
    "req_employed_title" -> stringResource(R.string.req_employed_title)
    "req_high_gpa_title" -> stringResource(R.string.req_high_gpa_title)
    "req_scholar_smarts_title" -> stringResource(R.string.req_scholar_smarts_title)
    "req_academic_honors_title" -> stringResource(R.string.req_academic_honors_title)
    "req_work_hustle_title" -> stringResource(R.string.req_work_hustle_title)
    "req_youth_savings_title" -> stringResource(R.string.req_youth_savings_title)
    "req_pass_grades_title" -> stringResource(R.string.req_pass_grades_title)
    "req_portfolio_100k_title" -> stringResource(R.string.req_portfolio_100k_title)
    "req_net_worth_250k_title" -> stringResource(R.string.req_net_worth_250k_title)
    "req_hold_savings_title" -> stringResource(R.string.req_hold_savings_title)
    else -> key
}

@Composable
fun resolveRibbonName(key: String): String = when (key) {
    "ribbon_healer" -> "Caduceus Silk Ribbon"
    "ribbon_counsel" -> "Golden Scales Ribbon"
    "ribbon_guardian" -> "Warm Hearth Ribbon"
    "ribbon_mogul" -> "Grand Manor Ribbon"
    "ribbon_redeemed" -> "Phoenix Feather Ribbon"
    "ribbon_valedictorian" -> "Laurel Wreath Ribbon"
    "ribbon_hustler" -> "Copper Shilling Ribbon"
    "ribbon_investor" -> "Bull Market Ribbon"
    else -> key.replace("ribbon_", "").replace("_", " ").replaceFirstChar { it.uppercase() }
}

@Composable
fun resolveTitleName(key: String): String = when (key) {
    "title_master_healer" -> stringResource(R.string.title_master_healer)
    "title_legal_ace" -> stringResource(R.string.title_legal_ace)
    "title_devoted_guardian" -> stringResource(R.string.title_devoted_guardian)
    "title_property_mogul" -> stringResource(R.string.title_property_mogul)
    "title_redeemed_soul" -> stringResource(R.string.title_redeemed_soul)
    "title_valedictorian" -> stringResource(R.string.title_valedictorian)
    "title_young_entrepreneur" -> stringResource(R.string.title_young_entrepreneur)
    "title_master_investor" -> stringResource(R.string.title_master_investor)
    else -> key
}
