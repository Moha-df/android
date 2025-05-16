package com.example.quizzapp.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val albumCover: String,
    val previewUrl: String
) : Parcelable 