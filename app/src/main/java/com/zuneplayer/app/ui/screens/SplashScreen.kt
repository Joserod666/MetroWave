package com.zuneplayer.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zuneplayer.app.ui.theme.ZuneAccent
import com.zuneplayer.app.ui.theme.ZuneColors

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit
) {
    var isAnimationDone by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isAnimationDone) 1f else 0.5f,
        animationSpec = tween(durationMillis = 800),
        label = "splashScale"
    )
    
    LaunchedEffect(Unit) {
        showContent = true
    }
    
    LaunchedEffect(showContent) {
        if (showContent) {
            kotlinx.coroutines.delay(2000)
            onNavigateToMain()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ZuneColors.Magenta,
                        ZuneColors.MagentaBright.copy(alpha = 0.8f),
                        ZuneAccent().copy(alpha = 0.9f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(ZuneColors.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mood,
                    contentDescription = "Happy",
                    tint = ZuneColors.White,
                    modifier = Modifier.size(80.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "METRO",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = ZuneColors.White
            )
            Text(
                text = "WAVE",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = ZuneColors.White.copy(alpha = 0.9f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Music Player",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = ZuneColors.White.copy(alpha = 0.7f)
            )
        }
    }
}
