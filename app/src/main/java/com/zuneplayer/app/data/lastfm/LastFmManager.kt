package com.zuneplayer.app.data.lastfm

import android.content.Context
import com.zuneplayer.app.data.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LastFmManager(context: Context) {
    
    private val lastFmService = LastFmService(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private var currentSong: Song? = null
    private var startTime: Long = 0
    private var hasScrobbled: Boolean = false
    private var isEnabled: Boolean = true

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (!enabled) {
            currentSong = null
            hasScrobbled = false
        }
    }

    fun isEnabled(): Boolean = isEnabled && lastFmService.isAuthenticated()

    fun updateNowPlaying(song: Song) {
        if (!isEnabled()) return
        
        if (currentSong?.id != song.id) {
            currentSong = song
            startTime = System.currentTimeMillis()
            hasScrobbled = false
            
            scope.launch {
                lastFmService.updateNowPlaying(
                    artist = song.artist,
                    track = song.title,
                    album = song.album,
                    duration = (song.duration / 1000).toInt()
                )
            }
        }
    }

    fun scrobble(song: Song) {
        if (!isEnabled()) return
        if (hasScrobbled) return
        
        hasScrobbled = true
        
        scope.launch {
            lastFmService.scrobble(
                artist = song.artist,
                track = song.title,
                album = song.album,
                duration = (song.duration / 1000).toInt()
            )
        }
    }

    fun scrobbleIfNeeded(song: Song, currentPosition: Int, duration: Int) {
        if (!isEnabled()) return
        if (currentSong?.id != song.id) {
            currentSong = song
            startTime = System.currentTimeMillis()
            hasScrobbled = false
        }
        
        val halfPlayed = duration > 0 && currentPosition > (duration / 2)
        val playedLongEnough = (System.currentTimeMillis() - startTime) > 30000
        
        if ((halfPlayed || playedLongEnough) && !hasScrobbled) {
            scrobble(song)
        }
    }

    fun onSongChanged(song: Song) {
        if (!isEnabled()) return
        updateNowPlaying(song)
    }

    fun onSongCompleted(song: Song) {
        if (!isEnabled()) return
        scrobble(song)
        currentSong = null
        hasScrobbled = false
    }
}
