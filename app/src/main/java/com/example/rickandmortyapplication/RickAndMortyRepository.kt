package com.example.rickandmortyapplication

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

class RickAndMortyRepository {
    private val api: RickAndMortyApi

    init {
        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://rickandmortyapi.com/api/")
            .addConverterFactory(Json.asConverterFactory(contentType))
            .build()

        api = retrofit.create(RickAndMortyApi::class.java)
    }

    suspend fun getCharacters(page: Int): CharacterResponse? = withContext(Dispatchers.IO) {
        try {
            api.getCharacters(page)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}