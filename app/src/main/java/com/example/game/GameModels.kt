package com.example.game

import androidx.compose.ui.graphics.Color

enum class EnemyType(
    val displayName: String,
    val maxHp: Float,
    val speed: Float,
    val bodyRadius: Float,
    val headRadius: Float,
    val headOffsetY: Float, // Relative offset from center of body towards top/facing
    val baseScore: Int,
    val coinsReward: Int,
    val skinColor: Color,
    val headColor: Color
) {
    RUNNER(
        displayName = "Speedy Goof",
        maxHp = 25f,
        speed = 3.2f,
        bodyRadius = 24f,
        headRadius = 18f,
        headOffsetY = -28f,
        baseScore = 150,
        coinsReward = 3,
        skinColor = Color(0xFF81C784), // Light Green
        headColor = Color(0xFFAED581)
    ),
    GOON(
        displayName = "Goofy Goon",
        maxHp = 50f,
        speed = 2.0f,
        bodyRadius = 32f,
        headRadius = 22f,
        headOffsetY = -36f,
        baseScore = 100,
        coinsReward = 2,
        skinColor = Color(0xFF64B5F6), // Light Blue
        headColor = Color(0xFF90CAF9)
    ),
    TANK(
        displayName = "Chunky Tank",
        maxHp = 140f,
        speed = 1.1f,
        bodyRadius = 46f,
        headRadius = 32f,
        headOffsetY = -48f,
        baseScore = 300,
        coinsReward = 8,
        skinColor = Color(0xFFBA68C8), // Purple
        headColor = Color(0xFFCE93D8)
    ),
    BRUTE(
        displayName = "Intimidating Brute",
        maxHp = 350f,
        speed = 0.9f,
        bodyRadius = 58f,
        headRadius = 38f,
        headOffsetY = -62f,
        baseScore = 1000,
        coinsReward = 25,
        skinColor = Color(0xFFE57373), // Red
        headColor = Color(0xFFFF8A80)
    ),
    EYEBALL(
        displayName = "Floating Eyeball",
        maxHp = 35f,
        speed = 2.8f,
        bodyRadius = 28f,
        headRadius = 20f,
        headOffsetY = -15f,
        baseScore = 200,
        coinsReward = 5,
        skinColor = Color(0xFFFFB74D), // Orange
        headColor = Color(0xFFFFD54F)
    )
}

data class Weapon(
    val id: String,
    val name: String,
    val damage: Float,
    val fireRateMs: Long,
    val magazineSize: Int,
    val reloadTimeMs: Long,
    val headshotMultiplier: Float = 2.5f,
    val spreadAngleDeg: Float = 0f,
    val pelletCount: Int = 1,
    val cost: Int = 0,
    val description: String,
    val iconEmoji: String = "🔫",
    val bulletColor: Color = Color(0xFFFFD54F),
    val soundFx: String = "pistol"
)

object WeaponRegistry {
    val PISTOL = Weapon(
        id = "pistol",
        name = "Basic Pistol",
        damage = 30f,
        fireRateMs = 220L,
        magazineSize = 10,
        reloadTimeMs = 1200L,
        headshotMultiplier = 2.5f,
        spreadAngleDeg = 2f,
        pelletCount = 1,
        cost = 0,
        description = "Reliable, well-balanced sidearm with quick reload.",
        iconEmoji = "🔫",
        soundFx = "pistol"
    )

    val DUAL_PISTOLS = Weapon(
        id = "dual_pistols",
        name = "Dual Pistols",
        damage = 25f,
        fireRateMs = 120L,
        magazineSize = 20,
        reloadTimeMs = 1400L,
        headshotMultiplier = 2.5f,
        spreadAngleDeg = 5f,
        pelletCount = 1,
        cost = 200,
        description = "Double trouble! Twice the firing speed for rapid tapping.",
        iconEmoji = "⚔️",
        soundFx = "dual"
    )

    val SHOTGUN = Weapon(
        id = "shotgun",
        name = "Pump Shotgun",
        damage = 18f, // 18 per pellet * 6 = 108 close range
        fireRateMs = 500L,
        magazineSize = 6,
        reloadTimeMs = 2000L,
        headshotMultiplier = 2.2f,
        spreadAngleDeg = 22f,
        pelletCount = 6,
        cost = 450,
        description = "Fires a wide cone of 6 lead pellets. Deadly up close!",
        iconEmoji = "💥",
        soundFx = "shotgun"
    )

