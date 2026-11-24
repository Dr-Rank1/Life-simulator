// app/src/main/java/com/maisha/game/ui/main/AssetsScreen.kt
package com.maisha.game.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.TrendingUp
import com.maisha.game.data.CountryCatalog
import com.maisha.game.ui.components.CategoryFilterChipRow
import com.maisha.game.ui.components.ConfirmActionDialog
import com.maisha.game.ui.components.ConfirmSeverity
import com.maisha.game.ui.components.ConfirmableActionHost
import com.maisha.game.ui.components.CountryFinanceSheet
import com.maisha.game.ui.components.DynastyVaultStoreSheet
import com.maisha.game.ui.components.RemittanceSheet
import com.maisha.game.ui.components.TabPageHero
import com.maisha.game.ui.components.rememberConfirmableAction
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import com.maisha.game.ui.components.DrillDownRow
import com.maisha.game.ui.components.InsetCardContainer
import com.maisha.game.ui.components.InsetDivider
import com.maisha.game.ui.theme.AccentPink
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.NavyDeep
import com.maisha.game.ui.theme.PaperWhite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.R
import com.maisha.game.data.AssetCatalog
import com.maisha.game.data.CatalogAsset
import com.maisha.game.data.IllustrationCatalog
import com.maisha.game.data.local.OnboardingTips
import com.maisha.game.data.model.AnnualCashFlow
import com.maisha.game.data.model.Asset
import com.maisha.game.data.model.AssetType
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.CommunityStandingTier
import com.maisha.game.data.model.CreditRatingTier
import com.maisha.game.data.model.EconomicClimate
import com.maisha.game.data.model.Person
import com.maisha.game.data.model.RelationType
import com.maisha.game.domain.FinanceEngine
import com.maisha.game.ui.components.AssetHeroImage
import com.maisha.game.ui.components.DismissibleTipCard
import com.maisha.game.ui.components.EmptyStateCard
import com.maisha.game.ui.components.StatBar
import com.maisha.game.ui.components.StatType
import com.maisha.game.ui.illustrations.EmptyStateIllustration
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.CreamBg
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.MaishaRadius
import com.maisha.game.ui.theme.MaishaSpacing
import com.maisha.game.ui.theme.SuccessGreen
import com.maisha.game.ui.theme.TealPrimary
import com.maisha.game.util.formatMoney

private enum class AssetsCategory { ALL, OVERVIEW, OWNED, SHOP, MARKETS, ESTATE }
private enum class AssetSheet {
    NONE, REAL_ESTATE, VEHICLES, MARKETS, LIVING_STANDARD, CASH_FLOW_LEDGER, DEBT_LIABILITIES,
    STORE, COUNTRY_FINANCE, REMITTANCE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(
    character: Character,
    netWorth: Int,
    uiState: LifeUiState,
    snackbarHostState: SnackbarHostState,
    onPurchaseAsset: (String) -> Unit,
    onSellAsset: (String) -> Unit,
    onRepairAsset: (String) -> Unit,
    onRenovateAsset: (String) -> Unit,
    onRentOutProperty: (String) -> Unit,
    onEvictTenant: (String) -> Unit,
    onSaveWill: (Map<String, Int>?) -> Unit,
    willBeneficiaries: List<Person>,
    onInvestFunds: (Int) -> Unit,
    onWithdrawFunds: (Int) -> Unit,
    onDepositSavings: (Int) -> Unit,
    onWithdrawSavings: (Int) -> Unit,
    onSetLivingStandard: (com.maisha.game.data.model.LivingStandard) -> Unit,
    onSetPortfolioStrategy: (com.maisha.game.data.model.PortfolioStrategy) -> Unit,
    onAssetsMessageDismissed: () -> Unit,
    onDismissAssetsMarketsTip: () -> Unit = {},
    onPerformLeisure: (com.maisha.game.domain.LeisureActivity) -> Unit = {},
    onPayMortgage: (String) -> Unit = {},
    onPrepayMortgagePrincipal: (String, Int) -> Unit = { _, _ -> },
    onServiceAsset: (String) -> Unit = {},
    onDepositJuniorTrust: (Int) -> Unit = {},
    onRepayStudentLoan: (Int) -> Unit = {},
    billingProducts: Map<String, com.android.billingclient.api.ProductDetails> = emptyMap(),
    isBillingConnected: Boolean = false,
    isBillingAvailable: Boolean = true,
    isAdFree: Boolean = false,
    isCreatorPass: Boolean = false,
    onPurchaseProduct: (String) -> Unit = {},
    onDepositCountryInstrument: (Int) -> Unit = {},
    onWithdrawCountryInstrument: (Int) -> Unit = {},
    onSendRemittance: (Int) -> Unit = {},
    carMakeCatalog: com.maisha.game.data.content.CarMakeCatalog =
        com.maisha.game.data.content.FallbackCarMakeCatalog,
    streetCatalog: com.maisha.game.data.content.StreetCatalog =
        com.maisha.game.data.content.FallbackStreetCatalog,
    modifier: Modifier = Modifier
) {
    val pendingPurchase = rememberConfirmableAction<CatalogAsset>()
    val pendingRepair = rememberConfirmableAction<Asset>()
    val pendingRenovate = rememberConfirmableAction<Asset>()
    val pendingRentOut = rememberConfirmableAction<Asset>()
    val pendingEvict = rememberConfirmableAction<Asset>()
    val pendingInvest = rememberConfirmableAction<Int>()
    val pendingWithdraw = rememberConfirmableAction<Int>()
    val pendingSavingsDeposit = rememberConfirmableAction<Int>()
    val pendingSavingsWithdraw = rememberConfirmableAction<Int>()
    val pendingJuniorTrustDeposit = rememberConfirmableAction<Int>()
    val financeEngine = remember { FinanceEngine() }
    val cashFlow = remember(character) { financeEngine.computeAnnualCashFlow(character) }
    val isPrestigeEligible = character.socialEcosystem.currentStandingTier == CommunityStandingTier.PILLAR_OF_SOCIETY ||
        character.socialEcosystem.currentStandingTier == CommunityStandingTier.WELL_RESPECTED
    var activeAssetSheet by rememberSaveable { mutableStateOf(AssetSheet.NONE) }
    var selectedAssetForAction by remember { mutableStateOf<Asset?>(null) }
    var showWillEditor by remember { mutableStateOf(false) }
    var showInvestDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showSavingsDepositDialog by remember { mutableStateOf(false) }
    var showSavingsWithdrawDialog by remember { mutableStateOf(false) }
    var showJuniorTrustDialog by remember { mutableStateOf(false) }
    var selectedCategory by rememberSaveable { mutableIntStateOf(0) }
    var shopTypeFilter by rememberSaveable { mutableStateOf("ALL") }
    val category = AssetsCategory.entries.getOrElse(selectedCategory) { AssetsCategory.ALL }
    fun show(cat: AssetsCategory): Boolean =
        category == AssetsCategory.ALL || category == cat

    val chipLabels = listOf(
        stringResource(R.string.chip_assets_all),
        stringResource(R.string.chip_assets_overview),
        stringResource(R.string.chip_assets_owned),
        stringResource(R.string.chip_assets_shop),
        stringResource(R.string.chip_assets_markets),
        stringResource(R.string.chip_assets_estate)
    )
    val shopCatalog = AssetCatalog.withPropertyListings(
        character.countryCode,
        carMakeCatalog,
        streetCatalog
    ).filter { item ->
        when (shopTypeFilter) {
            "HOUSE" -> item.type == AssetType.HOUSE
            "CAR" -> item.type == AssetType.CAR
            "MOTORBIKE" -> item.type == AssetType.MOTORBIKE
            "HEIRLOOM" -> item.type == AssetType.HEIRLOOM
            else -> true
        }
    }
    val livingCost = financeEngine.estimateAnnualCostOfLiving(character)

    LaunchedEffect(uiState.assetsMessage) {
        uiState.assetsMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onAssetsMessageDismissed()
        }
    }

