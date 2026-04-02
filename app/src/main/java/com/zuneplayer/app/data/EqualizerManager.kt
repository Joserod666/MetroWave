package com.zuneplayer.app.data

import android.media.audiofx.Equalizer
import android.util.Log

class EqualizerManager {

    private var equalizer: Equalizer? = null
    private val bandLevels = mutableMapOf<Int, Int>()
    private var isEnabled = false

    companion object {
        private const val TAG = "EqualizerManager"
    }

    fun attachToPlayer(audioSessionId: Int) {
        try {
            release()
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = isEnabled
            }
            loadBandLevels()
            Log.d(TAG, "Equalizer attached to audio session: $audioSessionId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach equalizer: ${e.message}")
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        equalizer?.enabled = enabled
    }

    fun isEnabled(): Boolean = isEnabled

    fun getNumberOfBands(): Int = equalizer?.numberOfBands?.toInt() ?: 0

    fun getCenterFreq(band: Int): String {
        return try {
            equalizer?.getCenterFreq(band.toShort())?.div(1000)?.toString() + " Hz" ?: "N/A"
        } catch (e: Exception) {
            "N/A"
        }
    }

    fun getBandLevelRange(): ShortArray {
        return equalizer?.bandLevelRange ?: shortArrayOf(-1500, 1500)
    }

    fun getBandLevel(band: Int): Int {
        return try {
            equalizer?.getBandLevel(band.toShort())?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun setBandLevel(band: Int, level: Int) {
        try {
            equalizer?.setBandLevel(band.toShort(), level.toShort())
            bandLevels[band] = level
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set band level: ${e.message}")
        }
    }

    fun getPresetNames(): List<String> {
        val presets = mutableListOf<String>()
        try {
            equalizer?.let { eq ->
                for (i in 0 until eq.numberOfPresets) {
                    presets.add(eq.getPresetName(i.toShort()))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get presets: ${e.message}")
        }
        return presets
    }

    fun usePreset(presetIndex: Int) {
        try {
            equalizer?.usePreset(presetIndex.toShort())
            savePreset(presetIndex)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to use preset: ${e.message}")
        }
    }

    fun getCurrentPreset(): Int {
        return try {
            equalizer?.currentPreset?.toInt() ?: -1
        } catch (e: Exception) {
            -1
        }
    }

    private fun loadBandLevels() {
        try {
            equalizer?.let { eq ->
                for (i in 0 until eq.numberOfBands.toInt()) {
                    bandLevels[i] = eq.getBandLevel(i.toShort()).toInt()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load band levels: ${e.message}")
        }
    }

    fun saveBandLevels(): Map<Int, Int> = bandLevels.toMap()

    fun restoreBandLevels(levels: Map<Int, Int>) {
        levels.forEach { (band, level) ->
            setBandLevel(band, level)
        }
    }

    private fun savePreset(presetIndex: Int) {
        // Preset saving handled externally via SharedPreferences
    }

    fun release() {
        try {
            equalizer?.release()
            equalizer = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release equalizer: ${e.message}")
        }
    }
}
