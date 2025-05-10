package com.example.quizzapp.data.remote

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
        return api.searchTracks(query).data.map { it.toTrack() }
    }

    override suspend fun getTrack(id: String): Track {
        return api.getTrack(id).toTrack()
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