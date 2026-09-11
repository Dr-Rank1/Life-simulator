package com.maisha.game.data.local

import androidx.room.Entity

@Entity(tableName = "procedural_cache", primaryKeys = ["kind", "id"])
data class ProceduralCacheEntity(
    val kind: String,
    val id: String,
    val payloadJson: String,
    val fetchedAt: Long
)
