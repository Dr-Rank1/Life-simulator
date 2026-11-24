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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.R
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.WorkplaceAction
import com.maisha.game.data.model.WorkplacePerson
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.Hairline
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.SurfaceMuted
import com.maisha.game.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkplaceRosterSheet(
    character: Character,
    roster: List<WorkplacePerson>,
    onWorkplaceAction: (String, WorkplaceAction) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPerson by remember { mutableStateOf<WorkplacePerson?>(null) }

    val supervisor = roster.firstOrNull { it.isSupervisor }
    val coworkers = roster.filter { !it.isSupervisor }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.workplace_roster_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = character.career.companyName ?: stringResource(R.string.career_company_unknown),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.workplace_roster_count, roster.size),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Supervisor Section
        if (supervisor != null) {
            item {
                Text(
                    text = stringResource(R.string.section_direct_manager),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
                Spacer(modifier = Modifier.height(6.dp))
                WorkplacePersonCard(
                    person = supervisor,
                    isSupervisor = true,
                    onClick = { selectedPerson = supervisor }
                )
            }
        }

        // Coworkers Section
        if (coworkers.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.section_coworkers),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            items(coworkers, key = { it.id }) { coworker ->
                WorkplacePersonCard(
                    person = coworker,
                    isSupervisor = false,
                    onClick = { selectedPerson = coworker }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Sub-Sheet for Person Interaction
    selectedPerson?.let { person ->
        val subSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { selectedPerson = null },
            sheetState = subSheetState
        ) {
            WorkplacePersonActionSheet(
                character = character,
                person = person,
                onAction = { action ->
                    selectedPerson = null
                    onWorkplaceAction(person.id, action)
                },
                onDismiss = { selectedPerson = null }
            )
        }
    }
}

@Composable
private fun WorkplacePersonCard(
    person: WorkplacePerson,
    isSupervisor: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSupervisor) GoldAccent.copy(alpha = 0.08f) else PaperWhite
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSupervisor) GoldAccent.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isSupervisor) GoldAccent.copy(alpha = 0.2f) else SurfaceMuted),
                contentAlignment = Alignment.Center
            ) {
                PersonAvatar(
                    avatarConfig = person.avatarConfig,
                    size = 44,
                    age = 35,
                    seed = person.id
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = person.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isSupervisor) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GoldAccent
                        ) {
                            Text(
                                text = stringResource(R.string.badge_boss),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (person.isRival) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CoralNegative
                        ) {
                            Text(
                                text = stringResource(R.string.badge_rival).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (person.isMentor) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TealPrimary
                        ) {
                            Text(
                                text = stringResource(R.string.badge_mentor).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (person.interactedThisYear) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = stringResource(R.string.badge_interacted_this_year),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = person.role,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val personaTagRes = if (isSupervisor) {
                        when {
                            person.strictness >= 70 -> R.string.npc_tag_strict_mgr
                            person.relationshipLevel >= 65 -> R.string.npc_tag_supportive_mgr
                            else -> R.string.npc_tag_chill_mgr
                        }
                    } else {
                        when {
                            person.competence >= 75 -> R.string.npc_tag_ambitious_coworker
                            person.relationshipLevel >= 50 -> R.string.npc_tag_reliable_coworker
                            else -> R.string.npc_tag_slacker_coworker
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = stringResource(personaTagRes),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Competence & Strictness Meters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniMeter(
                        label = stringResource(R.string.stat_competence),
                        value = person.competence,
                        color = TealPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSupervisor) {
                        MiniMeter(
                            label = stringResource(R.string.stat_strictness),
                            value = person.strictness,
                            color = if (person.strictness > 65) CoralNegative else GoldAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Relationship Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.stat_relationship),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SurfaceMuted)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (person.relationshipLevel / 100f).coerceIn(0f, 1f))
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(LifeGreen)
                        )
                    }
                    Text(
                        text = "${person.relationshipLevel}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = LifeGreen
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun WorkplacePersonActionSheet(
    character: Character,
    person: WorkplacePerson,
    onAction: (WorkplaceAction) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Person Header
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(SurfaceMuted),
            contentAlignment = Alignment.Center
        ) {
            PersonAvatar(
                avatarConfig = person.avatarConfig,
                size = 64,
                age = 35,
                seed = person.id
            )
        }

        Text(
            text = person.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = person.role,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (person.isRival || person.isMentor) {
            val (badgeText, badgeColor, contextText) = if (person.isRival) {
                Triple(
                    stringResource(R.string.badge_rival).uppercase(),
                    CoralNegative,
                    stringResource(R.string.reactive_rival_desc)
                )
            } else {
                Triple(
                    stringResource(R.string.badge_mentor).uppercase(),
                    TealPrimary,
                    stringResource(R.string.reactive_mentor_desc)
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = contextText,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary,
                        maxLines = 2
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Mini Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatPill(
                label = stringResource(R.string.stat_relationship),
                value = "${person.relationshipLevel}%",
                color = LifeGreen
            )
            StatPill(
                label = stringResource(R.string.stat_competence),
                value = "${person.competence}%",
                color = TealPrimary
            )
            StatPill(
                label = stringResource(R.string.stat_strictness),
                value = "${person.strictness}%",
                color = if (person.strictness > 65) CoralNegative else GoldAccent
            )
        }

        val isSupervisor = person.role == "Supervisor" || person.role == "Boss"
        val personaTagRes = if (isSupervisor) {
            when {
                person.strictness >= 70 -> R.string.npc_tag_strict_mgr
                person.relationshipLevel >= 65 -> R.string.npc_tag_supportive_mgr
                else -> R.string.npc_tag_chill_mgr
            }
        } else {
            when {
                person.competence >= 75 -> R.string.npc_tag_ambitious_coworker
                person.relationshipLevel >= 50 -> R.string.npc_tag_reliable_coworker
                else -> R.string.npc_tag_slacker_coworker
            }
        }
        val dialogueRes = if (isSupervisor) {
            when {
                person.relationshipLevel >= 65 -> R.string.npc_dialogue_mgr_pleased
                person.relationshipLevel <= 35 -> R.string.npc_dialogue_mgr_critical
                else -> R.string.npc_dialogue_mgr_neutral
            }
        } else {
            if (person.relationshipLevel >= 50) {
                R.string.npc_dialogue_coworker_friendly
            } else {
                R.string.npc_dialogue_coworker_aloof
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = SurfaceMuted.copy(alpha = 0.6f),
            border = BorderStroke(0.5.dp, Hairline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = stringResource(personaTagRes),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = stringResource(dialogueRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (person.interactedThisYear) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CoralNegative.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CoralNegative.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.msg_already_interacted_this_year, person.name),
                    style = MaterialTheme.typography.bodySmall,
                    color = CoralNegative,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val canInteract = !person.interactedThisYear

        // Action Buttons
        WorkplaceActionButton(
            label = stringResource(R.string.action_schmooze),
            subtitle = stringResource(R.string.action_schmooze_desc),
            icon = Icons.Default.ThumbUp,
            tint = LifeGreen,
            riskBadge = stringResource(R.string.badge_risk_low),
            riskColor = LifeGreen,
            cooldownBadge = stringResource(R.string.badge_cooldown_yearly),
            enabled = canInteract,
            onClick = { onAction(WorkplaceAction.SCHMOOZE) }
        )

        WorkplaceActionButton(
            label = stringResource(R.string.action_mentor_request),
            subtitle = stringResource(R.string.action_mentor_request_desc),
            icon = Icons.Default.MilitaryTech,
            tint = GoldAccent,
            riskBadge = stringResource(R.string.badge_risk_low),
            riskColor = LifeGreen,
            enabled = canInteract,
            onClick = { onAction(WorkplaceAction.MENTOR_REQUEST) }
        )

        WorkplaceActionButton(
            label = stringResource(R.string.action_pitch_project),
            subtitle = stringResource(R.string.action_pitch_project_desc),
            icon = Icons.Default.Work,
            tint = TealPrimary,
            riskBadge = stringResource(R.string.badge_risk_med),
            riskColor = GoldAccent,
            enabled = canInteract,
            onClick = { onAction(WorkplaceAction.PITCH_PROJECT) }
        )

        if (person.isSupervisor) {
            WorkplaceActionButton(
                label = stringResource(R.string.action_ask_raise),
                subtitle = stringResource(R.string.action_ask_raise_desc),
                icon = Icons.Default.Work,
                tint = GoldAccent,
                riskBadge = stringResource(R.string.badge_risk_med),
                riskColor = GoldAccent,
                cooldownBadge = stringResource(R.string.badge_req_rapport),
                enabled = canInteract,
                onClick = { onAction(WorkplaceAction.ASK_RAISE) }
            )

            WorkplaceActionButton(
                label = stringResource(R.string.action_ask_promotion),
                subtitle = stringResource(R.string.action_ask_promotion_desc),
                icon = Icons.Default.MilitaryTech,
                tint = TealPrimary,
                riskBadge = stringResource(R.string.badge_risk_high),
                riskColor = CoralNegative,
                cooldownBadge = stringResource(R.string.badge_req_performance),
                enabled = canInteract,
                onClick = { onAction(WorkplaceAction.ASK_PROMOTION) }
            )
        } else {
            WorkplaceActionButton(
                label = stringResource(R.string.action_collaborate),
                subtitle = stringResource(R.string.action_collaborate_desc),
                icon = Icons.Default.Work,
                tint = TealPrimary,
                riskBadge = stringResource(R.string.badge_risk_low),
                riskColor = LifeGreen,
                enabled = canInteract,
                onClick = { onAction(WorkplaceAction.COLLABORATE) }
            )

            WorkplaceActionButton(
                label = stringResource(R.string.action_office_romance),
                subtitle = stringResource(R.string.action_office_romance_desc),
                icon = Icons.Default.Favorite,
                tint = CoralNegative,
                riskBadge = stringResource(R.string.badge_risk_med),
                riskColor = GoldAccent,
                enabled = canInteract,
                onClick = { onAction(WorkplaceAction.OFFICE_ROMANCE) }
            )
        }

        WorkplaceActionButton(
            label = stringResource(R.string.action_office_prank),
            subtitle = stringResource(R.string.action_office_prank_desc),
            icon = Icons.Default.Person,
            tint = GoldAccent,
            riskBadge = stringResource(R.string.badge_risk_high),
            riskColor = CoralNegative,
            enabled = canInteract,
            onClick = { onAction(WorkplaceAction.PRANK) }
        )

        WorkplaceActionButton(
            label = stringResource(R.string.action_report_hr),
            subtitle = stringResource(R.string.action_report_hr_desc),
            icon = Icons.Default.Report,
            tint = CoralNegative,
            riskBadge = stringResource(R.string.badge_risk_high),
            riskColor = CoralNegative,
            enabled = canInteract,
            onClick = { onAction(WorkplaceAction.REPORT_HR) }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun MiniMeter(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = InkSecondary
            )
            Text(
                text = "$value%",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(SurfaceMuted)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (value / 100f).coerceIn(0f, 1f))
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun WorkplaceActionButton(
    label: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    riskBadge: String? = null,
    riskColor: Color = GoldAccent,
    cooldownBadge: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) PaperWhite else SurfaceMuted.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    riskBadge?.let { badge ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = riskColor.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = riskColor,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    cooldownBadge?.let { cooldown ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = TealPrimary.copy(alpha = 0.10f)
                        ) {
                            Text(
                                text = cooldown,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = TealPrimary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
