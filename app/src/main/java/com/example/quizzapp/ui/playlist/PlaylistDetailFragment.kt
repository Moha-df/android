package com.example.quizzapp.ui.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizzapp.R
import com.example.quizzapp.databinding.FragmentPlaylistDetailBinding
import com.example.quizzapp.ui.track.TrackAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlaylistDetailFragment : Fragment() {
    private var _binding: FragmentPlaylistDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistDetailViewModel by viewModels()
    private val args: PlaylistDetailFragmentArgs by navArgs()
    private lateinit var trackAdapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        viewModel.loadPlaylist(args.playlistId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        trackAdapter = TrackAdapter(
            onTrackClick = { track ->
                viewModel.playTrack(track)
                Snackbar.make(
                    binding.root,
                    "▶️ ${track.title}",
                    Snackbar.LENGTH_SHORT
                ).setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                .show()
            },
            onLongClick = { track ->
                viewModel.removeTrackFromPlaylist(track.id)
                Snackbar.make(
                    binding.root,
                    "Piste supprimée de la playlist",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        )
        binding.tracksRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = trackAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.playlist.collect { playlistWithTracks ->
                        playlistWithTracks?.let {
                            binding.playlistNameTextView.text = it.playlist.name
                            trackAdapter.submitList(it.tracks)
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
        _binding = null
    }
} 