package com.maisha.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maisha.game.R
import com.maisha.game.data.CountryCatalog
import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.Character
import com.maisha.game.ui.theme.AccentPink
import com.maisha.game.ui.theme.CreamBg
import com.maisha.game.ui.theme.Hairline
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.TealPrimary
import com.maisha.game.util.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemittanceSheet(
    character: Character,
    onSendRemittance: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val homeCountry = CountryCatalog.getCountry(character.birthCountryCode)
    val currentCountry = CountryCatalog.getCountry(character.countryCode)

    val step1 = EconomyScaler.scaleAmount(5_000, character.countryCode)
    val step2 = EconomyScaler.scaleAmount(15_000, character.countryCode)
    val step3 = EconomyScaler.scaleAmount(50_000, character.countryCode)

    var sendAmount by remember { mutableIntStateOf(step2) }
    val receivedAmount = EconomyScaler.convertCurrency(
        sendAmount,
        fromCountryCode = character.countryCode,
        toCountryCode = character.birthCountryCode
    )

    val relationBoost = when {
        sendAmount >= step3 -> 25
        sendAmount >= step2 -> 15
        else -> 10
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PaperWhite,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(TealPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Public,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.remittance_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    Text(
                        text = stringResource(R.string.remittance_subtitle, homeCountry.displayName),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkTertiary
                    )
                }
            }

            // Currency Exchange conversion card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CreamBg),
                border = BorderStroke(1.dp, Hairline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "You Send (${currentCountry.currencyCode})",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkTertiary
                            )
                            Text(
                                text = formatMoney(sendAmount, character.countryCode),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary
                            )
                        }

                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(28.dp)
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "They Receive (${homeCountry.currencyCode})",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkTertiary
                            )
                            Text(
                                text = formatMoney(receivedAmount, character.birthCountryCode),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = LifeGreen
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.remittance_fx_label) + ": ${formatMoney(receivedAmount, character.birthCountryCode)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkTertiary
                    )
                }
            }

            // Amount Presets
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select Wire Amount",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = InkPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RemittancePresetChip(
                        label = formatMoney(step1, character.countryCode),
                        isSelected = sendAmount == step1,
                        onClick = { sendAmount = step1 },
                        modifier = Modifier.weight(1f)
                    )
                    RemittancePresetChip(
                        label = formatMoney(step2, character.countryCode),
                        isSelected = sendAmount == step2,
                        onClick = { sendAmount = step2 },
                        modifier = Modifier.weight(1f)
                    )
                    RemittancePresetChip(
                        label = formatMoney(step3, character.countryCode),
                        isSelected = sendAmount == step3,
                        onClick = { sendAmount = step3 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Impact Preview Card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PaperWhite,
                border = BorderStroke(1.dp, Hairline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = AccentPink,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.remittance_relation_preview, relationBoost),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = InkPrimary
                        )
                        Text(
                            text = "Boosts family relationships, happiness, and Karma back home.",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkTertiary
                        )
                    }
                }
            }

            // Lifetime sent counter
            if (character.finance.lifetimeRemittancesSent > 0) {
                Text(
                    text = stringResource(
                        R.string.remittance_lifetime,
                        formatMoney(character.finance.lifetimeRemittancesSent, character.countryCode)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = InkTertiary
                )
            }

            // Send Button
            Button(
                onClick = { onSendRemittance(sendAmount) },
                enabled = character.stats.money >= sendAmount && sendAmount > 0,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.remittance_send_button),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RemittancePresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) TealPrimary.copy(alpha = 0.15f) else CreamBg,
        border = BorderStroke(1.dp, if (isSelected) TealPrimary else Hairline),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) TealPrimary else InkPrimary
            )
        }
    }
}
