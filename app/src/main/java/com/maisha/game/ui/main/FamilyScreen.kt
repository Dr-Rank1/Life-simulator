// app/src/main/java/com/maisha/game/ui/main/FamilyScreen.kt
package com.maisha.game.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.maisha.game.R
import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.local.OnboardingTips
import com.maisha.game.data.model.BenefactorFavorType
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.CommunityStandingTier
import com.maisha.game.data.model.FamilyAlliance
import com.maisha.game.data.model.FamilyFeud
import com.maisha.game.data.model.FamilyHeritageTag
import com.maisha.game.data.model.FeudAction
import com.maisha.game.data.model.Person
import com.maisha.game.data.model.Pet
import com.maisha.game.data.model.SocialEcosystemState
import com.maisha.game.data.model.PetSpecies
import com.maisha.game.data.model.RelationType
import com.maisha.game.domain.GiftTier
import com.maisha.game.domain.InteractionType
import com.maisha.game.domain.PetCareAction
import com.maisha.game.domain.RelationshipEngine
import com.maisha.game.domain.YearQuestKind
import com.maisha.game.domain.hasSpouse
import com.maisha.game.domain.isMarried
import com.maisha.game.domain.isPlatonicAlly
import com.maisha.game.ui.avatar.ExpressionResolver
import com.maisha.game.ui.components.CategoryFilterChipRow
import com.maisha.game.ui.components.ConfirmActionDialog
import com.maisha.game.ui.components.ConfirmSeverity
import com.maisha.game.ui.components.ConfirmableActionHost
import com.maisha.game.ui.components.CountryFlag
import com.maisha.game.ui.components.countryDisplayName
import com.maisha.game.ui.components.DismissibleTipCard
import com.maisha.game.ui.components.EmptyStateCard
import com.maisha.game.ui.components.InsetCardContainer
import com.maisha.game.ui.components.InsetDivider
import com.maisha.game.ui.components.PersonAvatar
import com.maisha.game.ui.components.PersonCard
import com.maisha.game.ui.components.PersonDetailSheet
import com.maisha.game.ui.components.MaishaIcon
import com.maisha.game.ui.components.PetCard
import com.maisha.game.ui.components.PetDetailSheet
import com.maisha.game.ui.components.StatBar
import com.maisha.game.ui.components.StatType
import com.maisha.game.ui.components.TabPageHero
import com.maisha.game.ui.components.rememberConfirmableAction
import com.maisha.game.ui.illustrations.EmptyStateIllustration
import com.maisha.game.ui.theme.AccentPink
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.Hairline
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.CreamBg
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.MaishaSpacing
import com.maisha.game.ui.theme.NavyDeep
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.TealPrimary
import com.maisha.game.util.formatMoney

private enum class FamilyCategory { ALL, FAMILY, FRIENDS, PETS }

