// app/src/main/java/com/maisha/game/ui/summary/LifeSummaryScreen.kt
package com.maisha.game.ui.summary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.maisha.game.R
import com.maisha.game.ads.AdUnitConfig
import com.maisha.game.data.IllustrationCatalog
import com.maisha.game.data.model.AchievementCategory
import com.maisha.game.data.model.AvatarConfig
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.Gender
import com.maisha.game.data.model.IllustrationRef
import com.maisha.game.data.model.Stats
import com.maisha.game.data.model.ageStageFor
import com.maisha.game.domain.LifeRibbon
import com.maisha.game.ui.components.AdaptiveBannerAd
import com.maisha.game.ui.components.AppLoadingIndicator
import com.maisha.game.ui.components.CountryFlag
import com.maisha.game.ui.components.DismissibleTipCard
import com.maisha.game.ui.components.IllustrationImage
import com.maisha.game.ui.components.StatBar
import com.maisha.game.ui.components.StatType
import com.maisha.game.ui.components.getPersonInitials
import com.maisha.game.ui.legacy.AncestryTimelinePreview
import com.maisha.game.ui.share.ShareAchievementBadge
import com.maisha.game.ui.share.ShareCardComposable
import com.maisha.game.ui.share.ShareCardData
import com.maisha.game.ui.share.ShareCardDimensions
import com.maisha.game.ui.theme.AmberWarning
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.CrimsonDanger
import com.maisha.game.ui.theme.EmeraldCash
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.HairlineBorder
import com.maisha.game.ui.theme.MaishaTheme
import com.maisha.game.ui.theme.ObsidianBg
import com.maisha.game.ui.theme.OffWhitePrimary
import com.maisha.game.ui.theme.RoyalIndigo
import com.maisha.game.ui.theme.SlateNavyCard
import com.maisha.game.ui.theme.SlateNavyElevated
import com.maisha.game.ui.theme.SlateNavyMuted
import com.maisha.game.ui.theme.SteelSecondary
import com.maisha.game.ui.theme.SteelTertiary
import com.maisha.game.ui.theme.TabularFigures
import com.maisha.game.util.ComposableToImage
import com.maisha.game.util.ShareIntentHelper
import com.maisha.game.util.formatMoney
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val CASCADE_STAGGER_MS = 90
private const val CASCADE_DURATION_MS = 420

