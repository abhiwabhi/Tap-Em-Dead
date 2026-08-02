package com.example.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.example.R
import com.example.game.EnemyType
import com.example.game.GameEngine
import com.example.game.GameEngineUiState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameCanvas(
    gameEngine: GameEngine,
    uiState: GameEngineUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Load background bitmap asset if available
    val bgImageBitmap = remember {
        try {
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img_game_background)
            bitmap?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }

    // 60 FPS Game Loop
    LaunchedEffect(uiState.isGameOver, uiState.isPaused) {
        while (!uiState.isGameOver && !uiState.isPaused) {
            gameEngine.updateGameFrame()
            kotlinx.coroutines.delay(16L) // ~60 fps update
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    gameEngine.handleTapToShoot(offset.x, offset.y)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        gameEngine.setCanvasSize(width, height)

        val centerX = width / 2f
        val centerY = height / 2f

        // Apply screen shake if active
        val shakeX = if (uiState.screenShake > 0) (Math.random().toFloat() * 2f - 1f) * uiState.screenShake else 0f
        val shakeY = if (uiState.screenShake > 0) (Math.random().toFloat() * 2f - 1f) * uiState.screenShake else 0f

        translate(left = shakeX, top = shakeY) {
            // 1. Draw Background Ground
            if (bgImageBitmap != null) {
                drawImage(
                    image = bgImageBitmap,
                    dstSize = androidx.compose.ui.unit.IntSize(width.toInt(), height.toInt())
                )
            } else {
                drawRect(color = Color(0xFFE2C391)) // Warm cartoon dusty dirt
                // Draw decorative grass patches
                drawCartoonGrass(this, width, height)
            }

            // 2. Draw Central Combat Zone Marker
            drawCircle(
                color = Color(0x33FF3D00),
                radius = 70f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color(0xFF1D3557),
                radius = 70f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 4f)
            )

            // 3. Draw Bullet Trails
            for (trail in gameEngine.bulletTrails) {
                drawLine(
                    color = trail.color.copy(alpha = trail.life.coerceIn(0f, 1f)),
                    start = Offset(trail.startX, trail.startY),
                    end = Offset(trail.endX, trail.endY),
                    strokeWidth = trail.strokeWidth
                )
            }

            // 4. Draw Particles
            for (p in gameEngine.particles) {
                drawCircle(
                    color = p.color.copy(alpha = p.life.coerceIn(0f, 1f)),
                    radius = p.size * p.life,
                    center = Offset(p.x, p.y)
                )
                // Outline on particles for cartoon crunch
                drawCircle(
                    color = Color.Black.copy(alpha = p.life.coerceIn(0f, 1f)),
                    radius = p.size * p.life,
                    center = Offset(p.x, p.y),
                    style = Stroke(width = 2f)
                )
            }

            // 5. Draw Enemies
            for (enemy in gameEngine.enemies) {
                drawCartoonEnemy(this, enemy)
            }

            // 6. Draw Player Character at exact center
            drawCartoonPlayer(this, centerX, centerY, gameEngine.playerState, uiState)

            // 7. Draw Floating Combo / Headshot Texts
            drawFloatingTexts(this, gameEngine)
        }
    }
}

private fun drawCartoonGrass(scope: DrawScope, width: Float, height: Float) {
    val grassColor = Color(0xFF66BB6A)
    val strokeColor = Color(0xFF1B5E20)
    val positions = listOf(
        Offset(100f, 150f), Offset(300f, 80f), Offset(800f, 120f),
        Offset(150f, 500f), Offset(700f, 480f), Offset(450f, 520f)
    )
    for (pos in positions) {
        scope.drawCircle(color = grassColor, radius = 25f, center = pos)
        scope.drawCircle(color = strokeColor, radius = 25f, center = pos, style = Stroke(width = 3f))
    }
}

