package com.example.quizzapp.data.local

import androidx.room.*
import com.example.quizzapp.data.model.Playlist
import com.example.quizzapp.data.model.PlaylistTrackCrossRef
import com.example.quizzapp.data.model.PlaylistWithTracks
import com.example.quizzapp.data.model.Track
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylist(playlistId: Long): Playlist?

    @Transaction
    @Query("SELECT * FROM playlists")
    fun getAllPlaylistsWithTracks(): Flow<List<PlaylistWithTracks>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistWithTracks(playlistId: Long): Flow<PlaylistWithTracks>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: Track)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrackCrossRef(crossRef: PlaylistTrackCrossRef)

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM tracks WHERE id = :trackId)")
    suspend fun trackExists(trackId: String): Boolean

    @Transaction
    suspend fun addTrackToPlaylist(playlistId: Long, trackId: String) {
        if (!trackExists(trackId)) {
            throw IllegalArgumentException("La piste n'existe pas dans la base de données")
        }
        val position = getPlaylistTrackCount(playlistId)
        val crossRef = PlaylistTrackCrossRef(playlistId, trackId, position)
        insertPlaylistTrackCrossRef(crossRef)
    }

    @Query("SELECT COUNT(*) FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    suspend fun getPlaylistTrackCount(playlistId: Long): Int
} 