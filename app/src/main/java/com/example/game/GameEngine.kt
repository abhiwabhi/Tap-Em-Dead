package com.example.game

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

data class GameEngineUiState(
    val score: Int = 0,
    val highScore: Int = 0,
    val combo: Int = 1,
    val comboTimer: Float = 0f,
    val wave: Int = 1,
    val waveEnemiesKilled: Int = 0,
    val totalKills: Int = 0,
    val headshots: Int = 0,
    val coinsEarned: Int = 0,
    val currentAmmo: Int = 10,
    val maxAmmo: Int = 10,
    val isReloading: Boolean = false,
    val reloadProgress: Float = 0f,
    val playerHp: Float = 100f,
    val maxPlayerHp: Float = 100f,
    val activeWeapon: Weapon = WeaponRegistry.PISTOL,
    val nukesAvailable: Int = 2,
    val freezeTimer: Float = 0f,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val screenShake: Float = 0f
)

class GameEngine(
    private val audioSynth: AudioSynth
) {
    private val _uiState = MutableStateFlow(GameEngineUiState())
    val uiState: StateFlow<GameEngineUiState> = _uiState.asStateFlow()

    // Game Objects
    val enemies = mutableListOf<EnemyInstance>()
    val bulletTrails = mutableListOf<BulletTrail>()
    val particles = mutableListOf<Particle>()
    val floatingTexts = mutableListOf<FloatingText>()
    val playerState = PlayerState()

    private var canvasWidth = 1000f
    private var canvasHeight = 600f
    private var nextEnemyId = 1L
    private var nextTextId = 1L
    private var lastFireTime = 0L
    private var spawnTimer = 0f
    private var targetSpawnInterval = 60f // frames

    fun setCanvasSize(width: Float, height: Float) {
        if (width > 0 && height > 0) {
            canvasWidth = width
            canvasHeight = height
            playerState.x = width / 2f
            playerState.y = height / 2f
        }
    }

    fun startNewGame(equippedWeapon: Weapon, highScore: Int, initialNukes: Int = 2) {
        enemies.clear()
        bulletTrails.clear()
        particles.clear()
        floatingTexts.clear()

        playerState.hp = 100f
        playerState.maxHp = 100f
        playerState.angle = -90f // Aiming up by default
        playerState.recoilDistance = 0f
        playerState.muzzleFlashTimer = 0

        _uiState.value = GameEngineUiState(
            score = 0,
            highScore = highScore,
            combo = 1,
            comboTimer = 0f,
            wave = 1,
            waveEnemiesKilled = 0,
            totalKills = 0,
            headshots = 0,
            coinsEarned = 0,
            activeWeapon = equippedWeapon,
            currentAmmo = equippedWeapon.magazineSize,
            maxAmmo = equippedWeapon.magazineSize,
            isReloading = false,
            playerHp = 100f,
            maxPlayerHp = 100f,
            nukesAvailable = initialNukes,
            isGameOver = false,
            isPaused = false
        )
    }

    fun updateGameFrame() {
        val state = _uiState.value
        if (state.isGameOver || state.isPaused) return

        val centerX = canvasWidth / 2f
        val centerY = canvasHeight / 2f

        // 1. Combo Timer Decay
        var currentCombo = state.combo
        var currentComboTimer = state.comboTimer
        if (currentComboTimer > 0) {
            currentComboTimer -= 0.016f
            if (currentComboTimer <= 0) {
                currentCombo = 1
                currentComboTimer = 0f
            }
        }

        // 2. Freeze Timer Decay
        var currentFreeze = state.freezeTimer
        if (currentFreeze > 0) {
            currentFreeze -= 0.016f
            if (currentFreeze < 0) currentFreeze = 0f
        }

        // 3. Reload Progress
        var isReloading = state.isReloading
        var currentAmmo = state.currentAmmo
        var reloadProgress = state.reloadProgress
        if (isReloading) {
            reloadProgress += 0.016f * 1000f / state.activeWeapon.reloadTimeMs
            if (reloadProgress >= 1.0f) {
                isReloading = false
                reloadProgress = 0f
                currentAmmo = state.activeWeapon.magazineSize
            }
        }

        // 4. Player Recoil & Flash Decay
        if (playerState.recoilDistance > 0) {
            playerState.recoilDistance = (playerState.recoilDistance - 1.5f).coerceAtLeast(0f)
        }
        if (playerState.muzzleFlashTimer > 0) {
            playerState.muzzleFlashTimer--
        }

        // 5. Enemy Spawning Loop
        spawnTimer += 1f
        val maxEnemies = 8 + (state.wave * 2).coerceAtMost(16)
        if (spawnTimer >= targetSpawnInterval && enemies.size < maxEnemies) {
            spawnTimer = 0f
            spawnEnemy(state.wave)
        }

        // 6. Update Enemies
        val speedMultiplier = if (currentFreeze > 0) 0.25f else 1.0f
        val iterator = enemies.iterator()
        var playerHp = state.playerHp
        var screenShake = (state.screenShake - 0.5f).coerceAtLeast(0f)

        while (iterator.hasNext()) {
            val enemy = iterator.next()

            // Flash effect decay
            if (enemy.hitFlashTimer > 0) {
                enemy.hitFlashTimer--
                if (enemy.hitFlashTimer == 0) enemy.isHit = false
            }

            // Move enemy toward player center
            val dx = centerX - enemy.x
            val dy = centerY - enemy.y
            val dist = hypot(dx, dy)
            val moveAngle = atan2(dy, dx)
            enemy.angle = Math.toDegrees(moveAngle.toDouble()).toFloat()

            val step = enemy.speed * speedMultiplier
            if (dist > enemy.bodyHitboxRadius + 20f) {
                enemy.x += cos(moveAngle) * step
                enemy.y += sin(moveAngle) * step
                enemy.walkPhase += 0.15f
            } else {
                // Enemy reached player! Hurt player
                playerHp -= 12f
                screenShake = 12f
                audioSynth.playHurt()

                // Spawn blood particles on player
                spawnExplosionParticles(enemy.x, enemy.y, Color(0xFFE57373), count = 12)

                // Push enemy back or destroy
                iterator.remove()

                if (playerHp <= 0) {
                    playerHp = 0f
                    _uiState.value = _uiState.value.copy(
                        isGameOver = true,
                        playerHp = 0f
                    )
                    return
                }
            }

            // Calculate world coordinates for Head
            // Head is positioned towards enemy facing direction or top offset
            val rad = Math.toRadians(enemy.angle.toDouble())
            enemy.headWorldX = enemy.x + cos(rad).toFloat() * enemy.type.headOffsetY * 0.7f
            enemy.headWorldY = enemy.y + sin(rad).toFloat() * enemy.type.headOffsetY * 0.7f
        }

        // 7. Update Particles
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx
            p.y += p.vy
            p.life -= p.decay
            if (p.life <= 0) pIter.remove()
        }

        // 8. Update Floating Texts
        val tIter = floatingTexts.iterator()
        while (tIter.hasNext()) {
            val t = tIter.next()
            t.y += t.dy
            t.alpha -= 0.02f
            if (t.alpha <= 0) tIter.remove()
        }

        // 9. Update Bullet Trails
        val bIter = bulletTrails.iterator()
        while (bIter.hasNext()) {
            val b = bIter.next()
            b.life -= 0.15f
            if (b.life <= 0) bIter.remove()
        }

        // 10. Update State
        _uiState.value = _uiState.value.copy(
            combo = currentCombo,
            comboTimer = currentComboTimer,
            freezeTimer = currentFreeze,
            isReloading = isReloading,
            reloadProgress = reloadProgress,
            currentAmmo = currentAmmo,
            playerHp = playerHp,
            screenShake = screenShake
        )
    }

    fun handleTapToShoot(tapX: Float, tapY: Float) {
        val state = _uiState.value
        if (state.isGameOver || state.isPaused) return

        val weapon = state.activeWeapon
        val now = System.currentTimeMillis()

        // Check fire rate
        if (now - lastFireTime < weapon.fireRateMs) return
        lastFireTime = now

        // Check if reloading
        if (state.isReloading) {
            addFloatingText("RELOADING!", tapX, tapY, Color(0xFFFFB74D))
            audioSynth.playReload()
            return
        }

        // Check out of ammo
        if (state.currentAmmo <= 0) {
            triggerReload()
            return
        }

        // Aim player towards tap point
        val centerX = canvasWidth / 2f
        val centerY = canvasHeight / 2f
        val aimRad = atan2(tapY - centerY, tapX - centerX)
        val aimDeg = Math.toDegrees(aimRad.toDouble()).toFloat()
        playerState.angle = aimDeg
        playerState.recoilDistance = 14f
        playerState.muzzleFlashTimer = 3

        // Decrement Ammo
        val newAmmo = state.currentAmmo - 1
        var isReloadingNow = false
        if (newAmmo <= 0) {
            isReloadingNow = true
            audioSynth.playReload()
        }

        // Play Weapon Sound
        when (weapon.soundFx) {
            "shotgun" -> audioSynth.playShotgunBlast()
            "dual" -> audioSynth.playDualShot()
            "rifle" -> audioSynth.playPistolShot()
            "sniper" -> audioSynth.playSniperShot()
            "laser" -> audioSynth.playLaserShot()
            "banana" -> audioSynth.playBananaShot()
            else -> audioSynth.playPistolShot()
        }

        // Process Shot(s)
        var hitTarget = false
        var headshotOccurred = false
        var scoreAdd = 0
        var coinsAdd = 0
        var killsAdd = 0
        var headshotsAdd = 0

        val pellets = weapon.pelletCount
        for (i in 0 until pellets) {
            val spreadOffset = if (pellets > 1 || weapon.spreadAngleDeg > 0) {
                Random.nextFloat() * weapon.spreadAngleDeg - (weapon.spreadAngleDeg / 2f)
            } else 0f

            val finalAngleRad = aimRad + Math.toRadians(spreadOffset.toDouble())
            val rayEndX = centerX + cos(finalAngleRad).toFloat() * 1200f
            val rayEndY = centerY + sin(finalAngleRad).toFloat() * 1200f

            // Check hit against enemies
            // Prioritize closest enemy to tap or ray line
            val bestHit = findTargetEnemy(tapX, tapY, finalAngleRad, centerX, centerY)

            if (bestHit != null) {
                hitTarget = true
                val enemy = bestHit.enemy
                val isHeadshot = bestHit.isHeadshot

                val baseDamage = weapon.damage
                val finalDamage = if (isHeadshot) baseDamage * weapon.headshotMultiplier else baseDamage

                enemy.hp -= finalDamage
                enemy.isHit = true
                enemy.hitFlashTimer = 4

                // Add bullet trail up to hit point
                bulletTrails.add(
                    BulletTrail(
                        startX = centerX + cos(finalAngleRad).toFloat() * 40f,
                        startY = centerY + sin(finalAngleRad).toFloat() * 40f,
                        endX = enemy.x,
                        endY = enemy.y,
                        color = weapon.bulletColor,
                        strokeWidth = if (weapon.id == "sniper" || weapon.id == "raygun") 6f else 3f
                    )
                )

                if (isHeadshot) {
                    headshotOccurred = true
                    headshotsAdd++
                    spawnExplosionParticles(enemy.headWorldX, enemy.headWorldY, Color(0xFFFF1744), count = 18, isHeadSplatter = true)
                    addFloatingText("HEADSHOT! +${finalDamage.toInt()}", enemy.headWorldX, enemy.headWorldY - 20f, Color(0xFFFFD54F), isHeadshot = true)
                    audioSynth.playHeadshotPop()
                } else {
                    spawnExplosionParticles(enemy.x, enemy.y, weapon.bulletColor, count = 8)
                    addFloatingText("+${finalDamage.toInt()}", enemy.x, enemy.y - 10f, Color.White)
                }

                // Check Enemy Death
                if (enemy.hp <= 0) {
                    killsAdd++
                    val currentCombo = (state.combo + (if (isHeadshot) 1 else 0)).coerceAtMost(10)
                    val rewardScore = enemy.type.baseScore * currentCombo + (if (isHeadshot) 100 else 0)
                    scoreAdd += rewardScore
                    coinsAdd += enemy.type.coinsReward

                    audioSynth.playEnemySquish()
                    spawnExplosionParticles(enemy.x, enemy.y, enemy.type.skinColor, count = 22)

                    if (currentCombo > 2) {
                        addFloatingText("COMBO x$currentCombo!", enemy.x, enemy.y - 40f, Color(0xFF00E676))
                    }

                    enemies.remove(enemy)
                }
            } else {
                // Missed shot trail
                bulletTrails.add(
                    BulletTrail(
                        startX = centerX + cos(finalAngleRad).toFloat() * 40f,
                        startY = centerY + sin(finalAngleRad).toFloat() * 40f,
                        endX = rayEndX,
                        endY = rayEndY,
                        color = weapon.bulletColor,
                        strokeWidth = 2f
                    )
                )
            }
        }

        // Update Combo Multiplier
        var newCombo = state.combo
        var newComboTimer = state.comboTimer
        if (headshotOccurred) {
            newCombo = (newCombo + 1).coerceAtMost(10)
            newComboTimer = 3.5f
        } else if (hitTarget) {
            newComboTimer = 3.0f
        }

        // Check Wave Progression
        var wave = state.wave
        var waveKilled = state.waveEnemiesKilled + killsAdd
        val enemiesPerWave = 10 + wave * 4
        if (waveKilled >= enemiesPerWave) {
            wave++
            waveKilled = 0
            addFloatingText("WAVE $wave!", centerX, centerY - 100f, Color(0xFFFFD54F), isHeadshot = true)
            audioSynth.playHeadshotPop()
        }

        val totalKills = state.totalKills + killsAdd
        val newScore = state.score + scoreAdd
        val newHighScore = maxOf(state.highScore, newScore)

        _uiState.value = state.copy(
            score = newScore,
            highScore = newHighScore,
            combo = newCombo,
            comboTimer = newComboTimer,
            currentAmmo = newAmmo,
            isReloading = isReloadingNow,
            reloadProgress = if (isReloadingNow) 0f else state.reloadProgress,
            wave = wave,
            waveEnemiesKilled = waveKilled,
            totalKills = totalKills,
            headshots = state.headshots + headshotsAdd,
            coinsEarned = state.coinsEarned + coinsAdd
        )
    }

    fun triggerReload() {
        val state = _uiState.value
        if (state.isReloading || state.currentAmmo == state.maxAmmo) return
        audioSynth.playReload()
        _uiState.value = state.copy(
            isReloading = true,
            reloadProgress = 0f
        )
    }

    fun triggerNuke() {
        val state = _uiState.value
        if (state.nukesAvailable <= 0 || enemies.isEmpty()) return

        audioSynth.playNuke()

        var totalRewardScore = 0
        var totalCoins = 0
        var killsCount = 0

        val iterator = enemies.iterator()
        while (iterator.hasNext()) {
            val enemy = iterator.next()
            totalRewardScore += enemy.type.baseScore
            totalCoins += enemy.type.coinsReward
            killsCount++
            spawnExplosionParticles(enemy.x, enemy.y, Color(0xFFFF3D00), count = 25)
            iterator.remove()
        }

        addFloatingText("BOOM! NUKE STRIKE!", canvasWidth / 2f, canvasHeight / 2f - 80f, Color(0xFFFF3D00), isHeadshot = true)

        val newScore = state.score + totalRewardScore
        _uiState.value = state.copy(
            nukesAvailable = state.nukesAvailable - 1,
            score = newScore,
            highScore = maxOf(state.highScore, newScore),
            coinsEarned = state.coinsEarned + totalCoins,
            totalKills = state.totalKills + killsCount,
            screenShake = 25f
        )
    }

    fun triggerTimeFreeze() {
        val state = _uiState.value
        if (state.freezeTimer > 0) return
        audioSynth.playLaserShot()
        addFloatingText("TIME FREEZE!", canvasWidth / 2f, canvasHeight / 2f - 60f, Color(0xFF00E676))
        _uiState.value = state.copy(freezeTimer = 6.0f)
    }

    private data class HitResult(val enemy: EnemyInstance, val isHeadshot: Boolean, val dist: Float)

    private fun findTargetEnemy(tapX: Float, tapY: Float, aimRad: Double, centerX: Float, centerY: Float): HitResult? {
        var bestResult: HitResult? = null
        var minDistance = Float.MAX_VALUE

        for (enemy in enemies) {
            // Distance from tap to enemy head vs body
            val headDist = hypot(tapX - enemy.headWorldX, tapY - enemy.headWorldY)
            val bodyDist = hypot(tapX - enemy.x, tapY - enemy.y)

            // Direct tap on Head?
            if (headDist <= enemy.headHitboxRadius + 16f) {
                if (headDist < minDistance) {
                    minDistance = headDist
                    bestResult = HitResult(enemy, isHeadshot = true, dist = headDist)
                }
            } else if (bodyDist <= enemy.bodyHitboxRadius + 14f) {
                if (bodyDist < minDistance && (bestResult == null || !bestResult.isHeadshot)) {
                    minDistance = bodyDist
                    bestResult = HitResult(enemy, isHeadshot = false, dist = bodyDist)
                }
            } else {
                // Line intersection test for tap direction ray
                val rayDistToHead = pointToLineDistance(enemy.headWorldX, enemy.headWorldY, centerX, centerY, aimRad)
                val rayDistToBody = pointToLineDistance(enemy.x, enemy.y, centerX, centerY, aimRad)

                if (rayDistToHead <= enemy.headHitboxRadius + 10f) {
                    if (headDist < minDistance) {
                        minDistance = headDist
                        bestResult = HitResult(enemy, isHeadshot = true, dist = headDist)
                    }
                } else if (rayDistToBody <= enemy.bodyHitboxRadius + 10f) {
                    if (bodyDist < minDistance && (bestResult == null || !bestResult.isHeadshot)) {
                        minDistance = bodyDist
                        bestResult = HitResult(enemy, isHeadshot = false, dist = bodyDist)
                    }
                }
            }
        }
        return bestResult
    }

    private fun pointToLineDistance(px: Float, py: Float, lx: Float, ly: Float, angleRad: Double): Float {
        val dx = cos(angleRad).toFloat()
        val dy = sin(angleRad).toFloat()
        val pdx = px - lx
        val pdy = py - ly
        val projection = pdx * dx + pdy * dy
        if (projection < 0) return hypot(px - lx, py - ly)
        val projX = lx + dx * projection
        val projY = ly + dy * projection
        return hypot(px - projX, py - projY)
    }

    private fun spawnEnemy(wave: Int) {
        val side = Random.nextInt(4) // 0: Top, 1: Right, 2: Bottom, 3: Left
        var spawnX = 0f
        var spawnY = 0f

        when (side) {
            0 -> { spawnX = Random.nextFloat() * canvasWidth; spawnY = -60f }
            1 -> { spawnX = canvasWidth + 60f; spawnY = Random.nextFloat() * canvasHeight }
            2 -> { spawnX = Random.nextFloat() * canvasWidth; spawnY = canvasHeight + 60f }
            3 -> { spawnX = -60f; spawnY = Random.nextFloat() * canvasHeight }
        }

        val typeRoll = Random.nextFloat()
        val type = when {
            wave >= 5 && (wave % 5 == 0) && Random.nextFloat() < 0.4f -> EnemyType.BRUTE
            wave >= 3 && typeRoll < 0.25f -> EnemyType.EYEBALL
            wave >= 2 && typeRoll < 0.50f -> EnemyType.TANK
            typeRoll < 0.75f -> EnemyType.RUNNER
            else -> EnemyType.GOON
        }

        val enemy = EnemyInstance(
            id = nextEnemyId++,
            type = type,
            x = spawnX,
            y = spawnY,
            hp = type.maxHp,
            maxHp = type.maxHp,
            speed = type.speed + (wave * 0.1f)
        )
        enemies.add(enemy)
    }

    private fun spawnExplosionParticles(x: Float, y: Float, color: Color, count: Int, isHeadSplatter: Boolean = false) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * Math.PI.toFloat() * 2f
            val speed = Random.nextFloat() * (if (isHeadSplatter) 9f else 6f) + 2f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    size = Random.nextFloat() * 10f + 5f,
                    life = 1.0f,
                    decay = Random.nextFloat() * 0.04f + 0.03f,
                    isHeadSplatter = isHeadSplatter
                )
            )
        }
    }

    private fun addFloatingText(text: String, x: Float, y: Float, color: Color, isHeadshot: Boolean = false) {
        floatingTexts.add(
            FloatingText(
                id = nextTextId++,
                text = text,
                x = x,
                y = y,
                color = color,
                scale = if (isHeadshot) 1.5f else 1.0f,
                isHeadshot = isHeadshot
            )
        )
    }
}
