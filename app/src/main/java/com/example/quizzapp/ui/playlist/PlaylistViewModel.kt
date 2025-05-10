package com.example.quizzapp.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizzapp.data.model.Playlist
import com.example.quizzapp.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                playlistRepository.getAllPlaylists().collect { playlists ->
                    _playlists.value = playlists
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur est survenue"
                _isLoading.value = false
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            try {
                playlistRepository.createPlaylist(name)
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur est survenue lors de la création de la playlist"
            }
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            try {
                playlistRepository.deletePlaylist(playlist)
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur est survenue lors de la suppression de la playlist"
            }
        }
    }
} 