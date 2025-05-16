package com.example.quizzapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.quizzapp.data.model.Playlist
import com.example.quizzapp.data.model.PlaylistTrackCrossRef
import com.example.quizzapp.data.model.Quiz
import com.example.quizzapp.data.model.Track

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Vérifier si les tables existent avant de les sauvegarder
        val cursor = database.query("SELECT name FROM sqlite_master WHERE type='table'")
        val existingTables = mutableSetOf<String>()
        while (cursor.moveToNext()) {
            existingTables.add(cursor.getString(0))
        }
        cursor.close()

        // Sauvegarder les données existantes si les tables existent
        if (existingTables.contains("playlists")) {
            database.execSQL("CREATE TABLE IF NOT EXISTS playlists_backup AS SELECT * FROM playlists")
        }
        if (existingTables.contains("playlist_track_cross_ref")) {
            database.execSQL("CREATE TABLE IF NOT EXISTS playlist_track_cross_ref_backup AS SELECT * FROM playlist_track_cross_ref")
        }
        if (existingTables.contains("quizzes")) {
            database.execSQL("CREATE TABLE IF NOT EXISTS quizzes_backup AS SELECT * FROM quizzes")
        }
        if (existingTables.contains("tracks")) {
            database.execSQL("CREATE TABLE IF NOT EXISTS tracks_backup AS SELECT * FROM tracks")
        }

        // Supprimer les anciennes tables si elles existent
        database.execSQL("DROP TABLE IF EXISTS playlists")
        database.execSQL("DROP TABLE IF EXISTS playlist_track_cross_ref")
        database.execSQL("DROP TABLE IF EXISTS quizzes")
        database.execSQL("DROP TABLE IF EXISTS tracks")

        // Créer les tables dans le bon ordre (d'abord les tables indépendantes)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS tracks (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                artist TEXT NOT NULL,
                album TEXT NOT NULL,
                albumCover TEXT NOT NULL,
                previewUrl TEXT NOT NULL
            )
        """)

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS playlists (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                description TEXT
            )
        """)

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS playlist_track_cross_ref (
                playlistId INTEGER NOT NULL,
                trackId TEXT NOT NULL,
                position INTEGER NOT NULL,
                PRIMARY KEY(playlistId, trackId),
                FOREIGN KEY(playlistId) REFERENCES playlists(id) ON DELETE CASCADE,
                FOREIGN KEY(trackId) REFERENCES tracks(id) ON DELETE CASCADE
            )
        """)

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS quizzes (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                playlistId INTEGER,
                FOREIGN KEY(playlistId) REFERENCES playlists(id) ON DELETE SET NULL
            )
        """)

        // Restaurer les données si les tables de sauvegarde existent
        if (existingTables.contains("playlists")) {
            database.execSQL("INSERT INTO playlists (id, name, description) SELECT id, name, description FROM playlists_backup")
        }
        if (existingTables.contains("playlist_track_cross_ref")) {
            database.execSQL("INSERT INTO playlist_track_cross_ref (playlistId, trackId, position) SELECT playlistId, trackId, position FROM playlist_track_cross_ref_backup")
        }
        if (existingTables.contains("quizzes")) {
            database.execSQL("INSERT INTO quizzes (id, title, description, playlistId) SELECT id, title, description, playlistId FROM quizzes_backup")
        }
        if (existingTables.contains("tracks")) {
            database.execSQL("INSERT INTO tracks (id, title, artist, album, albumCover, previewUrl) SELECT id, title, artist, album, albumCover, previewUrl FROM tracks_backup")
        }

        // Supprimer les tables de sauvegarde
        database.execSQL("DROP TABLE IF EXISTS playlists_backup")
        database.execSQL("DROP TABLE IF EXISTS playlist_track_cross_ref_backup")
        database.execSQL("DROP TABLE IF EXISTS quizzes_backup")
        database.execSQL("DROP TABLE IF EXISTS tracks_backup")
    }
}

@Database(
    entities = [
        Playlist::class,
        Quiz::class,
        Track::class,
        PlaylistTrackCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun quizDao(): QuizDao
} 