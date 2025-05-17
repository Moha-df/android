package com.example.quizzapp.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.quizzapp.data.model.Playlist
import com.example.quizzapp.databinding.DialogCreateQuizBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CreateQuizDialog : DialogFragment() {
    private var _binding: DialogCreateQuizBinding? = null
    private val binding get() = _binding!!

    private var onQuizCreated: ((String, Long) -> Unit)? = null
    private var playlists: List<Playlist> = emptyList()

    fun setOnQuizCreatedListener(listener: (String, Long) -> Unit) {
        onQuizCreated = listener
    }

    fun setPlaylists(playlists: List<Playlist>) {
        this.playlists = playlists
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playlistAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            playlists.map { it.name }
        )
        binding.spinnerPlaylist.adapter = playlistAdapter

        binding.buttonCreate.setOnClickListener {
            val quizName = binding.editTextQuizName.text.toString()
            if (quizName.isNotBlank() && binding.spinnerPlaylist.selectedItemPosition != -1) {
                val selectedPlaylist = playlists[binding.spinnerPlaylist.selectedItemPosition]
                onQuizCreated?.invoke(quizName, selectedPlaylist.id)
                dismiss()
            }
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CreateQuizDialog"
    }
} 