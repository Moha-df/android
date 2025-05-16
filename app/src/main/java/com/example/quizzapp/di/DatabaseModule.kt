package com.example.quizzapp.di

import android.content.Context
import androidx.room.Room
import com.example.quizzapp.data.local.AppDatabase
import com.example.quizzapp.data.local.MIGRATION_1_2
import com.example.quizzapp.data.local.PlaylistDao
import com.example.quizzapp.data.local.QuizDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "musical_quiz_database"
        )
        .addMigrations(MIGRATION_1_2)
        .build()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(database: AppDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    @Singleton
    fun provideQuizDao(database: AppDatabase): QuizDao {
        return database.quizDao()
    }
} 