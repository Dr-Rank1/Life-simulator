// app/src/main/java/com/maisha/game/ui/components/PersonDetailSheet.kt
package com.maisha.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.SentimentVeryDissatisfied
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Toys
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.Expression
import com.maisha.game.data.model.Person
import com.maisha.game.data.model.RelationType
import com.maisha.game.data.model.RelationshipStage
import com.maisha.game.data.model.relationshipTierFor
import com.maisha.game.domain.GiftTier
import com.maisha.game.domain.InteractionType
import com.maisha.game.domain.RelationshipEngine
import com.maisha.game.ui.avatar.ProceduralAvatarPortrait
import com.maisha.game.ui.illustrations.EmptyStateIllustration
import com.maisha.game.ui.theme.AccentPink
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.Hairline
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.SurfaceMuted
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

    // Local delta & reactive speech tracker
    var prevLevel by remember(member.id) { mutableIntStateOf(member.relationshipLevel) }
    var deltaBond by remember(member.id) { mutableIntStateOf(0) }
    var recentInteraction by remember(member.id) { mutableStateOf<InteractionType?>(null) }

    LaunchedEffect(member.relationshipLevel) {
        if (member.relationshipLevel != prevLevel) {
            deltaBond = member.relationshipLevel - prevLevel
            prevLevel = member.relationshipLevel
        }
    }

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

    // Expression synced to live relationship & temperament
    val liveExpression = remember(member.relationshipLevel, member.crazy) {
        when {
            member.relationshipLevel < 25 -> Expression.ANGRY
            member.relationshipLevel < 45 -> Expression.SAD
            member.relationshipLevel >= 75 -> Expression.HAPPY
            member.crazy >= 70 -> Expression.SURPRISED
            else -> Expression.NEUTRAL
        }
    }
    val travelEnabled = RelationshipEngine.canTravelTogether(member) && !isIncarcerated

    fun giftCost(tier: GiftTier) = EconomyScaler.scaleRelationshipCost(
        tier.baseCostKenya,
        playerCountryCode,
        playerAge
    )

    fun handleInteraction(type: InteractionType, tier: GiftTier? = null) {
        recentInteraction = type
        onInteraction(type, tier)
    }

    ConfirmableActionHost(
        state = pendingGiftTier,
        onConfirmed = { giftTier -> handleInteraction(InteractionType.GIFT, giftTier) }
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
        onConfirmed = { handleInteraction(InteractionType.TRAVEL_TOGETHER, null) }
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
        onConfirmed = { handleInteraction(InteractionType.PAY_ALLOWANCE, null) }
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
        onConfirmed = { handleInteraction(InteractionType.DATE_NIGHT, null) }
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
        onConfirmed = { handleInteraction(InteractionType.CELEBRATE_MILESTONE, null) }
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
        onConfirmed = { handleInteraction(InteractionType.FINANCIAL_SUPPORT, null) }
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
        // BitLife-Style Character Header Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(18.dp),
            color = SurfaceMuted.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, Hairline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Row: 64dp Avatar + Role/Clique Badges + Name & Occupation
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PaperWhite)
                            .border(2.dp, TealPrimary.copy(alpha = 0.4f), CircleShape)
                    ) {
                        ProceduralAvatarPortrait(
                            config = member.avatarConfig,
                            age = member.age,
                            expression = liveExpression,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Role / Clique Badge Pill
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val roleBadgeColor = when (member.relation) {
                                RelationType.SPOUSE -> AccentPink
                                RelationType.MOTHER, RelationType.FATHER -> TealPrimary
                                RelationType.SIBLING -> GoldAccent
                                RelationType.CHILD -> LifeGreen
                                RelationType.ENEMY -> CoralNegative
                                RelationType.BEST_FRIEND -> GoldAccent
                                else -> TealPrimary
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = roleBadgeColor.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, roleBadgeColor.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = relationLabel.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = roleBadgeColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp
                                )
                            }

                            if (isSpouse) {
                                val stageLabel = when {
                                    member.isMarried -> stringResource(R.string.stage_married)
                                    member.stage == RelationshipStage.ENGAGED -> stringResource(R.string.stage_engaged)
                                    member.stage == RelationshipStage.EXCLUSIVE -> stringResource(R.string.stage_exclusive)
                                    else -> stringResource(R.string.stage_casual)
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = TealPrimary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = stageLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TealPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Name with Flags
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (member.secondaryCountryCode != null) {
                                HeritageCountryFlags(
                                    primaryCountryCode = member.countryCode,
                                    secondaryCountryCode = member.secondaryCountryCode,
                                    size = 16.dp
                                )
                            } else if (member.countryCode != playerCountryCode) {
                                CountryFlag(countryCode = member.countryCode, size = 16.dp)
                            }
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Age & Occupation Subtitle
                        val subtitleText = if (member.occupation.isNotBlank()) {
                            "${stringResource(R.string.format_age, member.age)} · ${member.occupation}"
                        } else {
                            stringResource(R.string.format_age, member.age)
                        }
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Dual Color-Coded Stat Bars: Relationship & Temperament
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Relationship Meter
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.stat_relationship),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = InkSecondary
                            )
                            Text(
                                text = "${member.relationshipLevel}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    member.relationshipLevel >= 70 -> TealPrimary
                                    member.relationshipLevel < 35 -> CoralNegative
                                    else -> GoldAccent
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(3.5.dp))
                                .background(Hairline)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = (member.relationshipLevel / 100f).coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(3.5.dp))
                                    .background(
                                        when {
                                            member.relationshipLevel >= 70 -> TealPrimary
                                            member.relationshipLevel < 35 -> CoralNegative
                                            else -> GoldAccent
                                        }
                                    )
                            )
                        }
                    }

                    // Temperament / Drama Meter
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.stat_temperament),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = InkSecondary
                            )
                            Text(
                                text = "${member.crazy}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    member.crazy >= 70 -> CoralNegative
                                    member.crazy <= 35 -> LifeGreen
                                    else -> GoldAccent
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(3.5.dp))
                                .background(Hairline)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = (member.crazy / 100f).coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(3.5.dp))
                                    .background(
                                        when {
                                            member.crazy >= 70 -> CoralNegative
                                            member.crazy <= 35 -> LifeGreen
                                            else -> GoldAccent
                                        }
                                    )
                            )
                        }
                    }
                }

                // Reactive Speech / Thought Banner + Floating Delta Feedback
                val reactiveThought = when (recentInteraction) {
                    InteractionType.COMPLIMENT -> stringResource(R.string.npc_thought_compliment)
                    InteractionType.GIFT -> stringResource(R.string.npc_thought_gift)
                    InteractionType.ARGUE -> stringResource(R.string.npc_thought_argue)
                    InteractionType.PRANK -> stringResource(R.string.npc_thought_prank)
                    InteractionType.ASK_FOR_MONEY -> stringResource(R.string.npc_thought_money)
                    InteractionType.SPEND_TIME -> stringResource(R.string.npc_thought_time)
                    else -> when {
                        member.relationshipLevel >= 80 -> stringResource(R.string.npc_dialogue_high_bond)
                        member.relationshipLevel >= 55 -> stringResource(R.string.npc_dialogue_warm)
                        member.relationshipLevel >= 35 -> stringResource(R.string.npc_dialogue_neutral)
                        member.relationshipLevel >= 15 -> stringResource(R.string.npc_dialogue_cool)
                        else -> stringResource(R.string.npc_dialogue_hostile)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = PaperWhite,
                    border = BorderStroke(0.5.dp, Hairline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = reactiveThought,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkPrimary,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.weight(1f)
                        )

                        AnimatedVisibility(
                            visible = deltaBond != 0,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (deltaBond > 0) LifeGreen.copy(alpha = 0.15f) else CoralNegative.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, if (deltaBond > 0) LifeGreen else CoralNegative)
                            ) {
                                Text(
                                    text = if (deltaBond > 0) {
                                        stringResource(R.string.feedback_relationship_up, deltaBond)
                                    } else {
                                        stringResource(R.string.feedback_relationship_down, deltaBond)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (deltaBond > 0) LifeGreen else CoralNegative,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Social Bond Feud/Ally Tag (if active)
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
                        com.maisha.game.data.model.SocialBondType.NONE -> Triple(Color.Transparent, "", "")
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = bondBadgeColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, bondBadgeColor.copy(alpha = 0.35f)),
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
                                    color = PaperWhite,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    fontSize = 10.sp
                                )
                            }
                            Text(
                                text = bondDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = InkPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Tactile 2-Column Grid of Interaction Tiles
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Milestone / Memories
            if (member.milestones.isNotEmpty()) {
                SheetSectionTitle(stringResource(R.string.person_memories_title))
                MilestoneTimeline(milestones = member.milestones)
            }

            // Adult Child Actions
            if (isAdultChild) {
                SheetSectionTitle(stringResource(R.string.section_adult_child_actions))
                val visitDone = member.interactedTypesThisYear.contains(InteractionType.SPEND_TIME.name)
                val discussDone = member.interactedTypesThisYear.contains(InteractionType.DISCUSS_LIFE_CHOICES.name)

                InteractionGridRow(
                    tile1 = {
                        InteractionTile(
                            title = stringResource(R.string.tile_visit),
                            icon = AppIcons.SpendTime,
                            iconTint = TealPrimary,
                            isUsedThisYear = visitDone,
                            onClick = { handleInteraction(InteractionType.SPEND_TIME, null) }
                        )
                    },
                    tile2 = {
                        InteractionTile(
                            title = stringResource(R.string.tile_discuss_life),
                            icon = Icons.Rounded.Psychology,
                            iconTint = GoldAccent,
                            isUsedThisYear = discussDone,
                            onClick = { handleInteraction(InteractionType.DISCUSS_LIFE_CHOICES, null) }
                        )
                    }
                )

                InteractionGridRow(
                    tile1 = {
                        InteractionTile(
                            title = stringResource(R.string.tile_financial_support),
                            icon = Icons.Rounded.AttachMoney,
                            iconTint = LifeGreen,
                            costLabel = formatMoney(financialSupportCost, playerCountryCode),
                            enabled = playerMoney >= financialSupportCost,
                            onClick = { pendingFinancialSupport.request(Unit) }
                        )
                    },
                    tile2 = {
                        InteractionTile(
                            title = stringResource(R.string.tile_celebrate),
                            icon = Icons.Rounded.Celebration,
                            iconTint = AccentPink,
                            costLabel = formatMoney(celebrateMilestoneCost, playerCountryCode),
                            enabled = playerMoney >= celebrateMilestoneCost,
                            onClick = { pendingCelebrateMilestone.request(Unit) }
                        )
                    }
                )
            }

            // Minor Child Actions
            if (isMinorChild) {
                SheetSectionTitle(stringResource(R.string.section_parenting_actions))
                val playDone = member.interactedTypesThisYear.contains(InteractionType.SPEND_TIME.name)
                val hwDone = member.interactedTypesThisYear.contains(InteractionType.HELP_WITH_HOMEWORK.name)

                InteractionGridRow(
                    tile1 = {
                        InteractionTile(
                            title = stringResource(R.string.tile_play_together),
                            icon = Icons.Rounded.Toys,
                            iconTint = LifeGreen,
                            isUsedThisYear = playDone,
                            onClick = { handleInteraction(InteractionType.SPEND_TIME, null) }
                        )
                    },
                    tile2 = {
                        InteractionTile(
                            title = stringResource(R.string.tile_help_homework),
                            icon = Icons.Rounded.School,
                            iconTint = TealPrimary,
                            isUsedThisYear = hwDone,
                            onClick = { handleInteraction(InteractionType.HELP_WITH_HOMEWORK, null) }
                        )
                    }
                )

                InteractionGridRow(
                    tile1 = {
                        InteractionTile(
                            title = stringResource(R.string.tile_allowance),
                            icon = Icons.Rounded.AttachMoney,
                            iconTint = GoldAccent,
                            costLabel = formatMoney(allowanceCost, playerCountryCode),
                            enabled = playerMoney >= allowanceCost,
                            onClick = { pendingAllowance.request(Unit) }
                        )
                    },
                    tile2 = {
                        InteractionTile(
                            title = stringResource(R.string.tile_discipline),
                            icon = AppIcons.Argue,
                            iconTint = CoralNegative,
                            onClick = { handleInteraction(InteractionType.DISCIPLINE, null) }
                        )
                    }
                )

                InteractionGridRow(
                    tile1 = {
                        val giftSmallCost = giftCost(GiftTier.SMALL)
                        InteractionTile(
                            title = stringResource(R.string.btn_gift_small),
                            icon = Icons.Rounded.CardGiftcard,
                            iconTint = AccentPink,
                            costLabel = formatMoney(giftSmallCost, playerCountryCode),
                            enabled = playerMoney >= giftSmallCost,
                            onClick = { pendingGiftTier.request(GiftTier.SMALL) }
                        )
                    },
                    tile2 = {
                        val giftMedCost = giftCost(GiftTier.MEDIUM)
                        InteractionTile(
                            title = stringResource(R.string.btn_gift_medium),
                            icon = Icons.Rounded.CardGiftcard,
                            iconTint = AccentPink,
                            costLabel = formatMoney(giftMedCost, playerCountryCode),
                            enabled = playerMoney >= giftMedCost,
                            onClick = { pendingGiftTier.request(GiftTier.MEDIUM) }
                        )
                    }
                )
            } else if (playerAge < 3) {
                // Toddler / Infant Actions
                SheetSectionTitle(stringResource(R.string.section_bond_actions))
                val cuddleDone = member.interactedTypesThisYear.contains(InteractionType.SPEND_TIME.name)
                val babbleDone = member.interactedTypesThisYear.contains(InteractionType.COMPLIMENT.name)
                val tantrumDone = member.interactedTypesThisYear.contains(InteractionType.ARGUE.name)

                InteractionGridRow(
                    tile1 = {
                        InteractionTile(
                            title = stringResource(R.string.tile_cuddle),
                            icon = Icons.Rounded.Favorite,
                            iconTint = AccentPink,
                            isUsedThisYear = cuddleDone,
                            onClick = { handleInteraction(InteractionType.SPEND_TIME, null) }
                        )
                    },
                    tile2 = {
                        InteractionTile(
                            title = stringResource(R.string.tile_babble),
                            icon = Icons.Rounded.SentimentSatisfied,
                            iconTint = GoldAccent,
                            isUsedThisYear = babbleDone,
                            onClick = { handleInteraction(InteractionType.COMPLIMENT, null) }
                        )
                    }
                )

                InteractionGridRow(
                    tile1 = {
                        InteractionTile(
                            title = stringResource(R.string.tile_tantrum),
                            icon = Icons.Rounded.SentimentVeryDissatisfied,
                            iconTint = CoralNegative,
                            isUsedThisYear = tantrumDone,
                            onClick = { handleInteraction(InteractionType.ARGUE, null) }
                        )
                    }
                )
            } else if (playerAge in 3..5) {
                // Young Child Actions
                SheetSectionTitle(stringResource(R.string.section_bond_actions))
                val playDone = member.interactedTypesThisYear.contains(InteractionType.SPEND_TIME.name)
                val compDone = member.interactedTypesThisYear.contains(InteractionType.COMPLIMENT.name)
                val tantrumDone = member.interactedTypesThisYear.contains(InteractionType.ARGUE.name)

                InteractionGridRow(
                    tile1 = {
                        InteractionTile(
                            title = stringResource(R.string.tile_play_together),
                            icon = Icons.Rounded.Toys,
                            iconTint = LifeGreen,
                            isUsedThisYear = playDone,
                            onClick = { handleInteraction(InteractionType.SPEND_TIME, null) }
                        )
                    },
                    tile2 = {
                        InteractionTile(
                            title = stringResource(R.string.tile_compliment),
                            icon = Icons.Rounded.Favorite,
                            iconTint = AccentPink,
                            isUsedThisYear = compDone,
                            onClick = { handleInteraction(InteractionType.COMPLIMENT, null) }
                        )
                    }
                )

                InteractionGridRow(
                    tile1 = {
                        InteractionTile(
                            title = stringResource(R.string.tile_tantrum),
                            icon = Icons.Rounded.SentimentVeryDissatisfied,
                            iconTint = CoralNegative,
                            isUsedThisYear = tantrumDone,
                            onClick = { handleInteraction(InteractionType.ARGUE, null) }
                        )
                    }
                )
            } else {
                // Ages 6+: General Bond & Social Actions
                SheetSectionTitle(stringResource(R.string.section_bond_actions))
                val spendTimeDone = member.interactedTypesThisYear.contains(InteractionType.SPEND_TIME.name)
                val compDone = member.interactedTypesThisYear.contains(InteractionType.COMPLIMENT.name)
                val adviceDone = member.interactedTypesThisYear.contains(InteractionType.ASK_FOR_ADVICE.name)
                val moneyDone = member.interactedTypesThisYear.contains(InteractionType.ASK_FOR_MONEY.name)
                val moneyUnlocked = member.relationshipLevel > 60

                InteractionGridRow(
                    tile1 = {
                        InteractionTile(
                            title = if (isChild) stringResource(R.string.tile_play_together) else stringResource(R.string.tile_spend_time),
                            icon = AppIcons.SpendTime,
                            iconTint = TealPrimary,
                            isUsedThisYear = spendTimeDone,
                            onClick = { handleInteraction(InteractionType.SPEND_TIME, null) }
                        )
                    },
                    tile2 = {
                        InteractionTile(
                            title = stringResource(R.string.tile_compliment),
                            icon = Icons.Rounded.Favorite,
                            iconTint = AccentPink,
                            isUsedThisYear = compDone,
                            onClick = { handleInteraction(InteractionType.COMPLIMENT, null) }
                        )
                    }
                )

                InteractionGridRow(
                    tile1 = {
                        InteractionTile(
                            title = stringResource(R.string.tile_ask_advice),
                            icon = Icons.Rounded.QuestionAnswer,
                            iconTint = GoldAccent,
                            isUsedThisYear = adviceDone,
                            onClick = { handleInteraction(InteractionType.ASK_FOR_ADVICE, null) }
                        )
                    },
                    tile2 = if (canAskForMoney) {
                        {
                            InteractionTile(
                                title = stringResource(R.string.tile_ask_money),
                                icon = Icons.Rounded.AttachMoney,
                                iconTint = LifeGreen,
                                isUsedThisYear = moneyDone,
                                enabled = moneyUnlocked && !moneyDone,
                                costLabel = if (!moneyUnlocked) stringResource(R.string.btn_ask_for_money_locked) else null,
                                onClick = { handleInteraction(InteractionType.ASK_FOR_MONEY, null) }
                            )
                        }
                    } else null
                )

                // Gifts & Splurges (Ages 16+)
                if (playerAge >= 16) {
                    SheetSectionTitle(stringResource(R.string.section_spend_actions))
                    val giftSmallCost = giftCost(GiftTier.SMALL)
                    val giftMedCost = giftCost(GiftTier.MEDIUM)
                    val giftLargeCost = giftCost(GiftTier.LARGE)

                    InteractionGridRow(
                        tile1 = {
                            InteractionTile(
                                title = stringResource(R.string.btn_gift_small),
                                icon = Icons.Rounded.CardGiftcard,
                                iconTint = AccentPink,
                                costLabel = formatMoney(giftSmallCost, playerCountryCode),
                                enabled = playerMoney >= giftSmallCost,
                                onClick = { pendingGiftTier.request(GiftTier.SMALL) }
                            )
                        },
                        tile2 = {
                            InteractionTile(
                                title = stringResource(R.string.btn_gift_medium),
                                icon = Icons.Rounded.CardGiftcard,
                                iconTint = AccentPink,
                                costLabel = formatMoney(giftMedCost, playerCountryCode),
                                enabled = playerMoney >= giftMedCost,
                                onClick = { pendingGiftTier.request(GiftTier.MEDIUM) }
                            )
                        }
                    )

                    InteractionGridRow(
                        tile1 = {
                            InteractionTile(
                                title = stringResource(R.string.btn_gift_large),
                                icon = Icons.Rounded.CardGiftcard,
                                iconTint = AccentPink,
                                costLabel = formatMoney(giftLargeCost, playerCountryCode),
                                enabled = playerMoney >= giftLargeCost,
                                onClick = { pendingGiftTier.request(GiftTier.LARGE) }
                            )
                        },
                        tile2 = {
                            InteractionTile(
                                title = stringResource(R.string.tile_vacation),
                                icon = Icons.Rounded.Flight,
                                iconTint = TealPrimary,
                                costLabel = formatMoney(travelCost, playerCountryCode),
                                enabled = travelEnabled && playerMoney >= travelCost,
                                onClick = { pendingTravel.request(Unit) }
                            )
                        }
                    )
                }

                // Sibling Specials
                if (member.relation == RelationType.SIBLING && playerAge >= 3) {
                    SheetSectionTitle(stringResource(R.string.section_fun_risk_actions))
                    val teamUpDone = member.interactedTypesThisYear.contains(InteractionType.SIBLING_TEAM_UP.name)
                    val borrowDone = member.interactedTypesThisYear.contains(InteractionType.SIBLING_BORROW_ITEM.name)
                    val competeDone = member.interactedTypesThisYear.contains(InteractionType.SIBLING_COMPETE.name)

                    InteractionGridRow(
                        tile1 = {
                            InteractionTile(
                                title = stringResource(R.string.tile_sibling_team_up),
                                icon = Icons.Rounded.Handshake,
                                iconTint = LifeGreen,
                                isUsedThisYear = teamUpDone,
                                onClick = { handleInteraction(InteractionType.SIBLING_TEAM_UP, null) }
                            )
                        },
                        tile2 = if (playerAge >= 5) {
                            {
                                InteractionTile(
                                    title = stringResource(R.string.tile_sibling_borrow),
                                    icon = Icons.Rounded.CardGiftcard,
                                    iconTint = GoldAccent,
                                    isUsedThisYear = borrowDone,
                                    onClick = { handleInteraction(InteractionType.SIBLING_BORROW_ITEM, null) }
                                )
                            }
                        } else null
                    )

                    if (playerAge >= 5) {
                        InteractionGridRow(
                            tile1 = {
                                InteractionTile(
                                    title = stringResource(R.string.tile_sibling_compete),
                                    icon = Icons.Rounded.SportsEsports,
                                    iconTint = CoralNegative,
                                    isUsedThisYear = competeDone,
                                    onClick = { handleInteraction(InteractionType.SIBLING_COMPETE, null) }
                                )
                            }
                        )
                    }
                }

                // Fun, Mischief & Matchmaking
                val prankDone = member.interactedTypesThisYear.contains(InteractionType.PRANK.name)
                val dateDone = member.interactedTypesThisYear.contains(InteractionType.SET_UP_ON_DATE.name)

                InteractionGridRow(
                    tile1 = {
                        InteractionTile(
                            title = stringResource(R.string.tile_prank),
                            icon = AppIcons.Mischief,
                            iconTint = GoldAccent,
                            isUsedThisYear = prankDone,
                            onClick = { handleInteraction(InteractionType.PRANK, null) }
                        )
                    },
                    tile2 = if (canSetUpDate && playerAge >= 16) {
                        {
                            InteractionTile(
                                title = stringResource(R.string.tile_set_up_date),
                                icon = Icons.Rounded.FavoriteBorder,
                                iconTint = AccentPink,
                                isUsedThisYear = dateDone,
                                onClick = { handleInteraction(InteractionType.SET_UP_ON_DATE, null) }
                            )
                        }
                    } else null
                )

                // Conflict: Argue & Insult
                val argueDone = member.interactedTypesThisYear.contains(InteractionType.ARGUE.name)
                val insultDone = member.interactedTypesThisYear.contains(InteractionType.INSULT.name)

                InteractionGridRow(
                    tile1 = {
                        InteractionTile(
                            title = if (isChild) stringResource(R.string.btn_scold) else stringResource(R.string.tile_argue),
                            icon = AppIcons.Argue,
                            iconTint = CoralNegative,
                            isUsedThisYear = argueDone,
                            onClick = { handleInteraction(InteractionType.ARGUE, null) }
                        )
                    },
                    tile2 = {
                        InteractionTile(
                            title = stringResource(R.string.tile_insult),
                            icon = AppIcons.Insult,
                            iconTint = CoralNegative,
                            isUsedThisYear = insultDone,
                            onClick = { handleInteraction(InteractionType.INSULT, null) }
                        )
                    }
                )
            }

            // Rival Actions
            if (isEnemy || member.socialBond == com.maisha.game.data.model.SocialBondType.RECURRING_RIVAL) {
                SheetSectionTitle(stringResource(R.string.section_rival_actions))
                InteractionGridRow(
                    tile1 = {
                        InteractionTile(
                            title = stringResource(R.string.tile_reconcile),
                            icon = Icons.Rounded.Handshake,
                            iconTint = LifeGreen,
                            onClick = { onReconcileRivalry(member.id) }
                        )
                    }
                )
            }

            // Spouse & Partner Romance Hub
            if (isSpouse) {
                SheetSectionTitle(stringResource(R.string.section_life_together))

                if (!member.isMarried) {
                    if (member.stage == RelationshipStage.CASUAL) {
                        InteractionGridRow(
                            tile1 = {
                                InteractionTile(
                                    title = stringResource(R.string.tile_make_exclusive),
                                    icon = Icons.Rounded.Favorite,
                                    iconTint = AccentPink,
                                    enabled = member.relationshipLevel >= 40,
                                    onClick = onMakeExclusive
                                )
                            }
                        )
                    }

                    val proposeUnlocked = member.relationshipLevel >= RelationshipEngine.PROPOSAL_THRESHOLD
                    InteractionGridRow(
                        tile1 = {
                            InteractionTile(
                                title = stringResource(R.string.tile_propose),
                                icon = Icons.Rounded.Favorite,
                                iconTint = AccentPink,
                                enabled = proposeUnlocked,
                                costLabel = if (!proposeUnlocked) {
                                    stringResource(R.string.btn_propose_locked, RelationshipEngine.PROPOSAL_THRESHOLD)
                                } else null,
                                onClick = onPropose
                            )
                        },
                        tile2 = {
                            InteractionTile(
                                title = stringResource(R.string.tile_propose_prenup),
                                icon = Icons.Rounded.Handshake,
                                iconTint = GoldAccent,
                                enabled = proposeUnlocked,
                                costLabel = if (!proposeUnlocked) {
                                    stringResource(R.string.btn_propose_locked, RelationshipEngine.PROPOSAL_THRESHOLD)
                                } else null,
                                onClick = onProposeWithPrenup
                            )
                        }
                    )
                }

                if (isMarried && member.isMarried) {
                    InteractionGridRow(
                        tile1 = {
                            InteractionTile(
                                title = stringResource(R.string.tile_date_night),
                                icon = Icons.Rounded.Favorite,
                                iconTint = AccentPink,
                                costLabel = formatMoney(resolvedDateNightCost, playerCountryCode),
                                enabled = playerMoney >= resolvedDateNightCost,
                                onClick = { pendingDateNight.request(Unit) }
                            )
                        },
                        tile2 = {
                            InteractionTile(
                                title = stringResource(R.string.tile_have_baby),
                                icon = Icons.Rounded.ChildCare,
                                iconTint = LifeGreen,
                                costLabel = formatMoney(resolvedChildCost, playerCountryCode),
                                enabled = playerMoney >= resolvedChildCost,
                                onClick = { pendingHaveChild.request(Unit) }
                            )
                        }
                    )
                }

                // Break Up or Divorce
                InteractionGridRow(
                    tile1 = {
                        InteractionTile(
                            title = if (member.isMarried) stringResource(R.string.tile_divorce) else stringResource(R.string.tile_break_up),
                            icon = AppIcons.Argue,
                            iconTint = CoralNegative,
                            onClick = {
                                if (member.isMarried) pendingDivorce.request(Unit)
                                else pendingBreakUp.request(Unit)
                            }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InteractionGridRow(
    tile1: @Composable () -> Unit,
    tile2: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            tile1()
        }
        if (tile2 != null) {
            Box(modifier = Modifier.weight(1f)) {
                tile2()
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun InteractionTile(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    costLabel: String? = null,
    isUsedThisYear: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled && !isUsedThisYear,
        shape = RoundedCornerShape(14.dp),
        color = if (isUsedThisYear) SurfaceMuted.copy(alpha = 0.5f) else PaperWhite,
        border = BorderStroke(1.dp, if (isUsedThisYear) Hairline.copy(alpha = 0.4f) else Hairline),
        shadowElevation = if (isUsedThisYear || !enabled) 0.dp else 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isUsedThisYear) InkTertiary.copy(alpha = 0.12f)
                        else iconTint.copy(alpha = 0.14f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isUsedThisYear) InkTertiary else iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUsedThisYear) InkSecondary else InkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isUsedThisYear) {
                    Text(
                        text = stringResource(R.string.tile_used_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = InkTertiary,
                        fontWeight = FontWeight.Medium
                    )
                } else if (!costLabel.isNullOrBlank()) {
                    Text(
                        text = costLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (enabled) TealPrimary else CoralNegative,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = stringResource(R.string.tile_cost_free),
                        style = MaterialTheme.typography.labelSmall,
                        color = LifeGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
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
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
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
