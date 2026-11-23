// app/src/main/java/com/maisha/game/MaishaApplication.kt
package com.maisha.game

import android.app.Application
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.MobileAds
import com.maisha.game.data.events.EventRepository
import com.maisha.game.data.content.ContentCacheScheduler
import com.maisha.game.data.content.ContentPrefetcher
import com.maisha.game.data.local.SettingsRepository
import com.maisha.game.feedback.FeedbackManager
import com.maisha.game.notifications.NotificationHelper
import com.maisha.game.notifications.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class MaishaApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var feedbackManager: FeedbackManager

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var eventRepository: EventRepository

    @Inject
    lateinit var contentPrefetcher: ContentPrefetcher

    @Inject
    lateinit var contentCacheScheduler: ContentCacheScheduler

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)

        // AdMob SDK init is deferred off the main thread to avoid cold-start hitching.
        applicationScope.launch(Dispatchers.IO) {
            runCatching { MobileAds.initialize(this@MaishaApplication) }
        }

        applicationScope.launch(Dispatchers.Default) {
            runCatching { eventRepository.ensureLoaded() }
        }

        applicationScope.launch(Dispatchers.IO) {
            runCatching { contentPrefetcher.hydrateFromDisk() }
        }
        runCatching {
            com.google.android.gms.games.PlayGamesSdk.initialize(this@MaishaApplication)
        }
        runCatching {
            contentCacheScheduler.enqueueStartupRefresh()
            contentCacheScheduler.enqueuePeriodicRefresh()
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                feedbackManager.release()
            }
        })

        applicationScope.launch {
            if (settingsRepository.isNotificationsEnabledNow() &&
                NotificationManagerCompat.from(this@MaishaApplication).areNotificationsEnabled()
            ) {
                notificationScheduler.scheduleDailyReminder()
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onTerminate() {
        feedbackManager.release()
        super.onTerminate()
    }
}
