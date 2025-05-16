package com.example.quizzapp.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.quizzapp.R
import com.example.quizzapp.databinding.FragmentSearchBinding
import com.example.quizzapp.ui.dialog.AddToPlaylistDialog
import com.example.quizzapp.ui.track.TrackAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var trackAdapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchInput()
        observeViewModel()
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
                AddToPlaylistDialog.newInstance(track.id)
                    .show(childFragmentManager, "add_to_playlist")
            }
        )

        binding.searchResultsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = trackAdapter
        }
    }

    private fun setupSearchInput() {
        binding.searchEditText.doAfterTextChanged { text ->
            viewModel.onSearchQueryChange(text?.toString() ?: "")
        }

        binding.searchButton.setOnClickListener {
            viewModel.onSearchQueryChange(binding.searchEditText.text?.toString() ?: "")
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.searchResults.collect { tracks ->
                        trackAdapter.submitList(tracks)
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
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                                .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                .show()
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