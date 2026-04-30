package com.example.fooddelivery.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.usecase.GetUserProfileUseCase
import com.example.fooddelivery.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ProfileState(
    val user: User = User(),
    val isLoading: Boolean = false,
    val isLogoutSuccess: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ProfileEvent {
    object LoadUserProfile : ProfileEvent
    object LogoutClicked : ProfileEvent
    object ErrorDismissed : ProfileEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        onEvent(ProfileEvent.LoadUserProfile)
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.LoadUserProfile -> loadUserProfile()
            ProfileEvent.LogoutClicked -> logout()
            ProfileEvent.ErrorDismissed -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadUserProfile() {
        if (_state.value.isLoading) return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = withContext(Dispatchers.IO) {
                getUserProfileUseCase()
            }
            result.onSuccess { user ->
                _state.update { it.copy(user = user, isLoading = false) }
            }.onFailure { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load user profile"
                    )
                }
            }
        }
    }
    
    private fun logout() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = withContext(Dispatchers.IO) {
                logoutUseCase()
            }
            result.onSuccess {
                _state.update { it.copy(isLoading = false, isLogoutSuccess = true) }
            }.onFailure {
                _state.update { it.copy(isLoading = false, errorMessage = "Logout failed") }
            }
        }
    }
}