    ConfirmableActionHost(
        state = pendingPurchase,
        onConfirmed = { item -> onPurchaseAsset(item.id) }
    ) { item, onConfirm, onDismiss ->
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_confirm_purchase_title),
            description = stringResource(
                R.string.dialog_confirm_purchase_body,
                item.name,
                formatMoney(item.purchasePrice, character.countryCode),
                formatMoney(item.monthlyUpkeep, character.countryCode)
            ),
            confirmLabel = stringResource(R.string.btn_buy),
            severity = ConfirmSeverity.NEUTRAL,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    ConfirmableActionHost(
        state = pendingRepair,
        onConfirmed = { asset -> onRepairAsset(asset.id) }
    ) { asset, onConfirm, onDismiss ->
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_repair_asset_title),
            description = stringResource(
                R.string.dialog_repair_asset_body,
                asset.name,
                formatMoney(financeEngine.calculateRepairCost(asset, character.countryCode), character.countryCode)
            ),
            confirmLabel = stringResource(R.string.btn_repair),
            severity = ConfirmSeverity.NEUTRAL,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    ConfirmableActionHost(
        state = pendingRenovate,
        onConfirmed = { asset -> onRenovateAsset(asset.id) }
    ) { asset, onConfirm, onDismiss ->
        val nextLevel = asset.renovationLevel + 1
        val cost = financeEngine.calculateRenovationCost(asset, character.countryCode)
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_renovate_asset_title),
            description = stringResource(
                R.string.dialog_renovate_asset_body,
                asset.name,
                nextLevel,
                formatMoney(cost, character.countryCode)
            ),
            confirmLabel = stringResource(R.string.btn_renovate),
            severity = ConfirmSeverity.NEUTRAL,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    ConfirmableActionHost(
        state = pendingRentOut,
        onConfirmed = { asset -> onRentOutProperty(asset.id) }
    ) { asset, onConfirm, onDismiss ->
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_rent_out_title),
            description = stringResource(
                R.string.dialog_rent_out_body,
                asset.name,
                formatMoney(financeEngine.estimateYearlyRent(asset), character.countryCode)
            ),
            confirmLabel = stringResource(R.string.btn_rent_out),
            severity = ConfirmSeverity.NEUTRAL,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    ConfirmableActionHost(
        state = pendingEvict,
        onConfirmed = { asset -> onEvictTenant(asset.id) }
    ) { asset, onConfirm, onDismiss ->
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_evict_title),
            description = stringResource(
                R.string.dialog_evict_body,
                asset.name,
                formatMoney(financeEngine.evictionFee(character), character.countryCode)
            ),
            confirmLabel = stringResource(R.string.btn_evict_tenant),
            severity = ConfirmSeverity.WARNING,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    ConfirmableActionHost(
        state = pendingInvest,
        onConfirmed = { amount -> onInvestFunds(amount) }
    ) { amount, onConfirm, onDismiss ->
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_invest_title),
            description = stringResource(
                R.string.dialog_invest_body,
                formatMoney(amount, character.countryCode)
            ),
            confirmLabel = stringResource(R.string.btn_deposit),
            severity = ConfirmSeverity.NEUTRAL,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    ConfirmableActionHost(
        state = pendingWithdraw,
        onConfirmed = { amount -> onWithdrawFunds(amount) }
    ) { amount, onConfirm, onDismiss ->
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_withdraw_title),
            description = stringResource(
                R.string.dialog_withdraw_body,
                formatMoney(amount, character.countryCode)
            ),
            confirmLabel = stringResource(R.string.btn_withdraw),
            severity = ConfirmSeverity.NEUTRAL,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    ConfirmableActionHost(
        state = pendingSavingsDeposit,
        onConfirmed = { amount -> onDepositSavings(amount) }
    ) { amount, onConfirm, onDismiss ->
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_savings_deposit_title),
            description = stringResource(
                R.string.dialog_savings_deposit_body,
                formatMoney(amount, character.countryCode)
            ),
            confirmLabel = stringResource(R.string.btn_deposit),
            severity = ConfirmSeverity.NEUTRAL,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    ConfirmableActionHost(
        state = pendingSavingsWithdraw,
        onConfirmed = { amount -> onWithdrawSavings(amount) }
    ) { amount, onConfirm, onDismiss ->
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_savings_withdraw_title),
            description = stringResource(
                R.string.dialog_savings_withdraw_body,
                formatMoney(amount, character.countryCode)
            ),
            confirmLabel = stringResource(R.string.btn_withdraw),
            severity = ConfirmSeverity.NEUTRAL,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    ConfirmableActionHost(
        state = pendingJuniorTrustDeposit,
        onConfirmed = { amount -> onDepositJuniorTrust(amount) }
    ) { amount, onConfirm, onDismiss ->
        ConfirmActionDialog(
            title = stringResource(R.string.junior_trust_title),
            description = "Deposit ${formatMoney(amount, character.countryCode)} into your Junior Trust Piggy Bank?",
            confirmLabel = stringResource(R.string.btn_deposit),
            severity = ConfirmSeverity.NEUTRAL,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBg)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "finance_summary_card") {
                FinanceSummaryCard(
                    character = character,
                    netWorth = netWorth,
                    cashFlow = cashFlow,
                    onOpenStore = { activeAssetSheet = AssetSheet.STORE },
                    onOpenCountryFinance = { activeAssetSheet = AssetSheet.COUNTRY_FINANCE },
                    onOpenRemittance = { activeAssetSheet = AssetSheet.REMITTANCE },
                    onOpenLedger = { activeAssetSheet = AssetSheet.CASH_FLOW_LEDGER },
                    onOpenBanking = { activeAssetSheet = AssetSheet.MARKETS },
                    onOpenDebt = { activeAssetSheet = AssetSheet.DEBT_LIABILITIES },
                    onOpenInvestments = { activeAssetSheet = AssetSheet.MARKETS },
                    onOpenJuniorTrust = { showJuniorTrustDialog = true }
                )
            }

