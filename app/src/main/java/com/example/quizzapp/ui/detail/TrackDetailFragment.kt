package com.example.quizzapp.ui.detail

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.quizzapp.databinding.FragmentTrackDetailBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.IOException

@AndroidEntryPoint
class TrackDetailFragment : Fragment() {
    private var _binding: FragmentTrackDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TrackDetailViewModel by viewModels()
    private val args: TrackDetailFragmentArgs by navArgs()
    
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        viewModel.loadTrack(args.trackId)
    }

    private fun setupUI() {
        binding.playPauseButton.setOnClickListener {
            if (isPlaying) {
                pauseTrack()
            } else {
                playTrack()
            }
        }
    }

    private fun playTrack() {
        val previewUrl = viewModel.track.value?.previewUrl
        if (previewUrl != null) {
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(previewUrl)
                    prepareAsync()
                    setOnPreparedListener {
                        start()
                        updatePlayingState(true)
                    }
                    setOnCompletionListener {
                        updatePlayingState(false)
                    }
                }
            } catch (e: IOException) {
                Snackbar.make(binding.root, "Impossible de lire le morceau", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun pauseTrack() {
        mediaPlayer?.pause()
        updatePlayingState(false)
    }
    
    private fun updatePlayingState(playing: Boolean) {
        isPlaying = playing
        binding.playPauseButton.text = if (playing) "Pause" else "Lecture"
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.track.collect { track ->
                        track?.let {
                            binding.titleTextView.text = it.title
                            binding.artistTextView.text = it.artist
                            
                            Glide.with(requireContext())
                                .load(it.albumCover)
                                .centerCrop()
                                .into(binding.albumCoverImageView)
                                
                            binding.playPauseButton.isEnabled = true
                        }
                    }
                }

                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    }
                }

                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
        _binding = null
    }
} 