package com.example.quizzapp.data.repository

import com.example.quizzapp.data.local.QuizDao
import com.example.quizzapp.data.model.GameMode
import com.example.quizzapp.data.model.Quiz
import kotlinx.coroutines.flow.Flow

class QuizRepository(private val quizDao: QuizDao) {
    fun getAllQuizzes(): Flow<List<Quiz>> = quizDao.getAllQuizzes()

    fun getQuizzesByPlaylistId(playlistId: Long): Flow<List<Quiz>> =
        quizDao.getQuizzesByPlaylistId(playlistId)

    suspend fun getQuizById(id: Long): Quiz? = quizDao.getQuizById(id)

    suspend fun createQuiz(
        name: String,
        playlistId: Long,
        gameMode: GameMode = GameMode.MULTIPLE_CHOICE,
        timeLimit: Int? = null
    ): Long {
        val quiz = Quiz(
            name = name,
            playlistId = playlistId,
            gameMode = gameMode,
            timeLimit = timeLimit
        )
        return quizDao.insertQuiz(quiz)
    }

    suspend fun updateQuiz(quiz: Quiz) {
        quizDao.updateQuiz(quiz)
    }

    suspend fun deleteQuiz(quiz: Quiz) {
        quizDao.deleteQuiz(quiz)
    }
} 