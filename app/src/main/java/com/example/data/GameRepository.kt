package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val dao: GameDao) {
    val userStats: Flow<UserStatsEntity?> = dao.getUserStats()
    val weaponUnlocks: Flow<List<WeaponUnlockEntity>> = dao.getAllWeaponUnlocks()

    suspend fun saveGameResults(
        score: Int,
        kills: Int,
        headshots: Int,
        coinsEarned: Int
    ) {
        // Fetch current stats or create default
        var current = UserStatsEntity()
        // We do a direct update logic
        // Since userStats is a flow, we can also update safely
        // But for simplicity, we can query or pass updated entity
    }

    suspend fun updateStats(stats: UserStatsEntity) {
        dao.insertUserStats(stats)
    }

    suspend fun unlockWeapon(weaponId: String) {
        val existing = dao.getWeaponUnlock(weaponId)
        if (existing != null) {
            dao.updateWeaponUnlock(existing.copy(isUnlocked = true))
        } else {
            dao.insertWeaponUnlock(WeaponUnlockEntity(weaponId, isUnlocked = true))
        }
    }

    suspend fun upgradeWeapon(weaponId: String, type: String) {
        val existing = dao.getWeaponUnlock(weaponId) ?: return
        val updated = when (type) {
            "damage" -> existing.copy(damageLevel = existing.damageLevel + 1)
            "ammo" -> existing.copy(ammoLevel = existing.ammoLevel + 1)
            "reload" -> existing.copy(reloadLevel = existing.reloadLevel + 1)
            else -> existing
        }
        dao.updateWeaponUnlock(updated)
    }
}
