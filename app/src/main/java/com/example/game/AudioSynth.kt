package com.example.game

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class AudioSynth {
    private var isEnabled = true
    private val scope = CoroutineScope(Dispatchers.Default)

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    fun playPistolShot() {
        if (!isEnabled) return
        scope.launch {
            generateNoiseBurst(durationMs = 80, startFreq = 800f, endFreq = 120f)
        }
    }

    fun playDualShot() {
        if (!isEnabled) return
        scope.launch {
            generateNoiseBurst(durationMs = 60, startFreq = 950f, endFreq = 180f)
        }
    }

    fun playShotgunBlast() {
        if (!isEnabled) return
        scope.launch {
            generateHeavyBlast(durationMs = 180, lowFreq = 80f)
        }
    }

    fun playSniperShot() {
        if (!isEnabled) return
        scope.launch {
            generateToneSweep(durationMs = 120, startFreq = 1200f, endFreq = 100f)
        }
    }

    fun playLaserShot() {
        if (!isEnabled) return
        scope.launch {
            generateLaserSweep(durationMs = 100, startFreq = 1800f, endFreq = 300f)
        }
    }

    fun playBananaShot() {
        if (!isEnabled) return
        scope.launch {
            generateBoing(durationMs = 120)
        }
    }

    fun playHeadshotPop() {
        if (!isEnabled) return
        scope.launch {
            // High pitch satisfying coin/headshot ping!
            generateTwoTonePing(freq1 = 1200f, freq2 = 1800f)
        }
    }

    fun playEnemySquish() {
        if (!isEnabled) return
        scope.launch {
            generateNoiseBurst(durationMs = 70, startFreq = 300f, endFreq = 60f)
        }
    }

    fun playReload() {
        if (!isEnabled) return
        scope.launch {
            generateClick(freq = 1400f)
            kotlinx.coroutines.delay(120)
            generateClick(freq = 1800f)
        }
    }

    fun playHurt() {
        if (!isEnabled) return
        scope.launch {
            generateHeavyBlast(durationMs = 140, lowFreq = 60f)
        }
    }

    fun playNuke() {
        if (!isEnabled) return
        scope.launch {
            generateHeavyBlast(durationMs = 600, lowFreq = 40f)
        }
    }

    private fun generateNoiseBurst(durationMs: Int, startFreq: Float, endFreq: Float) {
        val sampleRate = 22050
        val numSamples = sampleRate * durationMs / 1000
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val env = (1.0f - progress) * (1.0f - progress)
            val freq = startFreq + (endFreq - startFreq) * progress
            val wave = sin(2.0 * Math.PI * i * freq / sampleRate)
            val noise = (Math.random() * 2.0 - 1.0) * 0.4
            val sampleVal = ((wave * 0.6 + noise) * env * Short.MAX_VALUE).toInt()
            samples[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playRawAudio(samples, sampleRate)
    }

    private fun generateHeavyBlast(durationMs: Int, lowFreq: Float) {
        val sampleRate = 22050
        val numSamples = sampleRate * durationMs / 1000
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val env = Math.exp(-4.0 * progress).toFloat()
            val noise = (Math.random() * 2.0 - 1.0) * 0.8
            val sub = sin(2.0 * Math.PI * i * lowFreq / sampleRate) * 0.5
            val sampleVal = ((noise + sub) * env * Short.MAX_VALUE).toInt()
            samples[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playRawAudio(samples, sampleRate)
    }

    private fun generateToneSweep(durationMs: Int, startFreq: Float, endFreq: Float) {
        val sampleRate = 22050
        val numSamples = sampleRate * durationMs / 1000
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val env = (1.0f - progress)
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            phase += 2.0 * Math.PI * currentFreq / sampleRate
            val sampleVal = (sin(phase) * env * Short.MAX_VALUE * 0.8).toInt()
            samples[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playRawAudio(samples, sampleRate)
    }

    private fun generateLaserSweep(durationMs: Int, startFreq: Float, endFreq: Float) {
        val sampleRate = 22050
        val numSamples = sampleRate * durationMs / 1000
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val env = 1.0f - progress
            val currentFreq = startFreq * Math.pow((endFreq / startFreq).toDouble(), progress.toDouble()).toFloat()
            phase += 2.0 * Math.PI * currentFreq / sampleRate
            // Square wave laser character
            val wave = if (sin(phase) > 0) 0.6 else -0.6
            val sampleVal = (wave * env * Short.MAX_VALUE).toInt()
            samples[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playRawAudio(samples, sampleRate)
    }

    private fun generateBoing(durationMs: Int) {
        val sampleRate = 22050
        val numSamples = sampleRate * durationMs / 1000
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val env = 1.0f - progress
            val freq = 400f + sin(progress * 25f) * 200f
            phase += 2.0 * Math.PI * freq / sampleRate
            val sampleVal = (sin(phase) * env * Short.MAX_VALUE * 0.7).toInt()
            samples[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playRawAudio(samples, sampleRate)
    }

    private fun generateTwoTonePing(freq1: Float, freq2: Float) {
        val sampleRate = 22050
        val numSamples = sampleRate * 120 / 1000
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val env = 1.0f - progress
            val freq = if (progress < 0.4f) freq1 else freq2
            phase += 2.0 * Math.PI * freq / sampleRate
            val sampleVal = (sin(phase) * env * Short.MAX_VALUE * 0.85).toInt()
            samples[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playRawAudio(samples, sampleRate)
    }

    private fun generateClick(freq: Float) {
        val sampleRate = 22050
        val numSamples = sampleRate * 20 / 1000
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val env = 1.0f - progress
            val wave = sin(2.0 * Math.PI * i * freq / sampleRate)
            val sampleVal = (wave * env * Short.MAX_VALUE * 0.7).toInt()
            samples[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playRawAudio(samples, sampleRate)
    }

    private fun playRawAudio(samples: ShortArray, sampleRate: Int) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(samples, 0, samples.size)
            track.play()
            scope.launch {
                kotlinx.coroutines.delay((samples.size * 1000L / sampleRate) + 100)
                track.release()
            }
        } catch (_: Exception) {
            // Safe fallback if audio hardware is unavailable
        }
    }
}
