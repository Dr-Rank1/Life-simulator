package com.maisha.game.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.maisha.game.data.local.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed class BillingPurchaseEvent {
    data class CashVoucherPurchased(
        val productId: String,
        val baseCashAmountKenya: Int
    ) : BillingPurchaseEvent()

    data class PermanentUnlockPurchased(
        val productId: String
    ) : BillingPurchaseEvent()

    data class PurchaseFailed(
        val productId: String?,
        val message: String
    ) : BillingPurchaseEvent()
}

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isServiceAvailable = MutableStateFlow(true)
    val isServiceAvailable: StateFlow<Boolean> = _isServiceAvailable.asStateFlow()

    private val _products = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val products: StateFlow<Map<String, ProductDetails>> = _products.asStateFlow()

    val isAdFree: StateFlow<Boolean> = MutableStateFlow(false).apply {
        scope.launch {
            settingsRepository.isAdFree.collect { value = it }
        }
    }

    val isCreatorPass: StateFlow<Boolean> = MutableStateFlow(false).apply {
        scope.launch {
            settingsRepository.isCreatorPass.collect { value = it }
        }
    }

    private val _purchaseEvents = MutableSharedFlow<BillingPurchaseEvent>(extraBufferCapacity = 8)
    val purchaseEvents: SharedFlow<BillingPurchaseEvent> = _purchaseEvents.asSharedFlow()

    private val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()
    }

    private var reconnectAttempts = 0

    init {
        startBillingConnection()
    }

    fun startBillingConnection() {
        if (billingClient.isReady) return

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Billing service connected successfully.")
                    _isConnected.value = true
                    _isServiceAvailable.value = true
                    reconnectAttempts = 0
                    queryAllProducts()
                    refreshPurchases()
                } else {
                    Log.w(TAG, "Billing setup finished with code: ${billingResult.responseCode} - ${billingResult.debugMessage}")
                    _isConnected.value = false
                    _isServiceAvailable.value = (billingResult.responseCode != BillingClient.BillingResponseCode.BILLING_UNAVAILABLE)
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected. Attempting auto reconnection...")
                _isConnected.value = false
                retryConnectionWithBackoff()
            }
        })
    }

    private fun retryConnectionWithBackoff() {
        scope.launch {
            val backoffMs = (1000L * (1 shl reconnectAttempts.coerceAtMost(5))).coerceAtMost(30000L)
            reconnectAttempts++
            delay(backoffMs)
            startBillingConnection()
        }
    }

    fun queryAllProducts() {
        if (!billingClient.isReady) return

        scope.launch {
            val productList = BillingProducts.ALL_PRODUCTS.map { id ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(id)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            }

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            try {
                val result = billingClient.queryProductDetails(params)
                if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val detailsList = result.productDetailsList ?: emptyList()
                    val detailsMap = detailsList.associateBy { it.productId }
                    _products.value = detailsMap
                    Log.i(TAG, "Loaded ${detailsMap.size} Play Billing product details.")
                } else {
                    Log.w(TAG, "queryProductDetails returned code: ${result.billingResult.responseCode}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error querying product details: ${e.message}", e)
            }
        }
    }

    /**
     * Checks and recovers pending purchases (called on app resume).
     */
    fun refreshPurchases() {
        if (!billingClient.isReady) return

        scope.launch {
            try {
                val params = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()

                val result = billingClient.queryPurchasesAsync(params)
                if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    processPurchases(result.purchasesList)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing purchases: ${e.message}", e)
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) {
                    scope.launch { processPurchases(purchases) }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "Purchase cancelled by user.")
            }
            else -> {
                Log.w(TAG, "Purchase failed: ${billingResult.responseCode} - ${billingResult.debugMessage}")
                _purchaseEvents.tryEmit(
                    BillingPurchaseEvent.PurchaseFailed(
                        productId = null,
                        message = billingResult.debugMessage
                    )
                )
            }
        }
    }

    private suspend fun processPurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                for (productId in purchase.products) {
                    when {
                        BillingProducts.INAPP_CONSUMABLES.contains(productId) -> {
                            handleConsumablePurchase(purchase, productId)
                        }
                        BillingProducts.INAPP_PERMANENT_UNLOCKS.contains(productId) -> {
                            handlePermanentUnlock(purchase, productId)
                        }
                    }
                }
            } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                Log.i(TAG, "Purchase pending user payment completion: ${purchase.products}")
            }
        }
    }

    private suspend fun handleConsumablePurchase(purchase: Purchase, productId: String) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        try {
            val consumeResult = billingClient.consumePurchase(consumeParams)
            if (consumeResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val baseCash = BillingProducts.getBaseCashForProduct(productId)
                _purchaseEvents.emit(BillingPurchaseEvent.CashVoucherPurchased(productId, baseCash))
                Log.i(TAG, "Consumed cash voucher $productId ($baseCash Kenya base units).")
            } else {
                Log.w(TAG, "Failed to consume $productId: ${consumeResult.billingResult.debugMessage}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while consuming purchase $productId", e)
        }
    }

    private suspend fun handlePermanentUnlock(purchase: Purchase, productId: String) {
        if (!purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            try {
                val ackResult = billingClient.acknowledgePurchase(ackParams)
                if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    applyPermanentUnlock(productId)
                    _purchaseEvents.emit(BillingPurchaseEvent.PermanentUnlockPurchased(productId))
                    Log.i(TAG, "Acknowledged permanent unlock: $productId")
                } else {
                    Log.w(TAG, "Failed acknowledging $productId: ${ackResult.debugMessage}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while acknowledging $productId", e)
            }
        } else {
            // Already acknowledged: ensure preference is active
            applyPermanentUnlock(productId)
        }
    }

    private suspend fun applyPermanentUnlock(productId: String) {
        when (productId) {
            BillingProducts.PRODUCT_UNLOCK_AD_FREE -> {
                settingsRepository.setAdFree(true)
            }
            BillingProducts.PRODUCT_UNLOCK_CREATOR_PASS -> {
                settingsRepository.setCreatorPass(true)
            }
        }
    }

    /**
     * Launches the Google Play purchase bottom sheet for [productId].
     */
    fun launchPurchaseFlow(activity: Activity, productId: String): Boolean {
        if (!billingClient.isReady) {
            startBillingConnection()
            return false
        }

        val productDetails = _products.value[productId]
        if (productDetails == null) {
            Log.w(TAG, "ProductDetails not found for productId: $productId")
            queryAllProducts()
            return false
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val result = billingClient.launchBillingFlow(activity, flowParams)
        return result.responseCode == BillingClient.BillingResponseCode.OK
    }

    companion object {
        private const val TAG = "BillingManager"
    }
}
