package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStatsEntity)

    @Update
    suspend fun updateUserStats(stats: UserStatsEntity)

    @Query("SELECT * FROM weapon_unlocks")
    fun getAllWeaponUnlocks(): Flow<List<WeaponUnlockEntity>>

    @Query("SELECT * FROM weapon_unlocks WHERE weaponId = :weaponId")
    suspend fun getWeaponUnlock(weaponId: String): WeaponUnlockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeaponUnlock(unlock: WeaponUnlockEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWeaponUnlocks(unlocks: List<WeaponUnlockEntity>)

    @Update
    suspend fun updateWeaponUnlock(unlock: WeaponUnlockEntity)
}
