package com.example.quizzapp.ui.playlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzapp.data.model.PlaylistWithTracks
import com.example.quizzapp.databinding.ItemPlaylistBinding

class PlaylistAdapter(
    private val onPlaylistClick: (PlaylistWithTracks) -> Unit,
    private val onPlaylistLongClick: (PlaylistWithTracks) -> Unit
) : ListAdapter<PlaylistWithTracks, PlaylistAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlaylistViewHolder(
        private val binding: ItemPlaylistBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onPlaylistClick(getItem(position))
                }
            }

            binding.root.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onPlaylistLongClick(getItem(position))
                    true
                } else {
                    false
                }
            }
        }

        fun bind(playlist: PlaylistWithTracks) {
            binding.playlistNameTextView.text = playlist.playlist.name
            binding.trackCountTextView.text = "${playlist.tracks.size} pistes"
        }
    }

    private class PlaylistDiffCallback : DiffUtil.ItemCallback<PlaylistWithTracks>() {
        override fun areItemsTheSame(oldItem: PlaylistWithTracks, newItem: PlaylistWithTracks): Boolean {
            return oldItem.playlist.id == newItem.playlist.id
        }

        override fun areContentsTheSame(oldItem: PlaylistWithTracks, newItem: PlaylistWithTracks): Boolean {
            return oldItem == newItem
        }
    }
} 