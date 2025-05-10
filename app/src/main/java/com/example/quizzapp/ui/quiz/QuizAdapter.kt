package com.example.quizzapp.ui.quiz

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzapp.data.model.GameMode
import com.example.quizzapp.data.model.Quiz
import com.example.quizzapp.databinding.ItemQuizBinding

class QuizAdapter(
    private val onQuizClick: (Quiz) -> Unit
) : ListAdapter<Quiz, QuizAdapter.QuizViewHolder>(QuizDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val binding = ItemQuizBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuizViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class QuizViewHolder(
        private val binding: ItemQuizBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onQuizClick(getItem(position))
                }
            }
        }

        fun bind(quiz: Quiz) {
            binding.quizNameTextView.text = quiz.name
            binding.quizModeTextView.text = when (quiz.gameMode) {
                GameMode.MULTIPLE_CHOICE -> "Mode: Choix multiples"
                GameMode.FILL_IN_BLANKS -> "Mode: Lettres manquantes"
            }
            binding.quizTimeLimitTextView.text = quiz.timeLimit?.let { "Temps: ${it}s" } ?: "Temps: illimité"
        }
    }

    private class QuizDiffCallback : DiffUtil.ItemCallback<Quiz>() {
        override fun areItemsTheSame(oldItem: Quiz, newItem: Quiz): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Quiz, newItem: Quiz): Boolean {
            return oldItem == newItem
        }
    }
} 