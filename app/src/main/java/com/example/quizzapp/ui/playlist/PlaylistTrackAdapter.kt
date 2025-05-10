package com.example.quizzapp.ui.playlist

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.quizzapp.data.model.Track
import com.example.quizzapp.databinding.ItemPlaylistTrackBinding
import java.io.IOException

class PlaylistTrackAdapter(
    private val onTrackClick: (Track) -> Unit
) : ListAdapter<Track, PlaylistTrackAdapter.TrackViewHolder>(TrackDiffCallback()) {

    private var mediaPlayer: MediaPlayer? = null
    private var currentlyPlayingTrackId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = ItemPlaylistTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TrackViewHolder(
        private val binding: ItemPlaylistTrackBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTrackClick(getItem(position))
                }
            }

            binding.playButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val track = getItem(position)
                    if (currentlyPlayingTrackId == track.id) {
                        // Si le morceau est déjà en cours de lecture, on l'arrête
                        stopPlayback()
                    } else {
                        // Sinon, on joue le nouveau morceau
                        playTrack(track)
                    }
                }
            }
        }

        fun bind(track: Track) {
            binding.titleTextView.text = track.title
            binding.artistTextView.text = track.artist

            // Mettre à jour l'icône du bouton de lecture
            if (currentlyPlayingTrackId == track.id) {
                binding.playButton.setImageResource(android.R.drawable.ic_media_pause)
            } else {
                binding.playButton.setImageResource(android.R.drawable.ic_media_play)
            }

            Glide.with(binding.root)
                .load(track.albumCover)
                .centerCrop()
                .into(binding.albumCoverImageView)
        }
    }

    private fun playTrack(track: Track) {
        // Arrêter la lecture en cours si elle existe
        stopPlayback()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(track.previewUrl)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    currentlyPlayingTrackId = track.id
                    notifyDataSetChanged() // Pour mettre à jour les icônes
                }
                setOnCompletionListener {
                    currentlyPlayingTrackId = null
                    notifyDataSetChanged() // Pour mettre à jour les icônes
                }
            }
        } catch (e: IOException) {
            // Gérer l'erreur
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        currentlyPlayingTrackId = null
        notifyDataSetChanged() // Pour mettre à jour les icônes
    }

    fun release() {
        stopPlayback()
    }

    private class TrackDiffCallback : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem == newItem
        }
    }
} 