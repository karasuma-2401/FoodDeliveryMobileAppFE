package com.example.fooddelivery.ui.screens.profile

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.usecase.GetUserProfileUseCase
import com.example.fooddelivery.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ProfileState(
    val user: User = User(),
    val isLoading: Boolean = false,
    val isLogoutSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val _state = mutableStateOf(ProfileState())
    val state: State<ProfileState> = _state

    init {
       loadUserProfile()
    }
    
    fun loadUserProfile() {
        if (_state.value.isLoading) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = withContext(Dispatchers.IO) {
                getUserProfileUseCase()
            }
            result.onSuccess { user ->
                _state.value = _state.value.copy(user = user, isLoading = false)
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Failed to load user profile"
                )
            }
        }
    }
    
    fun logout() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = withContext(Dispatchers.IO) {
                logoutUseCase()
            }
            result.onSuccess {
                _state.value = _state.value.copy(isLoading = false, isLogoutSuccess = true)
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Logout failed")
            }
        }
    }
}
