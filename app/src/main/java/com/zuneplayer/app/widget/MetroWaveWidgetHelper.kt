package com.zuneplayer.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.zuneplayer.app.data.Song

object MetroWaveWidgetHelper {

    fun updateWidget(context: Context, song: Song?, isPlaying: Boolean) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, MetroWaveWidgetReceiver::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isNotEmpty()) {
            val prefs = context.getSharedPreferences("metrowave_widget", Context.MODE_PRIVATE).edit()
            
            if (song != null) {
                prefs.putString("current_song_title", song.title)
                prefs.putString("current_song_artist", song.artist)
                prefs.putString("album_art_uri", song.albumArtUri?.toString())
            } else {
                prefs.putString("current_song_title", "MetroWave")
                prefs.putString("current_song_artist", "Tap to play")
                prefs.putString("album_art_uri", null)
            }
            
            prefs.putBoolean("is_playing", isPlaying)
            prefs.apply()

            val intent = Intent(context, MetroWaveWidgetReceiver::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            context.sendBroadcast(intent)
        }
    }

    fun clearWidget(context: Context) {
        val prefs = context.getSharedPreferences("metrowave_widget", Context.MODE_PRIVATE).edit()
        prefs.putString("current_song_title", "MetroWave")
        prefs.putString("current_song_artist", "Tap to play")
        prefs.putString("album_art_uri", null)
        prefs.putBoolean("is_playing", false)
        prefs.apply()

        updateWidget(context, null, false)
    }
}
