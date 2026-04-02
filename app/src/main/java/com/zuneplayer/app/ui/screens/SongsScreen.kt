package com.zuneplayer.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zuneplayer.app.data.Song
import com.zuneplayer.app.ui.formatDuration
import com.zuneplayer.app.ui.theme.ZuneAccent
import com.zuneplayer.app.ui.theme.ZuneColors
import com.zuneplayer.app.ui.viewmodel.MusicViewModel

@Composable
fun SongsScreen(
    viewModel: MusicViewModel,
    onSongClick: (Song) -> Unit,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val songs by viewModel.songs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    
    var selectedLetter by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    
    val letters = remember(songs) {
        val letterSet = mutableSetOf<String>()
        val digits = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        songs.forEach { song ->
            val firstChar = song.title.firstOrNull()?.uppercaseChar()
            if (firstChar != null && firstChar in digits) {
                letterSet.add("#")
            } else if (firstChar != null) {
                letterSet.add(firstChar.toString())
            }
        }
        letterSet.toList().sorted().let { list ->
            if (list.contains("#")) {
                list.filter { it != "#" }.sorted() + "#"
            } else list
        }
    }

    val currentLetter by remember {
        derivedStateOf {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            val digits = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
            if (firstVisibleIndex < songs.size && firstVisibleIndex >= 0) {
                val firstChar = songs.getOrNull(firstVisibleIndex)?.title?.firstOrNull()?.uppercaseChar()
                if (firstChar in digits) "#" else firstChar?.toString()
            } else null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ZuneColors.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    start = 8.dp,
                    end = 16.dp,
                    bottom = 8.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ZuneColors.White
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SONGS",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = ZuneColors.White
                    )
                    Text(
                        text = "${songs.size} songs",
                        fontSize = 14.sp,
                        color = ZuneColors.LightGray
                    )
                }
                
                IconButton(
                    onClick = { viewModel.playAllSongs() }
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play All",
                        tint = ZuneAccent(),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(songs) { index, song ->
                    val isCurrentSong = song.id == currentSong?.id
                    val firstChar = song.title.firstOrNull()?.uppercaseChar()
                    val digits = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
                    val displayLetter = if (firstChar in digits) "#" else (firstChar?.toString() ?: "#")
                    val prevFirstChar = songs.getOrNull(index - 1)?.title?.firstOrNull()?.uppercaseChar()
                    val prevDisplayLetter = if (prevFirstChar in digits) "#" else (prevFirstChar?.toString() ?: "#")
                    val showLetterHeader = index == 0 || displayLetter != prevDisplayLetter

                    Column {
                        if (showLetterHeader) {
                        Text(
                            text = displayLetter,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZuneAccent(),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                        }
                        SongItem(
                            song = song,
                            isCurrentSong = isCurrentSong,
                            onClick = { onSongClick(song) }
                        )
                    }
                }
            }

            LettersSidebar(
                letters = letters,
                selectedLetter = currentLetter,
                onLetterClick = { letter ->
                    selectedLetter = letter
                    val index = songs.indexOfFirst { 
                        val firstChar = it.title.firstOrNull()?.uppercaseChar()?.toString()
                        val itemLetter = if (firstChar != null && firstChar.all { c -> c in '0'..'9' }) "#" else (firstChar ?: "#")
                        itemLetter == letter 
                    }
                    if (index >= 0) {
                        scope.launch {
                            listState.animateScrollToItem(index)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun LettersSidebar(
    letters: List<String>,
    selectedLetter: String?,
    onLetterClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(16.dp)
            .fillMaxHeight()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        letters.forEach { letter ->
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (letter == selectedLetter) ZuneAccent().copy(alpha = 0.3f)
                        else ZuneColors.Transparent
                    )
                    .clickable { onLetterClick(letter) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (letter == selectedLetter) ZuneAccent() else ZuneColors.LightGray
                )
            }
        }
    }
}

@Composable
fun SongItem(
    song: Song,
    isCurrentSong: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (isCurrentSong) 1f else 0.85f,
        animationSpec = tween(300),
        label = "alpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isCurrentSong) ZuneAccent().copy(alpha = 0.15f)
                else ZuneColors.DarkGray
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ZuneColors.MediumGray),
            contentAlignment = Alignment.Center
        ) {
            if (song.albumArtUri != null) {
                coil.compose.AsyncImage(
                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                        .data(song.albumArtUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Album art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = ZuneColors.LightGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
        Text(
            text = song.title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = if (isCurrentSong) ZuneAccent() else ZuneColors.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
            Text(
                text = song.artist,
                fontSize = 13.sp,
                color = ZuneColors.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = formatDuration(song.duration),
            fontSize = 12.sp,
            color = ZuneColors.LightGray
        )
    }
}
