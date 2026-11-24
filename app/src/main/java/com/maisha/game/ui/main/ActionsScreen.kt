package com.maisha.game.ui.main

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.maisha.game.R
import com.maisha.game.data.CountryCatalog
import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.JobPool
import com.maisha.game.data.PetCatalog
import com.maisha.game.data.model.BucketGoalKind
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.CrimeTarget
import com.maisha.game.data.model.CrimeType
import com.maisha.game.data.model.FameTier
import com.maisha.game.data.model.HealthCondition
import com.maisha.game.data.model.HustleType
import com.maisha.game.data.model.LifestyleOption
import com.maisha.game.data.model.PetSpecies
import com.maisha.game.data.model.PrisonActivity
import com.maisha.game.data.model.SchoolStage
import com.maisha.game.data.model.SkillType
import com.maisha.game.data.local.OnboardingTips
import com.maisha.game.data.model.PartTimeDemand
import com.maisha.game.data.model.PartTimeJob
import com.maisha.game.data.model.TalentTraining
import com.maisha.game.domain.CareerEngine
import com.maisha.game.domain.CrimeEngine
import com.maisha.game.domain.ActionFamily
import com.maisha.game.domain.ActionQuestHints
import com.maisha.game.domain.BucketListEngine
import com.maisha.game.domain.CrimeStatusKind
import com.maisha.game.domain.CrimeStatusMapper
import com.maisha.game.domain.EducationEngine
import com.maisha.game.domain.HealthEngine
import com.maisha.game.domain.LeisureActivity
import com.maisha.game.domain.LeisureEngine
import com.maisha.game.domain.RelationshipEngine
import com.maisha.game.domain.RelocationEngine
import com.maisha.game.domain.SkillEngine
import com.maisha.game.domain.SkillMasteryTier
import com.maisha.game.domain.SocialMediaEngine
import com.maisha.game.domain.YearQuest
import com.maisha.game.ui.components.ActionCard
import com.maisha.game.ui.components.ActionCardAccent
import com.maisha.game.ui.components.ActivityGridCard
import com.maisha.game.ui.components.ActivityRiskLevel
import com.maisha.game.ui.components.CategoryFilterChipRow
import com.maisha.game.ui.components.ConditionBadge
import com.maisha.game.ui.components.ConfirmActionDialog
import com.maisha.game.ui.components.ConfirmSeverity
import com.maisha.game.ui.components.ConfirmableActionHost
import com.maisha.game.ui.components.DismissibleTipCard
import com.maisha.game.ui.components.DrillDownRow
import com.maisha.game.ui.components.EmptyStateCard
import com.maisha.game.ui.components.InsetCardContainer
import com.maisha.game.ui.components.InsetDivider
import com.maisha.game.ui.components.StatBar
import com.maisha.game.ui.components.StatType
import com.maisha.game.ui.components.TabPageHero
import com.maisha.game.ui.components.rememberConfirmableAction
import com.maisha.game.ui.illustrations.EmptyStateIllustration
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.CreamBg
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.MaishaRadius
import com.maisha.game.ui.theme.MaishaSpacing
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.SurfaceMuted
import com.maisha.game.ui.theme.TealPrimary
import com.maisha.game.util.formatMoney

private const val CRIME_UI_MIN_AGE = 16
private const val SIDE_HUSTLE_UI_MIN_AGE = 14

private enum class ActionCategory {
    ALL, CARE, EARN, GROW, LIVE, RISK
}

private enum class ActionSheet {
    NONE,
    CHILDHOOD,
    MIND_BODY,
    DOCTOR_HEALTH,
    SKILLS_MASTERY,
    LOVE_FAMILY,
    SIDE_HUSTLES,
    LICENSES_RELOCATION,
    PHILANTHROPY_GOALS,
    CRIME,
    PRISON
}

