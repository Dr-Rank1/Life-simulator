// app/src/main/java/com/maisha/game/ui/main/LifeScreen.kt (modified — tab crossfade, celebrations, stat floats)
package com.maisha.game.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.R
import com.maisha.game.data.local.OnboardingTips
import com.maisha.game.data.model.BenefactorFavorType
import com.maisha.game.data.model.BucketGoalKind
import com.maisha.game.data.model.CareerState
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.ChildhoodAptitude
import com.maisha.game.data.model.ConfrontationChoice
import com.maisha.game.data.model.CrimeType
import com.maisha.game.data.model.EducationState
import com.maisha.game.data.model.EventChoice
import com.maisha.game.data.model.Expression
import com.maisha.game.data.model.FeudAction
import com.maisha.game.data.model.Gender
import com.maisha.game.data.model.LivingStandard
import com.maisha.game.data.model.Person
import com.maisha.game.data.model.SchoolStage
import com.maisha.game.data.model.SecretIdentityType
import com.maisha.game.data.model.Stats
import com.maisha.game.domain.EventLogCategory
import com.maisha.game.domain.EventLogClassifier
import com.maisha.game.domain.EventLogTone
import com.maisha.game.domain.GiftTier
import com.maisha.game.domain.InteractionType
import com.maisha.game.domain.LifeArchetypeEngine
import com.maisha.game.domain.YearQuest
import com.maisha.game.domain.YearQuestKind
import com.maisha.game.domain.YearQuestProgress
import com.maisha.game.ui.celebration.CelebrationOverlay
import com.maisha.game.ui.components.AchievementUnlockedDialog
import com.maisha.game.domain.CrimePushLuckSession
import com.maisha.game.domain.ExamFocusChallenge
import com.maisha.game.domain.ExamFocusTactic
import com.maisha.game.domain.NegotiationChallenge
import com.maisha.game.domain.NegotiationTactics
import com.maisha.game.domain.PushLuckAction
import com.maisha.game.ui.components.CrimePushLuckSheet
import com.maisha.game.ui.components.DoubleLifeSheet
import com.maisha.game.ui.components.ExamFocusDialog
import com.maisha.game.ui.components.LifeArchetypeStrings
import com.maisha.game.ui.components.NegotiationDialog
import com.maisha.game.ui.components.SecretConfrontationDialog
import com.maisha.game.ui.components.AgeUpButton
import com.maisha.game.ui.components.AppLoadingIndicator
import com.maisha.game.ui.components.CountryFlag
import com.maisha.game.ui.components.DismissibleTipCard
import com.maisha.game.ui.components.EmptyStateCard
import com.maisha.game.ui.components.WeeklyChallengeCard
import com.maisha.game.ui.components.WeeklyChallengeSheet
import com.maisha.game.ui.components.FloatingStatChangeLayer
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import com.maisha.game.ui.components.LeftNavRail
import com.maisha.game.ui.theme.BorderCardSlate
import com.maisha.game.ui.theme.CanvasBg
import com.maisha.game.ui.theme.DeepSlate
import com.maisha.game.ui.components.MainTab
import com.maisha.game.ui.components.MaishaBottomNav
import com.maisha.game.ui.components.MaishaIcon
import com.maisha.game.ui.components.MaishaStatIcon
import com.maisha.game.ui.components.MoneyStatRow
import com.maisha.game.ui.components.PersonAvatar
import com.maisha.game.ui.components.RecordBadge
import com.maisha.game.ui.components.StatBar
import com.maisha.game.ui.components.StatType
import com.maisha.game.ui.components.color
import com.maisha.game.ui.feedback.FeedbackEffect
import com.maisha.game.ui.illustrations.EmptyStateIllustration
import com.maisha.game.ui.navigation.NavAnimations
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.CreamBg
import com.maisha.game.ui.theme.CreamMuted
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.Hairline
import com.maisha.game.ui.theme.HairlineSoft
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.LifeGreenSoft

import com.maisha.game.ui.theme.MaishaRadius
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import com.maisha.game.ui.components.DockedStatStrip
import com.maisha.game.ui.theme.MaishaSpacing
import com.maisha.game.ui.theme.NavyDeep
import com.maisha.game.ui.theme.NavyElevated
import com.maisha.game.ui.theme.OutlineInk
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.StatHealth
import com.maisha.game.ui.theme.StatHappiness
import com.maisha.game.ui.theme.StatLooks
import com.maisha.game.ui.theme.StatMoney
import com.maisha.game.ui.theme.StatSmarts
import com.maisha.game.ui.theme.SuccessGreen
import com.maisha.game.ui.theme.TealPrimary
import com.maisha.game.util.formatMoney

