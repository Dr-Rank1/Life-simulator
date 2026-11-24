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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.R
import com.maisha.game.data.IllustrationCatalog
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.Job
import com.maisha.game.data.model.SchoolStage
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
fun JobOpportunityCard(
    character: Character,
    job: Job,
    countryCode: String,
    isEligible: Boolean,
    ineligibilityReason: String?,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Requirements evaluation
    val degreeMet = character.education.stage.ordinal >= job.minEducation.ordinal
    val degreeLabel = when (job.minEducation) {
        SchoolStage.UNIVERSITY -> stringResource(R.string.job_opp_badge_degree, "University")
        SchoolStage.SECONDARY -> stringResource(R.string.job_opp_badge_degree, "High School")
        else -> stringResource(R.string.job_opp_badge_degree_none)
    }

    val minSmarts = when (job.minEducation) {
        SchoolStage.UNIVERSITY -> 55
        SchoolStage.SECONDARY -> 35
        else -> 20
    }
    val smartsMet = character.stats.smarts >= minSmarts

    // Estimated top-of-ladder salary ceiling (~2.2x base)
    val maxSalary = (job.baseSalary * 2.2).toInt()
    val salaryRangeLabel = "${formatMoney(job.baseSalary, countryCode)} – ${formatMoney(maxSalary, countryCode)} / yr"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isEligible) 1f else 0.72f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, if (isEligible) BorderCardSlate else BorderCardSlate.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isEligible) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Illustration + Job Title + Level Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CreamMuted),
                    contentAlignment = Alignment.Center
                ) {
                    IllustrationImage(
                        ref = IllustrationCatalog.getIllustrationForJob(job.id),
                        size = 38.dp,
                        contentDescription = job.title
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = salaryRangeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TealPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (job.isMilitary) Color(0xFF556B2F).copy(alpha = 0.15f) else GoldAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (job.isMilitary) "Military" else "Tier ${job.level}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (job.isMilitary) Color(0xFF556B2F) else GoldAccent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Requirement Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Degree Badge
                RequirementPill(
                    label = degreeLabel,
                    met = degreeMet,
                    modifier = Modifier.weight(1f)
                )

                // Smarts Badge
                RequirementPill(
                    label = stringResource(R.string.job_opp_badge_smarts, minSmarts),
                    met = smartsMet,
                    modifier = Modifier.weight(1f)
                )

                // Experience Badge
                RequirementPill(
                    label = stringResource(R.string.job_opp_badge_exp, "Entry"),
                    met = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Ineligibility reason if not eligible
            if (!isEligible && ineligibilityReason != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = CoralNegative,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = ineligibilityReason,
                        style = MaterialTheme.typography.labelSmall,
                        color = CoralNegative,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button
            Button(
                onClick = onApply,
                enabled = isEligible,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPrimary,
                    disabledContainerColor = CreamMuted
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isEligible) stringResource(R.string.job_opp_apply_btn) else stringResource(R.string.job_opp_locked_btn),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isEligible) Color.White else InkTertiary
                    )
                    if (isEligible) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RequirementPill(
    label: String,
    met: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = if (met) SuccessGreen.copy(alpha = 0.08f) else CoralNegative.copy(alpha = 0.08f),
        border = BorderStroke(0.5.dp, if (met) SuccessGreen.copy(alpha = 0.3f) else CoralNegative.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (met) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                contentDescription = null,
                tint = if (met) SuccessGreen else CoralNegative,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (met) InkPrimary else CoralNegative,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
