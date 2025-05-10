package com.example.quizzapp.ui.search

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
class SearchViewModel @Inject constructor(
    private val trackRepository: TrackRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            searchTracks(query)
        } else {
            _tracks.value = emptyList()
        }
    }

    private fun searchTracks(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                trackRepository.searchTracks(query).fold(
                    onSuccess = { tracks ->
                        _tracks.value = tracks
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Une erreur est survenue"
                        _tracks.value = emptyList()
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
} 