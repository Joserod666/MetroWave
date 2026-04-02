package com.zuneplayer.app.data.lastfm

import android.content.Context
import android.content.SharedPreferences
import com.zuneplayer.app.data.ArtistInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest

data class LastFmCredentials(
    val username: String,
    val apiKey: String,
    val apiSecret: String,
    val sessionKey: String
)

class LastFmService(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "lastfm_prefs"
        private const val KEY_USERNAME = "username"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_API_SECRET = "api_secret"
        private const val KEY_SESSION_KEY = "session_key"
        private const val BASE_URL = "https://ws.audioscrobbler.com/2.0/"
        
        const val API_KEY = "d2544f5357b728caf6cfed9b4f655869"
        const val API_SECRET = "3fc44f62ecd3d43ec54c5a28750c96a6"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isAuthenticated(): Boolean {
        return getSessionKey() != null && getSessionKey()!!.isNotEmpty()
    }

    fun saveCredentials(credentials: LastFmCredentials) {
        prefs.edit().apply {
            putString(KEY_USERNAME, credentials.username)
            putString(KEY_API_KEY, credentials.apiKey)
            putString(KEY_API_SECRET, credentials.apiSecret)
            putString(KEY_SESSION_KEY, credentials.sessionKey)
            apply()
        }
    }

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
    fun getApiKey(): String? = prefs.getString(KEY_API_KEY, API_KEY)
    fun getApiSecret(): String? = prefs.getString(KEY_API_SECRET, API_SECRET)
    fun getSessionKey(): String? = prefs.getString(KEY_SESSION_KEY, null)

    fun clearCredentials() {
        prefs.edit().clear().apply()
    }

    suspend fun scrobble(
        artist: String,
        track: String,
        album: String? = null,
        duration: Int? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val sessionKey = getSessionKey() ?: return@withContext Result.failure(Exception("Not authenticated"))
            val apiKey = getApiKey() ?: return@withContext Result.failure(Exception("No API key"))
            val apiSecret = getApiSecret() ?: return@withContext Result.failure(Exception("No API secret"))

            val timestamp = (System.currentTimeMillis() / 1000).toString()
            val sk = sessionKey

            val params = mutableListOf<Pair<String, String>>()
            params.add("artist" to artist)
            params.add("track" to track)
            params.add("timestamp" to timestamp)
            if (album != null) params.add("album" to album)
            if (duration != null) params.add("duration" to duration.toString())
            params.add("sk" to sk)

            val signature = createSignature(params, apiSecret)
            
            val body = buildString {
                append("method=track.scrobble")
                append("&artist=${URLEncoder.encode(artist, "UTF-8")}")
                append("&track=${URLEncoder.encode(track, "UTF-8")}")
                append("&timestamp=$timestamp")
                if (album != null) append("&album=${URLEncoder.encode(album, "UTF-8")}")
                if (duration != null) append("&duration=$duration")
                append("&sk=$sk")
                append("&api_key=$apiKey")
                append("&api_sig=$signature")
                append("&format=json")
            }

            val response = postRequest(body, apiKey)
            
            if (response.contains("\"status\":\"OK\"")) {
                Result.success(true)
            } else {
                Result.failure(Exception("Scrobble failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNowPlaying(
        artist: String,
        track: String,
        album: String? = null,
        duration: Int? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val sessionKey = getSessionKey() ?: return@withContext Result.failure(Exception("Not authenticated"))
            val apiKey = getApiKey() ?: return@withContext Result.failure(Exception("No API key"))
            val apiSecret = getApiSecret() ?: return@withContext Result.failure(Exception("No API secret"))

            val timestamp = (System.currentTimeMillis() / 1000).toString()

            val params = mutableListOf<Pair<String, String>>()
            params.add("artist" to artist)
            params.add("track" to track)
            if (album != null) params.add("album" to album)
            if (duration != null) params.add("duration" to duration.toString())
            params.add("sk" to sessionKey)

            val signature = createSignature(params, apiSecret)

            val body = buildString {
                append("method=track.updateNowPlaying")
                append("&artist=${URLEncoder.encode(artist, "UTF-8")}")
                append("&track=${URLEncoder.encode(track, "UTF-8")}")
                if (album != null) append("&album=${URLEncoder.encode(album, "UTF-8")}")
                if (duration != null) append("&duration=$duration")
                append("&sk=$sessionKey")
                append("&api_key=$apiKey")
                append("&api_sig=$signature")
                append("&format=json")
            }

            val response = postRequest(body, apiKey)
            
            if (response.contains("\"status\":\"OK\"")) {
                Result.success(true)
            } else {
                Result.failure(Exception("Now playing update failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSessionToken(apiKey: String, apiSecret: String, token: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val timestamp = (System.currentTimeMillis() / 1000).toString()
            
            val params = listOf(
                "method" to "auth.getsession",
                "token" to token,
                "timestamp" to timestamp
            )
            
            val signature = createSignature(params, apiSecret)

            val body = buildString {
                append("method=auth.getsession")
                append("&api_key=$apiKey")
                append("&token=$token")
                append("&timestamp=$timestamp")
                append("&api_sig=$signature")
                append("&format=json")
            }

            val response = postRequest(body, apiKey)
            val json = JSONObject(response)
            
            if (json.has("session")) {
                val session = json.getJSONObject("session")
                val sessionKey = session.getString("key")
                val username = session.getString("name")
                Result.success("$username|$sessionKey")
            } else {
                Result.failure(Exception("Invalid token or authorization failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAuthUrl(apiKey: String): String {
        return "https://www.last.fm/api/auth/?api_key=$apiKey"
    }

    private fun createSignature(params: List<Pair<String, String>>, apiSecret: String): String {
        val sortedParams = params.sortedBy { it.first }
        val message = buildString {
            sortedParams.forEach { (key, value) ->
                append("$key$value")
            }
        } + apiSecret

        return md5(message)
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun postRequest(body: String, apiKey: String): String {
        val url = URL("$BASE_URL?api_key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        conn.setRequestProperty("Content-Length", body.length.toString())

        OutputStreamWriter(conn.outputStream).use { writer ->
            writer.write(body)
            writer.flush()
        }

        return conn.inputStream.bufferedReader().use { it.readText() }
    }

    suspend fun getArtistInfo(artistName: String): Result<ArtistInfo> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey() ?: return@withContext Result.failure(Exception("No API key"))
            val encodedArtist = URLEncoder.encode(artistName, "UTF-8")
            val url = "$BASE_URL?method=artist.getinfo&artist=$encodedArtist&api_key=$apiKey&format=json"
            
            val response = URL(url).readText()
            val json = JSONObject(response)
            
            if (json.has("artist")) {
                val artistJson = json.getJSONObject("artist")
                
                val name = artistJson.optString("name", artistName)
                val bio = artistJson.optJSONObject("bio")?.optString("summary")?.let {
                    it.replace(Regex("<[^>]*>"), "")
                }
                
                val imageUrl = artistJson.optJSONArray("image")?.let { images ->
                    findBestImage(images)
                }
                
                Result.success(
                    ArtistInfo(
                        name = name,
                        imageUrl = imageUrl,
                        bio = bio
                    )
                )
            } else {
                Result.failure(Exception("Artist not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun findBestImage(images: org.json.JSONArray): String? {
        var bestImage: String? = null
        for (i in 0 until images.length()) {
            images.optJSONObject(i)?.optString("#text")?.let { url ->
                if (url.isNotEmpty()) {
                    bestImage = url
                }
            }
        }
        return bestImage
    }
}
