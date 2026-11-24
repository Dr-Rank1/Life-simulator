// app/src/main/java/com/maisha/game/ui/components/PersonDetailSheet.kt
package com.maisha.game.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.SentimentVeryDissatisfied
import androidx.compose.material.icons.rounded.Toys
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maisha.game.R
import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.Person
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.RelationshipStage
import com.maisha.game.data.model.relationshipTierFor
import com.maisha.game.domain.GiftTier
import com.maisha.game.domain.InteractionType
import com.maisha.game.domain.RelationshipEngine
import com.maisha.game.ui.avatar.AvatarImage
import com.maisha.game.ui.avatar.ExpressionResolver
import com.maisha.game.ui.components.DrillDownRow
import com.maisha.game.ui.illustrations.EmptyStateIllustration
import com.maisha.game.ui.theme.AccentPink
import com.maisha.game.ui.theme.AppIcons
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import com.maisha.game.ui.theme.Hairline
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.SurfaceMuted
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.NavyDeep
import com.maisha.game.ui.theme.TealPrimary
import com.maisha.game.util.formatMoney

@Composable
fun PersonDetailSheet(
    member: Person,
    playerCountryCode: String,
    playerMoney: Int,
    playerAge: Int,
    isIncarcerated: Boolean = false,
    isMarried: Boolean,
    relationLabel: String,
    dateNightCost: Int = 0,
    childHospitalCost: Int = 0,
    divorceSettlementCost: Int = 0,
    onInteraction: (InteractionType, GiftTier?) -> Unit,
    onMakeExclusive: () -> Unit = {},
    onPropose: () -> Unit,
    onProposeWithPrenup: () -> Unit = onPropose,
    onBreakUp: () -> Unit,
    onHaveChild: () -> Unit,
    onReconcileRivalry: (String) -> Unit = {}
) {
    val isSpouse = member.relation == RelationType.SPOUSE
    val isChild = member.relation == RelationType.CHILD
    val isEnemy = member.relation == RelationType.ENEMY
    val isMinorChild = RelationshipEngine.isMinorChild(member)
    val isAdultChild = RelationshipEngine.isAdultChild(member)
    val canAskForMoney = !isSpouse && !isChild
    val canSetUpDate = member.relation == RelationType.SIBLING ||
        member.relation == RelationType.FRIEND ||
        member.relation == RelationType.BEST_FRIEND
    val pendingGiftTier = rememberConfirmableAction<GiftTier>()
    val pendingTravel = rememberConfirmableAction<Unit>()
    val pendingAllowance = rememberConfirmableAction<Unit>()
    val pendingBreakUp = rememberConfirmableAction<Unit>()
    val pendingDivorce = rememberConfirmableAction<Unit>()
    val pendingHaveChild = rememberConfirmableAction<Unit>()
    val pendingDateNight = rememberConfirmableAction<Unit>()
    val pendingFinancialSupport = rememberConfirmableAction<Unit>()
    val pendingCelebrateMilestone = rememberConfirmableAction<Unit>()
    val travelCost = EconomyScaler.scaleRelationshipCost(
        RelationshipEngine.TRAVEL_BASE_COST_KENYA,
        playerCountryCode,
        playerAge
    )
    val allowanceCost = EconomyScaler.scaleAmount(
        RelationshipEngine.ALLOWANCE_BASE_COST_KENYA,
        playerCountryCode
    )
    val resolvedDateNightCost = if (dateNightCost > 0) {
        dateNightCost
    } else {
        EconomyScaler.scaleRelationshipCost(
            RelationshipEngine.DATE_NIGHT_COST_KENYA,
            playerCountryCode,
            playerAge
        )
    }
    val resolvedChildCost = if (childHospitalCost > 0) {
        childHospitalCost
    } else {
        EconomyScaler.scaleAmount(
            RelationshipEngine.CHILD_HOSPITAL_COST_KENYA,
            playerCountryCode
        )
    }
    val resolvedDivorceCost = if (divorceSettlementCost > 0) {
        divorceSettlementCost
    } else {
        EconomyScaler.scaleAmount(
            RelationshipEngine.DIVORCE_SETTLEMENT_KENYA,
            playerCountryCode
        )
    }
    val financialSupportCost = EconomyScaler.scaleAmount(
        RelationshipEngine.FINANCIAL_SUPPORT_COST_KENYA,
        playerCountryCode
    )
    val celebrateMilestoneCost = EconomyScaler.scaleAmount(
        RelationshipEngine.CELEBRATE_MILESTONE_COST_KENYA,
        playerCountryCode
    )
    val tier = relationshipTierFor(member.relationshipLevel)
    val expression = ExpressionResolver.resolvePersonExpression(member)
    val travelEnabled = RelationshipEngine.canTravelTogether(member) && !isIncarcerated

    fun giftCost(tier: GiftTier) = EconomyScaler.scaleRelationshipCost(
        tier.baseCostKenya,
        playerCountryCode,
        playerAge
    )

    ConfirmableActionHost(
        state = pendingGiftTier,
        onConfirmed = { giftTier -> onInteraction(InteractionType.GIFT, giftTier) }
    ) { giftTier, onConfirm, onDismiss ->
        ConfirmActionDialog(
            title = stringResource(R.string.confirm_gift_title),
            description = stringResource(
                R.string.confirm_cost_body,
                formatMoney(giftCost(giftTier), playerCountryCode)
            ),
            confirmLabel = stringResource(R.string.btn_confirm),
            severity = ConfirmSeverity.NEUTRAL,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    ConfirmableActionHost(
        state = pendingTravel,
        onConfirmed = { onInteraction(InteractionType.TRAVEL_TOGETHER, null) }
    ) { _, onConfirm, onDismiss ->
        ConfirmActionDialog(
            title = stringResource(R.string.confirm_travel_title),
            description = stringResource(
                R.string.confirm_cost_body,
                formatMoney(travelCost, playerCountryCode)
            ),
            confirmLabel = stringResource(R.string.btn_confirm),
            severity = ConfirmSeverity.NEUTRAL,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    ConfirmableActionHost(
        state = pendingAllowance,
        onConfirmed = { onInteraction(InteractionType.PAY_ALLOWANCE, null) }
    ) { _, onConfirm, onDismiss ->
        ConfirmActionDialog(
            title = stringResource(R.string.confirm_allowance_title),
            description = stringResource(
                R.string.confirm_allowance_body,
                member.name,
                formatMoney(allowanceCost, playerCountryCode)
            ),
            confirmLabel = stringResource(R.string.btn_confirm),
            severity = ConfirmSeverity.NEUTRAL,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    ConfirmableActionHost(
        state = pendingDateNight,
        onConfirmed = { onInteraction(InteractionType.DATE_NIGHT, null) }
    ) { _, onConfirm, onDismiss ->
        ConfirmActionDialog(
            title = stringResource(R.string.btn_date_night),
            description = stringResource(
                R.string.confirm_cost_body,
                formatMoney(resolvedDateNightCost, playerCountryCode)
            ),
            confirmLabel = stringResource(R.string.btn_confirm),
            severity = ConfirmSeverity.NEUTRAL,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    ConfirmableActionHost(
        state = pendingHaveChild,
        onConfirmed = { onHaveChild() }
    ) { _, onConfirm, onDismiss ->
        ConfirmActionDialog(
            title = stringResource(R.string.confirm_have_child_title),
            description = stringResource(
                R.string.confirm_have_child_body,
                formatMoney(resolvedChildCost, playerCountryCode)
            ),
            confirmLabel = stringResource(R.string.btn_have_child),
            severity = ConfirmSeverity.NEUTRAL,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    ConfirmableActionHost(
        state = pendingCelebrateMilestone,
        onConfirmed = { onInteraction(InteractionType.CELEBRATE_MILESTONE, null) }
    ) { _, onConfirm, onDismiss ->
        ConfirmActionDialog(
            title = stringResource(R.string.btn_celebrate_milestone, formatMoney(celebrateMilestoneCost, playerCountryCode)),
            description = stringResource(
                R.string.confirm_cost_body,
                formatMoney(celebrateMilestoneCost, playerCountryCode)
            ),
            confirmLabel = stringResource(R.string.btn_confirm),
            severity = ConfirmSeverity.NEUTRAL,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    ConfirmableActionHost(
        state = pendingFinancialSupport,
        onConfirmed = { onInteraction(InteractionType.FINANCIAL_SUPPORT, null) }
    ) { _, onConfirm, onDismiss ->
        ConfirmActionDialog(
            title = stringResource(
                R.string.btn_financial_support,
                formatMoney(financialSupportCost, playerCountryCode)
            ),
            description = stringResource(
                R.string.confirm_cost_body,
                formatMoney(financialSupportCost, playerCountryCode)
            ),
            confirmLabel = stringResource(R.string.btn_confirm),
            severity = ConfirmSeverity.NEUTRAL,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    if (isSpouse && !member.isMarried) {
        ConfirmableActionHost(
            state = pendingBreakUp,
            onConfirmed = { onBreakUp() }
        ) { _, onConfirm, onDismiss ->
            ConfirmActionDialog(
                title = stringResource(R.string.confirm_break_up_title),
                description = stringResource(R.string.confirm_break_up_body, member.name),
                confirmLabel = stringResource(R.string.btn_break_up),
                severity = ConfirmSeverity.NEUTRAL,
                onConfirm = onConfirm,
                onDismiss = onDismiss
            )
        }
    }

    if (isSpouse && member.isMarried) {
        ConfirmableActionHost(
            state = pendingDivorce,
            onConfirmed = { onBreakUp() }
        ) { _, onConfirm, onDismiss ->
            ConfirmActionDialog(
                title = stringResource(R.string.confirm_divorce_title),
                description = stringResource(
                    R.string.confirm_divorce_body,
                    member.name,
                    formatMoney(resolvedDivorceCost.coerceAtMost(playerMoney), playerCountryCode)
                ),
                confirmLabel = stringResource(R.string.btn_divorce),
                severity = ConfirmSeverity.WARNING,
                onConfirm = onConfirm,
                onDismiss = onDismiss
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AvatarImage(
                    config = member.avatarConfig,
                    size = 64.dp,
                    age = member.age,
                    expression = expression,
                    seed = member.id
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (member.secondaryCountryCode != null) {
                            HeritageCountryFlags(
                                primaryCountryCode = member.countryCode,
                                secondaryCountryCode = member.secondaryCountryCode,
                                size = 18.dp
                            )
                        } else if (member.countryCode != playerCountryCode) {
                            CountryFlag(countryCode = member.countryCode, size = 18.dp)
                        }
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "$relationLabel · ${stringResource(R.string.format_age, member.age)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = relationshipTierLabel(tier),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isSpouse) {
                            val stageLabel = when {
                                member.isMarried -> stringResource(R.string.stage_married)
                                member.stage == RelationshipStage.ENGAGED -> stringResource(R.string.stage_engaged)
                                member.stage == RelationshipStage.EXCLUSIVE -> stringResource(R.string.stage_exclusive)
                                else -> stringResource(R.string.stage_casual)
                            }
                            Text(
                                text = "· $stageLabel",
                                style = MaterialTheme.typography.labelLarge,
                                color = TealPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            StatBar(
                type = StatType.RELATIONSHIP,
                value = member.relationshipLevel,
                label = stringResource(R.string.stat_relationship)
            )

            if (isSpouse || member.relation == RelationType.FRIEND || member.relation == RelationType.ENEMY) {
                StatBar(
                    type = StatType.KARMA,
                    value = member.crazy,
                    label = stringResource(R.string.stat_crazy)
                )
            }

            Text(
                text = stringResource(
                    R.string.format_person_vitals,
                    member.stats.health,
                    member.stats.happiness,
                    member.stats.looks
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // NPC Personality Tags
            val personalityTags: List<Int> = remember(member) {
                val tags = mutableListOf<Int>()
                when {
                    member.relationshipLevel >= 80 -> tags.add(R.string.npc_tag_devoted)
                    member.relationshipLevel >= 50 -> tags.add(R.string.npc_tag_friendly)
                    member.relationshipLevel >= 25 -> tags.add(R.string.npc_tag_distant)
                    else -> tags.add(R.string.npc_tag_hostile)
                }
                when {
                    member.crazy >= 70 -> tags.add(R.string.npc_tag_hot_tempered)
                    member.crazy <= 30 -> tags.add(R.string.npc_tag_level_headed)
                }
                tags
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                personalityTags.forEach { tagRes ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(0.5.dp, Hairline)
                    ) {
                        Text(
                            text = stringResource(tagRes),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Reactive Dialogue Quote Box
            val dialogueQuoteRes = remember(member.relationshipLevel) {
                when {
                    member.relationshipLevel >= 80 -> R.string.npc_dialogue_high_bond
                    member.relationshipLevel >= 55 -> R.string.npc_dialogue_warm
                    member.relationshipLevel >= 35 -> R.string.npc_dialogue_neutral
                    member.relationshipLevel >= 15 -> R.string.npc_dialogue_cool
                    else -> R.string.npc_dialogue_hostile
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = SurfaceMuted.copy(alpha = 0.6f),
                border = BorderStroke(0.5.dp, Hairline)
            ) {
                Text(
                    text = stringResource(dialogueQuoteRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            if (member.socialBond != com.maisha.game.data.model.SocialBondType.NONE) {
                val (bondBadgeColor, bondBadgeText, bondDesc) = when (member.socialBond) {
                    com.maisha.game.data.model.SocialBondType.RECURRING_RIVAL -> Triple(
                        GoldAccent,
                        stringResource(R.string.badge_rival),
                        stringResource(R.string.reactive_rival_desc)
                    )
                    com.maisha.game.data.model.SocialBondType.BENEFACTOR_MENTOR -> Triple(
                        TealPrimary,
                        stringResource(R.string.badge_benefactor),
                        stringResource(R.string.reactive_benefactor_desc)
                    )
                    com.maisha.game.data.model.SocialBondType.FAMILY_FEUD -> Triple(
                        CoralNegative,
                        stringResource(R.string.badge_feud),
                        stringResource(R.string.reactive_feud_desc, member.feudFamilyName ?: "Rival", 65)
                    )
                    com.maisha.game.data.model.SocialBondType.FAMILY_ALLY -> Triple(
                        LifeGreen,
                        stringResource(R.string.badge_ally),
                        stringResource(R.string.reactive_ally_desc, member.feudFamilyName ?: "Allied", 75)
                    )
                    com.maisha.game.data.model.SocialBondType.NONE -> Triple(androidx.compose.ui.graphics.Color.Transparent, "", "")
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = bondBadgeColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, bondBadgeColor.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = bondBadgeColor
                        ) {
                            Text(
                                text = bondBadgeText.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = com.maisha.game.ui.theme.PaperWhite,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = bondDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkPrimary,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (member.milestones.isNotEmpty()) {
                SheetSectionTitle(stringResource(R.string.person_memories_title))
                MilestoneTimeline(milestones = member.milestones)
            } else {
                EmptyStateCard(
                    illustration = EmptyStateIllustration.FAMILY,
                    title = stringResource(R.string.person_memories_title),
                    message = stringResource(R.string.empty_person_memories),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (isAdultChild) {
                SheetSectionTitle(stringResource(R.string.section_adult_child_actions))
                DetailSheetButton(
                    text = stringResource(R.string.btn_visit_adult_child),
                    onClick = { onInteraction(InteractionType.SPEND_TIME, null) }
                )
                DetailSheetButton(
                    text = stringResource(
                        R.string.btn_financial_support,
                        formatMoney(financialSupportCost, playerCountryCode)
                    ),
                    onClick = { pendingFinancialSupport.request(Unit) },
                    enabled = playerMoney >= financialSupportCost
                )
                DetailSheetButton(
                    text = stringResource(
                        R.string.btn_celebrate_milestone,
                        formatMoney(celebrateMilestoneCost, playerCountryCode)
                    ),
                    onClick = { pendingCelebrateMilestone.request(Unit) },
                    enabled = playerMoney >= celebrateMilestoneCost
                )
                DetailSheetButton(
                    text = stringResource(R.string.btn_discuss_life),
                    onClick = { onInteraction(InteractionType.DISCUSS_LIFE_CHOICES, null) }
                )
            }

            if (isMinorChild) {
                SheetSectionTitle(stringResource(R.string.section_parenting_actions))
                DetailSheetButton(
                    text = stringResource(R.string.btn_play_together),
                    onClick = { onInteraction(InteractionType.SPEND_TIME, null) }
                )
                DetailSheetButton(
                    text = stringResource(R.string.btn_help_homework),
                    onClick = { onInteraction(InteractionType.HELP_WITH_HOMEWORK, null) }
                )
                DetailSheetButton(
                    text = "${stringResource(R.string.btn_pay_allowance)} (${formatMoney(allowanceCost, playerCountryCode)})",
                    onClick = { pendingAllowance.request(Unit) },
                    enabled = playerMoney >= allowanceCost
                )
                DetailSheetButton(
                    text = "${stringResource(R.string.btn_gift_small)} (${formatMoney(giftCost(GiftTier.SMALL), playerCountryCode)})",
                    onClick = { pendingGiftTier.request(GiftTier.SMALL) },
                    enabled = playerMoney >= giftCost(GiftTier.SMALL)
                )
                DetailSheetButton(
                    text = "${stringResource(R.string.btn_gift_medium)} (${formatMoney(giftCost(GiftTier.MEDIUM), playerCountryCode)})",
                    onClick = { pendingGiftTier.request(GiftTier.MEDIUM) },
                    enabled = playerMoney >= giftCost(GiftTier.MEDIUM)
                )
                DetailSheetButton(
                    text = stringResource(R.string.btn_discipline),
                    icon = AppIcons.Argue,
                    iconTint = CoralNegative,
                    onClick = { onInteraction(InteractionType.DISCIPLINE, null) }
                )
            } else if (playerAge < 3) {
                // INFANT / TODDLER ACTIONS (Ages 0-2)
                SheetSectionTitle(stringResource(R.string.section_bond_actions))
                val cuddleDone = member.interactedTypesThisYear.contains(InteractionType.SPEND_TIME.name)
                DetailSheetButton(
                    text = stringResource(R.string.btn_cuddle_bond),
                    subtitle = if (cuddleDone) stringResource(R.string.interaction_done_this_year) else null,
                    enabled = !cuddleDone,
                    icon = Icons.Rounded.Favorite,
                    iconTint = AccentPink,
                    onClick = { onInteraction(InteractionType.SPEND_TIME, null) }
                )
                val babbleDone = member.interactedTypesThisYear.contains(InteractionType.COMPLIMENT.name)
                DetailSheetButton(
                    text = stringResource(R.string.btn_babble_smile),
                    subtitle = if (babbleDone) stringResource(R.string.interaction_done_this_year) else null,
                    enabled = !babbleDone,
                    icon = Icons.Rounded.SentimentSatisfied,
                    iconTint = GoldAccent,
                    onClick = { onInteraction(InteractionType.COMPLIMENT, null) }
                )
                val tantrumDone = member.interactedTypesThisYear.contains(InteractionType.ARGUE.name)
                DetailSheetButton(
                    text = stringResource(R.string.btn_throw_tantrum),
                    subtitle = if (tantrumDone) stringResource(R.string.interaction_done_this_year) else null,
                    enabled = !tantrumDone,
                    icon = Icons.Rounded.SentimentVeryDissatisfied,
                    iconTint = CoralNegative,
                    onClick = { onInteraction(InteractionType.ARGUE, null) }
                )
            } else if (playerAge in 3..5) {
                // YOUNG CHILD (Ages 3-5)
                SheetSectionTitle(stringResource(R.string.section_bond_actions))
                val playDone = member.interactedTypesThisYear.contains(InteractionType.SPEND_TIME.name)
                DetailSheetButton(
                    text = stringResource(R.string.btn_play_together),
                    subtitle = if (playDone) stringResource(R.string.interaction_done_this_year) else null,
                    enabled = !playDone,
                    icon = Icons.Rounded.Toys,
                    iconTint = LifeGreen,
                    onClick = { onInteraction(InteractionType.SPEND_TIME, null) }
                )
                val complimentDone = member.interactedTypesThisYear.contains(InteractionType.COMPLIMENT.name)
                DetailSheetButton(
                    text = stringResource(R.string.btn_compliment),
                    subtitle = if (complimentDone) stringResource(R.string.interaction_done_this_year) else null,
                    enabled = !complimentDone,
                    icon = Icons.Rounded.Favorite,
                    iconTint = AccentPink,
                    onClick = { onInteraction(InteractionType.COMPLIMENT, null) }
                )
                val tantrumDone = member.interactedTypesThisYear.contains(InteractionType.ARGUE.name)
                DetailSheetButton(
                    text = stringResource(R.string.btn_throw_tantrum),
                    subtitle = if (tantrumDone) stringResource(R.string.interaction_done_this_year) else null,
                    enabled = !tantrumDone,
                    icon = Icons.Rounded.SentimentVeryDissatisfied,
                    iconTint = CoralNegative,
                    onClick = { onInteraction(InteractionType.ARGUE, null) }
                )
            } else {
                // AGES 6+ (Primary, Secondary, Adult)
                SheetSectionTitle(stringResource(R.string.section_bond_actions))
                val spendTimeDone = member.interactedTypesThisYear.contains(InteractionType.SPEND_TIME.name)
                DetailSheetButton(
                    text = if (isChild) stringResource(R.string.btn_play_together) else stringResource(R.string.btn_spend_time),
                    subtitle = if (spendTimeDone) stringResource(R.string.interaction_done_this_year) else null,
                    enabled = !spendTimeDone,
                    onClick = { onInteraction(InteractionType.SPEND_TIME, null) }
                )
                val complimentDone = member.interactedTypesThisYear.contains(InteractionType.COMPLIMENT.name)
                DetailSheetButton(
                    text = stringResource(R.string.btn_compliment),
                    subtitle = if (complimentDone) stringResource(R.string.interaction_done_this_year) else null,
                    enabled = !complimentDone,
                    onClick = { onInteraction(InteractionType.COMPLIMENT, null) }
                )
                val adviceDone = member.interactedTypesThisYear.contains(InteractionType.ASK_FOR_ADVICE.name)
                DetailSheetButton(
                    text = stringResource(R.string.btn_ask_advice),
                    subtitle = if (adviceDone) stringResource(R.string.interaction_done_this_year) else null,
                    enabled = !adviceDone,
                    onClick = { onInteraction(InteractionType.ASK_FOR_ADVICE, null) }
                )

                if (playerAge >= 16) {
                    SheetSectionTitle(stringResource(R.string.section_spend_actions))
                    val giftSmallCost = giftCost(GiftTier.SMALL)
                    val giftSmallAffordable = playerMoney >= giftSmallCost
                    DetailSheetButton(
                        text = "${stringResource(R.string.btn_gift_small)} (${formatMoney(giftSmallCost, playerCountryCode)})",
                        subtitle = if (!giftSmallAffordable) stringResource(R.string.insufficient_funds) else null,
                        onClick = { pendingGiftTier.request(GiftTier.SMALL) },
                        enabled = giftSmallAffordable
                    )
                    val giftMedCost = giftCost(GiftTier.MEDIUM)
                    val giftMedAffordable = playerMoney >= giftMedCost
                    DetailSheetButton(
                        text = "${stringResource(R.string.btn_gift_medium)} (${formatMoney(giftMedCost, playerCountryCode)})",
                        subtitle = if (!giftMedAffordable) stringResource(R.string.insufficient_funds) else null,
                        onClick = { pendingGiftTier.request(GiftTier.MEDIUM) },
                        enabled = giftMedAffordable
                    )
                    val giftLargeCost = giftCost(GiftTier.LARGE)
                    val giftLargeAffordable = playerMoney >= giftLargeCost
                    DetailSheetButton(
                        text = "${stringResource(R.string.btn_gift_large)} (${formatMoney(giftLargeCost, playerCountryCode)})",
                        subtitle = if (!giftLargeAffordable) stringResource(R.string.insufficient_funds) else null,
                        onClick = { pendingGiftTier.request(GiftTier.LARGE) },
                        enabled = giftLargeAffordable
                    )
                    val travelAffordable = playerMoney >= travelCost
                    DetailSheetButton(
                        text = "${stringResource(R.string.btn_travel_together)} (${formatMoney(travelCost, playerCountryCode)})",
                        subtitle = if (!travelAffordable) stringResource(R.string.insufficient_funds) else null,
                        onClick = { pendingTravel.request(Unit) },
                        enabled = travelEnabled && travelAffordable
                    )
                    if (!travelEnabled && isIncarcerated) {
                        Text(
                            text = stringResource(R.string.travel_blocked_incarcerated),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (!travelEnabled) {
                        Text(
                            text = stringResource(R.string.travel_requires_quality_time),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                SheetSectionTitle(stringResource(R.string.section_fun_risk_actions))
                if (member.relation == RelationType.SIBLING && playerAge >= 3) {
                    val teamUpDone = member.interactedTypesThisYear.contains(InteractionType.SIBLING_TEAM_UP.name)
                    DetailSheetButton(
                        text = stringResource(R.string.btn_sibling_team_up),
                        subtitle = if (teamUpDone) stringResource(R.string.interaction_done_this_year) else null,
                        enabled = !teamUpDone,
                        onClick = { onInteraction(InteractionType.SIBLING_TEAM_UP, null) }
                    )
                    if (playerAge >= 5) {
                        val borrowDone = member.interactedTypesThisYear.contains(InteractionType.SIBLING_BORROW_ITEM.name)
                        DetailSheetButton(
                            text = stringResource(R.string.btn_sibling_borrow),
                            subtitle = if (borrowDone) stringResource(R.string.interaction_done_this_year) else null,
                            enabled = !borrowDone,
                            onClick = { onInteraction(InteractionType.SIBLING_BORROW_ITEM, null) }
                        )
                        val competeDone = member.interactedTypesThisYear.contains(InteractionType.SIBLING_COMPETE.name)
                        DetailSheetButton(
                            text = stringResource(R.string.btn_sibling_compete),
                            subtitle = if (competeDone) stringResource(R.string.interaction_done_this_year) else null,
                            enabled = !competeDone,
                            onClick = { onInteraction(InteractionType.SIBLING_COMPETE, null) }
                        )
                    }
                }
                val prankDone = member.interactedTypesThisYear.contains(InteractionType.PRANK.name)
                DetailSheetButton(
                    text = stringResource(R.string.btn_prank),
                    subtitle = if (prankDone) stringResource(R.string.interaction_done_this_year) else null,
                    enabled = !prankDone,
                    onClick = { onInteraction(InteractionType.PRANK, null) }
                )
                if (canSetUpDate && playerAge >= 16) {
                    val dateDone = member.interactedTypesThisYear.contains(InteractionType.SET_UP_ON_DATE.name)
                    DetailSheetButton(
                        text = stringResource(R.string.btn_set_up_date),
                        subtitle = if (dateDone) stringResource(R.string.interaction_done_this_year) else null,
                        enabled = !dateDone,
                        onClick = { onInteraction(InteractionType.SET_UP_ON_DATE, null) }
                    )
                }
                val argueDone = member.interactedTypesThisYear.contains(InteractionType.ARGUE.name)
                DetailSheetButton(
                    text = if (isChild) stringResource(R.string.btn_scold) else stringResource(R.string.btn_argue),
                    subtitle = if (argueDone) stringResource(R.string.interaction_done_this_year) else null,
                    enabled = !argueDone,
                    icon = AppIcons.Argue,
                    iconTint = CoralNegative,
                    onClick = { onInteraction(InteractionType.ARGUE, null) }
                )
                val insultDone = member.interactedTypesThisYear.contains(InteractionType.INSULT.name)
                DetailSheetButton(
                    text = stringResource(R.string.btn_insult),
                    subtitle = if (insultDone) stringResource(R.string.interaction_done_this_year) else null,
                    enabled = !insultDone,
                    icon = AppIcons.Insult,
                    iconTint = CoralNegative,
                    onClick = { onInteraction(InteractionType.INSULT, null) }
                )
                if (canAskForMoney && playerAge >= 6) {
                    val moneyDone = member.interactedTypesThisYear.contains(InteractionType.ASK_FOR_MONEY.name)
                    val moneyUnlocked = member.relationshipLevel > 60
                    DetailSheetButton(
                        text = if (moneyUnlocked) {
                            stringResource(R.string.btn_ask_for_money)
                        } else {
                            stringResource(R.string.btn_ask_for_money_locked)
                        },
                        subtitle = if (moneyDone) stringResource(R.string.interaction_done_this_year) else null,
                        onClick = { onInteraction(InteractionType.ASK_FOR_MONEY, null) },
                        enabled = moneyUnlocked && !moneyDone
                    )
                }
            }

            if (isEnemy || member.socialBond == com.maisha.game.data.model.SocialBondType.RECURRING_RIVAL) {
                SheetSectionTitle(stringResource(R.string.section_rival_actions))
                DetailSheetButton(
                    text = stringResource(R.string.btn_reconcile_rivalry),
                    onClick = { onReconcileRivalry(member.id) },
                    emphasizeGreen = true
                )
            }

            if (isSpouse) {
                SheetSectionTitle(stringResource(R.string.section_life_together))
                if (!member.isMarried) {
                    if (member.stage == RelationshipStage.CASUAL) {
                        DetailSheetButton(
                            text = stringResource(R.string.btn_make_exclusive),
                            onClick = onMakeExclusive,
                            enabled = member.relationshipLevel >= 40,
                            emphasizePink = true
                        )
                    }
                    DetailSheetButton(
                        text = if (member.relationshipLevel >= RelationshipEngine.PROPOSAL_THRESHOLD) {
                            stringResource(R.string.btn_propose_marriage)
                        } else {
                            stringResource(
                                R.string.btn_propose_locked,
                                RelationshipEngine.PROPOSAL_THRESHOLD
                            )
                        },
                        onClick = onPropose,
                        enabled = member.relationshipLevel >= RelationshipEngine.PROPOSAL_THRESHOLD,
                        emphasizePink = true
                    )
                    DetailSheetButton(
                        text = stringResource(R.string.btn_propose_with_prenup),
                        onClick = onProposeWithPrenup,
                        enabled = member.relationshipLevel >= RelationshipEngine.PROPOSAL_THRESHOLD,
                        emphasizePink = false
                    )
                }
                if (isMarried && member.isMarried) {
                    DetailSheetButton(
                        text = "${stringResource(R.string.btn_date_night)} (${formatMoney(resolvedDateNightCost, playerCountryCode)})",
                        onClick = { pendingDateNight.request(Unit) },
                        enabled = playerMoney >= resolvedDateNightCost,
                        emphasizePink = true
                    )
                    DetailSheetButton(
                        text = "${stringResource(R.string.btn_have_child)} (${formatMoney(resolvedChildCost, playerCountryCode)})",
                        onClick = { pendingHaveChild.request(Unit) },
                        enabled = playerMoney >= resolvedChildCost,
                        emphasizeGreen = true
                    )
                }
                DetailSheetButton(
                    text = if (member.isMarried) {
                        stringResource(R.string.btn_divorce)
                    } else {
                        stringResource(R.string.btn_break_up)
                    },
                    icon = AppIcons.Argue,
                    iconTint = CoralNegative,
                    onClick = {
                        if (member.isMarried) pendingDivorce.request(Unit)
                        else pendingBreakUp.request(Unit)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SheetSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = InkTertiary,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun DetailSheetButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconTint: androidx.compose.ui.graphics.Color? = null,
    emphasizePink: Boolean = false,
    emphasizeGreen: Boolean = false
) {
    val tint = when {
        emphasizePink -> AccentPink
        emphasizeGreen -> LifeGreen
        iconTint != null -> iconTint
        else -> TealPrimary
    }
    DrillDownRow(
        title = text,
        subtitle = subtitle,
        icon = icon ?: AppIcons.Relationship,
        iconTint = tint,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
fun relationshipTierLabel(tier: com.maisha.game.data.model.RelationshipTier): String = when (tier) {
    com.maisha.game.data.model.RelationshipTier.ESTRANGED -> stringResource(R.string.tier_estranged)
    com.maisha.game.data.model.RelationshipTier.DISTANT -> stringResource(R.string.tier_distant)
    com.maisha.game.data.model.RelationshipTier.COOL -> stringResource(R.string.tier_cool)
    com.maisha.game.data.model.RelationshipTier.FRIENDLY -> stringResource(R.string.tier_friendly)
    com.maisha.game.data.model.RelationshipTier.CLOSE -> stringResource(R.string.tier_close)
    com.maisha.game.data.model.RelationshipTier.INSEPARABLE -> stringResource(R.string.tier_inseparable)
}
