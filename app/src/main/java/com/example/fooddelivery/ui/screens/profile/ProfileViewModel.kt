package com.example.fooddelivery.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.domain.usecase.GetUserProfileUseCase
import com.example.fooddelivery.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val user: User = User(),
    val cartItemCount: Int = 0,
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
    private val logoutUseCase: LogoutUseCase,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        onEvent(ProfileEvent.LoadUserProfile)
        observeCart()
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
            ProfileEvent.LoadUserProfile -> loadUserProfile()
            ProfileEvent.LogoutClicked -> logout()
            ProfileEvent.ErrorDismissed -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadUserProfile() {
        if (_state.value.isLoading) return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Mocking API call
            delay(1000)
            val mockUser = User(
                id = "user123",
                fullName = "Lê Minh",
                email = "leminh@example.com",
                phone = "0123456789",
                bio = "I love food delivery!",
                profileImage = null
            )
            
            _state.update { it.copy(user = mockUser, isLoading = false) }
        }
    }
    
    private fun logout() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Mocking API call
            delay(1000)

            _state.update { it.copy(isLoading = false, isLogoutSuccess = true) }
        }
    }
}