private enum class FamilyListContentType {
    SectionHeader,
    PersonCard,
    PetCard
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(
    character: Character,
    uiState: LifeUiState,
    snackbarHostState: SnackbarHostState,
    onMemberClick: (Person) -> Unit,
    onMemberDismiss: () -> Unit,
    onPetClick: (Pet) -> Unit,
    onPetDismiss: () -> Unit,
    onPetCare: (String, PetCareAction) -> Unit,
    onInteraction: (String, InteractionType, GiftTier?) -> Unit,
    onMessageDismissed: () -> Unit,
    onFindDate: () -> Unit,
    onSeekFriendship: () -> Unit,
    onDismissDatingProspects: () -> Unit,
    onStartDating: (Person) -> Unit,
    onMakeExclusive: (String) -> Unit = {},
    onPropose: (String) -> Unit,
    onProposeWithPrenup: (String) -> Unit = onPropose,
    onBreakUp: (String) -> Unit,
    onHaveChild: () -> Unit,
    onRelationshipMessageDismissed: () -> Unit,
    onDismissFamilyDatingTip: () -> Unit,
    onDismissFamilyDetailTip: () -> Unit,
    onThrowParty: (Int) -> Unit,
    onResolveFeudAction: (String, FeudAction) -> Unit = { _, _ -> },
    onCallInBenefactorFavor: (String, BenefactorFavorType) -> Unit = { _, _ -> },
    onReconcileRivalry: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val partyConfirm = rememberConfirmableAction<Int>()
    var showPartyTiers by remember { mutableStateOf(false) }
    var activeFeudToManage by remember { mutableStateOf<FamilyFeud?>(null) }
    var activeAllianceToManage by remember { mutableStateOf<FamilyAlliance?>(null) }
    val partyModest = EconomyScaler.scaleAmount(
        RelationshipEngine.PARTY_BUDGET_MIN_KENYA,
        character.countryCode
    )
    val partyNice = EconomyScaler.scaleAmount(
        RelationshipEngine.partyBudgetNiceKenya(),
        character.countryCode
    )
    val partyLavish = EconomyScaler.scaleAmount(
        RelationshipEngine.PARTY_BUDGET_MAX_KENYA,
        character.countryCode
    )
    val dateFee = EconomyScaler.scaleAmount(
        RelationshipEngine.FIRST_DATE_COST_KENYA,
        character.countryCode
    )
    val seekCost = EconomyScaler.scaleAmount(
        RelationshipEngine.SEEK_FRIEND_COST_KENYA,
        character.countryCode
    )
    val feedCost = EconomyScaler.scaleAmount(
        RelationshipEngine.PET_FEED_COST_KENYA,
        character.countryCode
    )
    val vetCost = EconomyScaler.scaleAmount(
        RelationshipEngine.PET_VET_COST_KENYA,
        character.countryCode
    )
    val childCost = EconomyScaler.scaleAmount(
        RelationshipEngine.CHILD_HOSPITAL_COST_KENYA,
        character.countryCode
    )
    val divorceCost = EconomyScaler.scaleAmount(
        RelationshipEngine.DIVORCE_SETTLEMENT_KENYA,
        character.countryCode
    )
    val dateNightCost = EconomyScaler.scaleRelationshipCost(
        RelationshipEngine.DATE_NIGHT_COST_KENYA,
        character.countryCode,
        character.age
    )
    val friendCount = character.family.count { it.alive && it.isPlatonicAlly() }
    val canHostParty = character.alive &&
        character.age >= 16 &&
        character.family.any {
            it.alive && (it.isPlatonicAlly() || it.relation == RelationType.SIBLING)
        }
    val canMeetPeople = character.alive &&
        character.age >= 6 &&
        !character.lifestyle.socializedThisYear &&
        friendCount < RelationshipEngine.MAX_FRIENDS &&
        !character.criminalRecord.currentlyIncarcerated &&
        !character.criminalRecord.awaitingTrial

    LaunchedEffect(uiState.familyInteractionMessage) {
        uiState.familyInteractionMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onMessageDismissed()
        }
    }

