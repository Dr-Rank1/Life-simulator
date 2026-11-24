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
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maisha.game.R
import com.maisha.game.data.CountryCatalog
import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.FinancialRiskLevel
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.CreamBg
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.Hairline
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.SuccessGreen
import com.maisha.game.ui.theme.TealPrimary
import com.maisha.game.util.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryFinanceSheet(
    character: Character,
    onDeposit: (Int) -> Unit,
    onWithdraw: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val profile = CountryCatalog.financeProfileFor(character.countryCode)
    val instrument = profile.primaryInstrument
    val minDeposit = EconomyScaler.scaleAmount(instrument.minimumDepositKenya, character.countryCode)
    val balance = character.finance.countryInstrumentBalance

    var selectedAmount by remember { mutableIntStateOf(minDeposit) }

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
                        imageVector = Icons.Rounded.AccountBalance,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = instrument.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    Text(
                        text = instrument.localName,
                        style = MaterialTheme.typography.labelMedium,
                        color = InkTertiary
                    )
                }
            }

            // Description card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CreamBg,
                border = BorderStroke(1.dp, Hairline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = instrument.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkPrimary
                    )
                }
            }

            // Metrics row: Yield & Tax Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Yield Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PaperWhite),
                    border = BorderStroke(1.dp, Hairline)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.country_finance_annual_yield),
                            style = MaterialTheme.typography.labelSmall,
                            color = InkTertiary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = LifeGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${(instrument.annualYieldPercent * 100).toInt()}% APY",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = LifeGreen
                            )
                        }
                    }
                }

                // Risk & Tax Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PaperWhite),
                    border = BorderStroke(1.dp, Hairline)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (instrument.isRetirementTaxAdvantaged) {
                                stringResource(R.string.country_finance_tax_advantaged)
                            } else {
                                stringResource(R.string.country_finance_standard_tax)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = InkTertiary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = instrument.riskLevel.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                        }
                    }
                }
            }

            // Current Invested Balance Card
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
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.country_finance_invested_balance),
                        style = MaterialTheme.typography.labelMedium,
                        color = InkTertiary
                    )
                    Text(
                        text = formatMoney(balance, character.countryCode),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )

                    val projectedDividend = (balance * instrument.annualYieldPercent).toInt()
                    if (projectedDividend > 0) {
                        Text(
                            text = "${stringResource(R.string.country_finance_projected_dividend)}: +${formatMoney(projectedDividend, character.countryCode)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = LifeGreen
                        )
                    }
                }
            }

            // Quick Amount Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Transaction Amount: ${formatMoney(selectedAmount, character.countryCode)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = InkPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val step1 = minDeposit
                    val step2 = minDeposit * 5
                    val step3 = minDeposit * 20

                    PresetAmountChip(
                        label = formatMoney(step1, character.countryCode),
                        isSelected = selectedAmount == step1,
                        onClick = { selectedAmount = step1 },
                        modifier = Modifier.weight(1f)
                    )
                    PresetAmountChip(
                        label = formatMoney(step2, character.countryCode),
                        isSelected = selectedAmount == step2,
                        onClick = { selectedAmount = step2 },
                        modifier = Modifier.weight(1f)
                    )
                    PresetAmountChip(
                        label = formatMoney(step3, character.countryCode),
                        isSelected = selectedAmount == step3,
                        onClick = { selectedAmount = step3 },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (balance == 0) {
                    Text(
                        text = stringResource(R.string.country_finance_min_deposit, formatMoney(minDeposit, character.countryCode)),
                        style = MaterialTheme.typography.labelSmall,
                        color = InkTertiary
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onWithdraw(selectedAmount)
                    },
                    enabled = balance >= selectedAmount && selectedAmount > 0,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(text = stringResource(R.string.country_finance_withdraw))
                }

                Button(
                    onClick = {
                        onDeposit(selectedAmount)
                    },
                    enabled = character.stats.money >= selectedAmount && selectedAmount >= minDeposit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(text = stringResource(R.string.country_finance_deposit))
                }
            }

            // Mobile Overdraft Buffer section (e.g. Kenya Fuliza)
            if (profile.mobileOverdraftFacility) {
                val maxLimit = EconomyScaler.scaleAmount(profile.mobileOverdraftLimitKenya, character.countryCode)
                val used = character.finance.mobileOverdraftUsed

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PaperWhite),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PhoneAndroid,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.country_finance_overdraft_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary
                            )
                        }
                        Text(
                            text = stringResource(R.string.country_finance_overdraft_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkTertiary
                        )
                        Text(
                            text = stringResource(
                                R.string.country_finance_overdraft_used,
                                formatMoney(used, character.countryCode),
                                formatMoney(maxLimit, character.countryCode)
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (used > 0) CoralNegative else LifeGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PresetAmountChip(
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
