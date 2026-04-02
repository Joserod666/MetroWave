package com.zuneplayer.app.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zuneplayer.app.ui.theme.ZuneColors
import com.zuneplayer.app.ui.theme.AccentOrange
import com.zuneplayer.app.ui.theme.AccentPink
import com.zuneplayer.app.ui.theme.AccentBlue
import com.zuneplayer.app.ui.theme.AccentGreen
import com.zuneplayer.app.ui.theme.ZuneAccent
import com.zuneplayer.app.ui.theme.updateAccentColor

@Composable
fun SettingsScreen(
    lastFmService: com.zuneplayer.app.data.lastfm.LastFmService,
    viewModel: com.zuneplayer.app.ui.viewmodel.MusicViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("metrowave_settings", Context.MODE_PRIVATE)
    
    var showArtistBackground by remember { mutableStateOf(prefs.getBoolean("artist_background", true)) }
    var blurIntensity by remember { mutableFloatStateOf(prefs.getFloat("blur_intensity", 30f)) }
    var accentColor by remember { 
        val savedColor = prefs.getString("accent_color", "orange") ?: "orange"
        updateAccentColor(getAccentColorValue(savedColor))
        mutableStateOf(savedColor) 
    }
    var equalizerEnabled by remember { mutableStateOf(prefs.getBoolean("equalizer_enabled", false)) }
    var selectedPreset by remember { mutableIntStateOf(-1) }
    var bandLevels by remember { mutableStateOf(mapOf<Int, Int>()) }
    var bandCount by remember { mutableIntStateOf(0) }
    
    val equalizerManager = viewModel.getEqualizerManager()
    
    LaunchedEffect(equalizerManager) {
        equalizerManager?.let { eq ->
            bandCount = eq.getNumberOfBands()
            if (bandLevels.isEmpty() && bandCount > 0) {
                val levels = mutableMapOf<Int, Int>()
                for (i in 0 until bandCount) {
                    levels[i] = eq.getBandLevel(i)
                }
                bandLevels = levels
            }
            selectedPreset = eq.getCurrentPreset()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ZuneColors.Black)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ZuneColors.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Text(
                text = "SETTINGS",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = ZuneColors.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            SectionTitle("VISUALIZATION")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingToggle(
                icon = Icons.Default.Image,
                title = "Artist Background",
                subtitle = "Show artist photo as background",
                checked = showArtistBackground,
                onCheckedChange = {
                    showArtistBackground = it
                    prefs.edit().putBoolean("artist_background", it).apply()
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingSlider(
                icon = Icons.Default.DarkMode,
                title = "Blur Intensity",
                value = blurIntensity,
                onValueChange = {
                    blurIntensity = it
                    prefs.edit().putFloat("blur_intensity", it).apply()
                },
                valueRange = 0f..100f
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "ACCENT COLOR",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ZuneAccent(),
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ColorOption(
                    color = AccentOrange,
                    name = "Orange",
                    isSelected = accentColor == "orange",
                    onClick = {
                        accentColor = "orange"
                        updateAccentColor(AccentOrange)
                        prefs.edit().putString("accent_color", "orange").apply()
                    }
                )
                ColorOption(
                    color = AccentPink,
                    name = "Pink",
                    isSelected = accentColor == "pink",
                    onClick = {
                        accentColor = "pink"
                        updateAccentColor(AccentPink)
                        prefs.edit().putString("accent_color", "pink").apply()
                    }
                )
                ColorOption(
                    color = AccentBlue,
                    name = "Blue",
                    isSelected = accentColor == "blue",
                    onClick = {
                        accentColor = "blue"
                        updateAccentColor(AccentBlue)
                        prefs.edit().putString("accent_color", "blue").apply()
                    }
                )
                ColorOption(
                    color = AccentGreen,
                    name = "Green",
                    isSelected = accentColor == "green",
                    onClick = {
                        accentColor = "green"
                        updateAccentColor(AccentGreen)
                        prefs.edit().putString("accent_color", "green").apply()
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "PLAYBACK",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ZuneAccent(),
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingInfo(
                icon = Icons.Default.MusicNote,
                title = "MetroWave",
                subtitle = "Version 1.0"
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SectionTitle("EQUALIZER")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            EqualizerCard(
                equalizerManager = equalizerManager,
                enabled = equalizerEnabled,
                onEnabledChange = {
                    equalizerEnabled = it
                    equalizerManager?.setEnabled(it)
                    prefs.edit().putBoolean("equalizer_enabled", it).apply()
                },
                selectedPreset = selectedPreset,
                onPresetSelected = { preset ->
                    selectedPreset = preset
                    equalizerManager?.usePreset(preset)
                },
                bandLevels = bandLevels,
                onBandLevelChange = { band, level ->
                    bandLevels = bandLevels + (band to level)
                    equalizerManager?.setBandLevel(band, level)
                },
                bandCount = bandCount,
                onReset = {
                    bandLevels = emptyMap()
                    selectedPreset = -1
                    equalizerManager?.setEnabled(false)
                    equalizerEnabled = false
                    prefs.edit().putBoolean("equalizer_enabled", false).apply()
                }
            )
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = ZuneAccent(),
        letterSpacing = 2.sp
    )
}

@Composable
private fun SettingToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ZuneColors.DarkGray)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ZuneAccent(),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = ZuneColors.White
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = ZuneColors.LightGray
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ZuneAccent(),
                checkedTrackColor = ZuneAccent().copy(alpha = 0.5f),
                uncheckedThumbColor = ZuneColors.LightGray,
                uncheckedTrackColor = ZuneColors.MediumGray
            )
        )
    }
}

@Composable
private fun SettingSlider(
    icon: ImageVector,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ZuneColors.DarkGray)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ZuneAccent(),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = ZuneColors.White,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${value.toInt()}%",
                fontSize = 14.sp,
                color = ZuneColors.LightGray
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = ZuneAccent(),
                activeTrackColor = ZuneAccent(),
                inactiveTrackColor = ZuneColors.MediumGray
            )
        )
    }
}

