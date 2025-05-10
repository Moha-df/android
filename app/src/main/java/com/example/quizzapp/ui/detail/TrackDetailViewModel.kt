package com.example.quizzapp.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizzapp.data.model.Track
import com.example.quizzapp.data.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackDetailViewModel @Inject constructor(
    private val trackRepository: TrackRepository
) : ViewModel() {
    private val _track = MutableStateFlow<Track?>(null)
    val track: StateFlow<Track?> = _track.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadTrack(trackId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = trackRepository.getTrack(trackId)
                result.onSuccess { track ->
                    _track.value = track
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Une erreur est survenue lors du chargement du morceau"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Une erreur est survenue lors du chargement du morceau"
            } finally {
                _isLoading.value = false
            }
        }
    }
} 