package com.example.rickandmortyapplication.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore by preferencesDataStore(name = "user_settings")
object DataStoreManager {
    fun getDataStore(context: Context) = context.dataStore
}