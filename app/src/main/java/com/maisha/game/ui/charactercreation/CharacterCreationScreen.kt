// app/src/main/java/com/maisha\game/ui/charactercreation/CharacterCreationScreen.kt
package com.maisha.game.ui.charactercreation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.R
import com.maisha.game.data.CountryCatalog
import com.maisha.game.data.model.Country
import com.maisha.game.data.model.Gender
import com.maisha.game.data.model.Stats
import com.maisha.game.domain.StartingStatArchetype
import com.maisha.game.ui.avatar.AvatarImage
import com.maisha.game.ui.components.CountryFlag
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.CreamBg
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.Hairline
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.LifeGreenSoft
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.StatHappiness
import com.maisha.game.ui.theme.StatHealth
import com.maisha.game.ui.theme.StatLooks
import com.maisha.game.ui.theme.StatSmarts
import com.maisha.game.ui.theme.TealPrimary
import kotlin.math.roundToInt

/**
 * Mobile life-sim character creation (BitLife + Another Life aesthetic).
 * Pre-filled with randomized defaults, bold hero header, quick shuffle,
 * country preview opportunity card, and 1-tap archetype presets.
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
    val selectedCountry = CountryCatalog.getCountry(uiState.selectedCountryCode)
    val countryFlavor = CountryCatalog.flavorFor(uiState.selectedCountryCode)

    var showCountrySheet by remember { mutableStateOf(false) }
    var showStatsSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top Header with Arcade Quick-Roll Action ────────────────────
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
                        color = InkPrimary,
                        letterSpacing = 1.sp
                    )
                    if (uiState.secondWindBonusLabel != null) {
                        Text(
                            text = uiState.secondWindBonusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = LifeGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Instant Shuffle / Quick Roll Action
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PaperWhite,
                    border = BorderStroke(1.dp, Hairline),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onRandomizeLife)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Dice,
                            contentDescription = stringResource(R.string.btn_quick_roll),
                            tint = TealPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.btn_quick_roll),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Centered 92dp Baby Avatar with Cycle Halo ───────────────────
            val avatarCycleHint = stringResource(R.string.cycle_avatar_hint)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, TealPrimary.copy(alpha = 0.3f), CircleShape)
                    .clickable(onClick = onCycleAvatar)
                    .semantics { contentDescription = avatarCycleHint },
                contentAlignment = Alignment.Center
            ) {
                AvatarImage(
                    config = uiState.avatarConfig,
                    size = 88.dp,
                    age = 0
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(PaperWhite)
                        .border(1.dp, Hairline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = avatarCycleHint,
                        tint = TealPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Primary Identity Inputs (Name & Gender) ─────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Name Input with Dice Shuffle Button
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.label_your_name)) },
                    placeholder = { Text(stringResource(R.string.hint_enter_name)) },
                    singleLine = true,
                    isError = uiState.nameError != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = InkTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = onRandomName,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = AppIcons.Dice,
                                contentDescription = stringResource(R.string.btn_random_name),
                                tint = TealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = InkPrimary,
                        unfocusedTextColor = InkPrimary,
                        focusedBorderColor = LifeGreen,
                        unfocusedBorderColor = Hairline,
                        focusedContainerColor = PaperWhite,
                        unfocusedContainerColor = PaperWhite,
                        cursorColor = LifeGreen
                    )
                )

                // Gender Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PaperWhite)
                        .border(1.dp, Hairline, RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isMale = uiState.selectedGender == Gender.MALE
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isMale) LifeGreenSoft else Color.Transparent)
                            .clickable { onGenderSelected(Gender.MALE) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = AppIcons.Male,
                                contentDescription = null,
                                tint = if (isMale) LifeGreen else InkTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.gender_male),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isMale) FontWeight.Bold else FontWeight.Medium,
                                color = if (isMale) InkPrimary else InkTertiary
                            )
                        }
                    }

                    val isFemale = uiState.selectedGender == Gender.FEMALE
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isFemale) LifeGreenSoft else Color.Transparent)
                            .clickable { onGenderSelected(Gender.FEMALE) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = AppIcons.Female,
                                contentDescription = null,
                                tint = if (isFemale) LifeGreen else InkTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.gender_female),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isFemale) FontWeight.Bold else FontWeight.Medium,
                                color = if (isFemale) InkPrimary else InkTertiary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Crisp Country Opportunity Card ─────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCountrySheet = true },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PaperWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Hairline)
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
                                    color = InkPrimary
                                )
                                Text(
                                    text = stringResource(R.string.label_birthplace_dossier),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = InkSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.btn_change_country),
                            tint = TealPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // 3-Column Country Details Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Currency Chip
                        CountryDetailPill(
                            icon = AppIcons.Money,
                            label = "${selectedCountry.currencyCode} (${selectedCountry.currencySymbol})",
                            modifier = Modifier.weight(1f)
                        )
                        // Commute Chip
                        CountryDetailPill(
                            icon = AppIcons.Transport,
                            label = countryFlavor.commonTransportMode.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.weight(1f)
                        )
                        // Exam Chip
                        CountryDetailPill(
                            icon = AppIcons.Exam,
                            label = countryFlavor.secondaryExamName,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Starting Aptitude Archetype Presets ──────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PaperWhite),
                border = BorderStroke(1.dp, Hairline),
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
                                text = stringResource(R.string.label_starting_aptitude),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary
                            )
                            Text(
                                text = stringResource(R.string.label_starting_aptitude_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = InkTertiary
                            )
                        }
                        IconButton(
                            onClick = { showStatsSheet = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = stringResource(R.string.btn_fine_tune_stats),
                                tint = TealPrimary,
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
                                targetValue = if (isSelected) LifeGreenSoft else PaperWhite,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )
                            val borderCol by animateColorAsState(
                                targetValue = if (isSelected) LifeGreen else Hairline,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
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
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bg)
                                    .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                                    .clickable { onArchetypeSelected(archetype) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(labelRes),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) LifeGreen else InkPrimary
                                )
                            }
                        }

                        // Custom Pill
                        val isCustom = uiState.selectedArchetype == null
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCustom) LifeGreenSoft else PaperWhite)
                                .border(1.dp, if (isCustom) LifeGreen else Hairline, RoundedCornerShape(12.dp))
                                .clickable { showStatsSheet = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.archetype_preset_custom),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isCustom) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isCustom) LifeGreen else InkPrimary
                            )
                        }
                    }

                    // Live 4-Stat Meters HUD
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatGaugeRow(
                            icon = AppIcons.Health,
                            name = stringResource(R.string.stat_health),
                            value = uiState.selectedStats.health,
                            color = StatHealth
                        )
                        StatGaugeRow(
                            icon = AppIcons.Happiness,
                            name = stringResource(R.string.stat_happiness),
                            value = uiState.selectedStats.happiness,
                            color = StatHappiness
                        )
                        StatGaugeRow(
                            icon = AppIcons.Smarts,
                            name = stringResource(R.string.stat_smarts),
                            value = uiState.selectedStats.smarts,
                            color = StatSmarts
                        )
                        StatGaugeRow(
                            icon = AppIcons.Looks,
                            name = stringResource(R.string.stat_looks),
                            value = uiState.selectedStats.looks,
                            color = StatLooks
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Action Footer ───────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Secondary Randomize Life Button
                OutlinedButton(
                    onClick = onRandomizeLife,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = PaperWhite,
                        contentColor = InkPrimary
                    ),
                    border = BorderStroke(1.dp, Hairline)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Dice,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.btn_randomize_life),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = InkPrimary
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
                    onClick = onStartLife,
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LifeGreen,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Text(
                        text = startLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ── Country Picker Bottom Sheet ─────────────────────────────────────────
    if (showCountrySheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showCountrySheet = false
                onCountrySearchChange("")
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = PaperWhite,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_country_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.countrySearchQuery,
                    onValueChange = onCountrySearchChange,
                    placeholder = { Text(stringResource(R.string.hint_search_countries), color = InkTertiary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LifeGreen,
                        unfocusedBorderColor = Hairline,
                        cursorColor = LifeGreen
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                val list = if (uiState.countrySearchQuery.isBlank()) {
                    CountryCatalog.all()
                } else {
                    filteredCountries
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(list, key = { it.code }) { country ->
                        val isSelected = country.code == uiState.selectedCountryCode
                        val flavor = CountryCatalog.flavorFor(country.code)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) LifeGreenSoft else Color.Transparent)
                                .clickable {
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
                                    color = InkPrimary
                                )
                                Text(
                                    text = "${country.currencyCode} (${country.currencySymbol}) · ${flavor.secondaryExamName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkTertiary
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = LifeGreen,
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

    // ── Custom Attributes Bottom Sheet (No Emojis!) ─────────────────────────
    if (showStatsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStatsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = PaperWhite,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.custom_attributes_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    Text(
                        text = stringResource(R.string.custom_attributes_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkTertiary
                    )
                }

                // Health Slider
                SimpleStatSlider(
                    icon = AppIcons.Health,
                    name = stringResource(R.string.stat_health),
                    value = uiState.selectedStats.health,
                    accentColor = StatHealth,
                    onValueChange = { onStatsChange(uiState.selectedStats.copy(health = it)) }
                )

                // Happiness Slider
                SimpleStatSlider(
                    icon = AppIcons.Happiness,
                    name = stringResource(R.string.stat_happiness),
                    value = uiState.selectedStats.happiness,
                    accentColor = StatHappiness,
                    onValueChange = { onStatsChange(uiState.selectedStats.copy(happiness = it)) }
                )

                // Smarts Slider
                SimpleStatSlider(
                    icon = AppIcons.Smarts,
                    name = stringResource(R.string.stat_smarts),
                    value = uiState.selectedStats.smarts,
                    accentColor = StatSmarts,
                    onValueChange = { onStatsChange(uiState.selectedStats.copy(smarts = it)) }
                )

                // Looks Slider
                SimpleStatSlider(
                    icon = AppIcons.Looks,
                    name = stringResource(R.string.stat_looks),
                    value = uiState.selectedStats.looks,
                    accentColor = StatLooks,
                    onValueChange = { onStatsChange(uiState.selectedStats.copy(looks = it)) }
                )

                Button(
                    onClick = { showStatsSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LifeGreen)
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
        shape = RoundedCornerShape(10.dp),
        color = CreamBg,
        border = BorderStroke(1.dp, Hairline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = InkPrimary,
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
    value: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = InkSecondary,
            modifier = Modifier.width(72.dp)
        )
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.18f)
        )
        Text(
            text = "$value%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun SimpleStatSlider(
    icon: ImageVector,
    name: String,
    value: Int,
    accentColor: Color,
    onValueChange: (Int) -> Unit
) {
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
                    color = InkPrimary
                )
            }
            Text(
                text = "$value%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt().coerceIn(0, 100)) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = accentColor.copy(alpha = 0.2f)
            )
        )
    }
}
