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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizzapp.data.model.PlaylistWithTracks
import com.example.quizzapp.databinding.FragmentPlaylistBinding
import com.example.quizzapp.ui.dialog.CreatePlaylistDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlaylistFragment : Fragment() {
    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistViewModel by viewModels()
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observePlaylists()
    }

    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter(
            onPlaylistClick = { playlistWithTracks ->
                findNavController().navigate(
                    PlaylistFragmentDirections.actionPlaylistToPlaylistDetail(playlistWithTracks.playlist.id)
                )
            },
            onPlaylistLongClick = { playlistWithTracks ->
                showDeleteConfirmationDialog(playlistWithTracks)
            }
        )
        binding.playlistsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = playlistAdapter
        }
    }

    private fun setupClickListeners() {
        binding.createPlaylistFab.setOnClickListener {
            showCreatePlaylistDialog()
        }
    }

    private fun observePlaylists() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.playlists.collect { playlists ->
                    playlistAdapter.submitList(playlists)
                }
            }
        }
    }

    private fun showCreatePlaylistDialog() {
        CreatePlaylistDialog().show(parentFragmentManager, "create_playlist")
    }

    private fun showDeleteConfirmationDialog(playlistWithTracks: PlaylistWithTracks) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Supprimer la playlist")
            .setMessage("Êtes-vous sûr de vouloir supprimer la playlist \"${playlistWithTracks.playlist.name}\" ?")
            .setPositiveButton("Oui") { _, _ ->
                viewModel.deletePlaylist(playlistWithTracks.playlist.id)
                Snackbar.make(
                    binding.root,
                    "Playlist supprimée",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Non", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 