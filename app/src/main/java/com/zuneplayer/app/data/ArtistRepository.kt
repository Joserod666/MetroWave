package com.zuneplayer.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

class ArtistRepository {

    companion object {
        private const val AUDIODB_API = "https://theaudiodb.com/api/v1/json/2/search.php"
        private const val WIKIPEDIA_API = "https://en.wikipedia.org/api/rest_v1/page/summary/"
        private const val DEEZER_API = "https://api.deezer.com/search/artist&q="
    }

    suspend fun getArtistInfo(artistName: String): Result<ArtistInfo> = withContext(Dispatchers.IO) {
        try {
            val encodedArtist = URLEncoder.encode(artistName, "UTF-8")
            val audioDbUrl = "$AUDIODB_API?s=$encodedArtist"
            
            val response = URL(audioDbUrl).readText()
            val json = JSONObject(response)
            
            val artists = json.optJSONArray("artists")
            if (artists != null && artists.length() > 0) {
                val artistJson = artists.getJSONObject(0)
                
                val name = artistJson.optString("strArtist", artistName)
                val bio = artistJson.optString("strBiographyEN").takeIf { it.isNotEmpty() && it != "null" }
                    ?: getWikipediaBio(artistName)
                
                val imageUrl = artistJson.optString("strArtistThumb").takeIf { it.isNotEmpty() && it != "null" }
                    ?: artistJson.optString("strArtistThumbHQ").takeIf { it.isNotEmpty() && it != "null" }
                    ?: artistJson.optString("strArtistWideThumb").takeIf { it.isNotEmpty() && it != "null" }
                
                if (imageUrl != null) {
                    return@withContext Result.success(
                        ArtistInfo(
                            name = name,
                            imageUrl = imageUrl,
                            bio = bio
                        )
                    )
                }
            }
            
            getDeezerWithFallback(artistName)
        } catch (e: Exception) {
            getDeezerWithFallback(artistName)
        }
    }

    private suspend fun getDeezerWithFallback(artistName: String): Result<ArtistInfo> = withContext(Dispatchers.IO) {
        try {
            val encodedArtist = URLEncoder.encode(artistName, "UTF-8")
            val deezerUrl = "$DEEZER_API$encodedArtist"
            
            val response = URL(deezerUrl).readText()
            val json = JSONObject(response)
            
            val data = json.optJSONArray("data")
            if (data != null && data.length() > 0) {
                val artistJson = data.getJSONObject(0)
                val imageUrl = artistJson.optString("picture_xl")
                    .takeIf { it.isNotEmpty() && it != "null" }
                    ?: artistJson.optString("picture_big")
                    .takeIf { it.isNotEmpty() && it != "null" }
                    ?: artistJson.optString("picture_medium")
                    .takeIf { it.isNotEmpty() && it != "null" }
                
                if (imageUrl != null) {
                    return@withContext Result.success(
                        ArtistInfo(
                            name = artistJson.optString("name", artistName),
                            imageUrl = imageUrl,
                            bio = getWikipediaBio(artistName)
                        )
                    )
                }
            }
            
            getWikipediaWithFallback(artistName)
        } catch (e: Exception) {
            getWikipediaWithFallback(artistName)
        }
    }

    private suspend fun getWikipediaWithFallback(artistName: String): Result<ArtistInfo> = withContext(Dispatchers.IO) {
        try {
            val encodedArtist = URLEncoder.encode(artistName, "UTF-8")
            val url = "$WIKIPEDIA_API$encodedArtist"
            
            val response = URL(url).readText()
            val json = JSONObject(response)
            
            val name = json.optString("title", artistName)
            val description = if (json.has("extract")) json.optString("extract") else null
            val imageUrl = json.optJSONObject("originalimage")?.optString("source")
                ?: json.optJSONObject("thumbnail")?.optString("source")
            
            Result.success(
                ArtistInfo(
                    name = name,
                    imageUrl = imageUrl,
                    bio = description
                )
            )
        } catch (e: Exception) {
            Result.success(
                ArtistInfo(
                    name = artistName,
                    imageUrl = null,
                    bio = null
                )
            )
        }
    }

    private suspend fun getWikipediaBio(artistName: String): String? = withContext(Dispatchers.IO) {
        try {
            val encodedArtist = URLEncoder.encode(artistName, "UTF-8")
            val url = "$WIKIPEDIA_API$encodedArtist"
            val response = URL(url).readText()
            val json = JSONObject(response)
            if (json.has("extract")) json.getString("extract") else null
        } catch (e: Exception) {
            null
        }
    }
}