@Composable
private fun SettingInfo(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ZuneColors.DarkGray)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ZuneAccent(),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = ZuneColors.White
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = ZuneColors.LightGray
            )
        }
    }
}

@Composable
private fun ColorOption(
    color: Color,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(color)
                .then(
                    if (isSelected) Modifier.border(3.dp, ZuneColors.White, RoundedCornerShape(24.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = ZuneColors.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            color = if (isSelected) ZuneAccent() else ZuneColors.LightGray
        )
    }
}

@Composable
private fun EqualizerCard(
    equalizerManager: com.zuneplayer.app.data.EqualizerManager?,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    selectedPreset: Int,
    onPresetSelected: (Int) -> Unit,
    bandLevels: Map<Int, Int>,
    onBandLevelChange: (Int, Int) -> Unit,
    bandCount: Int,
    onReset: () -> Unit
) {
    val presets = remember { equalizerManager?.getPresetNames() ?: emptyList() }
    val levelRange = remember { equalizerManager?.getBandLevelRange() ?: shortArrayOf(-1500, 1500) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ZuneColors.DarkGray)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = null,
                    tint = ZuneAccent(),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Equalizer",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = ZuneColors.White
                    )
                    Text(
                        text = if (enabled) "Active" else "Disabled",
                        fontSize = 12.sp,
                        color = if (enabled) ZuneAccent() else ZuneColors.LightGray
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onReset) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = ZuneColors.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ZuneAccent(),
                        checkedTrackColor = ZuneAccent().copy(alpha = 0.5f),
                        uncheckedThumbColor = ZuneColors.LightGray,
                        uncheckedTrackColor = ZuneColors.MediumGray
                    )
                )
            }
        }
        
        if (enabled && presets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "PRESETS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ZuneColors.LightGray,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.take(6).forEachIndexed { index, preset ->
                    val isSelected = selectedPreset == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ZuneAccent() else ZuneColors.MediumGray)
                            .clickable { onPresetSelected(index) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset.take(8),
                            fontSize = 11.sp,
                            color = ZuneColors.White,
                            maxLines = 1
                        )
                    }
                }
            }
            
            if (bandCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "BANDS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZuneColors.LightGray,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (band in 0 until bandCount.coerceAtMost(5)) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = equalizerManager?.getCenterFreq(band) ?: "",
                                fontSize = 9.sp,
                                color = ZuneColors.LightGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            VerticalSlider(
                                value = (bandLevels[band] ?: 0).toFloat(),
                                onValueChange = { onBandLevelChange(band, it.toInt()) },
                                valueRange = levelRange[0].toFloat()..levelRange[1].toFloat(),
                                modifier = Modifier.height(100.dp)
                            )
                            Text(
                                text = "${(bandLevels[band] ?: 0) / 100}dB",
                                fontSize = 9.sp,
                                color = ZuneAccent()
                            )
                        }
                    }
                }
            }
        } else if (enabled) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No equalizer bands available",
                fontSize = 12.sp,
                color = ZuneColors.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun VerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier
) {
    val accentColor = ZuneAccent()
    Box(
        modifier = modifier
            .width(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier
                .height(100.dp)
                .width(24.dp),
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = ZuneColors.MediumGray
            )
        )
    }
}

private fun getAccentColorValue(colorName: String): Color {
    return when (colorName) {
        "orange" -> AccentOrange
        "pink" -> AccentPink
        "blue" -> AccentBlue
        "green" -> AccentGreen
        else -> AccentOrange
    }
}
