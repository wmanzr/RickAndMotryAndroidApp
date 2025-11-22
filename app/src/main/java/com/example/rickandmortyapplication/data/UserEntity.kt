package com.example.rickandmortyapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val login: String,
    val email: String,
    val number: String,
    val pass: String
)