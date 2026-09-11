package com.maisha.game.ads

import com.maisha.game.BuildConfig

/**
 * AdMob unit IDs from [BuildConfig].
 * Debug uses Google sample units. Release reads `ADMOB_*` from gradle.properties
 * and leaves ads disabled until those real IDs are set.
 */
object AdUnitConfig {
    val INTERSTITIAL: String = BuildConfig.AD_INTERSTITIAL
    val REWARDED: String = BuildConfig.AD_REWARDED
    val BANNER: String = BuildConfig.AD_BANNER
    
    val usingTestAds: Boolean = BuildConfig.USE_TEST_ADS

    val isInterstitialEnabled: Boolean get() = INTERSTITIAL.isNotBlank()
    val isRewardedEnabled: Boolean get() = REWARDED.isNotBlank()
    val isBannerEnabled: Boolean get() = BANNER.isNotBlank()
    
    // Global fallback
    val areAdsGloballyEnabled: Boolean 
        get() = isInterstitialEnabled || isRewardedEnabled || isBannerEnabled
}