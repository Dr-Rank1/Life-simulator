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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
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
import androidx.compose.ui.platform.LocalView
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
import com.maisha.game.domain.InteractionType
import com.maisha.game.feedback.HapticType
import com.maisha.game.ui.feedback.LocalFeedbackManager
import com.maisha.game.ui.theme.AccentPink
import com.maisha.game.ui.theme.AmberWarning
import com.maisha.game.ui.theme.CrimsonDanger
import com.maisha.game.ui.theme.EmeraldCash
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.HairlineBorder
import com.maisha.game.ui.theme.OffWhitePrimary
import com.maisha.game.ui.theme.RoyalIndigo
import com.maisha.game.ui.theme.SlateNavyCard
import com.maisha.game.ui.theme.SlateNavyMuted
import com.maisha.game.ui.theme.SteelSecondary
import com.maisha.game.ui.theme.SteelTertiary
import com.maisha.game.ui.theme.TabularFigures
import com.maisha.game.ui.theme.TealLight
import com.maisha.game.ui.theme.TealPrimary

/**
 * Geometric monogram avatar badge generator (strictly no faces or emojis).
 */
fun getPersonInitials(name: String): String {
    val clean = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    return when {
        clean.isEmpty() -> "?"
        clean.size == 1 -> clean[0].take(2).uppercase()
        else -> "${clean[0].first()}${clean[1].first()}".uppercase()
    }
}

/**
 * Semantic accent color for relationship roles.
 */
fun relationColor(relation: RelationType): Color = when (relation) {
    RelationType.SPOUSE -> RoyalIndigo
    RelationType.MOTHER, RelationType.FATHER -> TealPrimary
    RelationType.SIBLING -> AmberWarning
    RelationType.CHILD -> AccentPink
    RelationType.FRIEND -> TealLight
    RelationType.BEST_FRIEND -> GoldAccent
    RelationType.ENEMY -> CrimsonDanger
    else -> SteelSecondary
}

/**
 * Personality trait badge resolution based on NPC stats.
 */
fun personTrait(person: Person): Pair<String, Color> = when {
    person.crazy >= 70 -> "VOLATILE" to CrimsonDanger
    person.crazy <= 30 -> "CALM" to EmeraldCash
    person.stats.smarts >= 75 -> "GENIUS" to RoyalIndigo
    person.stats.happiness >= 75 -> "CHEERFUL" to AmberWarning
    else -> "BALANCED" to SteelSecondary
}

/**
 * Tactile drill-down list row for NPCs in the Relationships screen:
 * Standardized 64.dp-72.dp rounded card (12.dp radius, #131822 fill, 1.dp border)
 * containing a clean 40.dp geometric/monogram avatar badge (no faces or emojis),
 * relationship role tag, personality trait pill, and a sleek animated relationship
 * meter bar with dynamic threshold colors (Emerald >60%, Amber 30-60%, Crimson <30%).
 * Tapping opens a ModalBottomSheet with interactive actions.
 */
@Composable
fun PersonCard(
    person: Person,
    relationLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    playerCountryCode: String? = null,
    insideContainer: Boolean = false,
    onQuickAction: ((InteractionType) -> Unit)? = null
) {
    val view = LocalView.current
    val feedbackManager = runCatching { LocalFeedbackManager.current }.getOrNull()

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

    val relColor = remember(person.relation) { relationColor(person.relation) }
    val (traitLabel, traitColor) = remember(person.crazy, person.stats.smarts, person.stats.happiness) {
        personTrait(person)
    }
    val initials = remember(person.name) { getPersonInitials(person.name) }

    val bondLevel = person.relationshipLevel.coerceIn(0, 100)
    val thresholdColor = when {
        bondLevel > 60 -> EmeraldCash
        bondLevel >= 30 -> AmberWarning
        else -> CrimsonDanger
    }

    val cardBorder = when (person.relation) {
        RelationType.BEST_FRIEND -> BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
        RelationType.ENEMY -> BorderStroke(1.dp, CrimsonDanger.copy(alpha = 0.5f))
        RelationType.SPOUSE -> BorderStroke(1.dp, RoyalIndigo.copy(alpha = 0.4f))
        else -> BorderStroke(1.dp, HairlineBorder)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp, max = 74.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = cardDescription
                role = Role.Button
            }
            .clickable {
                feedbackManager?.triggerHaptic(view, HapticType.LIGHT_TAP)
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SlateNavyCard,
            disabledContainerColor = SlateNavyMuted
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = cardBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left: 40dp Geometric / Monogram Avatar Badge (No faces or emojis)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(relColor.copy(alpha = 0.16f))
                    .border(1.dp, relColor.copy(alpha = 0.38f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = TabularFigures.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = relColor
                    )
                )
            }

            // Middle: Name, Role Tag Pill, Personality Trait Pill, Subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = person.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = OffWhitePrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    when (person.relation) {
                        RelationType.BEST_FRIEND -> Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        RelationType.ENEMY -> Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = CrimsonDanger,
                            modifier = Modifier.size(14.dp)
                        )
                        else -> Unit
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Relationship Role Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(relColor.copy(alpha = 0.16f))
                            .border(0.5.dp, relColor.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = relationLabel.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = relColor,
                            fontSize = 9.sp
                        )
                    }

                    // Personality Trait Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(traitColor.copy(alpha = 0.14f))
                            .border(0.5.dp, traitColor.copy(alpha = 0.32f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = traitLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = traitColor,
                            fontSize = 9.sp
                        )
                    }

                    // Age & Occupation Subtitle
                    val occupationPart = if (person.occupation.isNotBlank() && person.occupation != "Unemployed") {
                        " · ${person.occupation}"
                    } else ""
                    Text(
                        text = "$ageLabel$occupationPart",
                        style = MaterialTheme.typography.bodySmall,
                        color = SteelSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp
                    )
                }
            }

            // Right: Sleek Relationship Meter Bar + Right-Aligned Tabular Percentage & Chevron
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.width(52.dp)
                ) {
                    Text(
                        text = "$bondLevel%",
                        style = TabularFigures.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = thresholdColor
                        )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SlateNavyMuted)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(bondLevel / 100f)
                                .clip(RoundedCornerShape(2.dp))
                                .background(thresholdColor)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = SteelSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
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

/**
 * Geometric monogram avatar for NPCs (zero faces, zero emojis).
 */
@Composable
fun PersonAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 40,
    relation: RelationType = RelationType.FRIEND
) {
    val initials = getPersonInitials(name)
    val color = relationColor(relation)

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size / 4).dp))
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.38f), RoundedCornerShape((size / 4).dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = TabularFigures.copy(
                fontSize = (size * 0.35f).sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}

/**
 * Backwards compatibility overload for PersonAvatar taking AvatarConfig.
 * Renders a clean monogram/geometric badge using the provided seed or initials.
 */
@Composable
fun PersonAvatar(
    avatarConfig: AvatarConfig,
    modifier: Modifier = Modifier,
    size: Int = 40,
    age: Int = 18,
    expression: Expression = Expression.NEUTRAL,
    forPlayerCharacter: Boolean = false,
    hideFromAccessibility: Boolean = false,
    seed: String? = null
) {
    val initials = (seed?.take(2)?.uppercase()) ?: "ID"
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size / 4).dp))
            .background(TealPrimary.copy(alpha = 0.16f))
            .border(1.dp, TealPrimary.copy(alpha = 0.38f), RoundedCornerShape((size / 4).dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = TabularFigures.copy(
                fontSize = (size * 0.35f).sp,
                fontWeight = FontWeight.Bold,
                color = TealPrimary
            )
        )
    }
}