/**
 * Redesigned Tombstone & Legacy Summary Screen inspired by BitLife's iconic ribbon/legacy card.
 * Features a dignified dark commemorative card with silk-sash ribbon, monogram avatar medallion,
 * cause-of-death plaque, final net worth, career peak, and 4 highlight milestone chips,
 * with New Life and Life Log buttons anchored cleanly at the bottom.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LifeSummaryScreen(
    uiState: LifeSummaryUiState,
    onStartNewLife: () -> Unit,
    onContinueLegacy: () -> Unit,
    onHeirSelected: (com.maisha.game.data.model.Person) -> Unit,
    onConfirmLegacyContinuation: () -> Unit,
    onDismissHeirSelection: () -> Unit,
    onDismissLegacyConfirmation: () -> Unit,
    onWatchSecondWind: () -> Unit,
    onDismissAchievementsTip: () -> Unit,
    onShareMyLife: () -> Unit,
    onDismissSharePreview: () -> Unit,
    onShareCapturingStarted: () -> Unit,
    onShareCompleted: () -> Unit,
    onShareFailed: (String) -> Unit,
    onDismissShareError: () -> Unit
) {
    if (uiState.isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppLoadingIndicator()
        }
        return
    }

    val character = uiState.character ?: return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val shareCardData = uiState.shareCardData

    var cascadeVisible by remember { mutableStateOf(false) }
    LaunchedEffect(character.name) {
        cascadeVisible = false
        delay(40)
        cascadeVisible = true
    }

    if (uiState.showSharePreview && shareCardData != null) {
        SharePreviewDialog(
            cardData = shareCardData,
            isCapturing = uiState.isShareCapturing,
            onDismiss = onDismissSharePreview,
            onShare = {
                onShareCapturingStarted()
                scope.launch {
                    try {
                        val bitmap = withContext(Dispatchers.Main) {
                            ComposableToImage.captureComposableAsBitmap(
                                context = context,
                                widthPx = ShareCardDimensions.WIDTH_PX,
                                heightPx = ShareCardDimensions.HEIGHT_PX
                            ) {
                                MaishaTheme {
                                    ShareCardComposable(
                                        data = shareCardData,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                        val uri = ComposableToImage.saveBitmapToCache(context, bitmap)
                        ShareIntentHelper.shareImage(
                            context = context,
                            imageUri = uri,
                            caption = context.getString(R.string.share_default_caption)
                        )
                        onShareCompleted()
                    } catch (_: Exception) {
                        onShareFailed(context.getString(R.string.share_error))
                    }
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // -- Scrollable Story & Legacy Content Area ----------------------
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // -- 0. Dignified Dark Commemorative Monument Card -----------
                CascadeCard(visible = cascadeVisible, index = 0) {
                    MemorialHeader(
                        character = character,
                        deathCauseLabel = uiState.deathCauseLabel,
                        deathFlavorText = uiState.deathFlavorText,
                        lifeArchetypeLabel = uiState.lifeArchetypeLabel,
                        ribbon = uiState.ribbon,
                        legacyStamp = uiState.legacyStamp,
                        epitaph = uiState.epitaphText,
                        netWorth = uiState.netWorth,
                        careerRecap = uiState.careerRecap,
                        childrenCount = uiState.childrenCount
                    )
                }

                // -- 1. Eligible Heirs Banner --------------------------------
                if (uiState.eligibleHeirs.isNotEmpty()) {
                    CascadeCard(visible = cascadeVisible, index = 1) {
                        Button(
                            onClick = onContinueLegacy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = ObsidianBg
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = AppIcons.HeirCrown,
                                    contentDescription = null,
                                    tint = ObsidianBg,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(
                                        R.string.btn_continue_as_heir_count,
                                        uiState.eligibleHeirs.size
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // -- 2. Achievements Tip -------------------------------------
                if (uiState.showAchievementsCarryOverTip) {
                    CascadeCard(visible = cascadeVisible, index = 1) {
                        DismissibleTipCard(
                            text = stringResource(R.string.tip_first_death_achievements),
                            onDismiss = onDismissAchievementsTip
                        )
                    }
                }

                // -- 3. Final Character Stats Grid ---------------------------
                CascadeCard(visible = cascadeVisible, index = 2) {
                    SummarySectionCard(title = stringResource(R.string.section_final_stats)) {
                        FinalStatsGrid(
                            stats = character.stats,
                            netWorth = uiState.netWorth,
                            countryCode = character.countryCode
                        )
                    }
                }

                // -- 4. Wealth & Career Recap --------------------------------
                CascadeCard(visible = cascadeVisible, index = 3) {
                    SummarySectionCard(title = stringResource(R.string.section_wealth_career)) {
                        WealthCareerSection(
                            educationRecap = uiState.educationRecap,
                            careerRecap = uiState.careerRecap,
                            careerIllustration = uiState.shareCardData?.careerJobId?.let {
                                IllustrationCatalog.getIllustrationForJob(it)
                            },
                            spouseRecap = uiState.spouseRecap,
                            closestBondRecap = uiState.closestBondRecap,
                            childrenCount = uiState.childrenCount,
                            netWorth = uiState.netWorth,
                            money = character.stats.money,
                            countryCode = character.countryCode
                        )
                    }
                }

                // -- 5. Unlocked Achievements Showcase -----------------------
                val badges = shareCardData?.achievementBadges.orEmpty()
                if (badges.isNotEmpty()) {
                    CascadeCard(visible = cascadeVisible, index = 4) {
                        SummarySectionCard(title = stringResource(R.string.section_achievements_unlocked)) {
                            AchievementsShowcase(badges = badges)
                        }
                    }
                }

                // -- 6. Life Highlights / Event Log --------------------------
                if (uiState.eventHighlights.isNotEmpty()) {
                    CascadeCard(visible = cascadeVisible, index = 5) {
                        SummarySectionCard(title = stringResource(R.string.section_life_highlights)) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                uiState.eventHighlights.forEachIndexed { index, entry ->
                                    SummaryEventLogCard(
                                        entry = entry,
                                        ageTag = character.age - index
                                    )
                                }
                            }
                        }
                    }
                }

                // -- 7. Ancestry Timeline Preview ----------------------------
                if (character.ancestryHistory.isNotEmpty() || character.generationNumber > 1) {
                    CascadeCard(visible = cascadeVisible, index = 6) {
                        AncestryTimelinePreview(character = character)
                    }
                }

                // -- 8. Share Action & Second Wind ---------------------------
                if (shareCardData != null) {
                    OutlinedButton(
                        onClick = onShareMyLife,
                        enabled = !uiState.isShareCapturing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SlateNavyElevated,
                            contentColor = OffWhitePrimary
                        ),
                        border = BorderStroke(1.dp, HairlineBorder)
                    ) {
                        if (uiState.isShareCapturing) {
                            AppLoadingIndicator(size = 20.dp)
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = AppIcons.Share,
                                    contentDescription = null,
                                    tint = RoyalIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.btn_share_my_life),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                uiState.shareErrorMessage?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = CrimsonDanger,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        onClick = onDismissShareError,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(stringResource(R.string.btn_cancel), color = SteelSecondary)
                    }
                }

                if (uiState.showSecondWindButton) {
                    OutlinedButton(
                        onClick = onWatchSecondWind,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SlateNavyElevated,
                            contentColor = EmeraldCash
                        ),
                        border = BorderStroke(1.dp, EmeraldCash.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = stringResource(R.string.btn_watch_second_wind),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldCash,
                            maxLines = 2,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                uiState.secondWindMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = RoyalIndigo,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // -- Sponsored Banner Ad -----------------------------------------
            if (AdUnitConfig.areAdsGloballyEnabled) {
                Text(
                    text = stringResource(R.string.label_sponsored),
                    style = MaterialTheme.typography.labelSmall,
                    color = SteelTertiary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    textAlign = TextAlign.Center
                )
                AdaptiveBannerAd(
                    adUnitId = AdUnitConfig.BANNER,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // -- Docked Bottom Action Bar (Anchored Cleanly) -----------------
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SlateNavyCard,
                border = BorderStroke(1.dp, HairlineBorder),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Life Log Button: Smoothly scrolls down to Highlights / Log
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    scrollState.animateScrollTo(scrollState.maxValue)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = SlateNavyElevated,
                                contentColor = OffWhitePrimary
                            ),
                            border = BorderStroke(1.dp, HairlineBorder)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = AppIcons.Activity,
                                    contentDescription = null,
                                    tint = RoyalIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.btn_life_log),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OffWhitePrimary
                                )
                            }
                        }

                        // New Life Button (Primary CTA)
                        Button(
                            onClick = onStartNewLife,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalIndigo,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.btn_new_life),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // -- Heir Selection Dialog -----------------------------------------------
    if (uiState.showHeirSelection && uiState.eligibleHeirs.isNotEmpty()) {
        HeirSelectionDialog(
            heirs = uiState.eligibleHeirs,
            slotNumber = uiState.slotId + 1,
            selectedHeir = uiState.selectedHeir,
            showConfirmation = uiState.showLegacyConfirmation,
            heirPreviews = uiState.heirPreviews,
            onHeirSelected = onHeirSelected,
            onConfirmContinue = onConfirmLegacyContinuation,
            onDismissConfirmation = onDismissLegacyConfirmation,
            onDismiss = onDismissHeirSelection,
            deceasedName = character.name,
            legacyStamp = uiState.legacyStamp,
            epitaphText = uiState.epitaphText
        )
    }
}

@Composable
private fun CascadeCard(
    visible: Boolean,
    index: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(CASCADE_DURATION_MS, delayMillis = index * CASCADE_STAGGER_MS)) +
            slideInVertically(
                animationSpec = tween(CASCADE_DURATION_MS, delayMillis = index * CASCADE_STAGGER_MS),
                initialOffsetY = { it / 5 }
            )
    ) {
        Box(modifier = Modifier.padding(bottom = 2.dp)) {
            content()
        }
    }
}

@Composable
private fun SharePreviewDialog(
    cardData: ShareCardData,
    isCapturing: Boolean,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(
        onDismissRequest = { if (!isCapturing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBg.copy(alpha = 0.85f))
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SlateNavyCard),
                border = BorderStroke(1.dp, HairlineBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.share_preview_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    Text(
                        text = stringResource(R.string.share_preview_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = SteelSecondary
                    )
                    ShareCardComposable(
                        data = cardData,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .aspectRatio(9f / 16f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            enabled = !isCapturing,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = SlateNavyElevated,
                                contentColor = OffWhitePrimary
                            ),
                            border = BorderStroke(1.dp, HairlineBorder)
                        ) {
                            Text(stringResource(R.string.btn_cancel))
                        }
                        Button(
                            onClick = onShare,
                            enabled = !isCapturing,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalIndigo,
                                contentColor = Color.White
                            )
                        ) {
                            if (isCapturing) {
                                AppLoadingIndicator(size = 20.dp)
                            } else {
                                Text(
                                    text = stringResource(R.string.btn_share),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dignified dark commemorative card inspired by BitLife's ribbon dossier.
 * Features a high-contrast silk sash life ribbon banner, face-free monogram medallion,
 * cause-of-death plaque, and 4 highlight milestone chips.
 */
