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
    @ApplicationContext private val context: Context
) {
    companion object {
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me")
        val PHONE_KEY = stringPreferencesKey("saved_phone")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    suspend fun saveAuthData(
        accessToken: String,
        refreshToken: String,
        phone: String,
        name: String,
        email: String,
        rememberMe: Boolean
    ) {
        context.userPrefDataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
            preferences[USER_NAME_KEY] = name
            preferences[USER_EMAIL_KEY] = email
            
            if (rememberMe) {
                preferences[PHONE_KEY] = phone
                preferences[REMEMBER_ME_KEY] = true
            } else {
                preferences.remove(PHONE_KEY)
                preferences[REMEMBER_ME_KEY] = false
            }
        }
    }

    suspend fun clearAuthData() {
        context.userPrefDataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(USER_EMAIL_KEY)
        }
    }

    val getAccessToken: Flow<String?> = context.userPrefDataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN_KEY]
    }

    val getRefreshToken: Flow<String?> = context.userPrefDataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN_KEY]
    }

    val getUserName: Flow<String?> = context.userPrefDataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }
}