private fun drawCartoonPlayer(
    scope: DrawScope,
    centerX: Float,
    centerY: Float,
    playerState: com.example.game.PlayerState,
    uiState: GameEngineUiState
) {
    scope.rotate(degrees = playerState.angle, pivot = Offset(centerX, centerY)) {
        val recoil = playerState.recoilDistance

        // Player Shadow
        scope.drawOval(
            color = Color(0x55000000),
            topLeft = Offset(centerX - 35f, centerY + 20f),
            size = Size(70f, 25f)
        )

        // Player Body / Legs
        scope.drawCircle(
            color = Color(0xFF1E88E5), // Bold Comic Blue
            radius = 32f,
            center = Offset(centerX - recoil, centerY)
        )
        scope.drawCircle(
            color = Color.Black,
            radius = 32f,
            center = Offset(centerX - recoil, centerY),
            style = Stroke(width = 5f)
        )

        // Goofy Cap / Helmet
        scope.drawArc(
            color = Color(0xFFFF3D00), // Red helmet
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(centerX - 30f - recoil, centerY - 32f),
            size = Size(60f, 60f)
        )
        scope.drawArc(
            color = Color.Black,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(centerX - 30f - recoil, centerY - 32f),
            size = Size(60f, 60f),
            style = Stroke(width = 5f)
        )

        // Goofy Eyes looking forward
        scope.drawCircle(color = Color.White, radius = 9f, center = Offset(centerX + 12f - recoil, centerY - 8f))
        scope.drawCircle(color = Color.Black, radius = 9f, center = Offset(centerX + 12f - recoil, centerY - 8f), style = Stroke(width = 3f))
        scope.drawCircle(color = Color.Black, radius = 4f, center = Offset(centerX + 14f - recoil, centerY - 8f))

        // Weapon Barrel extending towards facing direction
        val gunLength = 45f
        val weapon = uiState.activeWeapon
        val gunColor = when (weapon.id) {
            "banana_cannon" -> Color(0xFFFFEB3B)
            "raygun" -> Color(0xFF00E676)
            "sniper" -> Color(0xFF78909C)
            else -> Color(0xFF424242)
        }

        // Draw Gun
        scope.drawRoundRect(
            color = gunColor,
            topLeft = Offset(centerX + 15f - recoil, centerY - 8f),
            size = Size(gunLength, 16f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        scope.drawRoundRect(
            color = Color.Black,
            topLeft = Offset(centerX + 15f - recoil, centerY - 8f),
            size = Size(gunLength, 16f),
            cornerRadius = CornerRadius(4f, 4f),
            style = Stroke(width = 4f)
        )

        // Muzzle Flash
        if (playerState.muzzleFlashTimer > 0) {
            val flashX = centerX + 15f + gunLength - recoil
            val flashY = centerY
            scope.drawCircle(color = Color(0xFFFFEB3B), radius = 22f, center = Offset(flashX, flashY))
            scope.drawCircle(color = Color(0xFFFF3D00), radius = 14f, center = Offset(flashX, flashY))
        }
    }
}

private fun drawCartoonEnemy(scope: DrawScope, enemy: com.example.game.EnemyInstance) {
    val x = enemy.x
    val y = enemy.y
    val type = enemy.type

    // Color animation on hit flash
    val bodyColor = if (enemy.isHit) Color.White else type.skinColor
    val headColor = if (enemy.isHit) Color.White else type.headColor

    // Enemy Shadow
    scope.drawOval(
        color = Color(0x44000000),
        topLeft = Offset(x - type.bodyRadius, y + type.bodyRadius * 0.5f),
        size = Size(type.bodyRadius * 2f, type.bodyRadius * 0.8f)
    )

    // Body
    scope.drawCircle(color = bodyColor, radius = type.bodyRadius, center = Offset(x, y))
    scope.drawCircle(color = Color.Black, radius = type.bodyRadius, center = Offset(x, y), style = Stroke(width = 5f))

    // Head Target Zone (Distinctly visible head!)
    val headX = enemy.headWorldX
    val headY = enemy.headWorldY
    val headRadius = type.headRadius

    // Draw Head Circle
    scope.drawCircle(color = headColor, radius = headRadius, center = Offset(headX, headY))
    scope.drawCircle(color = Color.Black, radius = headRadius, center = Offset(headX, headY), style = Stroke(width = 5f))

    // HEADSHOT TARGET ACCENT (Subtle dashed ring highlighting head target for precision tapping!)
    scope.drawCircle(
        color = Color(0xFFFFD54F).copy(alpha = 0.6f),
        radius = headRadius + 6f,
        center = Offset(headX, headY),
        style = Stroke(width = 2.5f)
    )

    // Goofy Eyes on Head
    val eyeOffsetX = 6f
    val eyeOffsetY = 4f
    scope.drawCircle(color = Color.White, radius = 6f, center = Offset(headX - eyeOffsetX, headY - eyeOffsetY))
    scope.drawCircle(color = Color.Black, radius = 6f, center = Offset(headX - eyeOffsetX, headY - eyeOffsetY), style = Stroke(width = 2f))
    scope.drawCircle(color = Color.Black, radius = 3f, center = Offset(headX - eyeOffsetX, headY - eyeOffsetY))

    scope.drawCircle(color = Color.White, radius = 6f, center = Offset(headX + eyeOffsetX, headY - eyeOffsetY))
    scope.drawCircle(color = Color.Black, radius = 6f, center = Offset(headX + eyeOffsetX, headY - eyeOffsetY), style = Stroke(width = 2f))
    scope.drawCircle(color = Color.Black, radius = 3f, center = Offset(headX + eyeOffsetX, headY - eyeOffsetY))

    // Goofy Expression / Teeth / Horns based on type
    if (type == EnemyType.BRUTE) {
        // Draw Brute Spiked Horns
        val path = Path().apply {
            moveTo(headX - 18f, headY - 10f)
            lineTo(headX - 30f, headY - 30f)
            lineTo(headX - 8f, headY - 20f)
            close()
        }
        scope.drawPath(path, color = Color(0xFFFFD54F))
        scope.drawPath(path, color = Color.Black, style = Stroke(width = 4f))

        val path2 = Path().apply {
            moveTo(headX + 18f, headY - 10f)
            lineTo(headX + 30f, headY - 30f)
            lineTo(headX + 8f, headY - 20f)
            close()
        }
        scope.drawPath(path2, color = Color(0xFFFFD54F))
        scope.drawPath(path2, color = Color.Black, style = Stroke(width = 4f))
    }

    // Health Bar above enemy head if injured
    if (enemy.hp < enemy.maxHp) {
        val barWidth = type.bodyRadius * 2f
        val barHeight = 8f
        val barX = x - type.bodyRadius
        val barY = headY - headRadius - 18f

        val hpRatio = (enemy.hp / enemy.maxHp).coerceIn(0f, 1f)

        // Bar background
        scope.drawRoundRect(
            color = Color.Black,
            topLeft = Offset(barX, barY),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(3f, 3f)
        )
        // Bar fill
        val barColor = if (hpRatio > 0.5f) Color(0xFF00E676) else Color(0xFFFF1744)
        scope.drawRoundRect(
            color = barColor,
            topLeft = Offset(barX, barY),
            size = Size(barWidth * hpRatio, barHeight),
            cornerRadius = CornerRadius(3f, 3f)
        )
    }
}

private fun drawFloatingTexts(scope: DrawScope, gameEngine: GameEngine) {
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        isFakeBoldText = true
    }

    for (ft in gameEngine.floatingTexts) {
        paint.textSize = if (ft.isHeadshot) 38f * ft.scale else 26f * ft.scale
        val colorInt = android.graphics.Color.argb(
            (ft.alpha * 255).toInt().coerceIn(0, 255),
            (ft.color.red * 255).toInt(),
            (ft.color.green * 255).toInt(),
            (ft.color.blue * 255).toInt()
        )
        paint.color = colorInt

        // Black outline for comic pop
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = if (ft.isHeadshot) 8f else 5f
        paint.color = android.graphics.Color.argb((ft.alpha * 255).toInt().coerceIn(0, 255), 0, 0, 0)
        scope.drawContext.canvas.nativeCanvas.drawText(ft.text, ft.x, ft.y, paint)

        // Text fill
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = colorInt
        scope.drawContext.canvas.nativeCanvas.drawText(ft.text, ft.x, ft.y, paint)
    }
}
