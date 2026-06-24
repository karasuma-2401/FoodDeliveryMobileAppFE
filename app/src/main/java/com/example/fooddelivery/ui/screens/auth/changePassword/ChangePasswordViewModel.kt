package com.example.fooddelivery.ui.screens.auth.changePassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.usecase.ChangePasswordUseCase
import com.example.fooddelivery.domain.usecase.ValidateAuthInputUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePasswordState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ChangePasswordEvent {
    data class CurrentPasswordChanged(val value: String) : ChangePasswordEvent
    data class NewPasswordChanged(val value: String) : ChangePasswordEvent
    data class ConfirmPasswordChanged(val value: String) : ChangePasswordEvent
    object SaveClicked : ChangePasswordEvent
    object ErrorDismissed : ChangePasswordEvent
    object ResetSuccessState : ChangePasswordEvent
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val validateInputUseCase: ValidateAuthInputUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ChangePasswordState())
    val state: StateFlow<ChangePasswordState> = _state.asStateFlow()

    fun onEvent(event: ChangePasswordEvent) {
        when (event) {
            is ChangePasswordEvent.CurrentPasswordChanged -> {
                _state.update { it.copy(currentPassword = event.value, currentPasswordError = null) }
            }
            is ChangePasswordEvent.NewPasswordChanged -> {
                _state.update { it.copy(newPassword = event.value, newPasswordError = null) }
            }
            is ChangePasswordEvent.ConfirmPasswordChanged -> {
                _state.update { it.copy(confirmPassword = event.value, confirmPasswordError = null) }
            }
            ChangePasswordEvent.SaveClicked -> changePassword()
            ChangePasswordEvent.ErrorDismissed -> _state.update { it.copy(errorMessage = null) }
            ChangePasswordEvent.ResetSuccessState -> _state.update { it.copy(isSuccess = false) }
        }
    }

    private fun validateInput(): Boolean {
        val currentState = _state.value
        val currentPassError = if (currentState.currentPassword.isBlank()) "Current password is required" else null
        val newPassError = when {
            currentState.newPassword == currentState.currentPassword -> "New password must be different from current password"
            else -> validateInputUseCase.validatePassword(currentState.newPassword)
        }
        val confirmPassError = validateInputUseCase.validateConfirmPassword(currentState.newPassword, currentState.confirmPassword)

        val hasError = listOf(currentPassError, newPassError, confirmPassError).any { it != null }

        if (hasError) {
            _state.update {
                it.copy(
                    currentPasswordError = currentPassError,
                    newPasswordError = newPassError,
                    confirmPasswordError = confirmPassError
                )
            }
        }
        return !hasError
    }

    private fun changePassword() {
        if (!validateInput()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = changePasswordUseCase(
                currentPass = _state.value.currentPassword,
                newPass = _state.value.newPassword
            )
            result.onSuccess {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to change password") }
            }
        }
    }
}
