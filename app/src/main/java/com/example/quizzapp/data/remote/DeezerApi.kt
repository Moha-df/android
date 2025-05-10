package com.example.quizzapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DeezerApi {
    @GET("search")
    suspend fun searchTracks(
        @Query("q") query: String
    ): SearchResponse

    @GET("track/{id}")
    suspend fun getTrack(
        @Path("id") id: String
    ): TrackResponse
}

data class SearchResponse(
    val data: List<TrackResponse>
)

data class TrackResponse(
    val id: String,
    val title: String,
    val artist: Artist,
    val album: Album,
    val preview: String
)

data class Artist(
    val name: String
)

data class Album(
    val cover_medium: String
) 