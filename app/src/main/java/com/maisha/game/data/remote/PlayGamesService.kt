package com.maisha.game.data.remote

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.SnapshotsClient
import com.google.android.gms.games.LeaderboardsClient
import com.google.android.gms.games.snapshot.SnapshotMetadataChange
import kotlinx.coroutines.tasks.await

/**
 * Service wrapper around Google Play Games Services v2 for cloud saves and leaderboards.
 *
 * Uses placeholder IDs from [LeaderboardIds]. Replace with real IDs from the Google Play Console.
 *
 * Call [initialize] once from [android.app.Application.onCreate] before using any other methods.
 */
class PlayGamesService {

    companion object {
        /** Must be called once from Application.onCreate(). */
        fun initialize(context: Context) {
            PlayGamesSdk.initialize(context)
        }
    }

    /** Obtains the snapshots client scoped to the given activity. */
    private fun snapshotsClient(activity: Activity): SnapshotsClient =
        PlayGames.getSnapshotsClient(activity)

    /** Obtains the leaderboard client scoped to the given activity. */
    private fun leaderboardsClient(activity: Activity): LeaderboardsClient =
        PlayGames.getLeaderboardsClient(activity)

    // ── Cloud Saves ──────────────────────────────────────────────────────

    /** Saves JSON data as a cloud snapshot. Returns true on success. */
    suspend fun saveSnapshot(activity: Activity, snapshotName: String, jsonData: String): Boolean {
        return try {
            val snapshotResult = snapshotsClient(activity)
                .open(snapshotName, /* createIfNotFound = */ true)
                .await()
            val snapshot = snapshotResult.data ?: return false
            snapshot.snapshotContents.writeBytes(jsonData.toByteArray(Charsets.UTF_8))
            val metadataChange = SnapshotMetadataChange.Builder()
                .setDescription("Maisha Life save")
                .build()
            snapshotsClient(activity)
                .commitAndClose(snapshot, metadataChange)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** Loads a snapshot by name and returns its JSON content, or null on error. */
    suspend fun loadSnapshot(activity: Activity, snapshotName: String): String? {
        return try {
            val snapshotResult = snapshotsClient(activity)
                .open(snapshotName, /* createIfNotFound = */ false)
                .await()
            val snapshot = snapshotResult.data ?: return null
            val bytes = snapshot.snapshotContents.readFully()
            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ── Leaderboards ─────────────────────────────────────────────────────

    /** Submits a score to the specified leaderboard. */
    fun submitScore(activity: Activity, leaderboardId: String, score: Long) {
        try {
            leaderboardsClient(activity).submitScore(leaderboardId, score)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Returns an intent that shows the Play Games leaderboard UI. */
    suspend fun getLeaderboardIntent(activity: Activity, leaderboardId: String): Intent =
        leaderboardsClient(activity).getLeaderboardIntent(leaderboardId).await()

    /** Returns an intent that shows all leaderboards. */
    suspend fun getAllLeaderboardsIntent(activity: Activity): Intent =
        leaderboardsClient(activity).allLeaderboardsIntent.await()
}