@Composable
private fun MemorialHeader(
    character: Character,
    deathCauseLabel: String,
    deathFlavorText: String,
    lifeArchetypeLabel: String = "",
    ribbon: LifeRibbon = LifeRibbon.MEDIOCRE,
    legacyStamp: String = "",
    epitaph: String = "",
    netWorth: Int = 0,
    careerRecap: String = "",
    childrenCount: Int = 0
) {
    val deathYear = character.birthYear + character.age
    val stage = ageStageFor(character.age)
    val ribbonTitleRes = when (ribbon) {
        LifeRibbon.LOADED -> R.string.ribbon_loaded
        LifeRibbon.ACADEMIC -> R.string.ribbon_academic
        LifeRibbon.CUNNING -> R.string.ribbon_cunning
        LifeRibbon.FAMILY_ANCHOR -> R.string.ribbon_family
        LifeRibbon.FAME_ICON -> R.string.ribbon_fame
        LifeRibbon.SAINT -> R.string.ribbon_saint
        LifeRibbon.SCANDALOUS -> R.string.ribbon_scandalous
        LifeRibbon.WORKAHOLIC -> R.string.ribbon_workaholic
        LifeRibbon.MEDIOCRE -> R.string.ribbon_mediocre
    }
    val ribbonDescRes = when (ribbon) {
        LifeRibbon.LOADED -> R.string.ribbon_loaded_desc
        LifeRibbon.ACADEMIC -> R.string.ribbon_academic_desc
        LifeRibbon.CUNNING -> R.string.ribbon_cunning_desc
        LifeRibbon.FAMILY_ANCHOR -> R.string.ribbon_family_desc
        LifeRibbon.FAME_ICON -> R.string.ribbon_fame_desc
        LifeRibbon.SAINT -> R.string.ribbon_saint_desc
        LifeRibbon.SCANDALOUS -> R.string.ribbon_scandalous_desc
        LifeRibbon.WORKAHOLIC -> R.string.ribbon_workaholic_desc
        LifeRibbon.MEDIOCRE -> R.string.ribbon_mediocre_desc
    }
    val col1 = Color(ribbon.primaryColorHex)
    val col2 = Color(ribbon.secondaryColorHex)

    // Dignified Dark Commemorative Monument Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = SlateNavyCard),
        border = BorderStroke(1.dp, HairlineBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        // High-contrast Silk Sash Life Ribbon Banner at Monument Crest
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(col1, col2)))
                .padding(vertical = 12.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = AppIcons.Tombstone,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(ribbonTitleRes).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Face-Free Geometric Monogram Avatar Medallion
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(SlateNavyElevated)
                    .border(2.dp, GoldAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getPersonInitials(character.name),
                    style = TabularFigures.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldAccent
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(ribbonDescRes),
                style = MaterialTheme.typography.bodySmall,
                color = col1,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.section_memorial).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = GoldAccent,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Full Name & Birthplace Country Flag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CountryFlag(countryCode = character.countryCode, size = 22.dp)
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OffWhitePrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Lifespan & Age in Tabular Monospace
            Text(
                text = "${character.birthYear} – $deathYear · ${stringResource(R.string.format_age, character.age)}",
                style = TabularFigures.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = SteelSecondary
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = stage.name.lowercase().replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.labelMedium,
                color = RoyalIndigo,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cause of Death Commemorative Plaque
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CrimsonDanger.copy(alpha = 0.12f)
                ),
                border = BorderStroke(1.dp, CrimsonDanger.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = deathCauseLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = OffWhitePrimary,
                        textAlign = TextAlign.Center
                    )
                    if (deathFlavorText.isNotBlank()) {
                        Text(
                            text = deathFlavorText,
                            style = MaterialTheme.typography.bodySmall,
                            color = OffWhitePrimary.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (lifeArchetypeLabel.isNotBlank()) {
                        Text(
                            text = lifeArchetypeLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    if (legacyStamp.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GoldAccent.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = legacyStamp,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OffWhitePrimary,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    if (epitaph.isNotBlank()) {
                        Text(
                            text = "\"$epitaph\"",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = OffWhitePrimary.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Highlight Milestone Chips (Net Worth, Pinnacle Career, Family, Legacy Ribbon)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MonumentMetricChip(
                    icon = AppIcons.Money,
                    label = stringResource(R.string.label_net_worth),
                    value = formatMoney(netWorth, character.countryCode),
                    valueColor = EmeraldCash,
                    modifier = Modifier.weight(1f)
                )
                MonumentMetricChip(
                    icon = AppIcons.Career,
                    label = stringResource(R.string.label_career),
                    value = careerRecap.split("·").firstOrNull()?.trim() ?: "Independent",
                    valueColor = OffWhitePrimary,
                    modifier = Modifier.weight(1f)
                )
                MonumentMetricChip(
                    icon = AppIcons.Family,
                    label = stringResource(R.string.label_family),
                    value = "$childrenCount Children",
                    valueColor = OffWhitePrimary,
                    modifier = Modifier.weight(1f)
                )
                MonumentMetricChip(
                    icon = AppIcons.Tombstone,
                    label = "Legacy",
                    value = stringResource(ribbonTitleRes),
                    valueColor = GoldAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MonumentMetricChip(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = OffWhitePrimary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = SlateNavyElevated,
        border = BorderStroke(1.dp, HairlineBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = valueColor,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = value,
                style = TabularFigures.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = SteelSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SummarySectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateNavyCard),
        border = BorderStroke(1.dp, HairlineBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = RoyalIndigo,
                letterSpacing = 1.sp
            )
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FinalStatsGrid(stats: Stats, netWorth: Int, countryCode: String) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 2
    ) {
        val chipModifier = Modifier.fillMaxWidth(0.48f)
        StatChip(label = stringResource(R.string.stat_health), value = "${stats.health}%", modifier = chipModifier)
        StatChip(label = stringResource(R.string.stat_happiness), value = "${stats.happiness}%", modifier = chipModifier)
        StatChip(label = stringResource(R.string.stat_smarts), value = "${stats.smarts}%", modifier = chipModifier)
        StatChip(label = stringResource(R.string.stat_looks), value = "${stats.looks}%", modifier = chipModifier)
        StatChip(
            label = stringResource(R.string.stat_money),
            value = formatMoney(stats.money, countryCode),
            accent = true,
            modifier = chipModifier
        )
        StatChip(
            label = stringResource(R.string.label_net_worth),
            value = formatMoney(netWorth, countryCode),
            accent = true,
            modifier = chipModifier
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        StatBar(type = StatType.HEALTH, value = stats.health)
        StatBar(type = StatType.HAPPINESS, value = stats.happiness)
        StatBar(type = StatType.SMARTS, value = stats.smarts)
        StatBar(type = StatType.LOOKS, value = stats.looks)
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    accent: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = if (accent) GoldAccent.copy(alpha = 0.14f) else SlateNavyElevated,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                1.dp,
                if (accent) GoldAccent.copy(alpha = 0.4f) else HairlineBorder,
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SteelSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = TabularFigures.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (accent) GoldAccent else OffWhitePrimary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun WealthCareerSection(
    educationRecap: String,
    careerRecap: String,
    careerIllustration: IllustrationRef?,
    spouseRecap: String,
    closestBondRecap: String,
    childrenCount: Int,
    netWorth: Int,
    money: Int,
    countryCode: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RecapRow(label = stringResource(R.string.label_education), value = educationRecap)
        RecapRow(
            label = stringResource(R.string.label_career),
            value = careerRecap,
            illustration = careerIllustration
        )
        RecapRow(label = stringResource(R.string.label_family), value = spouseRecap)
        RecapRow(label = stringResource(R.string.label_closest_bond), value = closestBondRecap)
        RecapRow(
            label = stringResource(R.string.label_children),
            value = pluralStringResource(R.plurals.child_count, childrenCount, childrenCount)
        )
        RecapRow(
            label = stringResource(R.string.stat_money),
            value = formatMoney(money, countryCode)
        )
        RecapRow(
            label = stringResource(R.string.label_net_worth),
            value = formatMoney(netWorth, countryCode)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AchievementsShowcase(badges: List<ShareAchievementBadge>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        badges.forEach { badge ->
            Row(
                modifier = Modifier
                    .background(
                        SlateNavyElevated,
                        RoundedCornerShape(10.dp)
                    )
                    .border(1.dp, GoldAccent.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IllustrationImage(
                    ref = IllustrationCatalog.getIllustrationForAchievementCategory(badge.category),
                    size = 20.dp
                )
                Text(
                    text = stringResource(badge.titleRes),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldAccent,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RecapRow(
    label: String,
    value: String,
    illustration: IllustrationRef? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (illustration != null) {
            IllustrationImage(ref = illustration, size = 28.dp)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = SteelSecondary,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = OffWhitePrimary,
            modifier = Modifier.weight(0.65f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SummaryEventLogCard(entry: String, ageTag: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SlateNavyElevated),
        border = BorderStroke(1.dp, HairlineBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(RoyalIndigo.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.format_age, ageTag.coerceAtLeast(0)),
                    style = TabularFigures.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalIndigo
                    )
                )
            }
            Text(
                text = entry,
                style = MaterialTheme.typography.bodySmall,
                color = OffWhitePrimary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, name = "Life summary 360dp")
@Preview(showBackground = true, widthDp = 412, name = "Life summary 412dp")
@Composable
private fun LifeSummaryScreenPreview() {
    val character = Character(
        name = "Sofia Reyes",
        age = 75,
        gender = Gender.FEMALE,
        stats = Stats(health = 20, happiness = 70, smarts = 65, looks = 55, money = 400_000),
        birthYear = 1965,
        alive = false,
        countryCode = "MX",
        avatarConfig = AvatarConfig.DEFAULT,
        generationNumber = 2,
        ancestryHistory = listOf(
            com.maisha.game.data.model.AncestryEntry(
                generationNumber = 1,
                characterName = "Diego Reyes",
                countryCode = "MX",
                ageAtDeath = 68,
                cause = "Passed away peacefully in old age"
            )
        )
    )
    MaishaTheme {
        LifeSummaryScreen(
            uiState = LifeSummaryUiState(
                isLoading = false,
                character = character,
                deathCauseLabel = "Passed away peacefully in old age",
                deathFlavorText = "Passed away peacefully at home in your sleep.",
                netWorth = 2_400_000,
                careerRecap = "Teacher · Level 4",
                educationRecap = "University graduate",
                spouseRecap = "Married to David",
                closestBondRecap = "Closest bond: David",
                childrenCount = 2,
                eventHighlights = listOf("Promoted at work.", "Welcomed a child."),
                shareCardData = ShareCardData(
                    characterName = character.name,
                    avatarConfig = character.avatarConfig,
                    birthYear = 1965,
                    deathYear = 2040,
                    ageAtDeath = 75,
                    countryCode = "MX",
                    deathCauseLabel = "Passed away peacefully in old age",
                    topStatLabel = "Happiness",
                    topStatValue = 70,
                    netWorthFormatted = "$ 2.4M",
                    careerHeadline = "Teacher · Level 4",
                    familySummary = "Married with 2 children",
                    legacySentence = "Sofia Reyes lived 75 years as a Teacher, built $ 2.4M, and raised 2 children.",
                    achievementBadges = listOf(
                        ShareAchievementBadge(
                            titleRes = R.string.app_name,
                            emoji = "",
                            category = AchievementCategory.FAMILY
                        )
                    )
                )
            ),
            onStartNewLife = {},
            onContinueLegacy = {},
            onHeirSelected = {},
            onConfirmLegacyContinuation = {},
            onDismissHeirSelection = {},
            onDismissLegacyConfirmation = {},
            onWatchSecondWind = {},
            onDismissAchievementsTip = {},
            onShareMyLife = {},
            onDismissSharePreview = {},
            onShareCapturingStarted = {},
            onShareCompleted = {},
            onShareFailed = {},
            onDismissShareError = {}
        )
    }
}
