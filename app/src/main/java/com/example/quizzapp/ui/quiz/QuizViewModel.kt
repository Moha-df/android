package com.example.quizzapp.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizzapp.data.model.GameMode
import com.example.quizzapp.data.model.Playlist
import com.example.quizzapp.data.model.Quiz
import com.example.quizzapp.data.repository.PlaylistRepository
import com.example.quizzapp.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _quizzes = MutableStateFlow<List<Quiz>>(emptyList())
    val quizzes: StateFlow<List<Quiz>> = _quizzes

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadQuizzes()
        loadPlaylists()
    }

    private fun loadQuizzes() {
        viewModelScope.launch {
            _isLoading.value = true
            quizRepository.getAllQuizzes()
                .catch { e ->
                    _error.value = e.message
                }
                .collect { quizzes ->
                    _quizzes.value = quizzes
                    _isLoading.value = false
                }
        }
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            playlistRepository.getAllPlaylists()
                .catch { e ->
                    _error.value = e.message
                }
                .collect { playlists ->
                    _playlists.value = playlists
                }
        }
    }

    fun createQuiz(name: String, playlistId: Long, gameMode: GameMode = GameMode.MULTIPLE_CHOICE, timeLimit: Int = 30) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val playlist = playlistRepository.getPlaylist(playlistId)
                if (playlist == null) {
                    _error.value = "Playlist non trouvée"
                    return@launch
                }
                
                val playlistWithTracks = playlistRepository.getPlaylistWithTracks(playlistId).first()
                if (playlistWithTracks?.tracks?.size ?: 0 < 4) {
                    _error.value = "La playlist doit contenir au moins 4 pistes pour créer un quiz"
                    return@launch
                }
                
                quizRepository.createQuiz(name, playlistId, gameMode, timeLimit, playlist.name)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteQuiz(quiz: Quiz) {
        viewModelScope.launch {
            quizRepository.deleteQuiz(quiz)
        }
    }

    fun clearError() {
        _error.value = null
    }
} 