package com.example.quizzapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.quizzapp.data.model.Playlist
import com.example.quizzapp.data.model.Quiz

@Database(
    entities = [Playlist::class, Quiz::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun quizDao(): QuizDao
} 