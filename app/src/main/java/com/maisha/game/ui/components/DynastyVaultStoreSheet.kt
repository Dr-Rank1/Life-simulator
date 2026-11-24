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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.LocalAtm
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.ProductDetails
import com.maisha.game.R
import com.maisha.game.billing.BillingProducts
import com.maisha.game.data.CountryCatalog
import com.maisha.game.data.EconomyScaler
import com.maisha.game.data.model.Character
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
fun DynastyVaultStoreSheet(
    character: Character,
    billingProducts: Map<String, ProductDetails>,
    isBillingConnected: Boolean,
    isBillingAvailable: Boolean,
    isAdFree: Boolean,
    isCreatorPass: Boolean,
    onPurchaseProduct: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val country = CountryCatalog.getCountry(character.countryCode)

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
                        .background(GoldAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalance,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.store_vault_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    Text(
                        text = stringResource(R.string.store_vault_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkTertiary
                    )
                }
            }

            // Offline / Play Store unavailable notice
            if (!isBillingConnected || !isBillingAvailable) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CreamBg,
                    border = BorderStroke(1.dp, Hairline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudOff,
                            contentDescription = null,
                            tint = InkTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.store_offline_notice),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkTertiary
                        )
                    }
                }
            }

            // Section 1: Permanent Dynasty Unlocks
            Text(
                text = stringResource(R.string.store_permanent_unlocks),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )

            // Ad-Free Card
            StoreItemCard(
                title = stringResource(R.string.store_ad_free_title),
                description = stringResource(R.string.store_ad_free_desc),
                icon = Icons.Rounded.Block,
                accentColor = TealPrimary,
                isOwned = isAdFree,
                formattedPrice = billingProducts[BillingProducts.PRODUCT_UNLOCK_AD_FREE]
                    ?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$2.99",
                onPurchase = { onPurchaseProduct(BillingProducts.PRODUCT_UNLOCK_AD_FREE) }
            )

            // Creator Pass Card
            StoreItemCard(
                title = stringResource(R.string.store_creator_pass_title),
                description = stringResource(R.string.store_creator_pass_desc),
                icon = Icons.Rounded.Star,
                accentColor = GoldAccent,
                isOwned = isCreatorPass,
                formattedPrice = billingProducts[BillingProducts.PRODUCT_UNLOCK_CREATOR_PASS]
                    ?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$4.99",
                onPurchase = { onPurchaseProduct(BillingProducts.PRODUCT_UNLOCK_CREATOR_PASS) }
            )

            // Section 2: Country-Scaled Cash Vouchers
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.store_cash_vouchers),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )
                Text(
                    text = "Values dynamically scaled for ${country.displayName} (${country.currencyCode})",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkTertiary
                )
            }

            // Starter Voucher
            val starterUnits = EconomyScaler.scaleAmount(
                BillingProducts.BASE_CASH_STARTER_KENYA,
                character.countryCode
            )
            StoreVoucherCard(
                title = stringResource(R.string.store_starter_title),
                creditAmount = formatMoney(starterUnits, character.countryCode),
                icon = Icons.Rounded.MonetizationOn,
                accentColor = LifeGreen,
                formattedPrice = billingProducts[BillingProducts.PRODUCT_CASH_STARTER]
                    ?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$0.99",
                onPurchase = { onPurchaseProduct(BillingProducts.PRODUCT_CASH_STARTER) }
            )

            // Fortune Voucher
            val fortuneUnits = EconomyScaler.scaleAmount(
                BillingProducts.BASE_CASH_FORTUNE_KENYA,
                character.countryCode
            )
            StoreVoucherCard(
                title = stringResource(R.string.store_fortune_title),
                creditAmount = formatMoney(fortuneUnits, character.countryCode),
                icon = Icons.Rounded.LocalAtm,
                accentColor = TealPrimary,
                formattedPrice = billingProducts[BillingProducts.PRODUCT_CASH_FORTUNE]
                    ?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$2.99",
                onPurchase = { onPurchaseProduct(BillingProducts.PRODUCT_CASH_FORTUNE) }
            )

            // Tycoon Voucher
            val tycoonUnits = EconomyScaler.scaleAmount(
                BillingProducts.BASE_CASH_TYCOON_KENYA,
                character.countryCode
            )
            StoreVoucherCard(
                title = stringResource(R.string.store_tycoon_title),
                creditAmount = formatMoney(tycoonUnits, character.countryCode),
                icon = Icons.Rounded.AccountBalance,
                accentColor = GoldAccent,
                formattedPrice = billingProducts[BillingProducts.PRODUCT_CASH_TYCOON]
                    ?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$9.99",
                onPurchase = { onPurchaseProduct(BillingProducts.PRODUCT_CASH_TYCOON) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StoreItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    isOwned: Boolean,
    formattedPrice: String,
    onPurchase: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, Hairline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkTertiary
                )
            }
            if (isOwned) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SuccessGreen.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.store_owned),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }
            } else {
                Button(
                    onClick = onPurchase,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text(
                        text = formattedPrice,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreVoucherCard(
    title: String,
    creditAmount: String,
    icon: ImageVector,
    accentColor: Color,
    formattedPrice: String,
    onPurchase: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PaperWhite),
        border = BorderStroke(1.dp, Hairline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )
                Text(
                    text = stringResource(R.string.store_credits_scaled, creditAmount),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
            }
            Button(
                onClick = onPurchase,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(
                    text = formattedPrice,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
