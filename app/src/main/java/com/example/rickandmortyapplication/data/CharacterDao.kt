package com.example.rickandmortyapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {

    @Query("SELECT * FROM characters ORDER BY id ASC")
    fun getAllCharactersFlow(): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters ORDER BY id ASC")
    suspend fun getAllCharacters(): List<CharacterEntity>

    @Query("SELECT * FROM characters WHERE id = :characterId")
    suspend fun getCharacterById(characterId: Int): CharacterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<CharacterEntity>)

    @Update
    suspend fun updateCharacter(character: CharacterEntity)

    @Delete
    suspend fun deleteCharacter(character: CharacterEntity)

    @Query("DELETE FROM characters")
    suspend fun deleteAllCharacters()

    @Query("SELECT COUNT(*) FROM characters")
    suspend fun getCharactersCount(): Int
}