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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zuneplayer.app.ui.theme.ZuneAccent
import com.zuneplayer.app.ui.theme.ZuneColors
import com.zuneplayer.app.ui.viewmodel.Album
import com.zuneplayer.app.ui.viewmodel.Artist
import com.zuneplayer.app.ui.viewmodel.MusicViewModel

@Composable
fun MixScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val artists by viewModel.artists.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val songs by viewModel.songs.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    val selectedArtists = remember { mutableStateListOf<String>() }
    val selectedAlbums = remember { mutableStateListOf<String>() }
    var showArtistPicker by remember { mutableStateOf(false) }
    var showAlbumPicker by remember { mutableStateOf(false) }
    
    val filteredArtists = artists.filter { it.name.contains(searchQuery, ignoreCase = true) }
    val filteredAlbums = albums.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || 
        it.artist.contains(searchQuery, ignoreCase = true) 
    }
    
    val selectedSongs = songs.filter { song ->
        (selectedArtists.any { it.equals(song.artist, ignoreCase = true) }) || 
        (selectedAlbums.any { it.equals(song.album, ignoreCase = true) })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ZuneColors.Black)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MIX",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = ZuneColors.White
            )
            if (selectedSongs.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${selectedSongs.size} songs",
                        fontSize = 12.sp,
                        color = ZuneColors.LightGray
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(4.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(ZuneColors.DarkGray)
                            .clickable {
                                selectedArtists.clear()
                                selectedAlbums.clear()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = ZuneColors.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(6.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(ZuneAccent())
                            .clickable { viewModel.playSongs(selectedSongs) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = ZuneColors.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        if (selectedArtists.isEmpty() && selectedAlbums.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Create your Mix",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZuneColors.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select artists or albums to create a custom playlist",
                    fontSize = 14.sp,
                    color = ZuneColors.LightGray
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PickerButton(
                        icon = Icons.Default.Person,
                        label = "Add Artists",
                        count = selectedArtists.size,
                        onClick = { showArtistPicker = true }
                    )
                    PickerButton(
                        icon = Icons.Default.Album,
                        label = "Add Albums",
                        count = selectedAlbums.size,
                        onClick = { showAlbumPicker = true }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PickerChip(
                            label = "Artists (${selectedArtists.size})",
                            onRemove = { showArtistPicker = true }
                        )
                        PickerChip(
                            label = "Albums (${selectedAlbums.size})",
                            onRemove = { showAlbumPicker = true }
                        )
                    }
                }
                
                if (selectedSongs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Add artists or albums to see songs",
                                color = ZuneColors.LightGray
                            )
                        }
                    }
                } else {
                    items(selectedSongs.take(20)) { song ->
                        MixSongItem(
                            title = song.title,
                            artist = song.artist,
                            album = song.album,
                            onClick = { viewModel.playSong(song) }
                        )
                    }
                    
                    if (selectedSongs.size > 20) {
                        item {
                            Text(
                                text = "+ ${selectedSongs.size - 20} more songs",
                                color = ZuneColors.LightGray,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }

    if (showArtistPicker) {
        ArtistPickerDialog(
            artists = artists,
            selectedArtists = selectedArtists.toList(),
            onDismiss = { showArtistPicker = false },
            onConfirm = { selected ->
                selectedArtists.clear()
                selectedArtists.addAll(selected)
                showArtistPicker = false
            }
        )
    }
    
    if (showAlbumPicker) {
        AlbumPickerDialog(
            albums = albums,
            selectedAlbums = selectedAlbums.toList(),
            onDismiss = { showAlbumPicker = false },
            onConfirm = { selected ->
                selectedAlbums.clear()
                selectedAlbums.addAll(selected)
                showAlbumPicker = false
            }
        )
    }
}

@Composable
private fun PickerButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(ZuneColors.DarkGray)
            .clickable(onClick = onClick)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ZuneAccent(),
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = ZuneColors.White
        )
        if (count > 0) {
            Text(
                text = "$count selected",
                fontSize = 12.sp,
                color = ZuneAccent()
            )
        }
    }
}

@Composable
private fun PickerChip(
    label: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(ZuneAccent().copy(alpha = 0.2f))
            .clickable(onClick = onRemove)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = ZuneAccent()
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = ZuneAccent(),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun MixSongItem(
    title: String,
    artist: String,
    album: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ZuneColors.MediumGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = ZuneColors.LightGray,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = ZuneColors.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$artist • $album",
                fontSize = 12.sp,
                color = ZuneColors.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ArtistPickerDialog(
    artists: List<Artist>,
    selectedArtists: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var selectedList by remember { mutableStateOf(selectedArtists.toSet()) }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredArtists = remember(artists, searchQuery) {
        artists.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZuneColors.Black.copy(alpha = 0.95f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp)
                .clickable { }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Artists",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZuneColors.White
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ZuneColors.MediumGray)
                                .clickable(onClick = onDismiss)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Cancel", color = ZuneColors.White, fontSize = 14.sp)
                        }
                        Box(
                            modifier = Modifier
                                .shadow(6.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(ZuneAccent())
                                .clickable { onConfirm(selectedList.toList()) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Done (${selectedList.size})",
                                fontWeight = FontWeight.Bold,
                                color = ZuneColors.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search artists...", color = ZuneColors.LightGray) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = ZuneColors.LightGray)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = ZuneColors.White,
                        unfocusedTextColor = ZuneColors.White,
                        focusedContainerColor = ZuneColors.DarkGray,
                        unfocusedContainerColor = ZuneColors.DarkGray,
                        cursorColor = ZuneAccent(),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (filteredArtists.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (artists.isEmpty()) "No artists found" else "No matches",
                            color = ZuneColors.LightGray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredArtists.size) { index ->
                            val artist = filteredArtists[index]
                            val isSelected = selectedList.contains(artist.name)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ZuneAccent().copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable {
                                        selectedList = if (isSelected) {
                                            selectedList - artist.name
                                        } else {
                                            selectedList + artist.name
                                        }
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .shadow(2.dp, RoundedCornerShape(12.dp))
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) ZuneAccent() else ZuneColors.MediumGray
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = ZuneColors.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = artist.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ZuneColors.White
                                    )
                                    Text(
                                        text = "${artist.songCount} songs",
                                        fontSize = 12.sp,
                                        color = ZuneColors.LightGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumPickerDialog(
    albums: List<Album>,
    selectedAlbums: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var selectedList by remember { mutableStateOf(selectedAlbums.toSet()) }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredAlbums = remember(albums, searchQuery) {
        albums.filter { 
            it.name.contains(searchQuery, ignoreCase = true) || 
            it.artist.contains(searchQuery, ignoreCase = true) 
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZuneColors.Black.copy(alpha = 0.95f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp)
                .clickable { }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Albums",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZuneColors.White
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ZuneColors.MediumGray)
                                .clickable(onClick = onDismiss)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Cancel", color = ZuneColors.White, fontSize = 14.sp)
                        }
                        Box(
                            modifier = Modifier
                                .shadow(6.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(ZuneAccent())
                                .clickable { onConfirm(selectedList.toList()) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Done (${selectedList.size})",
                                fontWeight = FontWeight.Bold,
                                color = ZuneColors.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search albums...", color = ZuneColors.LightGray) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = ZuneColors.LightGray)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = ZuneColors.White,
                        unfocusedTextColor = ZuneColors.White,
                        focusedContainerColor = ZuneColors.DarkGray,
                        unfocusedContainerColor = ZuneColors.DarkGray,
                        cursorColor = ZuneAccent(),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (filteredAlbums.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (albums.isEmpty()) "No albums found" else "No matches",
                            color = ZuneColors.LightGray
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(bottom = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredAlbums.size) { index ->
                            val album = filteredAlbums[index]
                            val isSelected = selectedList.contains(album.name)
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ZuneAccent().copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable {
                                        selectedList = if (isSelected) {
                                            selectedList - album.name
                                        } else {
                                            selectedList + album.name
                                        }
                                    }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .shadow(4.dp, RoundedCornerShape(8.dp))
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) ZuneAccent() else ZuneColors.MediumGray
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = ZuneColors.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Album,
                                            contentDescription = null,
                                            tint = ZuneColors.LightGray,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = album.name,
                                    fontSize = 11.sp,
                                    color = ZuneColors.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = album.artist,
                                    fontSize = 10.sp,
                                    color = ZuneColors.LightGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
