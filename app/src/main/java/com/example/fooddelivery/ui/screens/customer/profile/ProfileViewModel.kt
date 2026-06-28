package com.example.fooddelivery.ui.screens.customer.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.datastore.DataStoreManager
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.domain.repository.RestaurantRepository
import com.example.fooddelivery.domain.repository.AuthRepository
import com.example.fooddelivery.domain.repository.NotificationRepository
import com.example.fooddelivery.domain.usecase.GetUserProfileUseCase
import com.example.fooddelivery.domain.usecase.LogoutUseCase
import com.example.fooddelivery.domain.usecase.RegisterDeviceTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.first

data class ProfileState(
    val user: User? = null,
    val cartItemCount: Int = 0,
    val unreadNotificationCount: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLogoutSuccess: Boolean = false,
    val isBusinessRegisterSuccess: Boolean = false,
    val errorMessage: String? = null,
    val isDarkMode: Boolean = false,
    val isNotificationsEnabled: Boolean = true
)

sealed interface ProfileEvent {
    object LoadUserProfile : ProfileEvent
    object RefreshUserProfile : ProfileEvent
    object LogoutClicked : ProfileEvent
    object RegisterBusinessClicked : ProfileEvent
    object BusinessRegisterReset : ProfileEvent
    object ErrorDismissed : ProfileEvent
    data class ToggleDarkMode(val enabled: Boolean) : ProfileEvent
    data class ToggleNotifications(val enabled: Boolean) : ProfileEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val cartRepository: CartRepository,
    private val notificationRepository: NotificationRepository,
    private val dataStoreManager: DataStoreManager,
    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase,
    private val restaurantRepository: RestaurantRepository,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        if (_state.value.user == null) {
            onEvent(ProfileEvent.LoadUserProfile)
        }
        observeCart()
        observeNotifications()
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            dataStoreManager.readDarkModeState().collectLatest { isDark ->
                _state.update { it.copy(isDarkMode = isDark) }
            }
        }
        viewModelScope.launch {
            dataStoreManager.readNotificationsState().collectLatest { enabled ->
                _state.update { it.copy(isNotificationsEnabled = enabled) }
            }
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collectLatest { items ->
                val totalCount = items.sumOf { it.quantity }
                _state.update { it.copy(cartItemCount = totalCount) }
            }
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            notificationRepository.getUnreadCountFlow().collectLatest { localCount ->
                _state.update { it.copy(unreadNotificationCount = localCount) }
            }
        }
        refreshUnreadCount()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.LoadUserProfile -> loadUserProfile(isManualRefresh = false)
            ProfileEvent.RefreshUserProfile -> {
                loadUserProfile(isManualRefresh = true)
                refreshUnreadCount()
            }
            ProfileEvent.LogoutClicked -> logout()
            ProfileEvent.ErrorDismissed -> _state.update { it.copy(errorMessage = null) }
            ProfileEvent.RegisterBusinessClicked -> registerBusinessAccount()
            ProfileEvent.BusinessRegisterReset -> _state.update { it.copy(isBusinessRegisterSuccess = false) }
            is ProfileEvent.ToggleDarkMode -> {
                viewModelScope.launch {
                    dataStoreManager.saveDarkModeState(event.enabled)
                }
            }
            is ProfileEvent.ToggleNotifications -> {
                viewModelScope.launch {
                    dataStoreManager.saveNotificationsState(event.enabled)
                    if (event.enabled) {
                        registerDeviceTokenUseCase()
                    }
                }
            }
        }
    }
    private fun registerBusinessAccount() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            restaurantRepository.registerBusiness()
                .onSuccess { response ->
                    if (response.requiresTokenRefresh) {

                        val currentSavedToken = tokenManager.getRefreshTokenSync()
                        if (!currentSavedToken.isNullOrEmpty()) {

                            authRepository.refreshToken(currentSavedToken)
                                .onSuccess { loginResponse ->

                                    val newAccessToken = loginResponse.data?.accessToken
                                    val newRefreshToken = loginResponse.data?.refreshToken

                                    if (!newAccessToken.isNullOrEmpty() && !newRefreshToken.isNullOrEmpty()) {
                                        viewModelScope.launch {
                                            tokenManager.updateTokens(
                                                accessToken = newAccessToken,
                                                refreshToken = newRefreshToken
                                            )
                                        }

                                        _state.update { it.copy(isLoading = false, isBusinessRegisterSuccess = true) }
                                    } else {
                                        _state.update { it.copy(isLoading = false, errorMessage = "Error: Could not extract token from server response") }
                                    }
                                }
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isBusinessRegisterSuccess = true
                            )
                        }
                    }
                }
        }
    }

    private fun refreshUnreadCount() {
        viewModelScope.launch {
            notificationRepository.getUnreadCount().onSuccess { count ->
                _state.update { it.copy(unreadNotificationCount = count) }
            }
        }
    }

    private fun loadUserProfile(isManualRefresh: Boolean) {
        if (_state.value.isLoading || _state.value.isRefreshing) return
        
        if (!isManualRefresh && _state.value.user != null) return

        viewModelScope.launch {
            if (isManualRefresh) {
                _state.update { it.copy(isRefreshing = true) }
            } else {
                _state.update { it.copy(isLoading = true) }
            }
            
            getUserProfileUseCase().onSuccess { user ->
                _state.update { it.copy(user = user, isLoading = false, isRefreshing = false) }
            }.onFailure { error ->
                _state.update { it.copy(errorMessage = error.message, isLoading = false, isRefreshing = false) }
            }
        }
    }
    
    private fun logout() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            logoutUseCase().onSuccess {
                _state.update { it.copy(isLoading = false, isLogoutSuccess = true) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }
}