@Composable
fun LifeScreen(
    uiState: LifeUiState,
    onAgeUp: () -> Unit,
    onChoiceSelected: (EventChoice) -> Unit,
    onFamilyMemberSelected: (Person) -> Unit,
    onFamilyMemberDismissed: () -> Unit,
    onPetSelected: (com.maisha.game.data.model.Pet) -> Unit,
    onPetDismissed: () -> Unit,
    onPetCare: (String, com.maisha.game.domain.PetCareAction) -> Unit,
    onFamilyInteraction: (String, InteractionType, GiftTier?) -> Unit,
    onFamilyInteractionMessageDismissed: () -> Unit,
    onFindDate: () -> Unit,
    onSeekFriendship: () -> Unit,
    onDismissDatingProspects: () -> Unit,
    onStartDating: (Person) -> Unit,
    onMakeExclusive: (String) -> Unit = {},
    onPropose: (String) -> Unit,
    onProposeWithPrenup: (String) -> Unit = { id -> onPropose(id) },
    onBreakUp: (String) -> Unit,
    onHaveChild: () -> Unit,
    onRelationshipMessageDismissed: () -> Unit,
    onApplyForJob: (String) -> Unit,
    onQuitJob: () -> Unit,
    onRetire: () -> Unit,
    retirementPensionEstimate: Int,
    onDropOut: () -> Unit,
    onStartBusiness: (String, com.maisha.game.data.model.BusinessIndustry, Int) -> Unit,
    onLaunchCampaign: (com.maisha.game.data.model.PoliticalOffice, Int) -> Unit,
    onPassTaxPolicy: (com.maisha.game.data.model.TaxPolicyType) -> Unit,
    onSellBusiness: (String) -> Unit,
    businessInvestmentTiers: List<Int>,
    onSetWorkEffort: (com.maisha.game.data.model.WorkEffort) -> Unit,
    onOfficeAction: (com.maisha.game.data.model.OfficeAction) -> Unit = {},
    onSetStudyEffort: (com.maisha.game.data.model.StudyEffort) -> Unit,
    onSyncExamSchedule: () -> Unit,
    onJoinSchoolClub: (com.maisha.game.data.model.SchoolClub) -> Unit,
    onPerformClubActivity: (com.maisha.game.data.model.ClubPracticeIntensity) -> Unit,
    onClaimClubMajorEvent: () -> Unit,
    onHostClubFundraiser: () -> Unit,
    onChallengeRivalSchool: () -> Unit,
    onLeaveSchoolClub: () -> Unit,
    onPerformSchoolActivity: (com.maisha.game.data.model.SchoolActivity, String?) -> Unit,
    onSchoolPersonInteraction: (String, com.maisha.game.data.model.SchoolPersonAction) -> Unit,
    onWorkplaceAction: (String, com.maisha.game.data.model.WorkplaceAction) -> Unit = { _, _ -> },
    onResolveExpulsionHearing: (com.maisha.game.data.model.ExpulsionHearingChoice) -> Unit,
    onServeDetention: () -> Unit,
    onApologizeToPrincipal: () -> Unit,
    onExamPrepChoice: (com.maisha.game.data.model.ExamPrepChoice) -> Unit,
    onEnrollInUniversity: (
        com.maisha.game.data.model.UniversityMajor,
        com.maisha.game.data.model.UniversityFunding
    ) -> Unit = { _, _ -> },
    onCampusJob: () -> Unit = {},
    onUniversityInternship: () -> Unit = {},
    onRepayStudentLoan: (Int) -> Unit = {},
    onStartCareerTrack: (com.maisha.game.data.model.CareerTrack) -> Unit,
    onPracticeCareerTrack: () -> Unit,
    onTrainTalent: (com.maisha.game.data.model.TalentTraining) -> Unit = {},
    onSignEndorsement: () -> Unit = {},
    onDropEndorsement: () -> Unit = {},
    onLeaveCareerTrack: () -> Unit = {},
    onExploreChildhoodAptitude: (com.maisha.game.data.model.ChildhoodAptitude) -> Unit = {},
    onJoinSchoolClique: (com.maisha.game.data.model.SchoolClique) -> Unit = {},
    onMingleWithSchoolClique: () -> Unit = {},
    onRequestCurfewExtension: () -> Unit = {},
    onCareerMessageDismissed: () -> Unit,
    onPurchaseAsset: (String) -> Unit,
    onSellAsset: (String) -> Unit,
    onRepairAsset: (String) -> Unit,
    onRenovateAsset: (String) -> Unit,
    onRentOutProperty: (String) -> Unit,
    onEvictTenant: (String) -> Unit,
    onSaveWill: (Map<String, Int>?) -> Unit,
    willBeneficiaries: List<com.maisha.game.data.model.Person>,
    onInvestFunds: (Int) -> Unit,
    onWithdrawFunds: (Int) -> Unit,
    onDepositSavings: (Int) -> Unit,
    onWithdrawSavings: (Int) -> Unit,
    onSetLivingStandard: (com.maisha.game.data.model.LivingStandard) -> Unit,
    onSetPortfolioStrategy: (com.maisha.game.data.model.PortfolioStrategy) -> Unit,
    onAssetsMessageDismissed: () -> Unit,
    onAttemptCrime: (CrimeType) -> Unit,
    onAttemptTargetedCrime: (com.maisha.game.data.model.CrimeTarget) -> Unit = {},
    onGoToTrial: (com.maisha.game.data.model.LawyerTier) -> Unit,
    lawyerPublicAffordable: Boolean,
    lawyerAverageFee: Int,
    lawyerAverageAffordable: Boolean,
    lawyerExpensiveFee: Int,
    lawyerExpensiveAffordable: Boolean,
    onVisitDoctor: (String, CareType) -> Unit,
    onSetLifestyleOption: (com.maisha.game.data.model.LifestyleOption, Boolean) -> Unit,
    onExecuteSideHustle: (com.maisha.game.data.model.HustleType) -> Unit,
    onAdoptPet: (com.maisha.game.data.model.PetSpecies) -> Unit,
    onCreateSocialAccount: () -> Unit,
    onPostSocialContent: () -> Unit,
    onMonetizeSocialAccount: () -> Unit,
    onPracticeSkill: (com.maisha.game.data.model.SkillType) -> Unit,
    onTakeMasterclass: (com.maisha.game.data.model.SkillType) -> Unit,
    onShowcaseSkill: (com.maisha.game.data.model.SkillType) -> Unit,
    onAdoptBucketGoal: (String) -> Unit,
    onRenewVisa: () -> Unit,
    onApplyForCitizenship: () -> Unit,
    onTakeDrivingTest: () -> Unit,
    onVolunteer: () -> Unit,
    onDonateToCharity: (Int) -> Unit,
    donationTiers: List<Int>,
    onPerformLeisure: (com.maisha.game.domain.LeisureActivity) -> Unit,
    onPerformStudySession: () -> Unit = {},
    onPerformPrisonActivity: (com.maisha.game.data.model.PrisonActivity) -> Unit = {},
    onAdoptChild: () -> Unit = {},
    onRequestExpungement: () -> Unit = {},
    onWorkPartTime: (com.maisha.game.data.model.PartTimeJob) -> Unit = {},
    onQuitPartTimeJob: () -> Unit = {},
    onRestStudentEnergy: () -> Unit = {},
    onActionMessageDismissed: () -> Unit,
    onLifePulseMessageDismissed: () -> Unit,
    onYearRecapDismissed: () -> Unit,
    onViewCharacterStats: () -> Unit,
    onOpenSettings: () -> Unit,
    onAchievementDialogDismissed: () -> Unit,
    onCelebrationDismissed: () -> Unit,
    onStatDeltaFinished: (Long) -> Unit,
    onFeedbackHandled: () -> Unit,
    onDismissFamilyDatingTip: () -> Unit,
    onDismissFamilyDetailTip: () -> Unit,
    onDismissYearQuestsTip: () -> Unit = {},
    onDismissLeisureTip: () -> Unit = {},
    onDismissAssetsMarketsTip: () -> Unit = {},
    carMakeCatalog: com.maisha.game.data.content.CarMakeCatalog =
        com.maisha.game.data.content.FallbackCarMakeCatalog,
    streetCatalog: com.maisha.game.data.content.StreetCatalog =
        com.maisha.game.data.content.FallbackStreetCatalog,
    onThrowParty: (Int) -> Unit,
    onCampusMixer: () -> Unit = {},
    onProfessorOfficeHours: () -> Unit = {},
    onExamAllNighter: () -> Unit = {},
    onCampusRivalryPrank: () -> Unit = {},
    onPayMortgage: (String) -> Unit = {},
    onOpenWeeklyChallenge: () -> Unit = {},
    onDismissWeeklyChallenge: () -> Unit = {},
    onClaimWeeklyChallenge: () -> Unit = {},
    onWeeklyChallengeClaimSuccessDismissed: () -> Unit = {},
    onWorkPipDeliverable: () -> Unit = {},
    onRebutPipWithHR: () -> Unit = {},
    onLeadClubStrategy: () -> Unit = {},
    onMentorClubJunior: () -> Unit = {},
    onPrepayMortgagePrincipal: (String, Int) -> Unit = { _, _ -> },
    onServiceAsset: (String) -> Unit = {},
    onCaseCrimeTarget: (com.maisha.game.data.model.CrimeTarget) -> Unit = {},
    onLayLowCrime: () -> Unit = {},
    onBribeInformantCrime: () -> Unit = {},
    onPushLuckStep: (PushLuckAction) -> Unit = {},
    onDismissCrimePushLuck: () -> Unit = {},
    onResolveNegotiation: (NegotiationTactics) -> Unit = {},
    onDismissNegotiation: () -> Unit = {},
    onTriggerExamFocusCheck: (String?, String?) -> Unit = { _, _ -> },
    onResolveExamFocus: (ExamFocusTactic, Boolean) -> Unit = { _, _ -> },
    onDismissExamFocus: () -> Unit = {},
    onOpenDoubleLifeSheet: () -> Unit = {},
    onDismissDoubleLifeSheet: () -> Unit = {},
    onAdoptSecretIdentity: (SecretIdentityType, String, String?) -> Unit = { _, _, _ -> },
    onPlantDoubleLifeAlibi: () -> Unit = {},
    onScrubDoubleLifeTracks: () -> Unit = {},
    onExploitDoubleLife: () -> Unit = {},
    onComeCleanDoubleLife: () -> Unit = {},
    onResolveSecretConfrontation: (ConfrontationChoice) -> Unit = {},
    onDismissSecretConfrontation: () -> Unit = {},
    onDismissDoubleLifeMessage: () -> Unit = {},
    onResolveFeudAction: (String, FeudAction) -> Unit = { _, _ -> },
    onCallInBenefactorFavor: (String, BenefactorFavorType) -> Unit = { _, _ -> },
    onReconcileRivalry: (String) -> Unit = {},
    onDismissSocialEcosystemMessage: () -> Unit = {}
) {
    if (uiState.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppLoadingIndicator()
        }
        return
    }

    val character = uiState.character
    if (character == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaishaSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            EmptyStateCard(
                illustration = EmptyStateIllustration.FAMILY,
                title = stringResource(R.string.app_name),
                message = stringResource(R.string.empty_no_character)
            )
        }
        return
    }

    FeedbackEffect(
        cues = uiState.pendingFeedbackCues,
        onHandled = onFeedbackHandled
    )

    if (!character.alive || uiState.navigateToLifeSummary) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppLoadingIndicator()
        }
        return
    }

    uiState.currentCelebration?.let { celebration ->
        CelebrationOverlay(
            type = celebration,
            onDismiss = onCelebrationDismissed
        )
    }

    uiState.currentEvent?.let { event ->
        val queueTotal = uiState.pendingEvents.size + 1
        val queueIndex = queueTotal - uiState.pendingEvents.size
        EventDialog(
            event = event,
            character = character,
            expression = uiState.headerExpression,
            eventQueueIndex = queueIndex,
            eventQueueTotal = queueTotal,
            onChoiceSelected = onChoiceSelected
        )
    }

    if (uiState.currentEvent == null) {
        uiState.currentAchievementDialog?.let { achievement ->
            AchievementUnlockedDialog(
                achievement = achievement,
                countryCode = uiState.character?.countryCode ?: "XX",
                onDismiss = onAchievementDialogDismissed
            )
        }
    }

    if (character.criminalRecord.awaitingTrial) {
        ArrestTrialDialog(
            character = character,
            publicDefenderAffordable = lawyerPublicAffordable,
            averageFee = lawyerAverageFee,
            averageAffordable = lawyerAverageAffordable,
            expensiveFee = lawyerExpensiveFee,
            expensiveAffordable = lawyerExpensiveAffordable,
            onSelectLawyer = onGoToTrial
        )
    }

    val incarcerated = character.criminalRecord.currentlyIncarcerated
    val disabledTabs = if (incarcerated) {
        setOf(MainTab.CAREER, MainTab.ASSETS)
    } else {
        emptySet()
    }

    var selectedTabOrdinal by rememberSaveable { mutableIntStateOf(MainTab.LIFE.ordinal) }
    val selectedTab = MainTab.entries[selectedTabOrdinal.coerceIn(MainTab.entries.indices)]

    LaunchedEffect(incarcerated, selectedTab) {
        if (selectedTab in disabledTabs) {
            selectedTabOrdinal = MainTab.LIFE.ordinal
        }
    }

    val snackbarHostState = rememberFamilySnackbarHostState()
    val prisonBackground = if (incarcerated) NavyElevated else MaterialTheme.colorScheme.background

    LaunchedEffect(uiState.lifePulseMessage) {
        uiState.lifePulseMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onLifePulseMessageDismissed()
        }
    }

    LaunchedEffect(uiState.weeklyChallengeClaimSuccessMessage) {
        uiState.weeklyChallengeClaimSuccessMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onWeeklyChallengeClaimSuccessDismissed()
        }
    }

    LaunchedEffect(uiState.doubleLifeActionMessage) {
        uiState.doubleLifeActionMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onDismissDoubleLifeMessage()
        }
    }

    LaunchedEffect(uiState.socialEcosystemMessage) {
        uiState.socialEcosystemMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onDismissSocialEcosystemMessage()
        }
    }

    var showQuestsSheet by rememberSaveable { mutableStateOf(false) }

    if (showQuestsSheet) {
        YearQuestsSheet(
            quests = uiState.yearQuests,
            progress = uiState.yearQuestProgress,
            weeklyChallenge = uiState.weeklyChallenge,
            weeklyChallengeCompleted = uiState.weeklyChallengeCompleted,
            countryCode = character.countryCode,
            onDismiss = { showQuestsSheet = false }
        )
    }

    if (uiState.showWeeklyChallengeSheet) {
        uiState.weeklyChallengeProgress?.let { progress ->
            WeeklyChallengeSheet(
                progress = progress,
                countryCode = character.countryCode,
                onClaim = onClaimWeeklyChallenge,
                onDismiss = onDismissWeeklyChallenge
            )
        }
    }

    uiState.activeExamFocusChallenge?.let { challenge ->
        ExamFocusDialog(
            challenge = challenge,
            onResolve = onResolveExamFocus,
            onDismiss = onDismissExamFocus
        )
    }

    uiState.activeCrimePushLuck?.let { session ->
        CrimePushLuckSheet(
            session = session,
            countryCode = character.countryCode,
            onAction = onPushLuckStep,
            onDismiss = onDismissCrimePushLuck
        )
    }

    uiState.activeNegotiation?.let { challenge ->
        NegotiationDialog(
            challenge = challenge,
            onResolve = onResolveNegotiation,
            onDismiss = onDismissNegotiation
        )
    }

    if (uiState.showDoubleLifeSheet) {
        DoubleLifeSheet(
            character = character,
            onAdoptSecret = onAdoptSecretIdentity,
            onPlantAlibi = onPlantDoubleLifeAlibi,
            onScrubTracks = onScrubDoubleLifeTracks,
            onExploitSecret = onExploitDoubleLife,
            onComeClean = onComeCleanDoubleLife,
            onDismiss = onDismissDoubleLifeSheet
        )
    }

    uiState.activeSecretConfrontation?.let { confrontation ->
        SecretConfrontationDialog(
            confrontation = confrontation,
            countryCode = character.countryCode,
            onChoice = onResolveSecretConfrontation,
            onDismiss = onDismissSecretConfrontation
        )
    }

    val eventActive = uiState.currentEvent != null
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .background(if (incarcerated) NavyDeep else MaterialTheme.colorScheme.background)
    ) {
        val isWideLayout = maxWidth >= 600.dp
        Scaffold(
            modifier = Modifier.then(
                if (eventActive) Modifier.blur(16.dp) else Modifier
            ),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = prisonBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                GlobalHudHeader(
                    character = character,
                    expression = uiState.headerExpression,
                    netWorth = uiState.netWorth,
                    questCount = uiState.yearQuests.size,
                    questCompleted = uiState.yearQuestProgress.count { it.completed },
                    onViewCharacterStats = onViewCharacterStats,
                    onOpenSettings = onOpenSettings,
                    onOpenQuests = { showQuestsSheet = true }
                )
            },
            bottomBar = {
                if (!isWideLayout && !eventActive) {
                    MaishaBottomNav(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTabOrdinal = it.ordinal },
                        disabledTabs = disabledTabs
                    )
                }
            }
        ) { innerPadding ->
            val tabContent = @Composable {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        NavAnimations.tabEnter togetherWith NavAnimations.tabExit
                    },
                    label = "mainTabCrossfade"
                ) { tab ->
                    when (tab) {
                    MainTab.LIFE -> LifeTabContent(
                        character = character,
                        uiState = uiState,
                        netWorth = uiState.netWorth,
                        onAgeUp = onAgeUp,
                        onViewCharacterStats = onViewCharacterStats,
                        onOpenSettings = onOpenSettings,
                        onOpenWeeklyChallenge = onOpenWeeklyChallenge,
                        onExploreChildhoodAptitude = onExploreChildhoodAptitude,
                        onMingleWithSchoolClique = onMingleWithSchoolClique,
                        onRequestCurfewExtension = onRequestCurfewExtension,
                        onWorkPipDeliverable = onWorkPipDeliverable,
                        onStatDeltaFinished = onStatDeltaFinished,
                        onYearRecapDismissed = onYearRecapDismissed,
                        onDismissYearQuestsTip = onDismissYearQuestsTip,
                        modifier = Modifier.fillMaxSize()
                    )
            MainTab.FAMILY -> FamilyScreen(
                character = character,
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                onMemberClick = onFamilyMemberSelected,
                onMemberDismiss = onFamilyMemberDismissed,
                onPetClick = onPetSelected,
                onPetDismiss = onPetDismissed,
                onPetCare = onPetCare,
                onInteraction = onFamilyInteraction,
                onMessageDismissed = onFamilyInteractionMessageDismissed,
                onFindDate = onFindDate,
                onSeekFriendship = onSeekFriendship,
                onDismissDatingProspects = onDismissDatingProspects,
                onStartDating = onStartDating,
                onMakeExclusive = onMakeExclusive,
                onPropose = onPropose,
                onProposeWithPrenup = onProposeWithPrenup,
                onBreakUp = onBreakUp,
                onHaveChild = onHaveChild,
                onRelationshipMessageDismissed = onRelationshipMessageDismissed,
                onDismissFamilyDatingTip = onDismissFamilyDatingTip,
                onDismissFamilyDetailTip = onDismissFamilyDetailTip,
                onThrowParty = onThrowParty,
                onResolveFeudAction = onResolveFeudAction,
                onCallInBenefactorFavor = onCallInBenefactorFavor,
                onReconcileRivalry = onReconcileRivalry,
                modifier = Modifier.fillMaxSize()
            )
            MainTab.CAREER -> CareerScreen(
                character = character,
                eligibleJobs = uiState.eligibleJobs,
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                onApplyForJob = onApplyForJob,
                onQuitJob = onQuitJob,
                onRetire = onRetire,
                retirementPensionEstimate = retirementPensionEstimate,
                onDropOut = onDropOut,
                onStartBusiness = onStartBusiness,
                onSellBusiness = onSellBusiness,
                investmentTiers = businessInvestmentTiers,
                onLaunchCampaign = onLaunchCampaign,
                onPassTaxPolicy = onPassTaxPolicy,
                onSetWorkEffort = onSetWorkEffort,
                onOfficeAction = onOfficeAction,
                onSetStudyEffort = onSetStudyEffort,
                onSyncExamSchedule = onSyncExamSchedule,
                onJoinSchoolClub = onJoinSchoolClub,
                onPerformClubActivity = onPerformClubActivity,
                onClaimClubMajorEvent = onClaimClubMajorEvent,
                onHostClubFundraiser = onHostClubFundraiser,
                onChallengeRivalSchool = onChallengeRivalSchool,
                onLeaveSchoolClub = onLeaveSchoolClub,
                onPerformSchoolActivity = onPerformSchoolActivity,
                onSchoolPersonInteraction = onSchoolPersonInteraction,
                onWorkplaceAction = onWorkplaceAction,
                onResolveExpulsionHearing = onResolveExpulsionHearing,
                onServeDetention = onServeDetention,
                onApologizeToPrincipal = onApologizeToPrincipal,
                onExamPrepChoice = onExamPrepChoice,
                onEnrollInUniversity = onEnrollInUniversity,
                onCampusJob = onCampusJob,
                onInternship = onUniversityInternship,
                onRepayStudentLoan = onRepayStudentLoan,
                onCampusMixer = onCampusMixer,
                onProfessorOfficeHours = onProfessorOfficeHours,
                onExamAllNighter = onExamAllNighter,
                onCampusRivalryPrank = onCampusRivalryPrank,
                onStartCareerTrack = onStartCareerTrack,
                onPracticeCareerTrack = onPracticeCareerTrack,
                onTrainTalent = onTrainTalent,
                onSignEndorsement = onSignEndorsement,
                onDropEndorsement = onDropEndorsement,
                onLeaveCareerTrack = onLeaveCareerTrack,
                onWorkPartTime = onWorkPartTime,
                onExecuteSideHustle = onExecuteSideHustle,
                onQuitPartTimeJob = onQuitPartTimeJob,
                onRestStudentEnergy = onRestStudentEnergy,
                onWorkPipDeliverable = onWorkPipDeliverable,
                onRebutPipWithHR = onRebutPipWithHR,
                onLeadClubStrategy = onLeadClubStrategy,
                onMentorClubJunior = onMentorClubJunior,
                onExploreChildhoodAptitude = onExploreChildhoodAptitude,
                onJoinSchoolClique = onJoinSchoolClique,
                onMingleWithSchoolClique = onMingleWithSchoolClique,
                onRequestCurfewExtension = onRequestCurfewExtension,
                onTriggerExamFocusCheck = onTriggerExamFocusCheck,
                onCareerMessageDismissed = onCareerMessageDismissed,
                modifier = Modifier.fillMaxSize()
            )
            MainTab.ASSETS -> AssetsScreen(
                character = character,
                netWorth = uiState.netWorth,
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                onPurchaseAsset = onPurchaseAsset,
                onSellAsset = onSellAsset,
                onRepairAsset = onRepairAsset,
                onRenovateAsset = onRenovateAsset,
                onRentOutProperty = onRentOutProperty,
                onEvictTenant = onEvictTenant,
                onSaveWill = onSaveWill,
                willBeneficiaries = willBeneficiaries,
                onInvestFunds = onInvestFunds,
                onWithdrawFunds = onWithdrawFunds,
                onDepositSavings = onDepositSavings,
                onWithdrawSavings = onWithdrawSavings,
                onSetLivingStandard = onSetLivingStandard,
                onSetPortfolioStrategy = onSetPortfolioStrategy,
                onAssetsMessageDismissed = onAssetsMessageDismissed,
                onDismissAssetsMarketsTip = onDismissAssetsMarketsTip,
                onPerformLeisure = onPerformLeisure,
                onPayMortgage = onPayMortgage,
                onPrepayMortgagePrincipal = onPrepayMortgagePrincipal,
                onServiceAsset = onServiceAsset,
                carMakeCatalog = carMakeCatalog,
                streetCatalog = streetCatalog,
                modifier = Modifier.fillMaxSize()
            )
            MainTab.ACTIONS -> ActionsScreen(
                character = character,
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                onAttemptCrime = onAttemptCrime,
                onAttemptTargetedCrime = onAttemptTargetedCrime,
                onCaseCrimeTarget = onCaseCrimeTarget,
                onLayLowCrime = onLayLowCrime,
                onBribeInformantCrime = onBribeInformantCrime,
                onVisitDoctor = onVisitDoctor,
                onSetLifestyleOption = onSetLifestyleOption,
                onExecuteSideHustle = onExecuteSideHustle,
                onAdoptPet = onAdoptPet,
                onCreateSocialAccount = onCreateSocialAccount,
                onPostSocialContent = onPostSocialContent,
                onMonetizeSocialAccount = onMonetizeSocialAccount,
                onPracticeSkill = onPracticeSkill,
                onShowcaseSkill = onShowcaseSkill,
                onAdoptBucketGoal = onAdoptBucketGoal,
                onTakeMasterclass = onTakeMasterclass,
                onRenewVisa = onRenewVisa,
                onApplyForCitizenship = onApplyForCitizenship,
                onTakeDrivingTest = onTakeDrivingTest,
                onVolunteer = onVolunteer,
                onDonateToCharity = onDonateToCharity,
                donationTiers = donationTiers,
                onPerformLeisure = onPerformLeisure,
                onPerformStudySession = onPerformStudySession,
                onPerformPrisonActivity = onPerformPrisonActivity,
                onAdoptChild = onAdoptChild,
                onRequestExpungement = onRequestExpungement,
                onWorkPartTime = onWorkPartTime,
                onQuitPartTimeJob = onQuitPartTimeJob,
                onRestStudentEnergy = onRestStudentEnergy,
                onTrainTalent = onTrainTalent,
                onSignEndorsement = onSignEndorsement,
                onDropEndorsement = onDropEndorsement,
                onOpenDoubleLife = onOpenDoubleLifeSheet,
                onActionMessageDismissed = onActionMessageDismissed,
                onDismissLeisureTip = onDismissLeisureTip,
                modifier = Modifier.fillMaxSize()
            )
            }
        }
    }

        if (isWideLayout) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LeftNavRail(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTabOrdinal = it.ordinal },
                    disabledTabs = disabledTabs
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    tabContent()
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                tabContent()
            }
        }
    }
}
}

