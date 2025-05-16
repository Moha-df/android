package com.example.quizzapp.ui.nowplaying

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.quizzapp.databinding.FragmentNowPlayingBinding
import com.example.quizzapp.ui.shared.SharedState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NowPlayingFragment : Fragment() {
    private var _binding: FragmentNowPlayingBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedState: SharedState

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNowPlayingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observePlaybackState()
    }

    private fun setupViews() {
        binding.playPauseButton.setOnClickListener {
            sharedState.togglePlayPause()
        }

        binding.skipNextButton.setOnClickListener {
            sharedState.skipToNext()
        }

        binding.skipPreviousButton.setOnClickListener {
            sharedState.skipToPrevious()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    sharedState.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }

    private fun observePlaybackState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedState.currentTrack.collect { track ->
                    track?.let {
                        binding.trackTitleTextView.text = it.title
                        binding.artistNameTextView.text = it.artist
                        Glide.with(this@NowPlayingFragment)
                            .load(it.albumCover)
                            .into(binding.albumCoverImageView)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedState.currentPlaylist.collect { playlist ->
                    if (playlist != null) {
                        binding.playlistNameTextView.text = playlist.playlist.name
                        binding.playlistNameTextView.visibility = View.VISIBLE
                        binding.skipNextButton.isEnabled = true
                        binding.skipPreviousButton.isEnabled = true
                    } else {
                        binding.playlistNameTextView.visibility = View.GONE
                        binding.skipNextButton.isEnabled = false
                        binding.skipPreviousButton.isEnabled = false
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedState.isPlaying.collect { isPlaying ->
                    binding.playPauseButton.setImageResource(
                        if (isPlaying) com.example.quizzapp.R.drawable.ic_pause
                        else com.example.quizzapp.R.drawable.ic_play
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedState.currentPosition.collect { position ->
                    binding.seekBar.progress = position.toInt()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedState.duration.collect { duration ->
                    binding.seekBar.max = duration.toInt()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 