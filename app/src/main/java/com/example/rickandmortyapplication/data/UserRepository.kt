package com.example.rickandmortyapplication.data

import android.content.Context
import com.example.rickandmortyapplication.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(context: Context) {

    private val userDao: UserDao = AppDatabase.getDatabase(context).userDao()

    suspend fun addUser(user: User) = withContext(Dispatchers.IO) {
        userDao.insertUser(user.toEntity())
    }

    suspend fun getUser(login: String, pass: String): User? = withContext(Dispatchers.IO) {
        userDao.getUser(login, pass)?.toUser()
    }

    suspend fun getUserByLogin(login: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserByLogin(login)?.toUser()
    }

    suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        userDao.getAllUsers().map { it.toUser() }
    }

    suspend fun deleteAllUsers() = withContext(Dispatchers.IO) {
        userDao.deleteAllUsers()
    }
}