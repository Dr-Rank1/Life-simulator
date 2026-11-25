// app/src/main/java/com/maisha/game/ui/charactercreation/CharacterCreationScreen.kt
package com.maisha.game.ui.charactercreation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.R
import com.maisha.game.data.CountryCatalog
import com.maisha.game.data.model.Country
import com.maisha.game.data.model.Gender
import com.maisha.game.data.model.Stats
import com.maisha.game.domain.StartingStatArchetype
import com.maisha.game.feedback.HapticType
import com.maisha.game.ui.components.CountryFlag
import com.maisha.game.ui.feedback.LocalFeedbackManager
import com.maisha.game.ui.theme.AmberWarning
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.CrimsonDanger
import com.maisha.game.ui.theme.EmeraldCash
import com.maisha.game.ui.theme.HairlineBorder
import com.maisha.game.ui.theme.MaishaFontFamily
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
import kotlin.math.roundToInt

/**
 * Minimalist, face-free dark character creation screen (AltLife & BitLife inspired).
 * Features an instant randomized preset generator, crisp segmented selectors for
 * Gender and Starting Country/City, a clean starting attributes preview card,
 * and a bold full-width Start Life CTA button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreationScreen(
    uiState: CharacterCreationUiState,
    filteredCountries: List<Country>,
    onNameChange: (String) -> Unit,
    onGenderSelected: (Gender) -> Unit,
    onCountrySelected: (String) -> Unit,
    onCountrySearchChange: (String) -> Unit,
    onRandomName: () -> Unit,
    onCycleAvatar: () -> Unit = {},
    onRandomizeLife: () -> Unit = {},
    onArchetypeSelected: (StartingStatArchetype) -> Unit = {},
    onStatsChange: (Stats) -> Unit = {},
    onStartLife: () -> Unit = {},
    onContinueToAvatar: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val view = LocalView.current
    val feedbackManager = runCatching { LocalFeedbackManager.current }.getOrNull()

    val selectedCountry = CountryCatalog.getCountry(uiState.selectedCountryCode)
    val countryFlavor = CountryCatalog.flavorFor(uiState.selectedCountryCode)

    var showCountrySheet by remember { mutableStateOf(false) }
    var showStatsSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // -- Top Header with Arcade Preset Generator Action --------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.title_new_life).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = OffWhitePrimary,
                        letterSpacing = 1.sp
                    )
                    if (uiState.secondWindBonusLabel != null) {
                        Text(
                            text = uiState.secondWindBonusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldCash,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Forge your destiny",
                            style = MaterialTheme.typography.bodySmall,
                            color = SteelSecondary
                        )
                    }
                }

                // Instant Randomized Preset Generator Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SlateNavyElevated,
                    border = BorderStroke(1.dp, RoyalIndigo.copy(alpha = 0.45f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            feedbackManager?.triggerHaptic(view, HapticType.LIGHT_TAP)
                            onRandomizeLife()
                        }
                        .semantics { role = Role.Button }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Dice,
                            contentDescription = stringResource(R.string.btn_quick_roll),
                            tint = RoyalIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.btn_quick_roll),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = OffWhitePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // -- Name Input with Randomize Dice ------------------------------
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.label_your_name)) },
                placeholder = { Text(stringResource(R.string.hint_enter_name)) },
                singleLine = true,
                isError = uiState.nameError != null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = SteelSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            feedbackManager?.triggerHaptic(view, HapticType.LIGHT_TAP)
                            onRandomName()
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Dice,
                            contentDescription = stringResource(R.string.btn_random_name),
                            tint = RoyalIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = OffWhitePrimary,
                    unfocusedTextColor = OffWhitePrimary,
                    focusedBorderColor = RoyalIndigo,
                    unfocusedBorderColor = HairlineBorder,
                    focusedContainerColor = SlateNavyCard,
                    unfocusedContainerColor = SlateNavyCard,
                    cursorColor = RoyalIndigo,
                    focusedLabelColor = RoyalIndigo,
                    unfocusedLabelColor = SteelSecondary,
                    focusedPlaceholderColor = SteelTertiary,
                    unfocusedPlaceholderColor = SteelTertiary
                )
            )

            // -- Crisp Segmented Gender Selector -----------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SlateNavyElevated)
                    .border(1.dp, HairlineBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isMale = uiState.selectedGender == Gender.MALE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isMale) RoyalIndigo.copy(alpha = 0.22f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isMale) RoyalIndigo.copy(alpha = 0.55f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            feedbackManager?.triggerHaptic(view, HapticType.LIGHT_TAP)
                            onGenderSelected(Gender.MALE)
                        }
                        .semantics { role = Role.Tab },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Male,
                            contentDescription = null,
                            tint = if (isMale) RoyalIndigo else SteelSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.gender_male),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isMale) FontWeight.Bold else FontWeight.Medium,
                            color = if (isMale) OffWhitePrimary else SteelSecondary
                        )
                    }
                }

                val isFemale = uiState.selectedGender == Gender.FEMALE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isFemale) RoyalIndigo.copy(alpha = 0.22f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isFemale) RoyalIndigo.copy(alpha = 0.55f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            feedbackManager?.triggerHaptic(view, HapticType.LIGHT_TAP)
                            onGenderSelected(Gender.FEMALE)
                        }
                        .semantics { role = Role.Tab },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Female,
                            contentDescription = null,
                            tint = if (isFemale) RoyalIndigo else SteelSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.gender_female),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isFemale) FontWeight.Bold else FontWeight.Medium,
                            color = if (isFemale) OffWhitePrimary else SteelSecondary
                        )
                    }
                }
            }

            // -- Crisp Starting Country/City Drill-Down Card -----------------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        feedbackManager?.triggerHaptic(view, HapticType.LIGHT_TAP)
                        showCountrySheet = true
                    }
                    .semantics { role = Role.Button },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SlateNavyCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, HairlineBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            CountryFlag(countryCode = selectedCountry.code, size = 32.dp)
                            Column {
                                Text(
                                    text = selectedCountry.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = OffWhitePrimary
                                )
                                Text(
                                    text = "${selectedCountry.displayName} · ${stringResource(R.string.label_birthplace_dossier)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SteelSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = stringResource(R.string.btn_change_country),
                            tint = SteelSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // 3-Column Country Dossier Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CountryDetailPill(
                            icon = AppIcons.Money,
                            label = "${selectedCountry.currencyCode} (${selectedCountry.currencySymbol})",
                            modifier = Modifier.weight(1f)
                        )
                        CountryDetailPill(
                            icon = AppIcons.Transport,
                            label = countryFlavor.commonTransportMode.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.weight(1f)
                        )
                        CountryDetailPill(
                            icon = AppIcons.Exam,
                            label = countryFlavor.secondaryExamName,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // -- Clean Starting Attributes Preview Card ----------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SlateNavyCard),
                border = BorderStroke(1.dp, HairlineBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.label_starting_aptitude).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OffWhitePrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = stringResource(R.string.label_starting_aptitude_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = SteelSecondary,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(
                            onClick = {
                                feedbackManager?.triggerHaptic(view, HapticType.LIGHT_TAP)
                                showStatsSheet = true
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = stringResource(R.string.btn_fine_tune_stats),
                                tint = RoyalIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Horizontal Archetype Preset Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StartingStatArchetype.values().forEach { archetype ->
                            val isSelected = uiState.selectedArchetype == archetype
                            val bg by animateColorAsState(
                                targetValue = if (isSelected) RoyalIndigo.copy(alpha = 0.22f) else SlateNavyElevated,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "presetBg"
                            )
                            val borderCol by animateColorAsState(
                                targetValue = if (isSelected) RoyalIndigo else HairlineBorder,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "presetBorder"
                            )
                            val labelRes = when (archetype) {
                                StartingStatArchetype.BALANCED -> R.string.archetype_preset_balanced
                                StartingStatArchetype.PRODIGY -> R.string.archetype_preset_prodigy
                                StartingStatArchetype.ATHLETE -> R.string.archetype_preset_athlete
                                StartingStatArchetype.CHARMER -> R.string.archetype_preset_charmer
                                StartingStatArchetype.HUSTLER -> R.string.archetype_preset_hustler
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bg)
                                    .border(1.dp, borderCol, RoundedCornerShape(10.dp))
                                    .clickable {
                                        feedbackManager?.triggerHaptic(view, HapticType.LIGHT_TAP)
                                        onArchetypeSelected(archetype)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                                    .semantics { role = Role.Tab },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(labelRes),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) OffWhitePrimary else SteelSecondary
                                )
                            }
                        }

                        // Custom Pill
                        val isCustom = uiState.selectedArchetype == null
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCustom) RoyalIndigo.copy(alpha = 0.22f) else SlateNavyElevated)
                                .border(
                                    1.dp,
                                    if (isCustom) RoyalIndigo else HairlineBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    feedbackManager?.triggerHaptic(view, HapticType.LIGHT_TAP)
                                    showStatsSheet = true
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .semantics { role = Role.Tab },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.archetype_preset_custom),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isCustom) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isCustom) OffWhitePrimary else SteelSecondary
                            )
                        }
                    }

                    // Live 4-Stat Gauges (Threshold color-coded, Tabular monospace)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatGaugeRow(
                            icon = AppIcons.Health,
                            name = stringResource(R.string.stat_health),
                            value = uiState.selectedStats.health
                        )
                        StatGaugeRow(
                            icon = AppIcons.Happiness,
                            name = stringResource(R.string.stat_happiness),
                            value = uiState.selectedStats.happiness
                        )
                        StatGaugeRow(
                            icon = AppIcons.Smarts,
                            name = stringResource(R.string.stat_smarts),
                            value = uiState.selectedStats.smarts
                        )
                        StatGaugeRow(
                            icon = AppIcons.Looks,
                            name = stringResource(R.string.stat_looks),
                            value = uiState.selectedStats.looks
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // -- Action Footer: Randomize Secondary + Bold Full-Width Start CTA --
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Secondary Randomize Life Button
                OutlinedButton(
                    onClick = {
                        feedbackManager?.triggerHaptic(view, HapticType.LIGHT_TAP)
                        onRandomizeLife()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Dice,
                            contentDescription = null,
                            tint = RoyalIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.btn_randomize_life),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = OffWhitePrimary
                        )
                    }
                }

                // Primary Start Life Button
                val firstName = uiState.name.trim().split(" ").firstOrNull()?.ifBlank { null }
                val startLabel = if (firstName != null) {
                    stringResource(R.string.btn_start_named_life, firstName)
                } else {
                    stringResource(R.string.btn_start_life)
                }
                Button(
                    onClick = {
                        feedbackManager?.triggerHaptic(view, HapticType.SUCCESS)
                        onStartLife()
                    },
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalIndigo,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Text(
                        text = startLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MaishaFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // -- Country Picker Bottom Sheet (Dark Obsidian Aesthetic) ---------------
    if (showCountrySheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showCountrySheet = false
                onCountrySearchChange("")
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = SlateNavyCard,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_country_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OffWhitePrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.countrySearchQuery,
                    onValueChange = onCountrySearchChange,
                    placeholder = {
                        Text(stringResource(R.string.hint_search_countries), color = SteelSecondary)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OffWhitePrimary,
                        unfocusedTextColor = OffWhitePrimary,
                        focusedBorderColor = RoyalIndigo,
                        unfocusedBorderColor = HairlineBorder,
                        focusedContainerColor = SlateNavyElevated,
                        unfocusedContainerColor = SlateNavyElevated,
                        cursorColor = RoyalIndigo
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                val list = if (uiState.countrySearchQuery.isBlank()) {
                    CountryCatalog.all()
                } else {
                    filteredCountries
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(list, key = { it.code }) { country ->
                        val isSelected = country.code == uiState.selectedCountryCode
                        val flavor = CountryCatalog.flavorFor(country.code)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) RoyalIndigo.copy(alpha = 0.2f) else SlateNavyElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) RoyalIndigo.copy(alpha = 0.5f) else HairlineBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    feedbackManager?.triggerHaptic(view, HapticType.LIGHT_TAP)
                                    onCountrySelected(country.code)
                                    showCountrySheet = false
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CountryFlag(countryCode = country.code, size = 32.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = country.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = OffWhitePrimary
                                )
                                Text(
                                    text = "${country.currencyCode} (${country.currencySymbol}) · ${flavor.secondaryExamName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SteelSecondary
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = EmeraldCash,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // -- Custom Attributes Bottom Sheet (Dark Monospace Sliders) -------------
    if (showStatsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStatsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = SlateNavyCard,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.custom_attributes_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OffWhitePrimary
                    )
                    Text(
                        text = stringResource(R.string.custom_attributes_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = SteelSecondary
                    )
                }

                // Health Slider
                SimpleStatSlider(
                    icon = AppIcons.Health,
                    name = stringResource(R.string.stat_health),
                    value = uiState.selectedStats.health,
                    onValueChange = { onStatsChange(uiState.selectedStats.copy(health = it)) }
                )

                // Happiness Slider
                SimpleStatSlider(
                    icon = AppIcons.Happiness,
                    name = stringResource(R.string.stat_happiness),
                    value = uiState.selectedStats.happiness,
                    onValueChange = { onStatsChange(uiState.selectedStats.copy(happiness = it)) }
                )

                // Smarts Slider
                SimpleStatSlider(
                    icon = AppIcons.Smarts,
                    name = stringResource(R.string.stat_smarts),
                    value = uiState.selectedStats.smarts,
                    onValueChange = { onStatsChange(uiState.selectedStats.copy(smarts = it)) }
                )

                // Looks Slider
                SimpleStatSlider(
                    icon = AppIcons.Looks,
                    name = stringResource(R.string.stat_looks),
                    value = uiState.selectedStats.looks,
                    onValueChange = { onStatsChange(uiState.selectedStats.copy(looks = it)) }
                )

                Button(
                    onClick = {
                        feedbackManager?.triggerHaptic(view, HapticType.LIGHT_TAP)
                        showStatsSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalIndigo)
                ) {
                    Text(
                        text = stringResource(R.string.btn_done),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CountryDetailPill(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = SlateNavyElevated,
        border = BorderStroke(1.dp, HairlineBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RoyalIndigo,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = OffWhitePrimary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatGaugeRow(
    icon: ImageVector,
    name: String,
    value: Int
) {
    val progress = (value / 100f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "gaugeProgress"
    )

    val thresholdColor = when {
        value > 60 -> EmeraldCash
        value >= 30 -> AmberWarning
        else -> CrimsonDanger
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = thresholdColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = SteelSecondary,
            fontSize = 12.sp,
            modifier = Modifier.width(68.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(SlateNavyMuted)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(3.dp))
                    .background(thresholdColor)
            )
        }
        Text(
            text = "$value%",
            style = TabularFigures.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = thresholdColor
            ),
            modifier = Modifier.width(38.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun SimpleStatSlider(
    icon: ImageVector,
    name: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    val accentColor = when {
        value > 60 -> EmeraldCash
        value >= 30 -> AmberWarning
        else -> CrimsonDanger
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = OffWhitePrimary
                )
            }
            Text(
                text = "$value%",
                style = TabularFigures.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt().coerceIn(0, 100)) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = SlateNavyMuted
            )
        )
    }
}

@Preview(name = "Character Creation - 360dp", widthDp = 360)
@Preview(name = "Character Creation - 412dp", widthDp = 412)
@Composable
private fun CharacterCreationScreenPreview() {
    MaishaTheme {
        CharacterCreationScreen(
            uiState = CharacterCreationUiState(
                name = "Sofia Reyes",
                selectedGender = Gender.FEMALE,
                selectedCountryCode = "MX",
                selectedArchetype = StartingStatArchetype.BALANCED,
                selectedStats = Stats(health = 75, happiness = 80, smarts = 65, looks = 70)
            ),
            filteredCountries = CountryCatalog.all(),
            onNameChange = {},
            onGenderSelected = {},
            onCountrySelected = {},
            onCountrySearchChange = {},
            onRandomName = {}
        )
    }
}
