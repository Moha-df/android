package com.example.quizzapp.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizzapp.R
import com.example.quizzapp.databinding.DialogAddToPlaylistBinding
import com.example.quizzapp.ui.playlist.AddTrackState
import com.example.quizzapp.ui.playlist.PlaylistAdapter
import com.example.quizzapp.ui.playlist.PlaylistViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddToPlaylistDialog : DialogFragment() {
    private var _binding: DialogAddToPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistViewModel by viewModels()
    private lateinit var adapter: PlaylistAdapter
    private var trackId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_QuizzApp_Dialog)
        trackId = arguments?.getString(ARG_TRACK_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddToPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observePlaylists()
        observeErrors()
        observeAddTrackState()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawableResource(android.R.color.white)
        }
    }

    private fun setupRecyclerView() {
        adapter = PlaylistAdapter(
            onPlaylistClick = { playlistWithTracks ->
                trackId?.let { trackId ->
                    android.util.Log.d("AddToPlaylistDialog", "Clic sur la playlist ${playlistWithTracks.playlist.id} pour ajouter la piste $trackId")
                    viewModel.addTrackToPlaylist(playlistWithTracks.playlist.id, trackId)
                }
            },
            onPlaylistLongClick = { /* Ne rien faire pour le clic long dans cette vue */ }
        )
        binding.playlistsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AddToPlaylistDialog.adapter
        }
    }

    private fun observePlaylists() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.playlists.collect { playlists ->
                    adapter.submitList(playlists)
                    binding.emptyStateTextView.visibility = if (playlists.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun observeErrors() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { error ->
                    error?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun observeAddTrackState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addTrackState.collect { state ->
                    when (state) {
                        is AddTrackState.Success -> {
                            dismiss()
                        }
                        is AddTrackState.Error -> {
                            // L'erreur est déjà gérée par observeErrors()
                        }
                        else -> {
                            // Ignorer les autres états
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

    companion object {
        private const val ARG_TRACK_ID = "track_id"

        fun newInstance(trackId: String): AddToPlaylistDialog {
            return AddToPlaylistDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_TRACK_ID, trackId)
                }
            }
        }
    }
} 