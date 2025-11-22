package com.example.rickandmortyapplication.data

import android.content.Context
import com.example.rickandmortyapplication.model.Character
import com.example.rickandmortyapplication.model.CharacterResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

class RickAndMortyRepository(context: Context? = null) {
    private val api: RickAndMortyApi

    init {
        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://rickandmortyapi.com/api/")
            .addConverterFactory(Json.asConverterFactory(contentType))
            .build()

        api = retrofit.create(RickAndMortyApi::class.java)
    }

    private val characterDao: CharacterDao? = context?.let {
        AppDatabase.getDatabase(it).characterDao()
    }

    suspend fun getCharacters(page: Int): Response<CharacterResponse> = withContext(Dispatchers.IO) {
        api.getCharacters(page)
    }

    fun getAllCharactersFlow(): Flow<List<Character>>? {
        return characterDao?.getAllCharactersFlow()?.map { entities ->
            entities.toCharacterList()
        }
    }

    suspend fun getAllCharactersFromDb(): List<Character> = withContext(Dispatchers.IO) {
        characterDao?.getAllCharacters()?.toCharacterList() ?: emptyList()
    }

    suspend fun insertCharactersToDb(characters: List<Character>) = withContext(Dispatchers.IO) {
        characterDao?.insertCharacters(characters.toEntityList())
    }

    suspend fun deleteAllCharactersFromDb() = withContext(Dispatchers.IO) {
        characterDao?.deleteAllCharacters()
    }

    suspend fun getCharactersCount(): Int = withContext(Dispatchers.IO) {
        characterDao?.getCharactersCount() ?: 0
    }
}