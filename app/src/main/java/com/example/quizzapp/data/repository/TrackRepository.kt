package com.example.quizzapp.data.repository

import com.example.quizzapp.data.model.Track
import com.example.quizzapp.data.remote.DeezerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TrackRepository @Inject constructor(
    private val deezerService: DeezerService
) {
    suspend fun searchTracks(query: String): Result<List<Track>> = withContext(Dispatchers.IO) {
        try {
            val tracks = deezerService.searchTracks(query)
            Result.success(tracks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrack(id: String): Result<Track> = withContext(Dispatchers.IO) {
        try {
            val track = deezerService.getTrack(id)
            Result.success(track)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRefreshedTrack(track: Track): Result<Track> = withContext(Dispatchers.IO) {
        try {
            val refreshedTrack = deezerService.getTrack(track.id)
            Result.success(refreshedTrack)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 