package com.zuneplayer.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ZuneColorScheme = darkColorScheme(
    primary = ZuneColors.Magenta,
    onPrimary = ZuneColors.White,
    primaryContainer = ZuneColors.MagentaBright,
    onPrimaryContainer = ZuneColors.White,
    secondary = ZuneColors.Magenta,
    onSecondary = ZuneColors.White,
    background = ZuneColors.Black,
    onBackground = ZuneColors.White,
    surface = ZuneColors.DarkGray,
    onSurface = ZuneColors.White,
    surfaceVariant = ZuneColors.MediumGray,
    onSurfaceVariant = ZuneColors.LightGray,
    outline = ZuneColors.MediumGray
)

@Composable
fun MetroWaveTheme(content: @Composable () -> Unit) {
    val colorScheme = ZuneColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ZuneColors.Black.toArgb()
            window.navigationBarColor = ZuneColors.Black.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ZuneTypography,
        content = content
    )
}
