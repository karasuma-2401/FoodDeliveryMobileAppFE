package com.example.fooddelivery.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.datastore.DataStoreManager
import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.domain.usecase.GetUserProfileUseCase
import com.example.fooddelivery.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val user: User? = null,
    val cartItemCount: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLogoutSuccess: Boolean = false,
    val errorMessage: String? = null,
    val isDarkMode: Boolean = false,
    val isNotificationsEnabled: Boolean = true
)

sealed interface ProfileEvent {
    object LoadUserProfile : ProfileEvent
    object RefreshUserProfile : ProfileEvent
    object LogoutClicked : ProfileEvent
    object ErrorDismissed : ProfileEvent
    data class ToggleDarkMode(val enabled: Boolean) : ProfileEvent
    data class ToggleNotifications(val enabled: Boolean) : ProfileEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val cartRepository: CartRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        if (_state.value.user == null) {
            onEvent(ProfileEvent.LoadUserProfile)
        }
        observeCart()
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
            cartRepository.getCartItems().collectLatest { items ->
                val totalCount = items.sumOf { it.quantity }
                _state.update { it.copy(cartItemCount = totalCount) }
            }
        }
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.LoadUserProfile -> loadUserProfile(isManualRefresh = false)
            ProfileEvent.RefreshUserProfile -> loadUserProfile(isManualRefresh = true)
            ProfileEvent.LogoutClicked -> logout()
            ProfileEvent.ErrorDismissed -> _state.update { it.copy(errorMessage = null) }
            is ProfileEvent.ToggleDarkMode -> {
                viewModelScope.launch {
                    dataStoreManager.saveDarkModeState(event.enabled)
                }
            }
            is ProfileEvent.ToggleNotifications -> {
                viewModelScope.launch {
                    dataStoreManager.saveNotificationsState(event.enabled)
                }
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
