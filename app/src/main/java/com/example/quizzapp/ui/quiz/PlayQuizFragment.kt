package com.example.quizzapp.ui.quiz

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.quizzapp.R
import com.example.quizzapp.databinding.FragmentPlayQuizBinding
import com.example.quizzapp.data.model.Track
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlayQuizFragment : Fragment() {
    private var _binding: FragmentPlayQuizBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayQuizViewModel by viewModels()
    private val args: PlayQuizFragmentArgs by navArgs()
    private var player: ExoPlayer? = null
    private var retryCount = 0
    private val MAX_RETRIES = 2
    private var isInitialized = false
    private var pendingNavigationId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Gérer le bouton retour
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitConfirmationDialog()
                }
            }
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isInitialized", isInitialized)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stopCurrentMusic()
        
        // Restaurer l'état si nécessaire
        if (savedInstanceState != null) {
            isInitialized = savedInstanceState.getBoolean("isInitialized", false)
        }

        // Initialiser le quiz seulement si ce n'est pas déjà fait
        if (!isInitialized) {
            viewModel.initQuiz(args.quizId)
            isInitialized = true
        }
        
        setupViews()
        observeState()
        observeNavigation()
        updateBottomNavigation()
    }

    private fun observeNavigation() {
        // Observer les changements de navigation
        findNavController().addOnDestinationChangedListener { _, destination, _ ->
            // Si on quitte le quiz pour aller vers un autre fragment
            if (destination.id != R.id.playQuizFragment) {
                stopCurrentMusic()
                // Ne pas nettoyer l'état du quiz si on revient juste après
                if (destination.id != R.id.navigation_quiz) {
                    viewModel.clearState()
                    isInitialized = false
                }
            }
        }
    }

    private fun stopCurrentMusic() {
        try {
            player?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            player = null
            println("Musique arrêtée et lecteur libéré")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Erreur lors de l'arrêt de la musique: ${e.message}")
        }
    }

    private fun setupViews() {
        binding.playButton.setOnClickListener {
            if (player?.isPlaying == true) {
                player?.pause()
                binding.playButton.setImageResource(R.drawable.ic_play)
            } else {
                viewModel.currentTrack.value?.previewUrl?.let { url ->
                    if (url.isNotEmpty()) {
                        playPreview(url)
                        binding.playButton.setImageResource(R.drawable.ic_pause)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Aucun extrait disponible pour cette chanson",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        binding.exitButton.setOnClickListener {
            showExitConfirmationDialog()
        }

        val answerCards = listOf(
            binding.answer1Card,
            binding.answer2Card,
            binding.answer3Card,
            binding.answer4Card
        )

        answerCards.forEachIndexed { index, card ->
            card.setOnClickListener {
                stopCurrentMusic()
                binding.playButton.setImageResource(R.drawable.ic_play)
                viewModel.checkAnswer(index)
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.score.collect { score ->
                        updateScoreDisplay(score)
                    }
                }

                launch {
                    viewModel.totalQuestions.collect { total ->
                        updateScoreDisplay(viewModel.score.value)
                    }
                }

                launch {
                    viewModel.currentQuestionNumber.collect { questionNumber ->
                        val totalQuestions = viewModel.totalQuestions.value
                        if (totalQuestions > 0) {
                            binding.questionTextView.text = "Question $questionNumber/$totalQuestions"
                        }
                    }
                }

                launch {
                    viewModel.currentPreviewUrl.collect { url ->
                        url?.let {
                            if (player?.isPlaying == true) {
                                playPreview(it)
                            }
                        }
                    }
                }

                launch {
                    viewModel.playlistName.collect { name ->
                        name?.let {
                            binding.playlistNameTextView.text = it
                        }
                    }
                }

                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                            findNavController().popBackStack()
                        }
                    }
                }

                launch {
                    viewModel.currentAnswers.collect { answers ->
                        answers?.let { 
                            if (it.isNotEmpty()) {
                                updateAnswers(it)
                            }
                        }
                    }
                }

                launch {
                    viewModel.answerResult.collect { result ->
                        result?.let { handleAnswerResult(it) }
                    }
                }

                launch {
                    viewModel.isGameFinished.collect { isFinished ->
                        if (isFinished) {
                            stopCurrentMusic()
                            binding.playButton.setImageResource(R.drawable.ic_play)
                            showGameFinishedDialog()
                        }
                    }
                }
            }
        }
    }

    private fun updateScoreDisplay(score: Int) {
        val totalQuestions = viewModel.totalQuestions.value
        if (totalQuestions > 0) {
            binding.scoreTextView.text = "Score: $score/$totalQuestions"
        }
    }

    private fun updateAnswers(answers: List<Track>) {
        val answerTexts = listOf(
            binding.answer1TextView,
            binding.answer2TextView,
            binding.answer3TextView,
            binding.answer4TextView
        )

        val answerCards = listOf(
            binding.answer1Card,
            binding.answer2Card,
            binding.answer3Card,
            binding.answer4Card
        )

        // Réinitialiser l'état de toutes les cartes
        answerCards.forEach { card ->
            card.setCardBackgroundColor(Color.WHITE)
            card.isClickable = true
            card.isEnabled = true
            (card.getChildAt(0) as? android.widget.TextView)?.setTextColor(Color.BLACK)
        }

        // Mettre à jour les textes des réponses
        answers.forEachIndexed { index, track ->
            answerTexts[index].text = buildString {
                append(track.title)
                append("\n")
                append(track.artist)
            }
        }
    }

    private fun handleAnswerResult(result: Pair<Int, Boolean>) {
        val (index, isCorrect) = result
        val answerCards = listOf(
            binding.answer1Card,
            binding.answer2Card,
            binding.answer3Card,
            binding.answer4Card
        )

        // Désactiver toutes les cartes
        answerCards.forEach { card ->
            card.isClickable = false
            card.isEnabled = false
        }

        // Mettre en évidence la réponse sélectionnée
        val selectedCard = answerCards[index]
        val color = if (isCorrect) {
            ContextCompat.getColor(requireContext(), R.color.correct_answer)
        } else {
            ContextCompat.getColor(requireContext(), R.color.wrong_answer)
        }
        selectedCard.setCardBackgroundColor(color)
        (selectedCard.getChildAt(0) as? android.widget.TextView)?.setTextColor(Color.WHITE)

        // Mettre en évidence la bonne réponse si la réponse sélectionnée est incorrecte
        if (!isCorrect) {
            viewModel.currentAnswers.value?.let { answers ->
                val correctIndex = answers.indexOfFirst { it.id == viewModel.currentTrack.value?.id }
                if (correctIndex != -1) {
                    val correctCard = answerCards[correctIndex]
                    correctCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.correct_answer))
                    (correctCard.getChildAt(0) as? android.widget.TextView)?.setTextColor(Color.WHITE)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            delay(1500) // Augmenter le délai pour mieux voir la réponse
            viewModel.nextQuestion()
        }
    }

    private fun playPreview(url: String) {
        try {
            if (url.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Aucun extrait disponible pour cette chanson",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            stopCurrentMusic()
            println("Tentative de lecture de l'extrait: $url")
            
            player = ExoPlayer.Builder(requireContext()).build().apply {
                val mediaItem = MediaItem.fromUri(url)
                setMediaItem(mediaItem)
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        println("Erreur de lecture: ${error.message}")
                        
                        // Vérifier si c'est une erreur 403 (URL expirée)
                        if (error.cause is androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException) {
                            val httpError = error.cause as androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException
                            if (httpError.responseCode == 403) {
                                println("URL expirée, demande d'un nouvel extrait")
                                viewModel.refreshPreviewUrl()
                                // Attendre un court instant avant de réessayer avec la nouvelle URL
                                viewLifecycleOwner.lifecycleScope.launch {
                                    delay(1000) // Attendre 1 seconde
                                    viewModel.currentPreviewUrl.value?.let { newUrl ->
                                        if (newUrl.isNotBlank()) {
                                            println("Tentative de lecture avec la nouvelle URL: $newUrl")
                                            playPreview(newUrl)
                                        }
                                    }
                                }
                                return
                            }
                        }
                        
                        if (retryCount < MAX_RETRIES) {
                            retryCount++
                            println("Tentative de reconnexion $retryCount/$MAX_RETRIES")
                            viewLifecycleOwner.lifecycleScope.launch {
                                delay(1000)
                                playPreview(url)
                            }
                        } else {
                            retryCount = 0
                            Toast.makeText(
                                requireContext(),
                                "Impossible de lire l'extrait musical. Veuillez réessayer plus tard.",
                                Toast.LENGTH_SHORT
                            ).show()
                            stopCurrentMusic()
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                println("Lecteur prêt à jouer")
                                retryCount = 0
                                play() // S'assurer que la lecture démarre
                            }
                            Player.STATE_BUFFERING -> {
                                println("Mise en mémoire tampon...")
                            }
                            Player.STATE_ENDED -> {
                                println("Lecture terminée")
                                stopCurrentMusic()
                            }
                            Player.STATE_IDLE -> {
                                println("Lecteur inactif")
                            }
                        }
                    }
                })
                prepare()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Exception lors de la lecture: ${e.message}")
            stopCurrentMusic()
            Toast.makeText(
                requireContext(),
                "Erreur lors de la lecture de l'extrait. Veuillez réessayer.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateBottomNavigation() {
        // Trouver la BottomNavigationView dans l'activité
        val activity = requireActivity()
        val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        
        // Mettre à jour l'état de la barre de navigation
        bottomNav?.let {
            // Désactiver la navigation pendant le quiz
            it.menu.findItem(R.id.navigation_quiz).isEnabled = false
            it.menu.findItem(R.id.navigation_playlist).isEnabled = false
            it.menu.findItem(R.id.navigation_search).isEnabled = false
            it.menu.findItem(R.id.navigation_now_playing).isEnabled = false
        }
    }

    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Quitter le quiz")
            .setMessage("Êtes-vous sûr de vouloir quitter le quiz ? Votre progression sera perdue.")
            .setPositiveButton("Quitter") { _, _ ->
                stopCurrentMusic()
                viewModel.clearState()
                isInitialized = false
                // Réactiver la navigation avant de quitter
                val activity = requireActivity()
                val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation)
                bottomNav?.let {
                    it.menu.findItem(R.id.navigation_quiz).isEnabled = true
                    it.menu.findItem(R.id.navigation_playlist).isEnabled = true
                    it.menu.findItem(R.id.navigation_search).isEnabled = true
                    it.menu.findItem(R.id.navigation_now_playing).isEnabled = true
                }
                findNavController().popBackStack()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showGameFinishedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Quiz terminé !")
            .setMessage("Votre score final est : ${viewModel.score.value}/${viewModel.totalQuestions.value}")
            .setPositiveButton("OK") { _, _ ->
                findNavController().popBackStack()
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopCurrentMusic()
        retryCount = 0
        
        // Réactiver la navigation
        val activity = requireActivity()
        val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.let {
            it.menu.findItem(R.id.navigation_quiz).isEnabled = true
            it.menu.findItem(R.id.navigation_playlist).isEnabled = true
            it.menu.findItem(R.id.navigation_search).isEnabled = true
            it.menu.findItem(R.id.navigation_now_playing).isEnabled = true
        }
        
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        isInitialized = false
    }
}