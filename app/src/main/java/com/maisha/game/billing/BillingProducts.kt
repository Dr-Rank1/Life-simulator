package com.maisha.game.billing

/**
 * Google Play Billing product identifiers and baseline currency amounts.
 * Consumable Dynasty Cash Vouchers are authored in Kenya reference units and
 * scaled by the active character's CountryCatalog economyFactor upon redemption.
 */
object BillingProducts {
    // Consumable Dynasty Cash Vouchers
    const val PRODUCT_CASH_STARTER = "dynasty_cash_starter"
    const val PRODUCT_CASH_FORTUNE = "dynasty_cash_fortune"
    const val PRODUCT_CASH_TYCOON = "dynasty_cash_tycoon"

    // Base Kenya currency reference units before CountryCatalog scaling
    const val BASE_CASH_STARTER_KENYA = 100_000
    const val BASE_CASH_FORTUNE_KENYA = 500_000
    const val BASE_CASH_TYCOON_KENYA = 2_500_000

    // Non-consumable permanent Dynasty unlocks
    const val PRODUCT_UNLOCK_AD_FREE = "unlock_ad_free"
    const val PRODUCT_UNLOCK_CREATOR_PASS = "unlock_creator_pass"

    val INAPP_CONSUMABLES: Set<String> = setOf(
        PRODUCT_CASH_STARTER,
        PRODUCT_CASH_FORTUNE,
        PRODUCT_CASH_TYCOON
    )

    val INAPP_PERMANENT_UNLOCKS: Set<String> = setOf(
        PRODUCT_UNLOCK_AD_FREE,
        PRODUCT_UNLOCK_CREATOR_PASS
    )

    val ALL_PRODUCTS: Set<String> = INAPP_CONSUMABLES + INAPP_PERMANENT_UNLOCKS

    fun getBaseCashForProduct(productId: String): Int = when (productId) {
        PRODUCT_CASH_STARTER -> BASE_CASH_STARTER_KENYA
        PRODUCT_CASH_FORTUNE -> BASE_CASH_FORTUNE_KENYA
        PRODUCT_CASH_TYCOON -> BASE_CASH_TYCOON_KENYA
        else -> 0
    }
}
