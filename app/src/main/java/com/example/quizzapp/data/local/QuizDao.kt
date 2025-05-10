package com.example.quizzapp.data.local

import androidx.room.*
import com.example.quizzapp.data.model.Quiz
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes")
    fun getAllQuizzes(): Flow<List<Quiz>>

    @Query("SELECT * FROM quizzes WHERE id = :id")
    suspend fun getQuizById(id: Long): Quiz?

    @Query("SELECT * FROM quizzes WHERE playlistId = :playlistId")
    fun getQuizzesByPlaylistId(playlistId: Long): Flow<List<Quiz>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: Quiz): Long

    @Update
    suspend fun updateQuiz(quiz: Quiz)

    @Delete
    suspend fun deleteQuiz(quiz: Quiz)
} 