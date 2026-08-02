package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.GameCanvas
import com.example.ui.GameHud
import com.example.ui.GameOverDialog
import com.example.ui.GameViewModel
import com.example.ui.MainMenuScreen
import com.example.ui.PauseDialog
import com.example.ui.ScreenState
import com.example.ui.ShopScreen
import com.example.ui.StatsDialog
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                TapShooterApp()
            }
        }
    }
}

@Composable
fun TapShooterApp(viewModel: GameViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userStats by viewModel.userStats.collectAsStateWithLifecycle()
    val weaponUnlocks by viewModel.weaponUnlocks.collectAsStateWithLifecycle()
    val gameState by viewModel.gameEngineUiState.collectAsStateWithLifecycle()
    val isPaused by viewModel.isPaused.collectAsStateWithLifecycle()

    // Handle Game Over sync with Room Database
    LaunchedEffect(gameState.isGameOver) {
        if (gameState.isGameOver) {
            viewModel.handleGameOver()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            ScreenState.MAIN_MENU -> {
                MainMenuScreen(
                    userStats = userStats,
                    onStartGame = { viewModel.startGame() },
                    onOpenShop = { viewModel.navigateTo(ScreenState.SHOP) },
                    onOpenStats = { viewModel.navigateTo(ScreenState.STATS) },
                    onToggleSound = { viewModel.toggleSound() }
                )
            }

            ScreenState.STATS -> {
                MainMenuScreen(
                    userStats = userStats,
                    onStartGame = { viewModel.startGame() },
                    onOpenShop = { viewModel.navigateTo(ScreenState.SHOP) },
                    onOpenStats = { viewModel.navigateTo(ScreenState.STATS) },
                    onToggleSound = { viewModel.toggleSound() }
                )
                StatsDialog(
                    userStats = userStats,
                    onDismiss = { viewModel.navigateTo(ScreenState.MAIN_MENU) }
                )
            }

            ScreenState.SHOP -> {
                ShopScreen(
                    userStats = userStats,
                    weaponUnlocks = weaponUnlocks,
                    onEquipWeapon = { weapon -> viewModel.equipWeapon(weapon) },
                    onBuyWeapon = { weapon -> viewModel.buyWeapon(weapon) },
                    onBackClick = { viewModel.navigateTo(ScreenState.MAIN_MENU) }
                )
            }

            ScreenState.GAME_PLAY -> {
                GameCanvas(
                    gameEngine = viewModel.gameEngine,
                    uiState = gameState
                )

                GameHud(
                    uiState = gameState,
                    gameEngine = viewModel.gameEngine,
                    onPauseClick = { viewModel.togglePause() }
                )

                // Pause Dialog Overlay
                if (isPaused && !gameState.isGameOver) {
                    PauseDialog(
                        onResume = { viewModel.togglePause() },
                        onRestart = { viewModel.startGame() },
                        onMainMenu = { viewModel.navigateTo(ScreenState.MAIN_MENU) }
                    )
                }

                // Game Over Dialog Overlay
                if (gameState.isGameOver) {
                    GameOverDialog(
                        uiState = gameState,
                        onPlayAgain = { viewModel.startGame() },
                        onOpenShop = { viewModel.navigateTo(ScreenState.SHOP) },
                        onMainMenu = { viewModel.navigateTo(ScreenState.MAIN_MENU) }
                    )
                }
            }
        }
    }
}
