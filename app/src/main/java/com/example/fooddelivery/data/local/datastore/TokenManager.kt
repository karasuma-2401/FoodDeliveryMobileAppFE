package com.example.fooddelivery.data.local.datastore

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
import java.io.File
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

    private val securePrefs: SharedPreferences = try {
        createEncryptedSharedPreferences()
    } catch (e: Exception) {
        Log.e("TokenManager", "Error creating EncryptedSharedPreferences, clearing and retrying", e)
        // Clear the corrupted SharedPreferences
        context.deleteSharedPreferences("secure_user_prefs")
        createEncryptedSharedPreferences()
    }

    private fun createEncryptedSharedPreferences(): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            "secure_user_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val ACCESS_TOKEN = "access_token"
        private const val REFRESH_TOKEN = "refresh_token"
        val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me")
        val PHONE_KEY = stringPreferencesKey("saved_phone")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        val USER_ID_KEY = intPreferencesKey("user_id")
        val USER_ROLES_KEY = stringPreferencesKey("user_roles")
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
        securePrefs.edit()
            .putString(ACCESS_TOKEN, accessToken)
            .putString(REFRESH_TOKEN, refreshToken)
            .commit()

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

    suspend fun saveMeInfo(id: Int, email: String, roles: List<String>) {
        context.userPrefDataStore.edit { preferences ->
            preferences[USER_ID_KEY] = id
            preferences[USER_EMAIL_KEY] = email
            preferences[USER_ROLES_KEY] = roles.joinToString(",")
        }
    }

    suspend fun savePhone(phone: String) {
        context.userPrefDataStore.edit { preferences ->
            preferences[PHONE_KEY] = phone
        }
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        securePrefs.edit()
            .putString(ACCESS_TOKEN, accessToken)
            .putString(REFRESH_TOKEN, refreshToken)
            .commit()
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
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_ROLES_KEY)
            preferences.remove(RESTAURANT_ID_KEY)
        }
    }

    fun getAccessTokenSync(): String? = securePrefs.getString(ACCESS_TOKEN, null)

    fun getRefreshTokenSync(): String? = securePrefs.getString(REFRESH_TOKEN, null)

    fun bearerToken(): String? = getAccessTokenSync()?.takeIf { it.isNotBlank() }?.let { "Bearer $it" }

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

    val getUserId: Flow<Int?> = context.userPrefDataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    val getUserRoles: Flow<List<String>> = context.userPrefDataStore.data.map { preferences ->
        preferences[USER_ROLES_KEY]
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }
    
    val getPhone: Flow<String?> = context.userPrefDataStore.data.map { preferences ->
        preferences[PHONE_KEY]
    }

    val getRememberMe: Flow<Boolean> = context.userPrefDataStore.data.map { preferences ->
        preferences[REMEMBER_ME_KEY] ?: false
    }

    val getRestaurantId: Flow<Int?> = context.userPrefDataStore.data.map { preferences ->
        preferences[RESTAURANT_ID_KEY]
    }
}
