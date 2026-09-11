package com.maisha.game.data.content

import android.content.Context
import com.maisha.game.data.remote.JsonHttpGateway
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import javax.inject.Inject
import javax.inject.Singleton

/** Loads bundled JSON string arrays used when Room is empty and the network is down. */
@Singleton
class ContentAssetStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun loadStrings(assetPath: String): List<String> {
        return runCatching {
            val raw = context.assets.open(assetPath).bufferedReader().use { it.readText() }
            JsonHttpGateway.NETWORK_JSON.decodeFromString(ListSerializer(String.serializer()), raw)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }.getOrDefault(emptyList())
    }
}
