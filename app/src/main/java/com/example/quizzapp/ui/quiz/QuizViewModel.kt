package com.example.quizzapp.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizzapp.data.model.GameMode
import com.example.quizzapp.data.model.Quiz
import com.example.quizzapp.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _quizzes = MutableStateFlow<List<Quiz>>(emptyList())
    val quizzes: StateFlow<List<Quiz>> = _quizzes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadQuizzes()
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

    fun createQuiz(name: String, playlistId: Long, gameMode: GameMode, timeLimit: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                quizRepository.createQuiz(name, playlistId, gameMode, timeLimit)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteQuiz(quizId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val quiz = quizRepository.getQuizById(quizId)
                quiz?.let { quizRepository.deleteQuiz(it) }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
} 