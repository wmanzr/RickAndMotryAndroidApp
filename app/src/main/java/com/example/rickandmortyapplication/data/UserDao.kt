package com.example.rickandmortyapplication.data

import androidx.room.*

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE login = :login AND pass = :pass LIMIT 1")
    suspend fun getUser(login: String, pass: String): UserEntity?

    @Query("SELECT * FROM users WHERE login = :login LIMIT 1")
    suspend fun getUserByLogin(login: String): UserEntity?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}