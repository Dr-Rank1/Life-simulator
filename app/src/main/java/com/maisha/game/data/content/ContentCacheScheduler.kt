package com.maisha.game.data.content

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentCacheScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun enqueueStartupRefresh() {
        val work = OneTimeWorkRequestBuilder<ContentCacheWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            STARTUP_WORK,
            ExistingWorkPolicy.KEEP,
            work
        )
    }

    fun enqueuePeriodicRefresh() {
        val work = PeriodicWorkRequestBuilder<ContentCacheWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            work
        )
    }

    companion object {
        const val STARTUP_WORK = "procedural_content_startup"
        const val PERIODIC_WORK = "procedural_content_daily"
    }
}
