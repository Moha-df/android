package com.example.quizzapp.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizzapp.data.model.PlaylistWithTracks
import com.example.quizzapp.data.model.Track
import com.example.quizzapp.data.repository.PlaylistRepository
import com.example.quizzapp.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayQuizViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private val _currentQuestionNumber = MutableStateFlow(0)
    val currentQuestionNumber: StateFlow<Int> = _currentQuestionNumber

    private val _totalQuestions = MutableStateFlow(0)
    val totalQuestions: StateFlow<Int> = _totalQuestions

    private val _currentAnswers = MutableStateFlow<List<Track>?>(null)
    val currentAnswers: StateFlow<List<Track>?> = _currentAnswers

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack

    private val _answerResult = MutableStateFlow<Pair<Int, Boolean>?>(null)
    val answerResult: StateFlow<Pair<Int, Boolean>?> = _answerResult

    private val _isGameFinished = MutableStateFlow(false)
    val isGameFinished: StateFlow<Boolean> = _isGameFinished

    private val _playlistName = MutableStateFlow<String?>(null)
    val playlistName: StateFlow<String?> = _playlistName

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _currentPreviewUrl = MutableStateFlow<String?>(null)
    val currentPreviewUrl: StateFlow<String?> = _currentPreviewUrl

    private var currentQuestionIndex = 0
    private var questions = listOf<Track>()
    private var correctAnswers = listOf<Int>()
    private var currentQuizId: Long? = null

    fun initQuiz(quizId: Long) {
        // Ne pas réinitialiser si c'est le même quiz et que nous avons déjà des questions
        if (currentQuizId == quizId && questions.isNotEmpty()) {
            return
        }
        
        currentQuizId = quizId
        viewModelScope.launch {
            try {
                val quiz = quizRepository.getQuizById(quizId)
                if (quiz == null) {
                    _error.value = "Quiz non trouvé"
                    return@launch
                }

                val playlistWithTracks = playlistRepository.getPlaylistWithTracks(quiz.playlistId).first()
                playlistWithTracks?.let { playlist ->
                    if (playlist.tracks.size < 4) {
                        _error.value = "La playlist doit contenir au moins 4 pistes pour créer un quiz"
                        return@launch
                    }
                    
                    // Stocker le nom de la playlist
                    _playlistName.value = playlist.playlist.name
                    
                    // Initialiser le nombre total de questions en premier
                    _totalQuestions.value = playlist.tracks.size
                    
                    // Mélanger les pistes seulement si c'est une nouvelle initialisation
                    if (questions.isEmpty()) {
                        questions = playlist.tracks.shuffled()
                        currentQuestionIndex = 0
                        _score.value = 0
                        _currentQuestionNumber.value = 1
                    }
                    
                    // Générer les réponses pour la question actuelle
                    generateAnswers()
                } ?: run {
                    _error.value = "Playlist non trouvée"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Une erreur est survenue lors de l'initialisation du quiz: ${e.message}"
            }
        }
    }

    private fun generateAnswers() {
        if (currentQuestionIndex >= questions.size) {
            _isGameFinished.value = true
            return
        }

        val currentTrack = questions[currentQuestionIndex]
        _currentTrack.value = currentTrack
        _currentPreviewUrl.value = currentTrack.previewUrl

        // Vérifier si l'URL de l'extrait est valide
        if (currentTrack.previewUrl.isBlank()) {
            println("Attention: Pas d'URL d'extrait pour la piste: ${currentTrack.title}")
        }

        // Créer une liste de 4 réponses possibles
        val allTracks = questions.toMutableList()
        allTracks.remove(currentTrack) // Retirer la piste actuelle pour éviter les doublons

        // Mélanger les autres pistes et en prendre 3
        val otherTracks = allTracks.shuffled().take(3)

        // Créer la liste finale des réponses avec la bonne réponse à une position aléatoire
        val answers = (otherTracks + currentTrack).shuffled()
        _currentAnswers.value = answers

        // Stocker l'index de la bonne réponse
        correctAnswers = answers.mapIndexed { index, track ->
            if (track.id == currentTrack.id) index else -1
        }.filter { it != -1 }

        // S'assurer que les réponses sont bien mises à jour
        if (_currentAnswers.value.isNullOrEmpty()) {
            _error.value = "Erreur lors de la génération des réponses"
        }
    }

    fun checkAnswer(selectedIndex: Int) {
        val isCorrect = correctAnswers.contains(selectedIndex)
        _answerResult.value = Pair(selectedIndex, isCorrect)
        if (isCorrect) {
            val newScore = _score.value + 1
            _score.value = newScore
            println("Score mis à jour: $newScore")
        }
        println("Réponse vérifiée - Index: $selectedIndex, Correct: $isCorrect, Score actuel: ${_score.value}")
    }

    fun nextQuestion() {
        _answerResult.value = null
        if (currentQuestionIndex + 1 < questions.size) {
            currentQuestionIndex++
            _currentQuestionNumber.value = currentQuestionIndex + 1
            generateAnswers()
            println("Question suivante - Numéro: ${_currentQuestionNumber.value}, Score: ${_score.value}")
        } else {
            _isGameFinished.value = true
            println("Quiz terminé - Score final: ${_score.value}")
        }
    }

    fun clearState() {
        _score.value = 0
        _currentQuestionNumber.value = 0
        _totalQuestions.value = 0
        _currentAnswers.value = null
        _currentTrack.value = null
        _answerResult.value = null
        _isGameFinished.value = false
        _playlistName.value = null
        _error.value = null
        _currentPreviewUrl.value = null
        currentQuestionIndex = 0
        questions = emptyList()
        correctAnswers = emptyList()
        currentQuizId = null
    }

    fun refreshPreviewUrl() {
        viewModelScope.launch {
            try {
                val currentTrack = _currentTrack.value ?: return@launch
                // Demander un nouvel extrait via le repository
                val newUrl = playlistRepository.getTrackFromApi(currentTrack.id)?.previewUrl
                if (newUrl != null) {
                    _currentPreviewUrl.value = newUrl
                    // Ne pas mettre à jour la piste dans la base de données
                    // On garde juste l'URL pour la lecture
                }
            } catch (e: Exception) {
                println("Erreur lors du rafraîchissement de l'URL: ${e.message}")
            }
        }
    }
} 