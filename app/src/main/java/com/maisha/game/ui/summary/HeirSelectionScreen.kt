// app/src/main/java/com/maisha/game/ui/summary/HeirSelectionScreen.kt
package com.maisha.game.ui.summary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.maisha.game.R
import com.maisha.game.data.model.Expression
import com.maisha.game.data.model.Person
import com.maisha.game.domain.HeirCandidatePreview
import com.maisha.game.ui.avatar.ProceduralAvatarPortrait
import com.maisha.game.ui.components.ConfirmActionDialog
import com.maisha.game.ui.components.ConfirmSeverity
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.Hairline
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.StatHealth
import com.maisha.game.ui.theme.StatSmarts
import com.maisha.game.ui.theme.TealPrimary
import com.maisha.game.util.formatMoney

@Composable
fun HeirSelectionDialog(
    heirs: List<Person>,
    slotNumber: Int,
    selectedHeir: Person?,
    showConfirmation: Boolean,
    heirPreviews: List<HeirCandidatePreview> = emptyList(),
    deceasedName: String? = null,
    legacyStamp: String? = null,
    epitaphText: String? = null,
    onHeirSelected: (Person) -> Unit,
    onConfirmContinue: () -> Unit,
    onDismissConfirmation: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showConfirmation && selectedHeir != null) {
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_legacy_confirm_title),
            description = stringResource(
                R.string.dialog_legacy_confirm_body,
                selectedHeir.name,
                slotNumber
            ),
            confirmLabel = stringResource(R.string.btn_continue_legacy),
            severity = ConfirmSeverity.NEUTRAL,
            onConfirm = onConfirmContinue,
            onDismiss = onDismissConfirmation
        )
        return
    }

    // Build or align heir candidate previews
    val candidatePreviews = if (heirPreviews.isNotEmpty()) {
        heirPreviews
    } else {
        heirs.map { heir ->
            HeirCandidatePreview(
                heir = heir,
                inheritanceCash = 0,
                estateSharePercent = 0,
                relationshipLevel = heir.relationshipLevel
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .heightIn(max = 620.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = PaperWhite),
            border = BorderStroke(1.dp, Hairline),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppIcons.HeirCrown,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.heir_selection_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = stringResource(R.string.heir_selection_subtitle, slotNumber),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }

                if (!deceasedName.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ),
                        border = BorderStroke(1.dp, Hairline)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (!legacyStamp.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GoldAccent.copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = legacyStamp,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = InkPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = stringResource(R.string.heir_torch_carrying, deceasedName),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary
                            )
                            if (!epitaphText.isNullOrBlank()) {
                                Text(
                                    text = "\"$epitaphText\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = InkSecondary
                                )
                            }
                        }
                    }
                }

                // Scrollable List of Rich Heir Dossier Cards
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    items(candidatePreviews, key = { it.heir.id }) { preview ->
                        HeirDossierCard(
                            preview = preview,
                            onSelect = { onHeirSelected(preview.heir) }
                        )
                    }
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Hairline)
                ) {
                    Text(
                        text = stringResource(R.string.btn_cancel),
                        style = MaterialTheme.typography.labelLarge,
                        color = InkSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun HeirDossierCard(
    preview: HeirCandidatePreview,
    onSelect: () -> Unit
) {
    val heir = preview.heir
    val expression = when {
        preview.relationshipLevel >= 70 -> Expression.HAPPY
        preview.relationshipLevel <= 35 -> Expression.SAD
        else -> Expression.NEUTRAL
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PaperWhite
        ),
        border = BorderStroke(1.dp, Hairline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Procedural Portrait
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, TealPrimary.copy(alpha = 0.4f), CircleShape)
                ) {
                    ProceduralAvatarPortrait(
                        config = heir.avatarConfig,
                        age = heir.age,
                        expression = expression,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Heir identity
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = heir.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${stringResource(R.string.format_age, heir.age)} · ${heir.occupation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TealPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Relationship,
                            contentDescription = null,
                            tint = Color(0xFFE11D48),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = stringResource(R.string.heir_relationship_bond, preview.relationshipLevel),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = InkSecondary
                        )
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkTertiary
                        )
                        Text(
                            text = "${stringResource(R.string.stat_smarts)} ${heir.stats.smarts}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatSmarts,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Select CTA
                Button(
                    onClick = onSelect,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = InkPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.HeirCrown,
                            contentDescription = null,
                            tint = InkPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.btn_choose_heir),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Inheritance Share & Estate Tag Pill
            if (preview.inheritanceCash > 0 || preview.estateSharePercent > 0) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GoldAccent.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = AppIcons.Money,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(
                                    R.string.heir_inheritance_share,
                                    formatMoney(preview.inheritanceCash, heir.countryCode),
                                    preview.estateSharePercent
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TealPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = stringResource(R.string.heir_successor_badge),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
