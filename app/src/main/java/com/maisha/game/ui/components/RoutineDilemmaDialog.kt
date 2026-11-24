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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.maisha.game.R
import com.maisha.game.data.model.DilemmaChoice
import com.maisha.game.data.model.RoutineCategory
import com.maisha.game.data.model.RoutineDilemma
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.BorderCardSlate
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.CreamMuted
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.TealPrimary

@Composable
fun RoutineDilemmaDialog(
    dilemma: RoutineDilemma,
    interpolatedPrompt: String,
    onChoiceSelected: (DilemmaChoice) -> Unit,
    onDismiss: () -> Unit
) {
    val categoryIcon = when (dilemma.category) {
        RoutineCategory.COMMUTE -> AppIcons.Travel
        RoutineCategory.WORK_STUDY -> AppIcons.Career
        RoutineCategory.WEEKEND_SPLURGE -> AppIcons.Money
        RoutineCategory.FAMILY_DINNER -> AppIcons.Family
    }
    val categoryColor = when (dilemma.category) {
        RoutineCategory.COMMUTE -> TealPrimary
        RoutineCategory.WORK_STUDY -> GoldAccent
        RoutineCategory.WEEKEND_SPLURGE -> LifeGreen
        RoutineCategory.FAMILY_DINNER -> Color(0xFFE91E63)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PaperWhite),
            border = BorderStroke(1.5.dp, categoryColor.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Category Pill + Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = categoryColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = stringResource(RoutineDilemmaStrings.categoryTitleRes(dilemma.category)),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dilemma Headline Title
                Text(
                    text = stringResource(RoutineDilemmaStrings.stringResFor(dilemma.titleResKey)),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Narrative Prompt
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamMuted),
                    border = BorderStroke(1.dp, BorderCardSlate.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = interpolatedPrompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkPrimary,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Choices
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    dilemma.choices.forEach { choice ->
                        DilemmaChoiceCard(
                            choice = choice,
                            categoryColor = categoryColor,
                            onClick = { onChoiceSelected(choice) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dismiss Option
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.routine_dilemma_dismiss),
                        style = MaterialTheme.typography.labelMedium,
                        color = InkTertiary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun DilemmaChoiceCard(
    choice: DilemmaChoice,
    categoryColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, BorderCardSlate),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(RoutineDilemmaStrings.stringResFor(choice.titleResKey)),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = categoryColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = stringResource(RoutineDilemmaStrings.stringResFor(choice.tagResKey)),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = categoryColor,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(RoutineDilemmaStrings.stringResFor(choice.descResKey)),
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Impact tags row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (choice.karmaDelta != 0) {
                    ImpactBadge(
                        text = if (choice.karmaDelta > 0) "+Karma" else "-Karma",
                        color = if (choice.karmaDelta > 0) LifeGreen else CoralNegative
                    )
                }
                if (choice.healthDelta != 0 || choice.failureHealthDelta != 0) {
                    val delta = if (choice.healthDelta != 0) choice.healthDelta else choice.failureHealthDelta
                    ImpactBadge(
                        text = if (delta > 0) "+Health" else "-Health",
                        color = if (delta > 0) LifeGreen else CoralNegative
                    )
                }
                if (choice.happinessDelta != 0 || choice.failureHappinessDelta != 0) {
                    val delta = if (choice.happinessDelta != 0) choice.happinessDelta else choice.failureHappinessDelta
                    ImpactBadge(
                        text = if (delta > 0) "+Happiness" else "-Happiness",
                        color = if (delta > 0) LifeGreen else CoralNegative
                    )
                }
                if (choice.smartsDelta != 0) {
                    ImpactBadge(
                        text = if (choice.smartsDelta > 0) "+Smarts" else "-Smarts",
                        color = if (choice.smartsDelta > 0) TealPrimary else CoralNegative
                    )
                }
                if (choice.relationshipDelta != 0) {
                    ImpactBadge(
                        text = if (choice.relationshipDelta > 0) "+Bond" else "-Bond",
                        color = if (choice.relationshipDelta > 0) LifeGreen else CoralNegative
                    )
                }
                if (choice.heatDelta != 0 || choice.failureHeatDelta != 0) {
                    ImpactBadge(
                        text = "+Heat",
                        color = CoralNegative
                    )
                }
                if (choice.cashDelta != 0 || choice.failureCashDelta != 0) {
                    val delta = if (choice.cashDelta != 0) choice.cashDelta else choice.failureCashDelta
                    ImpactBadge(
                        text = if (delta > 0) "+Cash" else "-Cash",
                        color = if (delta > 0) GoldAccent else CoralNegative
                    )
                }
            }
        }
    }
}

@Composable
private fun ImpactBadge(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}
