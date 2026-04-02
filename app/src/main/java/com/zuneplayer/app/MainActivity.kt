package com.zuneplayer.app

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zuneplayer.app.ui.components.MiniPlayer
import com.zuneplayer.app.ui.navigation.Screen
import com.zuneplayer.app.ui.screens.AlbumDetailScreen
import com.zuneplayer.app.ui.screens.AlbumsScreen
import com.zuneplayer.app.ui.screens.ArtistDetailScreen
import com.zuneplayer.app.ui.screens.ArtistsScreen
import com.zuneplayer.app.ui.screens.FavoritesScreen
import com.zuneplayer.app.ui.screens.NowPlayingScreen
import com.zuneplayer.app.ui.screens.SearchScreen
import com.zuneplayer.app.ui.screens.SettingsScreen
import com.zuneplayer.app.ui.screens.SongsScreen
import com.zuneplayer.app.ui.screens.SplashScreen
import com.zuneplayer.app.ui.theme.MetroWaveTheme
import com.zuneplayer.app.ui.theme.ZuneAccent
import com.zuneplayer.app.ui.theme.ZuneColors
import com.zuneplayer.app.ui.theme.AccentOrange
import com.zuneplayer.app.ui.theme.AccentPink
import com.zuneplayer.app.ui.theme.AccentBlue
import com.zuneplayer.app.ui.theme.AccentGreen
import com.zuneplayer.app.ui.theme.updateAccentColor
import com.zuneplayer.app.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        startService(Intent(this, MusicService::class.java))
        
        setContent {
            MetroWaveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ZuneColors.Black
                ) {
                    MetroWaveApp()
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent?) {
        intent?.action?.let { action ->
            when (action) {
                "com.metrowave.ACTION_PLAY_PAUSE" -> {
                    sendBroadcast(Intent(action).setComponent(
                        ComponentName(this, MusicService::class.java)
                    ))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MetroWaveApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showSplash by remember { mutableStateOf(true) }
    var currentArtist by remember { mutableStateOf<String?>(null) }
    var currentAlbum by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    val viewModel: MusicViewModel = viewModel()
    var hasPermission by remember { mutableStateOf(false) }
    
    val currentSong by viewModel.currentSong.collectAsState()
    val isLastFmEnabled by viewModel.isLastFmEnabled.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            viewModel.loadMusic()
        }
    }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("metrowave_settings", Context.MODE_PRIVATE)
        val savedColor = prefs.getString("accent_color", "orange") ?: "orange"
        updateAccentColor(getAccentColorFromName(savedColor))
        
        val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        hasPermission = ContextCompat.checkSelfPermission(
            context, audioPermission
        ) == PackageManager.PERMISSION_GRANTED
        
        if (!hasPermission) {
            permissionLauncher.launch(audioPermission)
        } else {
            viewModel.loadMusic()
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermission = Manifest.permission.POST_NOTIFICATIONS
            val hasNotificationPermission = ContextCompat.checkSelfPermission(
                context, notificationPermission
            ) == PackageManager.PERMISSION_GRANTED
            
            if (!hasNotificationPermission) {
                permissionLauncher.launch(notificationPermission)
            }
        }
    }

    if (showSplash) {
        SplashScreen(
            onNavigateToMain = { showSplash = false }
        )
        return
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 6 }
    )
    val scope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZuneColors.Black)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val pageWidth = size.width.toFloat()
                        val velocity = dragOffset / pageWidth
                        when {
                            velocity < -0.3f && pagerState.currentPage < 5 -> {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            }
                            velocity > 0.3f && pagerState.currentPage > 0 -> {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                            }
                        }
                        dragOffset = 0f
                    },
                    onDragCancel = { dragOffset = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        dragOffset += dragAmount
                    }
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> NowPlayingScreen(
                        viewModel = viewModel,
                        isLastFmEnabled = isLastFmEnabled,
                        onSettingsClick = { showSettings = true }
                    )
                    1 -> SongsScreen(
                        viewModel = viewModel,
                        onSongClick = { song -> viewModel.playSong(song) }
                    )
                    2 -> AlbumsScreen(
                        viewModel = viewModel,
                        onAlbumClick = { albumName, artistName ->
                            currentAlbum = albumName to artistName
                        }
                    )
                    3 -> ArtistsScreen(
                        viewModel = viewModel,
                        onArtistClick = { artistName ->
                            currentArtist = artistName
                        }
                    )
                    4 -> SearchScreen(
                        viewModel = viewModel,
                        onSongClick = { song -> viewModel.playSong(song) }
                    )
                    5 -> FavoritesScreen(
                        viewModel = viewModel,
                        onSongClick = { song -> viewModel.playSong(song) }
                    )
                }
            }
            
            if (currentArtist != null) {
                ArtistDetailScreen(
                    artistName = currentArtist!!,
                    viewModel = viewModel,
                    onBackClick = { currentArtist = null }
                )
            }
            
            if (currentAlbum != null) {
                AlbumDetailScreen(
                    albumName = currentAlbum!!.first,
                    artistName = currentAlbum!!.second,
                    viewModel = viewModel,
                    onBackClick = { currentAlbum = null }
                )
            }
            
            if (showSettings) {
                SettingsScreen(
                    lastFmService = viewModel.lastFmService,
                    viewModel = viewModel,
                    onBackClick = { showSettings = false }
                )
            }

            if (currentSong != null && pagerState.currentPage != 0) {
                MiniPlayer(
                    viewModel = viewModel,
                    onExpandClick = { 
                        scope.launch { pagerState.animateScrollToPage(0) }
                    }
                )
            }
        }

        PageIndicator(
            currentPage = pagerState.currentPage,
            pageCount = 6,
            onPageClick = { page ->
                scope.launch { pagerState.animateScrollToPage(page) }
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        )
    }
}

@Composable
private fun PageIndicator(
    currentPage: Int,
    pageCount: Int,
    onPageClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            Box(
                modifier = Modifier
                    .width(if (isSelected) 24.dp else 8.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isSelected) ZuneAccent() else ZuneColors.LightGray.copy(alpha = 0.5f)
                    )
                    .clickable { onPageClick(index) }
            )
        }
    }
}

private fun getAccentColorFromName(colorName: String): androidx.compose.ui.graphics.Color {
    return when (colorName) {
        "orange" -> AccentOrange
        "pink" -> AccentPink
        "blue" -> AccentBlue
        "green" -> AccentGreen
        else -> AccentOrange
    }
}
