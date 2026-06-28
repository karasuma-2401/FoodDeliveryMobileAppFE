package com.example.fooddelivery.ui.screens.customer.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.usecase.GetUserProfileUseCase
import com.example.fooddelivery.domain.usecase.UpdateUserProfileUseCase
import com.example.fooddelivery.domain.usecase.ValidateAuthInputUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileState(
    val user: User = User(),
    val initialUser: User = User(),
    val fullNameError: String? = null,
    val selectedImageUri: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val isChanged: Boolean
        get() = user.fullName != initialUser.fullName ||
                selectedImageUri != null
}

sealed interface EditProfileEvent {
    data class FullNameChanged(val name: String) : EditProfileEvent
    data class ProfileImageChanged(val uri: String) : EditProfileEvent
    object SaveClicked : EditProfileEvent
    object ErrorDismissed : EditProfileEvent
    object ResetSuccessState : EditProfileEvent
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val validateInputUseCase: ValidateAuthInputUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(EditProfileState())
    val state: StateFlow<EditProfileState> = _state.asStateFlow()

    init {
        loadUserProfile()
    }

    fun onEvent(event: EditProfileEvent) {
        when (event) {
            is EditProfileEvent.FullNameChanged -> {
                _state.update { it.copy(user = it.user.copy(fullName = event.name), fullNameError = null) }
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
            getUserProfileUseCase().onSuccess { user ->
                _state.update { it.copy(isLoading = false, user = user, initialUser = user) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun validate(): Boolean {
        val nameError = validateInputUseCase.validateFullName(_state.value.user.fullName)

        if (nameError != null) {
            _state.update { it.copy(fullNameError = nameError) }
            return false
        }
        return true
    }

    private fun saveProfile() {
        if (!validate()) return
        if (_state.value.isLoading || !_state.value.isChanged) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            updateUserProfileUseCase(_state.value.user, _state.value.selectedImageUri).onSuccess { updatedUser ->
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        isSuccess = true, 
                        user = updatedUser, 
                        initialUser = updatedUser,
                        selectedImageUri = null
                    ) 
                }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
