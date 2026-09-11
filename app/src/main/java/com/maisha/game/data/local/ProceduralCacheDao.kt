package com.maisha.game.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ProceduralCacheDao {

    @Query("SELECT * FROM procedural_cache WHERE kind = :kind")
    suspend fun getAll(kind: String): List<ProceduralCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ProceduralCacheEntity>)

    @Query("DELETE FROM procedural_cache WHERE kind = :kind")
    suspend fun deleteKind(kind: String)

    @Transaction
    suspend fun replaceKind(kind: String, entities: List<ProceduralCacheEntity>) {
        deleteKind(kind)
        if (entities.isNotEmpty()) upsertAll(entities)
    }
}
