// app/src/main/java/com/maisha/game/ui/components/BusinessProgressionCard.kt
package com.maisha.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maisha.game.R
import com.maisha.game.data.model.Business
import com.maisha.game.data.model.BusinessIndustry
import com.maisha.game.data.model.CompanyStage
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BusinessProgressionCard(
    business: Business,
    countryCode: String,
    onLaunchProduct: (String) -> Unit,
    onScaleTeam: (String) -> Unit,
    onPivot: (Business) -> Unit,
    onRaiseCapital: (Business) -> Unit,
    onSellBusiness: (Business) -> Unit,
    onLiquidateBusiness: (Business) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, BorderCardSlate)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Company Name, Industry & Stage Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(TealPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppIcons.Rocket,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = business.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = businessIndustryText(business.industry),
                            style = MaterialTheme.typography.labelSmall,
                            color = InkTertiary
                        )
                    }
                }

                // Company Stage Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GoldAccent.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = stageLabel(business.stage),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Key Financial Metrics Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CreamBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.label_business_equity),
                        style = MaterialTheme.typography.labelSmall,
                        color = InkTertiary
                    )
                    Text(
                        text = "${business.equityShare}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.label_burn_rate),
                        style = MaterialTheme.typography.labelSmall,
                        color = InkTertiary
                    )
                    Text(
                        text = formatMoney(business.annualBurn, countryCode),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = CoralNegative
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.format_business_valuation, "").trim(),
                        style = MaterialTheme.typography.labelSmall,
                        color = InkTertiary
                    )
                    Text(
                        text = formatMoney(business.valuation, countryCode),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                }
            }

            // Operations & Morale Indicators
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stringResource(R.string.label_team_morale)}: ${business.teamMorale}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkSecondary
                    )
                    Text(
                        text = stringResource(R.string.format_business_employees, business.employeeCount),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = InkPrimary
                    )
                }
                LinearProgressIndicator(
                    progress = { business.teamMorale / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (business.teamMorale >= 60) SuccessGreen else CoralNegative,
                    trackColor = BorderCardSlate
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stringResource(R.string.label_product_quality)}: ${business.productQuality}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkSecondary
                    )
                    val profitColor = if (business.lastYearProfit >= 0) SuccessGreen else CoralNegative
                    val profitText = if (business.lastYearProfit >= 0) {
                        stringResource(R.string.format_business_profit, formatMoney(business.lastYearProfit, countryCode))
                    } else {
                        stringResource(R.string.format_business_loss, formatMoney(-business.lastYearProfit, countryCode))
                    }
                    Text(
                        text = profitText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = profitColor
                    )
                }
                LinearProgressIndicator(
                    progress = { business.productQuality / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = TealPrimary,
                    trackColor = BorderCardSlate
                )
            }

            // Status Banner if action taken this year
            if (business.actionDoneThisYear) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CreamBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.msg_business_action_done),
                        style = MaterialTheme.typography.labelSmall,
                        color = InkTertiary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Interactive Action Buttons
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Launch Product
                Button(
                    onClick = { onLaunchProduct(business.id) },
                    enabled = !business.actionDoneThisYear,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    contentPadding = ButtonDefaults.ContentPadding
                ) {
                    Icon(AppIcons.Rocket, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.btn_launch_product))
                }

                // Scale Team
                OutlinedButton(
                    onClick = { onScaleTeam(business.id) },
                    enabled = !business.actionDoneThisYear,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, TealPrimary)
                ) {
                    Icon(AppIcons.Career, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.btn_scale_expand), color = TealPrimary)
                }

                // Pivot Industry
                OutlinedButton(
                    onClick = { onPivot(business) },
                    enabled = !business.actionDoneThisYear,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(AppIcons.Travel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.btn_pivot_business))
                }

                // Raise Capital
                if (business.equityShare >= 25 && business.stage != CompanyStage.ENTERPRISE) {
                    OutlinedButton(
                        onClick = { onRaiseCapital(business) },
                        enabled = !business.actionDoneThisYear,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GoldAccent)
                    ) {
                        Icon(AppIcons.Money, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.btn_raise_capital), color = GoldAccent)
                    }
                }

                // Sell Exit
                OutlinedButton(
                    onClick = { onSellBusiness(business) },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SuccessGreen)
                ) {
                    Text(stringResource(R.string.btn_sell_business), color = SuccessGreen)
                }

                // Liquidate
                OutlinedButton(
                    onClick = { onLiquidateBusiness(business) },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CoralNegative)
                ) {
                    Text(stringResource(R.string.btn_liquidate_business), color = CoralNegative)
                }
            }
        }
    }
}

@Composable
fun stageLabel(stage: CompanyStage): String = when (stage) {
    CompanyStage.BOOTSTRAP -> stringResource(R.string.stage_bootstrap)
    CompanyStage.SEED -> stringResource(R.string.stage_seed)
    CompanyStage.GROWTH -> stringResource(R.string.stage_growth)
    CompanyStage.EXPANSION -> stringResource(R.string.stage_expansion)
    CompanyStage.ENTERPRISE -> stringResource(R.string.stage_enterprise)
}

@Composable
fun businessIndustryText(industry: BusinessIndustry): String = when (industry) {
    BusinessIndustry.TECH -> stringResource(R.string.industry_tech)
    BusinessIndustry.RETAIL -> stringResource(R.string.industry_retail)
    BusinessIndustry.FOOD -> stringResource(R.string.industry_food)
    BusinessIndustry.REAL_ESTATE -> stringResource(R.string.industry_real_estate)
    BusinessIndustry.ENTERTAINMENT -> stringResource(R.string.industry_entertainment)
}