            if (character.age < 16) {
                item(key = "dependent_minor") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = MaishaRadius.cardShape,
                        colors = CardDefaults.cardColors(containerColor = PaperWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, com.maisha.game.ui.theme.Hairline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(TealPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Home,
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Text(
                                text = stringResource(R.string.assets_minor_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = com.maisha.game.ui.theme.InkPrimary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(R.string.assets_minor_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkTertiary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                item(key = "minor_locked_assets") {
                    InsetCardContainer(title = stringResource(R.string.screen_assets)) {
                        DrillDownRow(
                            title = stringResource(R.string.asset_cat_real_estate),
                            subtitle = stringResource(R.string.asset_cat_real_estate_desc),
                            icon = Icons.Rounded.Home,
                            iconTint = LifeGreen,
                            isAgeLocked = true,
                            lockBadgeText = stringResource(R.string.badge_age_locked, 18),
                            insideContainer = true,
                            onClick = {}
                        )
                        InsetDivider()
                        DrillDownRow(
                            title = stringResource(R.string.asset_cat_vehicles),
                            subtitle = stringResource(R.string.asset_cat_vehicles_desc),
                            icon = Icons.Rounded.DirectionsCar,
                            iconTint = TealPrimary,
                            isAgeLocked = true,
                            lockBadgeText = stringResource(R.string.badge_age_locked, 18),
                            insideContainer = true,
                            onClick = {}
                        )
                    }
                }
            } else {
                val houses = character.assets.filter { it.type == AssetType.HOUSE }
                val vehicles = character.assets.filter {
                    it.type == AssetType.CAR || it.type == AssetType.MOTORBIKE ||
                        it.type == AssetType.BOAT || it.type == AssetType.PRIVATE_JET
                }
                val propertyEquity = houses.sumOf { (it.currentValue - it.mortgageBalance).coerceAtLeast(0) }
                val vehicleUpkeep = vehicles.sumOf { it.monthlyUpkeep * 12 }

                item(key = "drill_assets_group") {
                    InsetCardContainer(title = stringResource(R.string.screen_assets)) {
                        DrillDownRow(
                            title = stringResource(R.string.asset_cat_real_estate),
                            subtitle = if (houses.isNotEmpty()) {
                                "${houses.size} owned · Equity ${formatMoney(propertyEquity, character.countryCode)}"
                            } else {
                                stringResource(R.string.asset_cat_real_estate_desc)
                            },
                            icon = Icons.Rounded.Home,
                            iconTint = LifeGreen,
                            trailingBadge = if (houses.isNotEmpty()) "${houses.size}" else null,
                            trailingBadgeColor = LifeGreen,
                            insideContainer = true,
                            onClick = { activeAssetSheet = AssetSheet.REAL_ESTATE }
                        )

                        InsetDivider()

                        DrillDownRow(
                            title = stringResource(R.string.asset_cat_vehicles),
                            subtitle = if (vehicles.isNotEmpty()) {
                                "${vehicles.size} owned · Upkeep ${formatMoney(vehicleUpkeep, character.countryCode)}/yr"
                            } else {
                                stringResource(R.string.asset_cat_vehicles_desc)
                            },
                            icon = Icons.Rounded.DirectionsCar,
                            iconTint = TealPrimary,
                            trailingBadge = if (vehicles.isNotEmpty()) "${vehicles.size}" else null,
                            trailingBadgeColor = TealPrimary,
                            insideContainer = true,
                            onClick = { activeAssetSheet = AssetSheet.VEHICLES }
                        )

                        InsetDivider()

                        DrillDownRow(
                            title = stringResource(R.string.asset_cat_markets),
                            subtitle = "Portfolio: ${formatMoney(character.investmentPortfolioValue, character.countryCode)} · Savings: ${formatMoney(character.savingsBalance, character.countryCode)}",
                            icon = Icons.Rounded.Savings,
                            iconTint = GoldAccent,
                            insideContainer = true,
                            onClick = { activeAssetSheet = AssetSheet.MARKETS }
                        )

                        InsetDivider()

                        DrillDownRow(
                            title = stringResource(R.string.asset_cat_living),
                            subtitle = "${character.lifestyle.livingStandard.name} · ${formatMoney(livingCost, character.countryCode)}/yr",
                            icon = Icons.Rounded.Apartment,
                            iconTint = AccentPink,
                            insideContainer = true,
                            onClick = { activeAssetSheet = AssetSheet.LIVING_STANDARD }
                        )

                        if (willBeneficiaries.isNotEmpty() && character.alive) {
                            InsetDivider()

                            DrillDownRow(
                                title = stringResource(R.string.asset_cat_will),
                                subtitle = if (character.will != null) {
                                    stringResource(R.string.will_on_file)
                                } else {
                                    stringResource(R.string.asset_cat_will_desc)
                                },
                                icon = Icons.Rounded.Description,
                                iconTint = NavyDeep,
                                trailingBadge = if (character.will != null) "On File" else null,
                                trailingBadgeColor = if (character.will != null) SuccessGreen else null,
                                insideContainer = true,
                                onClick = { showWillEditor = true }
                            )
                        }
                    }
                }
            }

            if (show(AssetsCategory.MARKETS)) {
                item {
                    SavingsAccountCard(
                        savingsBalance = character.savingsBalance,
                        lastInterestPercent = character.lastSavingsInterestPercent,
                        countryCode = character.countryCode,
                        canDeposit = character.stats.money > 0 && character.alive,
                        canWithdraw = character.savingsBalance > 0 && character.alive,
                        onDeposit = { showSavingsDepositDialog = true },
                        onWithdraw = { showSavingsWithdrawDialog = true }
                    )
                }

                item {
                    InvestmentPortfolioCard(
                        portfolioValue = character.investmentPortfolioValue,
                        lastReturnPercent = character.lastPortfolioReturnPercent,
                        countryCode = character.countryCode,
                        currentStrategy = character.lifestyle.portfolioStrategy,
                        canDeposit = character.stats.money > 0 && character.alive,
                        canWithdraw = character.investmentPortfolioValue > 0 && character.alive,
                        onDeposit = { showInvestDialog = true },
                        onWithdraw = { showWithdrawDialog = true },
                        onSetStrategy = onSetPortfolioStrategy
                    )
                }
            }

            if (show(AssetsCategory.ESTATE) && willBeneficiaries.isNotEmpty() && character.alive) {
                item {
                    Text(
                        text = stringResource(R.string.section_estate_planning),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TealPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showWillEditor = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaishaRadius.buttonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text(stringResource(R.string.btn_last_will))
                    }
                    if (character.will != null) {
                        Text(
                            text = stringResource(R.string.will_on_file),
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessGreen,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            if (show(AssetsCategory.OWNED)) {
                item {
                    Text(
                        text = stringResource(R.string.section_owned_count, character.assets.size),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TealPrimary
                    )
                }

                if (character.assets.isEmpty()) {
                    item {
                        EmptyStateCard(
                            illustration = EmptyStateIllustration.ASSETS,
                            title = stringResource(R.string.section_owned_count, 0),
                            message = stringResource(R.string.empty_assets)
                        )
                    }
                } else {
                    items(character.assets, key = { it.id }) { asset ->
                        OwnedAssetCard(
                            asset = asset,
                            countryCode = character.countryCode,
                            currentGeneration = character.generationNumber,
                            repairCost = financeEngine.calculateRepairCost(asset, character.countryCode),
                            renovationCost = financeEngine.calculateRenovationCost(asset, character.countryCode),
                            yearlyYield = financeEngine.estimateYearlyRent(asset),
                            onLifestyleAction = onPerformLeisure,
                            onSell = { onSellAsset(asset.id) },
                            onRepair = { pendingRepair.request(asset) },
                            onRenovate = { pendingRenovate.request(asset) },
                            onRentOut = { pendingRentOut.request(asset) },
                            onEvict = { pendingEvict.request(asset) }
                        )
                    }
                }
            }

            if (show(AssetsCategory.SHOP)) {
                item {
                    Text(
                        text = stringResource(R.string.section_shop),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TealPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "ALL" to R.string.chip_shop_all,
                            "HOUSE" to R.string.chip_shop_house,
                            "CAR" to R.string.chip_shop_car,
                            "MOTORBIKE" to R.string.chip_shop_motorbike,
                            "HEIRLOOM" to R.string.chip_shop_heirloom
                        ).forEach { (key, labelRes) ->
                            val active = shopTypeFilter == key
                            Text(
                                text = stringResource(labelRes),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color.White else TealPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(if (active) LifeGreen else Color.White)
                                    .clickable { shopTypeFilter = key }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                items(shopCatalog, key = { it.id }) { catalogItem ->
                    ShopAssetCard(
                        item = catalogItem,
                        countryCode = character.countryCode,
                        canAfford = character.stats.money >= catalogItem.purchasePrice,
                        prestigeBadge = if (catalogItem.type == AssetType.HOUSE && isPrestigeEligible) {
                            stringResource(R.string.label_prestige_mortgage)
                        } else {
                            null
                        },
                        onBuy = { pendingPurchase.request(catalogItem) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showInvestDialog) {
        PortfolioTransferDialog(
            title = stringResource(R.string.dialog_invest_title),
            maxAmount = character.stats.money,
            countryCode = character.countryCode,
            confirmLabel = stringResource(R.string.btn_deposit),
            onDismiss = { showInvestDialog = false },
            onConfirm = { amount ->
                showInvestDialog = false
                pendingInvest.request(amount)
            }
        )
    }

    if (showWithdrawDialog) {
        PortfolioTransferDialog(
            title = stringResource(R.string.dialog_withdraw_title),
            maxAmount = character.investmentPortfolioValue,
            countryCode = character.countryCode,
            confirmLabel = stringResource(R.string.btn_withdraw),
            onDismiss = { showWithdrawDialog = false },
            onConfirm = { amount ->
                showWithdrawDialog = false
                pendingWithdraw.request(amount)
            }
        )
    }

    if (showSavingsDepositDialog) {
        PortfolioTransferDialog(
            title = stringResource(R.string.dialog_savings_deposit_title),
            maxAmount = character.stats.money,
            countryCode = character.countryCode,
            confirmLabel = stringResource(R.string.btn_deposit),
            onDismiss = { showSavingsDepositDialog = false },
            onConfirm = { amount ->
                showSavingsDepositDialog = false
                pendingSavingsDeposit.request(amount)
            }
        )
    }

    if (showSavingsWithdrawDialog) {
        PortfolioTransferDialog(
            title = stringResource(R.string.dialog_savings_withdraw_title),
            maxAmount = character.savingsBalance,
            countryCode = character.countryCode,
            confirmLabel = stringResource(R.string.btn_withdraw),
            onDismiss = { showSavingsWithdrawDialog = false },
            onConfirm = { amount ->
                showSavingsWithdrawDialog = false
                pendingSavingsWithdraw.request(amount)
            }
        )
    }

    if (showJuniorTrustDialog) {
        PortfolioTransferDialog(
            title = stringResource(R.string.junior_trust_title),
            maxAmount = character.stats.money,
            countryCode = character.countryCode,
            confirmLabel = stringResource(R.string.btn_deposit),
            onDismiss = { showJuniorTrustDialog = false },
            onConfirm = { amount ->
                showJuniorTrustDialog = false
                pendingJuniorTrustDeposit.request(amount)
            }
        )
    }

    if (showWillEditor) {
        WillEditorDialog(
            beneficiaries = willBeneficiaries,
            initialWill = character.will,
            onDismiss = { showWillEditor = false },
            onSave = { will ->
                onSaveWill(will)
                showWillEditor = false
            },
            onClear = {
                onSaveWill(null)
                showWillEditor = false
            }
        )
    }

    when (activeAssetSheet) {
        AssetSheet.REAL_ESTATE -> {
            val houses = character.assets.filter { it.type == AssetType.HOUSE }
            val realEstateCatalog = remember(character.countryCode) {
                AssetCatalog.getPurchasableAssetsForCountry(character.countryCode).filter { it.type == AssetType.HOUSE }
            }
            ModalBottomSheet(
                onDismissRequest = { activeAssetSheet = AssetSheet.NONE },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = PaperWhite
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.asset_cat_real_estate),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Owned Properties",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TealPrimary
                    )
                    if (houses.isEmpty()) {
                        Text(
                            text = "You do not own any real estate yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkTertiary
                        )
                    } else {
                        houses.forEach { house ->
                            DrillDownRow(
                                title = house.name,
                                subtitle = "Value: ${formatMoney(house.currentValue, character.countryCode)} · Equity: ${formatMoney(house.currentValue - house.mortgageBalance, character.countryCode)}",
                                icon = AppIcons.NavAssets,
                                iconTint = LifeGreen,
                                trailingBadge = if (house.mortgageBalance > 0) "Mortgage" else if (house.isRentedOut) "Rented" else "Owned",
                                trailingBadgeColor = if (house.isRentedOut) LifeGreen else if (house.mortgageBalance > 0) GoldAccent else TealPrimary,
                                onClick = {
                                    activeAssetSheet = AssetSheet.NONE
                                    selectedAssetForAction = house
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Property Market",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TealPrimary
                    )
                    realEstateCatalog.forEach { catalogItem ->
                        ShopAssetCard(
                            item = catalogItem,
                            countryCode = character.countryCode,
                            canAfford = character.stats.money >= catalogItem.purchasePrice,
                            prestigeBadge = if (catalogItem.type == AssetType.HOUSE && isPrestigeEligible) {
                                stringResource(R.string.label_prestige_mortgage)
                            } else {
                                null
                            },
                            onBuy = {
                                activeAssetSheet = AssetSheet.NONE
                                pendingPurchase.request(catalogItem)
                            }
                        )
                    }
                }
            }
        }
        AssetSheet.VEHICLES -> {
            val vehicles = character.assets.filter {
                it.type == AssetType.CAR || it.type == AssetType.MOTORBIKE ||
                    it.type == AssetType.BOAT || it.type == AssetType.PRIVATE_JET
            }
            val vehiclesCatalog = remember(character.countryCode) {
                AssetCatalog.getPurchasableAssetsForCountry(character.countryCode).filter {
                    it.type == AssetType.CAR || it.type == AssetType.MOTORBIKE ||
                        it.type == AssetType.BOAT || it.type == AssetType.PRIVATE_JET
                }
            }
            ModalBottomSheet(
                onDismissRequest = { activeAssetSheet = AssetSheet.NONE },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = PaperWhite
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.asset_cat_vehicles),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Owned Vehicles & Aircraft",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TealPrimary
                    )
                    if (vehicles.isEmpty()) {
                        Text(
                            text = "You do not own any vehicles yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkTertiary
                        )
                    } else {
                        vehicles.forEach { vehicle ->
                            DrillDownRow(
                                title = vehicle.name,
                                subtitle = "Value: ${formatMoney(vehicle.currentValue, character.countryCode)} · Condition: ${vehicle.condition}%",
                                icon = if (vehicle.type == AssetType.BOAT || vehicle.type == AssetType.PRIVATE_JET) AppIcons.Travel else AppIcons.Career,
                                iconTint = TealPrimary,
                                onClick = {
                                    activeAssetSheet = AssetSheet.NONE
                                    selectedAssetForAction = vehicle
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Dealerships",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TealPrimary
                    )
                    vehiclesCatalog.forEach { catalogItem ->
                        ShopAssetCard(
                            item = catalogItem,
                            countryCode = character.countryCode,
                            canAfford = character.stats.money >= catalogItem.purchasePrice,
                            onBuy = {
                                activeAssetSheet = AssetSheet.NONE
                                pendingPurchase.request(catalogItem)
                            }
                        )
                    }
                }
            }
        }
        AssetSheet.MARKETS -> {
            ModalBottomSheet(
                onDismissRequest = { activeAssetSheet = AssetSheet.NONE },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = PaperWhite
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.asset_cat_markets),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    SavingsAccountCard(
                        savingsBalance = character.savingsBalance,
                        lastInterestPercent = character.lastSavingsInterestPercent,
                        countryCode = character.countryCode,
                        canDeposit = character.stats.money > 0 && character.alive,
                        canWithdraw = character.savingsBalance > 0 && character.alive,
                        onDeposit = { showSavingsDepositDialog = true },
                        onWithdraw = { showSavingsWithdrawDialog = true }
                    )
                    InvestmentPortfolioCard(
                        portfolioValue = character.investmentPortfolioValue,
                        lastReturnPercent = character.lastPortfolioReturnPercent,
                        countryCode = character.countryCode,
                        currentStrategy = character.lifestyle.portfolioStrategy,
                        canDeposit = character.stats.money > 0 && character.alive,
                        canWithdraw = character.investmentPortfolioValue > 0 && character.alive,
                        onDeposit = { showInvestDialog = true },
                        onWithdraw = { showWithdrawDialog = true },
                        onSetStrategy = onSetPortfolioStrategy
                    )
                }
            }
        }
        AssetSheet.LIVING_STANDARD -> {
            ModalBottomSheet(
                onDismissRequest = { activeAssetSheet = AssetSheet.NONE },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = PaperWhite
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.asset_cat_living),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    LivingStandardCard(
                        character = character,
                        estimatedCost = livingCost,
                        onSelect = onSetLivingStandard
                    )
                }
            }
        }
        AssetSheet.CASH_FLOW_LEDGER -> {
            CashFlowLedgerSheet(
                character = character,
                cashFlow = cashFlow,
                onDismiss = { activeAssetSheet = AssetSheet.NONE }
            )
        }
        AssetSheet.DEBT_LIABILITIES -> {
            DebtLiabilitiesSheet(
                character = character,
                onPayMortgage = { id ->
                    activeAssetSheet = AssetSheet.NONE
                    onPayMortgage(id)
                },
                onPrepayMortgagePrincipal = { id, amount ->
                    activeAssetSheet = AssetSheet.NONE
                    onPrepayMortgagePrincipal(id, amount)
                },
                onRepayStudentLoan = { amount ->
                    activeAssetSheet = AssetSheet.NONE
                    onRepayStudentLoan(amount)
                },
                onDismiss = { activeAssetSheet = AssetSheet.NONE }
            )
        }
        AssetSheet.STORE -> {
            DynastyVaultStoreSheet(
                character = character,
                billingProducts = billingProducts,
                isBillingConnected = isBillingConnected,
                isBillingAvailable = isBillingAvailable,
                isAdFree = isAdFree,
                isCreatorPass = isCreatorPass,
                onPurchaseProduct = onPurchaseProduct,
                onDismiss = { activeAssetSheet = AssetSheet.NONE }
            )
        }
        AssetSheet.COUNTRY_FINANCE -> {
            CountryFinanceSheet(
                character = character,
                onDeposit = { amount ->
                    activeAssetSheet = AssetSheet.NONE
                    onDepositCountryInstrument(amount)
                },
                onWithdraw = { amount ->
                    activeAssetSheet = AssetSheet.NONE
                    onWithdrawCountryInstrument(amount)
                },
                onDismiss = { activeAssetSheet = AssetSheet.NONE }
            )
        }
        AssetSheet.REMITTANCE -> {
            RemittanceSheet(
                character = character,
                onSendRemittance = { amount ->
                    activeAssetSheet = AssetSheet.NONE
                    onSendRemittance(amount)
                },
                onDismiss = { activeAssetSheet = AssetSheet.NONE }
            )
        }
        AssetSheet.NONE -> Unit
    }

    selectedAssetForAction?.let { asset ->
        ModalBottomSheet(
            onDismissRequest = { selectedAssetForAction = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = PaperWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AssetHeroImage(
                    ref = IllustrationCatalog.getIllustrationForOwnedAsset(asset),
                    height = 130.dp,
                    contentDescription = asset.name,
                    cornerRadius = 16.dp
                )
                Text(
                    text = asset.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Value: ${formatMoney(asset.currentValue, character.countryCode)}", fontWeight = FontWeight.SemiBold, color = GoldAccent)
                    Text("Equity: ${formatMoney(asset.currentValue - asset.mortgageBalance, character.countryCode)}", fontWeight = FontWeight.SemiBold, color = SuccessGreen)
                }
                Text("Condition: ${asset.condition}% · Upkeep: ${formatMoney(asset.monthlyUpkeep * 12, character.countryCode)}/yr", style = MaterialTheme.typography.bodySmall, color = InkTertiary)

                Spacer(modifier = Modifier.height(4.dp))

                if (asset.mortgageBalance > 0) {
                    DrillDownRow(
                        title = stringResource(R.string.btn_pay_mortgage),
                        subtitle = "Balance: ${formatMoney(asset.mortgageBalance, character.countryCode)} (Payment: ${formatMoney(asset.annualMortgagePayment, character.countryCode)}/yr)",
                        icon = AppIcons.Money,
                        iconTint = LifeGreen,
                        enabled = character.stats.money >= asset.mortgageBalance,
                        onClick = {
                            val id = asset.id
                            selectedAssetForAction = null
                            onPayMortgage(id)
                        }
                    )

                    val prepayChunk = minOf(asset.mortgageBalance / 4, character.stats.money).coerceAtLeast(1_000)
                    if (character.stats.money >= prepayChunk && prepayChunk < asset.mortgageBalance) {
                        DrillDownRow(
                            title = stringResource(R.string.btn_prepay_mortgage_principal, formatMoney(prepayChunk, character.countryCode)),
                            subtitle = stringResource(R.string.desc_prepay_mortgage_principal),
                            icon = AppIcons.Money,
                            iconTint = GoldAccent,
                            enabled = true,
                            onClick = {
                                val id = asset.id
                                selectedAssetForAction = null
                                onPrepayMortgagePrincipal(id, prepayChunk)
                            }
                        )
                    }
                }

                if (!asset.isHeirloom && !asset.servicedThisYear) {
                    val serviceFee = (financeEngine.calculateRepairCost(asset, character.countryCode) * 0.4f).toInt().coerceAtLeast(1_500)
                    DrillDownRow(
                        title = stringResource(R.string.btn_service_asset, formatMoney(serviceFee, character.countryCode)),
                        subtitle = stringResource(R.string.desc_service_asset),
                        icon = AppIcons.Health,
                        iconTint = TealPrimary,
                        enabled = character.stats.money >= serviceFee,
                        onClick = {
                            val id = asset.id
                            selectedAssetForAction = null
                            onServiceAsset(id)
                        }
                    )
                }

                if (asset.type == AssetType.HOUSE) {
                    val renovationCost = financeEngine.calculateRenovationCost(asset, character.countryCode)
                    if (asset.renovationLevel < 3 && asset.condition >= FinanceEngine.RENOVATE_MIN_CONDITION) {
                        DrillDownRow(
                            title = stringResource(R.string.btn_renovate_with_cost, formatMoney(renovationCost, character.countryCode)),
                            subtitle = "Upgrade to Level ${asset.renovationLevel + 1}",
                            icon = AppIcons.NavAssets,
                            iconTint = TealPrimary,
                            enabled = character.stats.money >= renovationCost,
                            onClick = {
                                selectedAssetForAction = null
                                pendingRenovate.request(asset)
                            }
                        )
                    }
                    if (asset.isRentedOut) {
                        DrillDownRow(
                            title = stringResource(R.string.btn_evict_tenant),
                            subtitle = "Tenant satisfaction: ${asset.tenantHappiness ?: 0}%",
                            icon = AppIcons.Family,
                            iconTint = CoralNegative,
                            onClick = {
                                selectedAssetForAction = null
                                pendingEvict.request(asset)
                            }
                        )
                    } else {
                        val yield = financeEngine.estimateYearlyRent(asset)
                        DrillDownRow(
                            title = stringResource(R.string.btn_rent_out),
                            subtitle = "Est. yearly yield: ${formatMoney(yield, character.countryCode)}",
                            icon = AppIcons.Family,
                            iconTint = LifeGreen,
                            onClick = {
                                selectedAssetForAction = null
                                pendingRentOut.request(asset)
                            }
                        )
                    }
                }

                if (asset.condition < FinanceEngine.REPAIR_UI_THRESHOLD) {
                    val repairCost = financeEngine.calculateRepairCost(asset, character.countryCode)
                    DrillDownRow(
                        title = stringResource(R.string.btn_repair_with_cost, formatMoney(repairCost, character.countryCode)),
                        icon = AppIcons.Performance,
                        iconTint = TealPrimary,
                        enabled = character.stats.money >= repairCost,
                        onClick = {
                            selectedAssetForAction = null
                            pendingRepair.request(asset)
                        }
                    )
                }

                if (asset.type == AssetType.BOAT) {
                    DrillDownRow(
                        title = stringResource(R.string.leisure_yacht_day_title),
                        subtitle = stringResource(R.string.leisure_yacht_day_desc),
                        icon = AppIcons.Travel,
                        iconTint = TealPrimary,
                        onClick = {
                            selectedAssetForAction = null
                            onPerformLeisure(com.maisha.game.domain.LeisureActivity.YACHT_DAY)
                        }
                    )
                }

                if (asset.type == AssetType.PRIVATE_JET) {
                    DrillDownRow(
                        title = stringResource(R.string.leisure_private_jet_title),
                        subtitle = stringResource(R.string.leisure_private_jet_desc),
                        icon = AppIcons.Travel,
                        iconTint = GoldAccent,
                        onClick = {
                            selectedAssetForAction = null
                            onPerformLeisure(com.maisha.game.domain.LeisureActivity.PRIVATE_JET_TRIP)
                        }
                    )
                }

                DrillDownRow(
                    title = stringResource(R.string.btn_sell),
                    subtitle = "Estimated proceeds: ${formatMoney(asset.currentValue - asset.mortgageBalance, character.countryCode)}",
                    icon = AppIcons.Money,
                    iconTint = CoralNegative,
                    onClick = {
                        val id = asset.id
                        selectedAssetForAction = null
                        onSellAsset(id)
                    }
                )
            }
        }
    }
}

@Composable
private fun LivingStandardCard(
    character: Character,
    estimatedCost: Int,
    onSelect: (com.maisha.game.data.model.LivingStandard) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaishaRadius.cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.section_living_standard),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TealPrimary
            )
            Text(
                text = stringResource(
                    R.string.format_living_cost_estimate,
                    formatMoney(estimatedCost, character.countryCode)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                com.maisha.game.data.model.LivingStandard.entries.forEach { standard ->
                    val selected = character.lifestyle.livingStandard == standard
                    FilterChip(
                        selected = selected,
                        onClick = { onSelect(standard) },
                        enabled = character.alive,
                        label = {
                            Text(
                                text = when (standard) {
                                    com.maisha.game.data.model.LivingStandard.FRUGAL ->
                                        stringResource(R.string.living_standard_frugal)
                                    com.maisha.game.data.model.LivingStandard.MODEST ->
                                        stringResource(R.string.living_standard_modest)
                                    com.maisha.game.data.model.LivingStandard.COMFORTABLE ->
                                        stringResource(R.string.living_standard_comfortable)
                                    com.maisha.game.data.model.LivingStandard.LUXURY ->
                                        stringResource(R.string.living_standard_luxury)
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SavingsAccountCard(
    savingsBalance: Int,
    lastInterestPercent: Int,
    countryCode: String,
    canDeposit: Boolean,
    canWithdraw: Boolean,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit
) {
    val interestLabel = when {
        savingsBalance <= 0 -> stringResource(R.string.savings_no_interest_yet)
        else -> stringResource(R.string.format_savings_interest, lastInterestPercent)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaishaRadius.cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.section_savings),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TealPrimary
            )
            Text(
                text = formatMoney(savingsBalance, countryCode),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = GoldAccent
            )
            Text(
                text = interestLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.savings_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDeposit,
                    enabled = canDeposit,
                    modifier = Modifier.weight(1f),
                    shape = MaishaRadius.buttonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text(stringResource(R.string.btn_deposit))
                }
                OutlinedButton(
                    onClick = onWithdraw,
                    enabled = canWithdraw,
                    modifier = Modifier.weight(1f),
                    shape = MaishaRadius.buttonShape
                ) {
                    Text(stringResource(R.string.btn_withdraw))
                }
            }
        }
    }
}

@Composable
private fun InvestmentPortfolioCard(
    portfolioValue: Int,
    lastReturnPercent: Int,
    countryCode: String,
    currentStrategy: com.maisha.game.data.model.PortfolioStrategy,
    canDeposit: Boolean,
    canWithdraw: Boolean,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit,
    onSetStrategy: (com.maisha.game.data.model.PortfolioStrategy) -> Unit
) {
    val returnColor = when {
        lastReturnPercent > 0 -> SuccessGreen
        lastReturnPercent < 0 -> CoralNegative
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val returnLabel = when {
        portfolioValue <= 0 -> stringResource(R.string.portfolio_no_return_yet)
        lastReturnPercent > 0 -> stringResource(R.string.format_portfolio_return_pos, lastReturnPercent)
        lastReturnPercent < 0 -> stringResource(R.string.format_portfolio_return_neg, lastReturnPercent)
        else -> stringResource(R.string.format_portfolio_return_flat)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaishaRadius.cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.section_investment_portfolio),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TealPrimary
            )
            Text(
                text = formatMoney(portfolioValue, countryCode),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = GoldAccent
            )
            Text(
                text = returnLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = returnColor
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDeposit,
                    enabled = canDeposit,
                    modifier = Modifier.weight(1f),
                    shape = MaishaRadius.buttonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text(stringResource(R.string.btn_deposit))
                }
                OutlinedButton(
                    onClick = onWithdraw,
                    enabled = canWithdraw,
                    modifier = Modifier.weight(1f),
                    shape = MaishaRadius.buttonShape
                ) {
                    Text(stringResource(R.string.btn_withdraw))
                }
            }
            Text(
                text = stringResource(R.string.section_portfolio_strategy),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                com.maisha.game.data.model.PortfolioStrategy.entries.forEach { strategy ->
                    val selected = strategy == currentStrategy
                    FilterChip(
                        selected = selected,
                        onClick = { if (!selected) onSetStrategy(strategy) },
                        label = {
                            Text(
                                when (strategy) {
                                    com.maisha.game.data.model.PortfolioStrategy.CONSERVATIVE ->
                                        stringResource(R.string.portfolio_conservative)
                                    com.maisha.game.data.model.PortfolioStrategy.BALANCED ->
                                        stringResource(R.string.portfolio_balanced)
                                    com.maisha.game.data.model.PortfolioStrategy.AGGRESSIVE ->
                                        stringResource(R.string.portfolio_aggressive)
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PortfolioTransferDialog(
    title: String,
    maxAmount: Int,
    countryCode: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    if (maxAmount <= 0) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }
    var sliderValue by remember(maxAmount) {
        mutableFloatStateOf(maxAmount / 2f)
    }
    val amount = sliderValue.toInt().coerceIn(1, maxAmount)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = formatMoney(amount, countryCode),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 1f..maxAmount.toFloat()
                )
                Text(
                    text = stringResource(
                        R.string.format_transfer_max,
                        formatMoney(maxAmount, countryCode)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(amount) }) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

@Composable
private fun WillEditorDialog(
    beneficiaries: List<Person>,
    initialWill: Map<String, Int>?,
    onDismiss: () -> Unit,
    onSave: (Map<String, Int>) -> Unit,
    onClear: () -> Unit
) {
    val shares = remember(beneficiaries, initialWill) {
        mutableStateMapOf<String, Int>().apply {
            beneficiaries.forEach { person ->
                put(person.id, initialWill?.get(person.id) ?: 0)
            }
            if (initialWill == null && beneficiaries.isNotEmpty()) {
                val even = 100 / beneficiaries.size
                var remainder = 100 - even * beneficiaries.size
                beneficiaries.forEachIndexed { index, person ->
                    put(person.id, even + if (index == 0) remainder else 0)
                }
            }
        }
    }
    val total = shares.values.sum()
    val canSave = total == 100

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_will_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.dialog_will_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                beneficiaries.forEach { person ->
                    val share = shares[person.id] ?: 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = person.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = willRelationLabel(person),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = {
                                    shares[person.id] = (share - 5).coerceAtLeast(0)
                                },
                                enabled = share > 0
                            ) {
                                Text("−")
                            }
                            Text(
                                text = stringResource(R.string.format_will_percent, share),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            TextButton(
                                onClick = {
                                    shares[person.id] = (share + 5).coerceAtMost(100)
                                },
                                enabled = share < 100
                            ) {
                                Text("+")
                            }
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.format_will_total, total),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (canSave) SuccessGreen else CoralNegative
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(shares.toMap()) },
                enabled = canSave
            ) {
                Text(stringResource(R.string.btn_save_will))
            }
        },
        dismissButton = {
            Row {
                if (initialWill != null) {
                    TextButton(onClick = onClear) {
                        Text(stringResource(R.string.btn_clear_will))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        }
    )
}

@Composable
private fun willRelationLabel(person: Person): String = when (person.relation) {
    RelationType.SPOUSE -> if (person.isMarried) {
        stringResource(R.string.relation_spouse)
    } else {
        stringResource(R.string.relation_partner_dating)
    }
    RelationType.CHILD -> stringResource(R.string.relation_child)
    else -> person.relation.name
}

@Composable
private fun OwnedAssetCard(
    asset: Asset,
    countryCode: String,
    currentGeneration: Int,
    repairCost: Int,
    renovationCost: Int,
    yearlyYield: Int,
    onLifestyleAction: (com.maisha.game.domain.LeisureActivity) -> Unit = {},
    onSell: () -> Unit,
    onRepair: () -> Unit,
    onRenovate: () -> Unit,
    onRentOut: () -> Unit,
    onEvict: () -> Unit
) {
    val isHeirloom = asset.isHeirloom
    val generationsHeld = (currentGeneration - asset.generationAcquired).coerceAtLeast(0)
    val cardModifier = if (isHeirloom) {
        Modifier
            .fillMaxWidth()
            .shadow(8.dp, MaishaRadius.cardShape, spotColor = GoldAccent.copy(alpha = 0.45f))
            .border(1.5.dp, GoldAccent.copy(alpha = 0.85f), MaishaRadius.cardShape)
    } else {
        Modifier.fillMaxWidth()
    }

    Card(
        modifier = cardModifier,
        shape = MaishaRadius.cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isHeirloom) {
                GoldAccent.copy(alpha = 0.10f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column {
            AssetHeroImage(
                ref = IllustrationCatalog.getIllustrationForOwnedAsset(asset),
                height = if (isHeirloom) 120.dp else 132.dp,
                contentDescription = asset.name,
                cornerRadius = 12.dp
            )
            Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = asset.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isHeirloom) {
                        HeirloomBadge()
                        if (generationsHeld > 0) {
                            Text(
                                text = stringResource(
                                    R.string.format_heirloom_generations,
                                    generationsHeld
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldAccent
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.label_heirloom_new),
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldAccent
                            )
                        }
                    } else {
                        Text(
                            text = asset.type.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = formatMoney(asset.currentValue, countryCode),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                when {
                    asset.currentValue > asset.purchasePrice -> {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            contentDescription = stringResource(R.string.content_desc_asset_value_up),
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    asset.currentValue < asset.purchasePrice -> {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.content_desc_asset_value_down),
                            tint = CoralNegative,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            val delta = asset.currentValue - asset.purchasePrice
            val percent = if (asset.purchasePrice > 0) ((delta.toDouble() / asset.purchasePrice) * 100).toInt() else 0
            val equity = (asset.currentValue - asset.mortgageBalance).coerceAtLeast(0)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (asset.mortgageBalance > 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SuccessGreen.copy(alpha = 0.12f))
                                .border(1.dp, SuccessGreen.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.format_asset_equity, formatMoney(equity, countryCode)),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen,
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF1F5F9))
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.format_asset_mortgage, formatMoney(asset.mortgageBalance, countryCode)),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569),
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (delta != 0) {
                    val isGain = delta > 0
                    val pillBg = if (isGain) SuccessGreen.copy(alpha = 0.12f) else CoralNegative.copy(alpha = 0.12f)
                    val pillFg = if (isGain) SuccessGreen else CoralNegative
                    val pillText = if (isGain) {
                        stringResource(R.string.format_appreciated, percent, formatMoney(delta, countryCode))
                    } else {
                        stringResource(R.string.format_depreciated, percent, formatMoney(-delta, countryCode))
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(pillBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = pillText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = pillFg,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!isHeirloom) {
                StatBar(
                    type = StatType.CONDITION,
                    value = asset.condition,
                    label = stringResource(R.string.stat_condition),
                    barColorOverride = conditionBarColor(asset.condition)
                )

                Text(
                    text = stringResource(
                        R.string.format_upkeep_monthly,
                        formatMoney(asset.monthlyUpkeep, countryCode)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (asset.condition < FinanceEngine.REPAIR_UI_THRESHOLD) {
                    Button(
                        onClick = onRepair,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaishaRadius.buttonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.btn_repair_with_cost,
                                formatMoney(repairCost, countryCode)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (asset.type == AssetType.HOUSE &&
                    !asset.isHeirloom &&
                    asset.condition >= FinanceEngine.RENOVATE_MIN_CONDITION &&
                    asset.renovationLevel < 3
                ) {
                    OutlinedButton(
                        onClick = onRenovate,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaishaRadius.buttonShape
                    ) {
                        Text(
                            text = stringResource(
                                R.string.btn_renovate_with_cost,
                                formatMoney(renovationCost, countryCode)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (asset.type == AssetType.HOUSE && !asset.isHeirloom) {
                    if (asset.isRentedOut) {
                        Text(
                            text = stringResource(
                                R.string.format_yearly_yield,
                                formatMoney(yearlyYield, countryCode)
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = SuccessGreen,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        StatBar(
                            type = StatType.HAPPINESS,
                            value = asset.tenantHappiness ?: 0,
                            label = stringResource(R.string.label_tenant_happiness),
                            showIcon = false
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onEvict,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaishaRadius.buttonShape
                        ) {
                            Text(stringResource(R.string.btn_evict_tenant))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        Button(
                            onClick = onRentOut,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaishaRadius.buttonShape,
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            Text(stringResource(R.string.btn_rent_out))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (asset.type == AssetType.BOAT) {
                    Button(
                        onClick = { onLifestyleAction(com.maisha.game.domain.LeisureActivity.YACHT_DAY) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaishaRadius.buttonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_yacht_day),
                            color = Color(0xFF1A1200),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (asset.type == AssetType.PRIVATE_JET) {
                    Button(
                        onClick = { onLifestyleAction(com.maisha.game.domain.LeisureActivity.PRIVATE_JET_TRIP) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaishaRadius.buttonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_private_jet_trip),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = onSell,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaishaRadius.buttonShape
                ) {
                    Text(stringResource(R.string.btn_sell))
                }
            } else {
                Text(
                    text = stringResource(R.string.heirloom_no_upkeep),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            }
        }
    }
}

@Composable
private fun HeirloomBadge() {
    Text(
        text = stringResource(R.string.label_heirloom),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1A1200),
        modifier = Modifier
            .background(GoldAccent.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .border(1.dp, GoldAccent, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

private fun conditionBarColor(condition: Int): androidx.compose.ui.graphics.Color = when {
    condition >= 70 -> SuccessGreen
    condition >= 40 -> GoldAccent
    else -> CoralNegative
}

@Composable
private fun ShopAssetCard(
    item: CatalogAsset,
    countryCode: String,
    canAfford: Boolean,
    prestigeBadge: String? = null,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (canAfford) 1f else 0.65f),
        shape = MaishaRadius.cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            AssetHeroImage(
                ref = IllustrationCatalog.getIllustrationForCatalogAsset(item.id),
                height = 140.dp,
                contentDescription = item.name,
                cornerRadius = 12.dp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        R.string.format_asset_price_upkeep,
                        formatMoney(item.purchasePrice, countryCode),
                        formatMoney(item.monthlyUpkeep, countryCode)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (prestigeBadge != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = LifeGreen.copy(alpha = 0.15f),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = prestigeBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = LifeGreen,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (!canAfford) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = stringResource(R.string.content_desc_locked),
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.label_insufficient_funds),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Button(
                onClick = onBuy,
                enabled = canAfford,
                modifier = Modifier.heightIn(min = 48.dp),
                shape = MaishaRadius.buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    if (canAfford) {
                        stringResource(R.string.btn_buy)
                    } else {
                        stringResource(R.string.btn_locked)
                    }
                )
            }
            }
        }
    }
}

@Composable
private fun FinanceSummaryCard(
    character: Character,
    netWorth: Int,
    cashFlow: AnnualCashFlow,
    onOpenStore: () -> Unit,
    onOpenCountryFinance: () -> Unit,
    onOpenRemittance: () -> Unit,
    onOpenLedger: () -> Unit,
    onOpenBanking: () -> Unit,
    onOpenDebt: () -> Unit,
    onOpenInvestments: () -> Unit,
    onOpenJuniorTrust: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val creditTier = CreditRatingTier.forScore(character.finance.creditScore)
    val creditColor = when (creditTier) {
        CreditRatingTier.EXCELLENT -> SuccessGreen
        CreditRatingTier.GOOD -> LifeGreen
        CreditRatingTier.FAIR -> GoldAccent
        CreditRatingTier.POOR -> CoralNegative
    }
    val climateName = when (character.economicState.climate) {
        EconomicClimate.BOOM -> stringResource(R.string.climate_boom)
        EconomicClimate.STEADY -> stringResource(R.string.climate_steady)
        EconomicClimate.INFLATION_SURGE -> stringResource(R.string.climate_inflation)
        EconomicClimate.RECESSION -> stringResource(R.string.climate_recession)
    }
    val climateColor = when (character.economicState.climate) {
        EconomicClimate.BOOM -> SuccessGreen
        EconomicClimate.STEADY -> TealPrimary
        EconomicClimate.INFLATION_SURGE -> GoldAccent
        EconomicClimate.RECESSION -> CoralNegative
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaishaRadius.cardShape,
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.maisha.game.ui.theme.Hairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Row: Net Worth + Economic Climate Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.finance_net_worth).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = InkTertiary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = formatMoney(netWorth, character.countryCode),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = com.maisha.game.ui.theme.InkPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = climateColor.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, climateColor.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(climateColor)
                        )
                        Text(
                            text = climateName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = climateColor
                        )
                    }
                }
            }

            // Middle Row: Liquid Cash & Net Cash Flow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CreamBg.copy(alpha = 0.7f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.finance_liquid_cash),
                        style = MaterialTheme.typography.labelSmall,
                        color = InkTertiary
                    )
                    Text(
                        text = formatMoney(character.stats.money, character.countryCode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.maisha.game.ui.theme.InkPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.finance_net_cash_flow),
                        style = MaterialTheme.typography.labelSmall,
                        color = InkTertiary
                    )
                    val isPositive = cashFlow.netCashFlow >= 0
                    val flowColor = if (isPositive) LifeGreen else CoralNegative
                    val formattedFlow = if (isPositive) {
                        stringResource(R.string.finance_cash_flow_positive, formatMoney(cashFlow.netCashFlow, character.countryCode))
                    } else {
                        stringResource(R.string.finance_cash_flow_negative, formatMoney(-cashFlow.netCashFlow, character.countryCode))
                    }
                    Text(
                        text = formattedFlow,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = flowColor
                    )
                }
            }

            // Credit Score Row
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.credit_score_title),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = com.maisha.game.ui.theme.InkPrimary
                        )
                        Text(
                            text = "${character.finance.creditScore}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = creditColor
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = creditColor.copy(alpha = 0.12f)
                    ) {
                        val tierStr = when (creditTier) {
                            CreditRatingTier.EXCELLENT -> stringResource(R.string.credit_tier_excellent)
                            CreditRatingTier.GOOD -> stringResource(R.string.credit_tier_good)
                            CreditRatingTier.FAIR -> stringResource(R.string.credit_tier_fair)
                            CreditRatingTier.POOR -> stringResource(R.string.credit_tier_poor)
                        }
                        Text(
                            text = tierStr,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = creditColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                val creditProgress = ((character.finance.creditScore - 300) / 550f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.LightGray.copy(alpha = 0.4f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(creditProgress)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(creditColor)
                    )
                }
            }

            // Junior Trust Balance pill if character has balance or is minor
            if (character.finance.juniorTrustBalance > 0 || (character.age < 18 && character.stats.money > 0)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = character.age < 18, onClick = onOpenJuniorTrust),
                    shape = RoundedCornerShape(8.dp),
                    color = GoldAccent.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Savings,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.junior_trust_title),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = com.maisha.game.ui.theme.InkPrimary
                            )
                        }
                        Text(
                            text = formatMoney(character.finance.juniorTrustBalance, character.countryCode),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                    }
                }
            }

            // Action Pill Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FinanceQuickActionButton(
                    label = stringResource(R.string.finance_action_store),
                    icon = Icons.Rounded.AccountBalance,
                    color = GoldAccent,
                    onClick = onOpenStore
                )
                FinanceQuickActionButton(
                    label = CountryCatalog.financeProfileFor(character.countryCode).primaryInstrument.localName,
                    icon = Icons.Rounded.TrendingUp,
                    color = TealPrimary,
                    onClick = onOpenCountryFinance
                )
                if (character.isLivingAbroad() || character.countryCode != character.birthCountryCode) {
                    FinanceQuickActionButton(
                        label = stringResource(R.string.finance_action_remittance),
                        icon = Icons.Rounded.Public,
                        color = AccentPink,
                        onClick = onOpenRemittance
                    )
                }
                FinanceQuickActionButton(
                    label = stringResource(R.string.finance_action_ledger),
                    icon = Icons.Rounded.Description,
                    color = TealPrimary,
                    onClick = onOpenLedger
                )
                FinanceQuickActionButton(
                    label = stringResource(R.string.finance_action_banking),
                    icon = Icons.Rounded.Savings,
                    color = LifeGreen,
                    onClick = onOpenBanking
                )
                FinanceQuickActionButton(
                    label = stringResource(R.string.finance_action_debt),
                    icon = Icons.Rounded.Home,
                    color = AccentPink,
                    onClick = onOpenDebt
                )
                FinanceQuickActionButton(
                    label = stringResource(R.string.finance_action_invest),
                    icon = Icons.Rounded.Savings,
                    color = GoldAccent,
                    onClick = onOpenInvestments
                )
            }
        }
    }
}

@Composable
private fun FinanceQuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = com.maisha.game.ui.theme.InkPrimary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CashFlowLedgerSheet(
    character: Character,
    cashFlow: AnnualCashFlow,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = PaperWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.ledger_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = com.maisha.game.ui.theme.InkPrimary
            )
            Text(
                text = stringResource(R.string.ledger_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = InkTertiary
            )

            val isPositive = cashFlow.netCashFlow >= 0
            val bannerColor = if (isPositive) LifeGreen else CoralNegative
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = bannerColor.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, bannerColor.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.ledger_net_total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = bannerColor
                    )
                    Text(
                        text = if (isPositive) {
                            "+${formatMoney(cashFlow.netCashFlow, character.countryCode)} / yr"
                        } else {
                            "-${formatMoney(-cashFlow.netCashFlow, character.countryCode)} / yr"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = bannerColor
                    )
                }
            }

            InsetCardContainer(title = stringResource(R.string.ledger_section_income)) {
                LedgerItemRow(stringResource(R.string.ledger_salary), formatMoney(cashFlow.salaryIncome, character.countryCode), isIncome = true)
                if (cashFlow.partTimeIncome > 0) {
                    InsetDivider()
                    LedgerItemRow(stringResource(R.string.ledger_part_time), formatMoney(cashFlow.partTimeIncome, character.countryCode), isIncome = true)
                }
                if (cashFlow.rentalIncome > 0) {
                    InsetDivider()
                    LedgerItemRow(stringResource(R.string.ledger_rental), formatMoney(cashFlow.rentalIncome, character.countryCode), isIncome = true)
                }
                if (cashFlow.investmentDividends > 0) {
                    InsetDivider()
                    LedgerItemRow(stringResource(R.string.ledger_dividends), formatMoney(cashFlow.investmentDividends, character.countryCode), isIncome = true)
                }
                if (cashFlow.pensionIncome > 0) {
                    InsetDivider()
                    LedgerItemRow(stringResource(R.string.ledger_pension), formatMoney(cashFlow.pensionIncome, character.countryCode), isIncome = true)
                }
                if (cashFlow.businessProfits > 0) {
                    InsetDivider()
                    LedgerItemRow(stringResource(R.string.ledger_business), formatMoney(cashFlow.businessProfits, character.countryCode), isIncome = true)
                }
                if (cashFlow.socialMediaIncome > 0) {
                    InsetDivider()
                    LedgerItemRow(stringResource(R.string.ledger_social_media), formatMoney(cashFlow.socialMediaIncome, character.countryCode), isIncome = true)
                }
                InsetDivider()
                LedgerItemRow(
                    label = stringResource(R.string.ledger_gross_total),
                    amount = formatMoney(cashFlow.grossIncome, character.countryCode),
                    isIncome = true,
                    isTotal = true
                )
            }

            InsetCardContainer(title = stringResource(R.string.ledger_section_deductions)) {
                LedgerItemRow(stringResource(R.string.ledger_tax), formatMoney(cashFlow.incomeTax, character.countryCode), isIncome = false)
                InsetDivider()
                LedgerItemRow(stringResource(R.string.ledger_living), formatMoney(cashFlow.livingCost, character.countryCode), isIncome = false)
                if (cashFlow.mortgagePayments > 0) {
                    InsetDivider()
                    LedgerItemRow(stringResource(R.string.ledger_mortgage), formatMoney(cashFlow.mortgagePayments, character.countryCode), isIncome = false)
                }
                if (cashFlow.studentLoanPayment > 0) {
                    InsetDivider()
                    LedgerItemRow(stringResource(R.string.ledger_student_loan), formatMoney(cashFlow.studentLoanPayment, character.countryCode), isIncome = false)
                }
                if (cashFlow.assetUpkeep > 0) {
                    InsetDivider()
                    LedgerItemRow(stringResource(R.string.ledger_upkeep), formatMoney(cashFlow.assetUpkeep, character.countryCode), isIncome = false)
                }
                if (cashFlow.childSupport > 0) {
                    InsetDivider()
                    LedgerItemRow(stringResource(R.string.ledger_child_support), formatMoney(cashFlow.childSupport, character.countryCode), isIncome = false)
                }
                if (cashFlow.petUpkeep > 0) {
                    InsetDivider()
                    LedgerItemRow(stringResource(R.string.ledger_pet_upkeep), formatMoney(cashFlow.petUpkeep, character.countryCode), isIncome = false)
                }
                InsetDivider()
                LedgerItemRow(
                    label = stringResource(R.string.ledger_deductions_total),
                    amount = formatMoney(cashFlow.totalDeductions, character.countryCode),
                    isIncome = false,
                    isTotal = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LedgerItemRow(
    label: String,
    amount: String,
    isIncome: Boolean,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = com.maisha.game.ui.theme.InkPrimary
        )
        val color = if (isTotal) {
            if (isIncome) LifeGreen else CoralNegative
        } else {
            if (isIncome) LifeGreen else InkTertiary
        }
        val prefix = if (isIncome) "+" else "-"
        Text(
            text = "$prefix$amount",
            style = if (isTotal) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Medium,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebtLiabilitiesSheet(
    character: Character,
    onPayMortgage: (String) -> Unit,
    onPrepayMortgagePrincipal: (String, Int) -> Unit,
    onRepayStudentLoan: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val mortgagedHouses = character.assets.filter { it.mortgageBalance > 0 }
    val hasStudentLoan = character.education.studentLoanBalance > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = PaperWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.debt_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = com.maisha.game.ui.theme.InkPrimary
            )
            Text(
                text = stringResource(R.string.debt_sheet_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = InkTertiary
            )

            if (mortgagedHouses.isEmpty() && !hasStudentLoan) {
                EmptyStateCard(
                    illustration = EmptyStateIllustration.ASSETS,
                    title = "Debt Free",
                    message = stringResource(R.string.debt_no_liabilities)
                )
            }

            // Mortgaged properties
            mortgagedHouses.forEach { house ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaishaRadius.cardShape,
                    colors = CardDefaults.cardColors(containerColor = CreamBg.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.maisha.game.ui.theme.Hairline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = house.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = com.maisha.game.ui.theme.InkPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AccentPink.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = stringResource(R.string.debt_mortgage_rate, house.mortgageInterestRate * 100),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentPink,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.debt_mortgage_balance, formatMoney(house.mortgageBalance, character.countryCode)),
                                style = MaterialTheme.typography.bodySmall,
                                color = InkTertiary
                            )
                            Text(
                                text = stringResource(R.string.debt_mortgage_annual, formatMoney(house.annualMortgagePayment, character.countryCode)),
                                style = MaterialTheme.typography.bodySmall,
                                color = InkTertiary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val prepayChunk = minOf(house.mortgageBalance / 4, character.stats.money).coerceAtLeast(1_000)
                            if (character.stats.money >= 1_000 && prepayChunk <= house.mortgageBalance) {
                                OutlinedButton(
                                    onClick = { onPrepayMortgagePrincipal(house.id, prepayChunk) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Prepay ${formatMoney(prepayChunk, character.countryCode)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Button(
                                onClick = { onPayMortgage(house.id) },
                                enabled = character.stats.money >= house.mortgageBalance,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LifeGreen)
                            ) {
                                Text(
                                    text = stringResource(R.string.debt_payoff_button),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Student Loans
            if (hasStudentLoan) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaishaRadius.cardShape,
                    colors = CardDefaults.cardColors(containerColor = CreamBg.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.maisha.game.ui.theme.Hairline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.debt_student_loan_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = com.maisha.game.ui.theme.InkPrimary
                        )
                        Text(
                            text = stringResource(R.string.debt_student_loan_balance, formatMoney(character.education.studentLoanBalance, character.countryCode)),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkTertiary
                        )
                        val repayChunk = minOf(character.education.studentLoanBalance / 4, character.stats.money).coerceAtLeast(500)
                        if (character.stats.money >= 500) {
                            Button(
                                onClick = { onRepayStudentLoan(repayChunk) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                            ) {
                                Text(
                                    text = "Repay ${formatMoney(repayChunk, character.countryCode)}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
