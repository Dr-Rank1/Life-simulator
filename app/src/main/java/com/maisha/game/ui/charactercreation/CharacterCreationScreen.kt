// app/src/main/java/com/maisha/game/ui/charactercreation/CharacterCreationScreen.kt
package com.maisha.game.ui.charactercreation

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.R
import com.maisha.game.data.CountryCatalog
import com.maisha.game.data.model.Country
import com.maisha.game.data.model.Gender
import com.maisha.game.data.model.Stats
import com.maisha.game.ui.avatar.AvatarImage
import com.maisha.game.ui.components.CountryFlag
import com.maisha.game.ui.theme.CreamBg
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
 * Ultra-minimal single-screen character creation inspired by BitLife's "New Life" screen.
 * Pre-filled with randomized defaults and ready to start life in one tap.
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
    onStatsChange: (Stats) -> Unit = {},
    onStartLife: () -> Unit = {},
    onContinueToAvatar: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val selectedCountry = CountryCatalog.getCountry(uiState.selectedCountryCode)

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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top Header & Title ──────────────────────────────────────────
            Text(
                text = stringResource(R.string.title_new_life),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = InkPrimary,
                textAlign = TextAlign.Center
            )

            if (uiState.secondWindBonusLabel != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = uiState.secondWindBonusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = LifeGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Centered 88dp Baby Avatar with Shuffle Badge ────────────────
            val avatarCycleHint = stringResource(R.string.cycle_avatar_hint)
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
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

            Spacer(modifier = Modifier.height(28.dp))

            // ── Three Minimal Stacked Input Rows (12dp spacing) ─────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1: Name Field with inline Dice button
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
                            Text(
                                text = "🎲",
                                fontSize = 20.sp,
                                modifier = Modifier.semantics {
                                    contentDescription = "Randomize Name"
                                }
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

                // Row 2: Gender Segmented Pill Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
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
                            Text(
                                text = "♂",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMale) LifeGreen else InkTertiary
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
                            Text(
                                text = "♀",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFemale) LifeGreen else InkTertiary
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

                // Row 3: Country Selector (BitLife DrillDownRow style)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable { showCountrySheet = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PaperWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(1.dp, Hairline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CountryFlag(countryCode = selectedCountry.code, size = 28.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedCountry.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = InkPrimary
                            )
                            Text(
                                text = "${selectedCountry.currencyCode} (${selectedCountry.currencySymbol})",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkTertiary
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = InkTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Optional Custom Attributes Link ─────────────────────────────
            TextButton(
                onClick = { showStatsSheet = true },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.custom_attributes_optional),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TealPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                        Text(text = "🎲", fontSize = 18.sp)
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
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = startLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(list, key = { it.code }) { country ->
                        val isSelected = country.code == uiState.selectedCountryCode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) LifeGreenSoft else Color.Transparent)
                                .clickable {
                                    onCountrySelected(country.code)
                                    showCountrySheet = false
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CountryFlag(countryCode = country.code, size = 28.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = country.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = InkPrimary
                                )
                                Text(
                                    text = "${country.currencyCode} (${country.currencySymbol})",
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

    // ── Custom Attributes Bottom Sheet ──────────────────────────────────────
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
                    emoji = "❤️",
                    name = stringResource(R.string.stat_health),
                    value = uiState.selectedStats.health,
                    accentColor = StatHealth,
                    onValueChange = { onStatsChange(uiState.selectedStats.copy(health = it)) }
                )

                // Happiness Slider
                SimpleStatSlider(
                    emoji = "😊",
                    name = stringResource(R.string.stat_happiness),
                    value = uiState.selectedStats.happiness,
                    accentColor = StatHappiness,
                    onValueChange = { onStatsChange(uiState.selectedStats.copy(happiness = it)) }
                )

                // Smarts Slider
                SimpleStatSlider(
                    emoji = "🧠",
                    name = stringResource(R.string.stat_smarts),
                    value = uiState.selectedStats.smarts,
                    accentColor = StatSmarts,
                    onValueChange = { onStatsChange(uiState.selectedStats.copy(smarts = it)) }
                )

                // Looks Slider
                SimpleStatSlider(
                    emoji = "✨",
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
private fun SimpleStatSlider(
    emoji: String,
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
                Text(emoji, fontSize = 16.sp)
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