    val DOUBLE_BARREL = Weapon(
        id = "double_barrel",
        name = "Double Barrel",
        damage = 28f, // 28 * 10 = 280 destruction!
        fireRateMs = 350L,
        magazineSize = 2,
        reloadTimeMs = 1100L,
        headshotMultiplier = 2.5f,
        spreadAngleDeg = 30f,
        pelletCount = 10,
        cost = 800,
        description = "Huge 10-pellet blast pattern. Fast two-shot obliteration!",
        iconEmoji = "💣",
        soundFx = "shotgun"
    )

    val ASSAULT_RIFLE = Weapon(
        id = "assault_rifle",
        name = "Assault Rifle",
        damage = 32f,
        fireRateMs = 110L,
        magazineSize = 30,
        reloadTimeMs = 1600L,
        headshotMultiplier = 2.5f,
        spreadAngleDeg = 4f,
        pelletCount = 1,
        cost = 1200,
        description = "Rapid fire assault rifle. Shreds enemy crowds easily.",
        iconEmoji = "🌩️",
        soundFx = "rifle"
    )

    val SNIPER = Weapon(
        id = "sniper",
        name = "Sniper Rifle",
        damage = 120f,
        fireRateMs = 700L,
        magazineSize = 5,
        reloadTimeMs = 2200L,
        headshotMultiplier = 5.0f, // 600 Headshot damage!
        spreadAngleDeg = 0f,
        pelletCount = 1,
        cost = 1800,
        description = "High precision rifle with massive 5x Headshot bonus!",
        iconEmoji = "🎯",
        soundFx = "sniper"
    )

    val BANANA_CANNON = Weapon(
        id = "banana_cannon",
        name = "Banana Blaster",
        damage = 90f,
        fireRateMs = 300L,
        magazineSize = 12,
        reloadTimeMs = 1300L,
        headshotMultiplier = 3.0f,
        spreadAngleDeg = 8f,
        pelletCount = 2,
        cost = 2500,
        description = "Launches explosive potassium bananas with hilarious pop!",
        iconEmoji = "🍌",
        bulletColor = Color(0xFFFFEB3B),
        soundFx = "banana"
    )

    val RAYGUN = Weapon(
        id = "raygun",
        name = "Sci-Fi Raygun",
        damage = 150f,
        fireRateMs = 200L,
        magazineSize = 15,
        reloadTimeMs = 1500L,
        headshotMultiplier = 4.0f,
        spreadAngleDeg = 1f,
        pelletCount = 1,
        cost = 4000,
        description = "Futuristic laser beam weapon. Vaporizes enemies instantly!",
        iconEmoji = "👾",
        bulletColor = Color(0xFF00E676),
        soundFx = "laser"
    )

    val ALL_WEAPONS = listOf(
        PISTOL, DUAL_PISTOLS, SHOTGUN, DOUBLE_BARREL,
        ASSAULT_RIFLE, SNIPER, BANANA_CANNON, RAYGUN
    )

    fun getById(id: String): Weapon {
        return ALL_WEAPONS.find { it.id == id } ?: PISTOL
    }
}

data class EnemyInstance(
    val id: Long,
    val type: EnemyType,
    var x: Float,
    var y: Float,
    var hp: Float,
    val maxHp: Float,
    var speed: Float,
    var angle: Float = 0f, // Facing toward player
    var walkPhase: Float = (0..100).random().toFloat(),
    var isHit: Boolean = false,
    var hitFlashTimer: Int = 0,
    var headHitboxRadius: Float = type.headRadius,
    var bodyHitboxRadius: Float = type.bodyRadius,
    var headWorldX: Float = 0f,
    var headWorldY: Float = 0f
)

data class BulletTrail(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val color: Color,
    val strokeWidth: Float,
    var life: Float = 1.0f // Fades 1.0 -> 0.0
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var size: Float,
    var life: Float = 1.0f,
    val decay: Float = 0.05f,
    val isHeadSplatter: Boolean = false
)

data class FloatingText(
    val id: Long,
    val text: String,
    var x: Float,
    var y: Float,
    val color: Color,
    var scale: Float = 1.0f,
    var alpha: Float = 1.0f,
    val dy: Float = -2.5f,
    val isHeadshot: Boolean = false
)

data class PlayerState(
    var x: Float = 0f,
    var y: Float = 0f,
    var angle: Float = 0f, // Aim angle in degrees
    var hp: Float = 100f,
    var maxHp: Float = 100f,
    var recoilDistance: Float = 0f,
    var muzzleFlashTimer: Int = 0,
    var flashX: Float = 0f,
    var flashY: Float = 0f
)