    LaunchedEffect(uiState.relationshipMessage) {
        uiState.relationshipMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onRelationshipMessageDismissed()
        }
    }

    val partner = character.family.filter { it.relation == RelationType.SPOUSE }
    val children = character.family.filter { it.relation == RelationType.CHILD }
    val parents = character.family.filter {
        it.relation == RelationType.MOTHER || it.relation == RelationType.FATHER
    }
    val siblings = character.family.filter { it.relation == RelationType.SIBLING }
    val socialCircle = character.family.filter {
        it.relation == RelationType.FRIEND ||
            it.relation == RelationType.BEST_FRIEND ||
            it.relation == RelationType.ENEMY
    }
    val others = character.family.filter {
        it.relation != RelationType.SPOUSE &&
            it.relation != RelationType.CHILD &&
            it.relation != RelationType.MOTHER &&
            it.relation != RelationType.FATHER &&
            it.relation != RelationType.SIBLING &&
            it.relation != RelationType.FRIEND &&
            it.relation != RelationType.BEST_FRIEND &&
            it.relation != RelationType.ENEMY
    }

    ConfirmableActionHost(
        state = partyConfirm,
        onConfirmed = { budget -> onThrowParty(budget) }
    ) { budget, onConfirm, onDismiss ->
        val estimatedBoost = run {
            val minB = partyModest.coerceAtLeast(1)
            val maxB = partyLavish.coerceAtLeast(minB + 1)
            val t = ((budget - minB).toFloat() / (maxB - minB)).coerceIn(0f, 1f)
            (RelationshipEngine.PARTY_BOOST_MIN +
                (RelationshipEngine.PARTY_BOOST_MAX - RelationshipEngine.PARTY_BOOST_MIN) * t)
                .toInt()
                .coerceIn(RelationshipEngine.PARTY_BOOST_MIN, RelationshipEngine.PARTY_BOOST_MAX)
        }
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_throw_party_title),
            description = stringResource(
                R.string.dialog_throw_party_tier_body,
                formatMoney(budget, character.countryCode),
                estimatedBoost
            ),
            confirmLabel = stringResource(R.string.btn_host_party),
            severity = ConfirmSeverity.NEUTRAL,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    val partnerStatus = when {
        character.isMarried() -> stringResource(R.string.social_status_married)
        character.hasSpouse() -> stringResource(R.string.social_status_dating)
        else -> stringResource(R.string.social_status_single)
    }
    var selectedCategory by rememberSaveable { mutableIntStateOf(0) }
    val category = FamilyCategory.entries.getOrElse(selectedCategory) { FamilyCategory.ALL }
    fun show(cat: FamilyCategory): Boolean =
        category == FamilyCategory.ALL || category == cat
    val showFamily = show(FamilyCategory.FAMILY)
    val showFriends = show(FamilyCategory.FRIENDS)
    val showPets = show(FamilyCategory.PETS)
    val chipLabels = listOf(
        stringResource(R.string.chip_family_all),
        stringResource(R.string.chip_family_family),
        stringResource(R.string.chip_family_friends),
        stringResource(R.string.chip_family_pets)
    )
    val hasBondFamilyQuest = uiState.yearQuests.any { it.kind == YearQuestKind.BOND_FAMILY }
    val listEmpty = when (category) {
        FamilyCategory.ALL -> character.family.isEmpty() && character.pets.isEmpty()
        FamilyCategory.FAMILY ->
            parents.isEmpty() && siblings.isEmpty() && partner.isEmpty() &&
                children.isEmpty() && others.isEmpty()
        FamilyCategory.FRIENDS -> socialCircle.isEmpty()
        FamilyCategory.PETS -> character.pets.isEmpty()
    }

    val hasAnyPeopleOrPets = character.family.isNotEmpty() || character.pets.isNotEmpty()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamBg)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            if (hasBondFamilyQuest) {
                Text(
                    text = stringResource(R.string.family_quest_hint),
                    style = MaterialTheme.typography.labelMedium,
                    color = LifeGreen,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

            if (character.age >= 18 && !character.hasSpouse()) {
                Button(
                    onClick = onFindDate,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LifeGreen,
                        contentColor = Color.White
                    )
                ) {
                    MaishaIcon(icon = AppIcons.Family, contentDescription = null, size = 22.dp)
                    Text(
                        text = "  ${stringResource(R.string.btn_find_date)}",
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (canMeetPeople) {
                OutlinedButton(
                    onClick = onSeekFriendship,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.btn_meet_people,
                            formatMoney(seekCost, character.countryCode)
                        ),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!hasAnyPeopleOrPets) {
                EmptyStateCard(
                    illustration = EmptyStateIllustration.FAMILY,
                    title = stringResource(R.string.empty_family_title),
                    message = stringResource(R.string.empty_family),
                    actionLabel = if (character.age >= 18 && !character.hasSpouse()) {
                        stringResource(R.string.btn_find_date)
                    } else {
                        null
                    },
                    onAction = if (character.age >= 18 && !character.hasSpouse()) {
                        onFindDate
                    } else {
                        null
                    },
                    modifier = Modifier.padding(top = MaishaSpacing.sm)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item(key = "section_community_standing") {
                        CommunityStandingCard(
                            state = character.socialEcosystem,
                            countryCode = character.countryCode,
                            onFeudClick = { activeFeudToManage = it },
                            onAllianceClick = { activeAllianceToManage = it }
                        )
                    }
                    if (partner.isNotEmpty()) {
                        item(key = "section_partner") {
                            InsetCardContainer(title = stringResource(R.string.section_partner)) {
                                partner.forEachIndexed { idx, member ->
                                    if (idx > 0) InsetDivider(insetStart = false)
                                    FamilyPersonCard(member, character.countryCode, insideContainer = true) {
                                        onMemberClick(member)
                                    }
                                }
                            }
                        }
                    }
                    if (children.isNotEmpty()) {
                        item(key = "section_children") {
                            InsetCardContainer(title = stringResource(R.string.section_children)) {
                                children.forEachIndexed { idx, member ->
                                    if (idx > 0) InsetDivider(insetStart = false)
                                    FamilyPersonCard(member, character.countryCode, insideContainer = true) {
                                        onMemberClick(member)
                                    }
                                }
                            }
                        }
                    }
                    if (parents.isNotEmpty()) {
                        item(key = "section_parents") {
                            InsetCardContainer(title = stringResource(R.string.section_parents)) {
                                parents.forEachIndexed { idx, member ->
                                    if (idx > 0) InsetDivider(insetStart = false)
                                    FamilyPersonCard(member, character.countryCode, insideContainer = true) {
                                        onMemberClick(member)
                                    }
                                }
                            }
                        }
                    }
                    if (siblings.isNotEmpty()) {
                        item(key = "section_siblings") {
                            InsetCardContainer(title = stringResource(R.string.section_siblings)) {
                                siblings.forEachIndexed { idx, member ->
                                    if (idx > 0) InsetDivider(insetStart = false)
                                    FamilyPersonCard(member, character.countryCode, insideContainer = true) {
                                        onMemberClick(member)
                                    }
                                }
                            }
                        }
                    }
                    if (socialCircle.isNotEmpty()) {
                        item(key = "section_friends") {
                            InsetCardContainer(title = stringResource(R.string.section_friends_rivals)) {
                                socialCircle.forEachIndexed { idx, member ->
                                    if (idx > 0) InsetDivider(insetStart = false)
                                    FamilyPersonCard(member, character.countryCode, insideContainer = true) {
                                        onMemberClick(member)
                                    }
                                }
                            }
                        }
                    }
                    if (others.isNotEmpty()) {
                        item(key = "section_other") {
                            InsetCardContainer(title = stringResource(R.string.section_other)) {
                                others.forEachIndexed { idx, member ->
                                    if (idx > 0) InsetDivider(insetStart = false)
                                    FamilyPersonCard(member, character.countryCode, insideContainer = true) {
                                        onMemberClick(member)
                                    }
                                }
                            }
                        }
                    }
                    if (character.pets.isNotEmpty()) {
                        item(key = "section_pets") {
                            InsetCardContainer(title = stringResource(R.string.section_pets)) {
                                character.pets.forEachIndexed { idx, pet ->
                                    if (idx > 0) InsetDivider(insetStart = false)
                                    PetCard(
                                        pet = pet,
                                        speciesLabel = petSpeciesLabel(pet.species),
                                        onClick = { onPetClick(pet) }
                                    )
                                }
                            }
                        }
                    }
                    item(key = "fab_spacer") {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        if (canHostParty) {
            FloatingActionButton(
                onClick = { showPartyTiers = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(MaishaSpacing.md),
                containerColor = LifeGreen,
                contentColor = Color.White
            ) {
                MaishaIcon(
                    icon = AppIcons.Family,
                    contentDescription = stringResource(R.string.btn_host_party),
                    size = 24.dp
                )
            }
        }
    }

    if (showPartyTiers) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPartyTiers = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.sheet_party_tiers_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                PartyTierButton(
                    label = stringResource(R.string.party_tier_modest),
                    costLabel = formatMoney(partyModest, character.countryCode),
                    enabled = character.stats.money >= partyModest
                ) {
                    showPartyTiers = false
                    partyConfirm.request(partyModest)
                }
                PartyTierButton(
                    label = stringResource(R.string.party_tier_nice),
                    costLabel = formatMoney(partyNice, character.countryCode),
                    enabled = character.stats.money >= partyNice
                ) {
                    showPartyTiers = false
                    partyConfirm.request(partyNice)
                }
                PartyTierButton(
                    label = stringResource(R.string.party_tier_lavish),
                    costLabel = formatMoney(partyLavish, character.countryCode),
                    enabled = character.stats.money >= partyLavish
                ) {
                    showPartyTiers = false
                    partyConfirm.request(partyLavish)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (uiState.showDatingProspects) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val pendingDate = rememberConfirmableAction<Person>()
        ConfirmableActionHost(
            state = pendingDate,
            onConfirmed = { prospect -> onStartDating(prospect) }
        ) { prospect, onConfirm, onDismiss ->
            ConfirmActionDialog(
                title = stringResource(R.string.btn_start_dating),
                description = stringResource(
                    R.string.confirm_first_date_body,
                    prospect.name,
                    formatMoney(dateFee, character.countryCode)
                ),
                confirmLabel = stringResource(R.string.btn_start_dating),
                severity = ConfirmSeverity.NEUTRAL,
                onConfirm = onConfirm,
                onDismiss = onDismiss
            )
        }
        ModalBottomSheet(
            onDismissRequest = onDismissDatingProspects,
            sheetState = sheetState
        ) {
            com.maisha.game.ui.components.DatingMatchSheet(
                prospects = uiState.datingProspects,
                playerCountryCode = character.countryCode,
                dateFeeLabel = formatMoney(dateFee, character.countryCode),
                onStartDating = { pendingDate.request(it) },
                onDismiss = onDismissDatingProspects
            )
        }
    }

    uiState.selectedFamilyMember?.let { member ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onMemberDismiss,
            sheetState = sheetState
        ) {
            PersonDetailSheet(
                member = member,
                playerCountryCode = character.countryCode,
                playerMoney = character.stats.money,
                playerAge = character.age,
                isIncarcerated = character.criminalRecord.currentlyIncarcerated,
                isMarried = character.isMarried(),
                relationLabel = relationLabel(member),
                dateNightCost = dateNightCost,
                childHospitalCost = childCost,
                divorceSettlementCost = divorceCost,
                onInteraction = { type, giftTier -> onInteraction(member.id, type, giftTier) },
                onMakeExclusive = { onMakeExclusive(member.id) },
                onPropose = { onPropose(member.id) },
                onProposeWithPrenup = { onProposeWithPrenup(member.id) },
                onBreakUp = { onBreakUp(member.id) },
                onHaveChild = onHaveChild,
                onReconcileRivalry = onReconcileRivalry
            )
        }
    }

    activeFeudToManage?.let { feud ->
        FeudManageDialog(
            feud = feud,
            countryCode = character.countryCode,
            onAction = { action ->
                activeFeudToManage = null
                onResolveFeudAction(feud.id, action)
            },
            onDismiss = { activeFeudToManage = null }
        )
    }

    activeAllianceToManage?.let { ally ->
        AllianceManageDialog(
            alliance = ally,
            onFavor = { favor ->
                activeAllianceToManage = null
                onCallInBenefactorFavor(ally.id, favor)
            },
            onDismiss = { activeAllianceToManage = null }
        )
    }

    uiState.selectedPet?.let { pet ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onPetDismiss,
            sheetState = sheetState
        ) {
            PetDetailSheet(
                pet = pet,
                speciesLabel = petSpeciesLabel(pet.species),
                playerCountryCode = character.countryCode,
                playerMoney = character.stats.money,
                feedCost = feedCost,
                vetCost = vetCost,
                onCare = { action -> onPetCare(pet.id, action) }
            )
        }
    }
}

@Composable
private fun PartyTierButton(
    label: String,
    costLabel: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GoldAccent.copy(alpha = 0.85f),
            contentColor = InkPrimary
        )
    ) {
        Text("$label · $costLabel", fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FamilyPersonCard(
    member: Person,
    playerCountryCode: String,
    insideContainer: Boolean = false,
    onClick: () -> Unit
) {
    key(member.id, member.relation, member.isMarried) {
        PersonCard(
            person = member,
            relationLabel = relationLabel(member),
            playerCountryCode = playerCountryCode,
            insideContainer = insideContainer,
            onClick = onClick
        )
    }
}

@Composable
private fun petSpeciesLabel(species: PetSpecies): String = when (species) {
    PetSpecies.DOG -> stringResource(R.string.pet_species_dog)
    PetSpecies.CAT -> stringResource(R.string.pet_species_cat)
    PetSpecies.BIRD -> stringResource(R.string.pet_species_bird)
    PetSpecies.FISH -> stringResource(R.string.pet_species_fish)
    PetSpecies.EXOTIC -> stringResource(R.string.pet_species_exotic)
}

@Composable
private fun FamilySectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = InkTertiary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun DatingProspectsSheet(
    prospects: List<Person>,
    playerCountryCode: String,
    dateFeeLabel: String,
    onStartDating: (Person) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.sheet_dating_prospects_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.sheet_dating_prospects_subtitle_fee, dateFeeLabel),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        prospects.forEach { prospect ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PersonAvatar(
                            avatarConfig = prospect.avatarConfig,
                            age = prospect.age,
                            expression = ExpressionResolver.resolvePersonExpression(prospect),
                            seed = prospect.id
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CountryFlag(countryCode = prospect.countryCode, size = 16.dp)
                                Text(
                                    text = prospect.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = stringResource(
                                    R.string.format_prospect_from_country,
                                    countryDisplayName(prospect.countryCode)
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(
                                    R.string.format_prospect_chemistry,
                                    stringResource(R.string.format_age, prospect.age),
                                    prospect.relationshipLevel
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    StatBar(
                        type = StatType.LOOKS,
                        value = prospect.stats.looks,
                        label = stringResource(R.string.stat_looks),
                        showIcon = false
                    )
                    StatBar(
                        type = StatType.SMARTS,
                        value = prospect.stats.smarts,
                        label = stringResource(R.string.stat_smarts),
                        showIcon = false
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onStartDating(prospect) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentPink,
                            contentColor = InkPrimary
                        )
                    ) {
                        Text(
                            stringResource(R.string.btn_start_dating_fee, dateFeeLabel)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun rememberFamilySnackbarHostState(): SnackbarHostState = remember { SnackbarHostState() }

@Composable
private fun relationLabel(member: Person): String = when (member.relation) {
    RelationType.MOTHER -> stringResource(R.string.relation_mother)
    RelationType.FATHER -> stringResource(R.string.relation_father)
    RelationType.SIBLING -> stringResource(R.string.relation_sibling)
    RelationType.SPOUSE -> when {
        member.isMarried -> stringResource(R.string.relation_spouse)
        else -> stringResource(R.string.relation_partner_dating)
    }
    RelationType.CHILD -> stringResource(R.string.relation_child)
    RelationType.FRIEND -> stringResource(R.string.relation_friend)
    RelationType.BEST_FRIEND -> stringResource(R.string.relation_best_friend)
    RelationType.ENEMY -> stringResource(R.string.relation_enemy)
}

@Composable
fun CommunityStandingCard(
    state: SocialEcosystemState,
    countryCode: String,
    onFeudClick: (FamilyFeud) -> Unit,
    onAllianceClick: (FamilyAlliance) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.section_community_standing),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = TealPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = heritageLabel(state.familyHeritage),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TealPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = standingTierLabel(state.currentStandingTier),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = when (state.currentStandingTier) {
                        CommunityStandingTier.PILLAR_OF_SOCIETY,
                        CommunityStandingTier.WELL_RESPECTED -> LifeGreen
                        CommunityStandingTier.DISTRUSTED,
                        CommunityStandingTier.NOTORIOUS_PARIAH -> CoralNegative
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = stringResource(R.string.label_community_reputation, state.communityStandingScore),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LinearProgressIndicator(
                progress = { state.communityStandingScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when (state.currentStandingTier) {
                    CommunityStandingTier.PILLAR_OF_SOCIETY,
                    CommunityStandingTier.WELL_RESPECTED -> LifeGreen
                    CommunityStandingTier.DISTRUSTED,
                    CommunityStandingTier.NOTORIOUS_PARIAH -> CoralNegative
                    else -> TealPrimary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (state.activeFeuds.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.label_feuds_count, state.activeFeuds.size),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CoralNegative
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.activeFeuds.forEach { feud ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CoralNegative.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, CoralNegative.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable { onFeudClick(feud) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = feud.rivalFamilyName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CoralNegative
                                )
                                Text(
                                    text = "${feud.intensity}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (state.activeAlliances.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.label_alliances_count, state.activeAlliances.size),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = LifeGreen
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.activeAlliances.forEach { ally ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LifeGreen.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, LifeGreen.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable { onAllianceClick(ally) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ally.allyFamilyName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LifeGreen
                                )
                                Text(
                                    text = "${ally.trustLevel}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun standingTierLabel(tier: CommunityStandingTier): String = when (tier) {
    CommunityStandingTier.PILLAR_OF_SOCIETY -> stringResource(R.string.standing_pillar_of_society)
    CommunityStandingTier.WELL_RESPECTED -> stringResource(R.string.standing_well_respected)
    CommunityStandingTier.RESPECTABLE -> stringResource(R.string.standing_respectable)
    CommunityStandingTier.DISTRUSTED -> stringResource(R.string.standing_distrusted)
    CommunityStandingTier.NOTORIOUS_PARIAH -> stringResource(R.string.standing_notorious_pariah)
}

@Composable
private fun heritageLabel(tag: FamilyHeritageTag): String = when (tag) {
    FamilyHeritageTag.HUMBLE_ROOTS -> stringResource(R.string.heritage_humble_roots)
    FamilyHeritageTag.HONORED_PHILANTHROPISTS -> stringResource(R.string.heritage_honored_philanthropists)
    FamilyHeritageTag.PROSPEROUS_DYNASTY -> stringResource(R.string.heritage_prosperous_dynasty)
    FamilyHeritageTag.SCANDAL_TAINTED -> stringResource(R.string.heritage_scandal_tainted)
    FamilyHeritageTag.RUTHLESS_MOGULS -> stringResource(R.string.heritage_ruthless_moguls)
}

@Composable
fun FeudManageDialog(
    feud: FamilyFeud,
    countryCode: String,
    onAction: (FeudAction) -> Unit,
    onDismiss: () -> Unit
) {
    val oliveBranchCost = EconomyScaler.scaleAmount(1_500, countryCode)
    val legalFee = EconomyScaler.scaleAmount(3_000, countryCode)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${feud.rivalFamilyName} Family Feud",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CoralNegative
                )

                Text(
                    text = stringResource(R.string.reactive_feud_desc, feud.rivalFamilyName, feud.intensity),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (feud.originReason.isNotBlank()) {
                    Text(
                        text = feud.originReason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { onAction(FeudAction.OFFER_OLIVE_BRANCH) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LifeGreen)
                ) {
                    Text(stringResource(R.string.btn_feud_olive_branch, formatMoney(oliveBranchCost, countryCode)))
                }

                OutlinedButton(
                    onClick = { onAction(FeudAction.ESCALATE_FEUD) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralNegative),
                    border = BorderStroke(1.dp, CoralNegative)
                ) {
                    Text(stringResource(R.string.btn_feud_escalate))
                }

                OutlinedButton(
                    onClick = { onAction(FeudAction.LEGAL_INJUNCTION) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.btn_feud_legal, formatMoney(legalFee, countryCode)))
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        }
    }
}

@Composable
fun AllianceManageDialog(
    alliance: FamilyAlliance,
    onFavor: (BenefactorFavorType) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${alliance.allyFamilyName} Family Alliance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LifeGreen
                )

                Text(
                    text = stringResource(R.string.reactive_ally_desc, alliance.allyFamilyName, alliance.trustLevel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (alliance.pactReason.isNotBlank()) {
                    Text(
                        text = alliance.pactReason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { onFavor(BenefactorFavorType.CAREER_RECOMMENDATION) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text(stringResource(R.string.btn_benefactor_career))
                }

                Button(
                    onClick = { onFavor(BenefactorFavorType.FINANCIAL_BACKING) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text(stringResource(R.string.btn_benefactor_financial))
                }

                Button(
                    onClick = { onFavor(BenefactorFavorType.COMMUNITY_DEFENSE) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text(stringResource(R.string.btn_benefactor_defense))
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        }
    }
}
