package com.example.quizzapp.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizzapp.data.model.Playlist
import com.example.quizzapp.data.model.PlaylistWithTracks
import com.example.quizzapp.data.model.Track
import com.example.quizzapp.data.repository.PlaylistRepository
import com.example.quizzapp.data.repository.TrackRepository
import com.example.quizzapp.ui.shared.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val trackRepository: TrackRepository,
    private val sharedState: SharedState
) : ViewModel() {
    private val _playlist = MutableStateFlow<PlaylistWithTracks?>(null)
    val playlist: StateFlow<PlaylistWithTracks?> = _playlist.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                playlistRepository.getAllPlaylistsWithTracks().collect { playlists ->
                    _playlist.value = playlists.find { it.playlist.id == playlistId }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur est survenue lors du chargement de la playlist"
                _isLoading.value = false
            }
        }
    }

    fun playTrack(track: Track) {
        viewModelScope.launch {
            try {
                // Obtenir une version fraîche de la piste
                val refreshedTrack = trackRepository.getRefreshedTrack(track).getOrThrow()
                
                // Utiliser la playlist actuelle
                _playlist.value?.let { currentPlaylist ->
                    // Trouver l'index de la piste dans la playlist
                    val trackIndex = currentPlaylist.tracks.indexOfFirst { it.id == track.id }
                    if (trackIndex != -1) {
                        // Mettre à jour la piste dans la playlist
                        val updatedTracks = currentPlaylist.tracks.toMutableList()
                        updatedTracks[trackIndex] = refreshedTrack
                        
                        // Créer une nouvelle playlist avec la piste mise à jour
                        val updatedPlaylist = currentPlaylist.copy(
                            tracks = updatedTracks
                        )
                        
                        // Jouer la playlist
                        sharedState.playPlaylist(updatedPlaylist, trackIndex)
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur est survenue lors de la lecture"
            }
        }
    }

    fun removeTrackFromPlaylist(trackId: String) {
        viewModelScope.launch {
            try {
                _playlist.value?.let { playlist ->
                    playlistRepository.removeTrackFromPlaylist(playlist.playlist.id, trackId)
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur est survenue lors de la suppression de la piste"
            }
        }
    }
} 