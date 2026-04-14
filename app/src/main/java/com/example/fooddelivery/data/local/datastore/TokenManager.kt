package com.example.fooddelivery.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPrefDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class TokenManager @Inject constructor (
    @param:ApplicationContext private val context: Context
) {
    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me")
        val PHONE_KEY = stringPreferencesKey("saved_phone")
    }

    suspend fun saveAuthData(token: String, phone: String, rememberMe: Boolean) {
        context.userPrefDataStore.edit { preferences ->
            if (rememberMe) {
                preferences[TOKEN_KEY] = token
                preferences[PHONE_KEY] = phone
                preferences[REMEMBER_ME_KEY] = true
            } else {
                preferences.remove(TOKEN_KEY)
                preferences[REMEMBER_ME_KEY] = false
            }
        }
    }
    val getToken: Flow<String?> = context.userPrefDataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }
}