private data class YearBlock(
    val age: Int,
    val entries: List<YearLogItem>
)

private data class YearLogItem(
    val text: String,
    val tone: EventLogTone,
    val category: EventLogCategory = EventLogCategory.GENERAL,
    val deltaBadge: String? = null
)

private fun parseYearBlocks(entries: List<String>, currentAge: Int): List<YearBlock> {
    val blocks = mutableListOf<YearBlock>()
    var curAge = currentAge
    var curEntries = mutableListOf<YearLogItem>()

    for (raw in entries) {
        val trimmed = raw.trim()
        if (trimmed.startsWith("── Age ") || (trimmed.startsWith("Age ") && trimmed.length <= 8 && trimmed.any { it.isDigit() })) {
            if (curEntries.isNotEmpty()) {
                blocks.add(YearBlock(age = curAge, entries = curEntries.toList()))
                curEntries.clear()
            }
            val parsedAge = trimmed.filter { it.isDigit() }.toIntOrNull() ?: curAge
            curAge = parsedAge
        } else if (trimmed.isNotEmpty()) {
            val tone = EventLogClassifier.classify(trimmed)
            val category = EventLogClassifier.categorize(trimmed)
            val deltaMatch = Regex("""\(([+\-]?\d+[^)]+)\)""").find(trimmed)
            val deltaBadge = deltaMatch?.groups?.get(1)?.value
            val cleanText = if (deltaBadge != null) {
                trimmed.replace("($deltaBadge)", "").trim().trimEnd(',', ';')
            } else {
                trimmed
            }
            curEntries.add(
                YearLogItem(
                    text = cleanText,
                    tone = tone,
                    category = category,
                    deltaBadge = deltaBadge
                )
            )
        }
    }
    if (curEntries.isNotEmpty()) {
        blocks.add(YearBlock(age = curAge, entries = curEntries.toList()))
    }
    return blocks
}

