package com.example.quizzapp.ui.quiz

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
import com.example.quizzapp.data.model.GameMode
import com.example.quizzapp.databinding.FragmentQuizBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuizFragment : Fragment() {
    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuizViewModel by viewModels()
    private lateinit var quizAdapter: QuizAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        observeQuizzes()
    }

    private fun setupRecyclerView() {
        quizAdapter = QuizAdapter(
            onQuizClick = { quiz ->
                // TODO: Implémenter la navigation vers le détail du quiz
            }
        )
        binding.quizzesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = quizAdapter
        }
    }

    private fun setupFab() {
        binding.createQuizFab.setOnClickListener {
            showCreateQuizDialog()
        }
    }

    private fun showCreateQuizDialog() {
        val nameEditText = TextInputEditText(requireContext()).apply {
            hint = "Nom du quiz"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Créer un quiz")
            .setView(nameEditText)
            .setPositiveButton("Créer") { _, _ ->
                val name = nameEditText.text?.toString()
                if (!name.isNullOrBlank()) {
                    // TODO: Afficher un dialogue pour choisir la playlist et le mode de jeu
                    viewModel.createQuiz(
                        name = name,
                        playlistId = 1L, // TODO: Récupérer l'ID de la playlist sélectionnée
                        gameMode = GameMode.MULTIPLE_CHOICE,
                        timeLimit = 30
                    )
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun observeQuizzes() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.quizzes.collect { quizzes ->
                    quizAdapter.submitList(quizzes)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 