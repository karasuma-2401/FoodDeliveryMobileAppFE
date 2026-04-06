package com.example.fooddelivery.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.datastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    fun saveOnboardingState(completed: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveOnboardingState(completed = completed)
        }
    }
}