@Composable
@Suppress("UNUSED_PARAMETER")
private fun LifeTabContent(
    character: Character,
    uiState: LifeUiState,
    netWorth: Int,
    onAgeUp: () -> Unit,
    onViewCharacterStats: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenWeeklyChallenge: () -> Unit = {},
    onExploreChildhoodAptitude: (ChildhoodAptitude) -> Unit = {},
    onMingleWithSchoolClique: () -> Unit = {},
    onRequestCurfewExtension: () -> Unit = {},
    onWorkPipDeliverable: () -> Unit = {},
    onStatDeltaFinished: (Long) -> Unit,
    onYearRecapDismissed: () -> Unit,
    onDismissYearQuestsTip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val journalEntries = remember(character.eventLog) {
        character.eventLog.filterNot { it.startsWith("::DEATH:") }
    }
    var journalExpanded by rememberSaveable { mutableStateOf(false) }
    val visibleJournal = if (journalExpanded || journalEntries.size <= JOURNAL_VISIBLE_CAP) {
        journalEntries
    } else {
        journalEntries.take(JOURNAL_VISIBLE_CAP)
    }
    val hiddenJournalCount = (journalEntries.size - JOURNAL_VISIBLE_CAP).coerceAtLeast(0)

    val yearBlocks = remember(visibleJournal, character.age) {
        parseYearBlocks(visibleJournal, character.age)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBg)
    ) {
        val listState = rememberLazyListState()
        LaunchedEffect(character.age, character.eventLog.size) {
            if (character.eventLog.isNotEmpty()) {
                listState.animateScrollToItem(0)
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                if (uiState.yearRecapLines.isNotEmpty()) {
                    item {
                        YearRecapCard(
                            lines = uiState.yearRecapLines,
                            onDismiss = onYearRecapDismissed
                        )
                    }
                }

                uiState.weeklyChallengeProgress?.let { progress ->
                    item {
                        WeeklyChallengeCard(
                            progress = progress,
                            onClick = onOpenWeeklyChallenge
                        )
                    }
                }

                uiState.character?.let { character ->
                    item {
                        ContextualThisYearStrip(
                            character = character,
                            weeklyChallengeProgress = uiState.weeklyChallengeProgress,
                            onOpenWeeklyChallenge = onOpenWeeklyChallenge,
                            onExploreChildhoodAptitude = onExploreChildhoodAptitude,
                            onMingleWithSchoolClique = onMingleWithSchoolClique,
                            onRequestCurfewExtension = onRequestCurfewExtension,
                            onWorkPipDeliverable = onWorkPipDeliverable,
                            onViewCharacterStats = onViewCharacterStats
                        )
                    }
                }

                if (journalEntries.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.empty_event_log_life),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                        )
                    }
                } else {
                    items(yearBlocks) { block ->
                        YearTimelineCard(yearBlock = block)
                    }
                    if (!journalExpanded && hiddenJournalCount > 0) {
                        item {
                            TextButton(onClick = { journalExpanded = true }) {
                                Text(
                                    text = stringResource(
                                        R.string.btn_show_earlier_years,
                                        hiddenJournalCount
                                    )
                                )
                            }
                        }
                    } else if (journalExpanded && journalEntries.size > JOURNAL_VISIBLE_CAP) {
                        item {
                            TextButton(onClick = { journalExpanded = false }) {
                                Text(text = stringResource(R.string.btn_collapse_earlier_years))
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(6.dp)) }
            }

            FloatingStatChangeLayer(
                events = uiState.pendingStatDeltas,
                onEventFinished = onStatDeltaFinished,
                statLabel = { type ->
                    when (type) {
                        StatType.HEALTH -> stringResource(R.string.stat_health)
                        StatType.HAPPINESS -> stringResource(R.string.stat_happiness)
                        StatType.SMARTS -> stringResource(R.string.stat_smarts)
                        StatType.LOOKS -> stringResource(R.string.stat_looks)
                        StatType.MONEY -> stringResource(R.string.stat_money)
                        StatType.NET_WORTH -> stringResource(R.string.label_net_worth)
                        StatType.FOLLOWERS -> stringResource(R.string.stat_followers)
                        StatType.SKILL -> stringResource(R.string.stat_skill)
                        StatType.KARMA -> stringResource(R.string.stat_karma)
                        else -> ""
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = MaishaSpacing.sm, end = MaishaSpacing.sm)
            )
        }

        // Docked bottom control deck with stat strip and 3D tactile Age Up button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CanvasBg)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DockedStatStrip(
                happiness = character.stats.happiness,
                health = character.stats.health,
                smarts = character.stats.smarts,
                looks = character.stats.looks
            )
            AgeUpButton(
                onClick = onAgeUp,
                enabled = character.alive &&
                    !uiState.isAgingUp &&
                    uiState.currentEvent == null &&
                    !character.criminalRecord.awaitingTrial,
                isLoading = uiState.isAgingUp
            )
        }
    }
}

