package com.example.quizzapp.data.local

import androidx.room.TypeConverter
import com.example.quizzapp.data.model.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTrackList(value: List<Track>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toTrackList(value: String): List<Track> {
        val listType = object : TypeToken<List<Track>>() {}.type
        return gson.fromJson(value, listType)
    }
} 