private sealed class PendingAction {
    data class Crime(val type: CrimeType) : PendingAction()
    data class TargetedCrime(val target: CrimeTarget) : PendingAction()
    data class Treatment(val condition: HealthCondition, val careType: CareType) : PendingAction()
    data class Lifestyle(val option: LifestyleOption, val enable: Boolean) : PendingAction()
    data class SideHustle(val type: HustleType) : PendingAction()
    data class AdoptPet(val species: PetSpecies) : PendingAction()
    data object CreateSocialAccount : PendingAction()
    data object MonetizeSocialAccount : PendingAction()
    data class PracticeSkill(val type: SkillType) : PendingAction()
    data class Masterclass(val type: SkillType) : PendingAction()
    data class ShowcaseSkill(val type: SkillType) : PendingAction()
    data class AdoptBucket(val templateId: String) : PendingAction()
    data object RenewVisa : PendingAction()
    data object ApplyCitizenship : PendingAction()
    data object TakeDrivingTest : PendingAction()
    data object Volunteer : PendingAction()
    data class Donate(val amount: Int) : PendingAction()
    data class Leisure(val activity: LeisureActivity) : PendingAction()
    data object AdoptChild : PendingAction()
    data object RequestExpungement : PendingAction()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionsScreen(
    character: Character,
    uiState: LifeUiState,
    snackbarHostState: SnackbarHostState,
    onAttemptCrime: (CrimeType) -> Unit,
    onAttemptTargetedCrime: (CrimeTarget) -> Unit = {},
    onCaseCrimeTarget: (CrimeTarget) -> Unit = {},
    onLayLowCrime: () -> Unit = {},
    onBribeInformantCrime: () -> Unit = {},
    onVisitDoctor: (String, CareType) -> Unit,
    onSetLifestyleOption: (LifestyleOption, Boolean) -> Unit,
    onExecuteSideHustle: (HustleType) -> Unit,
    onAdoptPet: (PetSpecies) -> Unit,
    onCreateSocialAccount: () -> Unit,
    onPostSocialContent: () -> Unit,
    onMonetizeSocialAccount: () -> Unit,
    onPracticeSkill: (SkillType) -> Unit,
    onTakeMasterclass: (SkillType) -> Unit,
    onShowcaseSkill: (SkillType) -> Unit,
    onAdoptBucketGoal: (String) -> Unit,
    onRenewVisa: () -> Unit,
    onApplyForCitizenship: () -> Unit,
    onTakeDrivingTest: () -> Unit,
    onVolunteer: () -> Unit,
    onDonateToCharity: (Int) -> Unit,
    donationTiers: List<Int>,
    onPerformLeisure: (LeisureActivity) -> Unit,
    onPerformStudySession: () -> Unit = {},
    onPerformPrisonActivity: (PrisonActivity) -> Unit = {},
    onAdoptChild: () -> Unit = {},
    onRequestExpungement: () -> Unit = {},
    onWorkPartTime: (com.maisha.game.data.model.PartTimeJob) -> Unit = {},
    onQuitPartTimeJob: () -> Unit = {},
    onRestStudentEnergy: () -> Unit = {},
    onTrainTalent: (com.maisha.game.data.model.TalentTraining) -> Unit = {},
    onSignEndorsement: () -> Unit = {},
    onDropEndorsement: () -> Unit = {},
    onOpenDoubleLife: () -> Unit = {},
    onCommuteTransit: () -> Unit = {},
    onActionMessageDismissed: () -> Unit,
    onDismissLeisureTip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val incarcerated = character.criminalRecord.currentlyIncarcerated
    val awaitingTrial = character.criminalRecord.awaitingTrial
    val untreated = character.activeConditions.filter { !it.treated }
    val yearQuests = uiState.yearQuests
    val questHintLabel = stringResource(R.string.action_quest_hint)
    val leisureEngine = remember { LeisureEngine() }

    val showCrimeActions = character.age >= CRIME_UI_MIN_AGE &&
        !incarcerated && !awaitingTrial && character.alive
    val showLifestyleActions = character.alive && !incarcerated && !awaitingTrial
    val showLeisureActions = showLifestyleActions &&
        leisureEngine.activitiesFor(character).isNotEmpty()
    val showSideHustleActions = character.alive &&
        character.age >= SIDE_HUSTLE_UI_MIN_AGE &&
        !incarcerated &&
        !awaitingTrial &&
        !character.career.isRetired
    val showAdoptPetActions = character.alive &&
        !incarcerated &&
        !awaitingTrial &&
        character.pets.size < RelationshipEngine.MAX_PETS
    val showSocialMediaActions = character.alive &&
        character.age >= SocialMediaEngine.MIN_ACCOUNT_AGE &&
        !incarcerated &&
        !awaitingTrial
    val showSkillActions = character.alive &&
        character.age >= SkillEngine.MIN_SKILL_AGE &&
        !incarcerated &&
        !awaitingTrial
    val showBucketList = character.alive &&
        character.age >= 14 &&
        !incarcerated &&
        !awaitingTrial
    val showImmigrationOffice = character.alive &&
        character.isLivingAbroad() &&
        !incarcerated &&
        !awaitingTrial
    val showDrivingTest = character.alive &&
        !character.hasDrivingLicense &&
        character.age >= EducationEngine.MIN_DRIVING_AGE &&
        !incarcerated &&
        !awaitingTrial
    val showPhilanthropy = character.alive &&
        character.age >= 12 &&
        !incarcerated &&
        !awaitingTrial
    val crimeEngine = remember { CrimeEngine() }
    val careerEngine = remember { CareerEngine(HealthEngine(), RelocationEngine()) }
    val showStudentPartTime = character.alive &&
        !incarcerated &&
        !awaitingTrial &&
        careerEngine.canWorkPartTime(character)
    val showAdoptChild = character.alive &&
        character.age >= RelationshipEngine.MIN_ADOPT_AGE &&
        !incarcerated &&
        !awaitingTrial
    val showExpungement = character.alive &&
        !incarcerated &&
        !awaitingTrial &&
        crimeEngine.canRequestExpungement(character)
    val showStudySession = character.alive &&
        !incarcerated &&
        !awaitingTrial &&
        (character.education.stage == SchoolStage.PRIMARY ||
            character.education.stage == SchoolStage.SECONDARY ||
            character.education.stage == SchoolStage.UNIVERSITY) &&
        !character.education.expelled &&
        character.education.droppedOutFrom == null
    val donationAmounts = donationTiers.ifEmpty {
        listOf(100, 1_000, 10_000).map {
            EconomyScaler.scaleAmount(it, character.countryCode)
        }
    }
    val drivingTestFee = EconomyScaler.scaleAmount(
        EducationEngine.DRIVING_TEST_FEE_KENYA,
        character.countryCode
    )
    val canRenewVisa = showImmigrationOffice && character.currentVisa != null
    val canApplyCitizenship = showImmigrationOffice &&
        character.yearsInCurrentCountry >= RelocationEngine.NATURALIZATION_YEARS
    val visaRenewalFee = EconomyScaler.scaleAmount(
        RelocationEngine.VISA_RENEWAL_FEE_KENYA,
        character.countryCode
    )
    val citizenshipFee = EconomyScaler.scaleAmount(
        RelocationEngine.CITIZENSHIP_FEE_KENYA,
        character.countryCode
    )
    val masterclassCost = EconomyScaler.scaleAmount(
        SkillEngine.MASTERCLASS_BASE_COST_KENYA,
        character.countryCode
    )
    val crimeStatus = CrimeStatusMapper.map(character.criminalRecord)

    val showPrisonActions = incarcerated && character.alive
    val showJobsAndHustles = character.alive &&
        !incarcerated &&
        !awaitingTrial &&
        (showStudentPartTime || showSideHustleActions)
    val hasCare = untreated.isNotEmpty() || showLifestyleActions || showLeisureActions || showStudySession
    val hasEarn = showJobsAndHustles || showSocialMediaActions || showSkillActions
    val hasGrow = showSkillActions || showBucketList || showAdoptPetActions || showSocialMediaActions || showStudySession
    val hasLive = showDrivingTest || showPhilanthropy || showImmigrationOffice || showLeisureActions ||
        showStudentPartTime || showAdoptChild || showExpungement
    val showDoubleLife = character.alive && character.age >= 14 && !incarcerated
    val hasRisk = showCrimeActions || incarcerated || awaitingTrial || showPrisonActions || showDoubleLife
    val hasContent = hasCare || hasEarn || hasGrow || hasLive || hasRisk
    val leisureActivities = remember(character.age, character.criminalRecord) {
        leisureEngine.activitiesFor(character)
    }
    val actionCount = remember(
        leisureActivities.size,
        showStudySession,
        untreated.size,
        showJobsAndHustles,
        showSkillActions
    ) {
        var count = leisureActivities.size
        if (showStudySession) count++
        count += untreated.size
        if (showJobsAndHustles) {
            if (showStudentPartTime) count += PartTimeJob.entries.size
            if (showSideHustleActions) count += JobPool.getAllSideHustleTypes().size
        }
        if (showSkillActions) count += SkillType.entries.size
        count
    }

    val defaultCategory = when {
        untreated.isNotEmpty() -> ActionCategory.CARE
        incarcerated -> ActionCategory.RISK
        character.age < 18 -> ActionCategory.LIVE
        else -> ActionCategory.ALL
    }
    var selectedCategory by rememberSaveable { mutableStateOf(defaultCategory.name) }
    val category = ActionCategory.entries.find { it.name == selectedCategory } ?: ActionCategory.ALL

    fun show(cat: ActionCategory): Boolean =
        category == ActionCategory.ALL || category == cat

    val pendingAction = rememberConfirmableAction<PendingAction>()
    var expandedConditionId by remember { mutableStateOf<String?>(null) }
    var expandedSkill by rememberSaveable { mutableStateOf<String?>(null) }
    var activeActionSheet by rememberSaveable { mutableStateOf(ActionSheet.NONE) }

    LaunchedEffect(untreated.map { it.id }) {
        expandedConditionId = when {
            untreated.size == 1 -> untreated.first().id
            expandedConditionId != null && untreated.none { it.id == expandedConditionId } -> null
            else -> expandedConditionId
        }
    }

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onActionMessageDismissed()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBg)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        if (!hasContent) {
            val isToddler = character.age <= 4
            EmptyStateCard(
                illustration = EmptyStateIllustration.ACTIONS,
                title = stringResource(
                    if (isToddler) R.string.empty_actions_toddler_title else R.string.empty_actions_title
                ),
                message = stringResource(
                    if (isToddler) R.string.empty_actions_toddler_body else R.string.empty_actions_body
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = MaishaSpacing.md)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 156.dp),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(start = 10.dp, top = 4.dp, end = 10.dp, bottom = 28.dp)
            ) {
                if (showPrisonActions || incarcerated) {
                    if (crimeStatus.kind != CrimeStatusKind.CLEAR) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            CrimeStatusCard(
                                statusKind = crimeStatus.kind,
                                yearsRemaining = crimeStatus.yearsRemaining
                            )
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SectionHeaderTitle(title = stringResource(R.string.act_cat_prison_title))
                    }
                    item {
                        ActivityGridCard(
                            title = stringResource(R.string.act_cat_prison_title),
                            subtitle = stringResource(R.string.act_cat_prison_desc),
                            icon = Icons.Rounded.Lock,
                            iconTint = CoralNegative,
                            energyCost = 2,
                            riskLevel = ActivityRiskLevel.UNDERWORLD,
                            trailingBadge = "Inmate",
                            onClick = { activeActionSheet = ActionSheet.PRISON }
                        )
                    }
                } else {
                    if (character.age < 16) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SectionHeaderTitle(title = stringResource(R.string.act_cat_childhood_title))
                        }
                        item {
                            val childhoodSubtitle = if (character.age <= 2) {
                                "Infancy: Early development & bonding"
                            } else {
                                stringResource(R.string.act_cat_childhood_desc)
                            }
                            ActivityGridCard(
                                title = stringResource(R.string.act_cat_childhood_title),
                                subtitle = childhoodSubtitle,
                                icon = Icons.Rounded.ChildCare,
                                iconTint = LifeGreen,
                                energyCost = 1,
                                riskLevel = ActivityRiskLevel.SAFE,
                                onClick = { activeActionSheet = ActionSheet.CHILDHOOD }
                            )
                        }
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SectionHeaderTitle(title = stringResource(R.string.act_cat_mind_body_title))
                    }
                    item {
                        ActivityGridCard(
                            title = stringResource(R.string.act_cat_mind_body_title),
                            subtitle = stringResource(R.string.act_cat_mind_body_desc),
                            icon = Icons.Rounded.Spa,
                            iconTint = LifeGreen,
                            energyCost = 1,
                            riskLevel = ActivityRiskLevel.SAFE,
                            onClick = { activeActionSheet = ActionSheet.MIND_BODY }
                        )
                    }
                    item {
                        ActivityGridCard(
                            title = stringResource(R.string.act_cat_doctor_health_title),
                            subtitle = if (untreated.isNotEmpty()) "${untreated.size} untreated conditions" else stringResource(R.string.act_cat_doctor_health_desc),
                            icon = Icons.Rounded.MedicalServices,
                            iconTint = if (untreated.isNotEmpty()) CoralNegative else TealPrimary,
                            energyCost = 1,
                            riskLevel = if (untreated.isNotEmpty()) ActivityRiskLevel.MODERATE else ActivityRiskLevel.SAFE,
                            trailingBadge = if (untreated.isNotEmpty()) "! ${untreated.size}" else null,
                            onClick = { activeActionSheet = ActionSheet.DOCTOR_HEALTH }
                        )
                    }

                    val hasHustles = showSkillActions || showJobsAndHustles || showSocialMediaActions || (character.alive && character.age >= CareerEngine.MIN_TALENT_TRAINING_AGE)
                    if (hasHustles) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SectionHeaderTitle(title = stringResource(R.string.act_cat_side_hustles_title))
                        }
                        if (showJobsAndHustles || showSocialMediaActions || (character.alive && character.age >= CareerEngine.MIN_TALENT_TRAINING_AGE)) {
                            item {
                                ActivityGridCard(
                                    title = stringResource(R.string.section_jobs_side_hustles),
                                    subtitle = if (character.socialMedia.hasAccount) "${character.socialMedia.followers} Followers" else "Gigs & part-time jobs",
                                    icon = Icons.Rounded.Work,
                                    iconTint = LifeGreen,
                                    energyCost = 2,
                                    riskLevel = ActivityRiskLevel.LOW,
                                    onClick = { activeActionSheet = ActionSheet.SIDE_HUSTLES }
                                )
                            }
                        }
                        if (showSkillActions) {
                            item {
                                ActivityGridCard(
                                    title = stringResource(R.string.act_cat_side_hustles_title),
                                    subtitle = "Practice & masterclasses",
                                    icon = Icons.Rounded.School,
                                    iconTint = GoldAccent,
                                    energyCost = 1,
                                    riskLevel = ActivityRiskLevel.SAFE,
                                    onClick = { activeActionSheet = ActionSheet.SKILLS_MASTERY }
                                )
                            }
                        }
                    }

                    val hasLifestyle = showAdoptPetActions || showAdoptChild || showDrivingTest || showImmigrationOffice || showPhilanthropy || showBucketList
                    if (hasLifestyle) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SectionHeaderTitle(title = stringResource(R.string.act_cat_lifestyle_title))
                        }
                        if (showAdoptPetActions || showAdoptChild) {
                            item {
                                ActivityGridCard(
                                    title = stringResource(R.string.act_cat_adoption_title),
                                    subtitle = stringResource(R.string.act_cat_adoption_desc),
                                    icon = Icons.Rounded.Pets,
                                    iconTint = TealPrimary,
                                    energyCost = 1,
                                    riskLevel = ActivityRiskLevel.SAFE,
                                    onClick = { activeActionSheet = ActionSheet.LOVE_FAMILY }
                                )
                            }
                        }
                        if (showDrivingTest || showImmigrationOffice) {
                            item {
                                ActivityGridCard(
                                    title = stringResource(R.string.act_cat_relocate_title),
                                    subtitle = stringResource(R.string.act_cat_relocate_desc),
                                    icon = Icons.Rounded.Flight,
                                    iconTint = TealPrimary,
                                    energyCost = 1,
                                    riskLevel = ActivityRiskLevel.LOW,
                                    onClick = { activeActionSheet = ActionSheet.LICENSES_RELOCATION }
                                )
                            }
                        }
                        if (showPhilanthropy || showBucketList) {
                            item {
                                ActivityGridCard(
                                    title = stringResource(R.string.act_cat_philanthropy_title),
                                    subtitle = stringResource(R.string.act_cat_philanthropy_desc),
                                    icon = Icons.Rounded.EmojiEvents,
                                    iconTint = GoldAccent,
                                    energyCost = 1,
                                    riskLevel = ActivityRiskLevel.SAFE,
                                    onClick = { activeActionSheet = ActionSheet.PHILANTHROPY_GOALS }
                                )
                            }
                        }
                        item {
                            ActivityGridCard(
                                title = stringResource(R.string.routine_category_commute),
                                subtitle = stringResource(R.string.btn_transit_commute_desc),
                                icon = Icons.Rounded.Flight,
                                iconTint = TealPrimary,
                                energyCost = 1,
                                riskLevel = ActivityRiskLevel.LOW,
                                onClick = onCommuteTransit
                            )
                        }
                    }

                    if (showCrimeActions || showExpungement || showDoubleLife) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SectionHeaderTitle(title = stringResource(R.string.act_cat_crime_title))
                        }
                        if (showCrimeActions || showExpungement) {
                            item {
                                ActivityGridCard(
                                    title = stringResource(R.string.act_cat_crime_title),
                                    subtitle = if (character.criminalRecord.hasRecord) "Heat: ${character.criminalRecord.heatLevel}%" else stringResource(R.string.act_cat_crime_desc),
                                    icon = Icons.Rounded.Gavel,
                                    iconTint = CoralNegative,
                                    energyCost = 2,
                                    riskLevel = ActivityRiskLevel.HIGH,
                                    trailingBadge = if (character.criminalRecord.heatLevel > 40) "Heat!" else null,
                                    onClick = { activeActionSheet = ActionSheet.CRIME }
                                )
                            }
                        }
                        if (showDoubleLife) {
                            item {
                                val activeSecret = character.doubleLife.activeSecret
                                val doubleLifeSubtitle = if (activeSecret != null) {
                                    "${activeSecret.codename} (${activeSecret.exposureRisk}% Risk)"
                                } else {
                                    stringResource(R.string.desc_double_life_none)
                                }
                                ActivityGridCard(
                                    title = stringResource(R.string.title_double_life),
                                    subtitle = doubleLifeSubtitle,
                                    icon = Icons.Rounded.Lock,
                                    iconTint = if (activeSecret != null && activeSecret.exposureRisk >= 60) CoralNegative else TealPrimary,
                                    energyCost = 2,
                                    riskLevel = ActivityRiskLevel.UNDERWORLD,
                                    onClick = onOpenDoubleLife
                                )
                            }
                        }
                    }
                }
            }
        }

        when (activeActionSheet) {
            ActionSheet.CHILDHOOD -> {
                ActionCategorySheet(
                    title = stringResource(R.string.act_cat_childhood_title),
                    onDismiss = { activeActionSheet = ActionSheet.NONE }
                ) {
                    if (showStudySession) {
                        ActionCard(
                            icon = AppIcons.Education,
                            title = stringResource(R.string.action_study_session_title),
                            description = stringResource(R.string.action_study_session_desc),
                            metaLabel = stringResource(R.string.study_effort_hard),
                            accent = ActionCardAccent.CARE,
                            questHint = questHintIf(yearQuests, ActionFamily.STUDY, questHintLabel),
                            onClick = {
                                activeActionSheet = ActionSheet.NONE
                                onPerformStudySession()
                            }
                        )
                    }
                    val childhoodActivities = listOf(
                        LeisureActivity.PLAY_WITH_TOYS,
                        LeisureActivity.THROW_TANTRUM,
                        LeisureActivity.ASK_ALLOWANCE,
                        LeisureActivity.CHILDHOOD_HOBBY,
                        LeisureActivity.CHORES,
                        LeisureActivity.SNEAK_OUT,
                        LeisureActivity.MALL_HANGOUT,
                        LeisureActivity.GAMING_MARATHON
                    ).filter { it in leisureActivities }

                    childhoodActivities.forEach { activity ->
                        val cost = leisureEngine.cost(activity, character.countryCode)
                        val isChores = activity == LeisureActivity.CHORES
                        val canAfford = isChores || character.stats.money >= cost
                        val meta = if (isChores) {
                            stringResource(
                                R.string.format_leisure_meta,
                                stringResource(R.string.meta_earn_cash),
                                leisureEffectLabel(activity)
                            )
                        } else {
                            stringResource(
                                R.string.format_leisure_meta,
                                formatMoney(cost, character.countryCode),
                                leisureEffectLabel(activity)
                            )
                        }
                        ActionCard(
                            icon = AppIcons.Happiness,
                            title = leisureTitle(activity),
                            description = leisureDescription(activity),
                            metaLabel = meta,
                            enabled = canAfford,
                            accent = ActionCardAccent.CARE,
                            questHint = questHintIf(yearQuests, ActionFamily.LEISURE, questHintLabel),
                            onClick = {
                                if (canAfford) {
                                    activeActionSheet = ActionSheet.NONE
                                    pendingAction.request(PendingAction.Leisure(activity))
                                }
                            }
                        )
                    }
                }
            }
            ActionSheet.MIND_BODY -> {
                ActionCategorySheet(
                    title = stringResource(R.string.act_cat_mind_body_title),
                    onDismiss = { activeActionSheet = ActionSheet.NONE }
                ) {
                    val adultLeisure = leisureActivities.filter {
                        it !in listOf(
                            LeisureActivity.PLAY_WITH_TOYS,
                            LeisureActivity.THROW_TANTRUM,
                            LeisureActivity.ASK_ALLOWANCE,
                            LeisureActivity.CHILDHOOD_HOBBY,
                            LeisureActivity.SNEAK_OUT,
                            LeisureActivity.MALL_HANGOUT,
                            LeisureActivity.GAMING_MARATHON
                        )
                    }
                    adultLeisure.forEach { activity ->
                        val cost = leisureEngine.cost(activity, character.countryCode)
                        val isChores = activity == LeisureActivity.CHORES
                        val canAfford = isChores || character.stats.money >= cost
                        val meta = if (isChores) {
                            stringResource(
                                R.string.format_leisure_meta,
                                stringResource(R.string.meta_earn_cash),
                                leisureEffectLabel(activity)
                            )
                        } else {
                            stringResource(
                                R.string.format_leisure_meta,
                                formatMoney(cost, character.countryCode),
                                leisureEffectLabel(activity)
                            )
                        }
                        ActionCard(
                            icon = AppIcons.Happiness,
                            title = leisureTitle(activity),
                            description = leisureDescription(activity),
                            metaLabel = meta,
                            enabled = canAfford,
                            accent = ActionCardAccent.CARE,
                            questHint = questHintIf(yearQuests, ActionFamily.LEISURE, questHintLabel),
                            onClick = {
                                if (canAfford) {
                                    activeActionSheet = ActionSheet.NONE
                                    pendingAction.request(PendingAction.Leisure(activity))
                                }
                            }
                        )
                    }
                    ActionCard(
                        icon = AppIcons.Travel,
                        title = stringResource(
                            R.string.btn_transit_commute,
                            CountryCatalog.flavorFor(character.countryCode).commonTransportMode
                        ),
                        description = stringResource(R.string.btn_transit_commute_desc),
                        metaLabel = CountryCatalog.flavorFor(character.countryCode).commonTransportMode,
                        enabled = true,
                        accent = ActionCardAccent.CARE,
                        onClick = {
                            activeActionSheet = ActionSheet.NONE
                            onCommuteTransit()
                        }
                    )
                    if (showLifestyleActions) {
                        LifestyleActionCard(
                            character = character,
                            option = LifestyleOption.GYM,
                            activeTitleRes = R.string.lifestyle_gym_active,
                            inactiveTitleRes = R.string.lifestyle_gym_title,
                            descriptionRes = R.string.lifestyle_gym_desc,
                            yearlyCost = lifestyleYearlyCost(LifestyleOption.GYM, character.countryCode),
                            icon = AppIcons.Health,
                            questHint = questHintIf(yearQuests, ActionFamily.LIFESTYLE_WELLNESS, questHintLabel),
                            onToggle = { enable ->
                                activeActionSheet = ActionSheet.NONE
                                pendingAction.request(PendingAction.Lifestyle(LifestyleOption.GYM, enable))
                            }
                        )
                        LifestyleActionCard(
                            character = character,
                            option = LifestyleOption.DIET,
                            activeTitleRes = R.string.lifestyle_diet_active,
                            inactiveTitleRes = R.string.lifestyle_diet_title,
                            descriptionRes = R.string.lifestyle_diet_desc,
                            yearlyCost = lifestyleYearlyCost(LifestyleOption.DIET, character.countryCode),
                            icon = AppIcons.Looks,
                            questHint = questHintIf(yearQuests, ActionFamily.LIFESTYLE_WELLNESS, questHintLabel),
                            onToggle = { enable ->
                                activeActionSheet = ActionSheet.NONE
                                pendingAction.request(PendingAction.Lifestyle(LifestyleOption.DIET, enable))
                            }
                        )
                        LifestyleActionCard(
                            character = character,
                            option = LifestyleOption.THERAPIST,
                            activeTitleRes = R.string.lifestyle_therapist_active,
                            inactiveTitleRes = R.string.lifestyle_therapist_title,
                            descriptionRes = R.string.lifestyle_therapist_desc,
                            yearlyCost = lifestyleYearlyCost(LifestyleOption.THERAPIST, character.countryCode),
                            icon = AppIcons.Happiness,
                            questHint = questHintIf(yearQuests, ActionFamily.LIFESTYLE_WELLNESS, questHintLabel),
                            onToggle = { enable ->
                                activeActionSheet = ActionSheet.NONE
                                pendingAction.request(PendingAction.Lifestyle(LifestyleOption.THERAPIST, enable))
                            }
                        )
                    }
                }
            }
            ActionSheet.DOCTOR_HEALTH -> {
                ActionCategorySheet(
                    title = stringResource(R.string.act_cat_doctor_health_title),
                    onDismiss = { activeActionSheet = ActionSheet.NONE }
                ) {
                    if (untreated.isNotEmpty()) {
                        untreated.forEach { condition ->
                            val isExpanded = expandedConditionId == condition.id
                            val treatmentHint = questHintIf(yearQuests, ActionFamily.TREATMENT, questHintLabel)
                            ConditionBadge(
                                condition = condition,
                                onClick = {
                                    expandedConditionId = if (isExpanded) null else condition.id
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (isExpanded) {
                                ActionCard(
                                    icon = AppIcons.HealthClinic,
                                    title = stringResource(R.string.care_public_clinic),
                                    description = successHint(CareType.PUBLIC_CLINIC),
                                    metaLabel = treatmentCostLabel(character, condition.severity, CareType.PUBLIC_CLINIC),
                                    accent = ActionCardAccent.CARE,
                                    questHint = treatmentHint,
                                    onClick = {
                                        activeActionSheet = ActionSheet.NONE
                                        pendingAction.request(PendingAction.Treatment(condition, CareType.PUBLIC_CLINIC))
                                    }
                                )
                                ActionCard(
                                    icon = AppIcons.HealthHospital,
                                    title = CountryCatalog.flavorFor(character.countryCode).privateHospitalName,
                                    description = successHint(CareType.PRIVATE_HOSPITAL),
                                    metaLabel = treatmentCostLabel(character, condition.severity, CareType.PRIVATE_HOSPITAL),
                                    accent = ActionCardAccent.CARE,
                                    questHint = treatmentHint,
                                    onClick = {
                                        activeActionSheet = ActionSheet.NONE
                                        pendingAction.request(PendingAction.Treatment(condition, CareType.PRIVATE_HOSPITAL))
                                    }
                                )
                            }
                        }
                    }
                    if (showLifestyleActions) {
                        LifestyleActionCard(
                            character = character,
                            option = LifestyleOption.HEALTH_INSURANCE,
                            activeTitleRes = R.string.lifestyle_insurance_active,
                            inactiveTitleRes = R.string.lifestyle_insurance_title,
                            descriptionRes = R.string.lifestyle_insurance_desc,
                            yearlyCost = lifestyleYearlyCost(LifestyleOption.HEALTH_INSURANCE, character.countryCode),
                            icon = AppIcons.Health,
                            questHint = questHintIf(yearQuests, ActionFamily.LIFESTYLE_WELLNESS, questHintLabel),
                            onToggle = { enable ->
                                activeActionSheet = ActionSheet.NONE
                                pendingAction.request(PendingAction.Lifestyle(LifestyleOption.HEALTH_INSURANCE, enable))
                            }
                        )
                    }
                }
            }
            ActionSheet.SKILLS_MASTERY -> {
                ActionCategorySheet(
                    title = stringResource(R.string.act_cat_side_hustles_title),
                    onDismiss = { activeActionSheet = ActionSheet.NONE }
                ) {
                    SkillType.entries.forEach { skillType ->
                        val level = character.skills.find { it.type == skillType }?.level ?: 0
                        CollapsibleSkillRow(
                            skillType = skillType,
                            level = level,
                            expanded = expandedSkill == skillType.name,
                            onToggleExpand = {
                                expandedSkill = if (expandedSkill == skillType.name) null else skillType.name
                            },
                            masterclassCost = masterclassCost,
                            countryCode = character.countryCode,
                            money = character.stats.money,
                            canShowcase = level >= SkillEngine.TIER_MASTER_MIN && !character.skillShowcaseDoneThisYear,
                            practiceHint = questHintIf(yearQuests, ActionFamily.SKILL_PRACTICE, questHintLabel),
                            showcaseHint = questHintIf(yearQuests, ActionFamily.SKILL_SHOWCASE, questHintLabel),
                            showEarnActions = true,
                            showGrowActions = true,
                            onPractice = {
                                activeActionSheet = ActionSheet.NONE
                                pendingAction.request(PendingAction.PracticeSkill(skillType))
                            },
                            onMasterclass = {
                                activeActionSheet = ActionSheet.NONE
                                pendingAction.request(PendingAction.Masterclass(skillType))
                            },
                            onShowcase = {
                                activeActionSheet = ActionSheet.NONE
                                pendingAction.request(PendingAction.ShowcaseSkill(skillType))
                            }
                        )
                    }
                }
            }
            ActionSheet.LOVE_FAMILY -> {
                ActionCategorySheet(
                    title = stringResource(R.string.act_cat_adoption_title),
                    onDismiss = { activeActionSheet = ActionSheet.NONE }
                ) {
                    if (showAdoptPetActions) {
                        PetCatalog.getAll().forEach { entry ->
                            val adoptionCost = EconomyScaler.scaleAmount(entry.adoptionFee, character.countryCode)
                            val yearlyCost = EconomyScaler.scaleAmount(entry.yearlyUpkeep, character.countryCode)
                            val canAfford = character.stats.money >= adoptionCost
                            ActionCard(
                                icon = AppIcons.Family,
                                title = adoptPetTitle(entry.species),
                                description = adoptPetDescription(entry.species),
                                metaLabel = stringResource(
                                    R.string.format_pet_adoption_cost,
                                    formatMoney(adoptionCost, character.countryCode),
                                    formatMoney(yearlyCost, character.countryCode)
                                ),
                                enabled = canAfford,
                                onClick = {
                                    if (canAfford) {
                                        activeActionSheet = ActionSheet.NONE
                                        pendingAction.request(PendingAction.AdoptPet(entry.species))
                                    }
                                }
                            )
                        }
                    }
                    if (showAdoptChild) {
                        ActionCard(
                            icon = AppIcons.Family,
                            title = stringResource(R.string.btn_adopt_child),
                            description = stringResource(R.string.dialog_adopt_child_body),
                            accent = ActionCardAccent.CARE,
                            onClick = {
                                activeActionSheet = ActionSheet.NONE
                                pendingAction.request(PendingAction.AdoptChild)
                            }
                        )
                    }
                }
            }
            ActionSheet.SIDE_HUSTLES -> {
                ActionCategorySheet(
                    title = stringResource(R.string.section_jobs_side_hustles),
                    onDismiss = { activeActionSheet = ActionSheet.NONE }
                ) {
                    if (showSocialMediaActions) {
                        if (!character.socialMedia.hasAccount) {
                            ActionCard(
                                icon = AppIcons.Looks,
                                title = stringResource(R.string.btn_create_social_account),
                                description = stringResource(R.string.social_create_desc),
                                metaLabel = stringResource(R.string.social_create_meta),
                                onClick = {
                                    activeActionSheet = ActionSheet.NONE
                                    pendingAction.request(PendingAction.CreateSocialAccount)
                                }
                            )
                        } else {
                            ActionCard(
                                icon = AppIcons.Looks,
                                title = stringResource(R.string.btn_post_social_update),
                                description = stringResource(R.string.social_post_desc),
                                metaLabel = stringResource(
                                    R.string.format_social_followers,
                                    character.socialMedia.followers,
                                    if (character.socialMedia.isVerified) stringResource(R.string.social_verified_badge) else ""
                                ),
                                questHint = questHintIf(yearQuests, ActionFamily.SOCIAL_POST, questHintLabel),
                                onClick = {
                                    activeActionSheet = ActionSheet.NONE
                                    onPostSocialContent()
                                }
                            )
                            val canMonetize = character.socialMedia.followers >= SocialMediaEngine.MONETIZATION_FOLLOWER_THRESHOLD &&
                                !character.socialMedia.monetizedThisYear
                            ActionCard(
                                icon = AppIcons.Money,
                                title = stringResource(R.string.btn_monetize_social),
                                description = stringResource(R.string.social_monetize_desc),
                                metaLabel = when {
                                    character.socialMedia.monetizedThisYear -> stringResource(R.string.msg_social_already_monetized)
                                    character.socialMedia.followers < SocialMediaEngine.MONETIZATION_FOLLOWER_THRESHOLD ->
                                        stringResource(R.string.format_social_monetize_req, SocialMediaEngine.MONETIZATION_FOLLOWER_THRESHOLD)
                                    else -> stringResource(R.string.social_monetize_ready)
                                },
                                enabled = canMonetize,
                                accent = ActionCardAccent.GOLD,
                                questHint = questHintIf(yearQuests, ActionFamily.SOCIAL_MONETIZE, questHintLabel),
                                onClick = {
                                    if (canMonetize) {
                                        activeActionSheet = ActionSheet.NONE
                                        pendingAction.request(PendingAction.MonetizeSocialAccount)
                                    }
                                }
                            )
                        }
                    }
                    if (showJobsAndHustles) {
                        val canRest = showStudentPartTime && !character.career.energyRestedThisYear && character.career.energyLevel < 95
                        PartTimeEnergyHeader(
                            character = character,
                            showStudentHints = showStudentPartTime,
                            canRest = canRest,
                            onRestStudentEnergy = onRestStudentEnergy
                        )
                        if (character.career.activePartTimeJob != null) {
                            ActivePartTimeManagementCard(
                                character = character,
                                careerEngine = careerEngine,
                                onQuitPartTimeJob = onQuitPartTimeJob
                            )
                        }
                        if (showStudentPartTime) {
                            PartTimeJob.entries.forEach { job ->
                                PartTimeJobMarketCard(
                                    character = character,
                                    careerEngine = careerEngine,
                                    job = job,
                                    onApply = { onWorkPartTime(job) }
                                )
                            }
                        }
                        if (showSideHustleActions) {
                            JobPool.getAllSideHustleTypes().forEach { hustleType ->
                                SideHustleActionCard(
                                    character = character,
                                    hustleType = hustleType,
                                    questHint = questHintIf(yearQuests, ActionFamily.SIDE_HUSTLE, questHintLabel),
                                    onClick = {
                                        activeActionSheet = ActionSheet.NONE
                                        pendingAction.request(PendingAction.SideHustle(hustleType))
                                    }
                                )
                            }
                        }
                    }
                    if (character.alive && character.age >= CareerEngine.MIN_TALENT_TRAINING_AGE && !incarcerated && !awaitingTrial) {
                        ActionCard(
                            icon = AppIcons.Health,
                            title = stringResource(R.string.btn_gym_session),
                            description = stringResource(R.string.talent_athleticism, character.career.athleticism),
                            metaLabel = if (character.career.gymTrainedThisYear) stringResource(R.string.msg_talent_already_trained) else null,
                            enabled = !character.career.gymTrainedThisYear,
                            accent = ActionCardAccent.CARE,
                            onClick = {
                                activeActionSheet = ActionSheet.NONE
                                onTrainTalent(TalentTraining.GYM_SESSION)
                            }
                        )
                        ActionCard(
                            icon = AppIcons.Looks,
                            title = stringResource(R.string.btn_music_lesson),
                            description = stringResource(R.string.talent_musical, character.career.musicalTalent),
                            metaLabel = if (character.career.musicTrainedThisYear) stringResource(R.string.msg_talent_already_trained) else null,
                            enabled = !character.career.musicTrainedThisYear,
                            accent = ActionCardAccent.GOLD,
                            onClick = {
                                activeActionSheet = ActionSheet.NONE
                                onTrainTalent(TalentTraining.MUSIC_LESSON)
                            }
                        )
                        ActionCard(
                            icon = AppIcons.Money,
                            title = stringResource(R.string.btn_street_busk),
                            description = stringResource(R.string.fame_hub_hint),
                            enabled = !character.career.talentGigDoneThisYear,
                            accent = ActionCardAccent.GOLD,
                            onClick = {
                                activeActionSheet = ActionSheet.NONE
                                onTrainTalent(TalentTraining.STREET_BUSK)
                            }
                        )
                        ActionCard(
                            icon = AppIcons.Health,
                            title = stringResource(R.string.btn_exhibition_match),
                            description = stringResource(R.string.talent_athleticism, character.career.athleticism),
                            enabled = !character.career.talentGigDoneThisYear,
                            accent = ActionCardAccent.CARE,
                            onClick = {
                                activeActionSheet = ActionSheet.NONE
                                onTrainTalent(TalentTraining.EXHIBITION_MATCH)
                            }
                        )
                        if (careerEngine.canSignEndorsement(character) || character.career.endorsementActive) {
                            ActionCard(
                                icon = AppIcons.Money,
                                title = if (character.career.endorsementActive) stringResource(R.string.btn_drop_endorsement) else stringResource(R.string.btn_sign_endorsement),
                                description = if (character.career.endorsementActive) {
                                    stringResource(R.string.endorsement_active, formatMoney(character.career.endorsementPayoutPerYear, character.countryCode))
                                } else {
                                    stringResource(R.string.endorsement_none)
                                },
                                accent = ActionCardAccent.GOLD,
                                onClick = {
                                    activeActionSheet = ActionSheet.NONE
                                    if (character.career.endorsementActive) onDropEndorsement() else onSignEndorsement()
                                }
                            )
                        }
                    }
                }
            }
            ActionSheet.LICENSES_RELOCATION -> {
                ActionCategorySheet(
                    title = stringResource(R.string.act_cat_relocate_title),
                    onDismiss = { activeActionSheet = ActionSheet.NONE }
                ) {
                    if (showDrivingTest) {
                        val canAfford = character.stats.money >= drivingTestFee
                        ActionCard(
                            icon = AppIcons.Career,
                            title = stringResource(R.string.btn_take_driving_test),
                            description = stringResource(R.string.driving_test_desc, formatMoney(drivingTestFee, character.countryCode)),
                            metaLabel = formatMoney(drivingTestFee, character.countryCode),
                            enabled = canAfford,
                            onClick = {
                                if (canAfford) {
                                    activeActionSheet = ActionSheet.NONE
                                    pendingAction.request(PendingAction.TakeDrivingTest)
                                }
                            }
                        )
                    }
                    if (showImmigrationOffice) {
                        Text(
                            text = stringResource(
                                R.string.immigration_status_summary,
                                character.currentVisa?.name?.lowercase() ?: stringResource(R.string.immigration_no_visa),
                                character.visaYearsRemaining
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (canRenewVisa) {
                            val canAfford = character.stats.money >= visaRenewalFee
                            ActionCard(
                                icon = AppIcons.Career,
                                title = stringResource(R.string.btn_renew_visa),
                                description = stringResource(R.string.immigration_renew_visa_desc, formatMoney(visaRenewalFee, character.countryCode)),
                                metaLabel = formatMoney(visaRenewalFee, character.countryCode),
                                enabled = canAfford,
                                onClick = {
                                    if (canAfford) {
                                        activeActionSheet = ActionSheet.NONE
                                        pendingAction.request(PendingAction.RenewVisa)
                                    }
                                }
                            )
                        }
                        if (canApplyCitizenship) {
                            val canAfford = character.stats.money >= citizenshipFee
                            ActionCard(
                                icon = AppIcons.Family,
                                title = stringResource(R.string.btn_apply_citizenship),
                                description = stringResource(R.string.immigration_citizenship_desc, formatMoney(citizenshipFee, character.countryCode)),
                                metaLabel = formatMoney(citizenshipFee, character.countryCode),
                                enabled = canAfford,
                                onClick = {
                                    if (canAfford) {
                                        activeActionSheet = ActionSheet.NONE
                                        pendingAction.request(PendingAction.ApplyCitizenship)
                                    }
                                }
                            )
                        }
                    }
                }
            }
            ActionSheet.PHILANTHROPY_GOALS -> {
                ActionCategorySheet(
                    title = stringResource(R.string.act_cat_philanthropy_title),
                    onDismiss = { activeActionSheet = ActionSheet.NONE }
                ) {
                    if (showPhilanthropy) {
                        ActionCard(
                            icon = AppIcons.Happiness,
                            title = stringResource(R.string.btn_volunteer),
                            description = stringResource(R.string.volunteer_desc),
                            metaLabel = stringResource(R.string.meta_free_karma),
                            accent = ActionCardAccent.CARE,
                            questHint = questHintIf(yearQuests, ActionFamily.VOLUNTEER, questHintLabel),
                            onClick = {
                                activeActionSheet = ActionSheet.NONE
                                pendingAction.request(PendingAction.Volunteer)
                            }
                        )
                        donationAmounts.forEach { amount ->
                            val canAfford = character.stats.money >= amount
                            ActionCard(
                                icon = AppIcons.Money,
                                title = stringResource(R.string.btn_donate_amount, formatMoney(amount, character.countryCode)),
                                description = stringResource(R.string.donate_desc),
                                metaLabel = formatMoney(amount, character.countryCode),
                                enabled = canAfford,
                                accent = ActionCardAccent.CARE,
                                questHint = questHintIf(yearQuests, ActionFamily.DONATE, questHintLabel),
                                onClick = {
                                    if (canAfford) {
                                        activeActionSheet = ActionSheet.NONE
                                        pendingAction.request(PendingAction.Donate(amount))
                                    }
                                }
                            )
                        }
                    }
                    if (showBucketList) {
                        val bucketEngine = remember { BucketListEngine() }
                        val templates = bucketEngine.availableTemplates(character)
                        character.bucketList.filter { !it.completed }.forEach { goal ->
                            ActionCard(
                                icon = AppIcons.Smarts,
                                title = bucketTitle(goal.kind),
                                description = bucketDescription(goal.kind),
                                metaLabel = stringResource(R.string.format_bucket_in_progress, bucketTitle(goal.kind)),
                                enabled = false,
                                accent = ActionCardAccent.GOLD,
                                onClick = {}
                            )
                        }
                        templates.forEach { template ->
                            val cost = EconomyScaler.scaleAmount(template.commitmentKenya, character.countryCode)
                            val canAfford = character.stats.money >= cost
                            ActionCard(
                                icon = AppIcons.Money,
                                title = bucketTitle(template.kind),
                                description = bucketDescription(template.kind),
                                metaLabel = stringResource(R.string.format_bucket_commitment, formatMoney(cost, character.countryCode)),
                                enabled = canAfford,
                                accent = ActionCardAccent.GOLD,
                                onClick = {
                                    if (canAfford) {
                                        activeActionSheet = ActionSheet.NONE
                                        pendingAction.request(PendingAction.AdoptBucket(template.id))
                                    }
                                }
                            )
                        }
                    }
                }
            }
            ActionSheet.CRIME -> {
                ActionCategorySheet(
                    title = stringResource(R.string.act_cat_crime_title),
                    onDismiss = { activeActionSheet = ActionSheet.NONE }
                ) {
                    // Notoriety Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CoralNegative.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.crime_notoriety_title),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralNegative
                                )
                                Text(
                                    text = when {
                                        character.criminalRecord.notoriety >= 75 -> stringResource(R.string.notoriety_kingpin)
                                        character.criminalRecord.notoriety >= 40 -> stringResource(R.string.notoriety_infamous)
                                        character.criminalRecord.notoriety >= 15 -> stringResource(R.string.notoriety_hustler)
                                        else -> stringResource(R.string.notoriety_amateur)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${character.criminalRecord.notoriety}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CoralNegative
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    if (showCrimeActions) {
                        if (character.criminalRecord.heatLevel > 0) {
                            PoliceHeatMeterCard(
                                heatLevel = character.criminalRecord.heatLevel,
                                heatActionDone = character.criminalRecord.heatActionDoneThisYear,
                                onLayLow = {
                                    activeActionSheet = ActionSheet.NONE
                                    onLayLowCrime()
                                },
                                onBribeInformant = {
                                    activeActionSheet = ActionSheet.NONE
                                    onBribeInformantCrime()
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        ActionCard(
                            icon = AppIcons.CrimePickpocket,
                            title = stringResource(R.string.crime_pickpocket_title),
                            description = stringResource(
                                R.string.crime_pickpocket_desc,
                                CountryCatalog.flavorFor(character.countryCode).commonTransportMode
                            ),
                            metaLabel = stringResource(R.string.meta_risk_moderate),
                            riskLabel = stringResource(R.string.action_risk_medium),
                            energyCost = 1,
                            accent = ActionCardAccent.RISK,
                            onClick = {
                                activeActionSheet = ActionSheet.NONE
                                pendingAction.request(PendingAction.Crime(CrimeType.PICKPOCKET))
                            }
                        )
                        ActionCard(
                            icon = AppIcons.CrimeShoplift,
                            title = stringResource(R.string.crime_shoplift_title),
                            description = stringResource(R.string.crime_shoplift_desc),
                            metaLabel = stringResource(R.string.meta_risk_moderate),
                            riskLabel = stringResource(R.string.action_risk_medium),
                            energyCost = 1,
                            accent = ActionCardAccent.RISK,
                            onClick = {
                                activeActionSheet = ActionSheet.NONE
                                pendingAction.request(PendingAction.Crime(CrimeType.SHOPLIFT))
                            }
                        )
                        ActionCard(
                            icon = AppIcons.CrimeFraud,
                            title = stringResource(R.string.crime_fraud_title),
                            description = stringResource(R.string.crime_fraud_desc),
                            metaLabel = stringResource(R.string.meta_risk_high),
                            riskLabel = stringResource(R.string.action_risk_high),
                            energyCost = 2,
                            accent = ActionCardAccent.RISK,
                            onClick = {
                                activeActionSheet = ActionSheet.NONE
                                pendingAction.request(PendingAction.Crime(CrimeType.FRAUD))
                            }
                        )

                        // Specialized Crime Targets (Burglary, GTA, Heists)
                        CrimeTarget.entries.forEach { target ->
                            val scaledReward = EconomyScaler.scaleAmount(target.baseReward, character.countryCode)
                            val isCased = character.criminalRecord.casedTarget == target
                            val riskLabel = when {
                                isCased -> stringResource(R.string.btn_cased_target_badge)
                                target.difficulty >= 0.75f -> "Extreme Risk"
                                target.difficulty >= 0.50f -> "High Risk"
                                else -> "Moderate Risk"
                            }
                            ActionCard(
                                icon = AppIcons.CrimePickpocket,
                                title = "${target.category}: ${target.name.lowercase().replace('_', ' ').replaceFirstChar { it.titlecase() }}",
                                description = "Take: ~${formatMoney(scaledReward, character.countryCode)} · Notoriety: +${target.notorietyGain}%" +
                                    if (isCased) " [Cased: +20% Odds]" else "",
                                metaLabel = riskLabel,
                                accent = ActionCardAccent.RISK,
                                onClick = {
                                    activeActionSheet = ActionSheet.NONE
                                    pendingAction.request(PendingAction.TargetedCrime(target))
                                }
                            )

                            if (!isCased && character.criminalRecord.heatLevel < 85) {
                                ActionCard(
                                    icon = AppIcons.CrimeFraud,
                                    title = "Case ${target.name.lowercase().replace('_', ' ').replaceFirstChar { it.titlecase() }}",
                                    description = "Scope out guards and security before moving (+Planning bonus, +5 Heat)",
                                    metaLabel = stringResource(R.string.btn_case_target),
                                    accent = ActionCardAccent.RISK,
                                    onClick = {
                                        activeActionSheet = ActionSheet.NONE
                                        onCaseCrimeTarget(target)
                                    }
                                )
                            }
                        }
                    }
                    if (showExpungement) {
                        ActionCard(
                            icon = AppIcons.Career,
                            title = stringResource(R.string.btn_request_expungement),
                            description = stringResource(
                                R.string.dialog_expunge_body,
                                formatMoney(crimeEngine.expungementFee(character), character.countryCode)
                            ),
                            accent = ActionCardAccent.RISK,
                            onClick = {
                                activeActionSheet = ActionSheet.NONE
                                pendingAction.request(PendingAction.RequestExpungement)
                            }
                        )
                    }
                }
            }
            ActionSheet.PRISON -> {
                ActionCategorySheet(
                    title = stringResource(R.string.act_cat_prison_title),
                    onDismiss = { activeActionSheet = ActionSheet.NONE }
                ) {
                    if (crimeStatus.kind != CrimeStatusKind.CLEAR) {
                        CrimeStatusCard(statusKind = crimeStatus.kind, yearsRemaining = crimeStatus.yearsRemaining)
                    }
                    PrisonActivity.entries.forEach { activity ->
                        ActionCard(
                            icon = AppIcons.Health,
                            title = prisonActivityTitle(activity),
                            description = prisonActivityDescription(activity),
                            accent = ActionCardAccent.CARE,
                            onClick = {
                                activeActionSheet = ActionSheet.NONE
                                onPerformPrisonActivity(activity)
                            }
                        )
                    }
                }
            }
            ActionSheet.NONE -> Unit
        }
    }

    ConfirmableActionHost(
        state = pendingAction,
        onConfirmed = { action ->
            when (action) {
                is PendingAction.Crime -> onAttemptCrime(action.type)
                is PendingAction.TargetedCrime -> onAttemptTargetedCrime(action.target)
                is PendingAction.Treatment -> onVisitDoctor(action.condition.id, action.careType)
                is PendingAction.Lifestyle -> onSetLifestyleOption(action.option, action.enable)
                is PendingAction.SideHustle -> onExecuteSideHustle(action.type)
                is PendingAction.AdoptPet -> onAdoptPet(action.species)
                PendingAction.CreateSocialAccount -> onCreateSocialAccount()
                PendingAction.MonetizeSocialAccount -> onMonetizeSocialAccount()
                is PendingAction.PracticeSkill -> onPracticeSkill(action.type)
                is PendingAction.Masterclass -> onTakeMasterclass(action.type)
                is PendingAction.ShowcaseSkill -> onShowcaseSkill(action.type)
                is PendingAction.AdoptBucket -> onAdoptBucketGoal(action.templateId)
                PendingAction.RenewVisa -> onRenewVisa()
                PendingAction.ApplyCitizenship -> onApplyForCitizenship()
                PendingAction.TakeDrivingTest -> onTakeDrivingTest()
                PendingAction.Volunteer -> onVolunteer()
                is PendingAction.Donate -> onDonateToCharity(action.amount)
                is PendingAction.Leisure -> onPerformLeisure(action.activity)
                PendingAction.AdoptChild -> onAdoptChild()
                PendingAction.RequestExpungement -> onRequestExpungement()
            }
        }
    ) { action, onConfirm, onDismiss ->
        when (action) {
            is PendingAction.Crime -> {
                val (title, description) = crimeConfirmCopy(action.type)
                ConfirmActionDialog(
                    title = title,
                    description = description,
                    confirmLabel = stringResource(R.string.btn_attempt),
                    severity = ConfirmSeverity.WARNING,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
            is PendingAction.TargetedCrime -> {
                val rewardLabel = formatMoney(
                    EconomyScaler.scaleAmount(action.target.baseReward, character.countryCode),
                    character.countryCode
                )
                ConfirmActionDialog(
                    title = "${action.target.category}: ${action.target.name.lowercase().replace('_', ' ').replaceFirstChar { it.titlecase() }}",
                    description = "Attempting this score will net ~${rewardLabel}. Notoriety will increase by +${action.target.notorietyGain}%. Extreme police heat if you get caught!",
                    confirmLabel = stringResource(R.string.btn_attempt),
                    severity = ConfirmSeverity.WARNING,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
            is PendingAction.Treatment -> {
                val careName = when (action.careType) {
                    CareType.PUBLIC_CLINIC -> stringResource(R.string.care_public_clinic)
                    CareType.PRIVATE_HOSPITAL ->
                        CountryCatalog.flavorFor(character.countryCode).privateHospitalName
                }
                ConfirmActionDialog(
                    title = stringResource(R.string.dialog_seek_treatment_title),
                    description = stringResource(
                        R.string.dialog_seek_treatment_desc,
                        action.condition.name,
                        careName,
                        treatmentCostLabel(character, action.condition.severity, action.careType),
                        successHint(action.careType)
                    ),
                    confirmLabel = stringResource(R.string.btn_visit_doctor),
                    severity = ConfirmSeverity.NEUTRAL,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
            is PendingAction.SideHustle -> {
                ConfirmActionDialog(
                    title = stringResource(R.string.dialog_side_hustle_title),
                    description = stringResource(R.string.dialog_side_hustle_description),
                    confirmLabel = stringResource(R.string.btn_side_hustle),
                    severity = ConfirmSeverity.NEUTRAL,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
            is PendingAction.AdoptPet -> {
                val entry = PetCatalog.findBySpecies(action.species)
                val adoptionCost = entry?.let {
                    formatMoney(
                        EconomyScaler.scaleAmount(it.adoptionFee, character.countryCode),
                        character.countryCode
                    )
                } ?: ""
                ConfirmActionDialog(
                    title = stringResource(R.string.dialog_adopt_pet_title),
                    description = stringResource(
                        R.string.dialog_adopt_pet_description,
                        adoptPetTitle(action.species),
                        adoptionCost
                    ),
                    confirmLabel = stringResource(R.string.btn_adopt_pet),
                    severity = ConfirmSeverity.NEUTRAL,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
            PendingAction.CreateSocialAccount -> {
                ConfirmActionDialog(
                    title = stringResource(R.string.dialog_create_social_title),
                    description = stringResource(R.string.dialog_create_social_description),
                    confirmLabel = stringResource(R.string.btn_create_social_account),
                    severity = ConfirmSeverity.NEUTRAL,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
            PendingAction.MonetizeSocialAccount -> {
                ConfirmActionDialog(
                    title = stringResource(R.string.dialog_monetize_social_title),
                    description = stringResource(R.string.dialog_monetize_social_description),
                    confirmLabel = stringResource(R.string.btn_monetize_social),
                    severity = ConfirmSeverity.NEUTRAL,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
            is PendingAction.PracticeSkill -> {
                ConfirmActionDialog(
                    title = stringResource(R.string.dialog_practice_skill_title),
                    description = stringResource(
                        R.string.dialog_practice_skill_description,
                        skillTypeLabel(action.type)
                    ),
                    confirmLabel = stringResource(R.string.btn_practice_skill),
                    severity = ConfirmSeverity.NEUTRAL,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
            is PendingAction.Masterclass -> {
                ConfirmActionDialog(
                    title = stringResource(R.string.dialog_masterclass_title),
                    description = stringResource(
                        R.string.dialog_masterclass_description,
                        skillTypeLabel(action.type),
                        formatMoney(masterclassCost, character.countryCode)
                    ),
                    confirmLabel = stringResource(R.string.btn_take_masterclass),
                    severity = ConfirmSeverity.NEUTRAL,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
            is PendingAction.ShowcaseSkill -> {
                ConfirmActionDialog(
                    title = stringResource(R.string.btn_showcase_skill),
                    description = stringResource(R.string.skill_showcase_desc),
                    confirmLabel = stringResource(R.string.btn_showcase_skill),
                    severity = ConfirmSeverity.NEUTRAL,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
            is PendingAction.AdoptBucket -> {
                ConfirmActionDialog(
                    title = stringResource(R.string.section_bucket_list),
                    description = stringResource(R.string.msg_bucket_adopted),
                    confirmLabel = stringResource(R.string.btn_confirm),
                    severity = ConfirmSeverity.NEUTRAL,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
            PendingAction.RenewVisa -> {
                ConfirmActionDialog(
                    title = stringResource(R.string.dialog_renew_visa_title),
                    description = stringResource(
                        R.string.dialog_renew_visa_body,
                        formatMoney(visaRenewalFee, character.countryCode)
                    ),
                    confirmLabel = stringResource(R.string.btn_renew_visa),
                    severity = ConfirmSeverity.NEUTRAL,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
            PendingAction.ApplyCitizenship -> {
                ConfirmActionDialog(
                    title = stringResource(R.string.dialog_apply_citizenship_title),
                    description = stringResource(
                        R.string.dialog_apply_citizenship_body,
                        formatMoney(citizenshipFee, character.countryCode)
                    ),
                    confirmLabel = stringResource(R.string.btn_apply_citizenship),
                    severity = ConfirmSeverity.NEUTRAL,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
            PendingAction.TakeDrivingTest -> {
                ConfirmActionDialog(
                    title = stringResource(R.string.dialog_driving_test_title),
                    description = stringResource(
                        R.string.dialog_driving_test_body,
                        formatMoney(drivingTestFee, character.countryCode)
                    ),
                    confirmLabel = stringResource(R.string.btn_take_driving_test),
                    severity = ConfirmSeverity.NEUTRAL,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
            PendingAction.Volunteer -> {
                ConfirmActionDialog(
                    title = stringResource(R.string.dialog_volunteer_title),
                    description = stringResource(R.string.dialog_volunteer_body),
                    confirmLabel = stringResource(R.string.btn_volunteer),
                    severity = ConfirmSeverity.NEUTRAL,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
            is PendingAction.Donate -> {
                ConfirmActionDialog(
                    title = stringResource(R.string.dialog_donate_title),
                    description = stringResource(
                        R.string.dialog_donate_body,
                        formatMoney(action.amount, character.countryCode)
                    ),
                    confirmLabel = stringResource(R.string.btn_donate),
                    severity = ConfirmSeverity.NEUTRAL,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
            is PendingAction.Leisure -> {
                val cost = leisureEngine.cost(action.activity, character.countryCode)
                ConfirmActionDialog(
                    title = leisureTitle(action.activity),
                    description = stringResource(
                        R.string.dialog_leisure_body,
                        leisureDescription(action.activity),
                        formatMoney(cost, character.countryCode)
                    ),
                    confirmLabel = stringResource(R.string.btn_leisure_go),
                    severity = ConfirmSeverity.NEUTRAL,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
            is PendingAction.Lifestyle -> {
                val label = lifestyleLabel(action.option, action.enable)
                if (action.enable) {
                    ConfirmActionDialog(
                        title = stringResource(R.string.dialog_lifestyle_enable_title),
                        description = stringResource(
                            R.string.dialog_lifestyle_enable_desc,
                            label,
                            stringResource(
                                R.string.format_yearly_cost,
                                formatMoney(
                                    lifestyleYearlyCost(action.option, character.countryCode),
                                    character.countryCode
                                )
                            )
                        ),
                        confirmLabel = stringResource(R.string.btn_subscribe),
                        severity = ConfirmSeverity.NEUTRAL,
                        onConfirm = onConfirm,
                        onDismiss = onDismiss
                    )
                } else {
                    ConfirmActionDialog(
                        title = stringResource(R.string.dialog_lifestyle_disable_title),
                        description = stringResource(
                            R.string.dialog_lifestyle_disable_desc,
                            label
                        ),
                        confirmLabel = stringResource(R.string.btn_cancel_subscription),
                        severity = ConfirmSeverity.NEUTRAL,
                        onConfirm = onConfirm,
                        onDismiss = onDismiss
                    )
                }
            }
            PendingAction.AdoptChild -> {
                ConfirmActionDialog(
                    title = stringResource(R.string.dialog_adopt_child_title),
                    description = stringResource(R.string.dialog_adopt_child_body),
                    confirmLabel = stringResource(R.string.btn_adopt_child),
                    severity = ConfirmSeverity.NEUTRAL,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
            PendingAction.RequestExpungement -> {
                ConfirmActionDialog(
                    title = stringResource(R.string.dialog_expunge_title),
                    description = stringResource(
                        R.string.dialog_expunge_body,
                        formatMoney(crimeEngine.expungementFee(character), character.countryCode)
                    ),
                    confirmLabel = stringResource(R.string.btn_request_expungement),
                    severity = ConfirmSeverity.WARNING,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun CrimeStatusCard(statusKind: CrimeStatusKind, yearsRemaining: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaishaRadius.cardShape,
        colors = CardDefaults.cardColors(containerColor = CoralNegative.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.section_street_risks),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CoralNegative
            )
            when (statusKind) {
                CrimeStatusKind.AWAITING_TRIAL -> {
                    Text(
                        text = stringResource(R.string.crime_status_awaiting_trial),
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkPrimary
                    )
                }
                CrimeStatusKind.INCARCERATED -> {
                    Text(
                        text = stringResource(R.string.msg_in_prison, yearsRemaining),
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkPrimary
                    )
                    Text(
                        text = stringResource(R.string.crime_status_parole_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkTertiary
                    )
                }
                CrimeStatusKind.CLEAR -> Unit
            }
        }
    }
}

@Composable
private fun CollapsibleSkillRow(
    skillType: SkillType,
    level: Int,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    masterclassCost: Int,
    countryCode: String,
    money: Int,
    canShowcase: Boolean,
    practiceHint: String?,
    showcaseHint: String?,
    showEarnActions: Boolean,
    showGrowActions: Boolean,
    onPractice: () -> Unit,
    onMasterclass: () -> Unit,
    onShowcase: () -> Unit
) {
    val tier = SkillEngine.masteryTierOf(level)
    val (current, next) = SkillEngine.progressToNextTierOf(level)
    val tierLabel = skillTierLabel(tier)
    val progressLabel = if (tier == SkillMasteryTier.MASTER && level >= SkillEngine.MAX_SKILL_LEVEL) {
        stringResource(R.string.format_skill_tier_capped, tierLabel)
    } else {
        stringResource(R.string.format_skill_tier_progress, tierLabel, current, next)
    }
    val canAffordMasterclass = money >= masterclassCost
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = skillTypeLabel(skillType),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = InkPrimary
                    )
                    Text(
                        text = progressLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TealPrimary
                    )
                }
                Text(
                    text = if (expanded) "▾" else "▸",
                    color = InkTertiary,
                    fontSize = 16.sp
                )
            }
            if (expanded) {
                StatBar(
                    type = StatType.SKILL,
                    value = level,
                    label = "",
                    showIcon = false,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
        if (expanded) {
            if (showGrowActions) {
                ActionCard(
                    icon = AppIcons.Smarts,
                    title = stringResource(R.string.btn_practice_skill),
                    description = stringResource(R.string.skill_practice_desc),
                    metaLabel = stringResource(R.string.skill_practice_meta),
                    questHint = practiceHint,
                    onClick = onPractice
                )
                ActionCard(
                    icon = AppIcons.Money,
                    title = stringResource(R.string.btn_take_masterclass),
                    description = stringResource(R.string.skill_masterclass_desc),
                    metaLabel = if (canAffordMasterclass) {
                        stringResource(
                            R.string.format_masterclass_cost,
                            formatMoney(masterclassCost, countryCode)
                        )
                    } else {
                        stringResource(R.string.msg_skill_cannot_afford)
                    },
                    enabled = canAffordMasterclass,
                    accent = ActionCardAccent.GOLD,
                    onClick = { if (canAffordMasterclass) onMasterclass() }
                )
            }
            if (showEarnActions && tier == SkillMasteryTier.MASTER) {
                ActionCard(
                    icon = AppIcons.Money,
                    title = stringResource(R.string.btn_showcase_skill),
                    description = stringResource(R.string.skill_showcase_desc),
                    metaLabel = if (canShowcase) {
                        progressLabel
                    } else {
                        stringResource(R.string.msg_skill_showcase_done)
                    },
                    enabled = canShowcase,
                    accent = ActionCardAccent.GOLD,
                    questHint = showcaseHint,
                    onClick = { if (canShowcase) onShowcase() }
                )
            }
        }
    }
}

@Composable
private fun questHintIf(
    quests: List<YearQuest>,
    family: ActionFamily,
    label: String
): String? = if (ActionQuestHints.anyMatch(quests, family)) label else null

@Composable
private fun skillTierLabel(tier: SkillMasteryTier): String = when (tier) {
    SkillMasteryTier.NOVICE -> stringResource(R.string.skill_tier_novice)
    SkillMasteryTier.ADEPT -> stringResource(R.string.skill_tier_adept)
    SkillMasteryTier.EXPERT -> stringResource(R.string.skill_tier_expert)
    SkillMasteryTier.MASTER -> stringResource(R.string.skill_tier_master)
}

@Composable
private fun fameTierLabel(tier: FameTier): String = when (tier) {
    FameTier.UNKNOWN -> stringResource(R.string.fame_tier_unknown)
    FameTier.LOCAL -> stringResource(R.string.fame_tier_local)
    FameTier.REGIONAL -> stringResource(R.string.fame_tier_regional)
    FameTier.NATIONAL -> stringResource(R.string.fame_tier_national)
    FameTier.GLOBAL -> stringResource(R.string.fame_tier_global)
}

@Composable
private fun bucketTitle(kind: BucketGoalKind): String = when (kind) {
    BucketGoalKind.OWN_HOME -> stringResource(R.string.bucket_own_home_title)
    BucketGoalKind.REACH_FAME -> stringResource(R.string.bucket_reach_fame_title)
    BucketGoalKind.START_BUSINESS -> stringResource(R.string.bucket_start_business_title)
    BucketGoalKind.WIN_OFFICE -> stringResource(R.string.bucket_win_office_title)
    BucketGoalKind.RAISE_CHILD -> stringResource(R.string.bucket_raise_child_title)
    BucketGoalKind.HIT_WEALTH -> stringResource(R.string.bucket_hit_wealth_title)
    BucketGoalKind.MASTER_SKILL -> stringResource(R.string.bucket_master_skill_title)
}

@Composable
private fun bucketDescription(kind: BucketGoalKind): String = when (kind) {
    BucketGoalKind.OWN_HOME -> stringResource(R.string.bucket_own_home_desc)
    BucketGoalKind.REACH_FAME -> stringResource(R.string.bucket_reach_fame_desc)
    BucketGoalKind.START_BUSINESS -> stringResource(R.string.bucket_start_business_desc)
    BucketGoalKind.WIN_OFFICE -> stringResource(R.string.bucket_win_office_desc)
    BucketGoalKind.RAISE_CHILD -> stringResource(R.string.bucket_raise_child_desc)
    BucketGoalKind.HIT_WEALTH -> stringResource(R.string.bucket_hit_wealth_desc)
    BucketGoalKind.MASTER_SKILL -> stringResource(R.string.bucket_master_skill_desc)
}

@Composable
private fun skillTypeLabel(type: SkillType): String = when (type) {
    SkillType.GUITAR -> stringResource(R.string.skill_guitar)
    SkillType.COOKING -> stringResource(R.string.skill_cooking)
    SkillType.MARTIAL_ARTS -> stringResource(R.string.skill_martial_arts)
    SkillType.PROGRAMMING -> stringResource(R.string.skill_programming)
    SkillType.WRITING -> stringResource(R.string.skill_writing)
}

@Composable
private fun adoptPetTitle(species: PetSpecies): String = when (species) {
    PetSpecies.DOG -> stringResource(R.string.pet_species_dog)
    PetSpecies.CAT -> stringResource(R.string.pet_species_cat)
    PetSpecies.BIRD -> stringResource(R.string.pet_species_bird)
    PetSpecies.FISH -> stringResource(R.string.pet_species_fish)
    PetSpecies.EXOTIC -> stringResource(R.string.pet_species_exotic)
}

@Composable
private fun adoptPetDescription(species: PetSpecies): String = when (species) {
    PetSpecies.DOG -> stringResource(R.string.pet_adopt_dog_desc)
    PetSpecies.CAT -> stringResource(R.string.pet_adopt_cat_desc)
    PetSpecies.BIRD -> stringResource(R.string.pet_adopt_bird_desc)
    PetSpecies.FISH -> stringResource(R.string.pet_adopt_fish_desc)
    PetSpecies.EXOTIC -> stringResource(R.string.pet_adopt_exotic_desc)
}

@Composable
private fun prisonActivityTitle(activity: PrisonActivity): String = when (activity) {
    PrisonActivity.WORK_DETAIL -> stringResource(R.string.prison_work_title)
    PrisonActivity.LIBRARY -> stringResource(R.string.prison_library_title)
    PrisonActivity.EXERCISE -> stringResource(R.string.prison_exercise_title)
    PrisonActivity.GOOD_BEHAVIOR -> stringResource(R.string.prison_good_behavior_title)
    PrisonActivity.PRISON_YARD_GANG -> stringResource(R.string.prison_gang_title)
    PrisonActivity.PRISON_ESCAPE_ATTEMPT -> stringResource(R.string.prison_escape_title)
    PrisonActivity.PAROLE_HEARING_APPEAL -> stringResource(R.string.prison_parole_title)
}

@Composable
private fun prisonActivityDescription(activity: PrisonActivity): String = when (activity) {
    PrisonActivity.WORK_DETAIL -> stringResource(R.string.prison_work_desc)
    PrisonActivity.LIBRARY -> stringResource(R.string.prison_library_desc)
    PrisonActivity.EXERCISE -> stringResource(R.string.prison_exercise_desc)
    PrisonActivity.GOOD_BEHAVIOR -> stringResource(R.string.prison_good_behavior_desc)
    PrisonActivity.PRISON_YARD_GANG -> stringResource(R.string.prison_gang_desc)
    PrisonActivity.PRISON_ESCAPE_ATTEMPT -> stringResource(R.string.prison_escape_desc)
    PrisonActivity.PAROLE_HEARING_APPEAL -> stringResource(R.string.prison_parole_desc)
}

@Composable
private fun SideHustleActionCard(
    character: Character,
    hustleType: HustleType,
    questHint: String?,
    onClick: () -> Unit
) {
    val alreadyDone = character.career.sideHustleDoneThisYear
    val meetsPrerequisites = JobPool.meetsSideHustlePrerequisites(character, hustleType)
    val available = !alreadyDone && meetsPrerequisites
    val metaLabel = when {
        alreadyDone -> stringResource(R.string.msg_side_hustle_already_done)
        !meetsPrerequisites -> sideHustleRequirementLabel(hustleType)
        else -> stringResource(R.string.meta_earn_cash)
    }
    ActionCard(
        icon = AppIcons.Money,
        title = sideHustleTitle(hustleType),
        description = sideHustleDescription(hustleType),
        metaLabel = metaLabel,
        energyCost = 2,
        enabled = available,
        accent = ActionCardAccent.GOLD,
        questHint = if (available) questHint else null,
        onClick = { if (available) onClick() }
    )
}

@Composable
private fun partTimeJobTitle(job: PartTimeJob): String = when (job) {
    PartTimeJob.RETAIL -> stringResource(R.string.part_time_retail)
    PartTimeJob.FAST_FOOD -> stringResource(R.string.part_time_fast_food)
    PartTimeJob.BARISTA -> stringResource(R.string.part_time_barista)
    PartTimeJob.BABYSITTING -> stringResource(R.string.part_time_babysitting)
    PartTimeJob.TUTORING -> stringResource(R.string.part_time_tutoring)
    PartTimeJob.FREELANCE_CODER -> stringResource(R.string.part_time_freelance_coder)
}

@Composable
private fun partTimeDemandLabel(demand: PartTimeDemand): String = when (demand) {
    PartTimeDemand.HIGH -> stringResource(R.string.part_time_demand_high)
    PartTimeDemand.MEDIUM -> stringResource(R.string.part_time_demand_medium)
    PartTimeDemand.LOW -> stringResource(R.string.part_time_demand_low)
}

@Composable
private fun sideHustleTitle(type: HustleType): String = when (type) {
    HustleType.RIDE_SHARE -> stringResource(R.string.hustle_ride_share_title)
    HustleType.FREELANCE_CODING -> stringResource(R.string.hustle_freelance_coding_title)
    HustleType.TUTORING -> stringResource(R.string.hustle_tutoring_title)
    HustleType.FOOD_DELIVERY -> stringResource(R.string.hustle_food_delivery_title)
    HustleType.RESELLING -> stringResource(R.string.hustle_reselling_title)
    HustleType.HANDMADE_CRAFTS -> stringResource(R.string.hustle_handmade_crafts_title)
    HustleType.STREAMING -> stringResource(R.string.hustle_streaming_title)
    HustleType.SCRIPT_CODING -> stringResource(R.string.hustle_script_coding_title)
}

@Composable
private fun sideHustleDescription(type: HustleType): String = when (type) {
    HustleType.RIDE_SHARE -> stringResource(R.string.hustle_ride_share_desc)
    HustleType.FREELANCE_CODING -> stringResource(R.string.hustle_freelance_coding_desc)
    HustleType.TUTORING -> stringResource(R.string.hustle_tutoring_desc)
    HustleType.FOOD_DELIVERY -> stringResource(R.string.hustle_food_delivery_desc)
    HustleType.RESELLING -> stringResource(R.string.hustle_reselling_desc)
    HustleType.HANDMADE_CRAFTS -> stringResource(R.string.hustle_handmade_crafts_desc)
    HustleType.STREAMING -> stringResource(R.string.hustle_streaming_desc)
    HustleType.SCRIPT_CODING -> stringResource(R.string.hustle_script_coding_desc)
}

@Composable
private fun sideHustleRequirementLabel(type: HustleType): String = when (type) {
    HustleType.RIDE_SHARE -> stringResource(R.string.hustle_ride_share_req)
    HustleType.FREELANCE_CODING -> stringResource(R.string.hustle_freelance_coding_req)
    HustleType.TUTORING -> stringResource(R.string.hustle_tutoring_req)
    HustleType.RESELLING -> stringResource(R.string.hustle_reselling_req)
    HustleType.FOOD_DELIVERY -> stringResource(R.string.msg_side_hustle_prerequisites)
    HustleType.HANDMADE_CRAFTS -> stringResource(R.string.hustle_handmade_crafts_req)
    HustleType.STREAMING -> stringResource(R.string.hustle_streaming_req)
    HustleType.SCRIPT_CODING -> stringResource(R.string.hustle_script_coding_req)
}

@Composable
private fun LifestyleActionCard(
    character: Character,
    option: LifestyleOption,
    activeTitleRes: Int,
    inactiveTitleRes: Int,
    descriptionRes: Int,
    yearlyCost: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    questHint: String?,
    onToggle: (Boolean) -> Unit
) {
    val active = when (option) {
        LifestyleOption.GYM -> character.lifestyle.hasGymMembership
        LifestyleOption.DIET -> character.lifestyle.isVegan
        LifestyleOption.THERAPIST -> character.lifestyle.hasTherapist
        LifestyleOption.HEALTH_INSURANCE -> character.lifestyle.hasHealthInsurance
    }
    val isMinorInsurance = option == LifestyleOption.HEALTH_INSURANCE && character.age < 18
    val desc = if (isMinorInsurance) {
        stringResource(R.string.health_insurance_minor_desc)
    } else {
        stringResource(descriptionRes)
    }
    val canAfford = character.stats.money >= yearlyCost || active
    val isEnabled = !isMinorInsurance && canAfford
    ActionCard(
        icon = icon,
        title = stringResource(if (active) activeTitleRes else inactiveTitleRes),
        description = desc,
        metaLabel = if (isMinorInsurance) {
            stringResource(R.string.full_time_locked_age)
        } else {
            stringResource(
                R.string.format_yearly_cost,
                formatMoney(yearlyCost, character.countryCode)
            )
        },
        enabled = isEnabled,
        accent = ActionCardAccent.CARE,
        questHint = questHint,
        onClick = { onToggle(!active) }
    )
}

@Composable
private fun lifestyleLabel(option: LifestyleOption, enabling: Boolean): String = when (option) {
    LifestyleOption.GYM -> stringResource(
        if (enabling) R.string.lifestyle_gym_title else R.string.lifestyle_gym_active
    )
    LifestyleOption.DIET -> stringResource(
        if (enabling) R.string.lifestyle_diet_title else R.string.lifestyle_diet_active
    )
    LifestyleOption.THERAPIST -> stringResource(
        if (enabling) R.string.lifestyle_therapist_title else R.string.lifestyle_therapist_active
    )
    LifestyleOption.HEALTH_INSURANCE -> stringResource(
        if (enabling) R.string.lifestyle_insurance_title else R.string.lifestyle_insurance_active
    )
}

private fun lifestyleYearlyCost(option: LifestyleOption, countryCode: String): Int {
    val base = when (option) {
        LifestyleOption.GYM -> HealthEngine.GYM_YEARLY_COST
        LifestyleOption.DIET -> HealthEngine.DIET_YEARLY_COST
        LifestyleOption.THERAPIST -> HealthEngine.THERAPIST_YEARLY_COST
        LifestyleOption.HEALTH_INSURANCE -> HealthEngine.HEALTH_INSURANCE_YEARLY_COST
    }
    return EconomyScaler.scaleAmount(base, countryCode)
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = TealPrimary,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun treatmentCostLabel(
    character: Character,
    severity: Int,
    careType: CareType
): String {
    val amount = HealthUiHelpers.treatmentCost(character, severity, careType)
    return stringResource(
        R.string.format_treatment_cost,
        formatMoney(amount, character.countryCode)
    )
}

@Composable
private fun successHint(careType: CareType): String = when (careType) {
    CareType.PUBLIC_CLINIC -> stringResource(R.string.hint_public_clinic_success)
    CareType.PRIVATE_HOSPITAL -> stringResource(R.string.hint_private_hospital_success)
}

@Composable
private fun crimeConfirmCopy(type: CrimeType): Pair<String, String> = when (type) {
    CrimeType.PICKPOCKET -> stringResource(R.string.dialog_crime_pickpocket_title) to
        stringResource(R.string.dialog_crime_pickpocket_desc)
    CrimeType.SHOPLIFT -> stringResource(R.string.dialog_crime_shoplift_title) to
        stringResource(R.string.dialog_crime_shoplift_desc)
    CrimeType.FRAUD -> stringResource(R.string.dialog_crime_fraud_title) to
        stringResource(R.string.dialog_crime_fraud_desc)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionCategorySheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = PaperWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            content()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PoliceHeatMeterCard(
    heatLevel: Int,
    heatActionDone: Boolean,
    onLayLow: () -> Unit,
    onBribeInformant: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CoralNegative.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, CoralNegative.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CoralNegative
                    ) {
                        Text(
                            text = "HEAT $heatLevel%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.police_heat_meter_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CoralNegative
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(SurfaceMuted)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (heatLevel / 100f).coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(CoralNegative)
                )
            }

            Text(
                text = stringResource(R.string.police_heat_meter_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onLayLow,
                    enabled = !heatActionDone,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralNegative),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.btn_lay_low),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onBribeInformant,
                    enabled = !heatActionDone,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.btn_bribe_informant),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            color = InkTertiary
        )
    }
}
