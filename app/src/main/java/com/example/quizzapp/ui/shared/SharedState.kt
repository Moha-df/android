package com.example.quizzapp.ui.shared

import android.media.MediaPlayer
import android.util.Log
import com.example.quizzapp.data.model.PlaylistWithTracks
import com.example.quizzapp.data.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedState @Inject constructor() {
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlaylistWithTracks: PlaylistWithTracks? = null
    private var currentTrackIndex: Int = 0
    private var timer: Timer? = null
    private var prepareTimer: Timer? = null
    private var retryCount = 0
    private val MAX_RETRIES = 3

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

    fun playSingleTrack(track: Track) {
        Log.d("SharedState", "playSingleTrack: ${track.title}")
        currentPlaylistWithTracks = null
        currentTrackIndex = 0
        _currentPlaylist.value = null
        _currentTrack.value = track
        playTrack(track)
    }

    fun playTrack(track: Track) {
        Log.d("SharedState", "playTrack: ${track.title}")
        stopPlayback()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(track.previewUrl)
                Log.d("SharedState", "setDataSource: ${track.previewUrl}")
                
                setOnPreparedListener {
                    Log.d("SharedState", "onPrepared")
                    cancelPrepareTimer()
                    start()
                    _isPlaying.value = true
                    _currentTrack.value = track
                    _duration.value = duration.toLong()
                    _currentPosition.value = 0L
                    startPositionUpdates()
                    retryCount = 0
                }
                
                setOnCompletionListener {
                    Log.d("SharedState", "onCompletion")
                    _isPlaying.value = false
                    stopPositionUpdates()
                    if (currentPlaylistWithTracks != null) {
                        skipToNext()
                    }
                }
                
                setOnSeekCompleteListener {
                    Log.d("SharedState", "onSeekComplete")
                    _currentPosition.value = currentPosition.toLong()
                }
                
                setOnErrorListener { mp, what, extra ->
                    Log.e("SharedState", "onError: what=$what, extra=$extra")
                    cancelPrepareTimer()
                    _isPlaying.value = false
                    stopPositionUpdates()
                    
                    if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN && extra == 403) {
                        if (retryCount < MAX_RETRIES) {
                            retryCount++
                            Log.d("SharedState", "Tentative de lecture $retryCount/$MAX_RETRIES")
                            return@setOnErrorListener true
                        }
                    }
                    true
                }

                prepare()
                start()
                _isPlaying.value = true
                _currentTrack.value = track
                _duration.value = duration.toLong()
                _currentPosition.value = 0L
                startPositionUpdates()
            }
        } catch (e: IOException) {
            Log.e("SharedState", "Error playing track", e)
            cancelPrepareTimer()
            _isPlaying.value = false
            stopPositionUpdates()
            
            if (e.message?.contains("403") == true || e.message?.contains("status=0x1") == true) {
                if (retryCount < MAX_RETRIES) {
                    retryCount++
                    Log.d("SharedState", "Tentative de lecture $retryCount/$MAX_RETRIES")
                }
            }
        }
    }

    private fun startPrepareTimer() {
        cancelPrepareTimer()
        prepareTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    Log.e("SharedState", "Prepare timeout")
                    cancelPrepareTimer()
                    stopPlayback()
                    _isPlaying.value = false
                }
            }, 15000)
        }
    }

    private fun cancelPrepareTimer() {
        prepareTimer?.cancel()
        prepareTimer = null
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    mediaPlayer?.let {
                        if (it.isPlaying) {
                            _currentPosition.value = it.currentPosition.toLong()
                        }
                    }
                }
            }, 0, 1000)
        }
    }

    private fun stopPositionUpdates() {
        timer?.cancel()
        timer = null
    }

    fun togglePlayPause() {
        Log.d("SharedState", "togglePlayPause: current isPlaying=${_isPlaying.value}")
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
                stopPositionUpdates()
            } else {
                it.start()
                _isPlaying.value = true
                startPositionUpdates()
            }
        } ?: run {
            _currentTrack.value?.let { track ->
                playTrack(track)
            }
        }
    }

    fun skipToNext() {
        currentPlaylistWithTracks?.let { playlist ->
            if (currentTrackIndex < playlist.tracks.size - 1) {
                currentTrackIndex++
            } else {
                currentTrackIndex = 0
            }
            playTrack(playlist.tracks[currentTrackIndex])
        }
    }

    fun skipToPrevious() {
        currentPlaylistWithTracks?.let { playlist ->
            if (currentTrackIndex > 0) {
                currentTrackIndex--
            } else {
                currentTrackIndex = playlist.tracks.size - 1
            }
            playTrack(playlist.tracks[currentTrackIndex])
        }
    }

    fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position.toInt())
        _currentPosition.value = position
    }

    private fun stopPlayback() {
        Log.d("SharedState", "stopPlayback")
        cancelPrepareTimer()
        stopPositionUpdates()
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        _isPlaying.value = false
        _currentPosition.value = 0L
    }

    fun release() {
        stopPlayback()
    }
} 