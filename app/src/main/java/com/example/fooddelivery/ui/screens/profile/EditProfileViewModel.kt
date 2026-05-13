package com.example.fooddelivery.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.usecase.GetUserProfileUseCase
import com.example.fooddelivery.domain.usecase.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileState(
    val user: User = User(),
    val selectedImageUri: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

sealed interface EditProfileEvent {
    data class FullNameChanged(val name: String) : EditProfileEvent
    data class EmailChanged(val email: String) : EditProfileEvent
    data class PhoneChanged(val phone: String) : EditProfileEvent
    data class BioChanged(val bio: String) : EditProfileEvent
    data class ProfileImageChanged(val uri: String) : EditProfileEvent
    object SaveClicked : EditProfileEvent
    object ErrorDismissed : EditProfileEvent
    object ResetSuccessState : EditProfileEvent
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(EditProfileState())
    val state: StateFlow<EditProfileState> = _state.asStateFlow()

    init {
        loadUserProfile()
    }

    fun onEvent(event: EditProfileEvent) {
        when (event) {
            is EditProfileEvent.FullNameChanged -> {
                _state.update { it.copy(user = it.user.copy(fullName = event.name)) }
            }
            is EditProfileEvent.EmailChanged -> {
                _state.update { it.copy(user = it.user.copy(email = event.email)) }
            }
            is EditProfileEvent.PhoneChanged -> {
                _state.update { it.copy(user = it.user.copy(phone = event.phone)) }
            }
            is EditProfileEvent.BioChanged -> {
                _state.update { it.copy(user = it.user.copy(bio = event.bio)) }
            }
            is EditProfileEvent.ProfileImageChanged -> {
                _state.update { it.copy(selectedImageUri = event.uri) }
            }
            EditProfileEvent.SaveClicked -> {
                saveProfile()
            }
            EditProfileEvent.ErrorDismissed -> {
                _state.update { it.copy(errorMessage = null) }
            }
            EditProfileEvent.ResetSuccessState -> {
                _state.update { it.copy(isSuccess = false) }
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            delay(1000)
            val mockUser = User(
                id = "user123",
                fullName = "Lê Minh",
                email = "leminh@example.com",
                phone = "0123456789",
                bio = "I love food delivery!",
                profileImage = null
            )
            
            _state.update { it.copy(isLoading = false, user = mockUser) }
        }
    }

    private fun saveProfile() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            // Mocking API call for saving
            delay(1500)
            
            _state.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }
}
