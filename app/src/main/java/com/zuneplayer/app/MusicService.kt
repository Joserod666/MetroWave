package com.zuneplayer.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import com.zuneplayer.app.data.Song
import com.zuneplayer.app.data.EqualizerManager
import com.zuneplayer.app.widget.MetroWaveWidgetHelper

class MusicService : Service() {

    private val binder = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var currentSong: Song? = null
    private var playlist: List<Song> = emptyList()
    private var currentIndex: Int = -1
    private var isShuffleEnabled: Boolean = false
    private var originalPlaylist: List<Song> = emptyList()
    private lateinit var mediaSession: MediaSessionCompat
    private val equalizerManager = EqualizerManager()
    private val handler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null
    
    var onSongChanged: ((Song?) -> Unit)? = null
    var onPlaybackStateChanged: ((Boolean) -> Unit)? = null
    var onProgressChanged: ((Int, Int) -> Unit)? = null

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        mediaSession = MediaSessionCompat(this, "MetroWaveSession").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    mediaPlayer?.start()
                    onPlaybackStateChanged?.invoke(true)
                    updateNotification()
                    updateWidget()
                    startProgressUpdates()
                }
                
                override fun onPause() {
                    mediaPlayer?.pause()
                    onPlaybackStateChanged?.invoke(false)
                    updateNotification()
                    updateWidget()
                    stopProgressUpdates()
                }
                
                override fun onSkipToNext() {
                    playNext()
                }
                
                override fun onSkipToPrevious() {
                    playPrevious()
                }
                
                override fun onStop() {
                    stop()
                }
            })
            isActive = true
        }
        
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
        }
        setupMediaPlayer()
        
        mediaPlayer?.let { player ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val sessionId = player.audioSessionId
                    if (sessionId != 0) {
                        equalizerManager.attachToPlayer(sessionId)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupMediaPlayer() {
        mediaPlayer?.setOnCompletionListener {
            playNext()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "metrowave_playback",
                "Now Playing",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Music playback controls"
                setShowBadge(true)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun playSong(song: Song, list: List<Song> = emptyList()) {
        if (list.isNotEmpty()) {
            originalPlaylist = list
            playlist = if (isShuffleEnabled) list.shuffled() else list
            currentIndex = playlist.indexOfFirst { it.id == song.id }
            if (isShuffleEnabled && currentIndex == -1) {
                currentIndex = 0
            }
        }
        
        currentSong = song
        
        try {
            mediaPlayer?.apply {
                reset()
                setDataSource(this@MusicService, song.uri)
                prepare()
                start()
            }
            
            updateMediaMetadata(song)
            updateMediaSession()
            startForeground(1, createNotification())
            onSongChanged?.invoke(song)
            onPlaybackStateChanged?.invoke(true)
            startProgressUpdates()
            updateWidget()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun updateMediaMetadata(song: Song) {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
            .build()
        
        mediaSession.setMetadata(metadata)
    }
    
    private fun updateMediaSession() {
        val state = if (isPlaying()) {
            PlaybackStateCompat.STATE_PLAYING
        } else {
            PlaybackStateCompat.STATE_PAUSED
        }
        
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(state, getCurrentPosition().toLong(), 1f)
            .build()
        
        mediaSession.setPlaybackState(playbackState)
        mediaSession.isActive = true
    }

    fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                stopProgressUpdates()
                onPlaybackStateChanged?.invoke(false)
            } else {
                it.start()
                startProgressUpdates()
                onPlaybackStateChanged?.invoke(true)
            }
            updateNotification()
            updateWidget()
            updateMediaSession()
        }
    }

    fun playNext() {
        if (playlist.isEmpty()) return
        
        val nextIndex = if (currentIndex < playlist.size - 1) currentIndex + 1 else 0
        currentIndex = nextIndex
        playSong(playlist[currentIndex])
    }

    fun playPrevious() {
        if (playlist.isEmpty()) return
        
        mediaPlayer?.let {
            if (it.currentPosition > 3000) {
                it.seekTo(0)
            } else {
                val prevIndex = if (currentIndex > 0) currentIndex - 1 else playlist.size - 1
                currentIndex = prevIndex
                playSong(playlist[currentIndex])
            }
        }
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0
    fun getDuration(): Int = mediaPlayer?.duration ?: 0
    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false
    fun getCurrentSong(): Song? = currentSong
    fun getPlaylist(): List<Song> = playlist
    fun isShuffleEnabled(): Boolean = isShuffleEnabled

    fun getEqualizerManager(): EqualizerManager = equalizerManager

    fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
        if (isShuffleEnabled) {
            val currentSong = currentSong
            playlist = originalPlaylist.shuffled()
            currentSong?.let { song ->
                currentIndex = playlist.indexOfFirst { it.id == song.id }
                if (currentIndex == -1) currentIndex = 0
            }
        } else {
            val currentSong = currentSong
            playlist = originalPlaylist
            currentSong?.let { song ->
                currentIndex = playlist.indexOfFirst { it.id == song.id }
            }
        }
    }

    fun stop() {
        mediaPlayer?.stop()
        stopProgressUpdates()
        stopForeground(STOP_FOREGROUND_REMOVE)
        onPlaybackStateChanged?.invoke(false)
        updateWidget()
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        onProgressChanged?.invoke(player.currentPosition, player.duration)
                    }
                }
                handler.postDelayed(this, 1000)
            }
        }
        progressRunnable?.let { handler.post(it) }
    }

    private fun stopProgressUpdates() {
        progressRunnable?.let { handler.removeCallbacks(it) }
        progressRunnable = null
    }

    private fun createNotification(): Notification {
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying()) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseTitle = if (isPlaying()) "Pause" else "Play"

        val albumArt = currentSong?.albumArtUri?.let { uri ->
            try {
                contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                null
            }
        }

        val notification = NotificationCompat.Builder(this, "metrowave_playback")
            .setContentTitle(currentSong?.title ?: "MetroWave")
            .setContentText(currentSong?.artist ?: "Unknown Artist")
            .setSubText(currentSong?.album ?: "")
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(isPlaying())

        if (albumArt != null) {
            notification.setLargeIcon(albumArt)
        }

        notification.addAction(android.R.drawable.ic_media_previous, "Previous",
            createPendingIntent("com.metrowave.ACTION_PREVIOUS"))
        notification.addAction(playPauseIcon, playPauseTitle,
            createPendingIntent("com.metrowave.ACTION_PLAY_PAUSE"))
        notification.addAction(android.R.drawable.ic_media_next, "Next",
            createPendingIntent("com.metrowave.ACTION_NEXT"))

        val style = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)
            .setShowCancelButton(true)
            .setCancelButtonIntent(createPendingIntent("com.metrowave.ACTION_STOP"))
        notification.setStyle(style)

        return notification.build()
    }

    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun updateNotification() {
        if (currentSong != null) {
            try {
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.notify(1, createNotification())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateWidget() {
        MetroWaveWidgetHelper.updateWidget(this, currentSong, isPlaying())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "com.metrowave.ACTION_PLAY" -> {
                mediaPlayer?.start()
                onPlaybackStateChanged?.invoke(true)
                startProgressUpdates()
                updateNotification()
                updateWidget()
                updateMediaSession()
            }
            "com.metrowave.ACTION_PAUSE" -> {
                mediaPlayer?.pause()
                onPlaybackStateChanged?.invoke(false)
                stopProgressUpdates()
                updateNotification()
                updateWidget()
                updateMediaSession()
            }
            "com.metrowave.ACTION_PLAY_PAUSE" -> togglePlayPause()
            "com.metrowave.ACTION_NEXT" -> playNext()
            "com.metrowave.ACTION_PREVIOUS" -> playPrevious()
            "com.metrowave.ACTION_STOP" -> {
                stop()
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (currentSong == null || !isPlaying()) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        stopProgressUpdates()
        mediaSession.release()
        equalizerManager.release()
        mediaPlayer?.release()
        mediaPlayer = null
        MetroWaveWidgetHelper.clearWidget(this)
        super.onDestroy()
    }
}
