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
class PlaylistDetailViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {
    private val _playlist = MutableStateFlow<Playlist?>(null)
    val playlist: StateFlow<Playlist?> = _playlist.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val playlist = playlistRepository.getPlaylistById(playlistId)
                _playlist.value = playlist
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur est survenue lors du chargement de la playlist"
            } finally {
                _isLoading.value = false
            }
        }
    }
} 