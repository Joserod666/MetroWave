package com.zuneplayer.app.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.ContentUris
import android.net.Uri
import android.os.IBinder
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zuneplayer.app.MusicService
import com.zuneplayer.app.data.Song
import com.zuneplayer.app.data.ArtistInfo
import com.zuneplayer.app.data.ArtistRepository
import com.zuneplayer.app.data.EqualizerManager
import com.zuneplayer.app.data.lastfm.LastFmManager
import com.zuneplayer.app.data.lastfm.LastFmService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Artist(
    val name: String,
    val songCount: Int,
    val songs: List<Song> = emptyList()
)

data class Genre(
    val name: String,
    val songCount: Int,
    val songs: List<Song> = emptyList()
)

data class Album(
    val name: String,
    val artist: String,
    val songCount: Int,
    val songs: List<Song> = emptyList()
)

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private var musicService: MusicService? = null
    private var isBound = false
    private var progressJob: Job? = null
    
    val lastFmManager = LastFmManager(application)
    val lastFmService = LastFmService(application)
    private val artistRepository = ArtistRepository()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _favoriteSongs = MutableStateFlow<List<Song>>(emptyList())
    val favoriteSongs: StateFlow<List<Song>> = _favoriteSongs.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLastFmEnabled = MutableStateFlow(lastFmManager.isEnabled())
    val isLastFmEnabled: StateFlow<Boolean> = _isLastFmEnabled.asStateFlow()

    private val _currentArtistInfo = MutableStateFlow<ArtistInfo?>(null)
    val currentArtistInfo: StateFlow<ArtistInfo?> = _currentArtistInfo.asStateFlow()

    private val _isLoadingArtistInfo = MutableStateFlow(false)
    val isLoadingArtistInfo: StateFlow<Boolean> = _isLoadingArtistInfo.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            setupServiceCallbacks()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isBound = false
        }
    }

    init {
        bindService()
        loadMusic()
    }

    private fun bindService() {
        val context = getApplication<Application>()
        Intent(context, MusicService::class.java).also { intent ->
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun setupServiceCallbacks() {
        musicService?.onSongChanged = { song ->
            _currentSong.value = song
            song?.let { 
                _duration.value = it.duration.toInt()
                lastFmManager.onSongChanged(it)
                loadArtistInfo(it.artist)
            }
        }

        musicService?.onPlaybackStateChanged = { playing ->
            _isPlaying.value = playing
            if (playing) {
                startProgressUpdates()
            } else {
                stopProgressUpdates()
            }
        }

        _isShuffleEnabled.value = musicService?.isShuffleEnabled() ?: false
        
        musicService?.getCurrentSong()?.let { song ->
            if (song != null) {
                _currentSong.value = song
                _isPlaying.value = musicService?.isPlaying() ?: false
                _duration.value = musicService?.getDuration() ?: 0
                loadArtistInfo(song.artist)
            }
        }
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                musicService?.let { service ->
                    val position = service.getCurrentPosition()
                    val dur = service.getDuration()
                    _currentPosition.value = position
                    _duration.value = dur
                    
                    _currentSong.value?.let { song ->
                        lastFmManager.scrobbleIfNeeded(song, position, dur)
                    }
                }
                delay(1000)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
    }

    fun loadMusic() {
        viewModelScope.launch {
            _isLoading.value = true
            val musicList = withContext(Dispatchers.IO) {
                querySongs()
            }
            _songs.value = musicList
            _artists.value = groupByArtist(musicList)
            _genres.value = groupByGenre(musicList)
            _albums.value = groupByAlbum(musicList)
            loadFavorites()
            _isLoading.value = false
        }
    }

    private fun loadFavorites() {
        val prefs = getApplication<Application>().getSharedPreferences("metrowave_favorites", Context.MODE_PRIVATE)
        val favoriteIds = prefs.getStringSet("favorite_ids", emptySet()) ?: emptySet()
        _favoriteSongs.value = _songs.value.filter { it.id.toString() in favoriteIds }
    }

    fun toggleFavorite(song: Song) {
        val prefs = getApplication<Application>().getSharedPreferences("metrowave_favorites", Context.MODE_PRIVATE)
        val favoriteIds = prefs.getStringSet("favorite_ids", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        
        if (favoriteIds.contains(song.id.toString())) {
            favoriteIds.remove(song.id.toString())
        } else {
            favoriteIds.add(song.id.toString())
        }
        
        prefs.edit().putStringSet("favorite_ids", favoriteIds).apply()
        loadFavorites()
    }

    fun addToFavorites(song: Song) {
        val prefs = getApplication<Application>().getSharedPreferences("metrowave_favorites", Context.MODE_PRIVATE)
        val favoriteIds = prefs.getStringSet("favorite_ids", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        favoriteIds.add(song.id.toString())
        prefs.edit().putStringSet("favorite_ids", favoriteIds).apply()
        loadFavorites()
    }

    fun removeFromFavorites(song: Song) {
        val prefs = getApplication<Application>().getSharedPreferences("metrowave_favorites", Context.MODE_PRIVATE)
        val favoriteIds = prefs.getStringSet("favorite_ids", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        favoriteIds.remove(song.id.toString())
        prefs.edit().putStringSet("favorite_ids", favoriteIds).apply()
        loadFavorites()
    }

    fun isFavorite(song: Song): Boolean {
        val prefs = getApplication<Application>().getSharedPreferences("metrowave_favorites", Context.MODE_PRIVATE)
        val favoriteIds = prefs.getStringSet("favorite_ids", emptySet()) ?: emptySet()
        return favoriteIds.contains(song.id.toString())
    }

    private fun querySongs(): List<Song> {
        val songs = mutableListOf<Song>()
        val contentResolver = getApplication<Application>().contentResolver

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.MIME_TYPE
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                val duration = cursor.getLong(durationColumn)
                val albumId = cursor.getLong(albumIdColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                )

                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"), albumId
                )

                songs.add(
                    Song(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        uri = contentUri,
                        albumArtUri = albumArtUri
                    )
                )
            }
        }
        return songs
    }

    private fun groupByArtist(songs: List<Song>): List<Artist> {
        return songs.groupBy { it.artist }
            .map { (artist, artistSongs) ->
                Artist(
                    name = artist,
                    songCount = artistSongs.size,
                    songs = artistSongs
                )
            }
            .sortedBy { it.name }
    }

    private fun groupByGenre(songs: List<Song>): List<Genre> {
        return songs.groupBy { it.artist }
            .map { (artist, artistSongs) ->
                Genre(
                    name = "Music",
                    songCount = artistSongs.size,
                    songs = artistSongs
                )
            }
            .sortedBy { it.name }
    }

    private fun groupByAlbum(songs: List<Song>): List<Album> {
        return songs.groupBy { Pair(it.album, it.artist) }
            .map { (key, albumSongs) ->
                Album(
                    name = key.first,
                    artist = key.second,
                    songCount = albumSongs.size,
                    songs = albumSongs.sortedBy { it.title }
                )
            }
            .sortedBy { it.name }
    }

    fun playSong(song: Song) {
        if (isBound) {
            musicService?.playSong(song, _songs.value)
        }
    }

    fun playArtistSongs(artist: Artist) {
        if (isBound && artist.songs.isNotEmpty()) {
            musicService?.playSong(artist.songs.first(), artist.songs)
        }
    }

    fun playGenreSongs(genre: Genre) {
        if (isBound && genre.songs.isNotEmpty()) {
            musicService?.playSong(genre.songs.first(), genre.songs)
        }
    }

    fun playAlbumSongs(album: Album) {
        if (isBound && album.songs.isNotEmpty()) {
            musicService?.playSong(album.songs.first(), album.songs)
        }
    }

    fun playAllSongs() {
        if (isBound && _songs.value.isNotEmpty()) {
            musicService?.playSong(_songs.value.first(), _songs.value)
        }
    }

    fun playSongs(songs: List<Song>) {
        if (isBound && songs.isNotEmpty()) {
            musicService?.playSong(songs.first(), songs)
        }
    }

    fun togglePlayPause() {
        musicService?.togglePlayPause()
    }

    fun playNext() {
        musicService?.playNext()
    }

    fun playPrevious() {
        musicService?.playPrevious()
    }

    fun seekTo(position: Int) {
        musicService?.seekTo(position)
    }

    fun toggleShuffle() {
        musicService?.toggleShuffle()
        _isShuffleEnabled.value = musicService?.isShuffleEnabled() ?: false
    }

    fun loadArtistInfo(artistName: String) {
        _isLoadingArtistInfo.value = true
        viewModelScope.launch {
            val result = artistRepository.getArtistInfo(artistName)
            result.onSuccess { info ->
                _currentArtistInfo.value = info
            }.onFailure {
                _currentArtistInfo.value = ArtistInfo(name = artistName, imageUrl = null, bio = null)
            }
            _isLoadingArtistInfo.value = false
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        } else {
            _searchResults.value = _songs.value.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                song.artist.contains(query, ignoreCase = true) ||
                song.album.contains(query, ignoreCase = true)
            }
        }
    }

    fun getSongsByArtist(artistName: String): List<Song> {
        return _songs.value.filter { it.artist.equals(artistName, ignoreCase = true) }
    }

    fun getEqualizerManager(): EqualizerManager? = musicService?.getEqualizerManager()

    override fun onCleared() {
        super.onCleared()
        if (isBound) {
            getApplication<Application>().unbindService(serviceConnection)
            isBound = false
        }
    }
}
