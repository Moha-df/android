package com.example.quizzapp.ui.nowplaying

import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizzapp.data.model.PlaylistWithTracks
import com.example.quizzapp.data.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor() : ViewModel() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentPlaylistWithTracks: PlaylistWithTracks? = null
    private var currentTrackIndex: Int = 0

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<PlaylistWithTracks?>(null)
    val currentPlaylist: StateFlow<PlaylistWithTracks?> = _currentPlaylist.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    fun playPlaylist(playlist: PlaylistWithTracks, startIndex: Int) {
        currentPlaylistWithTracks = playlist
        currentTrackIndex = startIndex
        _currentPlaylist.value = playlist
        playTrack(playlist.tracks[startIndex])
    }

    private fun playTrack(track: Track) {
        stopPlayback()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(track.previewUrl)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    _isPlaying.value = true
                    _currentTrack.value = track
                    _duration.value = duration.toLong()
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    skipToNext()
                }
                setOnSeekCompleteListener {
                    _currentPosition.value = currentPosition.toLong()
                }
            }
        } catch (e: IOException) {
            // Gérer l'erreur
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            } else {
                it.start()
                _isPlaying.value = true
            }
        }
    }

    fun skipToNext() {
        currentPlaylistWithTracks?.let { playlist ->
            if (currentTrackIndex < playlist.tracks.size - 1) {
                currentTrackIndex++
                playTrack(playlist.tracks[currentTrackIndex])
            }
        }
    }

    fun skipToPrevious() {
        currentPlaylistWithTracks?.let { playlist ->
            if (currentTrackIndex > 0) {
                currentTrackIndex--
                playTrack(playlist.tracks[currentTrackIndex])
            }
        }
    }

    fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position.toInt())
        _currentPosition.value = position
    }

    private fun stopPlayback() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        _isPlaying.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }
} 