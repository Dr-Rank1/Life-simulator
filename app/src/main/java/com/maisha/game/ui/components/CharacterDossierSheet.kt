package com.maisha.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.CreditRatingTier
import com.maisha.game.domain.LifeArchetypeEngine
import com.maisha.game.ui.avatar.ExpressionResolver
import com.maisha.game.ui.theme.BorderCardSlate
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.CreamMuted
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.Hairline
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.StatMoney
import com.maisha.game.ui.theme.TealPrimary
import com.maisha.game.util.formatMoney

/**
 * Character Dossier Sheet: Tucks secondary stats (Credit Score, Karma, Fame, Aptitudes,
 * Community Standing, Consequence Flags) into a clean, arcade-styled dossier sheet
 * so the main screen remains focused on life events and primary progression.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CharacterDossierSheet(
    character: Character,
    netWorth: Int,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val expression = remember(character.stats.happiness, character.stats.health) {
        ExpressionResolver.resolveExpression(character, null)
    }

    val creditScore = character.finance.creditScore
    val creditTier = remember(creditScore) {
        CreditRatingTier.forScore(creditScore)
    }
    val chapterKey = remember(character.age, netWorth) {
        LifeArchetypeEngine.resolveChapterKey(character, netWorth)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PaperWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Avatar, Name, Chapter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(CreamMuted)
                        .border(1.5.dp, TealPrimary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    PersonAvatar(
                        avatarConfig = character.avatarConfig,
                        size = 50,
                        age = character.age,
                        expression = expression,
                        forPlayerCharacter = true,
                        hideFromAccessibility = true,
                        seed = "${character.gender.name}-${character.name}"
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = character.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        CountryFlag(countryCode = character.countryCode, size = 16.dp)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CreamMuted
                        ) {
                            Text(
                                text = stringResource(R.string.format_age, character.age),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = TealPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = stringResource(LifeArchetypeStrings.chapterStringRes(chapterKey)),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TealPrimary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = InkTertiary
                    )
                }
            }

            // Section 1: Financial Standing
            DossierSectionCard(
                title = "Financial Standing",
                icon = Icons.Rounded.AccountBalance,
                iconTint = StatMoney
            ) {
                // Credit Score Gauge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Credit Score",
                            style = MaterialTheme.typography.labelMedium,
                            color = InkSecondary
                        )
                        Text(
                            text = "$creditScore",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = when (creditTier) {
                                CreditRatingTier.EXCELLENT -> LifeGreen
                                CreditRatingTier.GOOD -> TealPrimary
                                CreditRatingTier.FAIR -> GoldAccent
                                CreditRatingTier.POOR -> CoralNegative
                            }
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (creditTier) {
                            CreditRatingTier.EXCELLENT -> LifeGreen.copy(alpha = 0.14f)
                            CreditRatingTier.GOOD -> TealPrimary.copy(alpha = 0.14f)
                            CreditRatingTier.FAIR -> GoldAccent.copy(alpha = 0.14f)
                            CreditRatingTier.POOR -> CoralNegative.copy(alpha = 0.14f)
                        }
                    ) {
                        Text(
                            text = creditTier.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (creditTier) {
                                CreditRatingTier.EXCELLENT -> LifeGreen
                                CreditRatingTier.GOOD -> TealPrimary
                                CreditRatingTier.FAIR -> GoldAccent
                                CreditRatingTier.POOR -> CoralNegative
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                val scorePercent = ((creditScore.toFloat() - 300f) / 550f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { scorePercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when (creditTier) {
                        CreditRatingTier.EXCELLENT -> LifeGreen
                        CreditRatingTier.GOOD -> TealPrimary
                        CreditRatingTier.FAIR -> GoldAccent
                        CreditRatingTier.POOR -> CoralNegative
                    },
                    trackColor = CreamMuted
                )

                // Living Standard & Net Worth
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DossierMetaItem(label = "Net Worth", value = formatMoney(netWorth, character.countryCode))
                    DossierMetaItem(label = "Liquid Cash", value = formatMoney(character.stats.money, character.countryCode))
                    DossierMetaItem(label = "Living Tier", value = character.lifestyle.livingStandard.name)
                }

                if (character.finance.lifetimeRemittancesSent > 0) {
                    DossierMetaItem(
                        label = "Remittances Sent",
                        value = formatMoney(character.finance.lifetimeRemittancesSent, character.countryCode)
                    )
                }
            }

            // Section 2: Moral & Community Standing
            DossierSectionCard(
                title = "Reputation & Moral Profile",
                icon = Icons.Rounded.Security,
                iconTint = GoldAccent
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DossierProgressItem(
                        label = "Karma",
                        value = character.stats.karma,
                        color = GoldAccent,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    DossierProgressItem(
                        label = "Public Fame",
                        value = character.career.fame.toInt(),
                        color = TealPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Community Standing Tier
                val standing = character.socialEcosystem.currentStandingTier
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Community Standing",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkSecondary
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CreamMuted
                    ) {
                        Text(
                            text = standing.name.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Section 3: Aptitudes & Academics
            DossierSectionCard(
                title = "Talents & Education",
                icon = Icons.Rounded.School,
                iconTint = Color(0xFF6366F1)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DossierMetaItem(
                        label = "Childhood Aptitude",
                        value = character.education.childhoodAptitude.name.replace("_", " ")
                    )
                    DossierMetaItem(
                        label = "School Clique",
                        value = character.education.schoolClique.name.replace("_", " ")
                    )
                }
                DossierMetaItem(
                    label = "Education Stage",
                    value = character.education.stage.name.replace("_", " ")
                )
            }

            // Section 4: Law & Police Heat
            if (character.criminalRecord.heatLevel > 0 || character.criminalRecord.hasRecord) {
                DossierSectionCard(
                    title = "Underworld & Legal Profile",
                    icon = Icons.Rounded.Gavel,
                    iconTint = CoralNegative
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DossierProgressItem(
                            label = "Police Heat",
                            value = character.criminalRecord.heatLevel,
                            color = CoralNegative,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        DossierMetaItem(
                            label = "Arrests",
                            value = "${character.criminalRecord.timesArrested} Times",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Section 5: Consequence Flags
            if (character.education.consequenceFlags.isNotEmpty()) {
                DossierSectionCard(
                    title = "Active Life Flags",
                    icon = Icons.Rounded.EmojiEvents,
                    iconTint = TealPrimary
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (flag in character.education.consequenceFlags) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CreamMuted
                            ) {
                                Text(
                                    text = flag.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = InkPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DossierSectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderCardSlate)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )
            }
            content()
        }
    }
}

@Composable
private fun DossierMetaItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = InkTertiary,
            fontSize = 10.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = InkPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DossierProgressItem(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val clamped = value.coerceIn(0, 100)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = InkSecondary)
            Text(text = "$clamped%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
        }
        LinearProgressIndicator(
            progress = { clamped / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = CreamMuted
        )
    }
}
