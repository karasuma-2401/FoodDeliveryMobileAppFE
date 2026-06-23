package com.example.fooddelivery.data.local.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
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
    // SECURITY ENHANCEMENT: Sử dụng EncryptedSharedPreferences cho dữ liệu nhạy cảm (Tokens)
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_user_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val ACCESS_TOKEN = "access_token"
        private const val REFRESH_TOKEN = "refresh_token"
        val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me")
        val PHONE_KEY = stringPreferencesKey("saved_phone")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        val RESTAURANT_ID_KEY = intPreferencesKey("restaurant_id")
    }

    suspend fun saveAuthData(
        accessToken: String,
        refreshToken: String,
        phone: String,
        name: String,
        email: String,
        rememberMe: Boolean
    ) {
        // Lưu token vào kho lưu trữ được mã hóa
        securePrefs.edit().apply {
            putString(ACCESS_TOKEN, accessToken)
            putString(REFRESH_TOKEN, refreshToken)
            apply()
        }

        // Các thông tin không nhạy cảm vẫn có thể lưu ở DataStore
        context.userPrefDataStore.edit { preferences ->
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

    suspend fun saveRestaurantId(restaurantId: Int) {
        context.userPrefDataStore.edit { preferences ->
            preferences[RESTAURANT_ID_KEY] = restaurantId
        }
    }

    suspend fun clearAuthData() {
        securePrefs.edit().clear().apply()
        context.userPrefDataStore.edit { preferences ->
            preferences.remove(USER_NAME_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(RESTAURANT_ID_KEY)
        }
    }

    // Lấy token từ kho mã hóa
    val getAccessToken: Flow<String?> = context.userPrefDataStore.data.map { 
        securePrefs.getString(ACCESS_TOKEN, null) 
    }

    val getRefreshToken: Flow<String?> = context.userPrefDataStore.data.map { 
        securePrefs.getString(REFRESH_TOKEN, null) 
    }

    val getUserName: Flow<String?> = context.userPrefDataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    val getUserEmail: Flow<String?> = context.userPrefDataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }
    
    val getPhone: Flow<String?> = context.userPrefDataStore.data.map { preferences ->
        preferences[PHONE_KEY]
    }

    val getRestaurantId: Flow<Int?> = context.userPrefDataStore.data.map { preferences ->
        preferences[RESTAURANT_ID_KEY]
    }
}
