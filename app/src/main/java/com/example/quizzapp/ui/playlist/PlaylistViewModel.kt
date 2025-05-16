package com.example.quizzapp.ui.playlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizzapp.data.model.Playlist
import com.example.quizzapp.data.model.PlaylistWithTracks
import com.example.quizzapp.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AddTrackState {
    object Idle : AddTrackState()
    object Loading : AddTrackState()
    object Success : AddTrackState()
    data class Error(val message: String) : AddTrackState()
}

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {
    private val _playlists = MutableStateFlow<List<PlaylistWithTracks>>(emptyList())
    val playlists: StateFlow<List<PlaylistWithTracks>> = _playlists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _addTrackState = MutableStateFlow<AddTrackState>(AddTrackState.Idle)
    val addTrackState: StateFlow<AddTrackState> = _addTrackState.asStateFlow()

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                playlistRepository.getAllPlaylistsWithTracks().collect { playlists ->
                    _playlists.value = playlists
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur est survenue"
                _isLoading.value = false
            }
        }
    }

    fun createPlaylist(name: String, description: String? = null) {
        viewModelScope.launch {
            try {
                val playlist = Playlist(
                    name = name,
                    description = description ?: ""
                )
                playlistRepository.createPlaylist(playlist)
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur est survenue lors de la création de la playlist"
            }
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            try {
                _addTrackState.value = AddTrackState.Loading
                Log.d("PlaylistViewModel", "Tentative d'ajout de la piste $trackId à la playlist $playlistId")
                
                // Vérifier d'abord si la piste existe déjà dans notre base de données
                if (!playlistRepository.trackExists(trackId)) {
                    Log.d("PlaylistViewModel", "La piste n'existe pas, tentative de récupération depuis l'API")
                    
                    // Si elle n'existe pas, essayer de la récupérer depuis l'API
                    val track = playlistRepository.getTrackFromApi(trackId)
                        ?: throw IllegalArgumentException("Impossible de récupérer la piste depuis l'API")
                    
                    // Sauvegarder la piste avant de créer la relation
                    Log.d("PlaylistViewModel", "Piste récupérée, sauvegarde dans la base de données")
                    playlistRepository.saveTrack(track)
                }
                
                // Maintenant que nous savons que la piste existe, ajouter la relation
                Log.d("PlaylistViewModel", "Ajout de la relation playlist-piste")
                playlistRepository.addTrackToPlaylist(playlistId, trackId)
                Log.d("PlaylistViewModel", "Piste ajoutée avec succès")
                
                _addTrackState.value = AddTrackState.Success
                
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Erreur lors de l'ajout de la piste", e)
                val errorMessage = e.message ?: "Une erreur est survenue lors de l'ajout de la piste à la playlist"
                _error.value = errorMessage
                _addTrackState.value = AddTrackState.Error(errorMessage)
            }
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            try {
                playlistRepository.removeTrackFromPlaylist(playlistId, trackId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur est survenue lors de la suppression de la piste"
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                playlistRepository.deletePlaylist(playlistId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur est survenue lors de la suppression de la playlist"
            }
        }
    }
}