// app/src/main/java/com/maisha/game/ui/components/PersonCard.kt (modified — Prompt 27: MaishaRadius/spacing, a11y, 360dp ellipsis)
package com.maisha.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.R
import com.maisha.game.data.model.AvatarConfig
import com.maisha.game.data.model.Expression
import com.maisha.game.data.model.Person
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.RelationshipTier
import com.maisha.game.data.model.SocialBondType
import com.maisha.game.data.model.relationshipTierFor
import com.maisha.game.ui.avatar.AvatarImage
import com.maisha.game.ui.avatar.ExpressionResolver
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.Hairline
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.MaishaRadius
import com.maisha.game.ui.theme.MaishaSpacing
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.TealPrimary


@Composable
fun PersonCard(
    person: Person,
    relationLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    playerCountryCode: String? = null,
    insideContainer: Boolean = false
) {
    val expression = remember(person.id, person.relationshipLevel) {
        ExpressionResolver.resolvePersonExpression(person)
    }
    val tier = remember(person.id, person.relationshipLevel) {
        relationshipTierFor(person.relationshipLevel)
    }
    val tierLabel = rememberRelationshipTierLabel(tier)
    val ageLabel = stringResource(R.string.format_age, person.age)
    val cardDescription = stringResource(
        R.string.content_desc_person_card,
        person.name,
        tierLabel,
        ageLabel
    )

    val socialBorder = when (person.relation) {
        RelationType.BEST_FRIEND -> BorderStroke(1.5.dp, GoldAccent)
        RelationType.ENEMY -> BorderStroke(1.5.dp, CoralNegative)
        else -> BorderStroke(1.dp, Hairline)
    }

    val rowContent = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = if (insideContainer) 10.dp else MaishaSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(MaishaSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PersonAvatar(
                avatarConfig = person.avatarConfig,
                size = 44,
                age = person.age,
                expression = expression,
                hideFromAccessibility = true,
                seed = person.id
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaishaSpacing.xs)
                ) {
                    if (person.secondaryCountryCode != null) {
                        HeritageCountryFlags(
                            primaryCountryCode = person.countryCode,
                            secondaryCountryCode = person.secondaryCountryCode,
                            size = 16.dp
                        )
                    } else if (playerCountryCode != null && person.countryCode != playerCountryCode) {
                        CountryFlag(countryCode = person.countryCode, size = 16.dp)
                    }
                    Text(
                        text = person.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    when (person.relation) {
                        RelationType.BEST_FRIEND -> Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        RelationType.ENEMY -> Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = CoralNegative,
                            modifier = Modifier.size(16.dp)
                        )
                        else -> Unit
                    }
                    if (person.socialBond != SocialBondType.NONE) {
                        val (badgeText, badgeColor) = when (person.socialBond) {
                            SocialBondType.RECURRING_RIVAL -> stringResource(R.string.badge_rival).uppercase() to GoldAccent
                            SocialBondType.BENEFACTOR_MENTOR -> stringResource(R.string.badge_benefactor).uppercase() to TealPrimary
                            SocialBondType.FAMILY_FEUD -> stringResource(R.string.badge_feud).uppercase() to CoralNegative
                            SocialBondType.FAMILY_ALLY -> stringResource(R.string.badge_ally).uppercase() to LifeGreen
                            SocialBondType.NONE -> "" to Color.Transparent
                        }
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = badgeColor
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PaperWhite,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$relationLabel, $ageLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (person.relationshipLevel < 40) {
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = CoralNegative.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, CoralNegative.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = stringResource(R.string.badge_needs_attention).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CoralNegative,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                fontSize = 8.sp
                            )
                        }
                    } else if (person.milestones.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = GoldAccent.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, GoldAccent.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = stringResource(R.string.badge_recent_milestone).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "${person.relationshipLevel}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            person.relationshipLevel >= 70 -> TealPrimary
                            person.relationshipLevel <= 30 -> CoralNegative
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Hairline)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (person.relationshipLevel / 100f).coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when {
                                        person.relationshipLevel >= 70 -> TealPrimary
                                        person.relationshipLevel <= 30 -> CoralNegative
                                        else -> GoldAccent
                                    }
                                )
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    if (insideContainer) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = cardDescription
                    role = Role.Button
                }
                .clickable(onClick = onClick)
        ) {
            rowContent()
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = cardDescription
                    role = Role.Button
                }
                .clickable(onClick = onClick),
            shape = MaishaRadius.cardShape,
            colors = CardDefaults.cardColors(
                containerColor = when (person.relation) {
                    RelationType.BEST_FRIEND -> GoldAccent.copy(alpha = 0.12f)
                    RelationType.ENEMY -> CoralNegative.copy(alpha = 0.10f)
                    else -> MaterialTheme.colorScheme.surface
                }
            ),
            border = socialBorder,
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            rowContent()
        }
    }
}

@Composable
private fun rememberRelationshipTierLabel(tier: RelationshipTier): String = when (tier) {
    RelationshipTier.ESTRANGED -> stringResource(R.string.tier_estranged)
    RelationshipTier.DISTANT -> stringResource(R.string.tier_distant)
    RelationshipTier.COOL -> stringResource(R.string.tier_cool)
    RelationshipTier.FRIENDLY -> stringResource(R.string.tier_friendly)
    RelationshipTier.CLOSE -> stringResource(R.string.tier_close)
    RelationshipTier.INSEPARABLE -> stringResource(R.string.tier_inseparable)
}

@Composable
fun PersonAvatar(
    avatarConfig: AvatarConfig,
    modifier: Modifier = Modifier,
    size: Int = 44,
    age: Int = 18,
    expression: Expression = Expression.NEUTRAL,
    forPlayerCharacter: Boolean = false,
    hideFromAccessibility: Boolean = false,
    seed: String? = null
) {
    AvatarImage(
        config = avatarConfig,
        size = size.dp,
        modifier = modifier,
        age = age,
        expression = expression,
        forPlayerCharacter = forPlayerCharacter,
        hideFromAccessibility = hideFromAccessibility,
        seed = seed
    )
}

@Composable
fun PersonAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 44
) {
    val initials = name.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")
        .ifEmpty { "?" }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(TealPrimary.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TealPrimary
        )
    }
}
