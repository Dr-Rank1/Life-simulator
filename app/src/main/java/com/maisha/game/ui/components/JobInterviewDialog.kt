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
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.maisha.game.R
import com.maisha.game.data.model.InterviewChoice
import com.maisha.game.data.model.JobInterviewSession
import com.maisha.game.ui.theme.BorderCardSlate
import com.maisha.game.ui.theme.CoralNegative
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
fun JobInterviewDialog(
    session: JobInterviewSession,
    onAnswerSelected: (InterviewChoice) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = PaperWhite),
            border = BorderStroke(1.dp, BorderCardSlate),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Badge
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                session.isConcluded && session.isHired -> SuccessGreen.copy(alpha = 0.15f)
                                session.isConcluded && !session.isHired -> CoralNegative.copy(alpha = 0.15f)
                                else -> GoldAccent.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            session.isConcluded && session.isHired -> Icons.Rounded.CheckCircle
                            session.isConcluded && !session.isHired -> Icons.Rounded.Cancel
                            else -> Icons.Rounded.Business
                        },
                        contentDescription = null,
                        tint = when {
                            session.isConcluded && session.isHired -> SuccessGreen
                            session.isConcluded && !session.isHired -> CoralNegative
                            else -> GoldAccent
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.interview_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(
                        R.string.interview_dialog_subtitle,
                        session.companyName,
                        session.jobTitle
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = TealPrimary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (session.isConcluded) {
                    // Conclusion Screen
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = if (session.isHired) SuccessGreen.copy(alpha = 0.10f) else CoralNegative.copy(alpha = 0.10f),
                        border = BorderStroke(
                            1.dp,
                            if (session.isHired) SuccessGreen.copy(alpha = 0.35f) else CoralNegative.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(
                                    if (session.isHired) R.string.interview_hired_headline else R.string.interview_rejected_headline
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (session.isHired) SuccessGreen else CoralNegative,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (session.isHired) {
                                    stringResource(
                                        R.string.interview_hired_body,
                                        session.jobTitle,
                                        session.companyName
                                    )
                                } else {
                                    stringResource(R.string.interview_rejected_body)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (session.isHired) SuccessGreen else TealPrimary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.interview_close_btn),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    // Question Screen
                    val question = session.questions.getOrNull(session.currentQuestionIndex)
                    if (question != null) {
                        // Progress Counter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CreamMuted
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.interview_question_indicator,
                                        session.currentQuestionIndex + 1,
                                        session.questions.size
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = stringResource(
                                    R.string.interview_score_preview,
                                    session.accumulatedScore
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = GoldAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Previous Feedback Note if answering Question 2
                        session.previousFeedbackKey?.let { fbKey ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = GoldAccent.copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = stringResource(InterviewStrings.stringResFor(fbKey)),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = InkSecondary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        // Question Prompt Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = CreamMuted,
                            border = BorderStroke(1.dp, BorderCardSlate)
                        ) {
                            Text(
                                text = stringResource(InterviewStrings.stringResFor(question.promptResKey)),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = InkPrimary,
                                modifier = Modifier.padding(14.dp),
                                lineHeight = 21.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = stringResource(R.string.interview_panel_reaction),
                            style = MaterialTheme.typography.labelSmall,
                            color = InkTertiary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Answer Choice Cards
                        question.choices.forEach { choice ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onAnswerSelected(choice) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = PaperWhite),
                                border = BorderStroke(1.dp, BorderCardSlate)
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
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(TealPrimary)
                                    )
                                    Text(
                                        text = stringResource(InterviewStrings.stringResFor(choice.textResKey)),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = InkPrimary,
                                        modifier = Modifier.weight(1f),
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
