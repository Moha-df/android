package com.example.quizzapp.data.repository

import com.example.quizzapp.data.local.PlaylistDao
import com.example.quizzapp.data.model.Playlist
import com.example.quizzapp.data.model.Track
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao
) {
    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    suspend fun getPlaylistById(id: Long): Playlist? = playlistDao.getPlaylistById(id)

    suspend fun createPlaylist(name: String): Long {
        val playlist = Playlist(name = name, tracks = emptyList())
        return playlistDao.insertPlaylist(playlist)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, track: Track) {
        val playlist = playlistDao.getPlaylistById(playlistId) ?: return
        val updatedTracks = playlist.tracks + track
        playlistDao.updatePlaylist(playlist.copy(tracks = updatedTracks))
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        val playlist = playlistDao.getPlaylistById(playlistId) ?: return
        val updatedTracks = playlist.tracks.filter { it.id != trackId }
        playlistDao.updatePlaylist(playlist.copy(tracks = updatedTracks))
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist)
    }
} 