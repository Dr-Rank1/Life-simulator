package com.maisha.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.R
import com.maisha.game.data.CountryCatalog
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.EducationState
import com.maisha.game.data.model.SchoolStage
import com.maisha.game.data.model.StudyEffort
import com.maisha.game.ui.theme.BorderCardSlate
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.CreamBg
import com.maisha.game.ui.theme.CreamMuted
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.SuccessGreen
import com.maisha.game.ui.theme.TealPrimary

@Composable
fun AcademicDossierCard(
    character: Character,
    education: EducationState,
    countryCode: String,
    onSetStudyEffort: (StudyEffort) -> Unit,
    modifier: Modifier = Modifier
) {
    val flavor = CountryCatalog.flavorFor(countryCode)
    val schoolName = education.schoolName ?: stringResource(R.string.label_education)

    // Stage subtitle
    val stageSubtitle = when (education.stage) {
        SchoolStage.PRIMARY -> "Primary School · Grade ${education.currentGrade}"
        SchoolStage.SECONDARY -> "Secondary School · Form ${education.currentGrade}"
        SchoolStage.UNIVERSITY -> education.universityMajor?.courseLabel ?: "University · Year ${education.currentGrade}"
        SchoolStage.GRADUATED -> "Graduated"
        SchoolStage.NONE -> "Not Enrolled"
    }

    // GPA and honors determination
    val gpa = if (education.gpa > 0f) education.gpa else 3.0f
    val (honorsRes, gpaColor) = when {
        gpa >= 3.8f -> R.string.dossier_honors_summa to GoldAccent
        gpa >= 3.5f -> R.string.dossier_honors_magna to TealPrimary
        gpa >= 3.0f -> R.string.dossier_honors_cum_laude to LifeGreen
        gpa >= 2.0f -> R.string.dossier_honors_good to InkSecondary
        else -> R.string.dossier_honors_probation to CoralNegative
    }

    // Exam countdown
    val (examName, yearsUntilExam) = when (education.stage) {
        SchoolStage.PRIMARY -> flavor.primaryExamName to (13 - character.age).coerceAtLeast(0)
        SchoolStage.SECONDARY -> flavor.secondaryExamName to (17 - character.age).coerceAtLeast(0)
        SchoolStage.UNIVERSITY -> "Degree Finals" to ((education.universityMajor?.programYears ?: 4) - education.currentGrade).coerceAtLeast(0)
        else -> "" to 0
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, BorderCardSlate),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Crest Badge + School Name + GPA Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(TealPrimary.copy(alpha = 0.12f))
                        .border(1.5.dp, TealPrimary.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.School,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.academic_dossier_title).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        fontSize = 10.sp
                    )
                    Text(
                        text = schoolName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stageSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TealPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // GPA Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = gpaColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, gpaColor.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.academic_dossier_gpa, gpa),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = gpaColor
                        )
                        Text(
                            text = stringResource(honorsRes),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = gpaColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Localized Exam Countdown Banner
            if (examName.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = CreamMuted,
                    border = BorderStroke(1.dp, BorderCardSlate)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = if (yearsUntilExam <= 0) CoralNegative else TealPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (yearsUntilExam <= 0) {
                                stringResource(R.string.exam_countdown_due_this_year, examName)
                            } else if (yearsUntilExam == 1) {
                                stringResource(R.string.exam_countdown_format, examName, 1)
                            } else {
                                stringResource(R.string.exam_countdown_format_plural, examName, yearsUntilExam)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (yearsUntilExam <= 0) CoralNegative else InkPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Study Effort Selector Pills (Slacker / Normal / Grind)
            Text(
                text = stringResource(R.string.label_study_effort),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = InkSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StudyEffortPill(
                    title = stringResource(R.string.study_pill_slacker),
                    selected = education.plannedStudyEffort == StudyEffort.SLACK,
                    activeColor = InkTertiary,
                    onClick = { onSetStudyEffort(StudyEffort.SLACK) },
                    modifier = Modifier.weight(1f)
                )

                StudyEffortPill(
                    title = stringResource(R.string.study_pill_normal),
                    selected = education.plannedStudyEffort == StudyEffort.NORMAL,
                    activeColor = TealPrimary,
                    onClick = { onSetStudyEffort(StudyEffort.NORMAL) },
                    modifier = Modifier.weight(1f)
                )

                StudyEffortPill(
                    title = stringResource(R.string.study_pill_grind),
                    selected = education.plannedStudyEffort == StudyEffort.HARD,
                    activeColor = GoldAccent,
                    onClick = { onSetStudyEffort(StudyEffort.HARD) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Remaining Yearly Action Pips: 1 Academic + 1 Social
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CreamMuted.copy(alpha = 0.7f),
                border = BorderStroke(0.5.dp, BorderCardSlate)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = stringResource(R.string.dossier_pips_header),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = InkSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Academic Action Pip
                        ActionPip(
                            label = stringResource(
                                R.string.dossier_pip_academic,
                                if (education.academicActionDoneThisYear) stringResource(R.string.dossier_pip_used) else stringResource(R.string.dossier_pip_available)
                            ),
                            used = education.academicActionDoneThisYear,
                            activeColor = TealPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        // Social Action Pip
                        ActionPip(
                            label = stringResource(
                                R.string.dossier_pip_social,
                                if (education.socialActionDoneThisYear) stringResource(R.string.dossier_pip_used) else stringResource(R.string.dossier_pip_available)
                            ),
                            used = education.socialActionDoneThisYear,
                            activeColor = LifeGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudyEffortPill(
    title: String,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(38.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) activeColor.copy(alpha = 0.16f) else CreamBg,
        border = BorderStroke(1.dp, if (selected) activeColor else BorderCardSlate)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) activeColor else InkSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActionPip(
    label: String,
    used: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (used) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (used) InkTertiary else activeColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = if (used) InkTertiary else InkPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