private const val JOURNAL_VISIBLE_CAP = 30

@Composable
private fun YearTimelineCard(
    yearBlock: YearBlock,
    modifier: Modifier = Modifier
) {
    val hasMilestone = remember(yearBlock) {
        yearBlock.entries.any { it.category == EventLogCategory.MILESTONE || it.tone == EventLogTone.MILESTONE }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(
            width = if (hasMilestone) 1.5.dp else 1.dp,
            color = if (hasMilestone) GoldAccent.copy(alpha = 0.5f) else BorderCardSlate
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (hasMilestone) 3.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (hasMilestone) GoldAccent else DeepSlate)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "AGE ${yearBlock.age}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                if (hasMilestone) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GoldAccent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = stringResource(R.string.timeline_tag_milestone),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = if (hasMilestone) GoldAccent.copy(alpha = 0.3f) else Hairline
                )
            }

            yearBlock.entries.forEach { entry ->
                val isMilestone = entry.category == EventLogCategory.MILESTONE || entry.tone == EventLogTone.MILESTONE
                val isDrama = entry.category == EventLogCategory.CRIME_DRAMA

                if (isMilestone) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = GoldAccent.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.40f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = AppIcons.Achievements,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = entry.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (!entry.deltaBadge.isNullOrBlank()) {
                                TimelineDeltaBadge(entry.deltaBadge)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val (iconVector, iconColor) = when (entry.category) {
                            EventLogCategory.CRIME_DRAMA -> AppIcons.Mischief to CoralNegative
                            EventLogCategory.CAREER_SCHOOL -> AppIcons.Career to TealPrimary
                            EventLogCategory.FAMILY_ROMANCE -> AppIcons.Family to StatHappiness
                            EventLogCategory.HEALTH_WELLNESS -> AppIcons.Health to StatHealth
                            else -> null to when (entry.tone) {
                                EventLogTone.POSITIVE -> Color(0xFF10B981)
                                EventLogTone.NEGATIVE -> CoralNegative
                                else -> Color(0xFF64748B)
                            }
                        }

                        if (iconVector != null) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(top = 2.dp)
                            )
                        } else {
                            Text(
                                text = "•",
                                color = iconColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 1.dp)
                            )
                        }

                        Text(
                            text = entry.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDrama) CoralNegative else InkPrimary,
                            fontWeight = if (isDrama) FontWeight.Medium else FontWeight.Normal,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )

                        if (!entry.deltaBadge.isNullOrBlank()) {
                            TimelineDeltaBadge(entry.deltaBadge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineDeltaBadge(deltaBadge: String) {
    val isPositive = !deltaBadge.startsWith("-")
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isPositive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = deltaBadge,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) Color(0xFF15803D) else Color(0xFFB91C1C),
            fontSize = 10.sp
        )
    }
}

private data class ThisYearFocusItem(
    val icon: ImageVector,
    val titleRes: Int,
    val descRes: Int,
    val accent: Color,
    val onClick: () -> Unit
)

