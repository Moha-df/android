package com.example.quizzapp.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizzapp.R
import com.example.quizzapp.data.model.GameMode
import com.example.quizzapp.data.model.Quiz
import com.example.quizzapp.databinding.FragmentQuizBinding
import com.example.quizzapp.ui.dialog.CreateQuizDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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
        observeErrors()
        setupNavigation()
    }

    private fun setupRecyclerView() {
        quizAdapter = QuizAdapter(
            onQuizClick = { quiz ->
                findNavController().navigate(
                    QuizFragmentDirections.actionQuizFragmentToPlayQuizFragment(quiz.id)
                )
            },
            onQuizLongClick = { quiz ->
                showDeleteConfirmationDialog(quiz)
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
        val dialog = CreateQuizDialog().apply {
            setPlaylists(viewModel.playlists.value)
            setOnQuizCreatedListener { name, playlistId ->
                viewModel.createQuiz(
                    name = name,
                    playlistId = playlistId,
                    gameMode = GameMode.MULTIPLE_CHOICE,
                    timeLimit = 30
                )
            }
        }
        dialog.show(childFragmentManager, CreateQuizDialog.TAG)
    }

    private fun showDeleteConfirmationDialog(quiz: Quiz) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Supprimer le quiz")
            .setMessage("Êtes-vous sûr de vouloir supprimer ce quiz ?")
            .setPositiveButton("Supprimer") { _, _ ->
                viewModel.deleteQuiz(quiz)
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

    private fun observeErrors() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { error ->
                    error?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun setupNavigation() {
        findNavController().addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.navigation_quiz) {
                viewModel.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 