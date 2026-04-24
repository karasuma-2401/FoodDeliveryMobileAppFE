package com.example.fooddelivery.ui.screens.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.usecase.GetUserProfileUseCase
import com.example.fooddelivery.domain.usecase.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileState(
    val user: User = User(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {

    private val _state = mutableStateOf(EditProfileState())
    val state: State<EditProfileState> = _state

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val result = getUserProfileUseCase()
            result.onSuccess { user ->
                _state.value = _state.value.copy(isLoading = false, user = user)
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Failed to load profile"
                )
            }
        }
    }

    fun onFullNameChange(name: String) {
        _state.value = _state.value.copy(user = _state.value.user.copy(fullName = name))
    }

    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(user = _state.value.user.copy(email = email))
    }

    fun onPhoneChange(phone: String) {
        _state.value = _state.value.copy(user = _state.value.user.copy(phone = phone))
    }

    fun onBioChange(bio: String) {
        _state.value = _state.value.copy(user = _state.value.user.copy(bio = bio))
    }

    fun onProfileImageChange(uri: String) {
        _state.value = _state.value.copy(user = _state.value.user.copy(profileImage = uri))
    }

    fun saveProfile() {
        if (_state.value.isLoading) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val result = updateUserProfileUseCase(_state.value.user)
            result.onSuccess {
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Failed to update profile"
                )
            }
        }
    }

    fun clearSuccessState() {
        _state.value = _state.value.copy(isSuccess = false)
    }
}