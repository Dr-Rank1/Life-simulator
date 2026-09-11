package com.maisha.game.data.local

interface GameDatabaseAccess {
    val isAvailable: Boolean
    val characterDao: CharacterDao?
    val achievementDao: AchievementDao?
    val proceduralCacheDao: ProceduralCacheDao?
}
