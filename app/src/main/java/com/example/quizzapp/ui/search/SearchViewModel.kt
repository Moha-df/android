package com.example.quizzapp.ui.search

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
class SearchViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val trackRepository: TrackRepository,
    private val sharedState: SharedState
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<Track>>(emptyList())
    val searchResults: StateFlow<List<Track>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Recherche initiale pour afficher les musiques populaires
        searchTracks("top hits")
    }

    fun onSearchQueryChange(query: String) {
        if (query.isNotBlank()) {
            searchTracks(query)
        } else {
            // Si la recherche est vide, on affiche à nouveau les musiques populaires
            searchTracks("top hits")
        }
    }

    fun searchTracks(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val result = trackRepository.searchTracks(query)
                result.onSuccess { tracks ->
                    _searchResults.value = tracks
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Une erreur est survenue lors de la recherche"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur est survenue lors de la recherche"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun playTrack(track: Track) {
        viewModelScope.launch {
            try {
                // Créer une playlist temporaire avec la piste sélectionnée
                val tempPlaylist = PlaylistWithTracks(
                    playlist = Playlist(
                        name = "Recherche",
                        description = "Playlist temporaire de recherche"
                    ),
                    tracks = listOf(track)
                )
                sharedState.playPlaylist(tempPlaylist, 0)
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur est survenue lors de la lecture"
            }
        }
    }
} 