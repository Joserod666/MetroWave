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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zuneplayer.app.ui.theme.ZuneAccent
import com.zuneplayer.app.ui.theme.ZuneColors
import com.zuneplayer.app.ui.viewmodel.Artist
import com.zuneplayer.app.ui.viewmodel.MusicViewModel

@Composable
fun ArtistsScreen(
    viewModel: MusicViewModel,
    onArtistClick: (String) -> Unit,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val artists by viewModel.artists.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val letters = remember(artists) {
        val letterSet = mutableSetOf<String>()
        val digits = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        artists.forEach { artist ->
            val firstChar = artist.name.firstOrNull()?.uppercaseChar()
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
            if (firstVisibleIndex < artists.size && firstVisibleIndex >= 0) {
                val firstChar = artists.getOrNull(firstVisibleIndex)?.name?.firstOrNull()?.uppercaseChar()
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
                        text = "ARTISTS",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = ZuneColors.White
                    )
                    Text(
                        text = "${artists.size} artists",
                        fontSize = 14.sp,
                        color = ZuneColors.LightGray
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
                itemsIndexed(artists) { index, artist ->
                    val digits = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
                    val firstChar = artist.name.firstOrNull()?.uppercaseChar()
                    val displayLetter = if (firstChar in digits) "#" else (firstChar?.toString() ?: "#")
                    val prevFirstChar = artists.getOrNull(index - 1)?.name?.firstOrNull()?.uppercaseChar()
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
                        ArtistItem(
                            artist = artist,
                            onClick = { onArtistClick(artist.name) },
                            onPlayClick = { viewModel.playArtistSongs(artist) }
                        )
                    }
                }
            }

            LettersSidebarArtists(
                letters = letters,
                selectedLetter = currentLetter,
                onLetterClick = { letter ->
                    val index = artists.indexOfFirst {
                        val firstChar = it.name.firstOrNull()?.uppercaseChar()?.toString()
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
fun ArtistItem(
    artist: Artist,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ZuneColors.DarkGray)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(ZuneColors.MediumGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = ZuneColors.LightGray,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = ZuneColors.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${artist.songCount} songs",
                fontSize = 13.sp,
                color = ZuneColors.LightGray
            )
        }

        IconButton(onClick = onPlayClick) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = ZuneAccent(),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
internal fun LettersSidebarArtists(
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
