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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.R
import com.maisha.game.domain.WeeklyChallenge
import com.maisha.game.domain.WeeklyChallengeProgress
import com.maisha.game.ui.theme.BorderCardSlate
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.LifeGreenPressed
import com.maisha.game.ui.theme.LifeGreenSoft
import com.maisha.game.ui.theme.PaperWhite

/**
 * Prominent weekly challenge card displayed on LifeScreen.
 * Shows active challenge title, live countdown, progress bar, and opens the detailed bottom sheet.
 */
@Composable
fun WeeklyChallengeCard(
    progress: WeeklyChallengeProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) = WeeklyChallengeCard(
    challenge = progress.challenge,
    progress = progress,
    isCompleted = progress.isAllCompleted,
    isClaimed = progress.isClaimed,
    onClick = onClick,
    modifier = modifier
)

@Composable
fun WeeklyChallengeCard(
    challenge: WeeklyChallenge,
    progress: WeeklyChallengeProgress?,
    isCompleted: Boolean,
    isClaimed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = progress?.completedCount ?: if (isCompleted) 1 else 0
    val totalCount = progress?.totalCount ?: 1
    val fraction = progress?.overallFraction ?: if (isCompleted) 1f else 0f
    val timeRemaining = progress?.formattedTimeRemaining() ?: ""

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(
            width = if (isAllCompletedReady(isCompleted, isClaimed)) 1.5.dp else 1.dp,
            color = when {
                isClaimed -> LifeGreenSoft
                isAllCompletedReady(isCompleted, isClaimed) -> GoldAccent
                else -> BorderCardSlate
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Trophy + Category + Time Remaining Countdown Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.section_weekly_challenge).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        letterSpacing = 0.6.sp
                    )
                }

                if (timeRemaining.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = null,
                                tint = InkSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = timeRemaining,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = InkSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Challenge Title & Status Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = resolveChallengeTitleString(challenge.titleKey),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(8.dp))

                StatusPill(
                    isClaimed = isClaimed,
                    isCompleted = isCompleted
                )
            }

            // Requirements Progress & Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$completedCount/$totalCount completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary,
                        fontSize = 12.sp
                    )

                    Text(
                        text = "${(fraction * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) LifeGreen else GoldAccent,
                        fontSize = 12.sp
                    )
                }

                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isCompleted) LifeGreen else GoldAccent,
                    trackColor = Color(0xFFF1F5F9)
                )
            }
        }
    }
}

private fun isAllCompletedReady(isCompleted: Boolean, isClaimed: Boolean): Boolean =
    isCompleted && !isClaimed

@Composable
private fun StatusPill(
    isClaimed: Boolean,
    isCompleted: Boolean
) {
    val (bg, fg, label) = when {
        isClaimed -> Triple(
            LifeGreenSoft,
            LifeGreenPressed,
            stringResource(R.string.challenge_card_badge_claimed)
        )
        isCompleted -> Triple(
            Color(0xFFFEF3C7),
            Color(0xFFB45309),
            stringResource(R.string.challenge_card_badge_ready)
        )
        else -> Triple(
            Color(0xFFF1F5F9),
            InkSecondary,
            stringResource(R.string.challenge_card_badge_in_progress)
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
            fontSize = 11.sp
        )
    }
}

@Composable
fun resolveChallengeTitleString(key: String): String = when (key) {
    "challenge_medical_title" -> stringResource(R.string.challenge_medical_title)
    "challenge_legal_title" -> stringResource(R.string.challenge_legal_title)
    "challenge_adopt_title" -> stringResource(R.string.challenge_adopt_title)
    "challenge_renovate_title" -> stringResource(R.string.challenge_renovate_title)
    "challenge_expunge_title" -> stringResource(R.string.challenge_expunge_title)
    "challenge_scholar_title" -> stringResource(R.string.challenge_scholar_title)
    "challenge_teen_job_title" -> stringResource(R.string.challenge_teen_job_title)
    "challenge_portfolio_title" -> stringResource(R.string.challenge_portfolio_title)
    else -> key
}