@Composable
private fun ContextualThisYearStrip(
    character: Character,
    weeklyChallengeProgress: com.maisha.game.domain.WeeklyChallengeProgress?,
    onOpenWeeklyChallenge: () -> Unit,
    onExploreChildhoodAptitude: (ChildhoodAptitude) -> Unit,
    onMingleWithSchoolClique: () -> Unit,
    onRequestCurfewExtension: () -> Unit,
    onWorkPipDeliverable: () -> Unit,
    onViewCharacterStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember(character, weeklyChallengeProgress) {
        val list = mutableListOf<ThisYearFocusItem>()
        if (character.stats.health < 40) {
            list.add(
                ThisYearFocusItem(
                    icon = AppIcons.Health,
                    titleRes = R.string.strip_action_health_title,
                    descRes = R.string.strip_action_health_desc,
                    accent = CoralNegative,
                    onClick = onViewCharacterStats
                )
            )
        }
        if (character.stats.happiness < 30) {
            list.add(
                ThisYearFocusItem(
                    icon = AppIcons.Happiness,
                    titleRes = R.string.strip_action_happiness_title,
                    descRes = R.string.strip_action_happiness_desc,
                    accent = CoralNegative,
                    onClick = onViewCharacterStats
                )
            )
        }
        if (character.career.onPip) {
            list.add(
                ThisYearFocusItem(
                    icon = AppIcons.Career,
                    titleRes = R.string.strip_action_pip_title,
                    descRes = R.string.strip_action_pip_desc,
                    accent = CoralNegative,
                    onClick = onWorkPipDeliverable
                )
            )
        }
        if (character.age in 4..10 && !character.education.aptitudeExploredThisYear) {
            val aptitudeToExplore = if (character.education.childhoodAptitude != ChildhoodAptitude.NONE) {
                character.education.childhoodAptitude
            } else {
                ChildhoodAptitude.ATHLETICS
            }
            list.add(
                ThisYearFocusItem(
                    icon = AppIcons.Smarts,
                    titleRes = R.string.strip_action_aptitude_title,
                    descRes = R.string.strip_action_aptitude_desc,
                    accent = TealPrimary,
                    onClick = { onExploreChildhoodAptitude(aptitudeToExplore) }
                )
            )
        }
        if (character.age in 11..14 && character.education.cliqueStanding < 60) {
            list.add(
                ThisYearFocusItem(
                    icon = AppIcons.Family,
                    titleRes = R.string.strip_action_clique_title,
                    descRes = R.string.strip_action_clique_desc,
                    accent = TealPrimary,
                    onClick = onMingleWithSchoolClique
                )
            )
        }
        if (character.age in 15..17 && character.education.curfewHour < 24) {
            list.add(
                ThisYearFocusItem(
                    icon = AppIcons.Longevity,
                    titleRes = R.string.strip_action_curfew_title,
                    descRes = R.string.strip_action_curfew_desc,
                    accent = TealPrimary,
                    onClick = onRequestCurfewExtension
                )
            )
        }
        if (character.age == 18) {
            list.add(
                ThisYearFocusItem(
                    icon = AppIcons.Achievements,
                    titleRes = R.string.strip_action_adulthood_title,
                    descRes = R.string.strip_action_adulthood_desc,
                    accent = GoldAccent,
                    onClick = onViewCharacterStats
                )
            )
        }
        if (weeklyChallengeProgress != null && !weeklyChallengeProgress.isAllCompleted) {
            list.add(
                ThisYearFocusItem(
                    icon = AppIcons.Achievements,
                    titleRes = R.string.strip_action_challenge_title,
                    descRes = R.string.strip_action_challenge_desc,
                    accent = GoldAccent,
                    onClick = onOpenWeeklyChallenge
                )
            )
        }
        list.take(2)
    }

    if (items.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, BorderCardSlate),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(TealPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AppIcons.NavLife,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.strip_this_year_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${items.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            items.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(thickness = 0.5.dp, color = HairlineSoft)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = item.onClick)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(item.accent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = item.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(item.titleRes),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(item.descRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = InkTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GlobalHudHeader(
    character: Character,
    expression: Expression,
    netWorth: Int,
    questCount: Int,
    questCompleted: Int,
    onViewCharacterStats: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenQuests: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderCardSlate)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(CreamMuted)
                    .border(1.5.dp, LifeGreenSoft, CircleShape)
                    .clickable(onClick = onViewCharacterStats),
                contentAlignment = Alignment.Center
            ) {
                PersonAvatar(
                    avatarConfig = character.avatarConfig,
                    size = 42,
                    age = character.age,
                    expression = expression,
                    forPlayerCharacter = true,
                    hideFromAccessibility = true,
                    seed = "${character.gender.name}-${character.name}"
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        modifier = Modifier.weight(1f, fill = false),
                        text = character.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    CountryFlag(countryCode = character.countryCode, size = 14.dp)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CreamMuted)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.format_age, character.age),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary,
                            fontSize = 11.sp
                        )
                    }
                }

                val chapterKey = remember(
                    character.age,
                    character.career.currentJob,
                    character.education.stage,
                    character.criminalRecord.notoriety,
                    character.education.childhoodAptitude,
                    character.education.schoolClique,
                    netWorth
                ) {
                    LifeArchetypeEngine.resolveChapterKey(character, netWorth)
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
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = formatMoney(character.stats.money, character.countryCode),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = StatMoney
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkTertiary
                    )
                    Text(
                        modifier = Modifier.weight(1f, fill = false),
                        text = stringResource(R.string.label_net_worth) + ": " + formatMoney(netWorth, character.countryCode),
                        style = MaterialTheme.typography.labelSmall,
                        color = InkSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (questCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldAccent.copy(alpha = 0.14f))
                        .clickable(onClick = onOpenQuests)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🎯", fontSize = 12.sp)
                        Text(
                            text = "$questCompleted/$questCount",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            IconButton(onClick = onOpenSettings, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = stringResource(R.string.content_desc_settings),
                    tint = InkSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun YearLogHeader(age: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(LifeGreen.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = stringResource(R.string.format_age, age).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = LifeGreen,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Hairline)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearQuestsSheet(
    quests: List<YearQuest>,
    progress: List<YearQuestProgress>,
    countryCode: String,
    weeklyChallenge: com.maisha.game.domain.WeeklyChallenge? = null,
    weeklyChallengeCompleted: Boolean = false,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (weeklyChallenge != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (weeklyChallengeCompleted) LifeGreenSoft.copy(alpha = 0.5f) else GoldAccent.copy(alpha = 0.12f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (weeklyChallengeCompleted) LifeGreenSoft else GoldAccent.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "🏆", fontSize = 16.sp)
                                Text(
                                    text = stringResource(R.string.section_weekly_challenge),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                            }
                            Text(
                                text = if (weeklyChallengeCompleted) stringResource(R.string.challenge_status_completed) else stringResource(R.string.challenge_status_in_progress),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (weeklyChallengeCompleted) LifeGreen else InkSecondary
                            )
                        }
                        Text(
                            text = resolveChallengeTitle(weeklyChallenge.titleKey),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = resolveChallengeDesc(weeklyChallenge.descriptionKey),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.section_year_quests),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )
                Text(
                    text = "🎯 ${progress.count { it.completed }}/${quests.size}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
            }

            if (quests.isEmpty()) {
                Text(
                    text = stringResource(R.string.tip_year_quests),
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSecondary
                )
            } else {
                quests.forEach { quest ->
                    val match = progress.firstOrNull { it.quest.kind == quest.kind }
                    val current = match?.current ?: 0
                    val done = match?.completed == true
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (done) LifeGreenSoft.copy(alpha = 0.5f) else CreamMuted
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (done) LifeGreenSoft else Hairline)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = yearQuestTitle(quest, countryCode),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = InkPrimary
                                )
                                Text(
                                    text = if (done) stringResource(R.string.year_quest_complete_badge) else stringResource(R.string.year_quest_progress, current.coerceAtMost(quest.target), quest.target),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (done) LifeGreen else InkSecondary
                                )
                            }
                            if (!done) {
                                LinearProgressIndicator(
                                    progress = { (current.toFloat() / quest.target.coerceAtLeast(1)).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = GoldAccent,
                                    trackColor = Hairline
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
private fun HeroChip(text: String, bg: Color, fg: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(bg)
            .border(1.dp, Hairline, RoundedCornerShape(99.dp))
            .padding(horizontal = 9.dp, vertical = 3.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun dynastyTitleLabel(titleKey: String): String {

    val resId = when (titleKey) {
        "dynasty_title_seedling" -> R.string.dynasty_title_seedling
        "dynasty_title_rooted" -> R.string.dynasty_title_rooted
        "dynasty_title_rising" -> R.string.dynasty_title_rising
        "dynasty_title_powerhouse" -> R.string.dynasty_title_powerhouse
        "dynasty_title_legend" -> R.string.dynasty_title_legend
        else -> R.string.dynasty_title_seedling
    }
    return stringResource(resId)
}

@Composable
private fun CharacterHeader(
    character: Character,
    expression: Expression,
    onViewCharacterStats: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val genderLabel = when (character.gender) {
        Gender.MALE -> stringResource(R.string.gender_male)
        Gender.FEMALE -> stringResource(R.string.gender_female)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PersonAvatar(
            avatarConfig = character.avatarConfig,
            size = 52,
            age = character.age,
            expression = expression,
            forPlayerCharacter = true,
            // Include gender in the dice seed so the face changes when gender changes.
            seed = "${character.gender.name}-${character.name}"
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CountryFlag(countryCode = character.countryCode, size = 18.dp)
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stringResource(
                    R.string.format_character_birth_info,
                    genderLabel,
                    character.birthYear
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onViewCharacterStats,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = stringResource(R.string.content_desc_view_full_life),
                tint = GoldAccent,
                modifier = Modifier.size(28.dp)
            )
        }

        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.content_desc_settings),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }

        Card(
            shape = MaishaRadius.buttonShape,
            colors = CardDefaults.cardColors(containerColor = TealPrimary)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = MaishaSpacing.md + 2.dp, vertical = MaishaSpacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = character.age.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = stringResource(R.string.label_years),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun StatsCard(stats: Stats, netWorth: Int, countryCode: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaishaRadius.cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(MaishaSpacing.md + 2.dp),
            verticalArrangement = Arrangement.spacedBy(MaishaSpacing.sm + 2.dp)
        ) {
            StatBar(type = StatType.HEALTH, value = stats.health)
            StatBar(type = StatType.HAPPINESS, value = stats.happiness)
            StatBar(type = StatType.SMARTS, value = stats.smarts)
            StatBar(type = StatType.LOOKS, value = stats.looks)
            StatBar(type = StatType.KARMA, value = stats.karma)
            MoneyStatRow(amount = stats.money, countryCode = countryCode)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.label_net_worth),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatMoney(netWorth, countryCode),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusInfoCard(
    character: Character,
    education: EducationState,
    career: CareerState,
    netWorth: Int,
    countryCode: String,
    hasCriminalRecord: Boolean,
    timesArrested: Int
) {
    val resources = LocalContext.current.resources
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Hairline, MaishaRadius.cardShape),
        shape = MaishaRadius.cardShape,
        colors = CardDefaults.cardColors(
            containerColor = PaperWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {

        Column(
            modifier = Modifier.padding(MaishaSpacing.md + 2.dp),
            verticalArrangement = Arrangement.spacedBy(MaishaSpacing.sm)
        ) {
            StatusInfoRow(
                icon = { MaishaIcon(AppIcons.Wealth, contentDescription = null, size = 22.dp) },
                label = stringResource(R.string.label_cash),
                value = formatMoney(character.stats.money, countryCode),
                valueColor = GoldAccent
            )
            StatusInfoRow(
                icon = { MaishaIcon(AppIcons.Wealth, contentDescription = null, size = 22.dp) },
                label = stringResource(R.string.label_net_worth),
                value = formatMoney(netWorth, countryCode)
            )
            StatusInfoRow(
                icon = { MaishaIcon(AppIcons.Education, contentDescription = null, size = 22.dp) },
                label = stringResource(R.string.label_education),
                value = EducationFormatter.formatStatus(education, resources, countryCode)
            )
            if (education.gpa > 0f && education.stage != SchoolStage.NONE &&
                education.stage != SchoolStage.GRADUATED
            ) {
                Text(
                    text = stringResource(R.string.format_gpa, education.gpa),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 28.dp)
                )
            }
            StatusInfoRow(
                icon = { MaishaIcon(AppIcons.Career, contentDescription = null, size = 22.dp) },
                label = stringResource(R.string.label_career),
                value = CareerFormatter.formatStatus(career, resources)
            )
            if (hasCriminalRecord) {
                RecordBadge(timesArrested = timesArrested)
            }
            Text(
                text = stringResource(R.string.section_lifestyle_pulse),
                style = MaterialTheme.typography.labelSmall,
                color = InkTertiary,
                fontWeight = FontWeight.Bold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HeroChip(
                    text = livingStandardLabel(character.lifestyle.livingStandard),
                    bg = LifeGreen.copy(alpha = 0.12f),
                    fg = LifeGreen
                )
                if (character.lifestyle.hasHealthInsurance) {
                    HeroChip(
                        text = stringResource(R.string.chip_lifestyle_insurance),
                        bg = TealPrimary.copy(alpha = 0.12f),
                        fg = TealPrimary
                    )
                }
                if (character.lifestyle.hasGymMembership) {
                    HeroChip(
                        text = stringResource(R.string.chip_lifestyle_gym),
                        bg = TealPrimary.copy(alpha = 0.12f),
                        fg = TealPrimary
                    )
                }
                if (character.lifestyle.hasTherapist) {
                    HeroChip(
                        text = stringResource(R.string.chip_lifestyle_therapist),
                        bg = TealPrimary.copy(alpha = 0.12f),
                        fg = TealPrimary
                    )
                }
                if (character.isLivingAbroad()) {
                    HeroChip(
                        text = if (character.currentVisa != null) {
                            stringResource(
                                R.string.format_visa_chip,
                                character.visaYearsRemaining
                            )
                        } else {
                            stringResource(R.string.chip_abroad_citizen_flavor)
                        },
                        bg = TealPrimary.copy(alpha = 0.18f),
                        fg = TealPrimary
                    )
                    if (character.yearsInCurrentCountry > 0) {
                        HeroChip(
                            text = stringResource(
                                R.string.format_years_in_country,
                                character.yearsInCurrentCountry
                            ),
                            bg = TealPrimary.copy(alpha = 0.1f),
                            fg = TealPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun livingStandardLabel(standard: LivingStandard): String = when (standard) {
    LivingStandard.FRUGAL -> stringResource(R.string.living_standard_frugal)
    LivingStandard.MODEST -> stringResource(R.string.living_standard_modest)
    LivingStandard.COMFORTABLE -> stringResource(R.string.living_standard_comfortable)
    LivingStandard.LUXURY -> stringResource(R.string.living_standard_luxury)
}

@Composable
private fun StatusInfoRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        icon()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = valueColor,
                fontWeight = if (valueColor != Color.Unspecified) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EventLogStrip(entry: String, ageTag: Int) {
    val tone = remember(entry) { EventLogClassifier.classify(entry) }
    val dot = when (tone) {
        EventLogTone.MILESTONE -> GoldAccent
        EventLogTone.POSITIVE -> SuccessGreen
        EventLogTone.NEGATIVE -> CoralNegative
        EventLogTone.NEUTRAL -> InkTertiary
    }
    val isMilestone = tone == EventLogTone.MILESTONE

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        if (ageTag >= 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.format_age, ageTag).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = InkTertiary,
                    letterSpacing = 0.8.sp
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(HairlineSoft)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isMilestone) {
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldAccent.copy(alpha = 0.10f))
                            .border(
                                width = 1.dp,
                                color = GoldAccent.copy(alpha = 0.40f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    } else {
                        Modifier.padding(vertical = 4.dp)
                    }
                ),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (isMilestone) 9.dp else 7.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(dot)
            )
            Text(
                text = entry,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isMilestone) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isMilestone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun YearQuestsCard(
    quests: List<YearQuest>,
    progress: List<YearQuestProgress>,
    countryCode: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Hairline, MaishaRadius.cardShape),
        shape = MaishaRadius.cardShape,
        colors = CardDefaults.cardColors(
            containerColor = PaperWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {

        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.section_year_quests),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = InkPrimary
            )
            quests.forEach { quest ->
                val match = progress.firstOrNull { it.quest.kind == quest.kind }
                val current = match?.current ?: 0
                val done = match?.completed == true
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = yearQuestTitle(quest, countryCode),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (done) {
                                stringResource(R.string.year_quest_complete_badge)
                            } else {
                                stringResource(
                                    R.string.year_quest_progress,
                                    current.coerceAtMost(quest.target),
                                    quest.target
                                )
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (done) SuccessGreen else TealPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    StatBar(
                        type = StatType.PERFORMANCE,
                        value = current.coerceAtMost(quest.target),
                        maxValue = quest.target.coerceAtLeast(1),
                        label = "",
                        showIcon = false
                    )
                }
            }
        }
    }
}

@Composable
private fun yearQuestTitle(quest: YearQuest, countryCode: String): String = when (quest.kind) {
    YearQuestKind.RAISE_HAPPINESS ->
        stringResource(R.string.year_quest_raise_happiness, quest.target)
    YearQuestKind.RAISE_HEALTH ->
        stringResource(R.string.year_quest_raise_health, quest.target)
    YearQuestKind.EARN_MONEY ->
        stringResource(R.string.year_quest_earn_money, formatMoney(quest.target, countryCode))
    YearQuestKind.STUDY_SMARTS ->
        stringResource(R.string.year_quest_study_smarts, quest.target)
    YearQuestKind.BOND_FAMILY ->
        stringResource(R.string.year_quest_bond_family)
    YearQuestKind.STAY_OUT_OF_TROUBLE ->
        stringResource(R.string.year_quest_stay_clean)
    YearQuestKind.GROW_FOLLOWERS ->
        stringResource(R.string.year_quest_grow_followers, quest.target)
    YearQuestKind.RAISE_SKILL ->
        stringResource(R.string.year_quest_raise_skill, quest.target)
    YearQuestKind.HOLD_JOB ->
        stringResource(R.string.year_quest_hold_job)
    YearQuestKind.GROW_SAVINGS ->
        stringResource(
            R.string.year_quest_grow_savings,
            formatMoney(quest.target, countryCode)
        )
}

@Composable
private fun resolveChallengeTitle(key: String): String = when (key) {
    "challenge_medical_title" -> stringResource(R.string.challenge_medical_title)
    "challenge_legal_title" -> stringResource(R.string.challenge_legal_title)
    "challenge_adopt_title" -> stringResource(R.string.challenge_adopt_title)
    "challenge_renovate_title" -> stringResource(R.string.challenge_renovate_title)
    "challenge_expunge_title" -> stringResource(R.string.challenge_expunge_title)
    "challenge_scholar_title" -> stringResource(R.string.challenge_scholar_title)
    "challenge_teen_job_title" -> stringResource(R.string.challenge_teen_job_title)
    "challenge_portfolio_title" -> stringResource(R.string.challenge_portfolio_title)
    else -> key
}

@Composable
private fun resolveChallengeDesc(key: String): String = when (key) {
    "challenge_medical_desc" -> stringResource(R.string.challenge_medical_desc)
    "challenge_legal_desc" -> stringResource(R.string.challenge_legal_desc)
    "challenge_adopt_desc" -> stringResource(R.string.challenge_adopt_desc)
    "challenge_renovate_desc" -> stringResource(R.string.challenge_renovate_desc)
    "challenge_expunge_desc" -> stringResource(R.string.challenge_expunge_desc)
    "challenge_scholar_desc" -> stringResource(R.string.challenge_scholar_desc)
    "challenge_teen_job_desc" -> stringResource(R.string.challenge_teen_job_desc)
    "challenge_portfolio_desc" -> stringResource(R.string.challenge_portfolio_desc)
    else -> key
}

@Composable
private fun YearFocusStrip(
    quests: List<YearQuest>,
    progress: List<YearQuestProgress>
) {
    val incomplete = quests.filter { quest ->
        progress.none { it.quest.kind == quest.kind && it.quest.target == quest.target && it.completed }
    }
    val focusQuest = incomplete.firstOrNull() ?: return
    val hintRes = when (focusQuest.kind) {
        YearQuestKind.RAISE_HAPPINESS,
        YearQuestKind.RAISE_HEALTH,
        YearQuestKind.STAY_OUT_OF_TROUBLE -> R.string.year_focus_actions
        YearQuestKind.BOND_FAMILY -> R.string.year_focus_family
        YearQuestKind.STUDY_SMARTS,
        YearQuestKind.HOLD_JOB,
        YearQuestKind.RAISE_SKILL -> R.string.year_focus_career
        YearQuestKind.EARN_MONEY,
        YearQuestKind.GROW_SAVINGS,
        YearQuestKind.GROW_FOLLOWERS -> R.string.year_focus_assets
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = stringResource(R.string.section_year_focus),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = InkTertiary,
        letterSpacing = 0.8.sp
    )
    Text(
        text = stringResource(hintRes),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
    )
}

@Composable
private fun YearRecapCard(lines: List<String>, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaishaRadius.cardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.section_year_recap),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = InkPrimary
                )
                Text(
                    text = stringResource(R.string.btn_dismiss_year_recap),
                    style = MaterialTheme.typography.labelSmall,
                    color = TealPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            lines.forEach { line ->
                Text(
                    text = "• $line",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkPrimary
                )
            }
        }
    }
}

@Composable
private fun BucketSpotlightRow(kind: BucketGoalKind) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaishaRadius.cardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.section_bucket_spotlight),
                    style = MaterialTheme.typography.labelSmall,
                    color = InkTertiary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = bucketSpotlightTitle(kind),
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            HeroChip(
                text = stringResource(R.string.bucket_spotlight_pending),
                bg = GoldAccent.copy(alpha = 0.18f),
                fg = GoldAccent
            )
        }
    }
}

@Composable
private fun bucketSpotlightTitle(kind: BucketGoalKind): String = when (kind) {
    BucketGoalKind.OWN_HOME -> stringResource(R.string.bucket_own_home_title)
    BucketGoalKind.REACH_FAME -> stringResource(R.string.bucket_reach_fame_title)
    BucketGoalKind.START_BUSINESS -> stringResource(R.string.bucket_start_business_title)
    BucketGoalKind.WIN_OFFICE -> stringResource(R.string.bucket_win_office_title)
    BucketGoalKind.RAISE_CHILD -> stringResource(R.string.bucket_raise_child_title)
    BucketGoalKind.HIT_WEALTH -> stringResource(R.string.bucket_hit_wealth_title)
    BucketGoalKind.MASTER_SKILL -> stringResource(R.string.bucket_master_skill_title)
}

@Composable
private fun RecentMilestonesRow(milestoneIds: List<String>) {
    val recent = milestoneIds.takeLast(4).reversed()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.section_recent_milestones),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = InkTertiary,
            letterSpacing = 0.8.sp
        )
        recent.forEach { id ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "✓",
                    color = GoldAccent,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = milestoneTitle(id),
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun milestoneTitle(id: String): String = when (id) {
    com.maisha.game.domain.MilestoneEngine.ID_AGE_18 -> stringResource(R.string.milestone_age_18)
    com.maisha.game.domain.MilestoneEngine.ID_AGE_50 -> stringResource(R.string.milestone_age_50)
    com.maisha.game.domain.MilestoneEngine.ID_AGE_75 -> stringResource(R.string.milestone_age_75)
    com.maisha.game.domain.MilestoneEngine.ID_AGE_100 -> stringResource(R.string.milestone_age_100)
    com.maisha.game.domain.MilestoneEngine.ID_FIRST_JOB -> stringResource(R.string.milestone_first_job)
    com.maisha.game.domain.MilestoneEngine.ID_DRIVING -> stringResource(R.string.milestone_driving_license)
    com.maisha.game.domain.MilestoneEngine.ID_MARRIAGE -> stringResource(R.string.milestone_marriage)
    com.maisha.game.domain.MilestoneEngine.ID_FIRST_CHILD -> stringResource(R.string.milestone_first_child)
    com.maisha.game.domain.MilestoneEngine.ID_FIRST_HOME -> stringResource(R.string.milestone_first_home)
    com.maisha.game.domain.MilestoneEngine.ID_FIRST_BUSINESS -> stringResource(R.string.milestone_first_business)
    com.maisha.game.domain.MilestoneEngine.ID_ELECTED -> stringResource(R.string.milestone_elected)
    com.maisha.game.domain.MilestoneEngine.ID_VERIFIED -> stringResource(R.string.milestone_verified)
    com.maisha.game.domain.MilestoneEngine.ID_GRADUATED -> stringResource(R.string.milestone_graduated)
    com.maisha.game.domain.MilestoneEngine.ID_WEALTHY -> stringResource(R.string.milestone_wealthy)
    else -> stringResource(R.string.life_milestone_unknown)
}

@Composable
private fun fameTierShortLabel(tier: com.maisha.game.data.model.FameTier): String = when (tier) {
    com.maisha.game.data.model.FameTier.LOCAL -> stringResource(R.string.fame_tier_local)
    com.maisha.game.data.model.FameTier.REGIONAL -> stringResource(R.string.fame_tier_regional)
    com.maisha.game.data.model.FameTier.NATIONAL -> stringResource(R.string.fame_tier_national)
    com.maisha.game.data.model.FameTier.GLOBAL -> stringResource(R.string.fame_tier_global)
    com.maisha.game.data.model.FameTier.UNKNOWN -> stringResource(R.string.fame_tier_unknown)
}

