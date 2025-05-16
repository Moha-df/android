package com.example.quizzapp.di

import com.example.quizzapp.data.local.PlaylistDao
import com.example.quizzapp.data.local.QuizDao
import com.example.quizzapp.data.remote.DeezerService
import com.example.quizzapp.data.repository.PlaylistRepository
import com.example.quizzapp.data.repository.QuizRepository
import com.example.quizzapp.data.repository.TrackRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideTrackRepository(deezerService: DeezerService): TrackRepository {
        return TrackRepository(deezerService)
    }

    @Provides
    @Singleton
    fun providePlaylistRepository(playlistDao: PlaylistDao, deezerService: DeezerService): PlaylistRepository {
        return PlaylistRepository(playlistDao, deezerService)
    }

    @Provides
    @Singleton
    fun provideQuizRepository(quizDao: QuizDao): QuizRepository {
        return QuizRepository(quizDao)
    }
} 