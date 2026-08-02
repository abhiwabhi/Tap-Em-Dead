package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val highScore: Int = 0,
    val totalKills: Int = 0,
    val totalHeadshots: Int = 0,
    val coins: Int = 150, // Starting bonus
    val selectedWeaponId: String = "pistol",
    val selectedSkinId: String = "hero_default",
    val soundEnabled: Boolean = true,
    val nukeCount: Int = 2
)

@Entity(tableName = "weapon_unlocks")
data class WeaponUnlockEntity(
    @PrimaryKey val weaponId: String,
    val isUnlocked: Boolean = false,
    val damageLevel: Int = 1,
    val ammoLevel: Int = 1,
    val reloadLevel: Int = 1
)
