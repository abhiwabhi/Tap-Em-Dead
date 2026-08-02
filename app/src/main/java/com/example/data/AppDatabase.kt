package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [UserStatsEntity::class, WeaponUnlockEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tap_shooter_db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).gameDao()
                            dao.insertUserStats(UserStatsEntity())
                            val defaultWeapons = listOf(
                                WeaponUnlockEntity("pistol", isUnlocked = true, damageLevel = 1, ammoLevel = 1, reloadLevel = 1),
                                WeaponUnlockEntity("dual_pistols", isUnlocked = false),
                                WeaponUnlockEntity("shotgun", isUnlocked = false),
                                WeaponUnlockEntity("double_barrel", isUnlocked = false),
                                WeaponUnlockEntity("assault_rifle", isUnlocked = false),
                                WeaponUnlockEntity("sniper", isUnlocked = false),
                                WeaponUnlockEntity("banana_cannon", isUnlocked = false),
                                WeaponUnlockEntity("raygun", isUnlocked = false)
                            )
                            dao.insertWeaponUnlocks(defaultWeapons)
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
