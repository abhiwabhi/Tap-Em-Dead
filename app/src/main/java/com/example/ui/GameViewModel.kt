package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.data.UserStatsEntity
import com.example.data.WeaponUnlockEntity
import com.example.game.AudioSynth
import com.example.game.GameEngine
import com.example.game.GameEngineUiState
import com.example.game.Weapon
import com.example.game.WeaponRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenState {
    MAIN_MENU,
    GAME_PLAY,
    SHOP,
    STATS
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(AppDatabase.getDatabase(application).gameDao())
    val audioSynth = AudioSynth()
    val gameEngine = GameEngine(audioSynth)

    private val _currentScreen = MutableStateFlow(ScreenState.MAIN_MENU)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    val userStats: StateFlow<UserStatsEntity> = repository.userStats
        .combine(MutableStateFlow(Unit)) { stats, _ ->
            stats ?: UserStatsEntity()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserStatsEntity()
        )

    val weaponUnlocks: StateFlow<List<WeaponUnlockEntity>> = repository.weaponUnlocks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val gameEngineUiState: StateFlow<GameEngineUiState> = gameEngine.uiState

    init {
        viewModelScope.launch {
            userStats.collect { stats ->
                audioSynth.setEnabled(stats.soundEnabled)
            }
        }
    }

    fun startGame() {
        val stats = userStats.value
        val equipped = WeaponRegistry.getById(stats.selectedWeaponId)
        gameEngine.startNewGame(
            equippedWeapon = equipped,
            highScore = stats.highScore,
            initialNukes = stats.nukeCount
        )
        _isPaused.value = false
        _currentScreen.value = ScreenState.GAME_PLAY
    }

    fun navigateTo(screen: ScreenState) {
        _currentScreen.value = screen
    }

    fun togglePause() {
        _isPaused.value = !_isPaused.value
    }

    fun toggleSound() {
        val current = userStats.value
        val updated = current.copy(soundEnabled = !current.soundEnabled)
        viewModelScope.launch {
            repository.updateStats(updated)
            audioSynth.setEnabled(updated.soundEnabled)
        }
    }

    fun equipWeapon(weapon: Weapon) {
        val current = userStats.value
        val updated = current.copy(selectedWeaponId = weapon.id)
        viewModelScope.launch {
            repository.updateStats(updated)
        }
    }

    fun buyWeapon(weapon: Weapon) {
        val current = userStats.value
        if (current.coins >= weapon.cost) {
            val updatedStats = current.copy(
                coins = current.coins - weapon.cost,
                selectedWeaponId = weapon.id
            )
            viewModelScope.launch {
                repository.updateStats(updatedStats)
                repository.unlockWeapon(weapon.id)
            }
        }
    }

    fun handleGameOver() {
        val gameState = gameEngineUiState.value
        val currentStats = userStats.value

        val newHighScore = maxOf(currentStats.highScore, gameState.score)
        val newTotalKills = currentStats.totalKills + gameState.totalKills
        val newHeadshots = currentStats.totalHeadshots + gameState.headshots
        val newCoins = currentStats.coins + gameState.coinsEarned

        val updated = currentStats.copy(
            highScore = newHighScore,
            totalKills = newTotalKills,
            totalHeadshots = newHeadshots,
            coins = newCoins
        )

        viewModelScope.launch {
            repository.updateStats(updated)
        }
    }
}
