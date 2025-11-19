package com.example.rickandmortyapplication.data

import com.example.rickandmortyapplication.model.CharacterResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RickAndMortyApi {
    @GET("character")
    suspend fun getCharacters(@Query("page") page: Int): Response<CharacterResponse>
}