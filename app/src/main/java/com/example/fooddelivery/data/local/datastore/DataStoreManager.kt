package com.example.fooddelivery.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dfood_preferences")

class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        val SELECTED_ADDRESS_ID_KEY = intPreferencesKey("selected_address_id")
    }

    // Onboarding
    fun readOnboardingState(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] ?: false
        }
    }

    suspend fun saveOnboardingState(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = completed
        }
    }
    // Dark Mode
    fun readDarkModeState(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }
    }
    suspend fun saveDarkModeState(isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDarkMode
        }
    }

    // Notifications
    fun readNotificationsState(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] ?: true
        }
    }

    suspend fun saveNotificationsState(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    fun readSelectedAddressId(): Flow<Int?> {
        return context.dataStore.data.map { preferences ->
            preferences[SELECTED_ADDRESS_ID_KEY]
        }
    }

    suspend fun saveSelectedAddressId(addressId: Int) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_ADDRESS_ID_KEY] = addressId
        }
    }
}