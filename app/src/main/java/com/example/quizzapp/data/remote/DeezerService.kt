package com.example.quizzapp.data.remote

import android.util.Log
import com.example.quizzapp.data.model.Track
import javax.inject.Inject

interface DeezerService {
    suspend fun searchTracks(query: String): List<Track>
    suspend fun getTrack(id: String): Track
}

class DeezerServiceImpl @Inject constructor(
    private val api: DeezerApi
) : DeezerService {
    override suspend fun searchTracks(query: String): List<Track> {
        return try {
            api.searchTracks(query).data.map { it.toTrack() }
        } catch (e: Exception) {
            Log.e("DeezerService", "Erreur lors de la recherche des pistes pour '$query'", e)
            throw e
        }
    }

    override suspend fun getTrack(id: String): Track {
        return try {
            api.getTrack(id).toTrack()
        } catch (e: Exception) {
            Log.e("DeezerService", "Erreur lors de la récupération de la piste $id", e)
            throw e
        }
    }
}

private fun TrackResponse.toTrack(): Track {
    return Track(
        id = id,
        title = title,
        artist = artist.name,
        albumCover = album.cover_medium,
        previewUrl = preview
    )
} 