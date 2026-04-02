package com.zuneplayer.app.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.zuneplayer.app.data.Song
import com.zuneplayer.app.ui.theme.ZuneAccent
import com.zuneplayer.app.ui.theme.ZuneColors
import com.zuneplayer.app.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    viewModel: MusicViewModel,
    onSongClick: (Song) -> Unit,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    val letters = remember(favoriteSongs) {
        val letterSet = mutableSetOf<String>()
        val digits = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        favoriteSongs.forEach { song ->
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
            if (firstVisibleIndex < favoriteSongs.size && firstVisibleIndex >= 0) {
                val firstChar = favoriteSongs.getOrNull(firstVisibleIndex)?.title?.firstOrNull()?.uppercaseChar()
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
                
                Column {
                    Text(
                        text = "FAVORITES",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = ZuneColors.White
                    )
                    Text(
                        text = "${favoriteSongs.size} songs",
                        fontSize = 14.sp,
                        color = ZuneColors.LightGray
                    )
                }
                
                if (favoriteSongs.isNotEmpty()) {
                    IconButton(onClick = { viewModel.playSongs(favoriteSongs) }) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play All",
                            tint = ZuneAccent(),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }

        if (favoriteSongs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = ZuneColors.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No favorites yet",
                        fontSize = 18.sp,
                        color = ZuneColors.LightGray
                    )
                    Text(
                        text = "Tap the heart icon on any song",
                        fontSize = 14.sp,
                        color = ZuneColors.LightGray.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
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
                    itemsIndexed(favoriteSongs) { index, song ->
                        val isCurrentSong = song.id == currentSong?.id
                        val digits = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
                        val firstChar = song.title.firstOrNull()?.uppercaseChar()
                        val displayLetter = if (firstChar in digits) "#" else (firstChar?.toString() ?: "#")
                        val prevFirstChar = favoriteSongs.getOrNull(index - 1)?.title?.firstOrNull()?.uppercaseChar()
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
                            FavoriteSongItem(
                                song = song,
                                isCurrentSong = isCurrentSong,
                                onClick = { onSongClick(song) },
                                onRemoveClick = { viewModel.removeFromFavorites(song) }
                            )
                        }
                    }
                }

                LettersSidebarFavorites(
                    letters = letters,
                    selectedLetter = currentLetter,
                    onLetterClick = { letter ->
                        val digits = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
                        val index = favoriteSongs.indexOfFirst {
                            val firstChar = it.title.firstOrNull()?.uppercaseChar()
                            val itemLetter = if (firstChar in digits) "#" else (firstChar?.toString() ?: "#")
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
}

@Composable
private fun LettersSidebarFavorites(
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
private fun FavoriteSongItem(
    song: Song,
    isCurrentSong: Boolean,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
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

        IconButton(onClick = onRemoveClick) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Remove from favorites",
                tint = ZuneAccent(),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
