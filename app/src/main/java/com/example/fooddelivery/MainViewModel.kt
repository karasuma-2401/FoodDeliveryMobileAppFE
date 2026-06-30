package com.example.fooddelivery

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.datastore.DataStoreManager
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.domain.usecase.RegisterDeviceTokenUseCase
import com.example.fooddelivery.domain.usecase.SyncNotificationsUseCase
import com.example.fooddelivery.domain.usecase.ValidateSessionUseCase
import com.example.fooddelivery.ui.navigation.LoginRoute
import com.example.fooddelivery.ui.navigation.OnboardingRoute
import com.example.fooddelivery.ui.navigation.toStartDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val tokenManager: TokenManager,
    private val validateSessionUseCase: ValidateSessionUseCase,
    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase,
    private val syncNotificationsUseCase: SyncNotificationsUseCase,
) : ViewModel() {
    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _startDestination = mutableStateOf<Any>(OnboardingRoute)
    val startDestination: State<Any> = _startDestination

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    init {
        viewModelScope.launch {
            val hasCompletedOnboarding = dataStoreManager.readOnboardingState().first()
            val token = tokenManager.getAccessToken.first()

            if (!hasCompletedOnboarding) {
                _startDestination.value = OnboardingRoute
            } else if (!token.isNullOrBlank()) {
                validateSessionUseCase().fold(
                    onSuccess = { me ->
                        _startDestination.value = me.toStartDestination()
                        launch { registerDeviceTokenUseCase() }
                        launch { syncNotificationsUseCase() }
                    },
                    onFailure = {
                        _startDestination.value = LoginRoute
                    }
                )
            } else {
                _startDestination.value = LoginRoute
            }
            delay(3000)
            _isLoading.value = false
        }

        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            dataStoreManager.readDarkModeState().collectLatest { isDark ->
                _isDarkMode.value = isDark
            }
        }
    }
}
