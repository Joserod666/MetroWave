package com.zuneplayer.app.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.zuneplayer.app.MainActivity
import com.zuneplayer.app.MusicService

class MetroWaveWidget : GlanceAppWidget() {

    companion object {
        val songTitleKey = ActionParameters.Key<String>("song_title")
        val songArtistKey = ActionParameters.Key<String>("song_artist")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences("metrowave_widget", Context.MODE_PRIVATE)
        val songTitle = prefs.getString("current_song_title", "MetroWave") ?: "MetroWave"
        val songArtist = prefs.getString("current_song_artist", "Tap to play") ?: "Tap to play"
        val isPlaying = prefs.getBoolean("is_playing", false)
        val albumArtUri = prefs.getString("album_art_uri", null)

        provideContent {
            GlanceTheme {
                MetroWaveWidgetContent(
                    context = context,
                    songTitle = songTitle,
                    songArtist = songArtist,
                    isPlaying = isPlaying,
                    albumArtUri = albumArtUri
                )
            }
        }
    }
}

@Composable
private fun MetroWaveWidgetContent(
    context: Context,
    songTitle: String,
    songArtist: String,
    isPlaying: Boolean,
    albumArtUri: String?
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .cornerRadius(16.dp)
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(56.dp)
                        .cornerRadius(8.dp)
                        .background(Color(0xFF2D2D2D)),
                    contentAlignment = Alignment.Center
                ) {
                    if (albumArtUri != null) {
                        // In Glance 1.0.0, ImageProvider doesn't support Uri directly for remote views.
                        // We use a placeholder or would need to load a Bitmap here.
                        Image(
                            provider = ImageProvider(com.zuneplayer.app.R.drawable.ic_music_note),
                            contentDescription = "Album Art",
                            modifier = GlanceModifier.fillMaxSize().cornerRadius(8.dp)
                        )
                    } else {
                        Image(
                            provider = ImageProvider(com.zuneplayer.app.R.drawable.ic_music_note),
                            contentDescription = "Music",
                            modifier = GlanceModifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.width(12.dp))

                Column(
                    modifier = GlanceModifier.defaultWeight()
                ) {
                    Text(
                        text = songTitle,
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = songArtist,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFF0529D)),
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(40.dp)
                        .cornerRadius(20.dp)
                        .background(Color(0xFFF0529D))
                        .clickable(actionRunCallback<PlayPauseAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = if (isPlaying) {
                            ImageProvider(com.zuneplayer.app.R.drawable.ic_pause)
                        } else {
                            ImageProvider(com.zuneplayer.app.R.drawable.ic_play)
                        },
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = GlanceModifier.size(20.dp)
                    )
                }

                Spacer(modifier = GlanceModifier.width(16.dp))

                Box(
                    modifier = GlanceModifier
                        .size(36.dp)
                        .cornerRadius(18.dp)
                        .background(Color(0xFF2D2D2D))
                        .clickable(actionRunCallback<NextSongAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(com.zuneplayer.app.R.drawable.ic_skip_next),
                        contentDescription = "Next",
                        modifier = GlanceModifier.size(18.dp)
                    )
                }

                Spacer(modifier = GlanceModifier.width(12.dp))

                Box(
                    modifier = GlanceModifier
                        .size(36.dp)
                        .cornerRadius(18.dp)
                        .background(Color(0xFF2D2D2D))
                        .clickable(actionRunCallback<PreviousSongAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(com.zuneplayer.app.R.drawable.ic_skip_previous),
                        contentDescription = "Previous",
                        modifier = GlanceModifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            Text(
                text = "METROWAVE",
                style = TextStyle(
                    color = ColorProvider(Color(0xFFF0529D)),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            )
        }
    }
}

class PlayPauseAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = Intent(context, MusicService::class.java).apply {
            action = "com.metrowave.ACTION_PLAY_PAUSE"
        }
        context.startService(intent)
    }
}

class NextSongAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = Intent(context, MusicService::class.java).apply {
            action = "com.metrowave.ACTION_NEXT"
        }
        context.startService(intent)
    }
}

class PreviousSongAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = Intent(context, MusicService::class.java).apply {
            action = "com.metrowave.ACTION_PREVIOUS"
        }
        context.startService(intent)
    }
}

class MetroWaveWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MetroWaveWidget()
}
