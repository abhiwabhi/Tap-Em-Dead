package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameEngine
import com.example.game.GameEngineUiState

@Composable
fun GameHud(
    uiState: GameEngineUiState,
    gameEngine: GameEngine,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // TOP HUD BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Score & Combo Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Score Box
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1D3557), RoundedCornerShape(12.dp))
                        .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            text = "SCORE",
                            color = Color(0xFF8D99AE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.score}",
                            color = Color(0xFFFFD54F),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Combo Badge
                if (uiState.combo > 1) {
                    val comboScale = remember { Animatable(1.0f) }
                    LaunchedEffect(uiState.combo) {
                        comboScale.animateTo(1.4f, animationSpec = tween(100))
                        comboScale.animateTo(1.0f, animationSpec = tween(150))
                    }

                    Box(
                        modifier = Modifier
                            .scale(comboScale.value)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFF3D00), Color(0xFFFFD54F))
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "COMBO x${uiState.combo}🔥",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Center: Wave Badge & Coins
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Wave Badge
                Box(
                    modifier = Modifier
                        .background(Color(0xFF7209B7), RoundedCornerShape(12.dp))
                        .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "WAVE ${uiState.wave}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Coins Badge
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFB74D), RoundedCornerShape(12.dp))
                        .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🪙 ${uiState.coinsEarned}",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Right: Pause Button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFE63946))
                    .border(3.dp, Color.Black, CircleShape)
                    .clickable { onPauseClick() }
                    .padding(8.dp)
                    .testTag("pause_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // RELOADING OVERLAY NOTIFICATION
        if (uiState.isReloading) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color(0xCC000000), RoundedCornerShape(16.dp))
                    .border(3.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "RELOADING...",
                        color = Color(0xFFFFD54F),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { uiState.reloadProgress },
                        modifier = Modifier
                            .width(140.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Color(0xFFFFD54F),
                        trackColor = Color.DarkGray
                    )
                }
            }
        }

        // BOTTOM HUD BAR (Health, Ammo, Weapon & Abilities)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Left: Player Health & Special Ability Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Special Skill Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Nuke Button
                    Button(
                        onClick = { gameEngine.triggerNuke() },
                        enabled = uiState.nukesAvailable > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF3D00),
                            disabledContainerColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                            .testTag("nuke_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Nuke",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "NUKE (${uiState.nukesAvailable})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Freeze Button
                    Button(
                        onClick = { gameEngine.triggerTimeFreeze() },
                        enabled = uiState.freezeTimer <= 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E676),
                            disabledContainerColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                            .testTag("freeze_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AcUnit,
                            contentDescription = "Freeze",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.freezeTimer > 0) "FROZEN!" else "FREEZE",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Health Bar Container
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1D3557), RoundedCornerShape(12.dp))
                        .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "❤️ HP ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${uiState.playerHp.toInt()} / ${uiState.maxPlayerHp.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF8A80)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val hpRatio = (uiState.playerHp / uiState.maxPlayerHp).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { hpRatio },
                            modifier = Modifier
                                .width(160.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = if (hpRatio > 0.4f) Color(0xFF00E676) else Color(0xFFFF1744),
                            trackColor = Color(0xFF37474F)
                        )
                    }
                }
            }

            // Right: Active Weapon & Reload Button
            Box(
                modifier = Modifier
                    .background(Color(0xFF1D3557), RoundedCornerShape(16.dp))
                    .border(3.dp, Color.Black, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Weapon Emoji & Details
                    Column {
                        Text(
                            text = "${uiState.activeWeapon.iconEmoji} ${uiState.activeWeapon.name}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AMMO: ",
                                color = Color(0xFF8D99AE),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${uiState.currentAmmo} / ${uiState.maxAmmo}",
                                color = if (uiState.currentAmmo <= 2) Color(0xFFFF1744) else Color(0xFFFFD54F),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Manual Reload Button
                    IconButton(
                        onClick = { gameEngine.triggerReload() },
                        enabled = !uiState.isReloading && uiState.currentAmmo < uiState.maxAmmo,
                        modifier = Modifier
                            .background(Color(0xFFFFB74D), CircleShape)
                            .border(2.dp, Color.Black, CircleShape)
                            .testTag("manual_reload_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    }
}
