package com.maisha.game.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.key
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.maisha.game.R
import com.maisha.game.data.CountryCatalog
import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.Country
import com.maisha.game.domain.RelocationEngine
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.BorderCardSlate
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.CreamBg
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.SuccessGreen
import com.maisha.game.ui.theme.TealPrimary
import com.maisha.game.util.formatMoney

private enum class RelocationTab {
    DOSSIER,
    EMIGRATION
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RelocationBoard(
    character: Character,
    onRenewVisa: () -> Unit,
    onApplyForCitizenship: () -> Unit,
    onTakeDrivingTest: () -> Unit,
    onRelocate: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val relocationEngine = remember { RelocationEngine() }
    var selectedTab by remember { mutableStateOf(RelocationTab.DOSSIER) }
    var destinationToConfirm by remember { mutableStateOf<Country?>(null) }

    val currentCountry = remember(character.countryCode) {
        CountryCatalog.getCountry(character.countryCode)
    }
    val currentFlavor = remember(character.countryCode) {
        CountryCatalog.flavorFor(character.countryCode)
    }

    val destinations = remember(character.countryCode) {
        CountryCatalog.all().filter { it.code != character.countryCode }
    }

    val canRenewVisa = relocationEngine.canRenewVisa(character)
    val visaRenewalFee = relocationEngine.visaRenewalFee(character)
    val canAffordVisa = character.stats.money >= visaRenewalFee

    val canApplyCitizenship = relocationEngine.canApplyForCitizenship(character)
    val citizenshipFee = relocationEngine.citizenshipFee(character)
    val canAffordCitizenship = character.stats.money >= citizenshipFee

    val drivingTestFee = EconomyScaler.scaleAmount(50_000, character.countryCode)
    val canAffordDriving = character.stats.money >= drivingTestFee
    val showDrivingTest = character.alive && character.age >= 16 && !character.hasDrivingLicense

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Dossier & Residence Snapshot Card
        ResidenceHeaderCard(
            character = character,
            currentCountry = currentCountry,
            currentFlavor = currentFlavor
        )

        // Tab Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedTab == RelocationTab.DOSSIER,
                onClick = { selectedTab = RelocationTab.DOSSIER },
                label = { Text(stringResource(R.string.tab_relocation_status)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TealPrimary.copy(alpha = 0.15f),
                    selectedLabelColor = TealPrimary
                )
            )
            FilterChip(
                selected = selectedTab == RelocationTab.EMIGRATION,
                onClick = { selectedTab = RelocationTab.EMIGRATION },
                label = { Text(stringResource(R.string.tab_relocation_destinations)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TealPrimary.copy(alpha = 0.15f),
                    selectedLabelColor = TealPrimary
                )
            )
        }

        if (selectedTab == RelocationTab.DOSSIER) {
            // Driving Test Action if applicable
            if (showDrivingTest) {
                DrivingTestActionCard(
                    fee = drivingTestFee,
                    countryCode = character.countryCode,
                    canAfford = canAffordDriving,
                    onTakeTest = onTakeDrivingTest
                )
            }

            // Visa Status & Renewal Card
            if (character.isLivingAbroad()) {
                VisaRenewalActionCard(
                    character = character,
                    canRenew = canRenewVisa,
                    fee = visaRenewalFee,
                    canAfford = canAffordVisa,
                    onRenew = onRenewVisa
                )

                // Naturalization & Citizenship Card
                CitizenshipApplicationCard(
                    character = character,
                    canApply = canApplyCitizenship,
                    fee = citizenshipFee,
                    canAfford = canAffordCitizenship,
                    onApply = onApplyForCitizenship
                )
            } else {
                NativeCitizenDossierCard(currentCountry = currentCountry)
            }
        } else {
            // Emigration Destination Cards
            destinations.forEach { destination ->
                key(destination.code) {
                    val flavor = CountryCatalog.flavorFor(destination.code)
                    val flightFee = relocationEngine.relocationFee(character)
                    val canAffordMove = character.stats.money >= flightFee
                    val holdsPassport = character.holdsCitizenship(destination.code)
                    val convertedCash = EconomyScaler.convertCurrency(
                        (character.stats.money - flightFee).coerceAtLeast(0),
                        character.countryCode,
                        destination.code
                    )

                    DestinationCountryCard(
                        character = character,
                        destination = destination,
                        flavor = flavor,
                        flightFee = flightFee,
                        canAfford = canAffordMove,
                        holdsPassport = holdsPassport,
                        convertedCash = convertedCash,
                        onSelect = { destinationToConfirm = destination }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Emigration Confirmation Dialog
    destinationToConfirm?.let { destination ->
        val flightFee = relocationEngine.relocationFee(character)
        val canAffordMove = character.stats.money >= flightFee
        val convertedCash = EconomyScaler.convertCurrency(
            (character.stats.money - flightFee).coerceAtLeast(0),
            character.countryCode,
            destination.code
        )

        Dialog(onDismissRequest = { destinationToConfirm = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PaperWhite,
                border = BorderStroke(1.dp, BorderCardSlate),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.dialog_relocate_title, destination.displayName),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    Text(
                        text = stringResource(
                            R.string.dialog_relocate_desc,
                            destination.currencyCode,
                            destination.displayName,
                            formatMoney(flightFee, character.countryCode)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSecondary
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CreamBg,
                        border = BorderStroke(1.dp, BorderCardSlate)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Projected Starting Cash:",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkSecondary
                            )
                            Text(
                                text = "${destination.currencySymbol}${convertedCash} ${destination.currencyCode}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { destinationToConfirm = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.btn_cancel))
                        }
                        Button(
                            onClick = {
                                destinationToConfirm = null
                                onRelocate(destination.code)
                            },
                            enabled = canAffordMove,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            Text(
                                text = if (canAffordMove) "Emigrate" else "Cannot Afford",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResidenceHeaderCard(
    character: Character,
    currentCountry: Country,
    currentFlavor: com.maisha.game.data.model.CountryFlavor
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, BorderCardSlate),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TealPrimary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = currentCountry.code,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Column {
                        Text(
                            text = currentCountry.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = "Currency: ${currentCountry.currencyCode} (${currentCountry.currencySymbol})",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (character.isLivingAbroad()) GoldAccent.copy(alpha = 0.12f) else SuccessGreen.copy(alpha = 0.12f),
                    border = BorderStroke(
                        1.dp,
                        if (character.isLivingAbroad()) GoldAccent.copy(alpha = 0.35f) else SuccessGreen.copy(alpha = 0.35f)
                    )
                ) {
                    Text(
                        text = if (character.isLivingAbroad()) "Expat Resident" else "Native Citizen",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (character.isLivingAbroad()) GoldAccent else SuccessGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoTag(label = "Commute", value = currentFlavor.commonTransportMode)
                InfoTag(label = "Hospital", value = currentFlavor.privateHospitalName)
                InfoTag(label = "Banking", value = currentFlavor.popularMoneyAppOrBank ?: "Banking")
            }
        }
    }
}

@Composable
private fun InfoTag(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = CreamBg,
        border = BorderStroke(1.dp, BorderCardSlate)
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = InkTertiary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = InkPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun NativeCitizenDossierCard(currentCountry: Country) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, BorderCardSlate),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.GovernmentSeal,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "Full Birthright Citizenship",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
            Text(
                text = "You hold permanent birthright citizenship in ${currentCountry.displayName}. No visa renewal or immigration reporting required.",
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun VisaRenewalActionCard(
    character: Character,
    canRenew: Boolean,
    fee: Int,
    canAfford: Boolean,
    onRenew: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, BorderCardSlate),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Immigration Visa Status",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    Text(
                        text = stringResource(
                            R.string.immigration_status_summary,
                            character.currentVisa?.name?.lowercase() ?: stringResource(R.string.immigration_no_visa),
                            character.visaYearsRemaining
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (character.visaYearsRemaining <= 1) CoralNegative else InkSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (character.visaYearsRemaining <= 1) CoralNegative.copy(alpha = 0.12f) else SuccessGreen.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (character.visaYearsRemaining <= 1) "Expires Soon" else "Active Visa",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (character.visaYearsRemaining <= 1) CoralNegative else SuccessGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (canRenew) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Renewal Legal Fee: ${formatMoney(fee, character.countryCode)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (canAfford) TealPrimary else CoralNegative
                    )
                    Button(
                        onClick = onRenew,
                        enabled = canAfford,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text(stringResource(R.string.btn_renew_visa), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CitizenshipApplicationCard(
    character: Character,
    canApply: Boolean,
    fee: Int,
    canAfford: Boolean,
    onApply: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, BorderCardSlate),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Dual Citizenship Naturalization",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
            Text(
                text = if (canApply) {
                    stringResource(R.string.immigration_citizenship_desc, formatMoney(fee, character.countryCode))
                } else {
                    stringResource(
                        R.string.immigration_citizenship_locked,
                        RelocationEngine.NATURALIZATION_YEARS,
                        character.yearsInCurrentCountry
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary
            )

            if (canApply) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onApply,
                        enabled = canAfford,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text(stringResource(R.string.btn_apply_citizenship), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DrivingTestActionCard(
    fee: Int,
    countryCode: String,
    canAfford: Boolean,
    onTakeTest: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, BorderCardSlate),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(TealPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.Transport,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.btn_take_driving_test),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )
                Text(
                    text = "Test Fee: ${formatMoney(fee, countryCode)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (canAfford) TealPrimary else CoralNegative
                )
            }

            Button(
                onClick = onTakeTest,
                enabled = canAfford,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Take Test", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DestinationCountryCard(
    character: Character,
    destination: Country,
    flavor: com.maisha.game.data.model.CountryFlavor,
    flightFee: Int,
    canAfford: Boolean,
    holdsPassport: Boolean,
    convertedCash: Int,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, BorderCardSlate),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TealPrimary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = destination.code,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Column {
                        Text(
                            text = destination.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = "${destination.currencyCode} (${destination.currencySymbol})",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (holdsPassport) SuccessGreen.copy(alpha = 0.12f) else CreamBg,
                    border = BorderStroke(
                        1.dp,
                        if (holdsPassport) SuccessGreen.copy(alpha = 0.35f) else BorderCardSlate
                    )
                ) {
                    Text(
                        text = if (holdsPassport) stringResource(R.string.label_holds_passport) else stringResource(R.string.label_tourist_entry),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (holdsPassport) SuccessGreen else InkSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Authentic Flavor pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InfoTag(label = "Commute", value = flavor.commonTransportMode)
                InfoTag(label = "Hospital", value = flavor.privateHospitalName)
                InfoTag(label = "Money App", value = flavor.popularMoneyAppOrBank ?: "Banking")
            }

            // PPP Cash Conversion & Logistics
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CreamBg.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, BorderCardSlate)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.label_flight_moving_fee, formatMoney(flightFee, character.countryCode)),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (canAfford) InkSecondary else CoralNegative
                        )
                        Text(
                            text = "PPP Preview: ${destination.currencySymbol}${convertedCash} ${destination.currencyCode}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }

                    Button(
                        onClick = onSelect,
                        enabled = canAfford,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Emigrate",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
