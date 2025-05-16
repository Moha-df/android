package com.example.quizzapp.data.repository

import com.example.quizzapp.data.local.PlaylistDao
import com.example.quizzapp.data.model.Playlist
import com.example.quizzapp.data.model.PlaylistTrackCrossRef
import com.example.quizzapp.data.model.PlaylistWithTracks
import com.example.quizzapp.data.model.Track
import com.example.quizzapp.data.remote.DeezerService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val deezerService: DeezerService
) {
    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    suspend fun getPlaylist(playlistId: Long): Playlist? = playlistDao.getPlaylist(playlistId)

    fun getPlaylistWithTracks(playlistId: Long): Flow<PlaylistWithTracks?> =
        playlistDao.getPlaylistWithTracks(playlistId)

    fun getAllPlaylistsWithTracks(): Flow<List<PlaylistWithTracks>> {
        return playlistDao.getAllPlaylistsWithTracks()
    }

    suspend fun createPlaylist(playlist: Playlist) {
        playlistDao.insertPlaylist(playlist)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: String) {
        playlistDao.addTrackToPlaylist(playlistId, trackId)
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        playlistDao.removeTrackFromPlaylist(playlistId, trackId)
    }

    suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun trackExists(trackId: String): Boolean {
        return playlistDao.trackExists(trackId)
    }

    suspend fun getTrackFromApi(trackId: String): Track? {
        return try {
            deezerService.getTrack(trackId)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveTrack(track: Track) {
        playlistDao.insertTrack(track)
    }
} 