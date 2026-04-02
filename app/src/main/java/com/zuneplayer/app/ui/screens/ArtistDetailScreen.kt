package com.zuneplayer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.zuneplayer.app.data.ArtistInfo
import com.zuneplayer.app.data.Song
import com.zuneplayer.app.ui.formatDuration
import com.zuneplayer.app.ui.theme.ZuneAccent
import com.zuneplayer.app.ui.theme.ZuneColors
import com.zuneplayer.app.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.launch

@Composable
fun ArtistDetailScreen(
    artistName: String,
    viewModel: MusicViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.songs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val artistSongs = songs.filter { it.artist.equals(artistName, ignoreCase = true) }
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsState()
    val artistInfo by viewModel.currentArtistInfo.collectAsState()
    val isLoadingInfo by viewModel.isLoadingArtistInfo.collectAsState()
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val letters = remember(artistSongs) {
        val letterSet = mutableSetOf<String>()
        val digits = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        artistSongs.forEach { song ->
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
            if (firstVisibleIndex < artistSongs.size && firstVisibleIndex >= 0) {
                val firstChar = artistSongs.getOrNull(firstVisibleIndex)?.title?.firstOrNull()?.uppercaseChar()
                if (firstChar in digits) "#" else firstChar?.toString()
            } else null
        }
    }

    LaunchedEffect(artistName) {
        viewModel.loadArtistInfo(artistName)
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(ZuneColors.Black)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp)
        ) {
            item {
                ArtistHeader(
                    artistName = artistName,
                    artistInfo = artistInfo,
                    isLoading = isLoadingInfo,
                    onBackClick = onBackClick
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${artistSongs.size} songs",
                        fontSize = 14.sp,
                        color = ZuneColors.LightGray
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleShuffle() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (isShuffleEnabled) ZuneAccent() else ZuneColors.LightGray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                if (artistSongs.isNotEmpty()) {
                                    viewModel.playSong(artistSongs.first())
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play All",
                                tint = ZuneColors.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            if (artistInfo?.bio != null && artistInfo?.bio!!.isNotEmpty()) {
                item {
                    ArtistBioSection(bio = artistInfo?.bio ?: "")
                }
            }

            item {
                Text(
                    text = "SONGS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZuneAccent(),
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
            }

            itemsIndexed(artistSongs) { index, song ->
                val digits = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
                val firstChar = song.title.firstOrNull()?.uppercaseChar()
                val displayLetter = if (firstChar in digits) "#" else (firstChar?.toString() ?: "#")
                val prevFirstChar = artistSongs.getOrNull(index - 1)?.title?.firstOrNull()?.uppercaseChar()
                val prevDisplayLetter = if (prevFirstChar in digits) "#" else (prevFirstChar?.toString() ?: "#")
                val showLetterHeader = index == 0 || displayLetter != prevDisplayLetter

                Column {
                    if (showLetterHeader) {
                        Text(
                            text = displayLetter,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZuneAccent(),
                            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                        )
                    }
                    val isCurrentSong = song.id == currentSong?.id
                    ArtistSongItem(
                        song = song,
                        isCurrentSong = isCurrentSong,
                        onClick = { viewModel.playSong(song) }
                    )
                }
            }
        }

        ArtistLettersSidebar(
            letters = letters,
            selectedLetter = currentLetter,
            onLetterClick = { letter ->
                val digits = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
                val index = artistSongs.indexOfFirst {
                    val firstChar = it.title.firstOrNull()?.uppercaseChar()
                    val itemLetter = if (firstChar in digits) "#" else (firstChar?.toString() ?: "#")
                    itemLetter == letter
                }
                if (index >= 0) {
                    val headerItemsCount = 4
                    scope.launch {
                        listState.animateScrollToItem(headerItemsCount + index)
                    }
                }
            }
        )
    }
}

@Composable
private fun ArtistLettersSidebar(
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
                        else Color.Transparent
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
fun ArtistHeader(
    artistName: String,
    artistInfo: ArtistInfo?,
    isLoading: Boolean,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ZuneColors.DarkGray)
        )

        artistInfo?.imageUrl?.let { imageUrl ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Artist Image",
                modifier = Modifier
                    .fillMaxSize()
                    .blur(40.dp)
                    .alpha(0.4f),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            ZuneColors.Black.copy(alpha = 0.4f),
                            ZuneColors.Black
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ZuneColors.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(ZuneColors.MediumGray),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = ZuneAccent(),
                        strokeWidth = 3.dp
                    )
                } else if (artistInfo?.imageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(artistInfo?.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Artist Photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(1.1f),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = ZuneColors.LightGray,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = artistName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = ZuneColors.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun ArtistBioSection(bio: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "ABOUT",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ZuneAccent(),
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = bio,
            fontSize = 14.sp,
            color = ZuneColors.LightGray,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun ArtistSongItem(
    song: Song,
    isCurrentSong: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isCurrentSong) ZuneColors.TransparentMagenta
                else ZuneColors.Transparent
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(ZuneColors.MediumGray),
            contentAlignment = Alignment.Center
        ) {
            if (song.albumArtUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song.albumArtUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Album art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
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
                text = song.album,
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
