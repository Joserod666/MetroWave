package com.zuneplayer.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object ZuneColors {
    val Black = Color(0xFF0D0D0D)
    val DarkGray = Color(0xFF1A1A1A)
    val MediumGray = Color(0xFF2D2D2D)
    val LightGray = Color(0xFF888888)
    val White = Color(0xFFFFFFFF)
    
    val Magenta = Color(0xFFE91E63)
    val MagentaBright = Color(0xFFFF4081)
    val TransparentMagenta = Color(0x40E91E63)
    val Transparent = Color(0x00000000)
    
    val CardBackground = Color(0xFF1E1E1E)
    val Divider = Color(0xFF333333)
}

private val accentColorState = mutableStateOf(Color(0xFFFF6B35))

@Composable
fun ZuneAccent(): Color {
    return accentColorState.value
}

fun updateAccentColor(color: Color) {
    accentColorState.value = color
}

val AccentOrange = Color(0xFFFF6B35)
val AccentPink = Color(0xFFE91E63)
val AccentBlue = Color(0xFF2196F3)
val AccentGreen = Color(0xFF4CAF50)
val AccentPurple = Color(0xFF9C27B0)
val AccentCyan = Color(0xFF00BCD4)
