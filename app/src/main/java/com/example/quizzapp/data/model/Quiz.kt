package com.example.quizzapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "quizzes",
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Quiz(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val playlistId: Long,
    val playlistName: String? = null,
    val gameMode: GameMode = GameMode.MULTIPLE_CHOICE,
    val timeLimit: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class GameMode {
    MULTIPLE_CHOICE,
    FILL_IN_BLANKS
} 